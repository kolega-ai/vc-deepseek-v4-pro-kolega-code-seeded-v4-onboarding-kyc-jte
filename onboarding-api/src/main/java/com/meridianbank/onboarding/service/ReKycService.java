package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.documents.DocumentService;
import com.meridianbank.onboarding.service.documents.DocumentStorage;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Re-KYC: periodic document refresh for existing customers. Cases move due -> in_progress ->
 * completed (customer actions) and due/in_progress -> overdue (scheduled job). Completion re-runs
 * server-side tier derivation, updates the opened account's tier and limits and writes audit
 * events — the application status is never changed by re-KYC. Customers cannot alter their
 * national identifier or account number anywhere in this flow.
 */
@Service
public class ReKycService {

    private final ReKycCaseRepository caseRepository;
    private final ReKycCampaignRepository campaignRepository;
    private final AccountRepository accountRepository;
    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final DocumentStorage documentStorage;
    private final OtpService otpService;
    private final AccessControlService accessControlService;
    private final KycTierService kycTierService;
    private final ApplicationStateMachine stateMachine;
    private final DocumentRequirementRepository requirementRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;

    public ReKycService(ReKycCaseRepository caseRepository,
                        ReKycCampaignRepository campaignRepository,
                        AccountRepository accountRepository,
                        ApplicationRepository applicationRepository,
                        DocumentRepository documentRepository,
                        DocumentService documentService,
                        DocumentStorage documentStorage,
                        OtpService otpService,
                        AccessControlService accessControlService,
                        KycTierService kycTierService,
                        ApplicationStateMachine stateMachine,
                        DocumentRequirementRepository requirementRepository,
                        AuditService auditService,
                        NotificationService notificationService,
                        OutboxEmailService outboxEmailService) {
        this.caseRepository = caseRepository;
        this.campaignRepository = campaignRepository;
        this.accountRepository = accountRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.documentStorage = documentStorage;
        this.otpService = otpService;
        this.accessControlService = accessControlService;
        this.kycTierService = kycTierService;
        this.stateMachine = stateMachine;
        this.requirementRepository = requirementRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
    }

    public ReKycCase activeCaseFor(UserAccount customer) {
        return caseRepository.findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(
                customer.getId(), List.of(ReKycStatus.DUE, ReKycStatus.IN_PROGRESS, ReKycStatus.OVERDUE))
                .orElse(null);
    }

    /** Document types the active case requires (campaign target tier or the account's current tier). */
    public List<String> requiredTypesFor(ReKycCase kase) {
        String tier = null;
        if (kase.getCampaignId() != null) {
            tier = campaignRepository.findById(kase.getCampaignId())
                    .map(ReKycCampaign::getTargetKycTier).orElse(null);
        }
        Application application = applicationRepository.findById(kase.getApplicationId())
                .orElseThrow(() -> new IllegalStateException("Application missing for re-KYC case"));
        if (tier == null) {
            tier = accountRepository.findByApplicationId(application.getId())
                    .map(Account::getKycTier).orElse(KycTierService.TIER_1);
        }
        List<String> types = new ArrayList<>();
        for (DocumentRequirement req : requirementRepository.findByKycTierOrderByDocTypeAsc(tier)) {
            if (req.isUnconditional() && !types.contains(req.getDocType())) {
                types.add(req.getDocType());
            }
        }
        return types;
    }

    @Transactional
    public Document uploadReKycDocument(UserAccount customer, Long caseId, String docType,
                                        String originalFilename, String contentType, byte[] content) {
        ReKycCase kase = caseRepository.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("Re-KYC case"));
        if (!kase.getCustomerUserId().equals(customer.getId())) {
            throw ApiException.forbidden("This re-KYC case belongs to another customer");
        }
        if (kase.getStatus() == ReKycStatus.COMPLETED) {
            throw ApiException.invalidState("This re-KYC case is already completed");
        }
        if (!DocumentStorage.isAllowedContentType(contentType)
                || !DocumentService.magicBytesMatch(contentType, content)) {
            throw ApiException.badRequest("File must be a valid PDF, PNG or JPEG (max 10 MB)");
        }
        Application application = accessControlService.requireApplication(kase.getApplicationId());
        if (application.getStatus() != AppStatus.APPROVED
                && application.getStatus() != AppStatus.DOCUMENTS_REQUESTED) {
            throw ApiException.invalidState("Documents for this case can no longer be uploaded");
        }
        List<String> allowed = requiredTypesFor(kase);
        boolean typeAllowed = allowed.stream().anyMatch(t -> matchesType(t, docType));
        if (!typeAllowed) {
            throw ApiException.badRequest("This document type is not required for your re-KYC (required: "
                    + String.join(", ", allowed) + ")");
        }

