package com.meridianbank.onboarding.domain.enums;

public enum DocState {
    UPLOADED("uploaded"),
    VERIFIED("verified"),
    REJECTED("rejected"),
    EXPIRED("expired");

    private final String label;

    DocState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
