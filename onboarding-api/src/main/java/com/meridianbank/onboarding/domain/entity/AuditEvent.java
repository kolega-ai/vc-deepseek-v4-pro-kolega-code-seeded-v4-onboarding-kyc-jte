package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Append-only audit event. Written for every state transition and privileged action, carrying the
 * actor, action, before/after values, timestamp and the authorization basis. No role may modify or
 * delete these rows (enforced in code and by a Postgres trigger).
 */
@Entity
@Table(name = "audit_event")
@Getter
@Setter
@NoArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** USER or SYSTEM. */
    @Column(name = "actor_type", nullable = false, length = 16)
    private String actorType;

    /** Email of the acting user, or the system component name. */
    @Column(nullable = false, length = 160)
    private String actor;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id", length = 60)
    private String entityId;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "authorization_basis", length = 120)
    private String authorizationBasis;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
