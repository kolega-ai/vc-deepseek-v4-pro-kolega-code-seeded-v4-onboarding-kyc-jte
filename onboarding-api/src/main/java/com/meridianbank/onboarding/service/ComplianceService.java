package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Compliance workflows: watchlist hit disposition, release/decline from hold, KYC tier override
 * (with mandatory justification and step-up OTP) and re-KYC campaign creation with CSV import.
 * Compliance acts across all branches but has no endpoint that creates or edits captured
 * applicant data.
 */
@Service
public class ComplianceService {

    private final AccessControlService accessControlService;
    private final ApplicationStateMachine stateMachine;
    private final OtpService otpService;
    private final ApplicationRepository applicationRepository;
    private final WatchlistHitRepository hitRepository;
    private final AccountRepository accountRepository;
    private final ApplicantRepository applicantRepository;
    private final UserAccountRepository userAccountRepository;
    private final ReKycCampaignRepository campaignRepository;
    private final ReKycCaseRepository caseRepository;
    private final KycTierPolicyRepository policyRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final WebhookService webhookService;
    private final ScreeningService screeningService;

    public ComplianceService(AccessControlService accessControlService,
                             ApplicationStateMachine stateMachine,
                             OtpService otpService,
                             ApplicationRepository applicationRepository,
                             WatchlistHitRepository hitRepository,
                             AccountRepository accountRepository,
                             ApplicantRepository applicantRepository,
                             UserAccountRepository userAccountRepository,
                             ReKycCampaignRepository campaignRepository,
                             ReKycCaseRepository caseRepository,
                             KycTierPolicyRepository policyRepository,
                             AuditService auditService,
                             NotificationService notificationService,
                             OutboxEmailService outboxEmailService,
                             WebhookService webhookService,
                             ScreeningService screeningService) {
        this.accessControlService = accessControlService;
        this.stateMachine = stateMachine;
        this.otpService = otpService;
        this.applicationRepository = applicationRepository;
        this.hitRepository = hitRepository;
        this.accountRepository = accountRepository;
        this.applicantRepository = applicantRepository;
        this.userAccountRepository = userAccountRepository;
        this.campaignRepository = campaignRepository;
        this.caseRepository = caseRepository;
        this.policyRepository = policyRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.webhookService = webhookService;
        this.screeningService = screeningService;
    }

    // ----------------------------------------------------------- watchlist dispositions

    @Transactional
    public WatchlistHit dispositionHit(UserAccount user, Long hitId, String dispositionName, String justification) {
        StaffUser staff = accessControlService.requireCompliance(user);
        WatchlistHit hit = hitRepository.findById(hitId).orElseThrow(() -> ApiException.notFound("Watchlist hit"));
        HitDisposition target;
        try {
            target = HitDisposition.valueOf(dispositionName);
        } catch (Exception e) {
            throw ApiException.badRequest("Unknown disposition");
        }
        boolean legal = switch (hit.getDisposition()) {
            case PENDING -> target == HitDisposition.FALSE_POSITIVE
                    || target == HitDisposition.CONFIRMED
                    || target == HitDisposition.ESCALATED;
            case ESCALATED -> target == HitDisposition.FALSE_POSITIVE || target == HitDisposition.CONFIRMED;
            // CONFIRMED and FALSE_POSITIVE are meant to be terminal, but escalating a closed hit
            // back into the active queue was left legal so senior review could reopen one without
            // a separate endpoint. That also lets any compliance user route an already CONFIRMED
            // or FALSE_POSITIVE hit back through ESCALATED and re-disposition it (e.g.
            // CONFIRMED -> ESCALATED -> FALSE_POSITIVE), silently overwriting a closed decision.
            case CONFIRMED, FALSE_POSITIVE -> target == HitDisposition.ESCALATED;
        };
        if (!legal) {
            throw ApiException.invalidState("Disposition " + hit.getDisposition().getLabel()
                    + " cannot move to " + target.getLabel());
        }
        if (target == HitDisposition.CONFIRMED
                && (justification == null || justification.trim().length() < 20)) {
            throw ApiException.badRequest("A confirmed disposition requires a justification of at least 20 characters");
        }
        String before = hit.getDisposition().name();
        hit.setDisposition(target);
        hit.setDisposedBy(staff.getId());
        hit.setDisposedAt(java.time.Instant.now());
        hit.setJustification(justification == null ? null : justification.trim());
        hitRepository.save(hit);
        auditService.recordUser(user, "WATCHLIST_HIT_DISPOSITIONED", "WatchlistHit", String.valueOf(hitId),
                before, target.name(), "COMPLIANCE_BRANCH_ALL");

        Application application = accessControlService.requireApplication(hit.getApplicationId());
        stateMachine.rederive(application, "WATCHLIST_DISPOSITION");
        return hit;
    }

