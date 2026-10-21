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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Officer console: branch-scoped dashboard, queues, search with filters, application review with
 * document verification, request-more-documents, approve/decline (both behind step-up OTP) and
 * the branch-scoped CSV export.
 */
@Controller
@RequestMapping("/officer")
public class OfficerController extends WebSupport {

    private final ApiClient apiClient;

    public OfficerController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        model.addAttribute("dashboard", apiClient.get("/api/officer/dashboard", token(request)));
        model.addAttribute("queue", queue(request, null));
        model.addAttribute("activeNav", "officer");
        return "officer/dashboard";
    }

    @GetMapping("/applications")
    public String applications(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String kycTier,
                               @RequestParam(required = false) String q,
                               @RequestParam(required = false) String from,
                               @RequestParam(required = false) String to,
                               @RequestParam(required = false) String sort,
                               @RequestParam(required = false) String dir,
                               @RequestParam(defaultValue = "0") int page,
                               HttpServletRequest request, Model model) {
        String token = token(request);
        StringBuilder uri = new StringBuilder("/api/officer/applications?page=").append(page);
        append(uri, "status", status);
        append(uri, "kycTier", kycTier);
        append(uri, "q", q);
        append(uri, "from", from);
        append(uri, "to", to);
        append(uri, "sort", sort);
        append(uri, "dir", dir);
        JsonNode result = apiClient.get(uri.toString(), token);
        model.addAttribute("result", result);
        model.addAttribute("filters", Map.of(
                "status", status == null ? "" : status,
                "kycTier", kycTier == null ? "" : kycTier,
                "q", q == null ? "" : q,
                "from", from == null ? "" : from,
                "to", to == null ? "" : to,
                "sort", sort == null ? "" : sort,
                "dir", dir == null ? "" : dir));
        model.addAttribute("activeNav", "officer");
        return "officer/applications";
    }

    private JsonNode queue(HttpServletRequest request, String status) {
        StringBuilder uri = new StringBuilder("/api/officer/applications?size=10");
        if (status != null && !status.isBlank()) {
            uri.append("&status=").append(status);
        }
        return apiClient.get(uri.toString(), token(request));
    }

    @GetMapping("/applications/{id}")
    public String application(@PathVariable Long id, HttpServletRequest request, Model model) {
        model.addAttribute("view", apiClient.get("/api/officer/applications/" + id, token(request)));
        model.addAttribute("activeNav", "officer");
        return "officer/application";
    }

    @PostMapping("/documents/{id}/verify")
    public String verifyDocument(@PathVariable Long id, @RequestParam String returnTo,
                                 HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/officer/documents/" + id + "/verify", token(request), null);
            flashSuccess(attributes, "Document verified.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:" + safeReturn(returnTo);
    }

    @PostMapping("/documents/{id}/reject")
    public String rejectDocument(@PathVariable Long id, @RequestParam String returnTo,
                                 HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/officer/documents/" + id + "/reject", token(request), null);
            flashSuccess(attributes, "Document rejected; the applicant will upload a replacement.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:" + safeReturn(returnTo);
    }

    @PostMapping("/applications/{id}/request-documents")
    public String requestDocuments(@PathVariable Long id, @RequestParam List<String> types,
                                   @RequestParam String message,
                                   HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/officer/applications/" + id + "/request-documents", token(request),
                    Map.of("types", types, "message", message));
            flashSuccess(attributes, "Documents requested from the applicant.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/officer/applications/" + id;
    }

    @PostMapping("/applications/{id}/approve/otp")
    public String approveOtp(@PathVariable Long id, HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/auth/otp/step-up/issue", token(request), Map.of("purpose", "approve"));
            flashSuccess(attributes, "Step-up code sent (check the notification bell).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/officer/applications/" + id;
    }

    @PostMapping("/applications/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam String otpCode,
                          @RequestParam String customerVisibleReason,
                          HttpServletRequest request, RedirectAttributes attributes) {
        String token = token(request);
        try {
            apiClient.post("/api/auth/otp/step-up/verify", token,
                    Map.of("purpose", "approve", "code", otpCode));
            apiClient.post("/api/officer/applications/" + id + "/approve", token,
                    Map.of("customerVisibleReason", customerVisibleReason));
            flashSuccess(attributes, "Application approved and the account was opened in the core.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/officer/applications/" + id;
    }

    @PostMapping("/applications/{id}/decline/otp")
    public String declineOtp(@PathVariable Long id, HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/auth/otp/step-up/issue", token(request), Map.of("purpose", "decline"));
            flashSuccess(attributes, "Step-up code sent (check the notification bell).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/officer/applications/" + id;
    }

    @PostMapping("/applications/{id}/decline")
    public String decline(@PathVariable Long id, @RequestParam String otpCode,
                          @RequestParam String customerVisibleReason,
                          @RequestParam String internalReason,
                          HttpServletRequest request, RedirectAttributes attributes) {
        String token = token(request);
        try {
            apiClient.post("/api/auth/otp/step-up/verify", token,
                    Map.of("purpose", "decline", "code", otpCode));
            apiClient.post("/api/officer/applications/" + id + "/decline", token,
                    Map.of("customerVisibleReason", customerVisibleReason,
                            "internalReason", internalReason));
            flashSuccess(attributes, "Application declined; the applicant has been notified.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/officer/applications/" + id;
    }

    @GetMapping("/export.csv")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String q,
                                            HttpServletRequest request) {
        StringBuilder uri = new StringBuilder("/api/officer/applications/export.csv");
        List<String> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            params.add("status=" + status);
        }
        if (q != null && !q.isBlank()) {
            params.add("q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        }
        if (!params.isEmpty()) {
            uri.append("?").append(String.join("&", params));
        }
        String csv = new String(apiClient.getBytes(uri.toString(), token(request)),
                java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private void append(StringBuilder uri, String name, String value) {
        if (value != null && !value.isBlank()) {
            uri.append("&").append(name).append("=")
                    .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String safeReturn(String returnTo) {
        if (returnTo != null && returnTo.startsWith("/officer/applications/")) {
            return returnTo;
        }
        return "/officer/dashboard";
    }
}
