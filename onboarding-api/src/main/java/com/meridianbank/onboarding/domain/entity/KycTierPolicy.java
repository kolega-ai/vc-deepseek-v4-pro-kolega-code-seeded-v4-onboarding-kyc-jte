package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Seeded reference policy for a KYC tier: transaction and balance limits in SGD.
 * Limits are read server-side from this table and are never accepted from the client.
 */
@Entity
@Table(name = "kyc_tier_policy")
@Getter
@Setter
@NoArgsConstructor
public class KycTierPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tier_code", nullable = false, unique = true, length = 16)
    private String tierCode;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "min_identity_match", nullable = false)
    private int minIdentityMatch;

    /** Fixed-precision decimal (SGD). Never floating point. */
    @Column(name = "daily_transaction_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyTransactionLimit;

    @Column(name = "balance_ceiling", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceCeiling;

    @Column(nullable = false, length = 3)
    private String currency;
}
