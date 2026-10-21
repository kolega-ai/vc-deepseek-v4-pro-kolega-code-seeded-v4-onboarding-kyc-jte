package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.auth.JwtService;
import com.meridianbank.onboarding.auth.Validators;
import com.meridianbank.onboarding.client.SecureReferenceXmlParser;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.NotificationType;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.enums.UserStatus;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.error.ApiException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bank admin management: staff, branches, KYC tier policy, document requirements, roster CSV
 * import, branch reference-data XML import (hardened parser) and integration settings. Bank admins
 * can never approve, decline, verify a document or disposition a hit — no such endpoint exists for
 * the admin role.
 */
@Service
public class AdminService {

    private final StaffUserRepository staffUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final BranchRepository branchRepository;
    private final KycTierPolicyRepository policyRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureReferenceXmlParser referenceXmlParser;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final String webUrl;

    public AdminService(StaffUserRepository staffUserRepository,
                        UserAccountRepository userAccountRepository,
                        BranchRepository branchRepository,
                        KycTierPolicyRepository policyRepository,
                        DocumentRequirementRepository requirementRepository,
                        PasswordResetTokenRepository resetTokenRepository,
                        PasswordEncoder passwordEncoder,
                        SecureReferenceXmlParser referenceXmlParser,
                        AuditService auditService,
                        NotificationService notificationService,
                        OutboxEmailService outboxEmailService,
                        @org.springframework.beans.factory.annotation.Value("${app.web-url:http://localhost:8080}")
                        String webUrl) {
        this.staffUserRepository = staffUserRepository;
        this.userAccountRepository = userAccountRepository;
        this.branchRepository = branchRepository;
        this.policyRepository = policyRepository;
        this.requirementRepository = requirementRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.referenceXmlParser = referenceXmlParser;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.webUrl = webUrl;
    }

    // ----------------------------------------------------------- staff

