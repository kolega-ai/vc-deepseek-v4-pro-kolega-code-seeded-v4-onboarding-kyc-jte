package com.meridianbank.web.auth;

import com.meridianbank.web.api.ApiClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Channel-tier session filter. The web tier authenticates the user: a successful login stores the
 * decision-tier JWT in the server-side HTTP session, and this filter reconstructs the Spring
 * Security context from the session on each request. Real authorization is enforced by the
 * decision tier on every API call; the role here only drives menu and route visibility.
 */
@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    public static final String TOKEN_ATTR = "authToken";
    public static final String ROLE_ATTR = "authRole";
    public static final String EMAIL_ATTR = "authEmail";
    public static final String NAME_ATTR = "authName";

    private final DeskSessionStore deskSessionStore;

    public SessionAuthFilter(DeskSessionStore deskSessionStore) {
        this.deskSessionStore = deskSessionStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String token = session == null ? null : (String) session.getAttribute(TOKEN_ATTR);
        if (token != null && isTokenCurrent(token)) {
            applyPrincipal((String) session.getAttribute(EMAIL_ATTR), (String) session.getAttribute(ROLE_ATTR));
        } else {
            WebUser desk = deskSessionStore.find(readDeskCookie(request));
            if (desk != null && desk.role() != null && !desk.role().isBlank()) {
                applyPrincipal(desk.email(), desk.role());
            } else {
                SecurityContextHolder.clearContext();
                if (token != null && session != null) {
                    // Expired locally; clear the session token so the user can log in again.
                    session.removeAttribute(TOKEN_ATTR);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private void applyPrincipal(String email, String role) {
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                email == null ? "user" : email, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? "UNKNOWN" : role.toUpperCase()))));
        SecurityContextHolder.getContext().setAuthentication(principal);
    }

    private String readDeskCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if ("mb_desk".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /** Lightweight local expiry check; full validation always happens in the decision tier. */
    private boolean isTokenCurrent(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long exp = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload).path("exp").asLong();
            return exp > System.currentTimeMillis() / 1000;
        } catch (Exception e) {
            return false;
        }
    }
}