    /**
     * Applies a batch of dispositions delivered by the upstream case-management feed. Used when the
     * vendor system closes several hits at once at the end of a review cycle.
     */
    @Transactional
    public int applyDispositionFeed(UserAccount user, DispositionFeed feed) {
        StaffUser staff = accessControlService.requireCompliance(user);
        int updated = 0;
        if (feed == null || feed.items() == null) {
            return 0;
        }
        for (DispositionFeedItem item : feed.items()) {
            if (item == null || item.hitId() == null || item.disposition() == null) {
                continue;
            }
            WatchlistHit hit = hitRepository.findById(item.hitId()).orElse(null);
            if (hit == null) {
                continue;
            }
            try {
                hit.setDisposition(HitDisposition.valueOf(item.disposition()));
            } catch (Exception e) {
                continue;
            }
            hit.setDisposedBy(staff.getId());
            hit.setDisposedAt(java.time.Instant.now());
            hit.setJustification(item.justification());
            hitRepository.save(hit);
            updated++;
        }
        return updated;
    }

    /** watchlist_hold -> under_review / declined depending on dispositions. */
    @Transactional
    public Application resolveHold(UserAccount user, Long applicationId,
                                   String customerVisibleReason, String internalReason) {
        StaffUser staff = accessControlService.requireCompliance(user);
        Application application = accessControlService.requireApplication(applicationId);
        if (application.getStatus() != AppStatus.WATCHLIST_HOLD) {
            throw ApiException.invalidState("Application is not on watchlist hold");
        }
        if (stateMachine.anyHitPendingOrEscalated(application)) {
            throw ApiException.invalidState("All hits must be dispositioned (no pending or escalated hits)");
        }
        if (stateMachine.hasConfirmedHit(application)) {
            com.meridianbank.onboarding.auth.Validators.validateRequiredText(
                    "Customer-visible reason", customerVisibleReason, 5, 500);
            com.meridianbank.onboarding.auth.Validators.validateRequiredText(
                    "Internal reason", internalReason, 5, 500);
            stateMachine.complianceDeclineFromHold(application, staff,
                    customerVisibleReason.trim(), internalReason.trim());
            notificationService.notifyApplicant(application, NotificationType.WARNING,
                    "A decision was made on your application",
                    customerVisibleReason.trim(), "/journey/status");
            outboxEmailService.send(applicantEmail(application), "Update on your Meridian Bank application",
                    "Application " + application.getReference() + ": " + customerVisibleReason.trim(), "decline");
            webhookService.deliver("application.declined", application.getReference(),
                    "{\"reference\":\"" + application.getReference() + "\"}");
        } else {
            stateMachine.complianceRelease(application, staff);
            screeningService.assignOfficer(application);
            notificationService.notifyBranchOfficers(application, NotificationType.INFO,
                    "Application released from hold: " + application.getReference(),
                    "All watchlist hits were cleared as false positives; the application is under review.",
                    "/officer/applications/" + application.getId());
        }
        return application;
    }

    // ----------------------------------------------------------- KYC tier override

    @Transactional
    public Application overrideTier(UserAccount user, String sessionId, Long applicationId,
                                    String tier, String justification) {
        StaffUser staff = accessControlService.requireCompliance(user);
        otpService.requireStepUp(user, sessionId, OtpPurpose.KYC_TIER_OVERRIDE);
        if (justification == null || justification.trim().length() < 20) {
            throw ApiException.badRequest("A tier override requires a justification of at least 20 characters");
        }
        Application application = accessControlService.requireApplication(applicationId);
        if (application.getStatus() == AppStatus.DECLINED || application.getStatus() == AppStatus.ABANDONED) {
            throw ApiException.invalidState("The tier of this application cannot be overridden");
        }
        if (policyRepository.findByTierCode(tier).isEmpty()) {
            throw ApiException.badRequest("Unknown KYC tier");
        }
        String before = application.effectiveKycTier();
        application.setOverriddenKycTier(tier);
        applicationRepository.save(application);
        auditService.recordUser(user, "KYC_TIER_OVERRIDDEN", "Application", application.getReference(),
                before, tier, "COMPLIANCE_BRANCH_ALL+STEP_UP_KYC_TIER_OVERRIDE+JUSTIFICATION");
        accountRepository.findByApplicationId(application.getId()).ifPresent(account -> {
            auditService.recordUser(user, "ACCOUNT_TIER_UPDATED", "Account", account.getAccountNumber(),
                    account.getKycTier(), tier, "COMPLIANCE_BRANCH_ALL+STEP_UP_KYC_TIER_OVERRIDE");
            account.setKycTier(tier);
            accountRepository.save(account);
        });
        notificationService.notifyApplicant(application, NotificationType.INFO,
                "Your account tier was updated",
                "Your assigned KYC tier is now " + tier + ". Limits follow the tier policy.",
                "/customer/dashboard");
        webhookService.deliver("kyc_tier.overridden", application.getReference(),
                "{\"reference\":\"" + application.getReference() + "\",\"kyc_tier\":\"" + tier + "\"}");
        return application;
    }

