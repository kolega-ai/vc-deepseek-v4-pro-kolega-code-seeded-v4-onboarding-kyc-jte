package com.meridianbank.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridianbank.web.api.ApiClient;
import com.meridianbank.web.support.WebSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

/**
 * Compliance console: watchlist queue and dispositions, hold resolution, KYC tier overrides
 * (step-up OTP + mandatory justification) and re-KYC campaign management with CSV import.
 */
@Controller
@RequestMapping("/compliance")
public class ComplianceController extends WebSupport {

    private final ApiClient apiClient;

    public ComplianceController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("dashboard", apiClient.get("/api/compliance/dashboard", token));
        model.addAttribute("hits", apiClient.get("/api/compliance/hits?disposition=pending", token));
        model.addAttribute("escalated", apiClient.get("/api/compliance/hits?disposition=escalated", token));
        model.addAttribute("activeNav", "compliance");
        return "compliance/dashboard";
    }

    @GetMapping("/hits")
    public String hits(@RequestParam(required = false) String disposition,
                       @RequestParam(required = false) String q,
                       HttpServletRequest request, Model model) {
        StringBuilder uri = new StringBuilder("/api/compliance/hits?");
        if (disposition != null && !disposition.isBlank()) {
            uri.append("disposition=").append(disposition);
        }
        if (q != null && !q.isBlank()) {
            uri.append(uri.toString().contains("=") && !uri.toString().endsWith("?") ? "&" : "")
                    .append("q=").append(java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        }
        model.addAttribute("result", apiClient.get(uri.toString(), token(request)));
        model.addAttribute("filters", Map.of(
                "disposition", disposition == null ? "" : disposition,
                "q", q == null ? "" : q));
        model.addAttribute("activeNav", "compliance");
        return "compliance/hits";
    }

    @GetMapping("/hits/{id}")
    public String hit(@PathVariable Long id, HttpServletRequest request, Model model) {
        model.addAttribute("view", apiClient.get("/api/compliance/hits/" + id, token(request)));
        model.addAttribute("activeNav", "compliance");
        return "compliance/hit";
    }

    @PostMapping("/hits/{id}/disposition")
    public String disposition(@PathVariable Long id, @RequestParam String disposition,
                              @RequestParam(required = false) String justification,
                              HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/compliance/hits/" + id + "/disposition", token(request),
                    Map.of("disposition", disposition,
                            "justification", justification == null ? "" : justification));
            flashSuccess(attributes, "Disposition recorded.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/compliance/hits/" + id;
    }

    @PostMapping("/applications/{id}/resolve-hold")
    public String resolveHold(@PathVariable Long id, @RequestParam(required = false) String customerVisibleReason,
                              @RequestParam(required = false) String internalReason,
                              HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/compliance/applications/" + id + "/resolve-hold", token(request),
                    Map.of("customerVisibleReason", customerVisibleReason == null ? "" : customerVisibleReason,
                            "internalReason", internalReason == null ? "" : internalReason));
            flashSuccess(attributes, "Hold resolved.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/compliance/hits";
    }

    @PostMapping("/applications/{id}/kyc-tier/otp")
    public String overrideOtp(@PathVariable Long id, HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/auth/otp/step-up/issue", token(request),
                    Map.of("purpose", "kyc_tier_override"));
            flashSuccess(attributes, "Step-up code sent (check the notification bell).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/compliance/hits/" + id;
    }

    @PostMapping("/applications/{id}/kyc-tier")
    public String overrideTier(@PathVariable Long id, @RequestParam String tier,
                               @RequestParam String justification, @RequestParam String otpCode,
                               @RequestParam(required = false, defaultValue = "") String returnTo,
                               HttpServletRequest request, RedirectAttributes attributes) {
        String token = token(request);
        try {
            apiClient.post("/api/auth/otp/step-up/verify", token,
                    Map.of("purpose", "kyc_tier_override", "code", otpCode));
            apiClient.post("/api/compliance/applications/" + id + "/kyc-tier", token,
                    Map.of("tier", tier, "justification", justification));
            flashSuccess(attributes, "KYC tier overridden.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:" + (returnTo.startsWith("/compliance/") ? returnTo : "/compliance/hits");
    }

    // ---------------------------------------------------------------- re-KYC campaigns

    @GetMapping("/rekyc")
    public String campaigns(HttpServletRequest request, Model model) {
        model.addAttribute("campaigns", apiClient.get("/api/compliance/rekyc/campaigns", token(request)));
        model.addAttribute("activeNav", "compliance");
        return "compliance/campaigns";
    }

    @PostMapping("/rekyc/campaigns")
    public String createCampaign(@RequestParam String name, @RequestParam(required = false) String targetSegment,
                                 @RequestParam(required = false) String targetKycTier,
                                 @RequestParam String dueDays,
                                 HttpServletRequest request, RedirectAttributes attributes) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("name", name);
            body.put("targetSegment", targetSegment);
            body.put("targetKycTier", targetKycTier);
            body.put("dueDays", Integer.valueOf(dueDays));
            apiClient.post("/api/compliance/rekyc/campaigns", token(request), body);
            flashSuccess(attributes, "Campaign created. Import a customer list to add cases.");
        } catch (Exception e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/compliance/rekyc";
    }

    @GetMapping("/rekyc/campaigns/{id}")
    public String campaign(@PathVariable Long id, HttpServletRequest request, Model model) {
        model.addAttribute("campaign", apiClient.get("/api/compliance/rekyc/campaigns/" + id, token(request)));
        model.addAttribute("activeNav", "compliance");
        return "compliance/campaign";
    }

    @PostMapping("/rekyc/campaigns/{id}/import")
    public String importCampaign(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                 HttpServletRequest request, RedirectAttributes attributes) throws IOException {
        try {
            JsonNode result = apiClient.postMultipart("/api/compliance/rekyc/campaigns/" + id + "/import",
                    token(request), "file",
                    file.getOriginalFilename() == null ? "customers.csv" : file.getOriginalFilename(),
                    file.getContentType() == null ? "text/csv" : file.getContentType(),
                    file.getBytes(), Map.of());
            int created = result.path("created").asInt();
            int errors = result.path("errors").size();
            flashSuccess(attributes, "Imported " + created + " cases (" + errors + " row errors).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/compliance/rekyc/campaigns/" + id;
    }

    @GetMapping("/export/hits.csv")
    public ResponseEntity<String> exportHits(HttpServletRequest request) {
        String csv = new String(apiClient.getBytes("/api/compliance/export/hits.csv", token(request)),
                java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"watchlist-hits.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
