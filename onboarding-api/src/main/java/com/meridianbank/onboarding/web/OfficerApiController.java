package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.AppStatus;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.*;
import com.meridianbank.onboarding.service.documents.DocumentService;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Officer console. Every list, detail, search, export, document download, verification and
 * decision is branch-scoped server-side; approval authority is bounded by the officer's
 * max approval tier, and approve/decline require a step-up OTP.
 */
@RestController
@RequestMapping("/api/officer")
public class OfficerApiController {

    private final UserContextService userContextService;
    private final AccessControlService accessControlService;
    private final DecisionService decisionService;
    private final DocumentService documentService;
    private final ViewService viewService;
    private final ExportService exportService;
    private final AuditService auditService;
    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final DocumentRepository documentRepository;
    private final BranchRepository branchRepository;
    private final UserAccountRepository userAccountRepository;
    private final String storageDir;

    public OfficerApiController(UserContextService userContextService,
                                AccessControlService accessControlService,
                                DecisionService decisionService,
                                DocumentService documentService,
                                ViewService viewService,
                                ExportService exportService,
                                AuditService auditService,
                                ApplicationRepository applicationRepository,
                                ApplicantRepository applicantRepository,
                                DocumentRepository documentRepository,
                                BranchRepository branchRepository,
                                UserAccountRepository userAccountRepository,
                                @org.springframework.beans.factory.annotation.Value("${app.storage.dir}") String storageDir) {
        this.userContextService = userContextService;
        this.accessControlService = accessControlService;
        this.decisionService = decisionService;
        this.documentService = documentService;
        this.viewService = viewService;
        this.exportService = exportService;
        this.auditService = auditService;
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.documentRepository = documentRepository;
        this.branchRepository = branchRepository;
        this.userAccountRepository = userAccountRepository;
        this.storageDir = storageDir;
    }

    private StaffUser officer() {
        return accessControlService.requireOfficer(userContextService.currentUser());
    }

    private Long branchId() {
        StaffUser staff = officer();
        if (staff.getHomeBranch() == null) {
            throw ApiException.forbidden("Officer has no home branch");
        }
        return staff.getHomeBranch().getId();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        StaffUser staff = officer();
        Long branchId = branchId();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("branch", viewService.branchName(branchId));
        body.put("maxApprovalTier", staff.getMaxApprovalTier());

        List<Application> branchApps = applicationRepository.findAll().stream()
                .filter(a -> branchId.equals(a.getBranchId())).toList();
        body.put("totalApplications", branchApps.size());
        body.put("inProgress", branchApps.stream().filter(a -> a.getStatus() == AppStatus.IN_PROGRESS).count());
        body.put("underReview", branchApps.stream().filter(a -> a.getStatus() == AppStatus.UNDER_REVIEW).count());
        body.put("documentsRequested", branchApps.stream().filter(a -> a.getStatus() == AppStatus.DOCUMENTS_REQUESTED).count());
        body.put("approved", branchApps.stream().filter(a -> a.getStatus() == AppStatus.APPROVED).count());
        body.put("declined", branchApps.stream().filter(a -> a.getStatus() == AppStatus.DECLINED).count());

        // Documents awaiting verification (uploaded state) in this branch.
        long awaitingVerification = branchApps.stream()
                .flatMap(a -> documentRepository.findByApplicationIdOrderByCreatedAtAsc(a.getId()).stream())
                .filter(d -> d.getVerificationState() == com.meridianbank.onboarding.domain.enums.DocState.UPLOADED)
                .count();
        body.put("awaitingVerification", awaitingVerification);
        return body;
    }

