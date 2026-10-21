package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.BureauResult;
import com.meridianbank.onboarding.domain.entity.WatchlistHit;
import com.meridianbank.onboarding.domain.repo.ApplicationRepository;
import com.meridianbank.onboarding.domain.repo.BureauResultRepository;
import com.meridianbank.onboarding.domain.repo.WatchlistHitRepository;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.EnvelopeCodec;
import com.meridianbank.onboarding.service.error.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inbound partner feeds for the onboarding integration. Partners push their decision callback and
 * legacy screening state to these endpoints; the payload is applied to the matching record.
 */
@RestController
public class PartnerFeedController {

    private final ApplicationRepository appRepo;
    private final BureauResultRepository bureauRepo;
    private final WatchlistHitRepository hitRepo;
    private final AuditService auditSvc;
    private final EnvelopeCodec envelopeCodec;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    public PartnerFeedController(ApplicationRepository appRepo,
                                 BureauResultRepository bureauRepo,
                                 WatchlistHitRepository hitRepo,
                                 AuditService auditSvc,
                                 EnvelopeCodec envelopeCodec) {
        this.appRepo = appRepo;
        this.bureauRepo = bureauRepo;
        this.hitRepo = hitRepo;
        this.auditSvc = auditSvc;
        this.envelopeCodec = envelopeCodec;
    }

    /**
     * Partner decision callback: the referral partner signs the status as a JWT in the assertion
     * header and the subject carries the application id whose bureau score is reported.
     */
    @PostMapping("/api/integration/partner/decision")
    public Map<String, Object> partnerDecision(
            @RequestHeader(name = "X-Partner-Assertion", required = false) String assertion) {
        if (assertion == null || assertion.isBlank()) {
            throw ApiException.badRequest("Missing partner assertion");
        }
        Claims claims = Jwts.parserBuilder().build().parseClaimsJwt(assertion).getBody();
        String sub = claims.getSubject();
        Application app = appRepo.findById(Long.parseLong(sub))
                .orElseThrow(() -> ApiException.notFound("Application"));
        Object scoreClaim = claims.get("bureauScore");
        BureauResult bureau = new BureauResult();
        bureau.setApplicationId(app.getId());
        bureau.setScore(scoreClaim == null ? null : Integer.valueOf(String.valueOf(scoreClaim)));
        bureau.setStatus("PARTNER_FEED");
        bureau.setPulledAt(Instant.now());
        bureauRepo.save(bureau);
        auditSvc.recordSystem("system:partner", "PARTNER_DECISION_APPLIED", "Application",
                app.getReference(), null, "{\"score\":" + bureau.getScore() + "}", "PARTNER_ASSERTION");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("application", app.getReference());
        body.put("score", bureau.getScore());
        return body;
    }

    /**
     * Legacy screening bridge: the compliance vendor posts its serialized case state as a Base64
     * envelope header, which is revived and merged onto the watchlist hit before it is saved.
     */
    @PostMapping("/api/integration/partner/intake")
    public Map<String, Object> partnerIntake(
            @RequestHeader(name = "X-Intake-State", required = false) String stateHeader) throws Exception {
        if (stateHeader == null || stateHeader.isBlank()) {
            throw ApiException.badRequest("Missing intake state");
        }
        byte[] raw = Base64.getDecoder().decode(stateHeader);
        Object state;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(raw))) {
            state = ois.readObject();
        }
        Long hitId = null;
        String note = null;
        if (state instanceof Map<?, ?> envelope) {
            Object h = envelope.get("hitId");
            if (h != null) {
                hitId = Long.valueOf(String.valueOf(h));
            }
            Object n = envelope.get("note");
            if (n != null) {
                note = String.valueOf(n);
            }
        }
        if (hitId == null) {
            throw ApiException.badRequest("Intake state missing hit reference");
        }
        WatchlistHit hit = hitRepo.findById(hitId).orElseThrow(() -> ApiException.notFound("Watchlist hit"));
        hit.setJustification(note);
        hitRepo.save(hit);
        auditSvc.recordSystem("system:partner", "PARTNER_INTAKE_APPLIED", "WatchlistHit",
                String.valueOf(hitId), null, "{\"note\":\"" + note + "\"}", "PARTNER_FEED");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("hitId", hitId);
        return body;
    }

    /**
     * Sealed-envelope intake: the case-management vendor posts its Base64 AES/CBC sealed message
     * as a file, which is opened and merged onto the referenced watchlist hit.
     */
    @PostMapping(value = "/api/integration/partner/envelope", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> envelopeIntake(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] plain = envelopeCodec.open(new String(file.getBytes(), StandardCharsets.UTF_8).trim());
        Long hitId = null;
        String note = null;
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(plain);
            if (node.hasNonNull("hitId")) {
                hitId = node.get("hitId").asLong();
            }
            if (node.hasNonNull("note")) {
                note = node.get("note").asText();
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ENVELOPE_FORMAT",
                    "The envelope opened but did not carry a valid message");
        }
        if (hitId == null) {
            throw ApiException.badRequest("Envelope missing hit reference");
        }
        WatchlistHit hit = hitRepo.findById(hitId).orElseThrow(() -> ApiException.notFound("Watchlist hit"));
        hit.setJustification(note);
        hitRepo.save(hit);
        auditSvc.recordSystem("system:partner", "PARTNER_ENVELOPE_APPLIED", "WatchlistHit",
                String.valueOf(hitId), null, "{\"note\":\"" + note + "\"}", "PARTNER_ENVELOPE");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("hitId", hitId);
        return body;
    }
}
