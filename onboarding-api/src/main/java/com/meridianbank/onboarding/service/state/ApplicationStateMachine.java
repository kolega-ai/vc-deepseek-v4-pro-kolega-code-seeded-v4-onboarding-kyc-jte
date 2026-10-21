package com.meridianbank.onboarding.service.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.KycTierService;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The explicit application state machine. Every allowed transition is enumerated here as an edge;
 * anything not in this list is impossible by construction. Each public method validates the edge
 * and its guard conditions, applies the change and writes an append-only audit event.
 *
 * Edge list (from the spec):
 *   in_progress -> submitted        (applicant; all steps complete, required docs verified, identity >= 80)
 *   submitted -> watchlist_hold     (system; any non-false_positive hit at screening)
 *   submitted -> under_review       (system; no hit or all hits false_positive)
 *   watchlist_hold -> under_review  (compliance; all hits dispositioned false_positive)
 *   watchlist_hold -> declined      (compliance; any hit confirmed)
 *   under_review -> documents_requested (officer)
 *   under_review -> documents_requested (system; a document rejected or expired pre-approval)
 *   documents_requested -> under_review (system; requested docs re-uploaded and re-verified, identity re-verified)
 *   under_review -> approved        (officer; derived tier <= officer max tier, no confirmed hit, identity verified)
 *   under_review -> declined        (officer)
 *   in_progress -> abandoned        (scheduled job; no applicant activity for 30 days)
 *   approved -> documents_requested (system; a document expired for an approved customer)
 */
@Service
public class ApplicationStateMachine {

    private static final Duration ABANDONMENT_INACTIVITY = Duration.ofDays(30);

    private record Edge(AppStatus from, AppStatus to) {
    }

    private static final Set<Edge> ALLOWED_EDGES = Set.of(
            new Edge(AppStatus.IN_PROGRESS, AppStatus.SUBMITTED),
            new Edge(AppStatus.SUBMITTED, AppStatus.WATCHLIST_HOLD),
            new Edge(AppStatus.SUBMITTED, AppStatus.UNDER_REVIEW),
            new Edge(AppStatus.WATCHLIST_HOLD, AppStatus.UNDER_REVIEW),
            new Edge(AppStatus.WATCHLIST_HOLD, AppStatus.DECLINED),
            new Edge(AppStatus.UNDER_REVIEW, AppStatus.DOCUMENTS_REQUESTED),
            new Edge(AppStatus.DOCUMENTS_REQUESTED, AppStatus.UNDER_REVIEW),
            new Edge(AppStatus.UNDER_REVIEW, AppStatus.APPROVED),
            new Edge(AppStatus.UNDER_REVIEW, AppStatus.DECLINED),
            new Edge(AppStatus.IN_PROGRESS, AppStatus.ABANDONED),
            new Edge(AppStatus.APPROVED, AppStatus.DOCUMENTS_REQUESTED)
    );

    private final AuditService auditService;
    private final KycTierService kycTierService;
    private final ApplicationRepository applicationRepository;
    private final JourneyStepRepository journeyStepRepository;
    private final DocumentRepository documentRepository;
    private final WatchlistHitRepository hitRepository;
    private final IdentityVerificationResultRepository identityRepository;
    private final ApplicantRepository applicantRepository;
    private final ObjectMapper objectMapper;

    public ApplicationStateMachine(AuditService auditService,
                                   KycTierService kycTierService,
                                   ApplicationRepository applicationRepository,
                                   JourneyStepRepository journeyStepRepository,
                                   DocumentRepository documentRepository,
                                   WatchlistHitRepository hitRepository,
                                   IdentityVerificationResultRepository identityRepository,
                                   ApplicantRepository applicantRepository,
                                   ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.kycTierService = kycTierService;
        this.applicationRepository = applicationRepository;
        this.journeyStepRepository = journeyStepRepository;
        this.documentRepository = documentRepository;
        this.hitRepository = hitRepository;
        this.identityRepository = identityRepository;
        this.applicantRepository = applicantRepository;
        this.objectMapper = objectMapper;
    }

