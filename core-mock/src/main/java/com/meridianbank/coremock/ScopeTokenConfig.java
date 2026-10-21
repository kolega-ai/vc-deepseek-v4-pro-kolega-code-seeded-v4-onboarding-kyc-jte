package com.meridianbank.coremock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * core-mock verifies every scope token it receives: HS256 signature only (absent or unexpected
 * algorithms are rejected), issuer, audience and expiry. The kyc_tier and application reference
 * used for account opening come exclusively from the verified token — raw parameters in the
 * request body are ignored.
 */
@Configuration
public class ScopeTokenConfig {

    @Bean
    public JwtDecoder scopeTokenDecoder(@Value("${app.core.token-secret}") String secret,
                                        @Value("${app.core.token-issuer}") String issuer,
                                        @Value("${app.core.token-audience}") String audience) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = new OAuth2TokenValidator<>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt token) {
                List<String> audiences = token.getAudience();
                if (audiences == null || !audiences.contains(audience)) {
                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Audience mismatch", null));
                }
                return OAuth2TokenValidatorResult.success();
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        return decoder;
    }
}
