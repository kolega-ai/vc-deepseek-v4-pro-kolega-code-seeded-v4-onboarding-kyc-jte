package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.OutboxEmail;
import com.meridianbank.onboarding.domain.repo.OutboxEmailRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Local mock of the outbound email gateway. Messages are written to the outbox table and a
 * scheduled job marks them delivered — no external mail provider is required.
 */
@Service
public class OutboxEmailService {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";

    private final OutboxEmailRepository outboxEmailRepository;

    public OutboxEmailService(OutboxEmailRepository outboxEmailRepository) {
        this.outboxEmailRepository = outboxEmailRepository;
    }

    public void send(String recipient, String subject, String body, String purpose) {
        OutboxEmail email = new OutboxEmail();
        email.setRecipient(recipient);
        email.setSubject(subject);
        email.setBody(body);
        email.setPurpose(purpose);
        email.setStatus(STATUS_PENDING);
        email.setCreatedAt(Instant.now());
        outboxEmailRepository.save(email);
    }

    public List<OutboxEmail> inboxFor(String recipient) {
        return outboxEmailRepository.findTop50ByRecipientOrderByCreatedAtDesc(recipient);
    }
}
