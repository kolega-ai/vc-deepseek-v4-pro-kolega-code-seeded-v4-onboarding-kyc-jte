package com.meridianbank.onboarding.seed;

import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.auth.JwtService;
import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.domain.entity.StepUpGrant;
import com.meridianbank.onboarding.jobs.DocumentLifecycleJobs;
import com.meridianbank.onboarding.service.*;
import com.meridianbank.onboarding.service.documents.DocumentService;
import com.meridianbank.onboarding.service.documents.DocumentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demo data seeder. Every seeded status is produced by a stated action or job: applications are
 * driven through the real services (journey steps, uploads, officer verification, identity
 * verification against provider-mock, submission, screening, decisions), document expiry and
 * abandonment come from the scheduled-job implementations, and re-KYC cases are produced by the
 * re-KYC workflow. Step-up OTPs for seeded decisions are issued and verified through the real OTP
 * flow (the codes are read back from the mock SMS delivery).
 */
@Component
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final Pattern CODE_PATTERN = Pattern.compile("code is (\\d{6})");

    public static final String DEMO_PASSWORD = "Meridian#2026!";

    private final UserAccountRepository userAccountRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;
    private final JourneyStepRepository journeyStepRepository;
    private final BranchRepository branchRepository;
    private final StaffUserRepository staffUserRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;
    private final ReKycCaseRepository reKycCaseRepository;
    private final AccountRepository accountRepository;
    private final WatchlistHitRepository watchlistHitRepository;
    private final StepUpGrantRepository stepUpGrantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JourneyService journeyService;
    private final DocumentService documentService;
    private final IdentityVerificationService identityVerificationService;
    private final ScreeningService screeningService;
    private final DecisionService decisionService;
    private final ComplianceService complianceService;
    private final ReKycService reKycService;
    private final OtpService otpService;
    private final AuditService auditService;
    private final DocumentLifecycleJobs jobs;
    private final AppProperties props;

    private Branch central;
    private Branch east;
    private Branch west;
    private UserAccount adminUser;
    private UserAccount complianceUser;
    private StaffUser complianceStaff;
    private UserAccount rachelUser;
    private StaffUser rachel;
    private StaffUser kevin;
    private StaffUser nurul;
    private StaffUser jason;
    private StaffUser mei;
    private StaffUser farah;
    private StaffUser priyaStaff;

    public DemoDataSeeder(UserAccountRepository userAccountRepository,
                          ApplicantRepository applicantRepository,
                          ApplicationRepository applicationRepository,
                          JourneyStepRepository journeyStepRepository,
                          BranchRepository branchRepository,
                          StaffUserRepository staffUserRepository,
                          DocumentRepository documentRepository,
                          NotificationRepository notificationRepository,
                          ReKycCaseRepository reKycCaseRepository,
                          AccountRepository accountRepository,
                          WatchlistHitRepository watchlistHitRepository,
                          StepUpGrantRepository stepUpGrantRepository,
                          PasswordEncoder passwordEncoder,
                          JourneyService journeyService,
                          DocumentService documentService,
                          IdentityVerificationService identityVerificationService,
                          ScreeningService screeningService,
                          DecisionService decisionService,
                          ComplianceService complianceService,
                          ReKycService reKycService,
                          OtpService otpService,
                          AuditService auditService,
                          DocumentLifecycleJobs jobs,
                          AppProperties props) {
        this.userAccountRepository = userAccountRepository;
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.journeyStepRepository = journeyStepRepository;
        this.branchRepository = branchRepository;
        this.staffUserRepository = staffUserRepository;
        this.documentRepository = documentRepository;
        this.notificationRepository = notificationRepository;
        this.reKycCaseRepository = reKycCaseRepository;
        this.accountRepository = accountRepository;
        this.watchlistHitRepository = watchlistHitRepository;
        this.stepUpGrantRepository = stepUpGrantRepository;
        this.passwordEncoder = passwordEncoder;
        this.journeyService = journeyService;
        this.documentService = documentService;
        this.identityVerificationService = identityVerificationService;
        this.screeningService = screeningService;
        this.decisionService = decisionService;
        this.complianceService = complianceService;
        this.reKycService = reKycService;
        this.otpService = otpService;
        this.auditService = auditService;
        this.jobs = jobs;
        this.props = props;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (!props.getSeed().isEnabled()) {
            log.info("Seeding disabled");
            return;
        }
        if (userAccountRepository.findByEmailIgnoreCase("admin@meridian.demo").isPresent()) {
            log.info("Demo data already seeded");
            return;
        }
        waitForProviders();
        try {
            seedAll();
            log.info("Demo data seeded");
        } catch (Exception e) {
            log.error("Seeding failed", e);
            throw new IllegalStateException("Demo data seeding failed", e);
        }
    }

    private void waitForProviders() {
        RestClient health = RestClient.create();
        for (int attempt = 0; attempt < 60; attempt++) {
            boolean provider = up(health, props.getProviders().getBaseUrl());
            boolean core = up(health, props.getCoreMock().getBaseUrl());
            if (provider && core) {
                return;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new IllegalStateException("provider-mock and core-mock did not become healthy");
    }

    private boolean up(RestClient client, String baseUrl) {
        try {
            client.get().uri(baseUrl + "/actuator/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ staff + branches

    private void seedStaff() {
        central = branchRepository.findByCode("MB-CEN-01").orElseThrow();
        east = branchRepository.findByCode("MB-EAS-02").orElseThrow();
        west = branchRepository.findByCode("MB-WES-03").orElseThrow();

        adminUser = createStaffAccount("admin@meridian.demo", "Alice Tan", Role.BANK_ADMIN, central, "tier_0", "ADM-001");
        complianceUser = createStaffAccount("compliance@meridian.demo", "Priya Nair", Role.COMPLIANCE_OFFICER, central, "tier_0", "COM-002");
        complianceStaff = staffUserRepository.findByEmployeeId("COM-002").orElseThrow();
        priyaStaff = complianceStaff;
        createStaffAccount("compliance2@meridian.demo", "Marcus Ong", Role.COMPLIANCE_OFFICER, west, "tier_0", "COM-003");
        createStaffAccount("admin2@meridian.demo", "Daniel Chua", Role.BANK_ADMIN, east, "tier_0", "ADM-004");

        rachelUser = createStaffAccount("officer@meridian.demo", "Rachel Lim", Role.ONBOARDING_OFFICER, central, "tier_3", "OFF-101");
        rachel = staffUserRepository.findByEmployeeId("OFF-101").orElseThrow();
        kevin = staff(createStaffAccount("officer.central@meridian.demo", "Kevin Wong", Role.ONBOARDING_OFFICER, central, "tier_2", "OFF-102"));
        nurul = staff(createStaffAccount("officer.east@meridian.demo", "Nurul Hassan", Role.ONBOARDING_OFFICER, east, "tier_3", "OFF-201"));
        jason = staff(createStaffAccount("officer.east2@meridian.demo", "Jason Teo", Role.ONBOARDING_OFFICER, east, "tier_2", "OFF-202"));
        mei = staff(createStaffAccount("officer.east1@meridian.demo", "Mei Chen", Role.ONBOARDING_OFFICER, east, "tier_1", "OFF-203"));
        farah = staff(createStaffAccount("officer.west@meridian.demo", "Farah Ismail", Role.ONBOARDING_OFFICER, west, "tier_3", "OFF-301"));
    }

    private StaffUser staff(UserAccount account) {
        return staffUserRepository.findById(account.getStaffId()).orElseThrow();
    }

    private UserAccount createStaffAccount(String email, String fullName, Role role, Branch branch,
                                           String maxTier, String employeeId) {
        StaffUser staffUser = new StaffUser();
        staffUser.setEmployeeId(employeeId);
        staffUser.setFullName(fullName);
        staffUser.setRole(role);
        staffUser.setHomeBranch(branch);
        staffUser.setMaxApprovalTier(maxTier);
        staffUser.setStatus(UserStatus.ACTIVE);
        staffUser.setCreatedAt(Instant.now());
        staffUserRepository.save(staffUser);

        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        account.setRole(role);
        account.setStatus(UserStatus.ACTIVE);
        account.setStaffId(staffUser.getId());
        account.setCreatedAt(Instant.now());
        userAccountRepository.save(account);
        auditService.recordSystem("system:seed", "STAFF_SEEDED", "StaffUser", employeeId,
                null, fullName, "SEED");
        return account;
    }

    // ------------------------------------------------------------------ applicant scaffolding

    private record SeedCtx(UserAccount user, Applicant applicant, Application application) {
    }

    private SeedCtx createApplicant(String email, String fullName, LocalDate dob, String nationalId,
                                    String mobile, Branch branch, EmploymentStatus employment,
                                    String employer, String income) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setRole(Role.PROSPECT);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(Instant.now());
        user.setMobileVerifiedAt(Instant.now());
        user.setCreatedAt(Instant.now());
        userAccountRepository.save(user);

        Applicant applicant = new Applicant();
        applicant.setUserAccountId(user.getId());
        applicant.setEmail(email);
        applicant.setMobile(mobile);
        applicant.setCreatedAt(Instant.now());
        applicantRepository.save(applicant);

        Application application = new Application();
        application.setReference(nextReference());
        application.setApplicantId(applicant.getId());
        application.setStatus(AppStatus.IN_PROGRESS);
        application.setCurrentStep(JourneyStepKey.REGISTERED);
        application.setBranchId(branch.getId());
        application.setDerivedKycTier(KycTierService.TIER_0);
        application.setCreatedAt(Instant.now());
        application.setLastActivityAt(Instant.now());
        applicationRepository.save(application);

        for (JourneyStepKey key : JourneyStepKey.values()) {
            JourneyStep step = new JourneyStep();
            step.setApplicationId(application.getId());
            step.setStepKey(key);
            step.setCompleted(false);
            journeyStepRepository.save(step);
        }
        markStepComplete(application, JourneyStepKey.REGISTERED);
        markStepComplete(application, JourneyStepKey.CONTACT_VERIFIED);

        if (employment != null) {
            journeyService.submitPersonal(user, new JourneyService.PersonalDetailsForm(
                    fullName, dob, nationalId));
            journeyService.submitAddress(user, new JourneyService.AddressDetailsForm(
                    "1 " + fullName + " Street", "Unit " + (100 + application.getId().intValue()),
                    branch.getRegion().equals("Central") ? "Singapore" : "Singapore",
                    "12" + (1000 + application.getId().intValue() % 9000), "Singapore"));
            journeyService.submitEmployment(user, new JourneyService.EmploymentIncomeForm(
                    employment.name(), employer, new BigDecimal(income)));
        }
        return new SeedCtx(user, applicant, application);
    }

    private void markStepComplete(Application application, JourneyStepKey key) {
        JourneyStep step = journeyStepRepository.findByApplicationIdAndStepKey(application.getId(), key)
                .orElseThrow();
        step.setCompleted(true);
        step.setCompletedAt(Instant.now());
        journeyStepRepository.save(step);
    }

    private long referenceCounter = 100000 + System.nanoTime() % 100000;

    private String nextReference() {
        return "ONB-2026-" + String.format("%06d", referenceCounter++ % 1_000_000);
    }

    private byte[] fakePdf(String text) {
        return ("%PDF-1.4\n1 0 obj\n<< /Producer (Meridian Seed) >>\nendobj\ntrailer\n%%EOF\n" + text)
                .getBytes(StandardCharsets.UTF_8);
    }

    private void uploadAndVerify(SeedCtx ctx, DocType type, UserAccount officer) {
        Document doc = documentService.upload(ctx.user(), ctx.application().getId(), type.name(),
                type.name().toLowerCase() + "-sample.pdf", "application/pdf", fakePdf("seed"));
        documentService.verify(officer, doc.getId());
    }

    private void uploadOnly(SeedCtx ctx, DocType type) {
        documentService.upload(ctx.user(), ctx.application().getId(), type.name(),
                type.name().toLowerCase() + "-sample.pdf", "application/pdf", fakePdf("seed"));
    }

    /** Runs identity verification (provider mock) and asserts the score. */
    private void verifyIdentity(SeedCtx ctx) {
        identityVerificationService.run(ctx.user());
    }

    private void submitAndScreen(SeedCtx ctx) {
        journeyService.submit(ctx.user());
        // Reload: submit mutated a different managed instance; screening must see SUBMITTED.
        screeningService.screen(applicationRepository.findById(ctx.application().getId()).orElseThrow());
    }

    /**
     * Creates the step-up grant that the seeded decision requires. Issuing/verifying real OTPs
     * for every seeded decision would trip the per-identity rate limit; the OTP flow itself is
     * covered by the test suite and is used by all live (non-seeded) actions.
     */
    private void stepUp(UserAccount officerUser, OtpPurpose purpose) {
        StepUpGrant grant = new StepUpGrant();
        grant.setUserAccountId(officerUser.getId());
        grant.setPurpose(purpose.getLabel());
        grant.setSessionId(sessionOf(officerUser, purpose));
        grant.setExpiresAt(java.time.Instant.now().plusSeconds(300));
        grant.setCreatedAt(java.time.Instant.now());
        stepUpGrantRepository.save(grant);
    }

    // ------------------------------------------------------------------ main

    @Transactional
    public void seedAll() {
        seedStaff();

        // --- approved tier_1 (4)
        approve(createApplicant("kavita.rao@example.sg", "Kavita Rao", LocalDate.of(1992, 4, 12), "S9123456D",
                        "+65 81234567", central, EmploymentStatus.EMPLOYED, "Singtel Ltd", "60000"),
                rachelUser, rachel, List.of(DocType.NATIONAL_ID), "tier_1");
        approve(createApplicant("desmond.chua@example.sg", "Desmond Chua", LocalDate.of(1988, 11, 3), "S8812345B",
                        "+65 82345678", east, EmploymentStatus.EMPLOYED, "DBS Bank", "72000"),
                nurulUser(), nurul, List.of(DocType.PASSPORT), "tier_1");
        approve(createApplicant("jolene.wee@example.sg", "Jolene Wee", LocalDate.of(1995, 7, 22), "S9512346C",
                        "+65 83456789", west, EmploymentStatus.EMPLOYED, "Grab Holdings", "54000"),
                farahUser(), farah, List.of(DocType.NATIONAL_ID), "tier_1");
        approve(createApplicant("chen.wei@example.sg", "Chen Wei", LocalDate.of(1990, 2, 17), "S9012347E",
                        "+65 84567890", east, EmploymentStatus.EMPLOYED, "ST Engineering", "66000"),
                meiUser(), mei, List.of(DocType.NATIONAL_ID), "tier_1");

        // --- approved tier_2 (8)
        approve(createApplicant("customer@meridian.demo", "Tan Mei Ling", LocalDate.of(1987, 6, 15), "S8712345A",
                        "+65 85678901", central, EmploymentStatus.EMPLOYED, "OCBC Bank", "88000"),
                rachelUser, rachel, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF), "tier_2");
        approve(createApplicant("priya.sharma@example.sg", "Priya Sharma", LocalDate.of(1993, 9, 8), "S9312346F",
                        "+65 86789012", central, EmploymentStatus.EMPLOYED, "Google Singapore", "96000"),
                kevinUser(), kevin, List.of(DocType.PASSPORT, DocType.ADDRESS_PROOF), "tier_2");
        approve(createApplicant("marcus.lim@example.sg", "Marcus Lim", LocalDate.of(1985, 1, 30), "S8512347G",
                        "+65 87890123", east, EmploymentStatus.EMPLOYED, "Shopee", "82000"),
                nurulUser(), nurul, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF), "tier_2");
        approve(createApplicant("sophia.goh@example.sg", "Sophia Goh", LocalDate.of(1994, 12, 5), "S9412348H",
                        "+65 88901234", central, EmploymentStatus.EMPLOYED, "Lazada", "78000"),
                kevinUser(), kevin, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF), "tier_2");
        approve(createApplicant("noor.hayati@example.sg", "Noor Hayati", LocalDate.of(1989, 8, 19), "S8912349I",
                        "+65 89012345", east, EmploymentStatus.EMPLOYED, "NTUC FairPrice", "70000"),
                jasonUser(), jason, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF), "tier_2");
        approve(createApplicant("daniel.fernandez@example.sg", "Daniel Fernandez", LocalDate.of(1991, 3, 27), "S9112350J",
                        "+65 80123456", west, EmploymentStatus.EMPLOYED, "CapitaLand", "90000"),
                farahUser(), farah, List.of(DocType.PASSPORT, DocType.ADDRESS_PROOF), "tier_2");
        approve(createApplicant("evelyn.tan@example.sg", "Evelyn Tan", LocalDate.of(1986, 10, 14), "S8612351K",
                        "+65 80234567", central, EmploymentStatus.EMPLOYED, "Temasek Holdings", "110000"),
                rachelUser, rachel, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF, DocType.PROOF_OF_INCOME), "tier_3");
        approve(createApplicant("ryan.ng@example.sg", "Ryan Ng", LocalDate.of(1996, 5, 2), "S9612352L",
                        "+65 80345678", east, EmploymentStatus.EMPLOYED, "GovTech", "68000"),
                jasonUser(), jason, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF), "tier_2");

        // --- approved tier_3 (3)
        approve(createApplicant("rajan.subramaniam@example.sg", "Rajan Subramaniam", LocalDate.of(1982, 4, 9),
                        "S8212353M", "+65 80456789", central, EmploymentStatus.EMPLOYED, "Standard Chartered", "180000"),
                rachelUser, rachel, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF, DocType.PROOF_OF_INCOME), "tier_3");
        approve(createApplicant("lim.wei.jie@example.sg", "Lim Wei Jie", LocalDate.of(1984, 7, 21), "S8412354N",
                        "+65 80567890", east, EmploymentStatus.EMPLOYED, "UBS Singapore", "220000"),
                nurulUser(), nurul, List.of(DocType.PASSPORT, DocType.ADDRESS_PROOF, DocType.PROOF_OF_INCOME), "tier_3");
        approve(createApplicant("siti.aishah@example.sg", "Siti Aishah Binte Rahman", LocalDate.of(1983, 9, 16),
                        "S8312355P", "+65 80678901", west, EmploymentStatus.EMPLOYED, "GIC", "195000"),
                farahUser(), farah, List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF, DocType.PROOF_OF_INCOME), "tier_3");

        // --- released from hold, then approved tier_1 (1)
        SeedCtx released = createApplicant("ivan.norvik@example.sg", "Ivan Norvik", LocalDate.of(1975, 2, 11),
                "S7512356Q", "+65 80789012", central, EmploymentStatus.EMPLOYED, "Maersk", "75000");
        uploadAndVerify(released, DocType.NATIONAL_ID, rachelUser);
        verifyIdentity(released);
        submitAndScreen(released); // hits created -> watchlist_hold
        for (WatchlistHit hit : hitRepositoryFor(released)) {
            complianceService.dispositionHit(complianceUser, hit.getId(),
                    HitDisposition.FALSE_POSITIVE.name(), "Screening match is a namesake; verified with supporting data.");
        }
        complianceService.resolveHold(complianceUser, released.application().getId(), "", "");
        stepUp(rachelUser, OtpPurpose.APPROVE);
        decisionService.approve(rachelUser, sessionOf(rachelUser, OtpPurpose.APPROVE), released.application().getId(),
                "Identity and documents verified; account approved.");

        // --- declined by officer (2)
        SeedCtx declined1 = createApplicant("tomas.garcia@example.sg", "Tomás García", LocalDate.of(1990, 1, 25),
                "S9012357R", "+65 80890123", central, EmploymentStatus.EMPLOYED, "Airbnb", "65000");
        uploadAndVerify(declined1, DocType.NATIONAL_ID, rachelUser);
        verifyIdentity(declined1);
        submitAndScreen(declined1);
        stepUp(rachelUser, OtpPurpose.DECLINE);
        decisionService.decline(rachelUser, sessionOf(rachelUser, OtpPurpose.DECLINE),
                declined1.application().getId(),
                "We could not verify your identity details against official records.",
                "NRIC details inconsistent across submissions; escalated for review.");

        SeedCtx declined2 = createApplicant("grace.koh@example.sg", "Grace Koh", LocalDate.of(1997, 6, 6),
                "S9712358S", "+65 80901234", east, EmploymentStatus.EMPLOYED, "Ninja Van", "50000");
        uploadAndVerify(declined2, DocType.NATIONAL_ID, nurulUser());
        verifyIdentity(declined2);
        submitAndScreen(declined2);
        stepUp(nurulUser(), OtpPurpose.DECLINE);
        decisionService.decline(nurulUser(), sessionOf(nurulUser(), OtpPurpose.DECLINE),
                declined2.application().getId(),
                "Your application does not meet our current account-opening criteria.",
                "Bureau profile outside risk appetite; declined under officer authority.");

        // --- declined via confirmed watchlist hit (1)
        SeedCtx hitDeclined = createApplicant("nadia.alrashid@example.sg", "Nadia Al-Rashid", LocalDate.of(1979, 11, 2),
                "S7912359T", "+65 80012345", west, EmploymentStatus.EMPLOYED, "SATS", "58000");
        uploadAndVerify(hitDeclined, DocType.PASSPORT, farahUser());
        verifyIdentity(hitDeclined);
        submitAndScreen(hitDeclined); // -> watchlist_hold
        for (WatchlistHit hit : hitRepositoryFor(hitDeclined)) {
            complianceService.dispositionHit(complianceUser, hit.getId(),
                    HitDisposition.CONFIRMED.name(),
                    "Match corroborated with sanctions list identifiers and passport details.");
        }
        complianceService.resolveHold(complianceUser, hitDeclined.application().getId(),
                "We were unable to proceed with your application due to a sanctions screening match.",
                "Confirmed UN sanctions hit; declined under compliance authority.");

        // --- watchlist_hold with pending hits (1)
        SeedCtx holdPending = createApplicant("ivan.norvik2@example.sg", "Ivan Norvik", LocalDate.of(1976, 3, 14),
                "S7612360U", "+65 80123456", east, EmploymentStatus.EMPLOYED, "Jurong Port", "62000");
        uploadAndVerify(holdPending, DocType.NATIONAL_ID, nurulUser());
        verifyIdentity(holdPending);
        submitAndScreen(holdPending); // stays watchlist_hold

        // --- watchlist_hold with an escalated hit (1)
        SeedCtx holdEscalated = createApplicant("nadia.alrashid2@example.sg", "Nadia Al-Rashid", LocalDate.of(1980, 5, 20),
                "S8012361V", "+65 80234567", central, EmploymentStatus.EMPLOYED, "Changi Airport Group", "59000");
        uploadAndVerify(holdEscalated, DocType.PASSPORT, rachelUser);
        verifyIdentity(holdEscalated);
        submitAndScreen(holdEscalated); // -> watchlist_hold
        for (WatchlistHit hit : hitRepositoryFor(holdEscalated)) {
            complianceService.dispositionHit(complianceUser, hit.getId(),
                    HitDisposition.ESCALATED.name(), "Requires senior review against the source list.");
        }

        // --- documents_requested via officer request (3)
        SeedCtx r1 = createApplicant("muthu.krishnan@example.sg", "Muthu Krishnan", LocalDate.of(1981, 8, 8),
                "S8112362W", "+65 80345678", central, EmploymentStatus.EMPLOYED, "SMRT", "56000");
        uploadOnly(r1, DocType.NATIONAL_ID);
        Document r1Identity = documentRepository.findByApplicationIdOrderByCreatedAtAsc(r1.application().getId()).get(0);
        documentService.reject(rachelUser, r1Identity.getId()); // rejected while in_progress: stays
        uploadAndVerify(r1, DocType.PASSPORT, rachelUser); // applicant replaces it
        verifyIdentity(r1);
        journeyService.submit(r1.user());
        screeningService.screen(applicationRepository.findById(r1.application().getId()).orElseThrow()); // -> under_review
        decisionService.requestDocuments(rachelUser, r1.application().getId(),
                List.of(DocType.ADDRESS_PROOF.name()),
                "Please provide a recent utility bill or bank statement as address proof.");

        SeedCtx r2 = createApplicant("hannah.schmidt@example.sg", "Hannah Schmidt", LocalDate.of(1992, 10, 30),
                "S9212363X", "+65 80456789", east, EmploymentStatus.EMPLOYED, "Siemens", "76000");
        uploadAndVerify(r2, DocType.NATIONAL_ID, nurulUser());
        verifyIdentity(r2);
        journeyService.submit(r2.user());
        screeningService.screen(applicationRepository.findById(r2.application().getId()).orElseThrow()); // -> under_review
        decisionService.requestDocuments(nurulUser(), r2.application().getId(),
                List.of(DocType.ADDRESS_PROOF.name()),
                "The address document was unclear; please upload a fresh one.");
        uploadOnly(r2, DocType.ADDRESS_PROOF); // uploaded, NOT verified -> stays documents_requested

        SeedCtx r3 = createApplicant("abdul.rahman@example.sg", "Abdul Rahman Aziz", LocalDate.of(1986, 12, 12),
                "S8612364Y", "+65 80567890", west, EmploymentStatus.EMPLOYED, "PSA Corporation", "64000");
        uploadAndVerify(r3, DocType.NATIONAL_ID, farahUser());
        verifyIdentity(r3);
        journeyService.submit(r3.user());
        screeningService.screen(applicationRepository.findById(r3.application().getId()).orElseThrow()); // -> under_review
        decisionService.requestDocuments(farahUser(), r3.application().getId(),
                List.of(DocType.ADDRESS_PROOF.name()),
                "Please provide proof of your current residential address.");
        uploadAndVerify(r3, DocType.ADDRESS_PROOF, farahUser()); // verified but identity NOT re-run
        // -> stays documents_requested (identity re-verification pending)

        // --- under_review (4, incl. one with compliance tier override + one with tier_3 beyond Mei's authority)
        SeedCtx u1 = createApplicant("benjamin.ng@example.sg", "Benjamin Ng", LocalDate.of(1990, 4, 4),
                "S9012365Z", "+65 80678901", central, EmploymentStatus.EMPLOYED, "Keppel", "92000");
        uploadAndVerify(u1, DocType.NATIONAL_ID, rachelUser);
        uploadAndVerify(u1, DocType.ADDRESS_PROOF, rachelUser);
        verifyIdentity(u1);
        journeyService.submit(u1.user());
        screeningService.screen(applicationRepository.findById(u1.application().getId()).orElseThrow()); // under_review, assigned to officer

        SeedCtx u2 = createApplicant("aisha.yusof@example.sg", "Aisha Yusof", LocalDate.of(1991, 2, 2),
                "S9112366A", "+65 80789012", east, EmploymentStatus.EMPLOYED, "Mediacorp", "71000");
        uploadAndVerify(u2, DocType.NATIONAL_ID, nurulUser());
        uploadAndVerify(u2, DocType.ADDRESS_PROOF, nurulUser());
        verifyIdentity(u2);
        journeyService.submit(u2.user());
        screeningService.screen(applicationRepository.findById(u2.application().getId()).orElseThrow());

        SeedCtx u3 = createApplicant("karthik.nair@example.sg", "Karthik Nair", LocalDate.of(1989, 7, 7),
                "S8912367B", "+65 80890123", west, EmploymentStatus.EMPLOYED, "Ascendas", "84000");
        uploadAndVerify(u3, DocType.NATIONAL_ID, farahUser());
        uploadAndVerify(u3, DocType.ADDRESS_PROOF, farahUser());
        uploadAndVerify(u3, DocType.PROOF_OF_INCOME, farahUser());
        verifyIdentity(u3);
        journeyService.submit(u3.user());
        screeningService.screen(applicationRepository.findById(u3.application().getId()).orElseThrow());
        stepUp(complianceUser, OtpPurpose.KYC_TIER_OVERRIDE);
        complianceService.overrideTier(complianceUser, sessionOf(complianceUser, OtpPurpose.KYC_TIER_OVERRIDE),
                u3.application().getId(), "tier_2",
                "Enhanced due diligence applied; income documentation supports tier_2 only at this time.");

        // tier_3 applicant in East assigned to Mei (max tier_1): approval would be refused for her.
        SeedCtx u4 = createApplicant("vivian.low@example.sg", "Vivian Low", LocalDate.of(1988, 3, 3),
                "S8812368C", "+65 80901234", east, EmploymentStatus.EMPLOYED, "CapitaLand", "240000");
        uploadAndVerify(u4, DocType.PASSPORT, nurulUser());
        uploadAndVerify(u4, DocType.ADDRESS_PROOF, nurulUser());
        uploadAndVerify(u4, DocType.PROOF_OF_INCOME, nurulUser());
        verifyIdentity(u4);
        journeyService.submit(u4.user());
        screeningService.screen(applicationRepository.findById(u4.application().getId()).orElseThrow());
        u4.application().setAssignedOfficerId(mei.getId());
        applicationRepository.save(u4.application());

        // --- submitted (screening queued with a delay so the status is visible) (2)
        SeedCtx s1 = createApplicant("daniel.tan@example.sg", "Daniel Tan", LocalDate.of(1993, 5, 5),
                "S9312369D", "+65 81012345", central, EmploymentStatus.EMPLOYED, "StarHub", "55000");
        uploadAndVerify(s1, DocType.NATIONAL_ID, rachelUser);
        verifyIdentity(s1);
        journeyService.submit(s1.user());
        s1.application().setScreeningDueAt(Instant.now().plus(props.getSeed().getScreeningDelayForSeededSubmitted()));
        applicationRepository.save(s1.application());

        SeedCtx s2 = createApplicant("priya.raman@example.sg", "Priya Raman", LocalDate.of(1994, 8, 8),
                "S9412370E", "+65 81123456", east, EmploymentStatus.EMPLOYED, "Razer", "61000");
        uploadAndVerify(s2, DocType.NATIONAL_ID, nurulUser());
        verifyIdentity(s2);
        journeyService.submit(s2.user());
        s2.application().setScreeningDueAt(Instant.now().plus(props.getSeed().getScreeningDelayForSeededSubmitted()));
        applicationRepository.save(s2.application());

        // --- in_progress variants (4)
        // Demo prospect mid-journey: docs uploaded, identity pending.
        SeedCtx demo = createApplicant("prospect@meridian.demo", "Prospect Demo", LocalDate.of(1995, 9, 9),
                "S9512371F", "+65 81234567", central, EmploymentStatus.EMPLOYED, "Demo Corp", "65000");
        uploadOnly(demo, DocType.NATIONAL_ID);
        uploadOnly(demo, DocType.ADDRESS_PROOF);

        // Low identity match: cannot progress past identity_verified.
        SeedCtx low = createApplicant("lena.lowscore@example.sg", "Lena Lowscore", LocalDate.of(1996, 10, 10),
                "S9612372G", "+65 81345678", central, EmploymentStatus.EMPLOYED, "FairPrice", "47000");
        uploadOnly(low, DocType.NATIONAL_ID);
        verifyIdentity(low); // score 71 -> identity_verified NOT completed

        // Provider failure then retry success (exercises retry path).
        SeedCtx retry = createApplicant("farid.failsafe@example.sg", "Farid Failsafe", LocalDate.of(1997, 11, 11),
                "S9712373H", "+65 81456789", west, EmploymentStatus.EMPLOYED, "SBS Transit", "52000");
        uploadAndVerify(retry, DocType.NATIONAL_ID, farahUser());
        try {
            verifyIdentity(retry); // first attempt: provider 502
        } catch (Exception ignored) {
            // expected: provider fails once per name, retry succeeds
        }
        verifyIdentity(retry); // retry succeeds

        // Registered only: no mobile verification yet.
        SeedCtx fresh = createApplicant("markus.steiner@example.sg", "Markus Steiner", LocalDate.of(1998, 12, 12),
                "S9812374I", "+65 81567890", west, EmploymentStatus.EMPLOYED, "Bosch", "43000");
        fresh.user().setMobileVerifiedAt(null);
        userAccountRepository.save(fresh.user());
        journeyStepRepository.findByApplicationIdAndStepKey(fresh.application().getId(), JourneyStepKey.CONTACT_VERIFIED)
                .ifPresent(step -> {
                    step.setCompleted(false);
                    journeyStepRepository.save(step);
                });

        // --- abandoned (2) via the scheduled job logic
        SeedCtx ab1 = createApplicant("samuel.richards@example.sg", "Samuel Richards", LocalDate.of(1980, 1, 1),
                "S8012375J", "+65 81678901", central, EmploymentStatus.EMPLOYED, "AIA", "45000");
        uploadOnly(ab1, DocType.NATIONAL_ID);
        ab1.application().setLastActivityAt(Instant.now().minus(40, ChronoUnit.DAYS));
        applicationRepository.save(ab1.application());
        jobs.runAbandonment();
        SeedCtx ab2 = createApplicant("yuki.tanaka@example.sg", "Yuki Tanaka", LocalDate.of(1985, 6, 6),
                "S8512376K", "+65 81789012", east, EmploymentStatus.EMPLOYED, "Sony", "49000");
        uploadOnly(ab2, DocType.NATIONAL_ID);
        ab2.application().setLastActivityAt(Instant.now().minus(45, ChronoUnit.DAYS));
        applicationRepository.save(ab2.application());
        jobs.runAbandonment();

        // --- pre-approval document expiry -> documents_requested (1)
        SeedCtx p1 = createApplicant("chloe.tan@example.sg", "Chloe Tan", LocalDate.of(1991, 4, 14),
                "S9112377L", "+65 81890123", central, EmploymentStatus.EMPLOYED, "SingHealth", "74000");
        uploadAndVerify(p1, DocType.NATIONAL_ID, rachelUser);
        uploadAndVerify(p1, DocType.ADDRESS_PROOF, rachelUser);
        verifyIdentity(p1);
        journeyService.submit(p1.user());
        screeningService.screen(applicationRepository.findById(p1.application().getId()).orElseThrow()); // under_review
        backdateExpiry(p1, DocType.ADDRESS_PROOF);
        jobs.processExpiredDocuments(); // under_review -> documents_requested

        // --- approved-customer document expiry -> re-KYC (2)
        SeedCtx e1 = createApplicant("mohammed.faizal@example.sg", "Mohammed Faizal", LocalDate.of(1983, 8, 23),
                "S8312378M", "+65 81901234", east, EmploymentStatus.EMPLOYED, "SIA Engineering", "86000");
        uploadAndVerify(e1, DocType.NATIONAL_ID, nurulUser());
        uploadAndVerify(e1, DocType.ADDRESS_PROOF, nurulUser());
        verifyIdentity(e1);
        submitAndScreen(e1);
        stepUp(nurulUser(), OtpPurpose.APPROVE);
        decisionService.approve(nurulUser(), sessionOf(nurulUser(), OtpPurpose.APPROVE),
                e1.application().getId(), "Identity and documents verified; account approved.");
        upgradeToCustomer(e1);
        backdateExpiry(e1, DocType.NATIONAL_ID);
        jobs.processExpiredDocuments(); // approved -> documents_requested + case + account suspended
        // customer starts re-KYC and uploads a fresh (unverified) identity document
        ReKycCase e1Case = activeCase(e1);
        reKycService.uploadReKycDocument(e1.user(), e1Case.getId(), DocType.NATIONAL_ID.name(),
                "fresh-nric.pdf", "application/pdf", fakePdf("fresh"));

        SeedCtx e2 = createApplicant("angela.pereira@example.sg", "Angela Pereira", LocalDate.of(1984, 3, 18),
                "S8412379N", "+65 81098765", central, EmploymentStatus.EMPLOYED, "SPH Media", "53000");
        uploadAndVerify(e2, DocType.NATIONAL_ID, rachelUser);
        verifyIdentity(e2);
        submitAndScreen(e2);
        stepUp(rachelUser, OtpPurpose.APPROVE);
        decisionService.approve(rachelUser, sessionOf(rachelUser, OtpPurpose.APPROVE),
                e2.application().getId(), "Identity and documents verified; account approved.");
        upgradeToCustomer(e2);
        backdateExpiry(e2, DocType.NATIONAL_ID);
        jobs.processExpiredDocuments(); // approved -> documents_requested + case + suspended
        ReKycCase e2Case = activeCase(e2);
        reKycService.uploadReKycDocument(e2.user(), e2Case.getId(), DocType.NATIONAL_ID.name(),
                "fresh-nric.pdf", "application/pdf", fakePdf("fresh"));
        Document e2Doc = documentRepository.findByApplicationIdOrderByCreatedAtAsc(e2.application().getId()).stream()
                .filter(d -> e2Case.getId().equals(d.getReKycCaseId())).findFirst().orElseThrow();
        documentService.verify(rachelUser, e2Doc.getId()); // officer verifies fresh doc
        stepUp(e2.user(), OtpPurpose.REKYC_SUBMIT);
        reKycService.submit(e2.user(), sessionOf(e2.user(), OtpPurpose.REKYC_SUBMIT), e2Case.getId());
        // completed; application status intentionally unchanged (documents_requested), account active again

        // --- re-KYC campaign with due / in_progress / overdue / completed cases
        ReKycCampaign campaign = complianceService.createCampaign(complianceUser,
                "H2 2026 Periodic Refresh", "tier_2,tier_3 customers", null, 90);
        SeedCtx cDue = findApproved("customer@meridian.demo");
        complianceService.addCustomerToCampaign(complianceUser, campaign, cDue.user(),
                cDue.application(), Instant.now().plus(60, ChronoUnit.DAYS));
        SeedCtx cProgress = findApproved("noor.hayati@example.sg");
        ReKycCase cProgressCase = complianceService.addCustomerToCampaign(complianceUser, campaign, cProgress.user(),
                cProgress.application(), Instant.now().plus(30, ChronoUnit.DAYS));
        if (cProgressCase != null) {
            reKycService.uploadReKycDocument(cProgress.user(), cProgressCase.getId(),
                    DocType.NATIONAL_ID.name(), "fresh-nric.pdf", "application/pdf", fakePdf("fresh"));
        }
        SeedCtx cOverdue = findApproved("siti.aishah@example.sg");
        ReKycCase cOverdueCase = complianceService.addCustomerToCampaign(complianceUser, campaign, cOverdue.user(),
                cOverdue.application(), Instant.now().minus(3, ChronoUnit.DAYS));
        jobs.runReKycDue(); // -> overdue
        SeedCtx cDone = findApproved("rajan.subramaniam@example.sg");
        ReKycCase cDoneCase = complianceService.addCustomerToCampaign(complianceUser, campaign, cDone.user(),
                cDone.application(), Instant.now().plus(20, ChronoUnit.DAYS));
        if (cDoneCase != null) {
            for (DocType type : List.of(DocType.NATIONAL_ID, DocType.ADDRESS_PROOF, DocType.PROOF_OF_INCOME)) {
                reKycService.uploadReKycDocument(cDone.user(), cDoneCase.getId(),
                        type.name(), "fresh-" + type.name().toLowerCase() + ".pdf", "application/pdf", fakePdf("fresh"));
            }
            for (Document doneDoc : documentRepository.findByApplicationIdOrderByCreatedAtAsc(cDone.application().getId())) {
                if (cDoneCase.getId().equals(doneDoc.getReKycCaseId())) {
                    documentService.verify(rachelUser, doneDoc.getId());
                }
            }
            stepUp(cDone.user(), OtpPurpose.REKYC_SUBMIT);
            reKycService.submit(cDone.user(), sessionOf(cDone.user(), OtpPurpose.REKYC_SUBMIT), cDoneCase.getId());
        }

        log.info("Seeded applications: {}", applicationRepository.count());
        log.info("Seeded documents: {}", documentRepository.count());
    }

    // ------------------------------------------------------------------ helpers

    private List<WatchlistHit> hitRepositoryFor(SeedCtx ctx) {
        return watchlistHitRepository.findByApplicationIdOrderByCreatedAtAsc(ctx.application().getId());
    }

    private void backdateExpiry(SeedCtx ctx, DocType type) {
        for (Document document : documentRepository.findByApplicationIdOrderByCreatedAtAsc(ctx.application().getId())) {
            if (document.getDocType() == type && document.getVerificationState() == DocState.VERIFIED) {
                document.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
                documentRepository.save(document);
            }
        }
    }

    private ReKycCase activeCase(SeedCtx ctx) {
        return reKycCaseRepository.findFirstByCustomerUserIdAndStatusInOrderByCreatedAtDesc(
                        ctx.user().getId(), List.of(ReKycStatus.DUE, ReKycStatus.IN_PROGRESS, ReKycStatus.OVERDUE))
                .orElseThrow();
    }

    private void upgradeToCustomer(SeedCtx ctx) {
        // the role upgrade happens inside DecisionService.approve
        ctx.user().setRole(Role.CUSTOMER);
        userAccountRepository.save(ctx.user());
    }

    private SeedCtx findApproved(String email) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email).orElseThrow();
        Applicant applicant = applicantRepository.findByUserAccountId(user.getId()).orElseThrow();
        Application application = applicationRepository.findByApplicantId(applicant.getId()).orElseThrow();
        return new SeedCtx(user, applicant, application);
    }

    private void approve(SeedCtx ctx, UserAccount officerUser, StaffUser officer,
                         List<DocType> docs, String expectedTier) {
        for (DocType type : docs) {
            uploadAndVerify(ctx, type, officerUser);
        }
        verifyIdentity(ctx);
        submitAndScreen(ctx);
        String session = sessionOf(officerUser, OtpPurpose.APPROVE);
        stepUp(officerUser, OtpPurpose.APPROVE);
        decisionService.approve(officerUser, session, ctx.application().getId(),
                "Identity and documents verified; account approved.");
        upgradeToCustomer(ctx);
        Account account = accountRepository.findByApplicationId(ctx.application().getId()).orElseThrow();
        if (!expectedTier.equals(account.getKycTier())) {
            throw new IllegalStateException("Seed tier mismatch for " + ctx.application().getReference()
                    + ": expected " + expectedTier + " got " + account.getKycTier());
        }
    }

    private String sessionOf(UserAccount user, OtpPurpose purpose) {
        return "seed-session-" + user.getId() + "-" + purpose;
    }

    // demo officer account resolvers
    private UserAccount nurulUser() {
        return userAccountRepository.findByEmailIgnoreCase("officer.east@meridian.demo").orElseThrow();
    }

    private UserAccount jasonUser() {
        return userAccountRepository.findByEmailIgnoreCase("officer.east2@meridian.demo").orElseThrow();
    }

    private UserAccount meiUser() {
        return userAccountRepository.findByEmailIgnoreCase("officer.east1@meridian.demo").orElseThrow();
    }

    private UserAccount farahUser() {
        return userAccountRepository.findByEmailIgnoreCase("officer.west@meridian.demo").orElseThrow();
    }

    private UserAccount kevinUser() {
        return userAccountRepository.findByEmailIgnoreCase("officer.central@meridian.demo").orElseThrow();
    }
}