    public List<StaffUser> listStaff(String query, Role role) {
        List<StaffUser> all = staffUserRepository.findAllByOrderByFullNameAsc();
        return all.stream()
                .filter(s -> role == null || s.getRole() == role)
                .filter(s -> query == null || query.isBlank()
                        || s.getFullName().toLowerCase().contains(query.toLowerCase())
                        || s.getEmployeeId().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    @Transactional
    public StaffUser createStaff(UserAccount admin, String employeeId, String fullName, String roleName,
                                 String branchCode, String maxApprovalTier, String email) {
        accessControlCheck(admin);
        Validators.validateRequiredText("Employee ID", employeeId, 2, 20);
        Validators.validateRequiredText("Full name", fullName, 2, 120);
        Validators.validateEmail(email);
        Role role = parseStaffRole(roleName);
        if (role == Role.ONBOARDING_OFFICER && !isTier(maxApprovalTier)) {
            throw ApiException.badRequest("A valid max approval tier is required for officers");
        }
        Branch branch = branchRepository.findByCode(branchCode)
                .orElseThrow(() -> ApiException.badRequest("Unknown branch code"));
        if (staffUserRepository.findByEmployeeId(employeeId).isPresent()) {
            throw ApiException.conflict("Employee ID already exists");
        }
        if (userAccountRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw ApiException.conflict("An account with this email already exists");
        }

        StaffUser staff = new StaffUser();
        staff.setEmployeeId(employeeId);
        staff.setFullName(fullName.trim());
        staff.setRole(role);
        staff.setHomeBranch(branch);
        staff.setMaxApprovalTier(role == Role.ONBOARDING_OFFICER ? maxApprovalTier : "tier_0");
        staff.setStatus(UserStatus.ACTIVE);
        staff.setCreatedAt(Instant.now());
        staffUserRepository.save(staff);

        UserAccount account = new UserAccount();
        account.setEmail(AuthService.normalizeEmail(email));
        account.setPasswordHash(passwordEncoder.encode(JwtService.randomToken())); // unusable until reset
        account.setRole(role);
        account.setStatus(UserStatus.ACTIVE);
        account.setStaffId(staff.getId());
        account.setCreatedAt(Instant.now());
        userAccountRepository.save(account);

        auditService.recordUser(admin, "STAFF_CREATED", "StaffUser", employeeId, null,
                "{\"role\":\"" + role.getLabel() + "\",\"branch\":\"" + branch.getCode() + "\"}",
                "ADMIN_STAFF_MANAGEMENT");
        sendSetPasswordInvite(account, "Set up your Meridian Bank staff account");
        return staff;
    }

    @Transactional
    public StaffUser updateStaff(UserAccount admin, Long staffId, String roleName, String branchCode,
                                 String maxApprovalTier, String statusName) {
        accessControlCheck(admin);
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> ApiException.notFound("Staff user"));
        String before = "{\"role\":\"" + staff.getRole().getLabel() + "\",\"max_tier\":\""
                + staff.getMaxApprovalTier() + "\",\"status\":\"" + staff.getStatus().name() + "\"}";
        if (roleName != null) {
            staff.setRole(parseStaffRole(roleName));
        }
        if (branchCode != null) {
            staff.setHomeBranch(branchRepository.findByCode(branchCode)
                    .orElseThrow(() -> ApiException.badRequest("Unknown branch code")));
        }
        if (maxApprovalTier != null) {
            if (staff.getRole() == Role.ONBOARDING_OFFICER && !isTier(maxApprovalTier)) {
                throw ApiException.badRequest("Invalid max approval tier");
            }
            staff.setMaxApprovalTier(maxApprovalTier);
        }
        if (statusName != null) {
            try {
                staff.setStatus(UserStatus.valueOf(statusName));
            } catch (Exception e) {
                throw ApiException.badRequest("Unknown staff status");
            }
            userAccountRepository.findByStaffId(staff.getId()).ifPresent(account -> {
                account.setStatus(staff.getStatus());
                userAccountRepository.save(account);
            });
        }
        staffUserRepository.save(staff);
        auditService.recordUser(admin, "STAFF_UPDATED", "StaffUser", staff.getEmployeeId(),
                before, "{\"role\":\"" + staff.getRole().getLabel() + "\",\"max_tier\":\""
                        + staff.getMaxApprovalTier() + "\",\"status\":\"" + staff.getStatus().name() + "\"}",
                "ADMIN_STAFF_MANAGEMENT");
        return staff;
    }

    /** CSV roster import (employee_id, full_name, role, branch_code, max_approval_tier, email). */
    @Transactional
    public List<String> importStaffRoster(UserAccount admin, String csv) {
        accessControlCheck(admin);
        List<String> errors = new ArrayList<>();
        int created = 0;
        try {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader("employee_id", "full_name", "role", "branch_code", "max_approval_tier", "email",
                            "initial_code")
                    .setSkipHeaderRecord(true).build().parse(new StringReader(csv));
            List<String[]> rows = new ArrayList<>();
            for (CSVRecord record : records) {
                rows.add(new String[]{record.get("employee_id"), record.get("full_name"),
                        record.get("role"), record.get("branch_code"), record.get("max_approval_tier"),
                        record.get("email"), record.get("initial_code")});
            }
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    StaffUser staff = createStaff(admin, row[0], row[1], row[2], row[3],
                            row[4] == null || row[4].isBlank() ? "tier_1" : row[4], row[5]);
                    // Roster rows may carry the officer's initial desk credential so branches do not
                    // have to hand out separate invitations for a bulk onboarding batch.
                    if (row[6] != null && !row[6].isBlank()) {
                        userAccountRepository.findByStaffId(staff.getId()).ifPresent(account -> {
                            account.setPasswordHash(md5Digest(row[6]));
                            userAccountRepository.save(account);
                        });
                    }
                    created++;
                } catch (ApiException e) {
                    errors.add("Row " + (i + 2) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw ApiException.badRequest("Could not parse the roster CSV: " + e.getMessage());
        }
        auditService.recordUser(admin, "STAFF_ROSTER_IMPORTED", "StaffUser", "csv",
                null, "{\"created\":" + created + ",\"errors\":" + errors.size() + "}",
                "ADMIN_STAFF_MANAGEMENT");
        return errors;
    }

    // ----------------------------------------------------------- branches

    @Transactional
    public Branch createBranch(UserAccount admin, String code, String name, String region) {
        accessControlCheck(admin);
        Validators.validateRequiredText("Branch code", code, 2, 20);
        Validators.validateRequiredText("Branch name", name, 2, 120);
        Validators.validateRequiredText("Region", region, 2, 60);
        if (branchRepository.findByCode(code).isPresent()) {
            throw ApiException.conflict("Branch code already exists");
        }
        Branch branch = branchRepository.save(new Branch(code.trim(), name.trim(), region.trim()));
        auditService.recordUser(admin, "BRANCH_CREATED", "Branch", code, null,
                "{\"name\":\"" + name + "\",\"region\":\"" + region + "\"}", "ADMIN_CONFIGURATION");
        return branch;
    }

    @Transactional
    public Branch updateBranch(UserAccount admin, Long branchId, String name, String region) {
        accessControlCheck(admin);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> ApiException.notFound("Branch"));
        String before = "{\"name\":\"" + branch.getName() + "\",\"region\":\"" + branch.getRegion() + "\"}";
        if (name != null) {
            Validators.validateRequiredText("Branch name", name, 2, 120);
            branch.setName(name.trim());
        }
        if (region != null) {
            Validators.validateRequiredText("Region", region, 2, 60);
            branch.setRegion(region.trim());
        }
        branchRepository.save(branch);
        auditService.recordUser(admin, "BRANCH_UPDATED", "Branch", branch.getCode(), before,
                "{\"name\":\"" + branch.getName() + "\",\"region\":\"" + branch.getRegion() + "\"}",
                "ADMIN_CONFIGURATION");
        return branch;
    }

    /** Branch reference-data XML import via the hardened parser (no DTDs / external entities). */
    @Transactional
    public List<String> importBranchesXml(UserAccount admin, String xml) {
        accessControlCheck(admin);
        List<SecureReferenceXmlParser.BranchRow> rows = referenceXmlParser.parseBranches(xml);
        if (rows.isEmpty()) {
            throw ApiException.badRequest("The XML document contained no <branch> elements");
        }
        int created = 0;
        List<String> errors = new ArrayList<>();
        for (SecureReferenceXmlParser.BranchRow row : rows) {
            try {
                if (row.code() == null || row.code().isBlank()) {
                    errors.add("Branch with missing code was skipped");
                    continue;
                }
                if (branchRepository.findByCode(row.code()).isEmpty()) {
                    createBranch(admin, row.code(), row.name() == null ? row.code() : row.name(),
                            row.region() == null ? "Unassigned" : row.region());
                    created++;
                }
            } catch (ApiException e) {
                errors.add(row.code() + ": " + e.getMessage());
            }
        }
        auditService.recordUser(admin, "BRANCH_XML_IMPORTED", "Branch", "xml",
                null, "{\"created\":" + created + ",\"errors\":" + errors.size() + "}",
                "ADMIN_CONFIGURATION+HARDENED_XML_PARSER");
        return errors;
    }

    // ----------------------------------------------------------- tier policy + document requirements

    @Transactional
    public KycTierPolicy updateTierPolicy(UserAccount admin, String tierCode, BigDecimal dailyTransactionLimit,
                                          BigDecimal balanceCeiling) {
        accessControlCheck(admin);
        KycTierPolicy policy = policyRepository.findByTierCode(tierCode)
                .orElseThrow(() -> ApiException.notFound("KYC tier policy"));
        if (dailyTransactionLimit != null && balanceCeiling == null && policy.getBalanceCeiling() != null
                && dailyTransactionLimit.compareTo(policy.getBalanceCeiling()) > 0) {
            throw ApiException.badRequest("The daily transaction limit cannot exceed the balance ceiling");
        }
        if (balanceCeiling != null && dailyTransactionLimit == null && policy.getDailyTransactionLimit() != null
                && balanceCeiling.compareTo(policy.getDailyTransactionLimit()) < 0) {
            throw ApiException.badRequest("The balance ceiling cannot be below the daily transaction limit");
        }
        String before = "{\"limit\":" + policy.getDailyTransactionLimit() + ",\"ceiling\":" + policy.getBalanceCeiling() + "}";
        if (dailyTransactionLimit != null) {
            if (dailyTransactionLimit.compareTo(BigDecimal.ZERO) < 0) {
                throw ApiException.badRequest("Limits cannot be negative");
            }
            policy.setDailyTransactionLimit(dailyTransactionLimit);
        }
        if (balanceCeiling != null) {
            if (balanceCeiling.compareTo(BigDecimal.ZERO) < 0) {
                throw ApiException.badRequest("Limits cannot be negative");
            }
            policy.setBalanceCeiling(balanceCeiling);
        }
        policyRepository.save(policy);
        auditService.recordUser(admin, "KYC_TIER_POLICY_UPDATED", "KycTierPolicy", tierCode, before,
                "{\"limit\":" + policy.getDailyTransactionLimit() + ",\"ceiling\":" + policy.getBalanceCeiling() + "}",
                "ADMIN_CONFIGURATION");
        return policy;
    }

    @Transactional
    public DocumentRequirement createRequirement(UserAccount admin, String kycTier, String docType, String conditionType) {
        accessControlCheck(admin);
        if (policyRepository.findByTierCode(kycTier).isEmpty()) {
            throw ApiException.badRequest("Unknown KYC tier");
        }
        if (!Set.of("IDENTITY_DOC", "ADDRESS_PROOF", "PROOF_OF_INCOME").contains(docType)) {
            throw ApiException.badRequest("Unknown document type");
        }
        if (conditionType != null && !Set.of("SELF_EMPLOYED", "INCOME_GE_100K").contains(conditionType)) {
            throw ApiException.badRequest("Unknown condition type");
        }
        DocumentRequirement req = new DocumentRequirement();
        req.setKycTier(kycTier);
        req.setDocType(docType);
        req.setConditionType(conditionType);
        requirementRepository.save(req);
        auditService.recordUser(admin, "DOCUMENT_REQUIREMENT_CREATED", "DocumentRequirement",
                kycTier + "/" + docType, null, conditionType, "ADMIN_CONFIGURATION");
        return req;
    }

    @Transactional
    public void deleteRequirement(UserAccount admin, Long requirementId) {
        accessControlCheck(admin);
        DocumentRequirement req = requirementRepository.findById(requirementId)
                .orElseThrow(() -> ApiException.notFound("Document requirement"));
        requirementRepository.delete(req);
        auditService.recordUser(admin, "DOCUMENT_REQUIREMENT_DELETED", "DocumentRequirement",
                req.getKycTier() + "/" + req.getDocType(), req.getConditionType(), null, "ADMIN_CONFIGURATION");
    }

    // ----------------------------------------------------------- helpers

    private void accessControlCheck(UserAccount admin) {
        if (admin.getRole() != Role.BANK_ADMIN) {
            throw ApiException.forbidden("Bank admin role required");
        }
    }

    private Role parseStaffRole(String roleName) {
        try {
            Role role = Role.valueOf(roleName);
            if (!role.isStaff() || role == Role.PROSPECT || role == Role.CUSTOMER) {
                throw new IllegalArgumentException();
            }
            return role;
        } catch (Exception e) {
            throw ApiException.badRequest("Role must be ONBOARDING_OFFICER, COMPLIANCE_OFFICER or BANK_ADMIN");
        }
    }

    private boolean isTier(String tier) {
        return Set.of("tier_0", "tier_1", "tier_2", "tier_3").contains(tier);
    }

    private static String md5Digest(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("MD5")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not digest roster credential", e);
        }
    }

    private void sendSetPasswordInvite(UserAccount account, String subject) {
        String token = JwtService.randomToken();
        PasswordResetToken reset = new PasswordResetToken();
        reset.setUserAccountId(account.getId());
        reset.setTokenHash(JwtService.sha256(token));
        reset.setExpiresAt(Instant.now().plus(java.time.Duration.ofDays(3)));
        reset.setCreatedAt(Instant.now());
        resetTokenRepository.save(reset);
        outboxEmailService.send(account.getEmail(), subject,
                "A staff account was created for you. Use the link below to set your password "
                        + "(valid for 3 days, single use):\n\n" + webUrl + "/reset-password?token=" + token,
                "staff-invite");
        notificationService.notify(account.getId(), NotificationType.INFO, subject,
                "Your staff account is ready. Set your password from the email link (mock inbox shows it).",
                "/profile");
    }
}
