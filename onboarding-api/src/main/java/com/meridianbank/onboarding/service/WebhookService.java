package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.WebhookDelivery;
import com.meridianbank.onboarding.domain.entity.WebhookSetting;
import com.meridianbank.onboarding.domain.repo.WebhookDeliveryRepository;
import com.meridianbank.onboarding.domain.repo.WebhookSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Outbound webhooks for decision events, with HMAC-SHA256 signed payloads and a recorded delivery
 * log (attempts, response codes, errors). Fully local: any reachable endpoint can be configured
 * by an admin; no external service is required.
 */
@Service
public class WebhookService {

    private final WebhookSettingRepository settingRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final AppProperties props;

    public WebhookService(WebhookSettingRepository settingRepository,
                          WebhookDeliveryRepository deliveryRepository,
                          AppProperties props) {
        this.settingRepository = settingRepository;
        this.deliveryRepository = deliveryRepository;
        this.props = props;
    }

    /** Delivers an event if webhooks are enabled; records every attempt. */
    public void deliver(String eventType, String entityId, String payloadJson) {
        WebhookSetting setting = settingRepository.findById(1L).orElse(null);
        if (setting == null || !setting.isEnabled()
                || setting.getEndpointUrl() == null || setting.getEndpointUrl().isBlank()) {
            return;
        }
        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setEventType(eventType);
        delivery.setPayload("{\"event\":\"" + eventType + "\",\"entity\":\"" + entityId
                + "\",\"data\":" + payloadJson + "}");
        delivery.setAttemptedAt(Instant.now());
        try {
            RestClient client = RestClient.create(setting.getEndpointUrl());
            String body = delivery.getPayload();
            var response = client.post()
                    .header("X-Meridian-Event", eventType)
                    .header("X-Meridian-Signature", hmacSha256Hex(setting.getSecret(), body))
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            delivery.setSuccess(response.getStatusCode().is2xxSuccessful());
            delivery.setResponseCode(response.getStatusCode().value());
        } catch (Exception e) {
            delivery.setSuccess(false);
            delivery.setResponseCode(null);
            delivery.setError(truncate(String.valueOf(e.getMessage())));
        }
        deliveryRepository.save(delivery);
    }

    static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec((secret == null ? "" : secret).getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 400 ? value.substring(0, 400) : value;
    }
}
