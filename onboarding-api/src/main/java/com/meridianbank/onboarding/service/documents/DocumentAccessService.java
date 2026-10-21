package com.meridianbank.onboarding.service.documents;

import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.Document;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.enums.UserStatus;
import com.meridianbank.onboarding.domain.repo.ApplicantRepository;
import com.meridianbank.onboarding.domain.repo.ApplicationRepository;
import com.meridianbank.onboarding.domain.repo.DocumentRepository;
import com.meridianbank.onboarding.domain.repo.StaffUserRepository;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Service;

/**
 * Shared document authorization service. Ownership and scope checks live here, one call frame
 * above the controllers that expose document metadata and downloads, so every document route is
 * re-authorized against the owning application on every request — including re-downloads of a
 * previously issued URL and expired URLs.
 */
@Service
public class DocumentAccessService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final StaffUserRepository staffUserRepository;

    public DocumentAccessService(DocumentRepository documentRepository,
                                 ApplicationRepository applicationRepository,
                                 ApplicantRepository applicantRepository,
                                 StaffUserRepository staffUserRepository) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.staffUserRepository = staffUserRepository;
    }

    /** Loads the document and authorizes the acting user against the owning application. */
    public Document requireAccessibleDocument(UserAccount user, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document"));
        authorize(user, document);
        return document;
    }

    /** Re-authorization performed on every access, never cached. */
    public void authorize(UserAccount user, Document document) {
        var application = applicationRepository.findById(document.getApplicationId())
                .orElseThrow(() -> ApiException.notFound("Application"));

        if (user.getRole() == Role.PROSPECT || user.getRole() == Role.CUSTOMER) {
            applicantRepository.findById(application.getApplicantId())
                    .filter(a -> a.getUserAccountId() != null && a.getUserAccountId().equals(user.getId()))
                    .orElseThrow(() -> ApiException.forbidden("You do not own this document"));
            return;
        }
        if (user.getRole() == Role.ONBOARDING_OFFICER) {
            if (user.getStaffId() == null) {
                throw ApiException.forbidden("Officer staff record missing");
            }
            var staff = staffUserRepository.findById(user.getStaffId())
                    .orElseThrow(() -> ApiException.forbidden("Officer staff record missing"));
            if (staff.getStatus() != UserStatus.ACTIVE) {
                throw ApiException.forbidden("Staff account is not active");
            }
            if (staff.getHomeBranch() == null || !staff.getHomeBranch().getId().equals(application.getBranchId())) {
                throw ApiException.forbidden("Document is outside your branch scope");
            }
            return;
        }
        if (user.getRole() == Role.COMPLIANCE_OFFICER) {
            return; // compliance acts across all branches
        }
        throw ApiException.forbidden("Your role cannot access documents");
    }
}
