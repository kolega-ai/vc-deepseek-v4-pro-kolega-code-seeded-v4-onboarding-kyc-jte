package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.JourneyStepKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "journey_step",
        uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "step_key"}))
@Getter
@Setter
@NoArgsConstructor
public class JourneyStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_key", nullable = false, length = 32)
    private JourneyStepKey stepKey;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** JSON snapshot of the data captured at completion of this step. */
    @Column(name = "captured_data", columnDefinition = "TEXT")
    private String capturedData;
}
