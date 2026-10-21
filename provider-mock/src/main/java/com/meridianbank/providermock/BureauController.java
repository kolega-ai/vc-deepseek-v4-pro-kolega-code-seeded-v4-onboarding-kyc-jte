package com.meridianbank.providermock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Local mock of the credit bureau: deterministic score and liabilities derived from the name. */
@RestController
public class BureauController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/v1/bureau/score", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> score(@RequestBody String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);
        String name = node.path("fullName").asText("");
        int score = 520 + Math.floorMod(name.hashCode(), 330); // deterministic 520..849

        List<Map<String, Object>> liabilities = new ArrayList<>();
        int n = Math.floorMod(name.hashCode(), 3) + 1;
        for (int i = 0; i < n; i++) {
            liabilities.add(Map.of(
                    "type", List.of("Credit card", "Personal loan", "Mortgage", "Auto loan").get(
                            Math.floorMod(name.hashCode() + i, 4)),
                    "amount", new BigDecimal(5000 + Math.floorMod(name.hashCode() * (i + 3), 95000)),
                    "currency", "SGD"));
        }
        return Map.of(
                "reportId", "BUR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "score", score,
                "pulledAt", Instant.now().toString(),
                "liabilities", liabilities);
    }
}
