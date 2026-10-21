package com.meridianbank.web.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridianbank.web.api.ApiClient;
import com.meridianbank.web.auth.SessionAuthFilter;
import com.meridianbank.web.auth.WebUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/**
 * Adds shared context to every rendered template: the CSRF token (rendered into forms), the
 * current user and the unread notification count for the shell bell.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final ApiClient apiClient;

    public GlobalModelAdvice(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @ModelAttribute
    public void sharedModel(HttpServletRequest request, org.springframework.ui.Model model, CsrfToken csrfToken) {
        model.addAttribute("csrfToken", csrfToken.getToken());
        model.addAttribute("csrfParameterName", csrfToken.getParameterName());
        model.addAttribute("csrfHeaderName", csrfToken.getHeaderName());
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionAuthFilter.TOKEN_ATTR) != null) {
            String token = (String) session.getAttribute(SessionAuthFilter.TOKEN_ATTR);
            WebUser user = new WebUser(
                    (String) session.getAttribute(SessionAuthFilter.EMAIL_ATTR),
                    (String) session.getAttribute(SessionAuthFilter.ROLE_ATTR),
                    (String) session.getAttribute(SessionAuthFilter.NAME_ATTR));
            model.addAttribute("user", user);
            model.addAttribute("unread", unreadCount(token));
        } else {
            model.addAttribute("user", null);
            model.addAttribute("unread", 0);
        }
    }

    private long unreadCount(String token) {
        try {
            JsonNode node = apiClient.get("/api/notifications", token);
            return node.path("unread").asLong();
        } catch (Exception e) {
            return 0;
        }
    }
}
