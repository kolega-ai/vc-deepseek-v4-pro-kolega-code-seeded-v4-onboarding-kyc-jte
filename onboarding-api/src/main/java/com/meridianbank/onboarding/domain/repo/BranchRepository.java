package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByCode(String code);

    List<Branch> findAllByOrderByNameAsc();
}
