package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.OtpPurpose;
import com.meridianbank.onboarding.domain.entity.OutboxEmail;
import com.meridianbank.onboarding.domain.repo.ApplicantRepository;
import com.meridianbank.onboarding.domain.repo.StaffUserRepository;
import com.meridianbank.onboarding.service.OutboxEmailService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Authentication endpoints: registration, OTP, login, logout, password reset and change. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final UserContextService userContextService;
    private final StaffUserRepository staffUserRepository;
    private final ApplicantRepository applicantRepository;
    private final OutboxEmailService outboxEmailService;

    public AuthController(AuthService authService, OtpService otpService, UserContextService userContextService,
                          StaffUserRepository staffUserRepository, ApplicantRepository applicantRepository,
                          OutboxEmailService outboxEmailService) {
        this.authService = authService;
        this.otpService = otpService;
        this.userContextService = userContextService;
        this.staffUserRepository = staffUserRepository;
        this.applicantRepository = applicantRepository;
        this.outboxEmailService = outboxEmailService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        String registrationToken = authService.register(request.email(), request.mobile());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("registrationToken", registrationToken);
        body.put("nextStep", "email-otp");
        return body;
    }

    /** Live availability check used by the sign-up form (before the registration token exists). */
    @GetMapping("/registration/availability")
    public Map<String, Object> checkAvailability(@RequestParam String email,
                                                  @CookieValue(name = "mb_visit", required = false) String visitId) {
        boolean exists = authService.emailExists(email);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("visit", visitId == null ? "new" : visitId);
        if (exists) {
            body.put("available", false);
            body.put("message", "An account with this email already exists. Try signing in instead.");
        } else {
            body.put("available", true);
            body.put("message", "This email is available.");
        }
        return body;
    }

    @PostMapping("/registration/otp/issue")
    public Map<String, Object> issueEmailOtp(@RequestBody RegistrationTokenRequest request) {
        UserAccount user = authService.userByRegistrationToken(request.registrationToken());
        otpService.issueRegistrationOtp(user, request.registrationToken());
        return ok("Code sent to your email (see the demo inbox).");
    }

    @PostMapping("/registration/otp/verify")
    public Map<String, Object> verifyEmailOtp(@RequestBody RegistrationOtpVerifyRequest request) {
        UserAccount user = authService.userByRegistrationToken(request.registrationToken());
        otpService.verifyRegistrationOtp(user, request.registrationToken(), request.code());
        return ok("Email verified.");
    }

    /**
     * Registration inbox: the pending applicant can read the mock email deliveries addressed to
     * them before they have a password. Guarded by the opaque registration token only.
     */
    @GetMapping("/registration/inbox")
    public Map<String, Object> registrationInbox(@RequestParam String registrationToken) {
        UserAccount user = authService.userByRegistrationToken(registrationToken);
        List<Map<String, Object>> items = new ArrayList<>();
        for (OutboxEmail email : outboxEmailService.inboxFor(user.getEmail())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", email.getId());
            item.put("subject", email.getSubject());
            item.put("body", email.getBody());
            item.put("purpose", email.getPurpose());
            item.put("createdAt", email.getCreatedAt());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @PostMapping("/registration/password")
    public AuthService.LoginResponse setPassword(@RequestBody RegistrationPasswordRequest request) {
        return authService.completeRegistration(request.registrationToken(), request.password());
    }

    @PostMapping("/login")
    public AuthService.LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        authService.logout(userContextService.currentUser(), userContextService.currentSessionId());
        return ok("Logged out.");
    }

    @PostMapping("/password-reset/request")
    public Map<String, Object> requestPasswordReset(@RequestBody Map<String, String> body) {
        authService.requestPasswordReset(body.getOrDefault("email", ""));
        return ok("If the email exists, a reset link has been sent.");
    }

    @PostMapping("/password-reset/complete")
    public Map<String, Object> completePasswordReset(@RequestBody Map<String, String> body) {
        authService.completePasswordReset(body.getOrDefault("token", ""),
                body.getOrDefault("newPassword", ""));
        return ok("Password reset. You can now log in.");
    }

    @PostMapping("/password/change")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> body) {
        authService.changePassword(userContextService.currentUser(), userContextService.currentSessionId(),
                body.getOrDefault("currentPassword", ""), body.getOrDefault("newPassword", ""));
        return ok("Password changed.");
    }

    @PostMapping("/otp/step-up/issue")
    public Map<String, Object> issueStepUp(@RequestBody Map<String, String> body) {
        OtpPurpose purpose = OtpPurpose.valueOf(body.getOrDefault("purpose", "").toUpperCase());
        String code = otpService.issueStepUpOtp(userContextService.currentUser(),
                userContextService.currentSessionId(), purpose);
        Map<String, Object> response = ok("Step-up code sent (check your notifications).");
        // SMS delivery is mocked locally; the code is echoed here too for local testing convenience.
        response.put("debugOtpCode", code);
        return response;
    }

    @PostMapping("/otp/step-up/verify")
    public Map<String, Object> verifyStepUp(@RequestBody Map<String, String> body) {
        OtpPurpose purpose = OtpPurpose.valueOf(body.getOrDefault("purpose", "").toUpperCase());
        otpService.verifyStepUpOtp(userContextService.currentUser(), userContextService.currentSessionId(),
                purpose, body.getOrDefault("code", ""));
        return ok("Step-up verified.");
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        UserAccount user = userContextService.currentUser();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.getId());
        body.put("email", user.getEmail());
        body.put("role", user.getRole().getLabel());
        body.put("mobileVerified", user.getMobileVerifiedAt() != null);
        body.put("emailVerified", user.getEmailVerifiedAt() != null);
        if (user.getStaffId() != null) {
            staffUserRepository.findById(user.getStaffId())
                    .ifPresent(staff -> body.put("displayName", staff.getFullName()));
        }
        if (!body.containsKey("displayName")) {
            applicantRepository.findByUserAccountId(user.getId())
                    .ifPresent(applicant -> body.put("displayName", applicant.getFullName()));
        }
        return body;
    }

    private Map<String, Object> ok(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", message);
        return body;
    }

    public record RegisterRequest(String email, String mobile) {
    }

    public record RegistrationTokenRequest(String registrationToken) {
    }

    public record RegistrationOtpVerifyRequest(String registrationToken, String code) {
    }

    public record RegistrationPasswordRequest(String registrationToken, String password) {
    }

    public record LoginRequest(String email, String password) {
    }
}
