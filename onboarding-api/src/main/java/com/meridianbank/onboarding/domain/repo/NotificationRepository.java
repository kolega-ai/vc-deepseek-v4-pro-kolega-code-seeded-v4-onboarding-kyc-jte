package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByUserAccountIdOrderByCreatedAtDesc(Long userAccountId);

    long countByUserAccountIdAndReadFalse(Long userAccountId);

    long deleteByUserAccountIdAndReadTrue(Long userAccountId);
}