    // ------------------------------------------------------------------ guards

    public boolean allStepsComplete(Application app) {
        List<JourneyStep> steps = journeyStepRepository.findByApplicationIdOrderByStepKeyAsc(app.getId());
        return JourneyStepKey.values().length == steps.size() && steps.stream().allMatch(JourneyStep::isCompleted);
    }

    public List<String> missingStepLabels(Application app) {
        Set<JourneyStepKey> completed = journeyStepRepository.findByApplicationIdOrderByStepKeyAsc(app.getId())
                .stream().filter(JourneyStep::isCompleted).map(JourneyStep::getStepKey).collect(Collectors.toSet());
        List<String> missing = new ArrayList<>();
        for (JourneyStepKey key : JourneyStepKey.values()) {
            if (!completed.contains(key)) {
                missing.add(key.getLabel());
            }
        }
        return missing;
    }

    private IdentityVerificationResult latestIdentity(Application app) {
        return identityRepository.findTopByApplicationIdOrderByCreatedAtDesc(app.getId()).orElse(null);
    }

    public boolean identityPassed(Application app) {
        IdentityVerificationResult latest = latestIdentity(app);
        return latest != null && latest.getMatchScore() != null && latest.getMatchScore() >= 80;
    }

    public boolean hasNonFalsePositiveHits(Application app) {
        return hitRepository.findByApplicationIdOrderByCreatedAtAsc(app.getId()).stream()
                .anyMatch(h -> h.getDisposition() != HitDisposition.FALSE_POSITIVE);
    }

    public boolean hasConfirmedHit(Application app) {
        return hitRepository.findByApplicationIdOrderByCreatedAtAsc(app.getId()).stream()
                .anyMatch(h -> h.getDisposition() == HitDisposition.CONFIRMED);
    }

    public boolean allHitsFalsePositive(Application app) {
        List<WatchlistHit> hits = hitRepository.findByApplicationIdOrderByCreatedAtAsc(app.getId());
        return !hits.isEmpty() && hits.stream().allMatch(h -> h.getDisposition() == HitDisposition.FALSE_POSITIVE);
    }

    public boolean anyHitPendingOrEscalated(Application app) {
        return hitRepository.findByApplicationIdOrderByCreatedAtAsc(app.getId()).stream()
                .anyMatch(h -> h.getDisposition() == HitDisposition.PENDING || h.getDisposition() == HitDisposition.ESCALATED);
    }

    /** Requested documents (types recorded on the application) are re-uploaded and re-verified. */
    public boolean requestedDocumentsResatisfied(Application app) {
        List<String> requested = requestedTypes(app);
        if (requested.isEmpty() || app.getRequestedAt() == null) {
            return false;
        }
        List<Document> verified = documentRepository
                .findByApplicationIdAndVerificationState(app.getId(), DocState.VERIFIED);
        for (String type : requested) {
            boolean satisfied = verified.stream().anyMatch(d ->
                    d.getDocType().name().equals(type)
                            && d.getCreatedAt() != null
                            && !d.getCreatedAt().isBefore(app.getRequestedAt()));
            if (!satisfied) {
                return false;
            }
        }
        return identityReVerified(app);
    }

    /** The identity_verified step must have been re-completed after the return to documents. */
    public boolean identityReVerified(Application app) {
        if (!identityPassed(app)) {
            return false;
        }
        Optional<JourneyStep> step = journeyStepRepository
                .findByApplicationIdAndStepKey(app.getId(), JourneyStepKey.IDENTITY_VERIFIED);
        if (step.isEmpty() || !step.get().isCompleted()) {
            return false;
        }
        if (app.getRequestedAt() != null && step.get().getCompletedAt() != null
                && step.get().getCompletedAt().isBefore(app.getRequestedAt())) {
            return false;
        }
        return true;
    }

