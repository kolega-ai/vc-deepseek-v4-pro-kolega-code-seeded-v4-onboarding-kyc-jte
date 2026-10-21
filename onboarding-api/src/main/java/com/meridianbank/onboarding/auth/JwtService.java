package com.meridianbank.onboarding.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Issues HMAC-SHA256 (HS256) JWTs for the channel tier. Validation of incoming tokens is owned by
 * the Spring Security resource-server configuration, which verifies signature, algorithm, issuer,
 * audience and expiry on every request and rejects tokens with an absent or unexpected algorithm.
 */
@Service
public class JwtService {

    private final AppProperties.Jwt props;
    private final String coreTokenSecret;

    public JwtService(AppProperties appProperties) {
        this.props = appProperties.getJwt();
        this.coreTokenSecret = appProperties.getCoreMock().getTokenSecret();
        if (props.getSecret() == null || props.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
        if (coreTokenSecret == null || coreTokenSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.core-mock.token-secret must be at least 32 bytes");
        }
    }

    public String issue(UserAccount user, String sessionId) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer(props.getIssuer())
                .audience(props.getAudience())
                .claim("sid", sessionId)
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(props.getTtl())))
                .build();
        return sign(claims);
    }

    /** Mints a scope token for the vendor core (audience: core-mock), signed with the shared core secret. */
    public String issueCoreScopeToken(Long applicationId, String applicationReference, String kycTier) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(applicationId))
                .issuer(props.getIssuer())
                .audience("meridian-core-mock")
                .claim("scope", "account.open")
                .claim("app_ref", applicationReference)
                .claim("kyc_tier", kycTier)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();
        try {
            SecretKey key = new SecretKeySpec(
                    coreTokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign scope token", e);
        }
    }

    private String sign(JWTClaimsSet claims) {
        try {
            SecretKey key = new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign JWT", e);
        }
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String randomToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    /** 6-digit OTP code generated with a cryptographically secure RNG. */
    public static String generateOtpCode() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
    }
}
