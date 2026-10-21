package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.auth.JwtService;
import com.meridianbank.onboarding.auth.OtpService;
import com.meridianbank.onboarding.client.CoreMockClient;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.error.ApiException;
import com.meridianbank.onboarding.service.state.ApplicationStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Officer decisioning: request documents, approve and decline. Approval is gated by branch scope,
 * step-up OTP, the officer's max approval tier and the state machine guards; account opening goes
 * through the vendor core stub with a decision-minted scope token.
 */
@Service
public class DecisionService {

    private final AccessControlService accessControlService;
    private final ApplicationStateMachine stateMachine;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final CoreMockClient coreMockClient;
    private final AccountRepository accountRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApplicantRepository applicantRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OutboxEmailService outboxEmailService;
    private final WebhookService webhookService;

    public DecisionService(AccessControlService accessControlService,
                           ApplicationStateMachine stateMachine,
                           OtpService otpService,
                           JwtService jwtService,
                           CoreMockClient coreMockClient,
                           AccountRepository accountRepository,
                           UserAccountRepository userAccountRepository,
                           ApplicantRepository applicantRepository,
                           AuditService auditService,
                           NotificationService notificationService,
                           OutboxEmailService outboxEmailService,
                           WebhookService webhookService) {
        this.accessControlService = accessControlService;
        this.stateMachine = stateMachine;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.coreMockClient = coreMockClient;
        this.accountRepository = accountRepository;
        this.userAccountRepository = userAccountRepository;
        this.applicantRepository = applicantRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.outboxEmailService = outboxEmailService;
        this.webhookService = webhookService;
    }

    @Transactional
    public Account approve(UserAccount user, String sessionId, Long applicationId, String customerVisibleReason) {
        StaffUser staff = accessControlService.requireOfficer(user);
        accessControlService.assertNotAdminDecision(staff);
        Application application = accessControlService.requireApplication(applicationId);
        accessControlService.assertOfficerBranchScope(staff, application);
        if (application.getStatus() != AppStatus.UNDER_REVIEW) {
            throw ApiException.invalidState("Only applications under review can be approved");
        }
        // Step-up OTP is mandatory for approve.
        otpService.requireStepUp(user, sessionId, OtpPurpose.APPROVE);
        // Approval authority: derived tier must be within the officer's max approval tier.
        accessControlService.assertWithinApprovalAuthority(staff, application);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText(
                "Customer-visible reason", customerVisibleReason, 5, 500);

        String tier = application.effectiveKycTier();
        String scopeToken = jwtService.issueCoreScopeToken(application.getId(), application.getReference(), tier);
        CoreMockClient.OpenedAccount opened = coreMockClient.openAccount(
                scopeToken, application.getReference(), "ESA", "SGD", tier);

        stateMachine.officerApprove(application, staff, customerVisibleReason.trim());

        Account account = new Account();
        account.setApplicationId(application.getId());
        account.setAccountNumber(opened.accountNumber());
        account.setCoreReference(opened.coreReference());
        account.setProduct(opened.product());
        account.setKycTier(opened.kycTier());
        account.setStatus(AccountState.ACTIVE);
        account.setOpenedAt(Instant.now());
        accountRepository.save(account);

        upgradeApplicantRole(application);

        auditService.recordUser(user, "ACCOUNT_OPENED_CORE", "Account", opened.accountNumber(),
                null, "{\"core_reference\":\"" + opened.coreReference() + "\",\"kyc_tier\":\"" + tier + "\"}",
                "OFFICER_BRANCH_SCOPE+STEP_UP_APPROVE+TIER_AUTHORITY+CORE_SCOPE_TOKEN");
        notificationService.notifyApplicant(application, NotificationType.SUCCESS,
                "Your account is open",
                "Application " + application.getReference() + " was approved. Your account number is "
                        + opened.accountNumber() + ".",
                "/customer/dashboard");
        outboxEmailService.send(applicantEmail(application), "Your Meridian Bank account is open",
                "Congratulations — application " + application.getReference()
                        + " was approved. Account number: " + opened.accountNumber()
                        + " (" + opened.product() + ", KYC tier " + tier + ").",
                "approval");
        webhookService.deliver("application.approved", application.getReference(),
                "{\"reference\":\"" + application.getReference() + "\",\"account_number\":\""
                        + opened.accountNumber() + "\",\"kyc_tier\":\"" + tier + "\"}");
        return account;
    }

    @Transactional
    public void decline(UserAccount user, String sessionId, Long applicationId,
                        String customerVisibleReason, String internalReason) {
        StaffUser staff = accessControlService.requireOfficer(user);
        accessControlService.assertNotAdminDecision(staff);
        Application application = accessControlService.requireApplication(applicationId);
        accessControlService.assertOfficerBranchScope(staff, application);
        if (application.getStatus() != AppStatus.UNDER_REVIEW) {
            throw ApiException.invalidState("Only applications under review can be declined");
        }
        otpService.requireStepUp(user, sessionId, OtpPurpose.DECLINE);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText(
                "Customer-visible reason", customerVisibleReason, 5, 500);
        com.meridianbank.onboarding.auth.Validators.validateRequiredText(
                "Internal reason", internalReason, 5, 500);

        stateMachine.officerDecline(application, staff, customerVisibleReason.trim(), internalReason.trim());
        notificationService.notifyApplicant(application, NotificationType.WARNING,
                "A decision was made on your application",
                customerVisibleReason.trim(), "/journey/status");
        outboxEmailService.send(applicantEmail(application), "Update on your Meridian Bank application",
                "Application " + application.getReference() + ": " + customerVisibleReason.trim(),
                "decline");
        webhookService.deliver("application.declined", application.getReference(),
                "{\"reference\":\"" + application.getReference() + "\"}");
    }

    @Transactional
    public void requestDocuments(UserAccount user, Long applicationId, List<String> documentTypes, String message) {
        StaffUser staff = accessControlService.requireOfficer(user);
        accessControlService.assertNotAdminDecision(staff);
        Application application = accessControlService.requireApplication(applicationId);
        accessControlService.assertOfficerBranchScope(staff, application);
        for (String type : documentTypes) {
            try {
                DocType.valueOf(type);
            } catch (Exception e) {
                throw ApiException.badRequest("Unknown document type: " + type);
            }
        }
        com.meridianbank.onboarding.auth.Validators.validateRequiredText("Message to applicant", message, 5, 500);
        stateMachine.officerRequestDocuments(application, staff, documentTypes, message.trim());
        notificationService.notifyApplicant(application, NotificationType.ACTION_REQUIRED,
                "Additional documents required",
                message.trim(), "/journey/documents");
        outboxEmailService.send(applicantEmail(application), "Additional documents required",
                "Application " + application.getReference() + ": " + message.trim(),
                "documents-requested");
    }

    private void upgradeApplicantRole(Application application) {
        applicantRepository.findById(application.getApplicantId())
                .filter(a -> a.getUserAccountId() != null)
                .ifPresent(applicant -> userAccountRepository.findById(applicant.getUserAccountId())
                        .ifPresent(account -> {
                            if (account.getRole() == Role.PROSPECT) {
                                auditService.recordSystem("system:decision", "ROLE_UPGRADED", "UserAccount",
                                        account.getEmail(), Role.PROSPECT.name(), Role.CUSTOMER.name(),
                                        "APPROVAL_ACCOUNT_OPENED");
                                account.setRole(Role.CUSTOMER);
                                userAccountRepository.save(account);
                            }
                        }));
    }

    private String applicantEmail(Application application) {
        return applicantRepository.findById(application.getApplicantId())
                .map(Applicant::getEmail).orElse("");
    }
}
