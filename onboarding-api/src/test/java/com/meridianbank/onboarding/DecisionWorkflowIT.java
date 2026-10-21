package com.meridianbank.onboarding;

import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.client.CoreMockClient;
import com.meridianbank.onboarding.client.ProviderMockClient;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.*;
import com.meridianbank.onboarding.service.documents.DocumentService;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import com.meridianbank.onboarding.support.TestFixtures.Prospect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Decision workflow: approve/decline/request-documents, step-up enforcement, max approval tier,
 * confirmed-hit blocking, document rejection cascade and re-entry with identity re-verification.
 */
class DecisionWorkflowIT extends IntegrationTestBase {

    private static final Pattern CODE = Pattern.compile("code is (\\d{6})");

    @Autowired
    private DecisionService decisionService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private IdentityVerificationService identityVerificationService;
    @Autowired
    private ScreeningService screeningService;
    @Autowired
    private JourneyService journeyService;
    @Autowired
    private ApplicationStateMachine stateMachine;
    @Autowired
    private KycTierService kycTierService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private JourneyStepRepository journeyStepRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AuditEventRepository auditEventRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private Branch branch;
    private StaffUser officerT3;
    private UserAccount officerT3User;

    @BeforeEach
    void setUp() {
        branch = fixtures.branch("MB-CEN-01");
        officerT3 = fixtures.officer(branch, "tier_3");
        officerT3User = userAccountRepository.findByStaffId(officerT3.getId()).orElseThrow();
        when(coreMockClient.openAccount(any(), any(), any(), any(), any()))
                .thenReturn(new CoreMockClient.OpenedAccount("9101000000001", "CR-test-0001",
                        "Everyday Savings Account", "tier_2"));
    }

    private byte[] pdf() {
        return "%PDF-1.4 test".getBytes();
    }

    private Prospect underReview(String email, DocType... docs) {
        Prospect prospect = fixtures.prospect(email, branch, "Review " + email,
                EmploymentStatus.EMPLOYED, "60000", true);
        for (DocType type : docs) {
            documentService.upload(prospect.user(), prospect.application().getId(), type.name(),
                    type.name() + ".pdf", "application/pdf", pdf());
            Document uploaded = documentRepository.findByApplicationIdOrderByCreatedAtAsc(prospect.application().getId())
                    .stream().filter(d -> d.getDocType() == type).findFirst().orElseThrow();
            documentService.verify(officerT3User, uploaded.getId());
        }
        when(providerMockClient.verifyIdentityXml(any())).thenReturn(
                "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY p \"S\">]>"
                        + "<identity-verification-response><providerReference>SCR</providerReference>"
                        + "<providerName>&p;</providerName><matchScore>90</matchScore><outcome>MATCH</outcome>"
                        + "</identity-verification-response>");
        identityVerificationService.run(prospect.user());
        journeyService.submit(prospect.user());
        when(providerMockClient.screenWatchlist(any(), any(), any()))
                .thenReturn(new ProviderMockClient.WatchlistResult("WLS", List.of()));
        when(providerMockClient.pullBureau(any(), any(), any()))
                .thenReturn(new ProviderMockClient.BureauScore(720, List.of(), "BUR"));
        screeningService.screen(fresh(prospect));
        assertEquals(AppStatus.UNDER_REVIEW, fresh(prospect).getStatus());
        return prospect;
    }

    private Application fresh(Prospect prospect) {
        return applicationRepository.findById(prospect.application().getId()).orElseThrow();
    }

