package com.meridianbank.onboarding.domain.enums;

public enum AccountState {
    ACTIVE("active"),
    SUSPENDED("suspended");

    private final String label;

    AccountState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
