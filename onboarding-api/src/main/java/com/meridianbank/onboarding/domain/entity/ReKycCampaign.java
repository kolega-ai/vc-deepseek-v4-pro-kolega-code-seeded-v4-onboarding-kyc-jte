package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "rekyc_campaign")
@Getter
@Setter
@NoArgsConstructor
public class ReKycCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "target_segment", length = 120)
    private String targetSegment;

    @Column(name = "target_kyc_tier", length = 16)
    private String targetKycTier;

    @Column(name = "due_days", nullable = false)
    private int dueDays;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
