package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByRegistrationTokenHash(String registrationTokenHash);

    Optional<UserAccount> findByStaffId(Long staffId);
}
