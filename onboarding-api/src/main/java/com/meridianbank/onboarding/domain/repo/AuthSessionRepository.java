package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
}
