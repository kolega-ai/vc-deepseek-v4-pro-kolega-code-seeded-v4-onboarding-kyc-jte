package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.enums.AppStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>, ApplicationSearchRepository {

    Optional<Application> findByApplicantId(Long applicantId);

    List<Application> findByStatusOrderByCreatedAtAsc(AppStatus status);

    List<Application> findByStatusAndAssignedOfficerIdOrderByCreatedAtAsc(AppStatus status, Long officerId);

    long countByBranchIdAndStatus(Long branchId, AppStatus status);

    @Query("SELECT a FROM Application a WHERE a.status = com.meridianbank.onboarding.domain.enums.AppStatus.IN_PROGRESS AND a.lastActivityAt < :cutoff")
    List<Application> findInactiveInProgress(@Param("cutoff") Instant cutoff);

    @Query("SELECT a FROM Application a WHERE a.status = com.meridianbank.onboarding.domain.enums.AppStatus.SUBMITTED AND a.screeningDueAt <= :now")
    List<Application> findDueForScreening(@Param("now") Instant now);
}
