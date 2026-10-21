package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.DocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, Long> {

    List<DocumentRequirement> findByKycTierOrderByDocTypeAsc(String kycTier);

    List<DocumentRequirement> findAllByOrderByKycTierAsc();
}
