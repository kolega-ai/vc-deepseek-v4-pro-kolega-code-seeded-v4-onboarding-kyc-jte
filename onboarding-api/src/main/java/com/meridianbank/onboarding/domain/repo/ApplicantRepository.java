package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    Optional<Applicant> findByUserAccountId(Long userAccountId);

    Optional<Applicant> findByNationalIdentifier(String nationalIdentifier);

    Optional<Applicant> findByEmailIgnoreCase(String email);
}
