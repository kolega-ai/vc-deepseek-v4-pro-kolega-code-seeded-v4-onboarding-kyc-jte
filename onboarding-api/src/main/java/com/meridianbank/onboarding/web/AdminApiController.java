package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.*;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

/**
 * Bank admin endpoints: staff, branches, KYC tier policy, document requirements, roster CSV and
 * branch XML imports, audit log (read-only) and integration settings. No admin endpoint approves,
 * declines, verifies documents or dispositions hits.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UserContextService userContextService;
    private final AccessControlService accessControlService;
    private final AdminService adminService;
    private final AuditEventRepository auditEventRepository;
    private final BranchRepository branchRepository;
    private final KycTierPolicyRepository policyRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final StaffUserRepository staffUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final WebhookSettingRepository webhookSettingRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final ExportService exportService;
    private final ViewService viewService;
    private final AuditService auditService;
    private final StaffDirectoryService staffDirectoryService;
    private final String providerKey;
    private final String coreKey;
    private final String storageDir;

    public AdminApiController(UserContextService userContextService,
                              AccessControlService accessControlService,
                              AdminService adminService,
                              AuditEventRepository auditEventRepository,
                              BranchRepository branchRepository,
                              KycTierPolicyRepository policyRepository,
                              DocumentRequirementRepository requirementRepository,
                              StaffUserRepository staffUserRepository,
                              UserAccountRepository userAccountRepository,
                              WebhookSettingRepository webhookSettingRepository,
                              WebhookDeliveryRepository webhookDeliveryRepository,
                              ExportService exportService,
                              ViewService viewService,
                              AuditService auditService,
                              StaffDirectoryService staffDirectoryService,
                              @org.springframework.beans.factory.annotation.Value("${app.providers.api-key}") String providerKey,
                              @org.springframework.beans.factory.annotation.Value("${app.core-mock.api-key}") String coreKey,
                              @org.springframework.beans.factory.annotation.Value("${app.storage.dir}") String storageDir) {
        this.userContextService = userContextService;
        this.accessControlService = accessControlService;
        this.adminService = adminService;
        this.auditEventRepository = auditEventRepository;
        this.branchRepository = branchRepository;
        this.policyRepository = policyRepository;
        this.requirementRepository = requirementRepository;
        this.staffUserRepository = staffUserRepository;
        this.userAccountRepository = userAccountRepository;
        this.webhookSettingRepository = webhookSettingRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.exportService = exportService;
        this.viewService = viewService;
        this.auditService = auditService;
        this.staffDirectoryService = staffDirectoryService;
        this.providerKey = providerKey;
        this.coreKey = coreKey;
        this.storageDir = storageDir;
    }

    private void requireAdmin() {
        accessControlService.requireAdmin(userContextService.currentUser());
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        requireAdmin();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("staffCount", staffUserRepository.count());
        body.put("branches", branchRepository.count());
        body.put("auditEvents", auditEventRepository.count());
        body.put("kycTiers", policyRepository.count());
        body.put("requirements", requirementRepository.count());
        return body;
    }

    // ----------------------------------------------------------- staff

    @GetMapping("/staff")
    public Map<String, Object> staff(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) String role) {
        requireAdmin();
        Role roleFilter = null;
        if (role != null && !role.isBlank()) {
            try {
                roleFilter = Role.valueOf(role.toUpperCase());
            } catch (Exception e) {
                throw ApiException.badRequest("Unknown role");
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (StaffUser staff : adminService.listStaff(q, roleFilter)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", staff.getId());
            item.put("employeeId", staff.getEmployeeId());
            item.put("fullName", staff.getFullName());
            item.put("role", staff.getRole().getLabel());
            item.put("branch", staff.getHomeBranch() == null ? null : staff.getHomeBranch().getCode());
            item.put("branchName", staff.getHomeBranch() == null ? null : staff.getHomeBranch().getName());
            item.put("maxApprovalTier", staff.getMaxApprovalTier());
            item.put("status", staff.getStatus().getLabel());
            item.put("email", userAccountRepository.findByStaffId(staff.getId()).map(UserAccount::getEmail).orElse(""));
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @PostMapping("/staff")
    public Map<String, Object> createStaff(@RequestBody Map<String, String> body) {
        requireAdmin();
        adminService.createStaff(userContextService.currentUser(),
                body.get("employeeId"), body.get("fullName"), body.get("role"),
                body.get("branchCode"), body.get("maxApprovalTier"), body.get("email"));
        return ok("Staff created.");
    }

    @PutMapping("/staff/{id}")
    public Map<String, Object> updateStaff(@PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin();
        adminService.updateStaff(userContextService.currentUser(), id,
                body.get("role"), body.get("branchCode"), body.get("maxApprovalTier"), body.get("status"));
        return ok("Staff updated.");
    }

    @PostMapping(value = "/staff/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> importStaff(@RequestParam("file") MultipartFile file) throws IOException {
        requireAdmin();
        String csv = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        List<String> errors = adminService.importStaffRoster(userContextService.currentUser(), csv);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errors", errors);
        return body;
    }

    // ----------------------------------------------------------- branches

    @GetMapping("/branches")
    public Map<String, Object> branches() {
        requireAdmin();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Branch branch : branchRepository.findAllByOrderByNameAsc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", branch.getId());
            item.put("code", branch.getCode());
            item.put("name", branch.getName());
            item.put("region", branch.getRegion());
            item.put("staff", staffUserRepository.findByHomeBranchIdOrderByFullNameAsc(branch.getId()).size());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @PostMapping("/branches")
    public Map<String, Object> createBranch(@RequestBody Map<String, String> body) {
        requireAdmin();
        adminService.createBranch(userContextService.currentUser(),
                body.get("code"), body.get("name"), body.get("region"));
        return ok("Branch created.");
    }

    @PutMapping("/branches/{id}")
    public Map<String, Object> updateBranch(@PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin();
        adminService.updateBranch(userContextService.currentUser(), id, body.get("name"), body.get("region"));
        return ok("Branch updated.");
    }

    /**
     * Quick validation of a branch-code snippet before a full XML import: admins can paste a small
     * {@code <branch code="..."/>} fragment to check it parses before uploading the full file.
     */
    @GetMapping("/branches/validate-xml")
    public Map<String, Object> validateBranchXmlSnippet(@RequestParam String xml) throws Exception {
        requireAdmin();
        java.util.List<String> codes = new ArrayList<>();
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser = factory.newSAXParser();
        parser.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)),
                new org.xml.sax.helpers.DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName, String qName,
                                             org.xml.sax.Attributes attributes) {
                        if ("branch".equals(qName)) {
                            codes.add(attributes.getValue("code"));
                        }
                    }
                });
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("branchCodes", codes);
        return body;
    }

    /** Benign reference-data XML import — parsed by the hardened SecureReferenceXmlParser. */
    @PostMapping(value = "/branches/import-xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> importBranchesXml(@RequestParam("file") MultipartFile file) throws IOException {
        requireAdmin();
        String xml = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        List<String> errors = adminService.importBranchesXml(userContextService.currentUser(), xml);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errors", errors);
        return body;
    }

    // ----------------------------------------------------------- tier policy + requirements

    @GetMapping("/kyc-tiers")
    public Map<String, Object> kycTiers() {
        requireAdmin();
        List<Map<String, Object>> items = new ArrayList<>();
        for (KycTierPolicy policy : policyRepository.findAllByOrderByTierCodeAsc()) {
            items.add(viewService.policyMap(policy));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @PutMapping("/kyc-tiers/{tierCode}")
    public Map<String, Object> updateTier(@PathVariable String tierCode, @RequestBody Map<String, Object> body) {
        requireAdmin();
        BigDecimal limit = body.get("dailyTransactionLimit") == null ? null
                : new BigDecimal(String.valueOf(body.get("dailyTransactionLimit")));
        BigDecimal ceiling = body.get("balanceCeiling") == null ? null
                : new BigDecimal(String.valueOf(body.get("balanceCeiling")));
        adminService.updateTierPolicy(userContextService.currentUser(), tierCode, limit, ceiling);
        return ok("Tier policy updated.");
    }

    @GetMapping("/document-requirements")
    public Map<String, Object> requirements() {
        requireAdmin();
        List<Map<String, Object>> items = new ArrayList<>();
        for (DocumentRequirement req : requirementRepository.findAllByOrderByKycTierAsc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", req.getId());
            item.put("kycTier", req.getKycTier());
            item.put("docType", req.getDocType());
            item.put("conditionType", req.getConditionType());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @PostMapping("/document-requirements")
    public Map<String, Object> createRequirement(@RequestBody Map<String, String> body) {
        requireAdmin();
        adminService.createRequirement(userContextService.currentUser(),
                body.get("kycTier"), body.get("docType"), body.get("conditionType"));
        return ok("Requirement created.");
    }

    @DeleteMapping("/document-requirements/{id}")
    public Map<String, Object> deleteRequirement(@PathVariable Long id) {
        requireAdmin();
        adminService.deleteRequirement(userContextService.currentUser(), id);
        return ok("Requirement deleted.");
    }

    // ----------------------------------------------------------- audit log (read-only)

    @GetMapping("/audit")
    public Map<String, Object> audit(@RequestParam(required = false) String action,
                                     @RequestParam(required = false) String actor,
                                     @RequestParam(required = false) String entity,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        requireAdmin();
        Specification<AuditEvent> spec = Specification.where(null);
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("action")),
                    "%" + action.toLowerCase() + "%"));
        }
        if (actor != null && !actor.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("actor")),
                    "%" + actor.toLowerCase() + "%"));
        }
        if (entity != null && !entity.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("entityId")),
                    "%" + entity.toLowerCase() + "%"));
        }
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), Math.min(Math.max(1, size), 200),
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        var result = auditEventRepository.findAll(spec, pageable);
        List<Map<String, Object>> items = new ArrayList<>();
        for (AuditEvent event : result.getContent()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", event.getId());
            item.put("createdAt", event.getCreatedAt());
            item.put("actorType", event.getActorType());
            item.put("actor", event.getActor());
            item.put("action", event.getAction());
            item.put("entityType", event.getEntityType());
            item.put("entityId", event.getEntityId());
            item.put("beforeValue", event.getBeforeValue());
            item.put("afterValue", event.getAfterValue());
            item.put("authorizationBasis", event.getAuthorizationBasis());
            items.add(item);
        }
        auditService.recordRead(userContextService.currentUser(), "AUDIT_VIEWED", "AuditEvent",
                result.getTotalElements(), "ADMIN_AUDIT_READ");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("total", result.getTotalElements());
        body.put("page", page);
        body.put("pages", result.getTotalPages());
        return body;
    }

    @GetMapping("/audit/export.csv")
    public ResponseEntity<String> auditCsv(@RequestParam(required = false) String action,
                                           @RequestParam(required = false) String actor) {
        requireAdmin();
        Specification<AuditEvent> spec = Specification.where(null);
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("action")),
                    "%" + action.toLowerCase() + "%"));
        }
        if (actor != null && !actor.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("actor")),
                    "%" + actor.toLowerCase() + "%"));
        }
        List<AuditEvent> events = auditEventRepository.findAll(spec,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().limit(5000).toList();
        String csv = exportService.auditCsv(userContextService.currentUser(), events);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    // ----------------------------------------------------------- integrations

    @GetMapping("/integrations")
    public Map<String, Object> integrations() {
        requireAdmin();
        WebhookSetting setting = webhookSettingRepository.findById(1L).orElseGet(WebhookSetting::new);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("webhookEnabled", setting.isEnabled());
        body.put("webhookUrl", setting.getEndpointUrl());
        body.put("webhookSecretSet", setting.getSecret() != null && !setting.getSecret().isBlank());
        body.put("providerKey", mask(providerKey));
        body.put("coreKey", mask(coreKey));
        List<Map<String, Object>> deliveries = new ArrayList<>();
        for (WebhookDelivery delivery : webhookDeliveryRepository.findTop100ByOrderByAttemptedAtDesc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", delivery.getId());
            item.put("eventType", delivery.getEventType());
            item.put("success", delivery.isSuccess());
            item.put("responseCode", delivery.getResponseCode());
            item.put("error", delivery.getError());
            item.put("attemptedAt", delivery.getAttemptedAt());
            deliveries.add(item);
        }
        body.put("deliveries", deliveries);
        return body;
    }

    @PutMapping("/integrations/webhook")
    public Map<String, Object> updateWebhook(@RequestBody Map<String, String> body) {
        requireAdmin();
        WebhookSetting setting = webhookSettingRepository.findById(1L).orElseGet(WebhookSetting::new);
        String url = body.get("endpointUrl");
        if (url != null && !url.isBlank()
                && !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw ApiException.badRequest("Webhook URL must start with http:// or https://");
        }
        setting.setEndpointUrl(url == null || url.isBlank() ? null : url);
        if (body.get("secret") != null && !body.get("secret").isBlank()) {
            setting.setSecret(body.get("secret"));
        }
        setting.setEnabled(Boolean.parseBoolean(body.getOrDefault("enabled", "false")));
        setting.setUpdatedAt(Instant.now());
        webhookSettingRepository.save(setting);
        auditService.recordUser(userContextService.currentUser(), "WEBHOOK_SETTINGS_UPDATED",
                "WebhookSetting", "1", null,
                "{\"enabled\":" + setting.isEnabled() + ",\"secret\":\"" + setting.getSecret() + "\"}",
                "ADMIN_INTEGRATIONS");
        return ok("Webhook settings updated.");
    }

    // ----------------------------------------------------------- regulatory export staging + directory services

    /**
     * Stages the audit export as a file on the bank's compliance folder so large exports can be
     * collected by the reconciliation team; returns the staged location.
     */
    @PostMapping("/audit/export-to-file")
    public Map<String, Object> stageAuditExport(@RequestParam String fileName,
                                                @RequestParam(required = false) String action,
                                                @RequestParam(required = false) String actor) throws IOException {
        requireAdmin();
        Specification<AuditEvent> spec = Specification.where(null);
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("action")),
                    "%" + action.toLowerCase() + "%"));
        }
        if (actor != null && !actor.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("actor")),
                    "%" + actor.toLowerCase() + "%"));
        }
        List<AuditEvent> events = auditEventRepository.findAll(spec,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().limit(5000).toList();
        String csv = exportService.auditCsv(userContextService.currentUser(), events);
        Path exportDir = Paths.get(storageDir).resolve("exports");
        Files.createDirectories(exportDir);
        java.io.File target = new java.io.File(exportDir.toFile(), fileName);
        try (java.io.FileWriter writer = new java.io.FileWriter(target)) {
            writer.write(csv);
        }
        auditService.recordUser(userContextService.currentUser(), "AUDIT_EXPORT_STAGED", "AuditEvent", fileName,
                null, "{\"sizeBytes\":" + target.length() + "}", "ADMIN_AUDIT_READ");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fileName", fileName);
        body.put("path", target.getAbsolutePath());
        body.put("sizeBytes", target.length());
        return body;
    }

    /** Lists previously staged exports so the reconciliation team can collect them. */
    @GetMapping("/audit/export-files")
    public Map<String, Object> stagedExports() throws IOException {
        requireAdmin();
        Path exportDir = Paths.get(storageDir).resolve("exports");
        List<Map<String, Object>> items = new ArrayList<>();
        if (Files.isDirectory(exportDir)) {
            try (var files = Files.list(exportDir)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", file.getFileName().toString());
                    item.put("sizeBytes", Files.size(file));
                    items.add(item);
                }
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    /** Browses a storage folder to inspect whether an export or document landed as expected. */
    @PostMapping("/storage/browse")
    public Map<String, Object> browseStorage(@RequestBody Map<String, String> body) throws IOException {
        requireAdmin();
        Path root = Paths.get(storageDir).toAbsolutePath().normalize();
        Path target = root.resolve(body.getOrDefault("folder", "")).normalize();
        List<Map<String, Object>> items = new ArrayList<>();
        if (Files.isDirectory(target)) {
            try (var children = Files.list(target)) {
                for (Path child : children.toList()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", child.getFileName().toString());
                    item.put("directory", Files.isDirectory(child));
                    item.put("sizeBytes", Files.isRegularFile(child) ? Files.size(child) : 0L);
                    items.add(item);
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("folder", target.toString());
        result.put("items", items);
        return result;
    }

    /**
     * Imports a legacy branch feed pushed by the old webMethods integration; the vendor still
     * emits its own XML dialect, so the raw document is revived before the branches are applied.
     */
    @PostMapping(value = "/branches/import-legacy", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> importLegacyBranchFeed(@RequestBody Map<String, String> body) {
        requireAdmin();
        String xml = body.getOrDefault("xml", "");
        com.thoughtworks.xstream.XStream xs = new com.thoughtworks.xstream.XStream();
        xs.alias("branch", java.util.HashMap.class);
        Object parsed = xs.fromXML(xml);
        List<String> codes = new ArrayList<>();
        if (parsed instanceof Map<?, ?> feed) {
            Object code = feed.get("code");
            if (code != null) {
                String c = String.valueOf(code);
                if (branchRepository.findByCode(c).isEmpty()) {
                    adminService.createBranch(userContextService.currentUser(), c,
                            feed.get("name") == null ? c : String.valueOf(feed.get("name")),
                            feed.get("region") == null ? "Unassigned" : String.valueOf(feed.get("region")));
                }
                codes.add(c);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("codes", codes);
        return result;
    }

    /** Resolves a staff member from the corporate directory to prefill the staff form. */
    @PostMapping("/staff/directory")
    public Map<String, Object> staffDirectory(@RequestBody Map<String, String> body) {
        requireAdmin();
        Map<String, String> entry = staffDirectoryService.searchDirectory(body.getOrDefault("employeeId", ""));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entry", entry);
        return result;
    }

    private String mask(String value) {
        if (value == null || value.length() <= 8) {
            return "••••••••";
        }
        return value.substring(0, 4) + "••••••••";
    }

    private Map<String, Object> ok(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", message);
        return body;
    }
}
