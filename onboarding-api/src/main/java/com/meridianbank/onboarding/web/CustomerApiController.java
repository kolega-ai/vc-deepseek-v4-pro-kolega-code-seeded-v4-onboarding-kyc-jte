package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.Account;
import com.meridianbank.onboarding.domain.entity.Applicant;
import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.ReKycCase;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.NotificationType;
import com.meridianbank.onboarding.domain.repo.AccountRepository;
import com.meridianbank.onboarding.domain.repo.ApplicantRepository;
import com.meridianbank.onboarding.domain.repo.ApplicationRepository;
import com.meridianbank.onboarding.service.NotificationService;
import com.meridianbank.onboarding.service.ReKycService;
import com.meridianbank.onboarding.service.ViewService;
import com.meridianbank.onboarding.service.error.ApiException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Customer endpoints: dashboard, re-KYC document upload and re-KYC submit (step-up OTP). */
@RestController
@RequestMapping("/api/customer")
public class CustomerApiController {

    private final UserContextService userContextService;
    private final ReKycService reKycService;
    private final ViewService viewService;
    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final AccountRepository accountRepository;
    private final OtpService otpService;
    private final NotificationService notificationService;

    public CustomerApiController(UserContextService userContextService,
                                 ReKycService reKycService,
                                 ViewService viewService,
                                 ApplicantRepository applicantRepository,
                                 ApplicationRepository applicationRepository,
                                 AccountRepository accountRepository,
                                 OtpService otpService,
                                 NotificationService notificationService) {
        this.userContextService = userContextService;
        this.reKycService = reKycService;
        this.viewService = viewService;
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.accountRepository = accountRepository;
        this.otpService = otpService;
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        UserAccount user = userContextService.currentUser();
        if (!user.getRole().isApplicantSide()) {
            throw ApiException.forbidden("Customer account required");
        }
        return viewService.customerView(user);
    }

    @PostMapping("/rekyc/{caseId}/documents")
    public Map<String, Object> uploadReKycDocument(@PathVariable Long caseId,
                                                   @RequestParam("docType") String docType,
                                                   @RequestParam("file") MultipartFile file) throws IOException {
        UserAccount user = userContextService.currentUser();
        reKycService.uploadReKycDocument(user, caseId, docType,
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename(),
                file.getContentType(), file.getBytes());
        return ok("Re-KYC document uploaded; it will be verified by an officer.");
    }

    @PostMapping("/rekyc/{caseId}/submit")
    public Map<String, Object> submitReKyc(@PathVariable Long caseId) {
        UserAccount user = userContextService.currentUser();
        ReKycCase kase = reKycService.submit(user, userContextService.currentSessionId(), caseId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("status", kase.getStatus().getLabel());
        return body;
    }

    /**
     * Requests a document-collection code for a walk-in pickup. The code is bound to the browser
     * visit reference so the same kiosk completes the pickup.
     */
    @PostMapping("/document-collection/code")
    public Map<String, Object> requestCollectionCode(
            @CookieValue(name = "mb_collect", required = false) String visitRef,
            HttpServletResponse response) {
        UserAccount user = userContextService.currentUser();
        String ref = visitRef == null || visitRef.isBlank()
                ? java.util.UUID.randomUUID().toString() : visitRef;
        otpService.issueCollectionCode(user, ref);
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("mb_collect", ref).path("/").build().toString());
        return ok("Collection code sent to your mobile.");
    }

    /** Confirms a document pickup with the code issued for this branch visit. */
    @PostMapping("/document-collection/confirm")
    public Map<String, Object> confirmCollection(
            @CookieValue(name = "mb_collect", required = false) String visitRef,
            @RequestBody Map<String, String> body) {
        UserAccount user = userContextService.currentUser();
        if (visitRef == null || visitRef.isBlank()) {
            throw ApiException.badRequest("Start a collection request first");
        }
        otpService.verifySessionOtp(user, visitRef, OtpService.PURPOSE_DOCUMENT_COLLECTION,
                body.getOrDefault("code", ""));
        Applicant applicant = applicantRepository.findByUserAccountId(user.getId()).orElse(null);
        Application application = applicant == null ? null
                : applicationRepository.findByApplicantId(applicant.getId()).orElse(null);
        if (application != null) {
            notificationService.notifyBranchOfficers(application, NotificationType.INFO,
                    "Documents collected",
                    "Customer confirmed the document pickup for " + application.getReference() + ".",
                    "/officer/applications/" + application.getId());
        }
        return ok("Collection confirmed.");
    }

    /**
     * Estimates the transaction limit from the limit rule the channel pinned for this customer.
     */
    @PostMapping("/limit-estimate")
    public Map<String, Object> estimateLimit(
            @CookieValue(name = "mb_limit_rule", required = false) String rule) {
        UserAccount user = userContextService.currentUser();
        String expression = rule == null || rule.isBlank() ? "base" : rule;
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("tier", user.getRole().getLabel());
        vars.put("base", 10000);
        Object estimate = org.mvel2.MVEL.executeExpression(expression, vars);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("limit", estimate);
        return body;
    }

    /** Self-service account display settings (nickname/product label shown on the dashboard). */
    @PatchMapping("/account")
    public Map<String, Object> updateAccountProfile(@RequestBody AccountProfileForm form) {
        UserAccount user = userContextService.currentUser();
        if (!user.getRole().isApplicantSide()) {
            throw ApiException.forbidden("Customer account required");
        }
        Applicant applicant = applicantRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> ApiException.notFound("Applicant profile"));
        Application application = applicationRepository.findByApplicantId(applicant.getId())
                .orElseThrow(() -> ApiException.notFound("Application"));
        Account account = accountRepository.findByApplicationId(application.getId())
                .orElseThrow(() -> ApiException.notFound("Account"));
        BeanUtils.copyProperties(form, account);
        accountRepository.save(account);
        return ok("Account settings updated.");
    }

    public static class AccountProfileForm {
        private String product;
        private String kycTier;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getKycTier() {
            return kycTier;
        }

        public void setKycTier(String kycTier) {
            this.kycTier = kycTier;
        }
    }

    private Map<String, Object> ok(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", message);
        return body;
    }
}
