package com.meridianbank.onboarding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.auth.AuthService;
import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.ApplicationRepository;
import com.meridianbank.onboarding.domain.repo.JourneyStepRepository;
import com.meridianbank.onboarding.domain.repo.ReKycCaseRepository;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The applicant journey: ordered step completion with server-side prerequisite enforcement,
 * save-and-resume data capture, submission and re-submission.
 */
@Service
public class JourneyService {

    private final AuthService authService;
    private final OtpService otpService;
    private final ApplicationStateMachine stateMachine;
    private final ApplicationRepository applicationRepository;
    private final JourneyStepRepository journeyStepRepository;
    private final ReKycCaseRepository reKycCaseRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final KycTierService kycTierService;
    private final AppProperties props;
    private final ObjectMapper objectMapper;

    public JourneyService(AuthService authService,
                          OtpService otpService,
                          ApplicationStateMachine stateMachine,
                          ApplicationRepository applicationRepository,
                          JourneyStepRepository journeyStepRepository,
                          ReKycCaseRepository reKycCaseRepository,
                          UserAccountRepository userAccountRepository,
                          AuditService auditService,
                          NotificationService notificationService,
                          KycTierService kycTierService,
                          AppProperties props,
                          ObjectMapper objectMapper) {
        this.authService = authService;
        this.otpService = otpService;
        this.stateMachine = stateMachine;
        this.applicationRepository = applicationRepository;
        this.journeyStepRepository = journeyStepRepository;
        this.reKycCaseRepository = reKycCaseRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.kycTierService = kycTierService;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    // ------------------------------------------------------------------ contact verification (mobile OTP)

    @Transactional
    public void issueMobileOtp(UserAccount user, String sessionId) {
        Application application = authService.applicationOf(user);
        ensureOwner(user, application);
        if (application.getStatus() != AppStatus.IN_PROGRESS) {
            throw ApiException.invalidState("Mobile verification is only available during the application journey");
        }
        if (user.getMobileVerifiedAt() != null) {
            throw ApiException.invalidState("Mobile is already verified");
        }
        otpService.issueSessionOtp(user, sessionId, OtpService.PURPOSE_MOBILE_VERIFY);
    }

    @Transactional
    public Application verifyMobileOtp(UserAccount user, String sessionId, String code) {
        Application application = authService.applicationOf(user);
        ensureOwner(user, application);
        if (application.getStatus() != AppStatus.IN_PROGRESS) {
            throw ApiException.invalidState("Mobile verification is only available during the application journey");
        }
        if (user.getMobileVerifiedAt() != null) {
            return application;
        }
        otpService.verifySessionOtp(user, sessionId, OtpService.PURPOSE_MOBILE_VERIFY, code);
        user.setMobileVerifiedAt(Instant.now());
        userAccountRepository.save(user);
        authService.completeStep(application, JourneyStepKey.CONTACT_VERIFIED,
                "{\"email_verified\":true,\"mobile_verified\":true}");
        application.setCurrentStep(JourneyStepKey.PERSONAL_DETAILS);
        application.setLastActivityAt(Instant.now());
        applicationRepository.save(application);
        auditService.recordUser(user, "CONTACT_VERIFIED", "Application", application.getReference(),
                "pending", "verified", "APPLICANT_OWNER+OTP_MOBILE");
        notificationService.notify(user.getId(), NotificationType.SUCCESS,
                "Mobile verified",
                "Your mobile number is verified. You can now continue with your application.",
                "/journey/personal");
        return application;
    }

    // ------------------------------------------------------------------ journey steps

    @Transactional
    public Application submitPersonal(UserAccount user, PersonalDetailsForm form) {
        Application application = editableApplication(user);
        requireStepComplete(application, JourneyStepKey.CONTACT_VERIFIED, "Contact verification must be completed first");
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("Full name", form.fullName(), 2, 120);
        if (form.dateOfBirth() == null) {
            throw ApiException.badRequest("Date of birth is required");
        }
        if (form.dateOfBirth().isAfter(LocalDate.now())) {
            throw ApiException.badRequest("Date of birth cannot be in the future");
        }
        if (Period.between(form.dateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw ApiException.badRequest("You must be at least 18 years old to open an account");
        }
        com.meridianbank.onboarding.auth.Validators.validateRequiredText(
                "National identifier", form.nationalIdentifier(), 3, 40);

        Applicant applicant = authService.applicantOf(user);
        String before = snapshotApplicant(applicant);
        applicant.setFullName(form.fullName().trim());
        applicant.setDateOfBirth(form.dateOfBirth());
        applicant.setNationalIdentifier(form.nationalIdentifier().trim().toUpperCase());
        applicant.setUpdatedAt(Instant.now());
        authService.completeStep(application, JourneyStepKey.PERSONAL_DETAILS, capturedOf(form));
        application.setCurrentStep(JourneyStepKey.ADDRESS_DETAILS);
        application.setLastActivityAt(Instant.now());
        applicationRepository.save(application);
        auditService.recordUser(user, "PERSONAL_DETAILS_SUBMITTED", "Application", application.getReference(),
                before, snapshotApplicant(applicant), "APPLICANT_OWNER");
        return application;
    }

    @Transactional
    public Application submitAddress(UserAccount user, AddressDetailsForm form) {
        Application application = editableApplication(user);
        requireStepComplete(application, JourneyStepKey.PERSONAL_DETAILS, "Personal details must be completed first");
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("Address line 1", form.addressLine1(), 3, 160);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("City", form.city(), 2, 80);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("Postal code", form.postalCode(), 3, 12);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("Country", form.country(), 2, 60);

        Applicant applicant = authService.applicantOf(user);
        String before = snapshotApplicant(applicant);
        applicant.setAddressLine1(form.addressLine1().trim());
        applicant.setAddressLine2(form.addressLine2() == null ? null : form.addressLine2().trim());
        applicant.setCity(form.city().trim());
        applicant.setPostalCode(form.postalCode().trim());
        applicant.setCountry(form.country().trim());
        applicant.setUpdatedAt(Instant.now());
        authService.completeStep(application, JourneyStepKey.ADDRESS_DETAILS, capturedOf(form));
        application.setCurrentStep(JourneyStepKey.EMPLOYMENT_INCOME);
        application.setLastActivityAt(Instant.now());
        applicationRepository.save(application);
        auditService.recordUser(user, "ADDRESS_DETAILS_SUBMITTED", "Application", application.getReference(),
                before, snapshotApplicant(applicant), "APPLICANT_OWNER");
        return application;
    }

    @Transactional
    public Application submitEmployment(UserAccount user, EmploymentIncomeForm form) {
        Application application = editableApplication(user);
        requireStepComplete(application, JourneyStepKey.ADDRESS_DETAILS, "Address details must be completed first");
        EmploymentStatus status;
        try {
            status = EmploymentStatus.valueOf(form.employmentStatus());
        } catch (Exception e) {
            throw ApiException.badRequest("Unknown employment status");
        }
        if ((status == EmploymentStatus.EMPLOYED || status == EmploymentStatus.SELF_EMPLOYED)
                && (form.employerName() == null || form.employerName().isBlank())) {
            throw ApiException.badRequest("Employer name is required for this employment status");
        }
        if (form.declaredAnnualIncome() == null
                || form.declaredAnnualIncome().compareTo(BigDecimal.ZERO) < 0
                || form.declaredAnnualIncome().compareTo(new BigDecimal("10000000")) > 0
                || form.declaredAnnualIncome().scale() > 2) {
            throw ApiException.badRequest("Declared annual income must be a non-negative amount (max 10,000,000.00 SGD)");
        }

        Applicant applicant = authService.applicantOf(user);
        String before = snapshotApplicant(applicant);
        applicant.setEmploymentStatus(status);
        applicant.setEmployerName(form.employerName() == null ? null : form.employerName().trim());
        applicant.setDeclaredAnnualIncome(form.declaredAnnualIncome());
        applicant.setUpdatedAt(Instant.now());
        authService.completeStep(application, JourneyStepKey.EMPLOYMENT_INCOME, capturedOf(form));
        application.setCurrentStep(JourneyStepKey.DOCUMENTS_UPLOADED);
        application.setLastActivityAt(Instant.now());
        applicationRepository.save(application);
        auditService.recordUser(user, "EMPLOYMENT_INCOME_SUBMITTED", "Application", application.getReference(),
                before, snapshotApplicant(applicant), "APPLICANT_OWNER");
        stateMachine.rederive(application, "EMPLOYMENT_SUBMITTED");
        return application;
    }

    // ------------------------------------------------------------------ submission

    @Transactional
    public Application submit(UserAccount user) {
        Application application = authService.applicationOf(user);
        ensureOwner(user, application);
        if (application.getStatus() != AppStatus.IN_PROGRESS) {
            throw ApiException.invalidState("Application cannot be submitted from status "
                    + application.getStatus().getLabel());
        }
        stateMachine.submit(application, user);
        application.setScreeningDueAt(Instant.now().plus(props.getScreening().getQueueDelay()));
        applicationRepository.save(application);
        notificationService.notify(user.getId(), NotificationType.SUCCESS,
                "Application submitted",
                "Application " + application.getReference()
                        + " was submitted. Screening runs automatically within a minute.",
                "/journey/status");
        return application;
    }

    /** Re-submission after documents were re-uploaded and re-verified. */
    @Transactional
    public Application resubmit(UserAccount user) {
        Application application = authService.applicationOf(user);
        ensureOwner(user, application);
        if (application.getStatus() != AppStatus.DOCUMENTS_REQUESTED) {
            throw ApiException.invalidState("Nothing to re-submit: application is "
                    + application.getStatus().getLabel());
        }
        // Re-KYC flows (document expiry on approved accounts) are completed through the re-KYC
        // case, never through a re-submission.
        if (reKycCaseRepository.findByApplicationIdAndStatusIn(application.getId(),
                java.util.List.of(ReKycStatus.DUE, ReKycStatus.IN_PROGRESS, ReKycStatus.OVERDUE)).isPresent()) {
            throw ApiException.invalidState(
                    "Your account needs a re-KYC document refresh; complete it from the re-KYC section.");
        }
        stateMachine.systemReenterReview(application);
        notificationService.notifyBranchOfficers(application, NotificationType.INFO,
                "Re-submitted application: " + application.getReference(),
                "Requested documents were re-verified; the application is back under review.",
                "/officer/applications/" + application.getId());
        return application;
    }

    // ------------------------------------------------------------------ helpers

    public Application applicationFor(UserAccount user) {
        return authService.applicationOf(user);
    }

    private void ensureOwner(UserAccount user, Application application) {
        if (!user.getRole().isApplicantSide()
                || !application.getApplicantId().equals(authService.applicantOf(user).getId())) {
            throw ApiException.forbidden("You do not own this application");
        }
    }

    private Application editableApplication(UserAccount user) {
        if (!user.getRole().isApplicantSide()) {
            throw ApiException.forbidden("Applicant account required");
        }
        Application application = authService.applicationOf(user);
        ensureOwner(user, application);
        if (application.getStatus() != AppStatus.IN_PROGRESS) {
            throw ApiException.invalidState(
                    "Captured application data can no longer be edited (status: "
                            + application.getStatus().getLabel() + ")");
        }
        return application;
    }

    private void requireStepComplete(Application application, JourneyStepKey key, String message) {
        boolean done = journeyStepRepository.findByApplicationIdAndStepKey(application.getId(), key)
                .map(JourneyStep::isCompleted).orElse(false);
        if (!done) {
            throw ApiException.invalidState(message);
        }
    }

    private String capturedOf(Object form) {
        try {
            return objectMapper.writeValueAsString(form);
        } catch (Exception e) {
            return null;
        }
    }

    private String snapshotApplicant(Applicant applicant) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("full_name", applicant.getFullName());
            map.put("date_of_birth", applicant.getDateOfBirth());
            map.put("national_identifier", applicant.getNationalIdentifier());
            map.put("address_line1", applicant.getAddressLine1());
            map.put("city", applicant.getCity());
            map.put("employment_status", applicant.getEmploymentStatus());
            map.put("declared_annual_income", applicant.getDeclaredAnnualIncome());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }

    public record PersonalDetailsForm(String fullName, LocalDate dateOfBirth, String nationalIdentifier) {
    }

    public record AddressDetailsForm(String addressLine1, String addressLine2, String city,
                                     String postalCode, String country) {
    }

    public record EmploymentIncomeForm(String employmentStatus, String employerName, BigDecimal declaredAnnualIncome) {
    }
}
