package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.IdentityVerificationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityVerificationResultRepository extends JpaRepository<IdentityVerificationResult, Long> {

    Optional<IdentityVerificationResult> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    List<IdentityVerificationResult> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}
