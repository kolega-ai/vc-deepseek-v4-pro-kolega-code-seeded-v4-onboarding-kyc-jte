package com.meridianbank.onboarding.auth;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.AttemptTrackerService;
import com.meridianbank.onboarding.service.KycTierService;
import com.meridianbank.onboarding.service.NotificationService;
import com.meridianbank.onboarding.service.OutboxEmailService;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.config.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * The in-module auth component: registration with dual-channel OTP verification, credentials
 * (BCrypt), login with lockout, logout/session lifecycle, single-use time-bound password reset
 * and step-up protected password change. JWTs are issued and validated in this module.
 */
@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final JourneyStepRepository journeyStepRepository;
    private final BranchRepository branchRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuditService auditService;
    private final AttemptTrackerService attemptTrackerService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final AppProperties props;
    private final String webUrl;

    public AuthService(UserAccountRepository userAccountRepository,
                       ApplicantRepository applicantRepository,
                       ApplicationRepository applicationRepository,
                       JourneyStepRepository journeyStepRepository,
                       BranchRepository branchRepository,
                       AuthSessionRepository authSessionRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService,
                       AuditService auditService,
                       AttemptTrackerService attemptTrackerService,
                       NotificationService notificationService,
                       OutboxEmailService outboxEmailService,
                       AppProperties props,
                       @Value("${app.web-url:http://localhost:8080}") String webUrl) {
        this.userAccountRepository = userAccountRepository;
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.journeyStepRepository = journeyStepRepository;
        this.branchRepository = branchRepository;
        this.authSessionRepository = authSessionRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.auditService = auditService;
        this.attemptTrackerService = attemptTrackerService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.props = props;
        this.webUrl = webUrl;
    }

    // ------------------------------------------------------------------ registration

    @Transactional
    public String register(String email, String mobile) {
        email = normalizeEmail(email);
        Validators.validateEmail(email);
        Validators.validateMobile(mobile);
        if (userAccountRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw ApiException.conflict("An account with this email already exists");
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setRole(Role.PROSPECT);
        user.setStatus(UserStatus.ACTIVE);
        user.setRegistrationTokenHash(null);
        user.setCreatedAt(Instant.now());

        Applicant applicant = new Applicant();
        applicant.setEmail(email);
        applicant.setMobile(mobile);
        applicant.setCreatedAt(Instant.now());

        // Branch assignment is server-side (round-robin), never client-supplied.
        java.util.List<Branch> branches = branchRepository.findAllByOrderByNameAsc();
        if (branches.isEmpty()) {
            throw new IllegalStateException("No branches configured");
        }
        long registered = applicationRepository.count() % branches.size();
        Branch branch = branches.get((int) registered);

        Application application = new Application();
        application.setReference(generateReference());
        application.setStatus(AppStatus.IN_PROGRESS);
        application.setCurrentStep(JourneyStepKey.REGISTERED);
        application.setBranchId(branch.getId());
        application.setDerivedKycTier(KycTierService.TIER_0);
        application.setCreatedAt(Instant.now());
        application.setLastActivityAt(Instant.now());

        user = userAccountRepository.save(user);
        applicant.setUserAccountId(user.getId());
        applicant = applicantRepository.save(applicant);
        application.setApplicantId(applicant.getId());
        applicationRepository.save(application);

        for (JourneyStepKey key : JourneyStepKey.values()) {
            JourneyStep step = new JourneyStep();
            step.setApplicationId(application.getId());
            step.setStepKey(key);
            step.setCompleted(false);
            journeyStepRepository.save(step);
        }

        String registrationToken = JwtService.randomToken();
        user.setRegistrationTokenHash(JwtService.sha256(registrationToken));
        userAccountRepository.save(user);
        auditService.recordUser(user, "REGISTRATION_STARTED", "Application", application.getReference(),
                null, "{\"email\":\"" + email + "\"}", "REGISTRATION");
        return registrationToken;
    }

    /** Read-only lookup for the pre-registration availability check (no side effects). */
    public boolean emailExists(String email) {
        return userAccountRepository.findByEmailIgnoreCase(normalizeEmail(email)).isPresent();
    }

    @Transactional
    public UserAccount userByRegistrationToken(String registrationToken) {
        return userAccountRepository.findByRegistrationTokenHash(JwtService.sha256(registrationToken))
                .orElseThrow(() -> ApiException.unauthorized("Registration session not found or expired"));
    }

    @Transactional
    public LoginResponse completeRegistration(String registrationToken, String password) {
        UserAccount user = userByRegistrationToken(registrationToken);
        Validators.validatePassword(password);
        if (user.getEmailVerifiedAt() == null) {
            throw ApiException.invalidState("Your email must be verified before creating a password");
        }
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRegistrationTokenHash(null);
        user.setFailedLoginCount(0);
        userAccountRepository.save(user);

        // Registration completes the registered step; contact_verified completes once the mobile
        // OTP is verified during the journey (both channels are prerequisites for document upload).
        Application application = applicationOf(user);
        completeStep(application, JourneyStepKey.REGISTERED, null);
        application.setCurrentStep(JourneyStepKey.CONTACT_VERIFIED);
        applicationRepository.save(application);

        auditService.recordUser(user, "REGISTRATION_COMPLETED", "Application", application.getReference(),
                "contact_verified", "in_progress", "REGISTRATION");
        outboxEmailService.send(user.getEmail(), "Welcome to Meridian Bank",
                "Welcome to Meridian Bank digital onboarding. Your application reference is "
                        + application.getReference() + ".", "welcome");
        notificationService.notify(user.getId(), NotificationType.INFO,
                "Registration complete", "Your application " + application.getReference()
                        + " has been created. Continue with your personal details.",
                "/journey/personal");
        return loginInternal(user);
    }

    // ------------------------------------------------------------------ login / logout

    @Transactional
    public LoginResponse login(String email, String password) {
        email = normalizeEmail(email);
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "BAD_CREDENTIALS", "Invalid email or password"));
        if (user.getPasswordHash() == null || user.isLocked(Instant.now())
                || user.getStatus() == UserStatus.DISABLED) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        String presented = password == null ? "" : password;
        if (!passwordEncoder.matches(presented, user.getPasswordHash())
                && !legacyDigestMatches(presented, user.getPasswordHash())) {
            Instant lockUntil = null;
            if (user.getFailedLoginCount() + 1 >= props.getAuth().getMaxFailedLogins()) {
                lockUntil = Instant.now().plus(java.time.Duration.ofMinutes(props.getAuth().getLoginLockMinutes()));
            }
            // Persisted in its own transaction so the rejection below cannot roll it back.
            attemptTrackerService.recordFailedLogin(user.getId(), lockUntil);
            auditService.recordUser(user, "LOGIN_FAILED", "UserAccount", user.getEmail(),
                    null, null, "PASSWORD");
            throw ApiException.unauthorized("Invalid email or password");
        }
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        return loginInternal(user);
    }

    private LoginResponse loginInternal(UserAccount user) {
        user.setLastLoginAt(Instant.now());
        userAccountRepository.save(user);
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        AuthSession session = new AuthSession();
        session.setId(sessionId);
        session.setUserAccountId(user.getId());
        session.setIssuedAt(now);
        session.setExpiresAt(now.plus(props.getJwt().getTtl()));
        authSessionRepository.save(session);
        String jwt = jwtService.issue(user, sessionId);
        auditService.recordUser(user, "LOGIN_SUCCESS", "UserAccount", user.getEmail(),
                null, null, "PASSWORD+JWT:" + sessionId);
        return new LoginResponse(jwt, user.getRole().getLabel(), user.getEmail());
    }

    @Transactional
    public void logout(UserAccount user, String sessionId) {
        authSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUserAccountId().equals(user.getId())) {
                session.setRevokedAt(Instant.now());
                authSessionRepository.save(session);
            }
        });
        auditService.recordUser(user, "LOGOUT", "UserAccount", user.getEmail(),
                null, null, "SESSION:" + sessionId);
    }

    // ------------------------------------------------------------------ password reset

    @Transactional
    public void requestPasswordReset(String email) {
        email = normalizeEmail(email);
        var found = userAccountRepository.findByEmailIgnoreCase(email);
        if (found.isEmpty() || found.get().getPasswordHash() == null) {
            // Uniform response: no account enumeration.
            return;
        }
        UserAccount user = found.get();
        String token = JwtService.randomToken();
        PasswordResetToken reset = new PasswordResetToken();
        reset.setUserAccountId(user.getId());
        reset.setTokenHash(JwtService.sha256(token));
        reset.setExpiresAt(Instant.now().plus(props.getAuth().getResetTokenTtl()));
        reset.setCreatedAt(Instant.now());
        resetTokenRepository.save(reset);
        outboxEmailService.send(user.getEmail(), "Reset your Meridian Bank password",
                "Use the link below to reset your password (valid for 30 minutes, single use):\n\n"
                        + webUrl + "/reset-password?token=" + token + "\n\n"
                        + "If you did not request this, ignore this email.",
                "password-reset");
        auditService.recordUser(user, "PASSWORD_RESET_REQUESTED", "UserAccount", user.getEmail(),
                null, null, "RESET_TOKEN");
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        Validators.validatePassword(newPassword);
        PasswordResetToken reset = resetTokenRepository.findByTokenHash(JwtService.sha256(token))
                .orElseThrow(() -> ApiException.unauthorized("Invalid or expired reset token"));
        if (reset.getConsumedAt() != null || Instant.now().isAfter(reset.getExpiresAt())) {
            throw ApiException.unauthorized("Invalid or expired reset token");
        }
        UserAccount user = userAccountRepository.findById(reset.getUserAccountId())
                .orElseThrow(() -> ApiException.unauthorized("Invalid or expired reset token"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);
        reset.setConsumedAt(Instant.now());
        resetTokenRepository.save(reset);
        // A reset invalidates all existing sessions.
        for (AuthSession session : authSessionRepository.findAll()) {
            if (session.getUserAccountId().equals(user.getId()) && session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now());
                authSessionRepository.save(session);
            }
        }
        auditService.recordUser(user, "PASSWORD_RESET_COMPLETED", "UserAccount", user.getEmail(),
                null, null, "RESET_TOKEN_SINGLE_USE");
    }

    // ------------------------------------------------------------------ password change (step-up)

    @Transactional
    public void changePassword(UserAccount user, String sessionId, String currentPassword, String newPassword) {
        Validators.validatePassword(newPassword);
        otpService.requireStepUp(user, sessionId, OtpPurpose.PASSWORD_CHANGE);
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(currentPassword == null ? "" : currentPassword, user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);
        auditService.recordUser(user, "PASSWORD_CHANGED", "UserAccount", user.getEmail(),
                null, null, "STEP_UP_OTP:PASSWORD_CHANGE");
    }

    // ------------------------------------------------------------------ helpers

    public Application applicationOf(UserAccount user) {
        Applicant applicant = applicantRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Applicant profile"));
        return applicationRepository.findByApplicantId(applicant.getId())
                .orElseThrow(() -> ApiException.notFound("Application"));
    }

    public Applicant applicantOf(UserAccount user) {
        return applicantRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Applicant profile"));
    }

    public void completeStep(Application app, JourneyStepKey key, String capturedDataJson) {
        JourneyStep step = journeyStepRepository.findByApplicationIdAndStepKey(app.getId(), key)
                .orElseThrow(() -> new IllegalStateException("Journey step missing: " + key));
        step.setCompleted(true);
        step.setCompletedAt(Instant.now());
        if (capturedDataJson != null) {
            step.setCapturedData(capturedDataJson);
        }
        journeyStepRepository.save(step);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /**
     * Verifies credentials imported from the legacy branch roster, which stored the pre-hashed
     * digest rather than a BCrypt value.
     */
    private static boolean legacyDigestMatches(String presented, String stored) {
        if (stored == null || stored.length() != 32) {
            return false;
        }
        try {
            byte[] digest = java.security.MessageDigest.getInstance("MD5")
                    .digest(presented.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return java.security.MessageDigest.isEqual(
                    sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private String generateReference() {
        while (true) {
            int suffix = new java.security.SecureRandom().nextInt(1_000_000);
            String ref = "ONB-" + java.time.Year.now().getValue() + "-" + String.format("%06d", suffix);
            if (applicationRepository.findAll().stream().noneMatch(a -> a.getReference().equals(ref))) {
                return ref;
            }
        }
    }

    public record LoginResponse(String token, String role, String email) {
    }
}
