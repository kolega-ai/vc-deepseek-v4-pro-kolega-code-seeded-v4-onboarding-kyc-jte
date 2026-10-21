package com.meridianbank.providermock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Single shared credential for all provider endpoints. Requests without the exact key are rejected
 * with 401 before reaching any handler.
 */
@Component
public class ProviderKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Provider-Key";

    private final String apiKey;

    public ProviderKeyFilter(@Value("${app.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader(HEADER);
        if (provided == null || !provided.equals(apiKey)) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"errorCode\":\"PROVIDER-1001\",\"message\":\"invalid provider credential\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
