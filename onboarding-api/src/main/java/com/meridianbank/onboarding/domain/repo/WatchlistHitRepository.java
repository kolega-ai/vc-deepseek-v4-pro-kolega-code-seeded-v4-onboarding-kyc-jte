package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.WatchlistHit;
import com.meridianbank.onboarding.domain.enums.HitDisposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchlistHitRepository extends JpaRepository<WatchlistHit, Long> {

    List<WatchlistHit> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    List<WatchlistHit> findByDispositionOrderByCreatedAtAsc(HitDisposition disposition);
}
