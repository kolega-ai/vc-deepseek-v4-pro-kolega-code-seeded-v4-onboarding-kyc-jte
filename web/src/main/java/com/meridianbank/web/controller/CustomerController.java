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
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Customer area: profile and account summary with tier limits, own documents, re-KYC prompt and
 * the re-KYC document refresh (step-up OTP required on submit).
 */
@Controller
@RequestMapping("/customer")
public class CustomerController extends WebSupport {

    private final ApiClient apiClient;

    public CustomerController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        model.addAttribute("view", apiClient.get("/api/customer/me", token(request)));
        model.addAttribute("activeNav", "customer");
        return "customer/dashboard";
    }

    @GetMapping("/rekyc")
    public String reKyc(HttpServletRequest request, Model model) {
        model.addAttribute("view", apiClient.get("/api/customer/me", token(request)));
        model.addAttribute("activeNav", "customer");
        return "customer/rekyc";
    }

    @PostMapping("/rekyc/{caseId}/documents")
    public String uploadReKyc(@PathVariable Long caseId, @RequestParam String docType,
                              @RequestParam("file") MultipartFile file,
                              HttpServletRequest request, RedirectAttributes attributes) throws IOException {
        try {
            apiClient.postMultipart("/api/customer/rekyc/" + caseId + "/documents", token(request),
                    "file", file.getOriginalFilename() == null ? "document" : file.getOriginalFilename(),
                    file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                    file.getBytes(), Map.of("docType", docType));
            flashSuccess(attributes, "Document uploaded. A bank officer will verify it.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/customer/rekyc";
    }

    @PostMapping("/rekyc/{caseId}/otp")
    public String issueReKycOtp(@PathVariable Long caseId, HttpServletRequest request,
                                RedirectAttributes attributes) {
        try {
            apiClient.post("/api/auth/otp/step-up/issue", token(request), Map.of("purpose", "rekyc_submit"));
            flashSuccess(attributes, "Step-up code sent (check the notification bell).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/customer/rekyc";
    }

    @PostMapping("/rekyc/{caseId}/submit")
    public String submitReKyc(@PathVariable Long caseId, @RequestParam String otpCode,
                              HttpServletRequest request, RedirectAttributes attributes) {
        String token = token(request);
        try {
            apiClient.post("/api/auth/otp/step-up/verify", token,
                    Map.of("purpose", "rekyc_submit", "code", otpCode));
            apiClient.post("/api/customer/rekyc/" + caseId + "/submit", token, null);
            flashSuccess(attributes, "Re-KYC completed. Your tier and limits have been refreshed.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/customer/dashboard";
    }

    /** Own-data JSON export (applicant-visible fields only). */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(HttpServletRequest request) {
        byte[] json = apiClient.getBytes("/api/applications/me/export", token(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my-application.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
