package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.ReKycCase;
import com.meridianbank.onboarding.domain.enums.ReKycStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReKycCaseRepository extends JpaRepository<ReKycCase, Long> {

    List<ReKycCase> findByCustomerUserIdOrderByCreatedAtDesc(Long customerUserId);

    List<ReKycCase> findByCampaignIdOrderByCreatedAtAsc(Long campaignId);

    List<ReKycCase> findByStatusOrderByDueDateAsc(ReKycStatus status);

    Optional<ReKycCase> findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(Long customerUserId, List<ReKycStatus> statuses);

    Optional<ReKycCase> findByApplicationIdAndStatusIn(Long applicationId, List<ReKycStatus> statuses);
}
