package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.WebhookDelivery;
import com.meridianbank.onboarding.domain.entity.WebhookSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookSettingRepository extends JpaRepository<WebhookSetting, Long> {
}
