package com.meridianbank.onboarding;

import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.jobs.DocumentLifecycleJobs;
import com.meridianbank.onboarding.service.*;
import com.meridianbank.onboarding.service.documents.DocumentService;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import com.meridianbank.onboarding.support.TestFixtures.Prospect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Re-KYC: case lifecycle, step-up enforcement, verified-document requirements, tier
 * re-derivation on completion, account restore, and the rule that the application status is
 * never changed by re-KYC.
 */
class ReKycIT extends IntegrationTestBase {

    private static final Pattern CODE = Pattern.compile("code is (\\d{6})");

    @Autowired
    private ReKycService reKycService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private DocumentLifecycleJobs jobs;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ReKycCaseRepository reKycCaseRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private OtpService otpService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AuditEventRepository auditEventRepository;
    @Autowired
    private IdentityVerificationResultRepository identityRepository;

    private Branch branch;
    private Prospect approved;
    private UserAccount officerUser;
    private Account account;

    @BeforeEach
    void setUp() {
        branch = fixtures.branch("MB-CEN-01");
        StaffUser officer = fixtures.officer(branch, "tier_3");
        officerUser = userAccountRepository.findByStaffId(officer.getId()).orElseThrow();
        approved = fixtures.prospect("rekyc-customer@meridian.test", branch, "ReKyc Customer",
                EmploymentStatus.EMPLOYED, "60000", true);
        approved.user().setRole(Role.CUSTOMER);
        userAccountRepository.save(approved.user());
        documentService.upload(approved.user(), approved.application().getId(),
                DocType.NATIONAL_ID.name(), "nric.pdf", "application/pdf", "%PDF-1.4 x".getBytes());
        Document doc = documentRepository.findByApplicationIdOrderByCreatedAtAsc(approved.application().getId()).get(0);
        documentService.verify(officerUser, doc.getId());
        when(providerMockClient.verifyIdentityXml(any())).thenReturn(
                "<?xml version=\"1.0\"?><identity-verification-response><providerReference>SCR</providerReference>"
                        + "<matchScore>90</matchScore><outcome>MATCH</outcome></identity-verification-response>");
        approved.application().setStatus(AppStatus.APPROVED);
        applicationRepository.save(approved.application());
        account = new Account();
        account.setApplicationId(approved.application().getId());
        account.setAccountNumber("910999999999");
        account.setProduct("Everyday Savings Account");
        account.setKycTier("tier_1");
        account.setCoreReference("CR-seed");
        account.setStatus(AccountState.ACTIVE);
        account.setOpenedAt(Instant.now());
        accountRepository.save(account);

        IdentityVerificationResult result = new IdentityVerificationResult();
        result.setApplicationId(approved.application().getId());
        result.setMatchScore(90);
        result.setOutcome("MATCH");
        result.setStatus(IdentityVerificationResult.STATUS_SUCCESS);
        result.setCreatedAt(Instant.now());
        identityRepository.save(result);
    }

    private ReKycCase expireDocumentAndOpenCase() {
        Document doc = documentRepository.findByApplicationIdOrderByCreatedAtAsc(approved.application().getId()).get(0);
        doc.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        documentRepository.save(doc);
        jobs.processExpiredDocuments();
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, freshApplication().getStatus());
        ReKycCase kase = reKycCaseRepository
                .findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(approved.user().getId(),
                        List.of(ReKycStatus.DUE, ReKycStatus.IN_PROGRESS, ReKycStatus.OVERDUE))
                .orElseThrow();
        assertEquals(AccountState.SUSPENDED, accountRepository.findById(account.getId()).orElseThrow().getStatus());
        return kase;
    }

    private Application freshApplication() {
        return applicationRepository.findById(approved.application().getId()).orElseThrow();
    }

    private void stepUpReKyc(UserAccount user, String sid) {
        otpService.issueStepUpOtp(user, sid, OtpPurpose.REKYC_SUBMIT);
        String code = extractCode(user);
        otpService.verifyStepUpOtp(user, sid, OtpPurpose.REKYC_SUBMIT, code);
    }

    private String extractCode(UserAccount user) {
        for (Notification n : notificationRepository.findTop50ByUserAccountIdOrderByCreatedAtDesc(user.getId())) {
            Matcher m = CODE.matcher(n.getBody());
            if (m.find()) {
                return m.group(1);
            }
        }
        throw new IllegalStateException("no OTP delivered");
    }

    @Test
    void fullReKycFlowRestoresAccountAndRederivesTierWithoutChangingApplicationStatus() {
        ReKycCase kase = expireDocumentAndOpenCase();
        // customer uploads a fresh identity document
        reKycService.uploadReKycDocument(approved.user(), kase.getId(), DocType.NATIONAL_ID.name(),
                "fresh-nric.pdf", "application/pdf", "%PDF-1.4 fresh".getBytes());
        Document fresh = documentRepository.findByApplicationIdOrderByCreatedAtAsc(approved.application().getId())
                .stream().filter(d -> kase.getId().equals(d.getReKycCaseId())).findFirst().orElseThrow();
        // officer verifies it
        documentService.verify(officerUser, fresh.getId());
        // submit requires step-up
        assertThrows(ApiException.class, () -> reKycService.submit(approved.user(), "no-grant", kase.getId()));
        stepUpReKyc(approved.user(), "rekyc-sid");
        ReKycCase completed = reKycService.submit(approved.user(), "rekyc-sid", kase.getId());
        assertEquals(ReKycStatus.COMPLETED, completed.getStatus());
        Account restored = accountRepository.findById(account.getId()).orElseThrow();
        assertEquals(AccountState.ACTIVE, restored.getStatus());
        assertEquals("tier_1", restored.getKycTier());
        // application status is NOT changed by re-KYC
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, freshApplication().getStatus());
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(e -> "REKYC_COMPLETED".equals(e.getAction())));
    }

    @Test
    void reKycSubmitRequiresVerifiedDocuments() {
        ReKycCase kase = expireDocumentAndOpenCase();
        reKycService.uploadReKycDocument(approved.user(), kase.getId(), DocType.NATIONAL_ID.name(),
                "fresh-nric.pdf", "application/pdf", "%PDF-1.4 fresh".getBytes());
        // not verified yet -> submit refused even with step-up
        stepUpReKyc(approved.user(), "rekyc-sid2");
        ApiException ex = assertThrows(ApiException.class, () ->
                reKycService.submit(approved.user(), "rekyc-sid2", kase.getId()));
        assertTrue(ex.getMessage().contains("verified"));
    }

    @Test
    void reKycRejectsDocumentsOutsideRequiredTypes() {
        ReKycCase kase = expireDocumentAndOpenCase();
        assertThrows(ApiException.class, () ->
                reKycService.uploadReKycDocument(approved.user(), kase.getId(),
                        DocType.PROOF_OF_INCOME.name(), "poi.pdf", "application/pdf", "%PDF-1.4".getBytes()));
    }

    @Test
    void customerCannotSubmitAnotherCustomersReKycCase() {
        ReKycCase kase = expireDocumentAndOpenCase();
        Prospect other = fixtures.prospect("rekyc-other@meridian.test", branch, "Other Customer",
                EmploymentStatus.EMPLOYED, "60000", true);
        other.user().setRole(Role.CUSTOMER);
        userAccountRepository.save(other.user());
        ApiException ex = assertThrows(ApiException.class, () ->
                reKycService.submit(other.user(), "sid", kase.getId()));
        assertTrue(ex.getMessage().contains("another customer"));
    }
}
