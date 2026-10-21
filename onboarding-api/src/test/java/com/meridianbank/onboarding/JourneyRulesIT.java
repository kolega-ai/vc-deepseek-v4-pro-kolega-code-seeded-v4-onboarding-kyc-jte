package com.meridianbank.onboarding;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.IdentityVerificationService;
import com.meridianbank.onboarding.service.JourneyService;
import com.meridianbank.onboarding.service.KycTierService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Journey rules: ordered steps, contact-verification gating for uploads, submission guards
 * (steps, verified documents, identity match >= 80, self-employed/high-income proof of income).
 */
class JourneyRulesIT extends IntegrationTestBase {

    private static final Pattern CODE = Pattern.compile("code is (\\d{6})");

    @Autowired
    private JourneyService journeyService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private IdentityVerificationService identityVerificationService;
    @Autowired
    private ApplicationStateMachine stateMachine;
    @Autowired
    private KycTierService kycTierService;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private JourneyStepRepository journeyStepRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;

    private Branch branch;
    private UserAccount officerUser;

    @BeforeEach
    void setUp() {
        branch = fixtures.branch("MB-CEN-01");
        StaffUser officer = fixtures.officer(branch, "tier_3");
        officerUser = userAccountRepository.findByStaffId(officer.getId()).orElseThrow();
    }

    private byte[] pdf() {
        return "%PDF-1.4 test".getBytes();
    }

    private void identityPasses(Prospect prospect) {
        when(providerMockClient.verifyIdentityXml(any())).thenReturn(identityXml(88, "MATCH"));
        identityVerificationService.run(prospect.user());
    }

    private String identityXml(int score, String outcome) {
        return "<?xml version=\"1.0\"?><!DOCTYPE identity-verification-response [<!ENTITY p \"ScreenSure\">]>"
                + "<identity-verification-response><providerReference>SCR-TEST</providerReference>"
                + "<providerName>&p;</providerName><matchScore>" + score + "</matchScore>"
                + "<outcome>" + outcome + "</outcome></identity-verification-response>";
    }

    private Document uploadVerified(Prospect prospect, DocType type) {
        documentService.upload(prospect.user(), prospect.application().getId(), type.name(),
                type.name() + ".pdf", "application/pdf", pdf());
        Document doc = documentRepository.findByApplicationIdOrderByCreatedAtAsc(prospect.application().getId())
                .stream().filter(d -> d.getDocType() == type).findFirst().orElseThrow();
        documentService.verify(officerUser, doc.getId());
        return doc;
    }

    private List<JourneyStep> journeySteps(Prospect prospect) {
        return journeyStepRepository.findByApplicationIdOrderByStepKeyAsc(prospect.application().getId());
    }

    @Test
    void contactVerificationIsAPrerequisiteForDocumentUpload() {
        Prospect prospect = fixtures.prospect("nogate@meridian.test", branch, "No Gate",
                EmploymentStatus.EMPLOYED, "50000", false);
        ApiException ex = assertThrows(ApiException.class, () ->
                documentService.upload(prospect.user(), prospect.application().getId(),
                        DocType.NATIONAL_ID.name(), "nric.pdf", "application/pdf", pdf()));
        assertTrue(ex.getMessage().contains("verified"));
    }

    @Test
    void submitIsBlockedWhenStepsAreIncomplete() {
        Prospect prospect = fixtures.prospect("incomplete@meridian.test", branch, "Incomplete",
                EmploymentStatus.EMPLOYED, "50000", true);
        // no documents, no identity -> blocked
        ApiException ex = assertThrows(ApiException.class, () -> journeyService.submit(prospect.user()));
        assertTrue(ex.getMessage().contains("journey steps") || ex.getMessage().contains("tier"));
    }

