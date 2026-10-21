package com.meridianbank.onboarding.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Client for the vendor core banking stub (core-mock). core-mock behaves like an external system:
 * its own network endpoint, its own credential and its own error codes. It verifies the signed
 * scope token minted by this (decision) tier and opens accounts only for the application named in
 * that token — raw scope parameters from the request body are never trusted.
 */
public class CoreMockClient {

    public static final String HEADER_CORE_KEY = "X-Core-Key";
    public static final String HEADER_SCOPE_TOKEN = "X-Scope-Token";

    private final RestClient restClient;
    private final String coreKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoreMockClient(RestClient restClient, String coreKey) {
        this.restClient = restClient;
        this.coreKey = coreKey;
    }

    public record OpenedAccount(String accountNumber, String coreReference, String product, String kycTier) {
    }

    public OpenedAccount openAccount(String scopeToken, String applicationReference,
                                     String productCode, String currency, String kycTier) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "applicationReference", applicationReference,
                    "productCode", productCode,
                    "currency", currency,
                    "kycTier", kycTier));
            String response = restClient.post()
                    .uri("/v1/accounts")
                    .header(HEADER_CORE_KEY, coreKey)
                    .header(HEADER_SCOPE_TOKEN, scopeToken)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode node = objectMapper.readTree(response);
            return new OpenedAccount(
                    node.path("accountNumber").asText(),
                    node.path("coreReference").asText(),
                    node.path("product").asText(),
                    node.path("kycTier").asText());
        } catch (RestClientResponseException e) {
            String code = "CORE_UNKNOWN";
            try {
                JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
                code = node.path("errorCode").asText("CORE_UNKNOWN");
            } catch (Exception ignored) {
                // keep default code
            }
            throw ApiException.upstream("Core banking rejected the account opening (" + code + ")");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.upstream("Core banking is temporarily unavailable");
        }
    }
}
