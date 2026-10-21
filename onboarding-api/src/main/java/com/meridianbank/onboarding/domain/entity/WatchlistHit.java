package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.HitDisposition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "watchlist_hit")
@Getter
@Setter
@NoArgsConstructor
public class WatchlistHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "matched_name", nullable = false, length = 160)
    private String matchedName;

    @Column(name = "list_source", nullable = false, length = 80)
    private String listSource;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private HitDisposition disposition;

    @Column(name = "disposed_by")
    private Long disposedBy;

    @Column(name = "disposed_at")
    private Instant disposedAt;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
