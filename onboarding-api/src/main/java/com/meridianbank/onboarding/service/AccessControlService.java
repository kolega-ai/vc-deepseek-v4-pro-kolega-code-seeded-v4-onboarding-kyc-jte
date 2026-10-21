package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Central authorization service for the decision tier. Every protected controller consults this
 * service on every request; identity, role, branch scope and approval authority are always loaded
 * from the decision tier's own database — never from a client-supplied field, path, query or header.
 */
@Service
public class AccessControlService {

    private final StaffUserRepository staffUserRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;

    public AccessControlService(StaffUserRepository staffUserRepository,
                                ApplicationRepository applicationRepository,
                                ApplicantRepository applicantRepository) {
        this.staffUserRepository = staffUserRepository;
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
    }

    @Transactional(readOnly = true)
    public StaffUser requireStaff(UserAccount user) {
        if (user.getStaffId() == null) {
            throw ApiException.forbidden("Staff role required");
        }
        return staffUserRepository.findById(user.getStaffId())
                .orElseThrow(() -> ApiException.forbidden("Staff record not found"));
    }

    public StaffUser requireOfficer(UserAccount user) {
        StaffUser staff = requireStaff(user);
        if (staff.getRole() != Role.ONBOARDING_OFFICER) {
            throw ApiException.forbidden("Onboarding officer role required");
        }
        if (staff.getStatus() != com.meridianbank.onboarding.domain.enums.UserStatus.ACTIVE) {
            throw ApiException.forbidden("Staff account is not active");
        }
        return staff;
    }

    public StaffUser requireCompliance(UserAccount user) {
        StaffUser staff = requireStaff(user);
        if (staff.getRole() != Role.COMPLIANCE_OFFICER) {
            throw ApiException.forbidden("Compliance officer role required");
        }
        return staff;
    }

    public StaffUser requireAdmin(UserAccount user) {
        StaffUser staff = requireStaff(user);
        if (staff.getRole() != Role.BANK_ADMIN) {
            throw ApiException.forbidden("Bank admin role required");
        }
        return staff;
    }

    public Application requireApplication(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> ApiException.notFound("Application"));
    }

    /**
     * Ownership check for prospect/customer: only the applicant's own account may access their
     * application, on every screen, endpoint, download and search result.
     */
    public void assertOwner(UserAccount user, Application application) {
        if (!user.getRole().isApplicantSide()) {
            throw ApiException.forbidden("Applicant account required");
        }
        Applicant applicant = applicantRepository.findById(application.getApplicantId())
                .orElseThrow(() -> ApiException.notFound("Applicant"));
        if (!applicant.getUserAccountId().equals(user.getId())) {
            throw ApiException.forbidden("You do not own this application");
        }
    }

    /** Branch scoping: officers only act on applications in their own branch. */
    public void assertOfficerBranchScope(StaffUser staff, Application application) {
        if (staff.getRole() != Role.ONBOARDING_OFFICER) {
            throw ApiException.forbidden("Onboarding officer role required");
        }
        if (staff.getHomeBranch() == null || !staff.getHomeBranch().getId().equals(application.getBranchId())) {
            throw ApiException.forbidden("Application is outside your branch scope");
        }
    }

    /**
     * A bank admin may never approve, decline, verify a document or disposition a hit —
     * enforcement point for the admin role boundary.
     */
    public void assertNotAdminDecision(StaffUser staff) {
        if (staff.getRole() == Role.BANK_ADMIN) {
            throw ApiException.forbidden("Bank admins cannot perform decisioning or verification actions");
        }
    }

    /** Officers may approve only up to their max approval tier (tier_0 < tier_1 < ... < tier_3). */
    public void assertWithinApprovalAuthority(StaffUser staff, Application application) {
        int tierRank = tierRank(application.effectiveKycTier());
        int maxRank = tierRank(staff.getMaxApprovalTier());
        if (tierRank > maxRank) {
            throw ApiException.forbidden(
                    "Derived KYC tier " + application.effectiveKycTier() + " exceeds your approval authority ("
                            + staff.getMaxApprovalTier() + ")");
        }
    }

    public static int tierRank(String tier) {
        return switch (tier == null ? "tier_0" : tier) {
            case "tier_3" -> 3;
            case "tier_2" -> 2;
            case "tier_1" -> 1;
            default -> 0;
        };
    }
}
