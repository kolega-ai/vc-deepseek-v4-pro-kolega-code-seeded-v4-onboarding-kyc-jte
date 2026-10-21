package com.meridianbank.onboarding.domain.enums;

public enum HitDisposition {
    PENDING("pending"),
    FALSE_POSITIVE("false_positive"),
    CONFIRMED("confirmed"),
    ESCALATED("escalated");

    private final String label;

    HitDisposition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
