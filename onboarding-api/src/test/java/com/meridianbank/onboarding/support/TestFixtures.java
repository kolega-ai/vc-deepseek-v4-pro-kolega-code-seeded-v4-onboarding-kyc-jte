package com.meridianbank.onboarding.support;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.KycTierService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared fixtures for integration tests: users, applicants, applications, journey steps,
 * documents and officers are created directly; workflow RULES are always exercised through the
 * real services and state machine.
 */
@TestConfiguration
public class TestFixtures {

    public record Prospect(UserAccount user, Applicant applicant, Application application) {
    }

    @Bean
    public FixtureFactory fixtureFactory(UserAccountRepository users, ApplicantRepository applicants,
                                         ApplicationRepository applications, JourneyStepRepository steps,
                                         BranchRepository branches, StaffUserRepository staffUsers,
                                         DocumentRepository documents, PasswordEncoder encoder) {
        return new FixtureFactory(users, applicants, applications, steps, branches, staffUsers, documents, encoder);
    }

    public static class FixtureFactory {

        private static final AtomicLong SEQ = new AtomicLong(1000);

        private final UserAccountRepository users;
        private final ApplicantRepository applicants;
        private final ApplicationRepository applications;
        private final JourneyStepRepository steps;
        private final BranchRepository branches;
        private final StaffUserRepository staffUsers;
        private final DocumentRepository documents;
        private final PasswordEncoder encoder;

        public FixtureFactory(UserAccountRepository users, ApplicantRepository applicants,
                              ApplicationRepository applications, JourneyStepRepository steps,
                              BranchRepository branches, StaffUserRepository staffUsers,
                              DocumentRepository documents, PasswordEncoder encoder) {
            this.users = users;
            this.applicants = applicants;
            this.applications = applications;
            this.steps = steps;
            this.branches = branches;
            this.staffUsers = staffUsers;
            this.documents = documents;
            this.encoder = encoder;
        }

        public Branch branch(String code) {
            return branches.findByCode(code).orElseGet(() ->
                    branches.save(new Branch(code, code + " branch", "Test Region")));
        }

        public StaffUser officer(Branch branch, String maxTier) {
            StaffUser staff = new StaffUser();
            staff.setEmployeeId("OFF-" + SEQ.incrementAndGet());
            staff.setFullName("Officer " + staff.getEmployeeId());
            staff.setRole(Role.ONBOARDING_OFFICER);
            staff.setHomeBranch(branch);
            staff.setMaxApprovalTier(maxTier);
            staff.setStatus(UserStatus.ACTIVE);
            staff.setCreatedAt(Instant.now());
            staffUsers.save(staff);

            UserAccount account = new UserAccount();
            account.setEmail("officer" + staff.getEmployeeId().toLowerCase() + "@meridian.test");
            account.setPasswordHash(encoder.encode("Password#2026!"));
            account.setRole(Role.ONBOARDING_OFFICER);
            account.setStatus(UserStatus.ACTIVE);
            account.setStaffId(staff.getId());
            account.setCreatedAt(Instant.now());
            users.save(account);
            return staff;
        }

        public UserAccount complianceUser(Branch branch) {
            StaffUser staff = new StaffUser();
            staff.setEmployeeId("COM-" + SEQ.incrementAndGet());
            staff.setFullName("Compliance " + staff.getEmployeeId());
            staff.setRole(Role.COMPLIANCE_OFFICER);
            staff.setHomeBranch(branch);
            staff.setMaxApprovalTier("tier_0");
            staff.setStatus(UserStatus.ACTIVE);
            staff.setCreatedAt(Instant.now());
            staffUsers.save(staff);

            UserAccount account = new UserAccount();
            account.setEmail("compliance" + staff.getEmployeeId().toLowerCase() + "@meridian.test");
            account.setPasswordHash(encoder.encode("Password#2026!"));
            account.setRole(Role.COMPLIANCE_OFFICER);
            account.setStatus(UserStatus.ACTIVE);
            account.setStaffId(staff.getId());
            account.setCreatedAt(Instant.now());
            users.save(account);
            return account;
        }

