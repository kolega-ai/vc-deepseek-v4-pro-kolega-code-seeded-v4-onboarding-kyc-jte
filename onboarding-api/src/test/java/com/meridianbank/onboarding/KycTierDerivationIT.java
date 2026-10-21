package com.meridianbank.onboarding;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.KycTierService;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import com.meridianbank.onboarding.support.TestFixtures.Prospect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KYC tier derivation against the seeded tier table: verified document set, identity match
 * score, dispositions, conditional proof-of-income and document expiry re-derivation.
 */
class KycTierDerivationIT extends IntegrationTestBase {

    @Autowired
    private KycTierService kycTierService;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private IdentityVerificationResultRepository identityRepository;
    @Autowired
    private WatchlistHitRepository hitRepository;
    @Autowired
    private ApplicantRepository applicantRepository;

    private Branch branch;
    private Prospect prospect;

    @BeforeEach
    void setUp() {
        branch = fixtures.branch("MB-CEN-01");
        prospect = fixtures.prospect("tier-derive@meridian.test", branch, "Tier Derive",
                EmploymentStatus.EMPLOYED, "60000", true);
    }

    private Document verifiedDoc(DocType type) {
        Document doc = new Document();
        doc.setApplicationId(prospect.application().getId());
        doc.setDocType(type);
        doc.setCategory(type.getCategory());
        doc.setOriginalFilename(type.name() + ".pdf");
        doc.setStorageName("tier-" + type.name() + "-" + System.nanoTime() + ".pdf");
        doc.setContentType("application/pdf");
        doc.setSizeBytes(10);
        doc.setUploadedBy(prospect.user().getId());
        doc.setVerificationState(DocState.VERIFIED);
        doc.setVerifiedAt(Instant.now());
        doc.setCreatedAt(Instant.now());
        doc.setExpiryDate(Instant.now().plus(365, ChronoUnit.DAYS));
        return documentRepository.save(doc);
    }

    private void identity(int score) {
        IdentityVerificationResult result = new IdentityVerificationResult();
        result.setApplicationId(prospect.application().getId());
        result.setMatchScore(score);
        result.setOutcome(score >= 80 ? "MATCH" : "REVIEW");
        result.setStatus(IdentityVerificationResult.STATUS_SUCCESS);
        result.setCreatedAt(Instant.now());
        identityRepository.save(result);
    }

    @Test
    void noIdentityResultYieldsTier0() {
        verifiedDoc(DocType.NATIONAL_ID);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void identityBelow80YieldsTier0EvenWithDocuments() {
        verifiedDoc(DocType.NATIONAL_ID);
        verifiedDoc(DocType.ADDRESS_PROOF);
        identity(79);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void oneIdentityDocumentYieldsTier1() {
        verifiedDoc(DocType.NATIONAL_ID);
        identity(88);
        assertEquals("tier_1", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void identityPlusAddressYieldsTier2() {
        verifiedDoc(DocType.PASSPORT);
        verifiedDoc(DocType.ADDRESS_PROOF);
        identity(90);
        assertEquals("tier_2", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void allThreeDocumentsYieldTier3() {
        verifiedDoc(DocType.NATIONAL_ID);
        verifiedDoc(DocType.ADDRESS_PROOF);
        verifiedDoc(DocType.PROOF_OF_INCOME);
        identity(95);
        assertEquals("tier_3", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void uploadedOrRejectedDocumentsNeverCount() {
        Document uploaded = new Document();
        uploaded.setApplicationId(prospect.application().getId());
        uploaded.setDocType(DocType.NATIONAL_ID);
        uploaded.setCategory(DocCategory.IDENTITY);
        uploaded.setOriginalFilename("n.pdf");
        uploaded.setStorageName("up-" + System.nanoTime() + ".pdf");
        uploaded.setContentType("application/pdf");
        uploaded.setSizeBytes(10);
        uploaded.setUploadedBy(prospect.user().getId());
        uploaded.setVerificationState(DocState.UPLOADED);
        uploaded.setCreatedAt(Instant.now());
        uploaded.setExpiryDate(Instant.now().plus(365, ChronoUnit.DAYS));
        documentRepository.save(uploaded);
        identity(92);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void confirmedWatchlistHitCapsAtTier0() {
        verifiedDoc(DocType.NATIONAL_ID);
        verifiedDoc(DocType.ADDRESS_PROOF);
        identity(92);
        WatchlistHit hit = new WatchlistHit();
        hit.setApplicationId(prospect.application().getId());
        hit.setMatchedName("Test Person");
        hit.setListSource("Sanctions");
        hit.setConfidence(new BigDecimal("0.9"));
        hit.setDisposition(HitDisposition.CONFIRMED);
        hit.setCreatedAt(Instant.now());
        hitRepository.save(hit);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void selfEmployedNeedsProofOfIncomeForAnyTierAboveZero() {
        prospect.applicant().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        applicantRepository.save(prospect.applicant());
        verifiedDoc(DocType.NATIONAL_ID);
        verifiedDoc(DocType.ADDRESS_PROOF);
        identity(92);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
        verifiedDoc(DocType.PROOF_OF_INCOME);
        assertEquals("tier_3", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void incomeAtOrAbove100kNeedsProofOfIncome() {
        prospect.applicant().setDeclaredAnnualIncome(new BigDecimal("100000.00"));
        applicantRepository.save(prospect.applicant());
        verifiedDoc(DocType.NATIONAL_ID);
        identity(92);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
        verifiedDoc(DocType.PROOF_OF_INCOME);
        assertEquals("tier_1", kycTierService.deriveTier(prospect.application()));
    }

    @Test
    void expiredDocumentInvalidatesDependentTier() {
        Document identityDoc = verifiedDoc(DocType.NATIONAL_ID);
        verifiedDoc(DocType.ADDRESS_PROOF);
        identity(92);
        assertEquals("tier_2", kycTierService.deriveTier(prospect.application()));
        identityDoc.setVerificationState(DocState.EXPIRED);
        documentRepository.save(identityDoc);
        assertEquals("tier_0", kycTierService.deriveTier(prospect.application()));
    }
}
