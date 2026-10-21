package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.AppStatus;
import com.meridianbank.onboarding.domain.enums.DecisionOutcome;
import com.meridianbank.onboarding.domain.enums.JourneyStepKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "onb_application")
@Getter
@Setter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 24)
    private String reference;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 32)
    private JourneyStepKey currentStep;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "assigned_officer_id")
    private Long assignedOfficerId;

    /** Tier derived server-side from verified documents, identity match score and dispositions. */
    @Column(name = "derived_kyc_tier", nullable = false, length = 16)
    private String derivedKycTier;

    /** Compliance override (with mandatory justification). Effective tier = override ?? derived. */
    @Column(name = "overridden_kyc_tier", length = 16)
    private String overriddenKycTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_outcome", length = 16)
    private DecisionOutcome decisionOutcome;

    @Column(name = "customer_visible_reason", columnDefinition = "TEXT")
    private String customerVisibleReason;

    @Column(name = "internal_reason", columnDefinition = "TEXT")
    private String internalReason;

    /** JSON array of document types an officer requested. */
    @Column(name = "requested_document_types", columnDefinition = "TEXT")
    private String requestedDocumentTypes;

    @Column(name = "requested_message", columnDefinition = "TEXT")
    private String requestedMessage;

    @Column(name = "requested_at")
    private Instant requestedAt;

    @Column(name = "screening_due_at")
    private Instant screeningDueAt;

    @Column(name = "screening_attempts", nullable = false)
    private int screeningAttempts;

    @Column(name = "screening_error", columnDefinition = "TEXT")
    private String screeningError;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String effectiveKycTier() {
        return overriddenKycTier != null ? overriddenKycTier : derivedKycTier;
    }
}
