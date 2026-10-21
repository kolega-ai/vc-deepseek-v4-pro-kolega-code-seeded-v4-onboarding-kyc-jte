package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.EmploymentStatus;
import com.meridianbank.onboarding.domain.enums.HitDisposition;
import com.meridianbank.onboarding.domain.repo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Server-side KYC tier derivation. The tier is computed from the verified document set, the
 * identity match score and watchlist dispositions against the seeded tier table; it is never
 * accepted from the client and never inferred from unverified claims.
 *
 * Rules:
 *  - identity match must be >= 80, otherwise the tier is tier_0;
 *  - tier_1 requires a verified identity document; tier_2 adds a verified address document;
 *    tier_3 adds a verified proof of income;
 *  - a self-employed applicant (or one declaring >= 100,000 SGD) needs a verified proof of income
 *    for ANY tier above tier_0;
 *  - a confirmed watchlist hit caps the tier at tier_0;
 *  - expired/rejected/uploaded documents never count.
 */
@Service
public class KycTierService {

    public static final String TIER_0 = "tier_0";
    public static final String TIER_1 = "tier_1";
    public static final String TIER_2 = "tier_2";
    public static final String TIER_3 = "tier_3";

    public static final BigDecimal SELF_EMPLOYED_INCOME_THRESHOLD = new BigDecimal("100000.00");

    private final ApplicantRepository applicantRepository;
    private final DocumentRepository documentRepository;
    private final IdentityVerificationResultRepository identityRepository;
    private final WatchlistHitRepository hitRepository;
    private final DocumentRequirementRepository requirementRepository;

    public KycTierService(ApplicantRepository applicantRepository,
                          DocumentRepository documentRepository,
                          IdentityVerificationResultRepository identityRepository,
                          WatchlistHitRepository hitRepository,
                          DocumentRequirementRepository requirementRepository) {
        this.applicantRepository = applicantRepository;
        this.documentRepository = documentRepository;
        this.identityRepository = identityRepository;
        this.hitRepository = hitRepository;
        this.requirementRepository = requirementRepository;
    }

    /** Derives the tier for the application from current server-side state. */
    public String deriveTier(Application application) {
        Applicant applicant = applicantRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new IllegalStateException("Applicant missing for application " + application.getId()));

        IdentityVerificationResult latest = identityRepository
                .findTopByApplicationIdOrderByCreatedAtDesc(application.getId()).orElse(null);
        if (latest == null || latest.getMatchScore() == null
                || latest.getMatchScore() < 80) {
            return TIER_0;
        }

        List<WatchlistHit> hits = hitRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId());
        boolean anyConfirmed = hits.stream().anyMatch(h -> h.getDisposition() == HitDisposition.CONFIRMED);
        if (anyConfirmed) {
            return TIER_0;
        }

        Set<String> verifiedDocTypes = documentRepository
                .findByApplicationIdAndVerificationState(application.getId(),
                        com.meridianbank.onboarding.domain.enums.DocState.VERIFIED)
                .stream()
                .map(d -> d.getDocType().name())
                .collect(Collectors.toSet());

        for (String tier : List.of(TIER_3, TIER_2, TIER_1)) {
            if (requirementsSatisfied(tier, applicant, verifiedDocTypes)) {
                return tier;
            }
        }
        return TIER_0;
    }

    private boolean requirementsSatisfied(String tier, Applicant applicant, Set<String> verifiedDocTypes) {
        for (DocumentRequirement req : requirementRepository.findByKycTierOrderByDocTypeAsc(tier)) {
            if (!conditionApplies(req, applicant)) {
                continue;
            }
            boolean satisfied = switch (req.getDocType()) {
                case "IDENTITY_DOC" ->
                        verifiedDocTypes.contains("NATIONAL_ID") || verifiedDocTypes.contains("PASSPORT");
                default -> verifiedDocTypes.contains(req.getDocType());
            };
            if (!satisfied) {
                return false;
            }
        }
        return true;
    }

    private boolean conditionApplies(DocumentRequirement req, Applicant applicant) {
        if (req.isUnconditional()) {
            return true;
        }
        return switch (req.getConditionType()) {
            case "SELF_EMPLOYED" -> applicant.getEmploymentStatus() == EmploymentStatus.SELF_EMPLOYED;
            case "INCOME_GE_100K" -> applicant.getDeclaredAnnualIncome() != null
                    && applicant.getDeclaredAnnualIncome().compareTo(SELF_EMPLOYED_INCOME_THRESHOLD) >= 0;
            default -> false;
        };
    }

    /** True when the applicant's situation requires a verified proof of income for any tier > tier_0. */
    public boolean proofOfIncomeRequired(Applicant applicant) {
        if (applicant.getEmploymentStatus() == EmploymentStatus.SELF_EMPLOYED) {
            return true;
        }
        return applicant.getDeclaredAnnualIncome() != null
                && applicant.getDeclaredAnnualIncome().compareTo(SELF_EMPLOYED_INCOME_THRESHOLD) >= 0;
    }

    public List<DocumentRequirement> requirementsFor(String tier) {
        return requirementRepository.findByKycTierOrderByDocTypeAsc(tier);
    }
}
