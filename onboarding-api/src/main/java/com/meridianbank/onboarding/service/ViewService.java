package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Assembles role-specific JSON views. Applicant-facing views never include bureau scores,
 * watchlist hits, internal reasons or raw provider payloads; staff views include the fields their
 * role is entitled to. All authorization happens before assembly.
 */
@Service
public class ViewService {

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_INSTANT;

    private final AuthService authService;
    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final JourneyStepRepository journeyStepRepository;
    private final DocumentRepository documentRepository;
    private final IdentityVerificationResultRepository identityRepository;
    private final BureauResultRepository bureauRepository;
    private final WatchlistHitRepository hitRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final StaffUserRepository staffUserRepository;
    private final KycTierPolicyRepository policyRepository;
    private final KycTierService kycTierService;
    private final ReKycService reKycService;
    private final UserAccountRepository userAccountRepository;

    public ViewService(AuthService authService,
                       ApplicantRepository applicantRepository,
                       ApplicationRepository applicationRepository,
                       JourneyStepRepository journeyStepRepository,
                       DocumentRepository documentRepository,
                       IdentityVerificationResultRepository identityRepository,
                       BureauResultRepository bureauRepository,
                       WatchlistHitRepository hitRepository,
                       AccountRepository accountRepository,
                       BranchRepository branchRepository,
                       StaffUserRepository staffUserRepository,
                       KycTierPolicyRepository policyRepository,
                       KycTierService kycTierService,
                       ReKycService reKycService,
                       UserAccountRepository userAccountRepository) {
        this.authService = authService;
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.journeyStepRepository = journeyStepRepository;
        this.documentRepository = documentRepository;
        this.identityRepository = identityRepository;
        this.bureauRepository = bureauRepository;
        this.hitRepository = hitRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.staffUserRepository = staffUserRepository;
        this.policyRepository = policyRepository;
        this.kycTierService = kycTierService;
        this.reKycService = reKycService;
        this.userAccountRepository = userAccountRepository;
    }

    // ---------------------------------------------------------------- applicant-facing

    public Map<String, Object> applicantOwnView(UserAccount user) {
        Application application = authService.applicationOf(user);
        Applicant applicant = authService.applicantOf(user);
        Map<String, Object> view = baseApplication(application, applicant, true);
        view.put("applicant", applicantMap(applicant, true));

        List<Map<String, Object>> steps = new ArrayList<>();
        for (JourneyStep step : journeyStepRepository.findByApplicationIdOrderByStepKeyAsc(application.getId())) {
            Map<String, Object> stepMap = new LinkedHashMap<>();
            stepMap.put("key", step.getStepKey().getLabel());
            stepMap.put("order", step.getStepKey().getOrder());
            stepMap.put("completed", step.isCompleted());
            stepMap.put("completedAt", step.getCompletedAt());
            steps.add(stepMap);
        }
        view.put("steps", steps);

        List<Map<String, Object>> documents = new ArrayList<>();
        for (Document document : documentRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())) {
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("id", document.getId());
            doc.put("type", document.getDocType().name());
            doc.put("typeLabel", document.getDocType().getLabel());
            doc.put("category", document.getCategory().getLabel());
            doc.put("filename", document.getOriginalFilename());
            doc.put("state", document.getVerificationState().getLabel());
            doc.put("verifiedAt", document.getVerifiedAt());
            doc.put("expiryDate", document.getExpiryDate());
            doc.put("reKycCaseId", document.getReKycCaseId());
            documents.add(doc);
        }
        view.put("documents", documents);

        List<Map<String, Object>> identityResults = new ArrayList<>();
        for (IdentityVerificationResult result :
                identityRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", result.getId());
            r.put("status", result.getStatus());
            r.put("matchScore", result.getMatchScore());
            r.put("outcome", result.getOutcome());
            r.put("providerReference", result.getProviderReference());
            r.put("errorMessage", result.getErrorMessage());
            r.put("createdAt", result.getCreatedAt());
            identityResults.add(r);
        }
        view.put("identityResults", identityResults);

        view.put("requiredDocuments", requiredDocumentTypes(application, applicant));
        view.put("requiredDocumentsBlocked", kycTierService.proofOfIncomeRequired(applicant));

        accountRepository.findByApplicationId(application.getId()).ifPresent(account -> {
            Map<String, Object> acct = accountMap(account, true);
            view.put("account", acct);
        });

