package com.meridianbank.onboarding.domain.enums;

public enum OtpChannel {
    EMAIL("email"),
    MOBILE("mobile");

    private final String label;

    OtpChannel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
