package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findTop100ByOrderByAttemptedAtDesc();
}
