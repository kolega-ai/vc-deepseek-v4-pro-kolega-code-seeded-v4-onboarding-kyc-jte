package com.meridianbank.onboarding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.client.IdentityRequestXmlBuilder;
import com.meridianbank.onboarding.client.ProviderMockClient;
import com.meridianbank.onboarding.client.ProviderResponseXmlParser;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.AppStatus;
import com.meridianbank.onboarding.domain.enums.JourneyStepKey;
import com.meridianbank.onboarding.domain.enums.NotificationType;
import com.meridianbank.onboarding.domain.repo.IdentityVerificationResultRepository;
import com.meridianbank.onboarding.domain.repo.JourneyStepRepository;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Identity verification against the provider mock. The provider accepts and returns XML; the raw
 * XML response is retained with the result. A score below 80 does not complete the
 * identity_verified step, so the application cannot progress past it. Provider failures leave a
 * retryable error result.
 */
@Service
public class IdentityVerificationService {

    private final ProviderMockClient providerMockClient;
    private final ProviderResponseXmlParser responseParser;
    private final IdentityVerificationResultRepository resultRepository;
    private final JourneyStepRepository journeyStepRepository;
    private final AuthService authService;
    private final ApplicationStateMachine stateMachine;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public IdentityVerificationService(ProviderMockClient providerMockClient,
                                       IdentityVerificationResultRepository resultRepository,
                                       JourneyStepRepository journeyStepRepository,
                                       AuthService authService,
                                       ApplicationStateMachine stateMachine,
                                       AuditService auditService,
                                       NotificationService notificationService,
                                       ObjectMapper objectMapper) {
        this.providerMockClient = providerMockClient;
        this.responseParser = new ProviderResponseXmlParser();
        this.resultRepository = resultRepository;
        this.journeyStepRepository = journeyStepRepository;
        this.authService = authService;
        this.stateMachine = stateMachine;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IdentityVerificationResult run(UserAccount user) {
        Application application = authService.applicationOf(user);
        if (application.getStatus() != AppStatus.IN_PROGRESS
                && application.getStatus() != AppStatus.DOCUMENTS_REQUESTED) {
            throw ApiException.invalidState("Identity verification is only available while the application is editable");
        }
        Applicant applicant = authService.applicantOf(user);
        if (applicant.getFullName() == null || applicant.getNationalIdentifier() == null) {
            throw ApiException.invalidState("Personal details must be completed first");
        }
        JourneyStep documentsStep = journeyStepRepository
                .findByApplicationIdAndStepKey(application.getId(), JourneyStepKey.DOCUMENTS_UPLOADED)
                .orElseThrow(() -> new IllegalStateException("Journey step missing"));
        if (!documentsStep.isCompleted()) {
            throw ApiException.invalidState("All required documents must be uploaded before identity verification");
        }

        String requestXml = IdentityRequestXmlBuilder.build(
                applicant.getFullName(),
                applicant.getNationalIdentifier(),
                applicant.getDateOfBirth() == null ? "" : applicant.getDateOfBirth().toString());
        String responseXml;
        try {
            responseXml = providerMockClient.verifyIdentityXml(requestXml);
        } catch (ApiException e) {
            IdentityVerificationResult failed = recordFailure(application, e.getMessage());
            auditService.recordUser(user, "IDENTITY_PROVIDER_ERROR", "Application", application.getReference(),
                    null, "{\"error\":\"" + e.getMessage() + "\"}", "APPLICANT_OWNER");
            throw e;
        }

        ProviderResponseXmlParser.IdentityResponse parsed = responseParser.parse(responseXml);
        IdentityVerificationResult result = new IdentityVerificationResult();
        result.setApplicationId(application.getId());
        result.setProviderReference(parsed.providerReference());
        result.setMatchScore(parsed.matchScore());
        result.setOutcome(parsed.outcome());
        result.setRawXml(responseXml);
        result.setStatus(IdentityVerificationResult.STATUS_SUCCESS);
        result.setCreatedAt(Instant.now());
        resultRepository.save(result);

        auditService.recordUser(user, "IDENTITY_VERIFICATION_RUN", "Application", application.getReference(),
                null, "{\"score\":" + parsed.matchScore() + ",\"outcome\":\"" + parsed.outcome() + "\"}",
                "APPLICANT_OWNER");

        if (parsed.matchScore() >= 80) {
            JourneyStep step = journeyStepRepository
                    .findByApplicationIdAndStepKey(application.getId(), JourneyStepKey.IDENTITY_VERIFIED)
                    .orElseThrow(() -> new IllegalStateException("Journey step missing"));
            if (!step.isCompleted()) {
                step.setCompleted(true);
                step.setCompletedAt(Instant.now());
                try {
                    Map<String, Object> captured = new LinkedHashMap<>();
                    captured.put("score", parsed.matchScore());
                    captured.put("provider_reference", parsed.providerReference());
                    step.setCapturedData(objectMapper.writeValueAsString(captured));
                } catch (Exception ignored) {
                    // captured data is best-effort
                }
                journeyStepRepository.save(step);
            }
            notificationService.notify(user.getId(), NotificationType.SUCCESS,
                    "Identity verified",
                    "Your identity verification passed with a match score of " + parsed.matchScore() + ".",
                    "/journey/identity");
        } else {
            notificationService.notify(user.getId(), NotificationType.WARNING,
                    "Identity verification below threshold",
                    "The identity match score was " + parsed.matchScore()
                            + ", below the required 80. The application cannot progress until it passes.",
                    "/journey/identity");
        }
        application.setLastActivityAt(Instant.now());
        stateMachine.rederive(application, "IDENTITY_RESULT");
        return result;
    }

    private IdentityVerificationResult recordFailure(Application application, String error) {
        IdentityVerificationResult result = new IdentityVerificationResult();
        result.setApplicationId(application.getId());
        result.setStatus(IdentityVerificationResult.STATUS_PROVIDER_ERROR);
        result.setErrorMessage(error == null ? "Provider unavailable" : error);
        result.setCreatedAt(Instant.now());
        return resultRepository.save(result);
    }
}
