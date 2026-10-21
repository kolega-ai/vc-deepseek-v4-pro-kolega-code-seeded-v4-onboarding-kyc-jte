package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.AppStatus;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.error.ApiException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * CSV exports. Applicant-supplied text fields are neutralised against spreadsheet formula
 * injection (leading =, +, -, @ and tab characters are prefixed with a single quote).
 */
@Service
public class ExportService {

    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final BranchRepository branchRepository;
    private final DocumentRepository documentRepository;
    private final WatchlistHitRepository hitRepository;
    private final StaffUserRepository staffUserRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public ExportService(ApplicationRepository applicationRepository,
                         ApplicantRepository applicantRepository,
                         BranchRepository branchRepository,
                         DocumentRepository documentRepository,
                         WatchlistHitRepository hitRepository,
                         StaffUserRepository staffUserRepository,
                         AccountRepository accountRepository,
                         AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.branchRepository = branchRepository;
        this.documentRepository = documentRepository;
        this.hitRepository = hitRepository;
        this.staffUserRepository = staffUserRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    /**
     * Neutralises spreadsheet formula prefixes in applicant-supplied text: any cell whose value
     * starts with =, +, -, @ (or a tab/CR after optional leading whitespace) gets a leading quote.
     */
    public static String neutralizeFormula(String value) {
        if (value == null) {
            return "";
        }
        String trimmedStart = value.replaceFirst("^[ \\t\\r]*", "");
        if (!trimmedStart.isEmpty()) {
            char first = trimmedStart.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                return "'" + value;
            }
        }
        return value;
    }

    /** Branch-scoped application report for officers (CSV). */
    public String applicationsCsv(UserAccount user, Long branchId, AppStatus status, String query) {
        List<Application> applications = applicationRepository.findAll().stream()
                .filter(a -> branchId != null && a.getBranchId() != null && a.getBranchId().equals(branchId))
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> query == null || query.isBlank()
                        || a.getReference().toLowerCase().contains(query.toLowerCase()))
                .toList();
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                .setHeader("reference", "applicant", "email", "status", "branch", "kyc_tier",
                        "submitted_at", "decision_outcome").build())) {
            for (Application application : applications) {
                Applicant applicant = applicantRepository.findById(application.getApplicantId()).orElse(null);
                String branchName = application.getBranchId() == null ? ""
                        : branchRepository.findById(application.getBranchId()).map(Branch::getName).orElse("");
                printer.printRecord(
                        application.getReference(),
                        neutralizeFormula(applicant == null ? "" : applicant.getFullName()),
                        neutralizeFormula(applicant == null ? "" : applicant.getEmail()),
                        application.getStatus().getLabel(),
                        neutralizeFormula(branchName),
                        application.effectiveKycTier(),
                        format(application.getSubmittedAt()),
                        application.getDecisionOutcome() == null ? "" : application.getDecisionOutcome().getLabel());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not build CSV export", e);
        }
        auditService.recordRead(user, "APPLICATIONS_CSV_EXPORT", "Application", applications.size(),
                "OFFICER_BRANCH_SCOPE");
        return out.toString();
    }

    /** Compliance watchlist report (CSV). */
    public String watchlistCsv(UserAccount user) {
        List<WatchlistHit> hits = hitRepository.findAll();
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                .setHeader("application", "matched_name", "list_source", "confidence",
                        "disposition", "disposed_at").build())) {
            for (WatchlistHit hit : hits) {
                Application application = applicationRepository.findById(hit.getApplicationId()).orElse(null);
                printer.printRecord(
                        application == null ? "" : application.getReference(),
                        neutralizeFormula(hit.getMatchedName()),
                        neutralizeFormula(hit.getListSource()),
                        hit.getConfidence(),
                        hit.getDisposition().getLabel(),
                        format(hit.getDisposedAt()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not build CSV export", e);
        }
        auditService.recordRead(user, "WATCHLIST_CSV_EXPORT", "WatchlistHit", hits.size(),
                "COMPLIANCE_BRANCH_ALL");
        return out.toString();
    }

    /** Audit log export (admin only; read-only). */
    public String auditCsv(UserAccount user, List<AuditEvent> events) {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                .setHeader("created_at", "actor_type", "actor", "action", "entity_type",
                        "entity_id", "authorization_basis", "after_value").build())) {
            for (AuditEvent event : events) {
                printer.printRecord(
                        format(event.getCreatedAt()),
                        event.getActorType(),
                        neutralizeFormula(event.getActor()),
                        event.getAction(),
                        event.getEntityType(),
                        event.getEntityId(),
                        event.getAuthorizationBasis(),
                        event.getAfterValue());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not build CSV export", e);
        }
        auditService.recordRead(user, "AUDIT_CSV_EXPORT", "AuditEvent", events.size(),
                "ADMIN_AUDIT_READ");
        return out.toString();
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).withLocale(Locale.ROOT);

    private String format(Instant instant) {
        return instant == null ? "" : FORMATTER.format(instant);
    }
}
