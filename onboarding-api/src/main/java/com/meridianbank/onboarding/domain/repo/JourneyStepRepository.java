package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.JourneyStep;
import com.meridianbank.onboarding.domain.enums.JourneyStepKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JourneyStepRepository extends JpaRepository<JourneyStep, Long> {

    List<JourneyStep> findByApplicationIdOrderByStepKeyAsc(Long applicationId);

    Optional<JourneyStep> findByApplicationIdAndStepKey(Long applicationId, JourneyStepKey stepKey);
}
