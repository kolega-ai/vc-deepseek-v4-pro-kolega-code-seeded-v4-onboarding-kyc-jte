package com.meridianbank.onboarding.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.IdentityVerificationResult;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.service.*;
import com.meridianbank.onboarding.service.documents.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Applicant-facing endpoints (prospect/customer). Every route resolves the caller's own
 * application from the authenticated token; cross-application access by changing identifiers is
 * impossible because ownership is checked server-side against the token's subject.
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicantApiController {

    private final UserContextService userContextService;
    private final JourneyService journeyService;
    private final DocumentService documentService;
    private final IdentityVerificationService identityVerificationService;
    private final ViewService viewService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public ApplicantApiController(UserContextService userContextService,
                                  JourneyService journeyService,
                                  DocumentService documentService,
                                  IdentityVerificationService identityVerificationService,
                                  ViewService viewService,
                                  AuditService auditService,
                                  ObjectMapper objectMapper) {
        this.userContextService = userContextService;
        this.journeyService = journeyService;
        this.documentService = documentService;
        this.identityVerificationService = identityVerificationService;
        this.viewService = viewService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/me")
    public Map<String, Object> ownApplication() {
        return viewService.applicantOwnView(userContextService.currentUser());
    }

    @PostMapping("/me/mobile-otp/issue")
    public Map<String, Object> issueMobileOtp() {
        journeyService.issueMobileOtp(userContextService.currentUser(), userContextService.currentSessionId());
        return ok("Code sent to your mobile (mock — check notifications).");
    }

    @PostMapping("/me/mobile-otp/verify")
    public Map<String, Object> verifyMobileOtp(@RequestBody Map<String, String> body) {
        journeyService.verifyMobileOtp(userContextService.currentUser(), userContextService.currentSessionId(),
                body.getOrDefault("code", ""));
        return ok("Mobile verified.");
    }

    @PutMapping("/me/personal")
    public Map<String, Object> submitPersonal(@RequestBody JourneyService.PersonalDetailsForm form) {
        journeyService.submitPersonal(userContextService.currentUser(), form);
        return ok("Personal details saved.");
    }

    @PutMapping("/me/address")
    public Map<String, Object> submitAddress(@RequestBody JourneyService.AddressDetailsForm form) {
        journeyService.submitAddress(userContextService.currentUser(), form);
        return ok("Address saved.");
    }

    @PutMapping("/me/employment")
    public Map<String, Object> submitEmployment(@RequestBody JourneyService.EmploymentIncomeForm form) {
        journeyService.submitEmployment(userContextService.currentUser(), form);
        return ok("Employment details saved.");
    }

    @PostMapping("/me/documents")
    public Map<String, Object> uploadDocument(@RequestParam("docType") String docType,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        UserAccount user = userContextService.currentUser();
        Application application = journeyService.applicationFor(user);
        documentService.upload(user, application.getId(), docType,
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename(),
                file.getContentType(), file.getBytes());
        return ok("Document uploaded.");
    }

    @DeleteMapping("/me/documents/{documentId}")
    public Map<String, Object> deleteDocument(@PathVariable Long documentId) {
        documentService.delete(userContextService.currentUser(), documentId);
        return ok("Document deleted.");
    }

    @PostMapping("/me/identity/verify")
    public Map<String, Object> verifyIdentity() {
        IdentityVerificationResult result = identityVerificationService.run(userContextService.currentUser());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("matchScore", result.getMatchScore());
        body.put("outcome", result.getOutcome());
        body.put("providerReference", result.getProviderReference());
        return body;
    }

    @PostMapping("/me/submit")
    public Map<String, Object> submit() {
        journeyService.submit(userContextService.currentUser());
        return ok("Application submitted; screening runs automatically.");
    }

    @PostMapping("/me/resubmit")
    public Map<String, Object> resubmit() {
        journeyService.resubmit(userContextService.currentUser());
        return ok("Application returned to review.");
    }

    /** Own-data JSON export: applicant-visible fields only (no bureau, hits or internal notes). */
    @GetMapping(value = "/me/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exportOwnData() {
        UserAccount user = userContextService.currentUser();
        Map<String, Object> view = viewService.applicantOwnView(user);
        auditService.recordRead(user, "APPLICATION_JSON_EXPORT", "Application", 1, "APPLICANT_OWNER");
        return view;
    }

    private Map<String, Object> ok(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", message);
        return body;
    }
}
