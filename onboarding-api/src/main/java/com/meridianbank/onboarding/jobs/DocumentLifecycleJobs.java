package com.meridianbank.onboarding.jobs;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.NotificationService;
import com.meridianbank.onboarding.service.OutboxEmailService;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled jobs: document expiry cascade, abandonment, re-KYC due-dating and housekeeping.
 * The expiry cascade is also invokable directly (the seeder drives it to produce seeded states).
 */
@Component
public class DocumentLifecycleJobs {

    private static final Logger log = LoggerFactory.getLogger(DocumentLifecycleJobs.class);

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final AccountRepository accountRepository;
    private final ReKycCaseRepository reKycCaseRepository;
    private final ApplicationStateMachine stateMachine;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final OtpChallengeRepository otpChallengeRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final StepUpGrantRepository stepUpGrantRepository;
    private final OutboxEmailRepository outboxEmailRepository;

    public DocumentLifecycleJobs(DocumentRepository documentRepository,
                                 ApplicationRepository applicationRepository,
                                 ApplicantRepository applicantRepository,
                                 AccountRepository accountRepository,
                                 ReKycCaseRepository reKycCaseRepository,
                                 ApplicationStateMachine stateMachine,
                                 AuditService auditService,
                                 NotificationService notificationService,
                                 OutboxEmailService outboxEmailService,
                                 OtpChallengeRepository otpChallengeRepository,
                                 PasswordResetTokenRepository resetTokenRepository,
                                 StepUpGrantRepository stepUpGrantRepository,
                                 OutboxEmailRepository outboxEmailRepository) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.accountRepository = accountRepository;
        this.reKycCaseRepository = reKycCaseRepository;
        this.stateMachine = stateMachine;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.otpChallengeRepository = otpChallengeRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.stepUpGrantRepository = stepUpGrantRepository;
        this.outboxEmailRepository = outboxEmailRepository;
    }

    @Scheduled(cron = "${app.jobs.document-expiry-cron}")
    public void runDocumentExpiry() {
        processExpiredDocuments();
    }

    /** Marks verified documents past their expiry date expired and cascades per the edge list. */
    @Transactional
    public int processExpiredDocuments() {
        List<Document> expired = documentRepository
                .findByVerificationStateAndExpiryDateLessThan(DocState.VERIFIED, Instant.now());
        for (Document document : expired) {
            Application application = applicationRepository.findById(document.getApplicationId()).orElse(null);
            if (application == null) {
                continue;
            }
            document.setVerificationState(DocState.EXPIRED);
            documentRepository.save(document);
            auditService.recordSystem("system:documents", "DOCUMENT_EXPIRED", "Document",
                    String.valueOf(document.getId()), "verified", "expired", "SCHEDULED_JOB_DOCUMENT_EXPIRY");
            stateMachine.rederive(application, "DOCUMENT_EXPIRED");

            String message = "Your " + document.getDocType().getLabel()
                    + " has expired; a fresh document is required.";
            switch (application.getStatus()) {
                case UNDER_REVIEW -> {
                    stateMachine.systemRequestDocumentsOnProblem(application, document.getDocType().name(), message);
                    notifyApplicant(application, message, "/journey/documents");
                }
                case APPROVED -> {
                    stateMachine.systemRequestDocumentsOnExpiry(application, document.getDocType().name());
                    openExpiryReKycCase(application);
                    accountRepository.findByApplicationId(application.getId()).ifPresent(account -> {
                        if (account.getStatus() == AccountState.ACTIVE) {
                            auditService.recordSystem("system:documents", "ACCOUNT_SUSPENDED", "Account",
                                    account.getAccountNumber(), "active", "suspended",
                                    "SCHEDULED_JOB_DOCUMENT_EXPIRY");
                            account.setStatus(AccountState.SUSPENDED);
                            accountRepository.save(account);
                        }
                    });
                    notifyApplicant(application,
                            "A document expired. Your account is temporarily suspended until you complete a re-KYC refresh.",
                            "/customer/rekyc");
                    notificationService.notifyAllCompliance(NotificationType.ACTION_REQUIRED,
                            "Re-KYC triggered by document expiry",
                            "Application " + application.getReference()
                                    + " had a document expire; a re-KYC case was opened.",
                            "/compliance/rekyc");
                }
                case DOCUMENTS_REQUESTED -> notifyApplicant(application, message, "/journey/documents");
                case IN_PROGRESS -> notifyApplicant(application, message, "/journey/documents");
                default -> log.info("Document {} expired on application {} (status {})",
                        document.getId(), application.getReference(), application.getStatus());
            }
        }
        return expired.size();
    }

    /** Opens a document-expiry re-KYC case (or reuses the open one). */
    private void openExpiryReKycCase(Application application) {
        boolean exists = reKycCaseRepository
                .findByApplicationIdAndStatusIn(application.getId(),
                        List.of(ReKycStatus.DUE, ReKycStatus.IN_PROGRESS, ReKycStatus.OVERDUE))
                .isPresent();
        if (exists) {
            return;
        }
        applicantUser(application).ifPresent(userId -> {
            ReKycCase kase = new ReKycCase();
            kase.setOrigin(ReKycOrigin.DOC_EXPIRY);
            kase.setCustomerUserId(userId);
            kase.setApplicationId(application.getId());
            kase.setStatus(ReKycStatus.DUE);
            kase.setDueDate(Instant.now().plus(14, ChronoUnit.DAYS));
            kase.setCreatedAt(Instant.now());
            reKycCaseRepository.save(kase);
            auditService.recordSystem("system:documents", "REKYC_CASE_OPENED", "ReKycCase",
                    String.valueOf(kase.getId()), null, "doc_expiry", "SCHEDULED_JOB_DOCUMENT_EXPIRY");
            notificationService.notify(userId, NotificationType.ACTION_REQUIRED,
                    "Re-KYC required",
                    "A document on your account expired. Please complete the document refresh.",
                    "/customer/rekyc");
        });
    }

    private void notifyApplicant(Application application, String message, String link) {
        notificationService.notifyApplicant(application, NotificationType.ACTION_REQUIRED,
                "Document expired", message, link);
    }

    private java.util.Optional<Long> applicantUser(Application application) {
        return applicantRepository.findById(application.getApplicantId()).map(Applicant::getUserAccountId);
    }

    @Scheduled(cron = "${app.jobs.abandonment-cron}")
    @Transactional
    public void runAbandonment() {
        var candidates = applicationRepository
                .findInactiveInProgress(Instant.now().minus(30, ChronoUnit.DAYS));
        for (Application application : candidates) {
            stateMachine.systemAbandon(application);
            auditService.recordSystem("system:jobs", "APPLICATION_ABANDONED", "Application",
                    application.getReference(), "in_progress", "abandoned", "SCHEDULED_JOB_ABANDONMENT");
            notificationService.notifyApplicant(application, NotificationType.WARNING,
                    "Application closed",
                    "Your application was closed after 30 days without activity.",
                    "/journey/status");
        }
    }

    @Scheduled(cron = "${app.jobs.rekyc-due-cron}")
    @Transactional
    public void runReKycDue() {
        for (ReKycCase kase : reKycCaseRepository.findByStatusOrderByDueDateAsc(ReKycStatus.DUE)) {
            if (kase.getDueDate() != null && kase.getDueDate().isBefore(Instant.now())) {
                kase.setStatus(ReKycStatus.OVERDUE);
                reKycCaseRepository.save(kase);
                auditService.recordSystem("system:jobs", "REKYC_OVERDUE", "ReKycCase",
                        String.valueOf(kase.getId()), "due", "overdue", "SCHEDULED_JOB_REKYC_DUE");
                notificationService.notify(kase.getCustomerUserId(), NotificationType.WARNING,
                        "Re-KYC overdue",
                        "Your document refresh is overdue. Please complete it as soon as possible.",
                        "/customer/rekyc");
            }
        }
        for (ReKycCase kase : reKycCaseRepository.findByStatusOrderByDueDateAsc(ReKycStatus.IN_PROGRESS)) {
            if (kase.getDueDate() != null && kase.getDueDate().isBefore(Instant.now())) {
                kase.setStatus(ReKycStatus.OVERDUE);
                reKycCaseRepository.save(kase);
                auditService.recordSystem("system:jobs", "REKYC_OVERDUE", "ReKycCase",
                        String.valueOf(kase.getId()), "in_progress", "overdue", "SCHEDULED_JOB_REKYC_DUE");
            }
        }
    }

    @Scheduled(cron = "${app.jobs.housekeeping-cron}")
    @Transactional
    public void runHousekeeping() {
        Instant now = Instant.now();
        otpChallengeRepository.findAll().stream()
                .filter(c -> c.getExpiresAt().isBefore(now.minus(1, ChronoUnit.DAYS)))
                .forEach(otpChallengeRepository::delete);
        resetTokenRepository.findAll().stream()
                .filter(t -> t.getExpiresAt().isBefore(now.minus(1, ChronoUnit.DAYS)) || t.getConsumedAt() != null)
                .forEach(resetTokenRepository::delete);
        stepUpGrantRepository.findAll().stream()
                .filter(g -> g.getExpiresAt().isBefore(now))
                .forEach(stepUpGrantRepository::delete);
        // Local mock MTA: mark pending outbox messages as sent.
        for (OutboxEmail email : outboxEmailRepository.findAll()) {
            if (OutboxEmailService.STATUS_PENDING.equals(email.getStatus())) {
                email.setStatus(OutboxEmailService.STATUS_SENT);
                email.setSentAt(now);
                outboxEmailRepository.save(email);
            }
        }
    }
}
