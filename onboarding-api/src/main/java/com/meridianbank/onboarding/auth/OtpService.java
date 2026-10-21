package com.meridianbank.onboarding.auth;

import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.OtpChallenge;
import com.meridianbank.onboarding.domain.entity.StepUpGrant;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.NotificationType;
import com.meridianbank.onboarding.domain.enums.OtpChannel;
import com.meridianbank.onboarding.domain.enums.OtpPurpose;
import com.meridianbank.onboarding.domain.repo.OtpChallengeRepository;
import com.meridianbank.onboarding.domain.repo.StepUpGrantRepository;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import com.meridianbank.onboarding.service.AttemptTrackerService;
import com.meridianbank.onboarding.service.NotificationService;
import com.meridianbank.onboarding.service.OutboxEmailService;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Single-use, time-bound OTP challenges.
 * - bound to the session reference (registration token or JWT session id) AND the identity that
 *   requested them;
 * - expire after the configured TTL;
 * - limited to a fixed number of verification attempts;
 * - rate-limited per identity (max issuances per rolling window);
 * - consumed exactly once on successful verification.
 */
@Service
public class OtpService {

    public static final String PURPOSE_REGISTRATION = "REGISTRATION";
    public static final String PURPOSE_MOBILE_VERIFY = "MOBILE_VERIFY";
    public static final String PURPOSE_DOCUMENT_COLLECTION = "DOCUMENT_COLLECTION";

    private final OtpChallengeRepository otpChallengeRepository;
    private final StepUpGrantRepository stepUpGrantRepository;
    private final UserAccountRepository userAccountRepository;
    private final AttemptTrackerService attemptTrackerService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final AppProperties props;

