package com.meridianbank.web.template;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/** Small formatting/class helpers shared by the JTE templates. */
public final class Tpl {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(java.time.ZoneOffset.UTC);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMM yyyy").withZone(java.time.ZoneOffset.UTC);

    private Tpl() {
    }

    public static String navClass(String activeNav, String item) {
        return item.equals(activeNav) ? "active" : "";
    }

    public static String firstInitial(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }

    public static String statusClass(String status) {
        return switch (status == null ? "" : status) {
            case "approved" -> "badge-success";
            case "declined" -> "badge-danger";
            case "under_review" -> "badge-primary";
            case "submitted" -> "badge-info";
            case "documents_requested" -> "badge-accent";
            case "watchlist_hold" -> "badge-danger";
            case "in_progress" -> "badge-neutral";
            case "abandoned" -> "badge-muted";
            default -> "badge-neutral";
        };
    }

    public static String statusLabel(String status) {
        return status == null ? "" : status.replace("_", " ");
    }

    public static String tierClass(String tier) {
        return switch (tier == null ? "" : tier) {
            case "tier_3" -> "badge-primary";
            case "tier_2" -> "badge-info";
            case "tier_1" -> "badge-neutral";
            default -> "badge-muted";
        };
    }

    public static String tierLabel(String tier) {
        return switch (tier == null ? "" : tier) {
            case "tier_3" -> "Premium";
            case "tier_2" -> "Standard";
            case "tier_1" -> "Basic";
            case "tier_0" -> "Unverified";
            default -> tier;
        };
    }

    public static String docStateClass(String state) {
        return switch (state == null ? "" : state) {
            case "verified" -> "badge-success";
            case "rejected" -> "badge-danger";
            case "expired" -> "badge-warning";
            default -> "badge-neutral";
        };
    }

    public static String dispositionClass(String disposition) {
        return switch (disposition == null ? "" : disposition) {
            case "confirmed" -> "badge-danger";
            case "false_positive" -> "badge-success";
            case "escalated" -> "badge-warning";
            default -> "badge-neutral";
        };
    }

    public static String dispositionLabel(String disposition) {
        return disposition == null ? "" : disposition.replace("_", " ");
    }

    public static String fmtDateTime(String iso) {
        if (iso == null || iso.isBlank()) {
            return "—";
        }
        try {
            return DATE_TIME.format(Instant.parse(iso));
        } catch (Exception e) {
            return iso;
        }
    }

    public static String fmtDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "—";
        }
        try {
            return DATE.format(Instant.parse(iso));
        } catch (Exception e) {
            return iso;
        }
    }

    public static String shortDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return "—";
        }
        try {
            return iso.length() >= 10 ? iso.substring(0, 10) : iso;
        } catch (Exception e) {
            return iso;
        }
    }

    /** Money formatting for fixed-precision SGD values (never floating point). */
    public static String money(String value) {
        if (value == null || value.isBlank()) {
            return "S$0.00";
        }
        try {
            return "S$" + String.format("%,.2f", new java.math.BigDecimal(value));
        } catch (Exception e) {
            return "S$" + value;
        }
    }

    public static String docTypeLabel(String type) {
        return switch (type == null ? "" : type) {
            case "NATIONAL_ID" -> "National ID (NRIC)";
            case "PASSPORT" -> "Passport";
            case "ADDRESS_PROOF" -> "Address proof";
            case "PROOF_OF_INCOME" -> "Proof of income";
            case "IDENTITY_DOC" -> "Identity document";
            default -> type.replace("_", " ").toLowerCase();
        };
    }

    public static String mask(String value, int visible) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        if (value.length() <= visible + 4) {
            return value;
        }
        return value.substring(0, visible) + "••••••";
    }

    public static String flashIcon(String kind) {
        return "success".equals(kind) ? "✓" : "!";
    }
}
