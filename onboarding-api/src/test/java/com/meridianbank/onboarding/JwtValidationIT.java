package com.meridianbank.onboarding;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.meridianbank.onboarding.domain.entity.AuthSession;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.enums.UserStatus;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JWT acceptance criteria: tokens with an absent or unexpected algorithm, a wrong audience, a
 * wrong issuer or an expired lifetime are rejected by the resource server on every request.
 */
class JwtValidationIT extends IntegrationTestBase {

    private static final String SECRET = "test-secret-0123456789abcdef0123456789abcdef";
    private static final String ISSUER = "meridian-onboarding-api";

    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserAccount user;
    private String sid;

    @BeforeEach
    void setUp() {
        user = new UserAccount();
        user.setEmail("jwt-test@meridian.test");
        user.setPasswordHash(passwordEncoder.encode("Password#2026!"));
        user.setRole(Role.PROSPECT);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        userAccountRepository.save(user);
        sid = UUID.randomUUID().toString();
        AuthSession session = new AuthSession();
        session.setId(sid);
        session.setUserAccountId(user.getId());
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(Instant.now().plusSeconds(3600));
        authSessionRepository.save(session);
    }

    private String unsignedToken(JWTClaimsSet claims) {
        return new com.nimbusds.jwt.PlainJWT(claims).serialize();
    }

    private String hs256Token(JWTClaimsSet claims, String secret) throws JOSEException {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(key));
        return jwt.serialize();
    }

    private String rs256Token(JWTClaimsSet claims) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(pair.getPrivate()));
        return jwt.serialize();
    }

    private JWTClaimsSet.Builder claims() {
        return new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer(ISSUER)
                .audience("meridian-channel")
                .claim("sid", sid)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(600)));
    }

    private void expect401(String token) throws Exception {
        mockMvc.perform(get("/api/applications/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithNoneAlgorithmIsRejected() throws Exception {
        expect401(unsignedToken(claims().build()));
    }

    @Test
    void tokenSignedWithUnexpectedAlgorithmIsRejected() throws Exception {
        expect401(rs256Token(claims().build()));
    }

    @Test
    void tokenWithHs512InsteadOfHs256IsRejected() throws Exception {
        // Hand-built HS512 token: the decoder is restricted to HS256 and must reject it.
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS512\"}".getBytes(StandardCharsets.UTF_8));
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(claims().build().toJSONObject().toString().getBytes(StandardCharsets.UTF_8));
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        String signature = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8)));
        expect401(header + "." + payload + "." + signature);
    }

    @Test
    void tokenWithWrongAudienceIsRejected() throws Exception {
        expect401(hs256Token(claims().audience("some-other-system").build(), SECRET));
    }

    @Test
    void tokenWithWrongIssuerIsRejected() throws Exception {
        expect401(hs256Token(claims().issuer("evil-issuer").build(), SECRET));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        JWTClaimsSet expired = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer(ISSUER)
                .audience("meridian-channel")
                .claim("sid", sid)
                .issueTime(Date.from(Instant.now().minusSeconds(7200)))
                .expirationTime(Date.from(Instant.now().minusSeconds(3600)))
                .build();
        expect401(hs256Token(expired, SECRET));
    }

    @Test
    void tokenSignedWithWrongSecretIsRejected() throws Exception {
        expect401(hs256Token(claims().build(), "another-secret-0123456789abcdef0123456789abcdef"));
    }

    @Test
    void tokenForRevokedSessionIsRejected() throws Exception {
        authSessionRepository.findById(sid).ifPresent(session -> {
            session.setRevokedAt(Instant.now());
            authSessionRepository.save(session);
        });
        expect401(hs256Token(claims().build(), SECRET));
    }

    @Test
    void validHs256TokenIsAccepted() throws Exception {
        mockMvc.perform(get("/api/reference/branches")
                        .header("Authorization", "Bearer " + hs256Token(claims().build(), SECRET)))
                .andExpect(status().isOk());
    }
}
