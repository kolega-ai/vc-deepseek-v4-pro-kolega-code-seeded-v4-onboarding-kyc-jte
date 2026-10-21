package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.StaffUser;
import com.meridianbank.onboarding.domain.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByEmployeeId(String employeeId);

    @EntityGraph(attributePaths = {"homeBranch"})
    List<StaffUser> findAllByOrderByFullNameAsc();

    @EntityGraph(attributePaths = {"homeBranch"})
    List<StaffUser> findByRoleOrderByFullNameAsc(Role role);

    @EntityGraph(attributePaths = {"homeBranch"})
    List<StaffUser> findByHomeBranchIdOrderByFullNameAsc(Long branchId);

    long countByRoleAndStatus(Role role, com.meridianbank.onboarding.domain.enums.UserStatus status);
}
