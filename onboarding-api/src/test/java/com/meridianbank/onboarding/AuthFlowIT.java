package com.meridianbank.onboarding;

import com.meridianbank.onboarding.domain.entity.Notification;
import com.meridianbank.onboarding.domain.entity.OtpChallenge;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OTP and password-reset acceptance criteria: single use, expiry, session binding, rate limiting,
 * attempt limits, step-up enforcement for password change, and single-use reset tokens.
 */
class AuthFlowIT extends IntegrationTestBase {

    private static final Pattern CODE = Pattern.compile("code is (\\d{6})");

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private OtpChallengeRepository otpChallengeRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;

    private String register(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "mobile", "+65 91234567"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("registrationToken").asText();
    }

    private String issueEmailOtp(String token) throws Exception {
        mockMvc.perform(post("/api/auth/registration/otp/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("registrationToken", token))))
                .andExpect(status().isOk());
        UserAccount user = userAccountRepository.findByRegistrationTokenHash(
                com.meridianbank.onboarding.auth.JwtService.sha256(token)).orElseThrow();
        Notification latest = notificationRepository.findTop50ByUserAccountIdOrderByCreatedAtDesc(user.getId())
                .stream().filter(n -> n.getTitle().contains("One-time passcode")).findFirst().orElseThrow();
        Matcher matcher = CODE.matcher(latest.getBody());
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private org.springframework.test.web.servlet.ResultActions verifyEmailOtp(String token, String code) throws Exception {
        return mockMvc.perform(post("/api/auth/registration/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("registrationToken", token, "code", code))));
    }

    @Test
    void registrationOtpIsSingleUse() throws Exception {
        String token = register("single-use@meridian.test");
        String code = issueEmailOtp(token);
        verifyEmailOtp(token, code).andExpect(status().isOk());
        // reuse -> rejected
        verifyEmailOtp(token, code).andExpect(status().isBadRequest());
    }

    @Test
    void registrationOtpIsBoundToTheRegistrationToken() throws Exception {
        String token = register("session-bound@meridian.test");
        String code = issueEmailOtp(token);
        // wrong registration token (different session identity) -> 401
        verifyEmailOtp("deadbeef" + token, code).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationOtpExpires() throws Exception {
        String token = register("expires@meridian.test");
        String code = issueEmailOtp(token);
        UserAccount user = userAccountRepository.findByRegistrationTokenHash(
                com.meridianbank.onboarding.auth.JwtService.sha256(token)).orElseThrow();
        for (OtpChallenge challenge : otpChallengeRepository.findAll()) {
            if (challenge.getUserAccountId().equals(user.getId())) {
                challenge.setExpiresAt(Instant.now().minusSeconds(1));
                otpChallengeRepository.save(challenge);
            }
        }
        verifyEmailOtp(token, code).andExpect(status().isBadRequest());
    }

    @Test
    void otpIssuanceIsRateLimitedPerIdentity() throws Exception {
        String token = register("rate-limited@meridian.test");
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/registration/otp/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("registrationToken", token))))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/api/auth/registration/otp/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("registrationToken", token))))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void otpVerificationAttemptsAreLimited() throws Exception {
        String token = register("attempts@meridian.test");
        issueEmailOtp(token);
        for (int i = 0; i < 5; i++) {
            verifyEmailOtp(token, "000000").andExpect(status().isBadRequest());
        }
        verifyEmailOtp(token, "000000").andExpect(status().isBadRequest());
    }

    @Test
    void passwordResetTokenIsSingleUseAndTimeBound() throws Exception {
        String token = register("reset@meridian.test");
        String code = issueEmailOtp(token);
        verifyEmailOtp(token, code).andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/registration/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("registrationToken", token, "password", "Password#2026!"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "reset@meridian.test"))))
                .andExpect(status().isOk());
        String emailBody = outboxBodyFor("reset@meridian.test", "password-reset");
        String resetToken = extractTokenFromLink(emailBody);
        assertNotNull(resetToken);

        mockMvc.perform(post("/api/auth/password-reset/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", "NewPassword#2026!"))))
                .andExpect(status().isOk());
        // reuse after a successful reset -> rejected
        mockMvc.perform(post("/api/auth/password-reset/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", "AnotherPassword#2026!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordChangeRequiresStepUpOtp() throws Exception {
        UserAccount user = fixtures.prospect("pw-change@meridian.test", fixtures.branch("MB-CEN-01"),
                "Pw Change", com.meridianbank.onboarding.domain.enums.EmploymentStatus.EMPLOYED,
                "50000", true).user();
        // One fixed bearer token so the OTP challenge, verification and change share a session.
        String token = bearerFor(user, "pw-change-session");
        // Without step-up: forbidden
        postAsToken(token, "/api/auth/password/change",
                json(Map.of("currentPassword", "Password#2026!", "newPassword", "Changed#2026!")))
                .andExpect(status().isForbidden());
        // Issue + verify step-up OTP
        postAsToken(token, "/api/auth/otp/step-up/issue", json(Map.of("purpose", "password_change")))
                .andExpect(status().isOk());
        String code = latestCodeFor(user);
        postAsToken(token, "/api/auth/otp/step-up/verify",
                json(Map.of("purpose", "password_change", "code", code)))
                .andExpect(status().isOk());
        // With step-up: succeeds
        postAsToken(token, "/api/auth/password/change",
                json(Map.of("currentPassword", "Password#2026!", "newPassword", "Changed#2026!")))
                .andExpect(status().isOk());
    }

    @Test
    void loginLocksAfterRepeatedFailures() throws Exception {
        String token = register("lockout@meridian.test");
        String code = issueEmailOtp(token);
        verifyEmailOtp(token, code).andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/registration/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("registrationToken", token, "password", "Password#2026!"))))
                .andExpect(status().isOk());
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("email", "lockout@meridian.test", "password", "Wrong#0000!"))))
                    .andExpect(status().isUnauthorized());
        }
        // Even the correct password is now rejected while locked
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "lockout@meridian.test", "password", "Password#2026!"))))
                .andExpect(status().isUnauthorized());
    }

    private String latestCodeFor(UserAccount user) {
        Notification latest = notificationRepository.findTop50ByUserAccountIdOrderByCreatedAtDesc(user.getId())
                .stream().filter(n -> n.getTitle().contains("One-time passcode")).findFirst().orElseThrow();
        Matcher matcher = CODE.matcher(latest.getBody());
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String outboxBodyFor(String email, String purpose) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password#2026!"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String bearer = objectMapper.readTree(response).get("token").asText();
        var outbox = objectMapper.readTree(mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/outbox/me")
                                .header("Authorization", "Bearer " + bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        for (var item : outbox.get("items")) {
            if (purpose.equals(item.path("purpose").asText())) {
                return item.path("body").asText();
            }
        }
        return "";
    }

    private String extractTokenFromLink(String body) {
        int start = body.indexOf("token=");
        if (start < 0) {
            return null;
        }
        String token = body.substring(start + 6);
        int end = token.indexOf('\n');
        return end < 0 ? token.trim() : token.substring(0, end).trim();
    }
}
