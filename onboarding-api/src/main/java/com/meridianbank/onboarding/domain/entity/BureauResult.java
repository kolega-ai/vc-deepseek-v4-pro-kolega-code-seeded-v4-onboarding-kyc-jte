package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "bureau_result")
@Getter
@Setter
@NoArgsConstructor
public class BureauResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    private Integer score;

    /** JSON array of reported liabilities. Staff-only; never exposed to the applicant. */
    @Column(columnDefinition = "TEXT")
    private String liabilities;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "pulled_at", nullable = false)
    private Instant pulledAt;
}
