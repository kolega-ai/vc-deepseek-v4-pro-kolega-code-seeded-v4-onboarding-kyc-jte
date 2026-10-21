package com.meridianbank.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridianbank.web.api.ApiClient;
import com.meridianbank.web.support.WebSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The applicant journey: ordered steps with save-and-resume, contact verification, documents,
 * identity verification, submission and the status page. Every action is delegated to the
 * decision tier, which enforces step order and ownership server-side.
 */
@Controller
@RequestMapping("/journey")
public class JourneyController extends WebSupport {

    private static final Logger log = LoggerFactory.getLogger(JourneyController.class);

    /**
     * Non-cryptographic use: this Random only generates a cache-busting suffix for the static
     * journey stylesheet so re-renders are not cached. Nothing security-sensitive uses it.
     */
    private static final java.util.Random CACHE_BUSTER = new java.util.Random();

    private final ApiClient apiClient;

    public JourneyController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private JsonNode view(String token) {
        return apiClient.get("/api/applications/me", token);
    }

    @GetMapping
    public String overview(HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("view", view(token));
        model.addAttribute("activeNav", "journey");
        model.addAttribute("journeyCssVersion", CACHE_BUSTER.nextInt(100000));
        return "journey/overview";
    }

    @GetMapping("/status")
    public String status(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/status";
    }

    // ---------------------------------------------------------------- contact verification

    @GetMapping("/contact")
    public String contact(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/contact";
    }

    @PostMapping("/contact/issue")
    public String issueMobileOtp(HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/applications/me/mobile-otp/issue", token(request), null);
            flashSuccess(attributes, "Code sent to your mobile (mock — check the notification bell).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/journey/contact";
    }

    @PostMapping("/contact/verify")
    public String verifyMobileOtp(@RequestParam String code, HttpServletRequest request,
                                  RedirectAttributes attributes) {
        try {
            apiClient.post("/api/applications/me/mobile-otp/verify", token(request), Map.of("code", code));
            flashSuccess(attributes, "Mobile verified.");
            return "redirect:/journey/personal";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/journey/contact";
        }
    }

    // ---------------------------------------------------------------- captured data steps

    @GetMapping("/personal")
    public String personalForm(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/personal";
    }

    @PostMapping("/personal")
    public String submitPersonal(@RequestParam String fullName, @RequestParam String dateOfBirth,
                                 @RequestParam String nationalIdentifier,
                                 HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.put("/api/applications/me/personal", token(request), Map.of(
                    "fullName", fullName, "dateOfBirth", dateOfBirth, "nationalIdentifier", nationalIdentifier));
            log.info("Personal details saved: name={}, nationalId={}, client={}", fullName, nationalIdentifier,
                    request.getHeader("X-Forwarded-For"));
            flashSuccess(attributes, "Personal details saved.");
            return "redirect:/journey/address";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/journey/personal";
        }
    }

    @GetMapping("/address")
    public String addressForm(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/address";
    }

