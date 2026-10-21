package com.meridianbank.onboarding;

import com.meridianbank.onboarding.domain.entity.Account;
import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.Branch;
import com.meridianbank.onboarding.domain.entity.Document;
import com.meridianbank.onboarding.domain.entity.ReKycCase;
import com.meridianbank.onboarding.domain.entity.StaffUser;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.jobs.DocumentLifecycleJobs;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import com.meridianbank.onboarding.support.TestFixtures.Prospect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Scheduled jobs: abandonment after 30 days, document expiry cascade and re-KYC due-dating. */
class ScheduledJobsIT extends IntegrationTestBase {

    @Autowired
    private DocumentLifecycleJobs jobs;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ReKycCaseRepository reKycCaseRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private AuditEventRepository auditEventRepository;

    private Branch branch;
    private UserAccount officerUser;

    @BeforeEach
    void setUp() {
        branch = fixtures.branch("MB-CEN-01");
        StaffUser officer = fixtures.officer(branch, "tier_3");
        officerUser = userAccountRepository.findByStaffId(officer.getId()).orElseThrow();
    }

    @Test
    void inactiveApplicationIsAbandoned() {
        Prospect prospect = fixtures.prospect("abandon@meridian.test", branch, "Abandon Me",
                EmploymentStatus.EMPLOYED, "50000", true);
        prospect.application().setLastActivityAt(Instant.now().minus(40, ChronoUnit.DAYS));
        applicationRepository.save(prospect.application());
        jobs.runAbandonment();
        Application reloaded = applicationRepository.findById(prospect.application().getId()).orElseThrow();
        assertEquals(AppStatus.ABANDONED, reloaded.getStatus());
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(e -> "APPLICATION_ABANDONED".equals(e.getAction())));
    }

    @Test
    void activeApplicationIsNotAbandoned() {
        Prospect prospect = fixtures.prospect("notabandon@meridian.test", branch, "Stay Alive",
                EmploymentStatus.EMPLOYED, "50000", true);
        jobs.runAbandonment();
        Application reloaded = applicationRepository.findById(prospect.application().getId()).orElseThrow();
        assertEquals(AppStatus.IN_PROGRESS, reloaded.getStatus());
    }

    @Test
    void expiredDocumentOnPreApprovalApplicationRoutesToDocumentsRequested() {
        Prospect prospect = fixtures.prospect("preapproval-expiry@meridian.test", branch, "Pre Expiry",
                EmploymentStatus.EMPLOYED, "50000", true);
        prospect.application().setStatus(AppStatus.UNDER_REVIEW);
        applicationRepository.save(prospect.application());
        Document doc = fixtures.document(prospect.application(), prospect.user(),
                DocType.NATIONAL_ID, DocState.VERIFIED);
        doc.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        documentRepository.save(doc);
        jobs.processExpiredDocuments();
        Application reloaded = applicationRepository.findById(prospect.application().getId()).orElseThrow();
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, reloaded.getStatus());
        assertEquals(DocState.EXPIRED, documentRepository.findById(doc.getId()).orElseThrow().getVerificationState());
        assertTrue(reloaded.getRequestedDocumentTypes().contains("NATIONAL_ID"));
    }

    @Test
    void expiredDocumentOnApprovedApplicationOpensRekycCaseAndSuspendsAccount() {
        Prospect prospect = fixtures.prospect("approved-expiry@meridian.test", branch, "Approved Expiry",
                EmploymentStatus.EMPLOYED, "50000", true);
        prospect.user().setRole(Role.CUSTOMER);
        userAccountRepository.save(prospect.user());
        prospect.application().setStatus(AppStatus.APPROVED);
        applicationRepository.save(prospect.application());
        Account account = new Account();
        account.setApplicationId(prospect.application().getId());
        account.setAccountNumber("910123456789");
        account.setProduct("Everyday Savings Account");
        account.setKycTier("tier_1");
        account.setCoreReference("CR-x");
        account.setStatus(AccountState.ACTIVE);
        account.setOpenedAt(Instant.now());
        accountRepository.save(account);

        Document doc = fixtures.document(prospect.application(), prospect.user(),
                DocType.NATIONAL_ID, DocState.VERIFIED);
        doc.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        documentRepository.save(doc);
        jobs.processExpiredDocuments();

        Application reloaded = applicationRepository.findById(prospect.application().getId()).orElseThrow();
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, reloaded.getStatus());
        assertEquals(AccountState.SUSPENDED, accountRepository.findById(account.getId()).orElseThrow().getStatus());
        ReKycCase kase = reKycCaseRepository
                .findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(prospect.user().getId(),
                        java.util.List.of(ReKycStatus.DUE, ReKycStatus.IN_PROGRESS, ReKycStatus.OVERDUE))
                .orElseThrow();
        assertEquals(ReKycOrigin.DOC_EXPIRY, kase.getOrigin());
    }

    @Test
    void reKycCasesGoOverdueAtDueDate() {
        Prospect prospect = fixtures.prospect("overdue@meridian.test", branch, "Overdue",
                EmploymentStatus.EMPLOYED, "50000", true);
        prospect.user().setRole(Role.CUSTOMER);
        userAccountRepository.save(prospect.user());
        ReKycCase kase = new ReKycCase();
        kase.setOrigin(ReKycOrigin.CAMPAIGN);
        kase.setCustomerUserId(prospect.user().getId());
        kase.setApplicationId(prospect.application().getId());
        kase.setStatus(ReKycStatus.DUE);
        kase.setDueDate(Instant.now().minus(2, ChronoUnit.DAYS));
        kase.setCreatedAt(Instant.now());
        reKycCaseRepository.save(kase);
        jobs.runReKycDue();
        assertEquals(ReKycStatus.OVERDUE, reKycCaseRepository.findById(kase.getId()).orElseThrow().getStatus());
    }
}
