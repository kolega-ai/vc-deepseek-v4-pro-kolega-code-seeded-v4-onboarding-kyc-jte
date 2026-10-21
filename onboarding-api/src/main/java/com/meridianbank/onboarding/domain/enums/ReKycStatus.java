package com.meridianbank.onboarding.domain.enums;

public enum ReKycStatus {
    DUE("due"),
    IN_PROGRESS("in_progress"),
    OVERDUE("overdue"),
    COMPLETED("completed");

    private final String label;

    ReKycStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
