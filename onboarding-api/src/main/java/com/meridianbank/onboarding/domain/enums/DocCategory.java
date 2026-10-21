package com.meridianbank.onboarding.domain.enums;

public enum DocCategory {
    IDENTITY("identity"),
    ADDRESS("address");

    private final String label;

    DocCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
