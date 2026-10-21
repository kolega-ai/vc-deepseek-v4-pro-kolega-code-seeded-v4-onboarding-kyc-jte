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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Public entry flow: login, registration with email OTP, password creation, password reset and
 * logout. The web tier authenticates the user by delegating to the decision tier's auth
 * component; the returned JWT is kept in the server-side session only.
 */
@Controller
public class AuthController extends WebSupport {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final ApiClient apiClient;

    public AuthController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/login")
    public String loginForm(Model model, @RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String next,
                            HttpServletRequest request) {
        model.addAttribute("next", next == null ? "" : next);
        if (error != null) {
            model.addAttribute("flash", Map.of("kind", "error", "message", error));
        }
        if (registered != null) {
            model.addAttribute("flash", Map.of("kind", "success",
                    "message", "Registration complete — you can now sign in."));
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        @RequestParam(required = false) String next,
                        HttpServletRequest request, RedirectAttributes attributes) {
        log.info("Login attempt for {} from client {}", email, request.getHeader("User-Agent"));
        try {
            JsonNode response = apiClient.post("/api/auth/login", null,
                    Map.of("email", email, "password", password));
            String jwt = response.path("token").asText();
            String role = response.path("role").asText();
            JsonNode me = apiClient.get("/api/auth/me", jwt);
            signIn(request, jwt, role, email, me.path("displayName").asText(email));
            if (next != null && !next.isBlank()) {
                return "redirect:" + next;
            }
            return "redirect:" + homeForRole(role);
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String mobile,
                           HttpServletRequest request, RedirectAttributes attributes) {
        try {
            JsonNode response = apiClient.post("/api/auth/register", null,
                    Map.of("email", email, "mobile", mobile));
            request.getSession(true).setAttribute("registrationToken",
                    response.path("registrationToken").asText());
            return "redirect:/register/email-otp";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/register/email-otp")
    public String emailOtpForm(HttpServletRequest request, Model model) {
        String registrationToken = registrationToken(request);
        if (registrationToken == null) {
            return "redirect:/register";
        }
        try {
            model.addAttribute("emails", apiClient.get(
                    "/api/auth/registration/inbox?registrationToken=" + registrationToken, null));
        } catch (Exception e) {
            model.addAttribute("emails", null);
        }
        return "auth/register-otp";
    }

    @PostMapping("/register/email-otp/issue")
    public String issueEmailOtp(HttpServletRequest request, RedirectAttributes attributes) {
        String registrationToken = registrationToken(request);
        if (registrationToken == null) {
            return "redirect:/register";
        }
        try {
            apiClient.post("/api/auth/registration/otp/issue", null,
                    Map.of("registrationToken", registrationToken));
            flashSuccess(attributes, "A code was sent to your email (see the demo inbox after sign-in).");
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
        }
        return "redirect:/register/email-otp";
    }

    @PostMapping("/register/email-otp/verify")
    public String verifyEmailOtp(@RequestParam String code, HttpServletRequest request,
                                 RedirectAttributes attributes) {
        String registrationToken = registrationToken(request);
        if (registrationToken == null) {
            return "redirect:/register";
        }
        try {
            apiClient.post("/api/auth/registration/otp/verify", null,
                    Map.of("registrationToken", registrationToken, "code", code));
            return "redirect:/register/password";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/register/email-otp";
        }
    }

    @GetMapping("/register/password")
    public String passwordForm(HttpServletRequest request) {
        if (registrationToken(request) == null) {
            return "redirect:/register";
        }
        return "auth/register-password";
    }

    @PostMapping("/register/password")
    public String setPassword(@RequestParam String password, @RequestParam(required = false) String next,
                              HttpServletRequest request, RedirectAttributes attributes) {
        String registrationToken = registrationToken(request);
        if (registrationToken == null) {
            return "redirect:/register";
        }
        try {
            JsonNode response = apiClient.post("/api/auth/registration/password", null,
                    Map.of("registrationToken", registrationToken, "password", password));
            String jwt = response.path("token").asText();
            String role = response.path("role").asText();
            String email = response.path("email").asText();
            JsonNode me = apiClient.get("/api/auth/me", jwt);
            request.getSession().removeAttribute("registrationToken");
            signIn(request, jwt, role, email, me.path("displayName").asText(email));
            flashSuccess(attributes, "Welcome to Meridian Bank! Your application has been created.");
            if (next != null && !next.isBlank()) {
                return "redirect:" + next;
            }
            return "redirect:" + homeForRole(role);
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/register/password";
        }
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token == null ? "" : token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password/request")
    public String requestReset(@RequestParam String email, RedirectAttributes attributes) {
        apiClient.post("/api/auth/password-reset/request", null, Map.of("email", email));
        flashSuccess(attributes,
                "If that email has an account, a reset link was sent (check the demo inbox after sign-in).");
        return "redirect:/login";
    }

    @PostMapping("/reset-password/complete")
    public String completeReset(@RequestParam String token, @RequestParam String newPassword,
                                RedirectAttributes attributes) {
        try {
            apiClient.post("/api/auth/password-reset/complete", null,
                    Map.of("token", token, "newPassword", newPassword));
            flashSuccess(attributes, "Password reset. You can now sign in.");
            return "redirect:/login";
        } catch (ApiClient.ApiClientException e) {
            flashError(attributes, e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        String token = token(request);
        if (token != null) {
            try {
                apiClient.post("/api/auth/logout", token, null);
            } catch (Exception ignored) {
                // the web session is cleared regardless
            }
        }
        signOut(request);
        return "redirect:/login";
    }

    private String registrationToken(HttpServletRequest request) {
        return request.getSession(false) == null ? null
                : (String) request.getSession(false).getAttribute("registrationToken");
    }

    public static String homeForRole(String role) {
        return switch (role) {
            case "prospect" -> "/journey";
            case "customer" -> "/customer/dashboard";
            case "onboarding_officer" -> "/officer/dashboard";
            case "compliance_officer" -> "/compliance/dashboard";
            case "bank_admin" -> "/admin/dashboard";
            default -> "/journey";
        };
    }
}
