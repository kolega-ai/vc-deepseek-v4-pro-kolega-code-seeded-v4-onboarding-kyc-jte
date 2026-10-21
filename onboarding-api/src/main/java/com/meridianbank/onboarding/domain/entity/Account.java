package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.AccountState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Account opened in the vendor core (core-mock) for an approved application. */
@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(name = "account_number", nullable = false, unique = true, length = 24)
    private String accountNumber;

    @Column(nullable = false, length = 80)
    private String product;

    @Column(name = "kyc_tier", nullable = false, length = 16)
    private String kycTier;

    @Column(name = "core_reference", nullable = false, length = 60)
    private String coreReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountState status;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;
}
