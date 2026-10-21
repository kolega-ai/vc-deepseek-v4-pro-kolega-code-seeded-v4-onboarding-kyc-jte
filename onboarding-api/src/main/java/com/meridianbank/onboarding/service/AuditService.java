package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.AuditEvent;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.repo.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Append-only audit log. Every state transition and privileged action writes one event carrying
 * the actor, action, before/after values, timestamp and the authorization basis. There is no
 * update or delete path anywhere in this codebase, and a Postgres trigger enforces immutability
 * at the database level.
 */
@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void record(String actorType, String actor, Long actorId, String action,
                       String entityType, String entityId, String before, String after, String basis) {
        AuditEvent event = new AuditEvent();
        event.setActorType(actorType);
        event.setActor(actor);
        event.setActorId(actorId);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setBeforeValue(before);
        event.setAfterValue(after);
        event.setAuthorizationBasis(basis);
        event.setCreatedAt(Instant.now());
        auditEventRepository.save(event);
    }

    public void recordUser(UserAccount user, String action, String entityType, String entityId,
                           String before, String after, String basis) {
        record("USER", user.getEmail(), user.getId(), action, entityType, entityId, before, after, basis);
    }

    public void recordSystem(String component, String action, String entityType, String entityId,
                             String before, String after, String basis) {
        record("SYSTEM", component, null, action, entityType, entityId, before, after, basis);
    }

    /**
     * Read-auditing: a single event per request naming the resource class and the count returned,
     * never one event per record.
     */
    public void recordRead(UserAccount user, String action, String resourceClass, long count, String basis) {
        record("USER", user.getEmail(), user.getId(), action, resourceClass, "count=" + count,
                null, null, basis);
    }

    public void recordReadSystem(String component, String action, String resourceClass, long count, String basis) {
        record("SYSTEM", component, null, action, resourceClass, "count=" + count, null, null, basis);
    }
}
