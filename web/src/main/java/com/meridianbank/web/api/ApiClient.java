package com.meridianbank.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Channel-tier client for the decision API. Every call carries the session JWT as a bearer token;
 * the decision tier validates it and authorizes the request against its own database. Error
 * responses are surfaced as {@link ApiClientException} with a user-displayable message.
 */
@Service
public class ApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ApiClient(@Value("${app.api-base-url}") String baseUrl, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    public JsonNode get(String path, String token) {
        return execute(() -> restClient.get().uri(path).headers(h -> { if (bearer(token) != null) { h.set("Authorization", bearer(token)); } })
                .retrieve().body(String.class));
    }

    public JsonNode post(String path, String token, Object body) {
        return execute(() -> restClient.post().uri(path)
                .headers(h -> { if (bearer(token) != null) { h.set("Authorization", bearer(token)); } })
                .contentType(MediaType.APPLICATION_JSON)
                .body(body == null ? Map.of() : body)
                .retrieve().body(String.class));
    }

    public JsonNode put(String path, String token, Object body) {
        return execute(() -> restClient.put().uri(path)
                .headers(h -> { if (bearer(token) != null) { h.set("Authorization", bearer(token)); } })
                .contentType(MediaType.APPLICATION_JSON)
                .body(body == null ? Map.of() : body)
                .retrieve().body(String.class));
    }

    public JsonNode delete(String path, String token) {
        return execute(() -> restClient.delete().uri(path)
                .headers(h -> { if (bearer(token) != null) { h.set("Authorization", bearer(token)); } })
                .retrieve().body(String.class));
    }

    public JsonNode postMultipart(String path, String token, String fileParam, String filename,
                                  String contentType, byte[] content, Map<String, String> fields) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add(fileParam, new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        fields.forEach(parts::add);
        return execute(() -> restClient.post().uri(path)
                .headers(h -> { if (bearer(token) != null) { h.set("Authorization", bearer(token)); } })
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve().body(String.class));
    }

    public byte[] getBytes(String path, String token) {
        try {
            return restClient.get().uri(path)
                    .headers(h -> { if (bearer(token) != null) { h.set("Authorization", bearer(token)); } })
                    .retrieve().body(byte[].class);
        } catch (RestClientResponseException e) {
            throw toApiException(e);
        }
    }

    private JsonNode execute(java.util.function.Supplier<String> call) {
        try {
            String body = call.get();
            return objectMapper.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (RestClientResponseException e) {
            throw toApiException(e);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiClientException(502, "The banking service is temporarily unavailable");
        }
    }

    private ApiClientException toApiException(RestClientResponseException e) {
        String message = "The request could not be completed";
        try {
            JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
            if (node.has("message") && !node.get("message").isNull()) {
                message = node.get("message").asText();
            }
        } catch (Exception ignored) {
            // fall back to generic message
        }
        return new ApiClientException(e.getStatusCode().value(), message);
    }

    private String bearer(String token) {
        return token == null || token.isBlank() ? null : "Bearer " + token;
    }

    public static class ApiClientException extends RuntimeException {
        private final int status;

        public ApiClientException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
