package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.OtpChannel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single-use, time-bound OTP challenge. Bound to the session reference (registration token or
 * JWT session id) and identity that requested it; rate-limited per identity by the issuing service.
 */
@Entity
@Table(name = "otp_challenge")
@Getter
@Setter
@NoArgsConstructor
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_account_id")
    private Long userAccountId;

    /** REGISTRATION (channel code) or one of the step-up purposes (OtpPurpose). */
    @Column(nullable = false, length = 32)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OtpChannel channel;

    /** SHA-256 of the OTP code; the plaintext code is never stored. */
    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "session_ref_hash", nullable = false, length = 64)
    private String sessionRefHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "consumed_at")
    private Instant consumedAt;
}
