package com.meridianbank.coremock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vendor core banking stub: opens accounts for approved applications. Behaves like an external
 * system — its own endpoint, its own static credential, its own error codes, and a signed scope
 * token minted by the decision tier that it verifies on every call. The account's kyc_tier is
 * taken from the verified token only; tier or scope values from the request body are ignored.
 */
@RestController
@RequestMapping("/v1")
public class AccountsController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtDecoder scopeTokenDecoder;
    private final String coreKey;
    private final Map<String, OpenedAccount> accountsByApplication = new ConcurrentHashMap<>();
    private final Map<String, OpenedAccount> accountsByNumber = new ConcurrentHashMap<>();
    private long sequence = 1000000000L;

    public AccountsController(JwtDecoder scopeTokenDecoder,
                              @Value("${app.core.api-key}") String coreKey) {
        this.scopeTokenDecoder = scopeTokenDecoder;
        this.coreKey = coreKey;
    }

    @PostMapping(value = "/accounts", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> openAccount(
            @RequestHeader(value = "X-Core-Key", required = false) String providedKey,
            @RequestHeader(value = "X-Scope-Token", required = false) String scopeToken,
            @RequestBody(required = false) String body) {
        if (providedKey == null || !providedKey.equals(coreKey)) {
            return error(HttpStatus.UNAUTHORIZED, "CORE-1001", "invalid core credential");
        }
        Jwt token;
        try {
            token = scopeTokenDecoder.decode(scopeToken);
        } catch (JwtException e) {
            return error(HttpStatus.UNAUTHORIZED, "CORE-1002", "invalid or expired scope token");
        }
        String scope = token.getClaimAsString("scope");
        if (!"account.open".equals(scope)) {
            return error(HttpStatus.FORBIDDEN, "CORE-1002", "scope token does not permit account opening");
        }
        String tokenAppRef = token.getClaimAsString("app_ref");
        String tier = token.getClaimAsString("kyc_tier");
        if (tokenAppRef == null || tier == null) {
            return error(HttpStatus.BAD_REQUEST, "CORE-1003", "scope token missing application reference or tier");
        }
        // The tier in the body is deliberately ignored: only the token-scoped tier is honoured.
        String bodyAppRef = "";
        try {
            JsonNode node = objectMapper.readTree(body == null ? "{}" : body);
            bodyAppRef = node.path("applicationReference").asText("");
        } catch (Exception ignored) {
            // unparseable body -> mismatch below
        }
        if (!tokenAppRef.equals(bodyAppRef)) {
            return error(HttpStatus.BAD_REQUEST, "CORE-1003", "application reference does not match scope token");
        }
        if (accountsByApplication.containsKey(tokenAppRef)) {
            return error(HttpStatus.CONFLICT, "CORE-1004", "an account is already opened for this application");
        }

        String accountNumber = "910" + (sequence++);
        String coreReference = "CR-" + tokenAppRef + "-" + accountNumber.substring(accountNumber.length() - 4);
        OpenedAccount account = new OpenedAccount(accountNumber, coreReference, "Everyday Savings Account", tier,
                Instant.now());
        accountsByApplication.put(tokenAppRef, account);
        accountsByNumber.put(accountNumber, account);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "accountNumber", account.accountNumber(),
                "coreReference", account.coreReference(),
                "product", account.product(),
                "kycTier", account.kycTier(),
                "openedAt", account.openedAt().toString()));
    }

    @GetMapping(value = "/accounts/{accountNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAccount(
            @RequestHeader(value = "X-Core-Key", required = false) String providedKey,
            @PathVariable String accountNumber) {
        if (providedKey == null || !providedKey.equals(coreKey)) {
            return error(HttpStatus.UNAUTHORIZED, "CORE-1001", "invalid core credential");
        }
        OpenedAccount account = accountsByNumber.get(accountNumber);
        if (account == null) {
            return error(HttpStatus.NOT_FOUND, "CORE-1005", "account not found");
        }
        return ResponseEntity.ok(Map.of(
                "accountNumber", account.accountNumber(),
                "coreReference", account.coreReference(),
                "product", account.product(),
                "kycTier", account.kycTier(),
                "openedAt", account.openedAt().toString()));
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("errorCode", code, "message", message));
    }

    private record OpenedAccount(String accountNumber, String coreReference, String product,
                                 String kycTier, Instant openedAt) {
    }
}
