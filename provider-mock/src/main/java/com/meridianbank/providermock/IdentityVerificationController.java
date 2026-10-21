package com.meridianbank.providermock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local mock of the external identity verification provider. Accepts and returns XML. The response
 * carries a DOCTYPE declaration with an internal entity, as legacy vendor systems commonly emit —
 * the consuming application's provider-response ingest must therefore parse it on every call.
 */
@RestController
public class IdentityVerificationController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Boolean> failedOnce = new ConcurrentHashMap<>();

    @PostMapping(value = "/v1/identity/verify", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> verify(@RequestBody String requestXml) {
        String fullName = extract(requestXml, "fullName");
        String upper = fullName == null ? "" : fullName.toUpperCase();

        // Transient provider outage: the FIRST call for a name containing "FAIL" returns 502;
        // a retry succeeds. Exercises the application's provider-failure/retry path.
        if (upper.contains("FAIL") && !failedOnce.getOrDefault(upper, false)) {
            failedOnce.put(upper, true);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<?xml version=\"1.0\"?><error><code>PROVIDER-5011</code></error>");
        }

        int score;
        String outcome;
        if (upper.contains("MISMATCH")) {
            score = 42;
            outcome = "NO_MATCH";
        } else if (upper.contains("LOWSCORE")) {
            score = 71;
            outcome = "REVIEW";
        } else {
            score = 84 + Math.floorMod(fullName.hashCode(), 15); // deterministic 84..98
            outcome = "MATCH";
        }

        String reference = "SCR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE identity-verification-response [\n"
                + "  <!ENTITY providerName \"ScreenSure Identity Services\">\n"
                + "]>\n"
                + "<identity-verification-response>\n"
                + "  <providerReference>" + reference + "</providerReference>\n"
                + "  <providerName>&providerName;</providerName>\n"
                + "  <matchScore>" + score + "</matchScore>\n"
                + "  <outcome>" + outcome + "</outcome>\n"
                + "  <verificationTime>" + Instant.now() + "</verificationTime>\n"
                + "</identity-verification-response>";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(body);
    }

    private String extract(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);
        if (start < 0 || end < 0) {
            return "";
        }
        return xml.substring(start + open.length(), end).trim();
    }
}
