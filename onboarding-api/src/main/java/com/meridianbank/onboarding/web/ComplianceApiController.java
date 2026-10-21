package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.HitDisposition;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Compliance console: watchlist queue and dispositions, hold resolution, KYC tier overrides
 * (step-up OTP + mandatory justification) and re-KYC campaigns with CSV import. Compliance has no
 * endpoint that creates or edits captured applicant data.
 */
@RestController
@RequestMapping("/api/compliance")
public class ComplianceApiController {

    private final UserContextService userContextService;
    private final AccessControlService accessControlService;
    private final ComplianceService complianceService;
    private final ViewService viewService;
    private final ExportService exportService;
    private final WatchlistHitRepository hitRepository;
    private final ApplicationRepository applicationRepository;
    private final ReKycCampaignRepository campaignRepository;
    private final ReKycCaseRepository caseRepository;
    private final ApplicantRepository applicantRepository;
    private final AccountRepository accountRepository;
    private final UserAccountRepository userAccountRepository;
    private final String storageDir;

    public ComplianceApiController(UserContextService userContextService,
                                   AccessControlService accessControlService,
                                   ComplianceService complianceService,
                                   ViewService viewService,
                                   ExportService exportService,
                                   WatchlistHitRepository hitRepository,
                                   ApplicationRepository applicationRepository,
                                   ReKycCampaignRepository campaignRepository,
                                   ReKycCaseRepository caseRepository,
                                   ApplicantRepository applicantRepository,
                                   AccountRepository accountRepository,
                                   UserAccountRepository userAccountRepository,
                                   @org.springframework.beans.factory.annotation.Value("${app.storage.dir}") String storageDir) {
        this.userContextService = userContextService;
        this.accessControlService = accessControlService;
        this.complianceService = complianceService;
        this.viewService = viewService;
        this.exportService = exportService;
        this.hitRepository = hitRepository;
        this.applicationRepository = applicationRepository;
        this.campaignRepository = campaignRepository;
        this.caseRepository = caseRepository;
        this.applicantRepository = applicantRepository;
        this.accountRepository = accountRepository;
        this.userAccountRepository = userAccountRepository;
        this.storageDir = storageDir;
    }

