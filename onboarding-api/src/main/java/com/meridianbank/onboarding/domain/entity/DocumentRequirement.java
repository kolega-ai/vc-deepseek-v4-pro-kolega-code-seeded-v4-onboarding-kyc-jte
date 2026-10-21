package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_requirement",
        uniqueConstraints = @UniqueConstraint(columnNames = {"kyc_tier", "doc_type", "condition_type"}))
@Getter
@Setter
@NoArgsConstructor
public class DocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kyc_tier", nullable = false, length = 16)
    private String kycTier;

    /** IDENTITY_DOC (any identity-category document), ADDRESS_PROOF or PROOF_OF_INCOME. */
    @Column(name = "doc_type", nullable = false, length = 32)
    private String docType;

    /** null (unconditional), SELF_EMPLOYED or INCOME_GE_100K. */
    @Column(name = "condition_type", length = 32)
    private String conditionType;

    public boolean isUnconditional() {
        return conditionType == null;
    }
}
