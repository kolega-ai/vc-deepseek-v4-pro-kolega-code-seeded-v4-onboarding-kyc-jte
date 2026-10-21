package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByApplicationId(Long applicationId);

    Optional<Account> findByAccountNumber(String accountNumber);
}
