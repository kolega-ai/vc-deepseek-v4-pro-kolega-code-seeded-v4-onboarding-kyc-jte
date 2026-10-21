package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.BureauResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BureauResultRepository extends JpaRepository<BureauResult, Long> {

    Optional<BureauResult> findTopByApplicationIdOrderByPulledAtDesc(Long applicationId);
}
