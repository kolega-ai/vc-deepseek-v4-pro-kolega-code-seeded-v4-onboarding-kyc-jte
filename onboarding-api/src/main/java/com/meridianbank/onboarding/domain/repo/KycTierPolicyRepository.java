package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.KycTierPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycTierPolicyRepository extends JpaRepository<KycTierPolicy, Long> {

    Optional<KycTierPolicy> findByTierCode(String tierCode);

    java.util.List<KycTierPolicy> findAllByOrderByTierCodeAsc();
}