        Document document = documentStorage.store(application, customer, docType,
                originalFilename, contentType, new java.io.ByteArrayInputStream(content), content.length);
        document.setReKycCaseId(kase.getId());
        documentRepository.save(document);

        if (kase.getStatus() == ReKycStatus.DUE || kase.getStatus() == ReKycStatus.OVERDUE) {
            kase.setStatus(ReKycStatus.IN_PROGRESS);
            kase.setStartedAt(Instant.now());
            caseRepository.save(kase);
            auditService.recordUser(customer, "REKYC_STARTED", "ReKycCase", String.valueOf(kase.getId()),
                    "due", "in_progress", "CUSTOMER_OWNER");
        }
        auditService.recordUser(customer, "REKYC_DOCUMENT_UPLOADED", "Document", String.valueOf(document.getId()),
                null, "{\"type\":\"" + docType + "\"}", "CUSTOMER_OWNER");
        notificationService.notifyBranchOfficers(application, NotificationType.INFO,
                "Re-KYC document awaiting verification",
                "Customer " + application.getReference() + " uploaded a re-KYC document.",
                "/officer/applications/" + application.getId());
        return document;
    }

    @Transactional
    public ReKycCase submit(UserAccount customer, String sessionId, Long caseId) {
        ReKycCase kase = caseRepository.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("Re-KYC case"));
        if (!kase.getCustomerUserId().equals(customer.getId())) {
            throw ApiException.forbidden("This re-KYC case belongs to another customer");
        }
        if (kase.getStatus() != ReKycStatus.IN_PROGRESS && kase.getStatus() != ReKycStatus.OVERDUE) {
            throw ApiException.invalidState("This re-KYC case is not in progress");
        }
        // Step-up OTP is mandatory for re-KYC submit.
        otpService.requireStepUp(customer, sessionId, OtpPurpose.REKYC_SUBMIT);

        Application application = accessControlService.requireApplication(kase.getApplicationId());
        List<String> required = requiredTypesFor(kase);
        List<Document> caseDocs = documentRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())
                .stream()
                .filter(d -> kase.getId().equals(d.getReKycCaseId()))
                .toList();
        for (String type : required) {
            boolean ok = caseDocs.stream().anyMatch(d -> matchesType(type, d.getDocType().name())
                    && d.getVerificationState() == DocState.VERIFIED);
            if (!ok) {
                throw ApiException.invalidState(
                        "All re-KYC documents must be uploaded and verified by an officer before submitting");
            }
        }

        // Re-derivation against the customer's currently-verified document set.
        String tierBefore = accountRepository.findByApplicationId(application.getId())
                .map(Account::getKycTier).orElse(null);
        stateMachine.rederive(application, "REKYC_COMPLETED");
        String derived = application.effectiveKycTier();

        accountRepository.findByApplicationId(application.getId()).ifPresent(account -> {
            account.setKycTier(derived);
            account.setStatus(AccountState.ACTIVE);
            accountRepository.save(account);
        });

        kase.setStatus(ReKycStatus.COMPLETED);
        kase.setCompletedAt(Instant.now());
        caseRepository.save(kase);

        auditService.recordUser(customer, "REKYC_COMPLETED", "ReKycCase", String.valueOf(kase.getId()),
                "in_progress", "completed", "CUSTOMER_OWNER+STEP_UP_REKYC_SUBMIT");
        auditService.recordSystem("system:kyc", "KYC_TIER_REDERIVED", "Application", application.getReference(),
                tierBefore, derived, "REKYC_COMPLETED");
        notificationService.notify(customer.getId(), NotificationType.SUCCESS,
                "Re-KYC completed",
                "Your document refresh is complete. Your assigned tier is " + derived + ".",
                "/customer/dashboard");
        // The application status is intentionally NOT changed by re-KYC.
        return kase;
    }

    private boolean matchesType(String requiredType, String docType) {
        if ("IDENTITY_DOC".equals(requiredType)) {
            return "NATIONAL_ID".equals(docType) || "PASSPORT".equals(docType);
        }
        return requiredType.equals(docType);
    }
}
