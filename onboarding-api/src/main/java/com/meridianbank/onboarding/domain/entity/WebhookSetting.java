package com.meridianbank.onboarding.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Admin-managed outbound webhook configuration (single row). */
@Entity
@Table(name = "webhook_setting")
@Getter
@Setter
@NoArgsConstructor
public class WebhookSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Column(length = 200)
    private String secret;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