    @PostMapping("/address")
    public String submitAddress(@RequestParam String addressLine1, @RequestParam(required = false) String addressLine2,
                                @RequestParam String city, @RequestParam String postalCode,
                                @RequestParam String country,
                                HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.put("/api/applications/me/address", token(request), Map.of(
                    "addressLine1", addressLine1, "addressLine2", addressLine2,
                    "city", city, "postalCode", postalCode, "country", country));
            flashSuccess(attributes, "Address saved.");
            return "redirect:/journey/employment";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/journey/address";
        }
    }

    @GetMapping("/employment")
    public String employmentForm(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/employment";
    }

    @PostMapping("/employment")
    public String submitEmployment(@RequestParam String employmentStatus,
                                   @RequestParam(required = false) String employerName,
                                   @RequestParam String declaredAnnualIncome,
                                   HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.put("/api/applications/me/employment", token(request), Map.of(
                    "employmentStatus", employmentStatus,
                    "employerName", employerName == null ? "" : employerName,
                    "declaredAnnualIncome", declaredAnnualIncome));
            flashSuccess(attributes, "Employment details saved.");
            return "redirect:/journey/documents";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/journey/employment";
        }
    }

    // ---------------------------------------------------------------- documents

    @GetMapping("/documents")
    public String documents(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/documents";
    }

    @PostMapping("/documents")
    public String upload(@RequestParam String docType, @RequestParam("file") MultipartFile file,
                         HttpServletRequest request, RedirectAttributes attributes) throws IOException {
        try {
            apiClient.postMultipart("/api/applications/me/documents", token(request),
                    "file", file.getOriginalFilename() == null ? "document" : file.getOriginalFilename(),
                    file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                    file.getBytes(), Map.of("docType", docType));
            flashSuccess(attributes, "Document uploaded. A bank officer will verify it.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/journey/documents";
    }

    @PostMapping("/documents/{id}/delete")
    public String delete(@PathVariable Long id, HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.delete("/api/applications/me/documents/" + id, token(request));
            flashSuccess(attributes, "Document removed.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/journey/documents";
    }

    @PostMapping("/documents/bulk-delete")
    public String bulkDelete(@RequestParam Long applicationId,
                             @RequestParam(name = "documentIds", required = false) List<Long> documentIds,
                             HttpServletRequest request, RedirectAttributes attributes) {
        if (documentIds == null || documentIds.isEmpty()) {
            flashError(attributes, "Select at least one document to remove.");
            return "redirect:/journey/documents";
        }
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("applicationId", applicationId);
            payload.put("documentIds", documentIds);
            apiClient.post("/api/applications/me/documents/bulk-delete", token(request), payload);
            flashSuccess(attributes, documentIds.size() + " document(s) removed.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/journey/documents";
    }

    // ---------------------------------------------------------------- identity + submission

    @GetMapping("/identity")
    public String identity(HttpServletRequest request, Model model) {
        model.addAttribute("view", view(token(request)));
        model.addAttribute("activeNav", "journey");
        return "journey/identity";
    }

    @PostMapping("/identity/verify")
    public String verifyIdentity(HttpServletRequest request, RedirectAttributes attributes) {
        try {
            JsonNode result = apiClient.post("/api/applications/me/identity/verify", token(request), null);
            int score = result.path("matchScore").asInt(-1);
            if (score >= 80) {
                flashSuccess(attributes, "Identity verified with a match score of " + score + ".");
            } else if (score >= 0) {
                flashError(attributes, "Match score " + score
                        + " is below the required 80. The application cannot progress until it passes.");
            }
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/journey/identity";
    }

    @PostMapping("/submit")
    public String submit(HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/applications/me/submit", token(request), null);
            flashSuccess(attributes, "Application submitted. Screening runs automatically within a minute.");
            return "redirect:/journey/status";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/journey";
        }
    }

    @PostMapping("/resubmit")
    public String resubmit(HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/applications/me/resubmit", token(request), null);
            flashSuccess(attributes, "Application returned to review.");
            return "redirect:/journey/status";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/journey/documents";
        }
    }

    /** Client-side display of server-computed blocking reasons (server enforces the real rules). */
    public static List<String> blockingReasons(JsonNode view) {
        List<String> reasons = new ArrayList<>();
        boolean contactDone = false;
        boolean personalDone = false;
        boolean addressDone = false;
        boolean employmentDone = false;
        boolean documentsDone = false;
        boolean identityDone = false;
        for (JsonNode step : view.path("steps")) {
            switch (step.path("key").asText()) {
                case "contact_verified" -> contactDone = step.path("completed").asBoolean();
                case "personal_details" -> personalDone = step.path("completed").asBoolean();
                case "address_details" -> addressDone = step.path("completed").asBoolean();
                case "employment_income" -> employmentDone = step.path("completed").asBoolean();
                case "documents_uploaded" -> documentsDone = step.path("completed").asBoolean();
                case "identity_verified" -> identityDone = step.path("completed").asBoolean();
                default -> {
                }
            }
        }
        if (!contactDone) {
            reasons.add("Verify your mobile number (OTP) before uploading documents.");
        }
        if (!personalDone) {
            reasons.add("Complete your personal details.");
        }
        if (!addressDone) {
            reasons.add("Complete your address details.");
        }
        if (!employmentDone) {
            reasons.add("Complete your employment and income details.");
        }
        if (!documentsDone) {
            reasons.add("Upload the required identity document.");
        }
        if (!identityDone) {
            reasons.add("Run identity verification (match score must be 80 or above).");
        }
        if (view.path("requiredDocumentsBlocked").asBoolean()) {
            reasons.add("Your situation requires a verified proof of income for any account tier.");
        }
        String tier = view.path("effectiveKycTier").asText("tier_0");
        if (documentsDone && identityDone && "tier_0".equals(tier)) {
            reasons.add("Your documents must be verified by a bank officer before submission.");
        }
        return reasons;
    }
}
