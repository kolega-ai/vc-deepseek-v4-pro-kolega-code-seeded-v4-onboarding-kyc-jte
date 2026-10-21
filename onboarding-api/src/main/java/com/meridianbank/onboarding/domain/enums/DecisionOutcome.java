package com.meridianbank.onboarding.domain.enums;

public enum DecisionOutcome {
    APPROVED("approved"),
    DECLINED("declined");

    private final String label;

    DecisionOutcome(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
