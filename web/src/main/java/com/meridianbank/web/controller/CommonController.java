package com.meridianbank.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridianbank.web.api.ApiClient;
import com.meridianbank.web.auth.DeskSessionStore;
import com.meridianbank.web.auth.SessionAuthFilter;
import com.meridianbank.web.auth.WebUser;
import com.meridianbank.web.support.WebSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/** Shared pages: home redirect, profile/password change, notifications, mock inbox and errors. */
@Controller
public class CommonController extends WebSupport {

    private final ApiClient apiClient;
    private final DeskSessionStore deskSessionStore;

    public CommonController(ApiClient apiClient, DeskSessionStore deskSessionStore) {
        this.apiClient = apiClient;
        this.deskSessionStore = deskSessionStore;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        WebUser user = currentUser(request);
        return "redirect:" + (user == null ? "/login" : AuthController.homeForRole(user.role()));
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        model.addAttribute("me", apiClient.get("/api/auth/me", token(request)));
        model.addAttribute("activeNav", "profile");
        return "common/profile";
    }

    @PostMapping("/profile/password/otp")
    public String passwordOtp(HttpServletRequest request, RedirectAttributes attributes) {
        try {
            apiClient.post("/api/auth/otp/step-up/issue", token(request), Map.of("purpose", "password_change"));
            flashSuccess(attributes, "Step-up code sent (check the notification bell).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
                                 @RequestParam String otpCode,
                                 HttpServletRequest request, RedirectAttributes attributes) {
        String token = token(request);
        try {
            apiClient.post("/api/auth/otp/step-up/verify", token,
                    Map.of("purpose", "password_change", "code", otpCode));
            apiClient.post("/api/auth/password/change", token,
                    Map.of("currentPassword", currentPassword, "newPassword", newPassword));
            flashSuccess(attributes, "Password changed.");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/notifications")
    public String notifications(HttpServletRequest request, Model model) {
        model.addAttribute("result", apiClient.get("/api/notifications", token(request)));
        model.addAttribute("activeNav", "notifications");
        return "common/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@PathVariable Long id, HttpServletRequest request) {
        apiClient.post("/api/notifications/" + id + "/read", token(request), null);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(HttpServletRequest request) {
        apiClient.post("/api/notifications/read-all", token(request), null);
        return "redirect:/notifications";
    }

    /** Small AJAX endpoint used by the notification bell's "mark visible as read" widget. */
    @PostMapping(value = "/notifications/bulk-read", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> bulkMarkRead(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Object ids = payload.get("ids");
        JsonNode result = apiClient.post("/api/notifications/bulk-read", token(request),
                Map.of("ids", ids == null ? java.util.List.of() : ids));
        return Map.of("ok", true, "updated", result.path("updated").asInt());
    }

    /**
     * Branch-tablet workspace context: the front desk pins the operator identity and desk role for
     * the shift so the console opens on the right workspace without re-selecting it every time.
     */
    @PostMapping(value = "/workspace/context", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> setWorkspaceContext(@RequestBody Map<String, Object> body,
                                                   HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if (body.get("role") != null) {
            session.setAttribute(SessionAuthFilter.ROLE_ATTR, String.valueOf(body.get("role")));
        }
        if (body.get("email") != null) {
            session.setAttribute(SessionAuthFilter.EMAIL_ATTR, String.valueOf(body.get("email")));
        }
        if (body.get("name") != null) {
            session.setAttribute(SessionAuthFilter.NAME_ATTR, String.valueOf(body.get("name")));
        }
        return Map.of("ok", true);
    }

    /**
     * Opens the shared-tablet desk workspace for the shift; the desk reference is kept in the
     * tablet's cookie so the front desk can return to the same workspace.
     */
    @PostMapping("/workspace/desk")
    public String openDesk(@RequestParam String role, @RequestParam String email,
                           @RequestParam(required = false) String name,
                           HttpServletResponse response, RedirectAttributes attributes) {
        String deskRef = deskSessionStore.open(role, email, name);
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("mb_desk", deskRef).path("/").build().toString());
        flashSuccess(attributes, "Desk session opened for this shift.");
        return "redirect:/";
    }

    @GetMapping("/inbox")
    public String inbox(HttpServletRequest request, Model model) {
        model.addAttribute("result", apiClient.get("/api/outbox/me", token(request)));
        model.addAttribute("activeNav", "inbox");
        return "common/inbox";
    }

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("message", "You do not have permission to view this page. If you believe this is a mistake, contact a bank administrator.");
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound(Model model) {
        model.addAttribute("message", "That page does not exist or has moved.");
        return "error/404";
    }

    /** Document download proxy: the decision tier re-authorizes on every request. */
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, HttpServletRequest request) {
        byte[] bytes = apiClient.getBytes("/api/documents/" + id + "/download", token(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    /** Inline preview of an uploaded document (embedded in the review widget, not downloaded). */
    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id, HttpServletRequest request) {
        byte[] bytes = apiClient.getBytes("/api/documents/" + id + "/download", token(request));
        return ResponseEntity.ok()
                .header("X-Frame-Options", "ALLOWALL")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