    @Transactional
    public Application clearOverride(UserAccount user, String sessionId, Long applicationId, String justification) {
        StaffUser staff = accessControlService.requireCompliance(user);
        otpService.requireStepUp(user, sessionId, OtpPurpose.KYC_TIER_OVERRIDE);
        if (justification == null || justification.trim().length() < 20) {
            throw ApiException.badRequest("Clearing an override requires a justification of at least 20 characters");
        }
        Application application = accessControlService.requireApplication(applicationId);
        if (application.getOverriddenKycTier() == null) {
            throw ApiException.invalidState("No override is set for this application");
        }
        String before = application.effectiveKycTier();
        application.setOverriddenKycTier(null);
        applicationRepository.save(application);
        stateMachine.rederive(application, "OVERRIDE_CLEARED");
        auditService.recordUser(user, "KYC_TIER_OVERRIDE_CLEARED", "Application", application.getReference(),
                before, application.effectiveKycTier(),
                "COMPLIANCE_BRANCH_ALL+STEP_UP_KYC_TIER_OVERRIDE+JUSTIFICATION");
        return application;
    }

    // ----------------------------------------------------------- re-KYC campaigns

    @Transactional
    public ReKycCampaign createCampaign(UserAccount user, String name, String targetSegment,
                                        String targetKycTier, Integer dueDays) {
        StaffUser staff = accessControlService.requireCompliance(user);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("Campaign name", name, 3, 160);
        if (dueDays == null || dueDays < 7 || dueDays > 365) {
            throw ApiException.badRequest("Due period must be between 7 and 365 days");
        }
        if (targetKycTier != null && policyRepository.findByTierCode(targetKycTier).isEmpty()) {
            throw ApiException.badRequest("Unknown target KYC tier");
        }
        ReKycCampaign campaign = new ReKycCampaign();
        campaign.setName(name.trim());
        campaign.setTargetSegment(targetSegment);
        campaign.setTargetKycTier(targetKycTier);
        campaign.setDueDays(dueDays);
        campaign.setCreatedBy(staff.getId());
        campaign.setCreatedAt(java.time.Instant.now());
        campaignRepository.save(campaign);
        auditService.recordUser(user, "REKYC_CAMPAIGN_CREATED", "ReKycCampaign", String.valueOf(campaign.getId()),
                null, name.trim(), "COMPLIANCE_BRANCH_ALL");
        return campaign;
    }

    /** Adds a customer to a campaign (also used by CSV import). */
    @Transactional
    public ReKycCase addCustomerToCampaign(UserAccount actor, ReKycCampaign campaign, UserAccount customer,
                                           Application application, java.time.Instant dueDate) {
        boolean exists = caseRepository.findByCampaignIdOrderByCreatedAtAsc(campaign.getId()).stream()
                .anyMatch(c -> c.getCustomerUserId().equals(customer.getId())
                        && c.getStatus() != ReKycStatus.COMPLETED);
        if (exists) {
            return null;
        }
        ReKycCase kase = new ReKycCase();
        kase.setCampaignId(campaign.getId());
        kase.setOrigin(ReKycOrigin.CAMPAIGN);
        kase.setCustomerUserId(customer.getId());
        kase.setApplicationId(application.getId());
        kase.setStatus(ReKycStatus.DUE);
        kase.setDueDate(dueDate);
        kase.setCreatedAt(java.time.Instant.now());
        caseRepository.save(kase);
        notificationService.notify(customer.getId(), NotificationType.INFO,
                "Periodic re-KYC due",
                "Campaign \"" + campaign.getName() + "\" requires a document refresh by "
                        + dueDate + ".",
                "/customer/rekyc");
        return kase;
    }

    private String applicantEmail(Application application) {
        return applicantRepository.findById(application.getApplicantId())
                .map(Applicant::getEmail).orElse("");
    }

    public record HitDispositionForm(String disposition, String justification) {
    }

    public record DispositionFeed(java.util.List<DispositionFeedItem> items) {
    }

    public record DispositionFeedItem(Long hitId, String disposition, String justification) {
    }

    public record TierOverrideForm(String tier, String justification) {
    }
}
