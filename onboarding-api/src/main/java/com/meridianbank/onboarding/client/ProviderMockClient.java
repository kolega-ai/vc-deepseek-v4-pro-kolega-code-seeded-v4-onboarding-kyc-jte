package com.meridianbank.onboarding.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Client for the local provider mock: identity verification (XML), credit bureau (JSON) and
 * watchlist screening (JSON) behind a single provider credential.
 */
public class ProviderMockClient {

    public static final String HEADER_API_KEY = "X-Provider-Key";

    private final RestClient restClient;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProviderMockClient(RestClient restClient, String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    /** Identity verification accepts and returns XML. */
    public String verifyIdentityXml(String requestXml) {
        try {
            return restClient.post()
                    .uri("/v1/identity/verify")
                    .header(HEADER_API_KEY, apiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                    .body(requestXml)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw ApiException.upstream("Identity provider is temporarily unavailable (" + e.getStatusCode() + ")");
        } catch (Exception e) {
            throw ApiException.upstream("Identity provider is temporarily unavailable");
        }
    }

    public record BureauScore(int score, java.util.List<Liability> liabilities, String reportId) {
    }

    public record Liability(String type, java.math.BigDecimal amount, String currency) {
    }

    /** Credit bureau pull (JSON). */
    public BureauScore pullBureau(String nationalId, String fullName, String dateOfBirth) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "nationalId", nationalId == null ? "" : nationalId,
                    "fullName", fullName == null ? "" : fullName,
                    "dateOfBirth", dateOfBirth == null ? "" : dateOfBirth));
            JsonNode node = objectMapper.readTree(restClient.post()
                    .uri("/v1/bureau/score")
                    .header(HEADER_API_KEY, apiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class));
            java.util.List<Liability> liabilities = new java.util.ArrayList<>();
            if (node.has("liabilities")) {
                for (JsonNode li : node.get("liabilities")) {
                    liabilities.add(new Liability(
                            li.path("type").asText(),
                            new java.math.BigDecimal(li.path("amount").asText()),
                            li.path("currency").asText()));
                }
            }
            return new BureauScore(node.path("score").asInt(), liabilities, node.path("reportId").asText());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.upstream("Credit bureau is temporarily unavailable");
        }
    }

    public record WatchlistMatch(String matchedName, String listSource, java.math.BigDecimal confidence) {
    }

    public record WatchlistResult(String reference, java.util.List<WatchlistMatch> matches) {
    }

    /** Watchlist screening (JSON). */
    public WatchlistResult screenWatchlist(String fullName, String dateOfBirth, String nationality) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "fullName", fullName == null ? "" : fullName,
                    "dateOfBirth", dateOfBirth == null ? "" : dateOfBirth,
                    "nationality", nationality == null ? "" : nationality));
            JsonNode node = objectMapper.readTree(restClient.post()
                    .uri("/v1/watchlist/screen")
                    .header(HEADER_API_KEY, apiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class));
            java.util.List<WatchlistMatch> matches = new java.util.ArrayList<>();
            if (node.has("matches")) {
                for (JsonNode m : node.get("matches")) {
                    matches.add(new WatchlistMatch(
                            m.path("matchedName").asText(),
                            m.path("listSource").asText(),
                            new java.math.BigDecimal(m.path("confidence").asText())));
                }
            }
            return new WatchlistResult(node.path("reference").asText(), matches);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.upstream("Watchlist screening service is temporarily unavailable");
        }
    }
}