        public UserAccount adminUser(Branch branch) {
            StaffUser staff = new StaffUser();
            staff.setEmployeeId("ADM-" + SEQ.incrementAndGet());
            staff.setFullName("Admin " + staff.getEmployeeId());
            staff.setRole(Role.BANK_ADMIN);
            staff.setHomeBranch(branch);
            staff.setMaxApprovalTier("tier_0");
            staff.setStatus(UserStatus.ACTIVE);
            staff.setCreatedAt(Instant.now());
            staffUsers.save(staff);

            UserAccount account = new UserAccount();
            account.setEmail("admin" + staff.getEmployeeId().toLowerCase() + "@meridian.test");
            account.setPasswordHash(encoder.encode("Password#2026!"));
            account.setRole(Role.BANK_ADMIN);
            account.setStatus(UserStatus.ACTIVE);
            account.setStaffId(staff.getId());
            account.setCreatedAt(Instant.now());
            users.save(account);
            return account;
        }

        public Prospect prospect(String email, Branch branch, String fullName, EmploymentStatus employment,
                                 String income, boolean contactVerified) {
            UserAccount user = new UserAccount();
            user.setEmail(email);
            user.setPasswordHash(encoder.encode("Password#2026!"));
            user.setRole(Role.PROSPECT);
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerifiedAt(Instant.now());
            if (contactVerified) {
                user.setMobileVerifiedAt(Instant.now());
            }
            user.setCreatedAt(Instant.now());
            users.save(user);

            Applicant applicant = new Applicant();
            applicant.setUserAccountId(user.getId());
            applicant.setEmail(email);
            applicant.setMobile("+65 81234567");
            applicant.setFullName(fullName);
            applicant.setDateOfBirth(LocalDate.of(1990, 1, 1));
            applicant.setNationalIdentifier("S90" + (1000000 + SEQ.incrementAndGet()) + "A");
            applicant.setAddressLine1("1 Test Street");
            applicant.setCity("Singapore");
            applicant.setPostalCode("123456");
            applicant.setCountry("Singapore");
            applicant.setEmploymentStatus(employment);
            applicant.setEmployerName("Test Employer");
            applicant.setDeclaredAnnualIncome(new BigDecimal(income));
            applicant.setCreatedAt(Instant.now());
            applicants.save(applicant);

            Application application = new Application();
            application.setReference("ONB-T-" + SEQ.incrementAndGet());
            application.setApplicantId(applicant.getId());
            application.setStatus(AppStatus.IN_PROGRESS);
            application.setCurrentStep(JourneyStepKey.CONTACT_VERIFIED);
            application.setBranchId(branch.getId());
            application.setDerivedKycTier(KycTierService.TIER_0);
            application.setCreatedAt(Instant.now());
            application.setLastActivityAt(Instant.now());
            applications.save(application);

            for (JourneyStepKey key : JourneyStepKey.values()) {
                JourneyStep step = new JourneyStep();
                step.setApplicationId(application.getId());
                step.setStepKey(key);
                step.setCompleted(key == JourneyStepKey.REGISTERED
                        || (key == JourneyStepKey.CONTACT_VERIFIED && contactVerified));
                if (key == JourneyStepKey.PERSONAL_DETAILS || key == JourneyStepKey.ADDRESS_DETAILS
                        || key == JourneyStepKey.EMPLOYMENT_INCOME) {
                    step.setCompleted(true);
                    step.setCompletedAt(Instant.now());
                }
                steps.save(step);
            }
            return new Prospect(user, applicant, application);
        }

        public Document document(Application application, UserAccount uploadedBy, DocType type, DocState state) {
            Document doc = new Document();
            doc.setApplicationId(application.getId());
            doc.setDocType(type);
            doc.setCategory(type.getCategory());
            doc.setOriginalFilename(type.name().toLowerCase() + ".pdf");
            doc.setStorageName("stored-" + SEQ.incrementAndGet() + ".pdf");
            doc.setContentType("application/pdf");
            doc.setSizeBytes(1024);
            doc.setUploadedBy(uploadedBy.getId());
            doc.setVerificationState(state);
            doc.setCreatedAt(Instant.now());
            doc.setExpiryDate(Instant.now().plus(365, java.time.temporal.ChronoUnit.DAYS));
            return documents.save(doc);
        }
    }
}
