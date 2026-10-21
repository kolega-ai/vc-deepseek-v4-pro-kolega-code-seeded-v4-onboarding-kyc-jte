package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.StepUpGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface StepUpGrantRepository extends JpaRepository<StepUpGrant, Long> {

    Optional<StepUpGrant> findFirstByUserAccountIdAndPurposeAndSessionIdAndExpiresAtGreaterThanOrderByCreatedAtDesc(
            Long userAccountId, String purpose, String sessionId, Instant now);
}