    public OtpService(OtpChallengeRepository otpChallengeRepository,
                      StepUpGrantRepository stepUpGrantRepository,
                      UserAccountRepository userAccountRepository,
                      AttemptTrackerService attemptTrackerService,
                      NotificationService notificationService,
                      OutboxEmailService outboxEmailService,
                      AppProperties props) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.stepUpGrantRepository = stepUpGrantRepository;
        this.userAccountRepository = userAccountRepository;
        this.attemptTrackerService = attemptTrackerService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.props = props;
    }

    // ----------------------------------------------------------- registration (email OTP)

    @Transactional
    public void issueRegistrationOtp(UserAccount user, String registrationToken) {
        String code = issueChallenge(user, PURPOSE_REGISTRATION, OtpChannel.EMAIL, registrationToken);
        deliverCode(user, OtpChannel.EMAIL, code, "registration");
    }

    @Transactional
    public void verifyRegistrationOtp(UserAccount user, String registrationToken, String code) {
        OtpChallenge challenge = requireActiveChallenge(user, PURPOSE_REGISTRATION, OtpChannel.EMAIL, registrationToken);
        tryVerify(challenge, code);
        challenge.setConsumedAt(Instant.now());
        otpChallengeRepository.save(challenge);
        user.setEmailVerifiedAt(Instant.now());
        userAccountRepository.save(user);
    }

    // ----------------------------------------------------------- session OTPs (mobile / step-up)

    @Transactional
    public String issueSessionOtp(UserAccount user, String sessionId, String purpose) {
        String code = issueChallenge(user, purpose, OtpChannel.MOBILE, sessionId);
        deliverCode(user, OtpChannel.MOBILE, code, purpose);
        return code;
    }

    @Transactional
    public void verifySessionOtp(UserAccount user, String sessionId, String purpose, String code) {
        OtpChallenge challenge = requireActiveChallenge(user, purpose, OtpChannel.MOBILE, sessionId);
        tryVerify(challenge, code);
        challenge.setConsumedAt(Instant.now());
        otpChallengeRepository.save(challenge);
    }

    /**
     * Issues the short code the branch desk reads back when releasing filed documents to a walk-in
     * customer; bound to the browser visit reference so only the same kiosk can complete it.
     */
    @Transactional
    public String issueCollectionCode(UserAccount user, String visitRef) {
        Instant now = Instant.now();
        String code = String.format("%06d",
                java.util.concurrent.ThreadLocalRandom.current().nextInt(1_000_000));
        OtpChallenge challenge = new OtpChallenge();
        challenge.setUserAccountId(user.getId());
        challenge.setPurpose(PURPOSE_DOCUMENT_COLLECTION);
        challenge.setChannel(OtpChannel.MOBILE);
        challenge.setCodeHash(JwtService.sha256(code));
        challenge.setSessionRefHash(JwtService.sha256(visitRef));
        challenge.setIssuedAt(now);
        challenge.setExpiresAt(now.plus(props.getAuth().getOtpTtl()));
        challenge.setAttemptCount(0);
        otpChallengeRepository.save(challenge);
        deliverCode(user, OtpChannel.MOBILE, code, PURPOSE_DOCUMENT_COLLECTION);
        return code;
    }

    // ----------------------------------------------------------- step-up (sensitive actions)

    @Transactional
    public String issueStepUpOtp(UserAccount user, String sessionId, OtpPurpose purpose) {
        validatePurpose(purpose);
        return issueSessionOtp(user, sessionId, purpose.getLabel());
    }

    @Transactional
    public void verifyStepUpOtp(UserAccount user, String sessionId, OtpPurpose purpose, String code) {
        validatePurpose(purpose);
        verifySessionOtp(user, sessionId, purpose.getLabel(), code);
        StepUpGrant grant = new StepUpGrant();
        grant.setUserAccountId(user.getId());
        grant.setPurpose(purpose.getLabel());
        grant.setSessionId(sessionId);
        grant.setExpiresAt(Instant.now().plus(props.getAuth().getStepUpTtl()));
        grant.setCreatedAt(Instant.now());
        stepUpGrantRepository.save(grant);
    }

    /** Sensitive actions require a still-valid step-up grant for the exact session and purpose. */
    public void requireStepUp(UserAccount user, String sessionId, OtpPurpose purpose) {
        boolean present = stepUpGrantRepository
                .findFirstByUserAccountIdAndPurposeAndSessionIdAndExpiresAtGreaterThanOrderByCreatedAtDesc(
                        user.getId(), purpose.getLabel(), sessionId, Instant.now())
                .isPresent();
        if (!present) {
            throw ApiException.forbidden(
                    "Step-up OTP verification is required for: " + purpose.getLabel());
        }
    }

    // ----------------------------------------------------------- internals

    private String issueChallenge(UserAccount user, String purpose, OtpChannel channel, String sessionRef) {
        Instant now = Instant.now();
        long issuedInWindow = otpChallengeRepository.countByUserAccountIdAndIssuedAtGreaterThan(
                user.getId(), now.minus(props.getAuth().getOtpIssueWindow()));
        if (issuedInWindow >= props.getAuth().getOtpMaxIssuesPerWindow()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_RATE_LIMITED",
                    "Too many codes requested; please wait before requesting another code");
        }
        String code = JwtService.generateOtpCode();
        OtpChallenge challenge = new OtpChallenge();
        challenge.setUserAccountId(user.getId());
        challenge.setPurpose(purpose);
        challenge.setChannel(channel);
        challenge.setCodeHash(JwtService.sha256(code));
        challenge.setSessionRefHash(JwtService.sha256(sessionRef));
        challenge.setIssuedAt(now);
        challenge.setExpiresAt(now.plus(props.getAuth().getOtpTtl()));
        challenge.setAttemptCount(0);
        otpChallengeRepository.save(challenge);
        return code;
    }

    private OtpChallenge requireActiveChallenge(UserAccount user, String purpose, OtpChannel channel, String sessionRef) {
        List<OtpChallenge> active = otpChallengeRepository.findActiveForSession(
                user.getId(), JwtService.sha256(sessionRef), purpose, Instant.now());
        OtpChallenge challenge = active.stream()
                .filter(c -> c.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("No active code for this channel; request a new code"));
        if (challenge.getAttemptCount() >= props.getAuth().getOtpMaxVerifyAttempts()) {
            throw ApiException.badRequest("Too many attempts; request a new code");
        }
        return challenge;
    }

    private void tryVerify(OtpChallenge challenge, String code) {
        // Attempt count is persisted in its own transaction so a failed verification does not
        // roll the counter back with the request transaction.
        attemptTrackerService.recordOtpAttempt(challenge.getId());
        if (!challenge.getCodeHash().equals(JwtService.sha256(code == null ? "" : code))) {
            throw ApiException.badRequest("Incorrect code");
        }
    }

    private void validatePurpose(OtpPurpose purpose) {
        if (purpose == null) {
            throw ApiException.badRequest("A purpose is required");
        }
    }

    private void deliverCode(UserAccount user, OtpChannel channel, String code, String purpose) {
        String prettyPurpose = purpose.replace('_', ' ');
        if (channel == OtpChannel.EMAIL) {
            outboxEmailService.send(user.getEmail(),
                    "Your Meridian Bank one-time code",
                    "Your one-time code for " + prettyPurpose + " is " + code
                            + ". It expires in 10 minutes. If you did not request this code, ignore this email.",
                    "otp-email");
        } else {
            outboxEmailService.send(user.getEmail(),
                    "SMS (mock) to your mobile: one-time code",
                    "Meridian Bank (SMS mock): your code for " + prettyPurpose + " is " + code
                            + ". Expires in 10 minutes.",
                    "otp-sms");
        }
        notificationService.notify(user.getId(), NotificationType.OTP,
                "One-time passcode (" + prettyPurpose + ")",
                "Your code is " + code + ". It expires in 10 minutes. "
                        + "(SMS delivery is mocked locally: the code is shown here instead.)",
                null);
    }
}