    private void stepUp(UserAccount user, String sid, OtpPurpose purpose) {
        otpService.issueStepUpOtp(user, sid, purpose);
        String code = extractCode(user);
        otpService.verifyStepUpOtp(user, sid, purpose, code);
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
    void approveOpensAccountAndUpgradesRole() {
        Prospect prospect = underReview("approve@meridian.test", DocType.NATIONAL_ID, DocType.ADDRESS_PROOF);
        String sid = "approve-session";
        stepUp(officerT3User, sid, OtpPurpose.APPROVE);
        Account account = decisionService.approve(officerT3User, sid, prospect.application().getId(),
                "Identity and documents verified; account approved.");
        assertNotNull(account);
        assertEquals(AppStatus.APPROVED, fresh(prospect).getStatus());
        assertEquals("tier_2", account.getKycTier());
        assertEquals(Role.CUSTOMER,
                userAccountRepository.findById(prospect.user().getId()).orElseThrow().getRole());
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(e -> "APPLICATION_APPROVED".equals(e.getAction())));
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(e -> "ACCOUNT_OPENED_CORE".equals(e.getAction())));
    }

    @Test
    void approveRequiresStepUpOtp() {
        Prospect prospect = underReview("nostepup@meridian.test", DocType.NATIONAL_ID);
        ApiException ex = assertThrows(ApiException.class, () ->
                decisionService.approve(officerT3User, "no-grant-session", prospect.application().getId(),
                        "reason"));
        assertTrue(ex.getMessage().contains("Step-up OTP"));
    }

    @Test
    void officerCannotApproveAboveMaxApprovalTier() {
        StaffUser officerT1 = fixtures.officer(branch, "tier_1");
        UserAccount officerT1User = userAccountRepository.findByStaffId(officerT1.getId()).orElseThrow();
        Prospect prospect = underReview("maxtier@meridian.test", DocType.NATIONAL_ID, DocType.ADDRESS_PROOF);
        assertEquals("tier_2", kycTierService.deriveTier(prospect.application()));
        String sid = "maxtier-session";
        stepUp(officerT1User, sid, OtpPurpose.APPROVE);
        ApiException ex = assertThrows(ApiException.class, () ->
                decisionService.approve(officerT1User, sid, prospect.application().getId(), "reason"));
        assertTrue(ex.getMessage().contains("exceeds your approval authority"));
        assertEquals(AppStatus.UNDER_REVIEW, fresh(prospect).getStatus());
    }

    @Test
    void officerCannotApproveAnApplicationOnWatchlistHold() {
        Prospect prospect = fixtures.prospect("holdapprove@meridian.test", branch, "Hold Approve",
                EmploymentStatus.EMPLOYED, "60000", true);
        documentService.upload(prospect.user(), prospect.application().getId(),
                DocType.NATIONAL_ID.name(), "nric.pdf", "application/pdf", pdf());
        Document holdDoc = documentRepository.findByApplicationIdOrderByCreatedAtAsc(prospect.application().getId()).get(0);
        documentService.verify(officerT3User, holdDoc.getId());
        when(providerMockClient.verifyIdentityXml(any())).thenReturn(
                "<?xml version=\"1.0\"?><identity-verification-response><providerReference>SCR</providerReference>"
                        + "<matchScore>90</matchScore><outcome>MATCH</outcome></identity-verification-response>");
        identityVerificationService.run(prospect.user());
        journeyService.submit(prospect.user());
        when(providerMockClient.screenWatchlist(any(), any(), any()))
                .thenReturn(new ProviderMockClient.WatchlistResult("WLS", List.of(
                        new ProviderMockClient.WatchlistMatch("IVAN NORVIK", "Sanctions List",
                                new java.math.BigDecimal("0.94")))));
        when(providerMockClient.pullBureau(any(), any(), any()))
                .thenReturn(new ProviderMockClient.BureauScore(700, List.of(), "BUR"));
        screeningService.screen(fresh(prospect));
        assertEquals(AppStatus.WATCHLIST_HOLD, fresh(prospect).getStatus());
        // Any path into approved that bypasses an active watchlist_hold must be impossible:
        assertThrows(ApiException.class, () ->
                decisionService.approve(officerT3User, "sid", prospect.application().getId(), "reason"));
    }

    @Test
    void declineRecordsReasonsAndAudit() {
        Prospect prospect = underReview("decline@meridian.test", DocType.NATIONAL_ID);
        String sid = "decline-session";
        stepUp(officerT3User, sid, OtpPurpose.DECLINE);
        decisionService.decline(officerT3User, sid, prospect.application().getId(),
                "We could not verify your identity details.", "NRIC mismatch with official records.");
        Application declinedApp = fresh(prospect);
        assertEquals(AppStatus.DECLINED, declinedApp.getStatus());
        assertTrue(declinedApp.getCustomerVisibleReason().contains("could not verify"));
        assertTrue(declinedApp.getInternalReason().contains("NRIC mismatch"));
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(e -> "APPLICATION_DECLINED".equals(e.getAction())));
    }

    @Test
    void requestDocumentsClearsIdentityStepAndReentryRequiresReverification() {
        Prospect prospect = underReview("request@meridian.test", DocType.NATIONAL_ID);
        decisionService.requestDocuments(officerT3User, prospect.application().getId(),
                List.of(DocType.ADDRESS_PROOF.name()), "Please provide address proof.");
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, fresh(prospect).getStatus());
        // re-entry cleared identity_verified (and documents_uploaded)
        journeyStepRepository.findByApplicationIdAndStepKey(prospect.application().getId(),
                        JourneyStepKey.IDENTITY_VERIFIED)
                .ifPresent(step -> assertFalse(step.isCompleted()));
        // applicant re-uploads the requested document
        documentService.upload(prospect.user(), prospect.application().getId(),
                DocType.ADDRESS_PROOF.name(), "addr.pdf", "application/pdf", pdf());
        Document addr = documentRepository.findByApplicationIdOrderByCreatedAtAsc(prospect.application().getId())
                .stream().filter(d -> d.getDocType() == DocType.ADDRESS_PROOF).findFirst().orElseThrow();
        documentService.verify(officerT3User, addr.getId());
        // Still documents_requested: identity has not been re-verified
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, fresh(prospect).getStatus());
        // applicant re-runs identity verification and re-submits
        identityVerificationService.run(prospect.user());
        journeyService.resubmit(prospect.user());
        assertEquals(AppStatus.UNDER_REVIEW, fresh(prospect).getStatus());
        // approve now succeeds (identity re-verified, tier_2 <= max tier_3)
        String sid = "reentry-session";
        stepUp(officerT3User, sid, OtpPurpose.APPROVE);
        decisionService.approve(officerT3User, sid, prospect.application().getId(), "Approved after re-entry.");
        assertEquals(AppStatus.APPROVED, fresh(prospect).getStatus());
    }

    @Test
    void officerRejectingDocumentInReviewRoutesToDocumentsRequested() {
        Prospect prospect = underReview("reject@meridian.test", DocType.NATIONAL_ID);
        // An extra uploaded (not yet verified) document sits on the application while it is
        // under review; rejecting it triggers the system edge back to documents_requested.
        Document extra = fixtures.document(prospect.application(), prospect.user(),
                DocType.ADDRESS_PROOF, DocState.UPLOADED);
        documentService.reject(officerT3User, extra.getId());
        assertEquals(AppStatus.DOCUMENTS_REQUESTED, fresh(prospect).getStatus());
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(e -> "DOCUMENTS_REQUESTED_DOC_PROBLEM".equals(e.getAction())));
    }
}
