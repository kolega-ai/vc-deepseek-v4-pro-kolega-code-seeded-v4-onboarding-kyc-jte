package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Short-lived proof that a step-up OTP was verified for a purpose within a session.
 * Sensitive endpoints require a valid grant before executing.
 */
@Entity
@Table(name = "step_up_grant")
@Getter
@Setter
@NoArgsConstructor
public class StepUpGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_account_id", nullable = false)
    private Long userAccountId;

    @Column(nullable = false, length = 32)
    private String purpose;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