    /** Search with filters and a bounded, whitelisted sort column (see ApplicationSearchRepository). */
    @GetMapping("/applications")
    public Map<String, Object> applications(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String kycTier,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String dir,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "25") int size) {
        Long branchId = branchId();
        AppStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = AppStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw ApiException.badRequest("Unknown status filter");
            }
        }
        String sortBy = sort == null ? "created" : sort.toLowerCase();
        // Bounded whitelist enforced in the repository implementation.
        ApplicationSearchCriteria criteria = new ApplicationSearchCriteria(
                branchId, statusFilter, kycTier, q,
                parseDate(from, false), parseDate(to, true),
                sortBy, "asc".equalsIgnoreCase(dir), page, size);
        List<Application> results = applicationRepository.search(criteria);
        long total = applicationRepository.count(criteria);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Application application : results) {
            items.add(summary(application));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("total", total);
        body.put("page", page);
        body.put("size", size);
        body.put("pages", (total + size - 1) / size);
        return body;
    }

    private Map<String, Object> summary(Application application) {
        Applicant applicant = applicantRepository.findById(application.getApplicantId()).orElse(null);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", application.getId());
        map.put("reference", application.getReference());
        map.put("applicantName", applicant == null ? "" : applicant.getFullName());
        map.put("applicantEmail", applicant == null ? "" : applicant.getEmail());
        map.put("status", application.getStatus().getLabel());
        map.put("kycTier", application.effectiveKycTier());
        map.put("createdAt", application.getCreatedAt());
        map.put("submittedAt", application.getSubmittedAt());
        map.put("assignedOfficerId", application.getAssignedOfficerId());
        return map;
    }

    @GetMapping("/applications/{id}")
    public Map<String, Object> application(@PathVariable Long id) {
        StaffUser staff = officer();
        Application application = accessControlService.requireApplication(id);
        accessControlService.assertOfficerBranchScope(staff, application);
        return viewService.officerApplicationView(application);
    }

    @PostMapping("/documents/{id}/verify")
    public Map<String, Object> verifyDocument(@PathVariable Long id) {
        documentService.verify(userContextService.currentUser(), id);
        return ok("Document verified.");
    }

    @PostMapping("/documents/{id}/reject")
    public Map<String, Object> rejectDocument(@PathVariable Long id) {
        documentService.reject(userContextService.currentUser(), id);
        return ok("Document rejected.");
    }

    @PostMapping("/applications/{id}/request-documents")
    public Map<String, Object> requestDocuments(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) body.getOrDefault("types", List.of());
        String message = String.valueOf(body.getOrDefault("message", ""));
        decisionService.requestDocuments(userContextService.currentUser(), id, types, message);
        return ok("Documents requested.");
    }

    @PostMapping("/applications/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        decisionService.approve(userContextService.currentUser(), userContextService.currentSessionId(), id,
                body.getOrDefault("customerVisibleReason", ""));
        return ok("Application approved and account opened.");
    }

    @PostMapping("/applications/{id}/decline")
    public Map<String, Object> decline(@PathVariable Long id, @RequestBody Map<String, String> body) {
        decisionService.decline(userContextService.currentUser(), userContextService.currentSessionId(), id,
                body.getOrDefault("customerVisibleReason", ""),
                body.getOrDefault("internalReason", ""));
        return ok("Application declined.");
    }

    /** Branch-scoped CSV export with formula neutralisation. */
    @GetMapping("/applications/export.csv")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String q) {
        AppStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            statusFilter = AppStatus.valueOf(status.toUpperCase());
        }
        String csv = exportService.applicationsCsv(userContextService.currentUser(), branchId(), statusFilter, q);
        auditService.recordRead(userContextService.currentUser(), "APPLICATIONS_CSV_EXPORT",
                "Application", countLines(csv) - 1, "OFFICER_BRANCH_SCOPE");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"applications.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /** Renders a branch notice so the officer can preview the customer wording before it is sent. */
    @GetMapping("/notices/preview")
    public Map<String, Object> noticePreview(@RequestParam String wording) {
        officer();
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();
        context.put("reference", "ONB-2026-000000");
        context.put("applicantName", "Applicant");
        context.put("bankName", "Meridian Bank");
        java.io.StringWriter writer = new java.io.StringWriter();
        org.apache.velocity.app.Velocity.evaluate(context, writer, "officerNotice", wording);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("preview", writer.toString());
        return body;
    }

    /** Inspects a stored document referenced by a support message before it is attached to a case. */
    @PostMapping("/documents/inspect")
    public Map<String, Object> inspectStoredDocument(@RequestBody Map<String, String> message) throws IOException {
        officer();
        String reference = message.getOrDefault("reference", "");
        Path target = Paths.get(storageDir).resolve(reference);
        java.nio.file.attribute.BasicFileAttributes attributes = Files.readAttributes(target, java.nio.file.attribute.BasicFileAttributes.class);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reference", reference);
        body.put("sizeBytes", attributes.size());
        body.put("modifiedAt", attributes.lastModifiedTime().toInstant().toString());
        body.put("directory", attributes.isDirectory());
        return body;
    }

    /** Enrols the branch terminal's desk code as the officer's sign-in credential. */
    @PostMapping("/desk/credential")
    public Map<String, Object> enrolDeskCredential(@RequestHeader(name = "X-Desk-Code") String deskCode) {
        StaffUser staff = officer();
        UserAccount account = userAccountRepository.findByStaffId(staff.getId())
                .orElseThrow(() -> ApiException.notFound("Staff account"));
        account.setPasswordHash(deskCredentialHash(deskCode));
        userAccountRepository.save(account);
        return ok("Desk credential enrolled.");
    }

    private static String deskCredentialHash(String deskCode) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("MD5").digest(deskCode.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not digest the desk credential", e);
        }
    }

    private long countLines(String csv) {
        return csv.isEmpty() ? 0 : csv.split("\n", -1).length;
    }

    private Instant parseDate(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            return endOfDay
                    ? date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().minusNanos(1)
                    : date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid date filter (use yyyy-MM-dd)");
        }
    }

    private Map<String, Object> ok(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", message);
        return body;
    }
}
