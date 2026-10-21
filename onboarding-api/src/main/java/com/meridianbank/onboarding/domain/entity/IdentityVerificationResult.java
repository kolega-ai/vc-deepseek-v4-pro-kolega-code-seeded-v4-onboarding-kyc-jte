package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "identity_verification_result")
@Getter
@Setter
@NoArgsConstructor
public class IdentityVerificationResult {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_PROVIDER_ERROR = "PROVIDER_ERROR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "provider_reference", length = 80)
    private String providerReference;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(length = 24)
    private String outcome;

    /** Raw XML response body from the identity provider, kept for audit and dispute handling. */
    @Column(name = "raw_xml", columnDefinition = "TEXT")
    private String rawXml;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
