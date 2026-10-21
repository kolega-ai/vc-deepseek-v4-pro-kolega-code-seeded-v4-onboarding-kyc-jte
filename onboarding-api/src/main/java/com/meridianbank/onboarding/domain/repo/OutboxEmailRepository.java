package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.OutboxEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEmailRepository extends JpaRepository<OutboxEmail, Long> {

    List<OutboxEmail> findTop50ByRecipientOrderByCreatedAtDesc(String recipient);
}