    @Test
    void submitIsBlockedWithoutVerifiedDocuments() {
        Prospect prospect = fixtures.prospect("uploadedonly@meridian.test", branch, "Uploaded Only",
                EmploymentStatus.EMPLOYED, "50000", true);
        documentService.upload(prospect.user(), prospect.application().getId(),
                DocType.NATIONAL_ID.name(), "nric.pdf", "application/pdf", pdf());
        identityPasses(prospect);
        // documents are uploaded but NOT verified -> derived tier stays tier_0 -> blocked
        ApiException ex = assertThrows(ApiException.class, () -> journeyService.submit(prospect.user()));
        assertTrue(ex.getMessage().contains("tier"));
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void identityBelow80DoesNotCompleteIdentityStep() {
        Prospect prospect = fixtures.prospect("lowscore@meridian.test", branch, "Lena Lowscore",
                EmploymentStatus.EMPLOYED, "50000", true);
        documentService.upload(prospect.user(), prospect.application().getId(),
                DocType.NATIONAL_ID.name(), "nric.pdf", "application/pdf", pdf());
        when(providerMockClient.verifyIdentityXml(any())).thenReturn(identityXml(71, "REVIEW"));
        identityVerificationService.run(prospect.user());
        boolean identityStepDone = journeySteps(prospect).stream()
                .anyMatch(s -> s.getStepKey() == JourneyStepKey.IDENTITY_VERIFIED && s.isCompleted());
        assertFalse(identityStepDone);
        assertThrows(ApiException.class, () -> journeyService.submit(prospect.user()));
    }

    @Test
    void selfEmployedWithoutProofOfIncomeIsCappedAtTier0() {
        Prospect prospect = fixtures.prospect("selfemp@meridian.test", branch, "Self Employed",
                EmploymentStatus.SELF_EMPLOYED, "60000", true);
        uploadVerified(prospect, DocType.NATIONAL_ID);
        uploadVerified(prospect, DocType.ADDRESS_PROOF);
        identityPasses(prospect);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
        assertThrows(ApiException.class, () -> journeyService.submit(prospect.user()));
    }

    @Test
    void selfEmployedWithProofOfIncomeReachesTier3() {
        Prospect prospect = fixtures.prospect("selfemp2@meridian.test", branch, "Self Employed Two",
                EmploymentStatus.SELF_EMPLOYED, "60000", true);
        uploadVerified(prospect, DocType.NATIONAL_ID);
        uploadVerified(prospect, DocType.ADDRESS_PROOF);
        uploadVerified(prospect, DocType.PROOF_OF_INCOME);
        identityPasses(prospect);
        assertEquals("tier_3", kycTierService.deriveTier(prospect.application()));
        journeyService.submit(prospect.user());
        assertEquals(AppStatus.SUBMITTED,
                applicationRepository.findById(prospect.application().getId()).orElseThrow().getStatus());
    }

    @Test
    void highIncomeWithoutProofOfIncomeIsCappedAtTier0() {
        Prospect prospect = fixtures.prospect("highincome@meridian.test", branch, "High Income",
                EmploymentStatus.EMPLOYED, "150000", true);
        uploadVerified(prospect, DocType.NATIONAL_ID);
        identityPasses(prospect);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
        assertThrows(ApiException.class, () -> journeyService.submit(prospect.user()));
    }

    @Test
    void happyPathSubmission() {
        Prospect prospect = fixtures.prospect("happy@meridian.test", branch, "Happy Path",
                EmploymentStatus.EMPLOYED, "60000", true);
        uploadVerified(prospect, DocType.NATIONAL_ID);
        identityPasses(prospect);
        journeyService.submit(prospect.user());
        Application submitted = applicationRepository.findById(prospect.application().getId()).orElseThrow();
        assertEquals(AppStatus.SUBMITTED, submitted.getStatus());
        assertNotNull(submitted.getScreeningDueAt());
    }

    @Test
    void mobileOtpCompletesContactVerifiedStep() {
        Prospect prospect = fixtures.prospect("mobile@meridian.test", branch, "Mobile Check",
                EmploymentStatus.EMPLOYED, "60000", false);
        journeyService.issueMobileOtp(prospect.user(), "session-mobile-test");
        String code = extractCodeFromNotifications(prospect.user());
        journeyService.verifyMobileOtp(prospect.user(), "session-mobile-test", code);
        boolean contactDone = journeySteps(prospect).stream()
                .anyMatch(s -> s.getStepKey() == JourneyStepKey.CONTACT_VERIFIED && s.isCompleted());
        assertTrue(contactDone);
    }

    private String extractCodeFromNotifications(UserAccount user) {
        for (Notification notification :
                notificationRepository.findTop50ByUserAccountIdOrderByCreatedAtDesc(user.getId())) {
            Matcher matcher = CODE.matcher(notification.getBody());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        throw new IllegalStateException("No OTP delivery found");
    }
}
