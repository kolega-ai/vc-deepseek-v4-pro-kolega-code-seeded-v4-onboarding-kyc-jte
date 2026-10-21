package com.meridianbank.onboarding.config;

import com.meridianbank.onboarding.domain.repo.AuthSessionRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Decision tier security. This module is a Spring Security OAuth2 resource server: it validates
 * every JWT's signature, algorithm, issuer, audience and expiry on every request, rejects tokens
 * with an absent or unexpected algorithm (including "none"), and additionally rejects tokens whose
 * session was revoked. All authorization decisions are then made server-side from the database.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/registration/**",
            "/api/auth/login",
            "/api/auth/password-reset/request",
            "/api/auth/password-reset/complete",
            "/api/integration/partner/**",
            "/portal/documents",
            "/portal/documents/**",
            "/actuator/health",
            "/actuator/health/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Invalid or missing bearer token\"}");
                        }))
                .exceptionHandling(eh -> eh
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Insufficient authorization\"}");
                        }));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret,
                                 @Value("${app.jwt.issuer}") String issuer,
                                 @Value("${app.jwt.audience}") String audience,
                                 AuthSessionRepository authSessionRepository) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        // Restrict to exactly HS256: tokens signed with any other algorithm (or with none at all)
        // are rejected before any claim is trusted.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new OAuth2TokenValidator<>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt token) {
                List<String> audiences = token.getAudience();
                if (audiences == null || !audiences.contains(audience)) {
                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Audience mismatch: '" + audience + "' required", null));
                }
                return OAuth2TokenValidatorResult.success();
            }
        };
        OAuth2TokenValidator<Jwt> sessionRevocation = new OAuth2TokenValidator<>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt token) {
                String sid = token.getClaimAsString("sid");
                if (sid == null || token.getSubject() == null) {
                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Missing session binding", null));
                }
                var session = authSessionRepository.findById(sid);
                if (session.isEmpty() || session.get().getRevokedAt() != null) {
                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Session revoked or unknown", null));
                }
                Instant expiresAt = session.get().getExpiresAt();
                if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Session expired", null));
                }
                return OAuth2TokenValidatorResult.success();
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience, sessionRevocation));
        return decoder;
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_" + (role == null ? "UNKNOWN" : role)));
        });
        return converter;
    }
}
