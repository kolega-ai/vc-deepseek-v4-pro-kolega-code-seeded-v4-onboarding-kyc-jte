package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.ReKycOrigin;
import com.meridianbank.onboarding.domain.enums.ReKycStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "rekyc_case")
@Getter
@Setter
@NoArgsConstructor
public class ReKycCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReKycOrigin origin;

    @Column(name = "customer_user_id", nullable = false)
    private Long customerUserId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReKycStatus status;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
