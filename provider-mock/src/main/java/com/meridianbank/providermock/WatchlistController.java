package com.meridianbank.providermock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Local mock of watchlist screening: hits only for the configured names. */
@RestController
public class WatchlistController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WatchlistProperties properties;

    public WatchlistController(WatchlistProperties properties) {
        this.properties = properties;
    }

    @PostMapping(value = "/v1/watchlist/screen", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> screen(@RequestBody String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);
        String name = node.path("fullName").asText("").trim().toUpperCase();

        List<Map<String, Object>> matches = new ArrayList<>();
        for (WatchlistProperties.Entry entry : properties.getNames()) {
            if (entry.getName().equalsIgnoreCase(name)) {
                for (WatchlistProperties.Hit hit : entry.getHits()) {
                    matches.add(Map.of(
                            "matchedName", entry.getName(),
                            "listSource", hit.getSource(),
                            "confidence", new BigDecimal(hit.getConfidence())));
                }
            }
        }
        return Map.of(
                "reference", "WLS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "matches", matches);
    }

    @Component
    @ConfigurationProperties(prefix = "app.watchlist")
    public static class WatchlistProperties {
        private List<Entry> names = new ArrayList<>();

        public List<Entry> getNames() {
            return names;
        }

        public void setNames(List<Entry> names) {
            this.names = names;
        }

        public static class Entry {
            private String name;
            private List<Hit> hits = new ArrayList<>();

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<Hit> getHits() {
                return hits;
            }

            public void setHits(List<Hit> hits) {
                this.hits = hits;
            }
        }

        public static class Hit {
            private String source;
            private double confidence;

            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public double getConfidence() {
                return confidence;
            }

            public void setConfidence(double confidence) {
                this.confidence = confidence;
            }
        }
    }
}