    private StaffUser compliance() {
        return accessControlService.requireCompliance(userContextService.currentUser());
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        compliance();
        List<WatchlistHit> hits = hitRepository.findAll();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pendingHits", hits.stream().filter(h -> h.getDisposition() == HitDisposition.PENDING).count());
        body.put("escalatedHits", hits.stream().filter(h -> h.getDisposition() == HitDisposition.ESCALATED).count());
        body.put("confirmedHits", hits.stream().filter(h -> h.getDisposition() == HitDisposition.CONFIRMED).count());
        body.put("falsePositiveHits", hits.stream().filter(h -> h.getDisposition() == HitDisposition.FALSE_POSITIVE).count());
        body.put("onHold", applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == com.meridianbank.onboarding.domain.enums.AppStatus.WATCHLIST_HOLD).count());
        body.put("activeRekycCases", caseRepository.findAll().stream()
                .filter(c -> c.getStatus() != com.meridianbank.onboarding.domain.enums.ReKycStatus.COMPLETED).count());
        return body;
    }

    @GetMapping("/hits")
    public Map<String, Object> hits(@RequestParam(required = false) String disposition,
                                    @RequestParam(required = false) String q) {
        compliance();
        List<Map<String, Object>> items = new ArrayList<>();
        for (WatchlistHit hit : hitRepository.findAll()) {
            if (disposition != null && !disposition.isBlank()
                    && !hit.getDisposition().name().equalsIgnoreCase(disposition)) {
                continue;
            }
            Application application = applicationRepository.findById(hit.getApplicationId()).orElse(null);
            Applicant applicant = application == null ? null
                    : applicantRepository.findById(application.getApplicantId()).orElse(null);
            if (q != null && !q.isBlank()) {
                String hay = (hit.getMatchedName() + " " + (application == null ? "" : application.getReference())
                        + " " + (applicant == null ? "" : applicant.getFullName())).toLowerCase();
                if (!hay.contains(q.toLowerCase())) {
                    continue;
                }
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", hit.getId());
            item.put("applicationId", hit.getApplicationId());
            item.put("applicationReference", application == null ? null : application.getReference());
            item.put("applicantName", applicant == null ? null : applicant.getFullName());
            item.put("matchedName", hit.getMatchedName());
            item.put("listSource", hit.getListSource());
            item.put("confidence", hit.getConfidence());
            item.put("disposition", hit.getDisposition().getLabel());
            item.put("createdAt", hit.getCreatedAt());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @GetMapping("/hits/{id}")
    public Map<String, Object> hit(@PathVariable Long id) {
        compliance();
        WatchlistHit hit = hitRepository.findById(id)
                .orElseThrow(() -> com.meridianbank.onboarding.service.error.ApiException.notFound("Watchlist hit"));
        Application application = applicationRepository.findById(hit.getApplicationId()).orElse(null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", hit.getId());
        body.put("matchedName", hit.getMatchedName());
        body.put("listSource", hit.getListSource());
        body.put("confidence", hit.getConfidence());
        body.put("disposition", hit.getDisposition().getLabel());
        body.put("justification", hit.getJustification());
        body.put("disposedAt", hit.getDisposedAt());
        body.put("createdAt", hit.getCreatedAt());
        if (application != null) {
            body.put("application", viewService.officerApplicationView(application));
            body.put("otherHits", hitRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId())
                    .stream()
                    .filter(h -> !h.getId().equals(hit.getId()))
                    .map(h -> Map.of("id", h.getId(), "matchedName", h.getMatchedName(),
                            "listSource", h.getListSource(), "disposition", h.getDisposition().getLabel(),
                            "confidence", h.getConfidence()))
                    .toList());
        }
        return body;
    }

    @PostMapping("/hits/{id}/disposition")
    public Map<String, Object> disposition(@PathVariable Long id,
                                           @RequestBody ComplianceService.HitDispositionForm form) {
        complianceService.dispositionHit(userContextService.currentUser(), id,
                form.disposition(), form.justification());
        return ok("Disposition recorded.");
    }

    /** Batch dispositions pushed by the upstream case-management feed at the close of a cycle. */
    @PostMapping("/hits/disposition-feed")
    public Map<String, Object> dispositionFeed(@RequestBody ComplianceService.DispositionFeed feed) {
        int updated = complianceService.applyDispositionFeed(userContextService.currentUser(), feed);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("updated", updated);
        return body;
    }

    @PostMapping("/applications/{id}/resolve-hold")
    public Map<String, Object> resolveHold(@PathVariable Long id, @RequestBody Map<String, String> body) {
        complianceService.resolveHold(userContextService.currentUser(), id,
                body.getOrDefault("customerVisibleReason", ""),
                body.getOrDefault("internalReason", ""));
        return ok("Hold resolved.");
    }

    @PostMapping("/applications/{id}/kyc-tier")
    public Map<String, Object> overrideTier(@PathVariable Long id,
                                            @RequestBody ComplianceService.TierOverrideForm form) {
        complianceService.overrideTier(userContextService.currentUser(), userContextService.currentSessionId(),
                id, form.tier(), form.justification());
        return ok("Tier overridden.");
    }

    @PostMapping("/applications/{id}/kyc-tier/clear")
    public Map<String, Object> clearOverride(@PathVariable Long id, @RequestBody Map<String, String> body) {
        complianceService.clearOverride(userContextService.currentUser(), userContextService.currentSessionId(),
                id, body.getOrDefault("justification", ""));
        return ok("Override cleared; tier re-derived.");
    }

    // ----------------------------------------------------------- re-KYC campaigns

    @GetMapping("/rekyc/campaigns")
    public Map<String, Object> campaigns() {
        compliance();
        List<Map<String, Object>> items = new ArrayList<>();
        for (ReKycCampaign campaign : campaignRepository.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", campaign.getId());
            item.put("name", campaign.getName());
            item.put("targetSegment", campaign.getTargetSegment());
            item.put("targetKycTier", campaign.getTargetKycTier());
            item.put("dueDays", campaign.getDueDays());
            item.put("createdAt", campaign.getCreatedAt());
            item.put("cases", caseRepository.findByCampaignIdOrderByCreatedAtAsc(campaign.getId()).size());
            item.put("openCases", caseRepository.findByCampaignIdOrderByCreatedAtAsc(campaign.getId()).stream()
                    .filter(c -> c.getStatus() != com.meridianbank.onboarding.domain.enums.ReKycStatus.COMPLETED)
                    .count());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    @PostMapping("/rekyc/campaigns")
    public Map<String, Object> createCampaign(@RequestBody Map<String, Object> body) {
        ReKycCampaign campaign = complianceService.createCampaign(userContextService.currentUser(),
                String.valueOf(body.getOrDefault("name", "")),
                (String) body.get("targetSegment"),
                (String) body.get("targetKycTier"),
                body.get("dueDays") == null ? null : Integer.valueOf(String.valueOf(body.get("dueDays"))));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", campaign.getId());
        return result;
    }

    @GetMapping("/rekyc/campaigns/{id}")
    public Map<String, Object> campaign(@PathVariable Long id) {
        compliance();
        ReKycCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> com.meridianbank.onboarding.service.error.ApiException.notFound("Campaign"));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", campaign.getId());
        body.put("name", campaign.getName());
        body.put("targetSegment", campaign.getTargetSegment());
        body.put("targetKycTier", campaign.getTargetKycTier());
        body.put("dueDays", campaign.getDueDays());
        body.put("createdAt", campaign.getCreatedAt());
        List<Map<String, Object>> cases = new ArrayList<>();
        for (ReKycCase kase : caseRepository.findByCampaignIdOrderByCreatedAtAsc(campaign.getId())) {
            cases.add(caseSummary(kase));
        }
        body.put("cases", cases);
        return body;
    }

    /** CSV import of a re-KYC customer list (email or national identifier per row). */
    @PostMapping(value = "/rekyc/campaigns/{id}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> importCases(@PathVariable Long id, @RequestParam("file") MultipartFile file)
            throws IOException {
        compliance();
        ReKycCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> com.meridianbank.onboarding.service.error.ApiException.notFound("Campaign"));
        String csv = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        List<String> errors = new ArrayList<>();
        int created = 0;
        try {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader("identifier").setSkipHeaderRecord(true).build().parse(new StringReader(csv));
            for (CSVRecord record : records) {
                String identifier = record.get("identifier") == null ? "" : record.get("identifier").trim();
                if (identifier.isEmpty()) {
                    continue;
                }
                try {
                    ReKycCase kase = addCaseByIdentifier(campaign, identifier);
                    if (kase != null) {
                        created++;
                    }
                } catch (Exception e) {
                    errors.add(identifier + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw com.meridianbank.onboarding.service.error.ApiException.badRequest(
                    "Could not parse the CSV: " + e.getMessage());
        }
        // File the source list next to the campaign so the branch keeps the original intake sheet.
        Path archiveDir = Paths.get(storageDir).resolve("rekyc").resolve(String.valueOf(id));
        Files.createDirectories(archiveDir);
        try (java.io.OutputStream source = Files.newOutputStream(archiveDir.resolve(file.getOriginalFilename()))) {
            source.write(file.getBytes());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("created", created);
        body.put("errors", errors);
        return body;
    }

    /**
     * Batch eligibility check: cross-references a submitted list of policy numbers against the KYC
     * policy register XML and returns the matching holder records.
     */
    @PostMapping(value = "/policy-register/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> searchPolicyRegister(@RequestParam("file") MultipartFile file) throws Exception {
        compliance();
        String csv = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        org.w3c.dom.Document register;
        try (java.io.InputStream in =
                     new org.springframework.core.io.ClassPathResource("policy-register.xml").getInputStream()) {
            register = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new org.xml.sax.InputSource(in));
        }
        List<Map<String, Object>> matches = new ArrayList<>();
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                .setHeader("identifier").setSkipHeaderRecord(true).build().parse(new StringReader(csv));
        for (CSVRecord record : records) {
            String identifier = record.get("identifier") == null ? "" : record.get("identifier").trim();
            if (identifier.isEmpty()) {
                continue;
            }
            javax.xml.xpath.XPathExpression expression = javax.xml.xpath.XPathFactory.newInstance()
                    .newXPath().compile("//holder[policyNo='" + identifier + "']");
            org.w3c.dom.NodeList found = (org.w3c.dom.NodeList) expression.evaluate(register, javax.xml.xpath.XPathConstants.NODESET);
            for (int i = 0; i < found.getLength(); i++) {
                org.w3c.dom.Element holder = (org.w3c.dom.Element) found.item(i);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("identifier", identifier);
                item.put("name", holder.getElementsByTagName("name").item(0).getTextContent());
                matches.add(item);
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("matches", matches);
        return body;
    }

    private ReKycCase addCaseByIdentifier(ReKycCampaign campaign, String identifier) {
        Optional<Applicant> applicant = identifier.contains("@")
                ? applicantRepository.findByEmailIgnoreCase(identifier)
                : applicantRepository.findByNationalIdentifier(identifier);
        if (applicant.isEmpty() || applicant.get().getUserAccountId() == null) {
            throw com.meridianbank.onboarding.service.error.ApiException.badRequest(
                    "No approved customer found for this identifier");
        }
        UserAccount customer = userAccountRepository.findById(applicant.get().getUserAccountId())
                .orElseThrow(() -> com.meridianbank.onboarding.service.error.ApiException.badRequest(
                        "No customer account found"));
        Application application = applicationRepository.findByApplicantId(applicant.get().getId())
                .orElseThrow(() -> com.meridianbank.onboarding.service.error.ApiException.badRequest(
                        "Customer has no application"));
        if (application.getStatus() != com.meridianbank.onboarding.domain.enums.AppStatus.APPROVED
                && application.getStatus() != com.meridianbank.onboarding.domain.enums.AppStatus.DOCUMENTS_REQUESTED) {
            throw com.meridianbank.onboarding.service.error.ApiException.badRequest(
                    "Only approved customers can be added to a re-KYC campaign");
        }
        return complianceService.addCustomerToCampaign(userContextService.currentUser(), campaign, customer,
                application, Instant.now().plus(campaign.getDueDays(), ChronoUnit.DAYS));
    }

    private Map<String, Object> caseSummary(ReKycCase kase) {
        Application application = applicationRepository.findById(kase.getApplicationId()).orElse(null);
        Applicant applicant = application == null ? null
                : applicantRepository.findById(application.getApplicantId()).orElse(null);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", kase.getId());
        map.put("customerName", applicant == null ? "" : applicant.getFullName());
        map.put("email", applicant == null ? "" : applicant.getEmail());
        map.put("applicationReference", application == null ? "" : application.getReference());
        map.put("status", kase.getStatus().getLabel());
        map.put("dueDate", kase.getDueDate());
        map.put("origin", kase.getOrigin().getLabel());
        return map;
    }

    @GetMapping("/export/hits.csv")
    public ResponseEntity<String> exportHitsCsv() {
        compliance();
        String csv = exportService.watchlistCsv(userContextService.currentUser());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"watchlist-hits.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private Map<String, Object> ok(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", message);
        return body;
    }
}