    private List<String> requestedTypes(Application app) {
        if (app.getRequestedDocumentTypes() == null || app.getRequestedDocumentTypes().isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(app.getRequestedDocumentTypes(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean hasProblemDocument(Application app) {
        return documentRepository.findByApplicationIdOrderByCreatedAtAsc(app.getId()).stream()
                .anyMatch(d -> d.getVerificationState() == DocState.REJECTED || d.getVerificationState() == DocState.EXPIRED);
    }

    // ------------------------------------------------------------------ transitions

    /** in_progress -> submitted (applicant). */
    public void submit(Application app, UserAccount applicant) {
        requireEdge(app.getStatus(), AppStatus.SUBMITTED);
        if (!allStepsComplete(app)) {
            throw ApiException.invalidState("Not all journey steps are complete");
        }
        if (!identityPassed(app)) {
            throw ApiException.invalidState("Identity verification must pass (match score >= 80) before submission");
        }
        String tier = kycTierService.deriveTier(app);
        if (KycTierService.TIER_0.equals(tier)) {
            throw ApiException.invalidState(
                    "Submission requires a verified identity document (and proof of income where applicable); "
                            + "derived tier is tier_0");
        }
        Instant now = Instant.now();
        apply(app, AppStatus.SUBMITTED, "APPLICATION_SUBMITTED",
                "APPLICANT_OWNER", applicant.getEmail(), applicant.getId(), null);
        app.setSubmittedAt(now);
        app.setLastActivityAt(now);
        applicationRepository.save(app);
    }

    /** submitted -> watchlist_hold (system screening). */
    public void screenToHold(Application app) {
        requireEdge(app.getStatus(), AppStatus.WATCHLIST_HOLD);
        if (!hasNonFalsePositiveHits(app)) {
            throw ApiException.invalidState("No non-false_positive watchlist hit present");
        }
        apply(app, AppStatus.WATCHLIST_HOLD, "SCREENING_WATCHLIST_HOLD",
                "SCREENING_WATCHLIST_HIT", "system:screening", null, null);
        applicationRepository.save(app);
    }

    /** submitted -> under_review (system screening, clean). */
    public void screenToReview(Application app) {
        requireEdge(app.getStatus(), AppStatus.UNDER_REVIEW);
        if (hasNonFalsePositiveHits(app)) {
            throw ApiException.invalidState("Cannot clear screening while non-false_positive hits remain");
        }
        apply(app, AppStatus.UNDER_REVIEW, "SCREENING_CLEAR_TO_REVIEW",
                "SCREENING_CLEAR", "system:screening", null, null);
        applicationRepository.save(app);
    }

    /** watchlist_hold -> under_review (compliance: all hits false_positive). */
    public void complianceRelease(Application app, StaffUser compliance) {
        requireEdge(app.getStatus(), AppStatus.UNDER_REVIEW);
        if (!allHitsFalsePositive(app)) {
            throw ApiException.invalidState("All watchlist hits must be dispositioned false_positive to release");
        }
        apply(app, AppStatus.UNDER_REVIEW, "WATCHLIST_RELEASED",
                "COMPLIANCE_BRANCH_ALL", compliance.getFullName(), compliance.getId(), null);
        applicationRepository.save(app);
    }

    /** watchlist_hold -> declined (compliance: any confirmed hit). */
    public void complianceDeclineFromHold(Application app, StaffUser compliance,
                                          String customerVisibleReason, String internalReason) {
        requireEdge(app.getStatus(), AppStatus.DECLINED);
        if (!hasConfirmedHit(app)) {
            throw ApiException.invalidState("A confirmed watchlist hit is required to decline from hold");
        }
        apply(app, AppStatus.DECLINED, "APPLICATION_DECLINED",
                "COMPLIANCE_CONFIRMED_HIT", compliance.getFullName(), compliance.getId(), null);
        app.setDecisionOutcome(DecisionOutcome.DECLINED);
        app.setCustomerVisibleReason(customerVisibleReason);
        app.setInternalReason(internalReason);
        app.setDecidedAt(Instant.now());
        applicationRepository.save(app);
    }

    /** under_review -> documents_requested (officer request). */
    public void officerRequestDocuments(Application app, StaffUser officer, List<String> types, String message) {
        requireEdge(app.getStatus(), AppStatus.DOCUMENTS_REQUESTED);
        if (types == null || types.isEmpty()) {
            throw ApiException.badRequest("At least one document type must be requested");
        }
        apply(app, AppStatus.DOCUMENTS_REQUESTED, "DOCUMENTS_REQUESTED",
                "OFFICER_BRANCH_SCOPE", officer.getFullName(), officer.getId(), null);
        recordRequest(app, types, message);
        reenterDocumentsStep(app);
    }

    /** under_review -> documents_requested (system: document rejected or expired, pre-approval). */
    public void systemRequestDocumentsOnProblem(Application app, String docType, String message) {
        requireEdge(app.getStatus(), AppStatus.DOCUMENTS_REQUESTED);
        if (!hasProblemDocument(app)) {
            throw ApiException.invalidState("No rejected/expired document present");
        }
        apply(app, AppStatus.DOCUMENTS_REQUESTED, "DOCUMENTS_REQUESTED_DOC_PROBLEM",
                "SYSTEM_DOC_PROBLEM", "system:documents", null, null);
        recordRequest(app, List.of(docType), message);
        reenterDocumentsStep(app);
    }

    /** approved -> documents_requested (system: document expired for an approved customer). */
    public void systemRequestDocumentsOnExpiry(Application app, String docType) {
        requireEdge(app.getStatus(), AppStatus.DOCUMENTS_REQUESTED);
        apply(app, AppStatus.DOCUMENTS_REQUESTED, "DOCUMENTS_REQUESTED_EXPIRY",
                "SYSTEM_DOC_EXPIRY", "system:documents", null, null);
        recordRequest(app, List.of(docType), "A document expired; fresh documents are required (re-KYC).");
        reenterDocumentsStep(app);
    }

    /** documents_requested -> under_review (system: requested docs re-verified and identity re-verified). */
    public void systemReenterReview(Application app) {
        requireEdge(app.getStatus(), AppStatus.UNDER_REVIEW);
        if (!requestedDocumentsResatisfied(app)) {
            throw ApiException.invalidState("Requested documents must be re-uploaded, re-verified and identity re-verified");
        }
        apply(app, AppStatus.UNDER_REVIEW, "REENTER_REVIEW",
                "SYSTEM_REENTRY_CHECK", "system:journey", null, null);
        applicationRepository.save(app);
    }

    /** under_review -> approved (officer). Guards: branch scope + tier authority checked by caller. */
    public void officerApprove(Application app, StaffUser officer, String customerVisibleReason) {
        requireEdge(app.getStatus(), AppStatus.APPROVED);
        if (hasConfirmedHit(app)) {
            throw ApiException.invalidState("A confirmed watchlist hit blocks approval");
        }
        if (!identityReVerified(app)) {
            throw ApiException.invalidState("Identity verification must be current (step re-verified, score >= 80)");
        }
        apply(app, AppStatus.APPROVED, "APPLICATION_APPROVED",
                "OFFICER_BRANCH_SCOPE+STEP_UP_APPROVE+TIER_AUTHORITY", officer.getFullName(), officer.getId(), null);
        app.setDecisionOutcome(DecisionOutcome.APPROVED);
        app.setCustomerVisibleReason(customerVisibleReason);
        app.setDecidedAt(Instant.now());
        applicationRepository.save(app);
    }

    /** under_review -> declined (officer). */
    public void officerDecline(Application app, StaffUser officer, String customerVisibleReason, String internalReason) {
        requireEdge(app.getStatus(), AppStatus.DECLINED);
        apply(app, AppStatus.DECLINED, "APPLICATION_DECLINED",
                "OFFICER_BRANCH_SCOPE+STEP_UP_DECLINE", officer.getFullName(), officer.getId(), null);
        app.setDecisionOutcome(DecisionOutcome.DECLINED);
        app.setCustomerVisibleReason(customerVisibleReason);
        app.setInternalReason(internalReason);
        app.setDecidedAt(Instant.now());
        applicationRepository.save(app);
    }

    /** in_progress -> abandoned (scheduled job). */
    public void systemAbandon(Application app) {
        requireEdge(app.getStatus(), AppStatus.ABANDONED);
        Instant cutoff = Instant.now().minus(ABANDONMENT_INACTIVITY);
        if (app.getLastActivityAt() != null && app.getLastActivityAt().isAfter(cutoff)) {
            throw ApiException.invalidState("Application is still active");
        }
        apply(app, AppStatus.ABANDONED, "APPLICATION_ABANDONED",
                "SCHEDULED_JOB_ABANDONMENT", "system:jobs", null, null);
        applicationRepository.save(app);
    }

    // ------------------------------------------------------------------ helpers

    private void requireEdge(AppStatus from, AppStatus to) {
        if (!ALLOWED_EDGES.contains(new Edge(from, to))) {
            throw ApiException.invalidState("Illegal transition " + from + " -> " + to);
        }
    }

    private void apply(Application app, AppStatus target, String action, String basis,
                       String actor, Long actorId, Long userActorId) {
        String before = snapshot(app);
        app.setStatus(target);
        app.setDerivedKycTier(kycTierService.deriveTier(app));
        applicationRepository.save(app);
        String after = snapshot(app);
        auditService.record(userActorId != null ? "USER" : "SYSTEM", actor, userActorId != null ? userActorId : actorId,
                action, "Application", app.getReference(), before, after, basis);
    }

    private void recordRequest(Application app, List<String> types, String message) {
        try {
            app.setRequestedDocumentTypes(objectMapper.writeValueAsString(types));
        } catch (Exception ignored) {
            app.setRequestedDocumentTypes("[]");
        }
        app.setRequestedMessage(message);
        app.setRequestedAt(Instant.now());
    }

    /** Re-entry at the document step clears documents_uploaded and identity_verified. */
    private void reenterDocumentsStep(Application app) {
        for (JourneyStepKey key : List.of(JourneyStepKey.DOCUMENTS_UPLOADED, JourneyStepKey.IDENTITY_VERIFIED)) {
            journeyStepRepository.findByApplicationIdAndStepKey(app.getId(), key).ifPresent(step -> {
                if (step.isCompleted()) {
                    auditService.recordSystem("system:journey", "STEP_CLEARED", "JourneyStep", key.getLabel(),
                            "completed", "cleared", "SYSTEM_REENTRY");
                    step.setCompleted(false);
                    step.setCompletedAt(null);
                    journeyStepRepository.save(step);
                }
            });
        }
        app.setCurrentStep(JourneyStepKey.DOCUMENTS_UPLOADED);
    }

    /** Re-derives and persists the tier; writes an audit event when the value changes. */
    public void rederive(Application app, String reason) {
        String before = app.getDerivedKycTier();
        String derived = kycTierService.deriveTier(app);
        if (!derived.equals(before)) {
            app.setDerivedKycTier(derived);
            applicationRepository.save(app);
            auditService.recordSystem("system:kyc", "KYC_TIER_REDERIVED", "Application", app.getReference(),
                    before, derived, reason);
        }
    }

    private String snapshot(Application app) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("reference", app.getReference());
            map.put("status", app.getStatus().name());
            map.put("derived_kyc_tier", app.getDerivedKycTier());
            map.put("overridden_kyc_tier", app.getOverriddenKycTier());
            map.put("decision_outcome", app.getDecisionOutcome() == null ? null : app.getDecisionOutcome().name());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"status\":\"" + app.getStatus().name() + "\"}";
        }
    }
}
