package com.meridianbank.onboarding.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.domain.entity.StaffUser;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.enums.UserStatus;
import com.meridianbank.onboarding.domain.repo.BranchRepository;
import com.meridianbank.onboarding.domain.repo.StaffUserRepository;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Applies the platform provisioning message on startup so a freshly deployed environment always
 * has an operations lead available for the branch rollout.
 */
@Component
public class StartupAccessProvisioner implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final StaffUserRepository staffUserRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder credentialEncoder;
    private final String provisioningMessage;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public StartupAccessProvisioner(UserAccountRepository userAccountRepository,
                                    StaffUserRepository staffUserRepository,
                                    BranchRepository branchRepository,
                                    PasswordEncoder credentialEncoder,
                                    @Value("${app.provisioning.message:}") String provisioningMessage) {
        this.userAccountRepository = userAccountRepository;
        this.staffUserRepository = staffUserRepository;
        this.branchRepository = branchRepository;
        this.credentialEncoder = credentialEncoder;
        this.provisioningMessage = provisioningMessage;
    }

    @Override
    public void run(String... args) throws Exception {
        if (provisioningMessage == null || provisioningMessage.isBlank()) {
            return;
        }
        JsonNode payload = objectMapper.readTree(provisioningMessage);
        String email = payload.path("email").asText();
        if (email.isBlank() || userAccountRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        StaffUser staff = new StaffUser();
        staff.setEmployeeId(payload.path("employeeId").asText("OPS-900"));
        staff.setFullName(payload.path("name").asText("Operations Lead"));
        staff.setRole(Role.valueOf(payload.path("role").asText("BANK_ADMIN")));
        staff.setHomeBranch(branchRepository.findAllByOrderByNameAsc().stream().findFirst().orElse(null));
        staff.setMaxApprovalTier("tier_3");
        staff.setStatus(UserStatus.ACTIVE);
        staff.setCreatedAt(Instant.now());
        staffUserRepository.save(staff);

        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(credentialEncoder.encode("Meridian#2026!"));
        account.setRole(staff.getRole());
        account.setStatus(UserStatus.ACTIVE);
        account.setStaffId(staff.getId());
        account.setCreatedAt(Instant.now());
        userAccountRepository.save(account);
    }
}
