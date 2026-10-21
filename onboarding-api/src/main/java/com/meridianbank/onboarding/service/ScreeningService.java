package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.client.ProviderMockClient;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Submission screening: watchlist screening with hit creation and credit bureau pull. Runs from
 * the scheduled screening job; failures leave the application submitted and are retried.
 */
@Service
public class ScreeningService {

    private final ProviderMockClient providerMockClient;
    private final ApplicationStateMachine stateMachine;
    private final WatchlistHitRepository hitRepository;
    private final BureauResultRepository bureauResultRepository;
    private final ApplicantRepository applicantRepository;
    private final StaffUserRepository staffUserRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final KycTierService kycTierService;

    public ScreeningService(ProviderMockClient providerMockClient,
                            ApplicationStateMachine stateMachine,
                            WatchlistHitRepository hitRepository,
                            BureauResultRepository bureauResultRepository,
                            ApplicantRepository applicantRepository,
                            StaffUserRepository staffUserRepository,
                            ApplicationRepository applicationRepository,
                            AuditService auditService,
                            NotificationService notificationService,
                            OutboxEmailService outboxEmailService,
                            KycTierService kycTierService) {
        this.providerMockClient = providerMockClient;
        this.stateMachine = stateMachine;
        this.hitRepository = hitRepository;
        this.bureauResultRepository = bureauResultRepository;
        this.applicantRepository = applicantRepository;
        this.staffUserRepository = staffUserRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.kycTierService = kycTierService;
    }

    /** Screens one submitted application: watchlist + bureau, then hold or review. */
    @Transactional
    public void screen(Application application) {
        if (application.getStatus() != AppStatus.SUBMITTED) {
            return;
        }
        Applicant applicant = applicantRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new IllegalStateException("Applicant missing"));
        try {
            ProviderMockClient.WatchlistResult watchlist = providerMockClient.screenWatchlist(
                    applicant.getFullName(),
                    applicant.getDateOfBirth() == null ? "" : applicant.getDateOfBirth().toString(),
                    "SG");
            for (ProviderMockClient.WatchlistMatch match : watchlist.matches()) {
                WatchlistHit hit = new WatchlistHit();
                hit.setApplicationId(application.getId());
                hit.setMatchedName(match.matchedName());
                hit.setListSource(match.listSource());
                hit.setConfidence(match.confidence());
                hit.setDisposition(HitDisposition.PENDING);
                hit.setCreatedAt(Instant.now());
                hitRepository.save(hit);
            }

            BureauResult bureau = new BureauResult();
            bureau.setApplicationId(application.getId());
            bureau.setStatus("SUCCESS");
            bureau.setPulledAt(Instant.now());
            try {
                ProviderMockClient.BureauScore score = providerMockClient.pullBureau(
                        applicant.getNationalIdentifier(), applicant.getFullName(),
                        applicant.getDateOfBirth() == null ? "" : applicant.getDateOfBirth().toString());
                bureau.setScore(score.score());
                bureau.setLiabilities(toJson(score.liabilities()));
            } catch (ApiException e) {
                bureau.setStatus("PROVIDER_ERROR");
            }
            bureauResultRepository.save(bureau);

            application.setScreeningAttempts(application.getScreeningAttempts() + 1);
            application.setScreeningError(null);

            if (stateMachine.hasNonFalsePositiveHits(application)) {
                stateMachine.screenToHold(application);
                auditService.recordSystem("system:screening", "SCREENING_HITS_CREATED", "Application",
                        application.getReference(), null, "watchlist_hold", "SCREENING_WATCHLIST_HIT");
                notificationService.notifyAllCompliance(NotificationType.ACTION_REQUIRED,
                        "Watchlist hold: " + application.getReference(),
                        "An application matched a watchlist entry and requires compliance disposition.",
                        "/compliance/hits");
                notificationService.notifyApplicant(application, NotificationType.INFO,
                        "Application received",
                        "Your application has been received and is being processed.",
                        "/journey/status");
            } else {
                stateMachine.screenToReview(application);
                assignOfficer(application);
                auditService.recordSystem("system:screening", "SCREENING_CLEAR", "Application",
                        application.getReference(), "submitted", "under_review", "SCREENING_CLEAR");
                notificationService.notifyBranchOfficers(application, NotificationType.INFO,
                        "New application for review: " + application.getReference(),
                        "A screened application is waiting for officer review.",
                        "/officer/applications/" + application.getId());
                notificationService.notifyApplicant(application, NotificationType.INFO,
                        "Application under review",
                        "Screening completed and your application is now under officer review.",
                        "/journey/status");
            }
            stateMachine.rederive(application, "SCREENING");
        } catch (ApiException e) {
            application.setScreeningError(e.getMessage());
            application.setScreeningAttempts(application.getScreeningAttempts() + 1);
            applicationRepository.save(application);
            auditService.recordSystem("system:screening", "SCREENING_FAILED", "Application",
                    application.getReference(), null, "{\"error\":\"" + e.getMessage() + "\"}",
                    "SCHEDULED_JOB_RETRY");
        }
    }

    /** Officer assignment by branch: the active officer with the fewest open reviews gets it. */
    @Transactional
    public void assignOfficer(Application application) {
        List<StaffUser> officers = staffUserRepository
                .findByHomeBranchIdOrderByFullNameAsc(application.getBranchId()).stream()
                .filter(s -> s.getRole() == Role.ONBOARDING_OFFICER && s.getStatus() == UserStatus.ACTIVE)
                .toList();
        if (officers.isEmpty()) {
            return;
        }
        Map<Long, Long> openCounts = applicationRepository
                .findByStatusOrderByCreatedAtAsc(AppStatus.UNDER_REVIEW).stream()
                .filter(a -> a.getAssignedOfficerId() != null)
                .collect(Collectors.groupingBy(Application::getAssignedOfficerId, Collectors.counting()));
        StaffUser chosen = officers.stream()
                .min((a, b) -> Long.compare(openCounts.getOrDefault(a.getId(), 0L),
                        openCounts.getOrDefault(b.getId(), 0L)))
                .orElse(officers.get(0));
        application.setAssignedOfficerId(chosen.getId());
        applicationRepository.save(application);
    }

    private String toJson(List<ProviderMockClient.Liability> liabilities) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(liabilities);
        } catch (Exception e) {
            return "[]";
        }
    }
}
