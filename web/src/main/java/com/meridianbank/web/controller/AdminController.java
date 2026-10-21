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
 * Bank admin console: staff and branch management, KYC tier policy, document requirements,
 * roster CSV and branch XML imports, the append-only audit log (read-only) and integration
 * settings. Admins cannot approve, decline, verify documents or disposition hits.
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends WebSupport {

    private final ApiClient apiClient;

    public AdminController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        model.addAttribute("dashboard", apiClient.get("/api/admin/dashboard", token(request)));
        model.addAttribute("staff", apiClient.get("/api/admin/staff", token(request)));
        model.addAttribute("activeNav", "admin");
        return "admin/dashboard";
    }

    // ---------------------------------------------------------------- staff

    @GetMapping("/staff")
    public String staff(@RequestParam(required = false) String q, @RequestParam(required = false) String role,
                        HttpServletRequest request, Model model) {
        StringBuilder uri = new StringBuilder("/api/admin/staff?");
        if (q != null && !q.isBlank()) {
            uri.append("q=").append(java.net.URLEncoder.encode(q, StandardCharsets.UTF_8));
        }
        if (role != null && !role.isBlank()) {
            uri.append(uri.toString().endsWith("?") ? "" : "&").append("role=").append(role);
        }
        model.addAttribute("staff", apiClient.get(uri.toString(), token(request)));
        model.addAttribute("branches", apiClient.get("/api/reference/branches", token(request)));
        model.addAttribute("filters", Map.of("q", q == null ? "" : q, "role", role == null ? "" : role));
        model.addAttribute("activeNav", "admin");
        return "admin/staff";
    }

    @PostMapping("/staff")
    public String createStaff(@RequestParam String employeeId, @RequestParam String fullName,
                              @RequestParam String role, @RequestParam String branchCode,
                              @RequestParam(required = false) String maxApprovalTier,
                              @RequestParam String email,
                              HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/admin/staff", token(request), Map.of(
                    "employeeId", employeeId, "fullName", fullName, "role", role,
                    "branchCode", branchCode,
                    "maxApprovalTier", maxApprovalTier == null ? "tier_0" : maxApprovalTier,
                    "email", email));
            flashSuccess(attributes, "Staff created; a set-password invitation was emailed.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}")
    public String updateStaff(@PathVariable Long id, @RequestParam(required = false) String role,
                              @RequestParam(required = false) String branchCode,
                              @RequestParam(required = false) String maxApprovalTier,
                              @RequestParam(required = false) String status,
                              HttpServletRequest request, RedirectAttributes attributes) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            if (role != null && !role.isBlank()) {
                body.put("role", role);
            }
            if (branchCode != null && !branchCode.isBlank()) {
                body.put("branchCode", branchCode);
            }
            if (maxApprovalTier != null && !maxApprovalTier.isBlank()) {
                body.put("maxApprovalTier", maxApprovalTier);
            }
            if (status != null && !status.isBlank()) {
                body.put("status", status);
            }
            apiClient.put("/api/admin/staff/" + id, token(request), body);
            flashSuccess(attributes, "Staff updated.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/import")
    public String importStaff(@RequestParam("file") MultipartFile file, HttpServletRequest request,
                              RedirectAttributes attributes) throws IOException {
        try {
            JsonNode result = apiClient.postMultipart("/api/admin/staff/import", token(request),
                    "file", file.getOriginalFilename() == null ? "roster.csv" : file.getOriginalFilename(),
                    file.getContentType() == null ? "text/csv" : file.getContentType(),
                    file.getBytes(), Map.of());
            int errors = result.path("errors").size();
            flashSuccess(attributes, "Roster imported (" + errors + " row errors, if any).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    // ---------------------------------------------------------------- branches

    @GetMapping("/branches")
    public String branches(HttpServletRequest request, Model model) {
        model.addAttribute("branches", apiClient.get("/api/admin/branches", token(request)));
        model.addAttribute("activeNav", "admin");
        return "admin/branches";
    }

    @PostMapping("/branches")
    public String createBranch(@RequestParam String code, @RequestParam String name,
                               @RequestParam String region,
                               HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/admin/branches", token(request),
                    Map.of("code", code, "name", name, "region", region));
            flashSuccess(attributes, "Branch created.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/branches";
    }

    @PostMapping("/branches/{id}")
    public String updateBranch(@PathVariable Long id, @RequestParam String name,
                               @RequestParam String region,
                               HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.put("/api/admin/branches/" + id, token(request),
                    Map.of("name", name, "region", region));
            flashSuccess(attributes, "Branch updated.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/branches";
    }

    @PostMapping("/branches/import-xml")
    public String importBranchesXml(@RequestParam("file") MultipartFile file, HttpServletRequest request,
                                    RedirectAttributes attributes) throws IOException {
        try {
            JsonNode result = apiClient.postMultipart("/api/admin/branches/import-xml", token(request),
                    "file", file.getOriginalFilename() == null ? "branches.xml" : file.getOriginalFilename(),
                    file.getContentType() == null ? "application/xml" : file.getContentType(),
                    file.getBytes(), Map.of());
            int errors = result.path("errors").size();
            flashSuccess(attributes, "Branches imported (" + errors + " row errors, if any).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/branches";
    }

    // ---------------------------------------------------------------- tier policy + requirements

    @GetMapping("/kyc-tiers")
    public String kycTiers(HttpServletRequest request, Model model) {
        model.addAttribute("tiers", apiClient.get("/api/admin/kyc-tiers", token(request)));
        model.addAttribute("activeNav", "admin");
        return "admin/kyc-tiers";
    }

    @PostMapping("/kyc-tiers/{tierCode}")
    public String updateTier(@PathVariable String tierCode, @RequestParam String dailyTransactionLimit,
                             @RequestParam String balanceCeiling,
                             HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.put("/api/admin/kyc-tiers/" + tierCode, token(request), Map.of(
                    "dailyTransactionLimit", dailyTransactionLimit,
                    "balanceCeiling", balanceCeiling));
            flashSuccess(attributes, "Tier policy updated.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/kyc-tiers";
    }

    @GetMapping("/requirements")
    public String requirements(HttpServletRequest request, Model model) {
        model.addAttribute("requirements", apiClient.get("/api/admin/document-requirements", token(request)));
        model.addAttribute("activeNav", "admin");
        return "admin/requirements";
    }

    @PostMapping("/requirements")
    public String createRequirement(@RequestParam String kycTier, @RequestParam String docType,
                                    @RequestParam(required = false) String conditionType,
                                    HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/admin/document-requirements", token(request),
                    Map.of("kycTier", kycTier, "docType", docType,
                            "conditionType", conditionType == null ? "" : conditionType));
            flashSuccess(attributes, "Requirement created.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/requirements";
    }

    @PostMapping("/requirements/{id}/delete")
    public String deleteRequirement(@PathVariable Long id, HttpServletRequest request,
                                    RedirectAttributes attributes) {
        try {
            apiClient.delete("/api/admin/document-requirements/" + id, token(request));
            flashSuccess(attributes, "Requirement deleted.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/requirements";
    }

    // ---------------------------------------------------------------- audit (read-only)

    @GetMapping("/audit")
    public String audit(@RequestParam(required = false) String action,
                        @RequestParam(required = false) String actor,
                        @RequestParam(required = false) String entity,
                        @RequestParam(defaultValue = "0") int page,
                        HttpServletRequest request, Model model) {
        StringBuilder uri = new StringBuilder("/api/admin/audit?page=").append(page);
        if (action != null && !action.isBlank()) {
            uri.append("&action=").append(java.net.URLEncoder.encode(action, StandardCharsets.UTF_8));
        }
        if (actor != null && !actor.isBlank()) {
            uri.append("&actor=").append(java.net.URLEncoder.encode(actor, StandardCharsets.UTF_8));
        }
        if (entity != null && !entity.isBlank()) {
            uri.append("&entity=").append(java.net.URLEncoder.encode(entity, StandardCharsets.UTF_8));
        }
        model.addAttribute("result", apiClient.get(uri.toString(), token(request)));
        model.addAttribute("filters", Map.of(
                "action", action == null ? "" : action,
                "actor", actor == null ? "" : actor,
                "entity", entity == null ? "" : entity));
        model.addAttribute("activeNav", "admin");
        return "admin/audit";
    }

    @GetMapping("/audit/export.csv")
    public ResponseEntity<String> auditCsv(@RequestParam(required = false) String action,
                                           @RequestParam(required = false) String actor,
                                           HttpServletRequest request) {
        StringBuilder uri = new StringBuilder("/api/admin/audit/export.csv?");
        if (action != null && !action.isBlank()) {
            uri.append("action=").append(java.net.URLEncoder.encode(action, StandardCharsets.UTF_8));
        }
        if (actor != null && !actor.isBlank()) {
            uri.append(uri.toString().endsWith("?") ? "" : "&")
                    .append("actor=").append(java.net.URLEncoder.encode(actor, StandardCharsets.UTF_8));
        }
        String csv = new String(apiClient.getBytes(uri.toString(), token(request)), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    // ---------------------------------------------------------------- integrations

    @GetMapping("/integrations")
    public String integrations(HttpServletRequest request, Model model) {
        model.addAttribute("integrations", apiClient.get("/api/admin/integrations", token(request)));
        model.addAttribute("activeNav", "admin");
        return "admin/integrations";
    }

    @PostMapping("/integrations/webhook")
    public String updateWebhook(@RequestParam String endpointUrl, @RequestParam(required = false) String secret,
                                @RequestParam(required = false, defaultValue = "false") String enabled,
                                HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.put("/api/admin/integrations/webhook", token(request), Map.of(
                    "endpointUrl", endpointUrl,
                    "secret", secret == null ? "" : secret,
                    "enabled", enabled));
            flashSuccess(attributes, "Webhook settings updated.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/admin/integrations";
    }
}
