package com.meridianbank.onboarding.domain.enums;

public enum NotificationType {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    ACTION_REQUIRED("action_required"),
    OTP("otp");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
