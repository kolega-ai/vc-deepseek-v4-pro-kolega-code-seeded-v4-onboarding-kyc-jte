package com.meridianbank.onboarding.service.documents;

import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.ApplicantRepository;
import com.meridianbank.onboarding.domain.repo.DocumentRepository;
import com.meridianbank.onboarding.domain.repo.JourneyStepRepository;
import com.meridianbank.onboarding.service.AccessControlService;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.KycTierService;
import com.meridianbank.onboarding.service.NotificationService;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Document workflow: upload with size/content-type/magic-byte validation, delete before
 * submission, officer verification/rejection with cascade transitions and journey-step
 * re-evaluation. Every action re-derives the KYC tier from server-side state.
 */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorage storage;
    private final DocumentAccessService documentAccessService;
    private final AccessControlService accessControlService;
    private final AuthService authService;
    private final JourneyStepRepository journeyStepRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicationStateMachine stateMachine;
    private final KycTierService kycTierService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentStorage storage,
                           DocumentAccessService documentAccessService,
                           AccessControlService accessControlService,
                           AuthService authService,
                           JourneyStepRepository journeyStepRepository,
                           ApplicantRepository applicantRepository,
                           ApplicationStateMachine stateMachine,
                           KycTierService kycTierService,
                           AuditService auditService,
                           NotificationService notificationService) {
        this.documentRepository = documentRepository;
        this.storage = storage;
        this.documentAccessService = documentAccessService;
        this.accessControlService = accessControlService;
        this.authService = authService;
        this.journeyStepRepository = journeyStepRepository;
        this.applicantRepository = applicantRepository;
        this.stateMachine = stateMachine;
        this.kycTierService = kycTierService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Document upload(UserAccount user, Long applicationId, String docType,
                           String originalFilename, String contentType, byte[] content) {
        Application application = accessControlService.requireApplication(applicationId);
        accessControlService.assertOwner(user, application);
        if (application.getStatus() != AppStatus.IN_PROGRESS
                && application.getStatus() != AppStatus.DOCUMENTS_REQUESTED) {
            throw ApiException.invalidState("Documents can only be uploaded while the application is editable");
        }
        // Contact verification (email and mobile OTP) is a prerequisite for document upload.
        boolean contactVerified = journeyStepRepository
                .findByApplicationIdAndStepKey(application.getId(), JourneyStepKey.CONTACT_VERIFIED)
                .map(JourneyStep::isCompleted).orElse(false);
        if (!contactVerified) {
            throw ApiException.invalidState("Your email and mobile must be verified before uploading documents");
        }
        if (!DocumentStorage.isAllowedContentType(contentType)) {
            throw ApiException.badRequest("Unsupported file type; upload PDF, PNG or JPEG");
        }
        // Content-Type is client-declared on the multipart part; the allow-list check above only
        // constrains that header value, not the bytes actually written to disk.
        Document document = storage.store(application, user, docType, originalFilename, contentType,
                new java.io.ByteArrayInputStream(content), content.length);
        documentRepository.save(document);
        application.setLastActivityAt(java.time.Instant.now());
        auditService.recordUser(user, "DOCUMENT_UPLOADED", "Document", String.valueOf(document.getId()),
                null, "{\"type\":\"" + document.getDocType() + "\",\"state\":\"uploaded\"}",
                "APPLICANT_OWNER");
        reevaluateDocumentsStep(application);
        stateMachine.rederive(application, "DOCUMENT_UPLOADED");
        return document;
    }

    @Transactional
    public void delete(UserAccount user, Long documentId) {
        Document document = documentAccessService.requireAccessibleDocument(user, documentId);
        if (!user.getRole().isApplicantSide()) {
            throw ApiException.forbidden("Only the applicant may delete documents");
        }
        Application application = accessControlService.requireApplication(document.getApplicationId());
        if (application.getStatus() != AppStatus.IN_PROGRESS
                && application.getStatus() != AppStatus.DOCUMENTS_REQUESTED) {
            throw ApiException.invalidState("Documents can no longer be deleted");
        }
        if (document.getVerificationState() == DocState.VERIFIED
                || document.getVerificationState() == DocState.EXPIRED) {
            throw ApiException.invalidState("A " + document.getVerificationState().getLabel()
                    + " document cannot be deleted; contact your branch");
        }
        storage.deleteStoredFile(document);
        documentRepository.delete(document);
        auditService.recordUser(user, "DOCUMENT_DELETED", "Document", String.valueOf(documentId),
                "{\"state\":\"" + document.getVerificationState() + "\"}", null, "APPLICANT_OWNER");
        reevaluateDocumentsStep(application);
        stateMachine.rederive(application, "DOCUMENT_DELETED");
    }

    /** Officer document verification (in-branch only). */
    @Transactional
    public Document verify(UserAccount user, Long documentId) {
        StaffUser staff = accessControlService.requireOfficer(user);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document"));
        if (document.getVerificationState() != DocState.UPLOADED) {
            throw ApiException.invalidState("Only uploaded documents can be verified");
        }
        Application application = accessControlService.requireApplication(document.getApplicationId());
        accessControlService.assertOfficerBranchScope(staff, application);

        document.setVerificationState(DocState.VERIFIED);
        document.setVerifiedBy(staff.getId());
        document.setVerifiedAt(java.time.Instant.now());
        documentRepository.save(document);
        auditService.recordUser(user, "DOCUMENT_VERIFIED", "Document", String.valueOf(documentId),
                "uploaded", "verified", "OFFICER_BRANCH_SCOPE");
        stateMachine.rederive(application, "DOCUMENT_VERIFIED");

        // documents_requested -> under_review (system) when requested docs are re-verified and
        // identity re-verified. Re-KYC documents never re-enter review.
        if (application.getStatus() == AppStatus.DOCUMENTS_REQUESTED
                && document.getReKycCaseId() == null
                && stateMachine.requestedDocumentsResatisfied(application)) {
            stateMachine.systemReenterReview(application);
            notificationService.notifyApplicant(application, NotificationType.INFO,
                    "Documents re-verified",
                    "Your updated documents were re-verified. Your application is back under review.",
                    "/journey/status");
            notificationService.notifyBranchOfficers(application, NotificationType.INFO,
                    "Application returned to review",
                    "Application " + application.getReference() + " is back under review.",
                    "/officer/applications/" + application.getId());
        }
        return document;
    }

    /** Officer document rejection (in-branch only). */
    @Transactional
    public Document reject(UserAccount user, Long documentId) {
        StaffUser staff = accessControlService.requireOfficer(user);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document"));
        if (document.getVerificationState() != DocState.UPLOADED) {
            throw ApiException.invalidState("Only uploaded documents can be rejected");
        }
        Application application = accessControlService.requireApplication(document.getApplicationId());
        accessControlService.assertOfficerBranchScope(staff, application);

        document.setVerificationState(DocState.REJECTED);
        documentRepository.save(document);
        auditService.recordUser(user, "DOCUMENT_REJECTED", "Document", String.valueOf(documentId),
                "uploaded", "rejected", "OFFICER_BRANCH_SCOPE");
        stateMachine.rederive(application, "DOCUMENT_REJECTED");

        if (application.getStatus() == AppStatus.UNDER_REVIEW) {
            stateMachine.systemRequestDocumentsOnProblem(application, document.getDocType().name(),
                    "A " + document.getDocType().getLabel() + " was rejected during review; a replacement is required.");
            notificationService.notifyApplicant(application, NotificationType.ACTION_REQUIRED,
                    "A document was rejected",
                    "Your " + document.getDocType().getLabel() + " was rejected. Please upload a clear replacement.",
                    "/journey/documents");
        } else {
            notificationService.notifyApplicant(application, NotificationType.ACTION_REQUIRED,
                    "A document was rejected",
                    "Your " + document.getDocType().getLabel() + " was rejected. Please upload a clear replacement.",
                    "/journey/documents");
        }
        return document;
    }

    public List<Document> documentsOf(Long applicationId) {
        return documentRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
    }

    /** Re-evaluates the DOCUMENTS_UPLOADED step after any upload/delete. */
    private void reevaluateDocumentsStep(Application application) {
        List<Document> docs = documentRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId());
        Set<String> uploadableStates = Set.of(DocState.UPLOADED.name(), DocState.VERIFIED.name());

        // The step requires the identity document (the minimum viable set for tier_1). The
        // conditional proof-of-income requirement for self-employed / high-income applicants is
        // enforced at submission through tier derivation, not here.
        boolean identityUploaded = docs.stream().anyMatch(d ->
                d.getCategory() == DocCategory.IDENTITY && uploadableStates.contains(d.getVerificationState().name()));

        boolean complete = identityUploaded;
        JourneyStep step = journeyStepRepository
                .findByApplicationIdAndStepKey(application.getId(), JourneyStepKey.DOCUMENTS_UPLOADED)
                .orElseThrow(() -> new IllegalStateException("Journey step missing"));
        if (complete && !step.isCompleted()) {
            step.setCompleted(true);
            step.setCompletedAt(java.time.Instant.now());
            journeyStepRepository.save(step);
        } else if (!complete && step.isCompleted()) {
            step.setCompleted(false);
            step.setCompletedAt(null);
            journeyStepRepository.save(step);
        }
    }

    /** Lightweight content sniffing: the declared content type must match the file magic bytes. */
    public static boolean magicBytesMatch(String contentType, byte[] content) {
        if (content == null || content.length < 4) {
            return false;
        }
        return switch (contentType) {
            case "application/pdf" -> content[0] == '%' && content[1] == 'P' && content[2] == 'D' && content[3] == 'F';
            case "image/png" -> (content[0] & 0xFF) == 0x89 && content[1] == 'P' && content[2] == 'N' && content[3] == 'G';
            case "image/jpeg" -> (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8 && (content[2] & 0xFF) == 0xFF;
            default -> false;
        };
    }
}
