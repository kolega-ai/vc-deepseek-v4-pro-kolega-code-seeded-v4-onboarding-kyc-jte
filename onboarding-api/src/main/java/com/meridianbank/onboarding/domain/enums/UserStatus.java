package com.meridianbank.onboarding.domain.enums;

public enum UserStatus {
    ACTIVE("active"),
    DISABLED("disabled");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
