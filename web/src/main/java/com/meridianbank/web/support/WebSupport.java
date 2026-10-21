package com.meridianbank.web.support;

import com.meridianbank.web.auth.SessionAuthFilter;
import com.meridianbank.web.auth.WebUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/** Shared helpers for channel controllers: session state, flash messages and model maps. */
public abstract class WebSupport {

    protected String token(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(SessionAuthFilter.TOKEN_ATTR);
    }

    protected WebUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return new WebUser(
                (String) session.getAttribute(SessionAuthFilter.EMAIL_ATTR),
                (String) session.getAttribute(SessionAuthFilter.ROLE_ATTR),
                (String) session.getAttribute(SessionAuthFilter.NAME_ATTR));
    }

    protected void signIn(HttpServletRequest request, String jwt, String role, String email, String name) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionAuthFilter.TOKEN_ATTR, jwt);
        session.setAttribute(SessionAuthFilter.ROLE_ATTR, role);
        session.setAttribute(SessionAuthFilter.EMAIL_ATTR, email);
        session.setAttribute(SessionAuthFilter.NAME_ATTR, name);
    }

    protected void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    protected void flash(RedirectAttributes attributes, String kind, String message) {
        attributes.addFlashAttribute("flash", Map.of("kind", kind, "message", message));
    }

    protected void flashError(RedirectAttributes attributes, String message) {
        flash(attributes, "error", message);
    }

    protected void flashSuccess(RedirectAttributes attributes, String message) {
        flash(attributes, "success", message);
    }

    protected Map<String, Object> model(String token, WebUser user, String activeNav) {
        Map<String, Object> model = new HashMap<>();
        model.put("activeNav", activeNav);
        return model;
    }
}