        ReKycCase activeCase = user.getRole() == Role.CUSTOMER ? reKycService.activeCaseFor(user) : null;
        if (activeCase != null) {
            view.put("rekycCase", reKycCaseMap(activeCase));
        }
        return view;
    }

    /** Customer dashboard view: profile, account, tier, limits, re-KYC prompt. */
    public Map<String, Object> customerView(UserAccount user) {
        Map<String, Object> view = applicantOwnView(user);
        Application application = authService.applicationOf(user);
        view.put("profile", applicantMap(authService.applicantOf(user), true));
        accountRepository.findByApplicationId(application.getId()).ifPresent(account -> {
            KycTierPolicy policy = policyRepository.findByTierCode(account.getKycTier()).orElse(null);
            Map<String, Object> acct = accountMap(account, true);
            acct.put("limits", policy == null ? null : policyMap(policy));
            acct.put("suspended", account.getStatus() == AccountState.SUSPENDED);
            view.put("account", acct);
        });
        ReKycCase activeCase = reKycService.activeCaseFor(user);
        if (activeCase != null) {
            Map<String, Object> caseMap = reKycCaseMap(activeCase);
            caseMap.put("requiredTypes", reKycService.requiredTypesFor(activeCase));
            caseMap.put("documents", documentsForCase(activeCase));
            view.put("rekycCase", caseMap);
        }
        return view;
    }

    private List<Map<String, Object>> documentsForCase(ReKycCase kase) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Document document : documentRepository.findByApplicationIdOrderByCreatedAtAsc(kase.getApplicationId())) {
            if (!kase.getId().equals(document.getReKycCaseId())) {
                continue;
            }
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("id", document.getId());
            doc.put("type", document.getDocType().name());
            doc.put("typeLabel", document.getDocType().getLabel());
            doc.put("filename", document.getOriginalFilename());
            doc.put("state", document.getVerificationState().getLabel());
            doc.put("verifiedAt", document.getVerifiedAt());
            doc.put("expiryDate", document.getExpiryDate());
            list.add(doc);
        }
        return list;
    }

    /** Required document types for the applicant's situation (server-derived, informational). */
    public List<String> requiredDocumentTypes(Application application, Applicant applicant) {
        List<String> types = new ArrayList<>();
        types.add("IDENTITY_DOC");
        if (kycTierService.proofOfIncomeRequired(applicant)) {
            types.add("PROOF_OF_INCOME");
        }
        if (application.getRequestedDocumentTypes() != null && !application.getRequestedDocumentTypes().isBlank()) {
            try {
                for (Object item : new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(application.getRequestedDocumentTypes(), List.class)) {
                    if (!types.contains(item)) {
                        types.add(String.valueOf(item));
                    }
                }
            } catch (Exception ignored) {
                // requested types are informational
            }
        }
        return types;
    }

    // ---------------------------------------------------------------- officer-facing

    public Map<String, Object> officerApplicationView(Application application) {
        Applicant applicant = applicantRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new IllegalStateException("Applicant missing"));
        Map<String, Object> view = baseApplication(application, applicant, false);
        view.put("applicant", applicantMap(applicant, false));

        List<Map<String, Object>> documents = new ArrayList<>();
        for (Document document : documentRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())) {
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("id", document.getId());
            doc.put("type", document.getDocType().name());
            doc.put("typeLabel", document.getDocType().getLabel());
            doc.put("category", document.getCategory().getLabel());
            doc.put("filename", document.getOriginalFilename());
            doc.put("state", document.getVerificationState().getLabel());
            doc.put("verifiedAt", document.getVerifiedAt());
            doc.put("expiryDate", document.getExpiryDate());
            doc.put("reKycCaseId", document.getReKycCaseId());
            doc.put("sizeBytes", document.getSizeBytes());
            doc.put("contentType", document.getContentType());
            staffUserRepository.findById(document.getVerifiedBy() == null ? -1 : document.getVerifiedBy())
                    .ifPresent(staff -> doc.put("verifiedBy", staff.getFullName()));
            documents.add(doc);
        }
        view.put("documents", documents);

        identityRepository.findTopByApplicationIdOrderByCreatedAtDesc(application.getId()).ifPresent(result -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", result.getId());
            r.put("status", result.getStatus());
            r.put("matchScore", result.getMatchScore());
            r.put("outcome", result.getOutcome());
            r.put("providerReference", result.getProviderReference());
            r.put("errorMessage", result.getErrorMessage());
            r.put("createdAt", result.getCreatedAt());
            view.put("identityResult", r);
        });

        // Staff-only: bureau score and liabilities.
        bureauRepository.findTopByApplicationIdOrderByPulledAtDesc(application.getId()).ifPresent(bureau -> {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("score", bureau.getScore());
            b.put("status", bureau.getStatus());
            b.put("pulledAt", bureau.getPulledAt());
            b.put("liabilities", bureau.getLiabilities());
            view.put("bureauResult", b);
        });

        // Staff-only: watchlist hits.
        List<Map<String, Object>> hits = new ArrayList<>();
        for (WatchlistHit hit : hitRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())) {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("id", hit.getId());
            h.put("matchedName", hit.getMatchedName());
            h.put("listSource", hit.getListSource());
            h.put("confidence", hit.getConfidence());
            h.put("disposition", hit.getDisposition().getLabel());
            h.put("justification", hit.getJustification());
            h.put("disposedAt", hit.getDisposedAt());
            hits.add(h);
        }
        view.put("watchlistHits", hits);

        view.put("internalReason", application.getInternalReason());
        view.put("screeningError", application.getScreeningError());
        if (application.getAssignedOfficerId() != null) {
            staffUserRepository.findById(application.getAssignedOfficerId())
                    .ifPresent(officer -> view.put("assignedOfficer", officer.getFullName()));
        }
        return view;
    }

    // ---------------------------------------------------------------- shared builders

    private Map<String, Object> baseApplication(Application application, Applicant applicant, boolean applicantFacing) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", application.getId());
        map.put("reference", application.getReference());
        map.put("status", application.getStatus().getLabel());
        map.put("currentStep", application.getCurrentStep() == null ? null : application.getCurrentStep().getLabel());
        map.put("branch", branchName(application.getBranchId()));
        map.put("branchId", application.getBranchId());
        map.put("derivedKycTier", application.getDerivedKycTier());
        map.put("effectiveKycTier", application.effectiveKycTier());
        map.put("overriddenKycTier", application.getOverriddenKycTier());
        map.put("decisionOutcome", application.getDecisionOutcome() == null ? null
                : application.getDecisionOutcome().getLabel());
        map.put("customerVisibleReason", application.getCustomerVisibleReason());
        map.put("submittedAt", application.getSubmittedAt());
        map.put("createdAt", application.getCreatedAt());
        map.put("lastActivityAt", application.getLastActivityAt());
        Map<String, Object> requested = new LinkedHashMap<>();
        requested.put("types", requestedTypes(application));
        requested.put("message", application.getRequestedMessage());
        requested.put("at", application.getRequestedAt());
        map.put("requestedDocuments", requested);
        map.put("identityPassed", latestIdentityPassed(application));
        map.put("screeningPending", application.getStatus() == AppStatus.SUBMITTED);
        if (!applicantFacing) {
            map.put("assignedOfficerId", application.getAssignedOfficerId());
        }
        return map;
    }

    private boolean latestIdentityPassed(Application application) {
        return identityRepository.findTopByApplicationIdOrderByCreatedAtDesc(application.getId())
                .map(r -> r.getMatchScore() != null && r.getMatchScore() >= 80).orElse(false);
    }

    private List<String> requestedTypes(Application application) {
        if (application.getRequestedDocumentTypes() == null || application.getRequestedDocumentTypes().isBlank()) {
            return List.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(application.getRequestedDocumentTypes(), List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> applicantMap(Applicant applicant, boolean applicantFacing) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", applicant.getId());
        map.put("fullName", applicant.getFullName());
        map.put("dateOfBirth", applicant.getDateOfBirth());
        map.put("nationalIdentifier", applicant.getNationalIdentifier());
        map.put("email", applicant.getEmail());
        map.put("mobile", applicant.getMobile());
        map.put("addressLine1", applicant.getAddressLine1());
        map.put("addressLine2", applicant.getAddressLine2());
        map.put("city", applicant.getCity());
        map.put("postalCode", applicant.getPostalCode());
        map.put("country", applicant.getCountry());
        map.put("employmentStatus", applicant.getEmploymentStatus() == null ? null
                : applicant.getEmploymentStatus().getLabel());
        map.put("employerName", applicant.getEmployerName());
        map.put("declaredAnnualIncome", applicant.getDeclaredAnnualIncome());
        return map;
    }

    private Map<String, Object> accountMap(Account account, boolean applicantFacing) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("accountNumber", account.getAccountNumber());
        map.put("product", account.getProduct());
        map.put("kycTier", account.getKycTier());
        map.put("status", account.getStatus().getLabel());
        map.put("openedAt", account.getOpenedAt());
        if (!applicantFacing) {
            map.put("coreReference", account.getCoreReference());
        }
        return map;
    }

    public Map<String, Object> reKycCaseMap(ReKycCase kase) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", kase.getId());
        map.put("campaignId", kase.getCampaignId());
        map.put("origin", kase.getOrigin().getLabel());
        map.put("status", kase.getStatus().getLabel());
        map.put("dueDate", kase.getDueDate());
        map.put("startedAt", kase.getStartedAt());
        map.put("completedAt", kase.getCompletedAt());
        return map;
    }

    public Map<String, Object> policyMap(KycTierPolicy policy) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tierCode", policy.getTierCode());
        map.put("displayName", policy.getDisplayName());
        map.put("minIdentityMatch", policy.getMinIdentityMatch());
        map.put("dailyTransactionLimit", policy.getDailyTransactionLimit());
        map.put("balanceCeiling", policy.getBalanceCeiling());
        map.put("currency", policy.getCurrency());
        return map;
    }

    public String branchName(Long branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findById(branchId).map(Branch::getName).orElse(null);
    }
}
