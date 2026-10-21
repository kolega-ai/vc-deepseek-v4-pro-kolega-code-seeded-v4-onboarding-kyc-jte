package com.meridianbank.onboarding.domain.enums;

public enum ReKycOrigin {
    CAMPAIGN("campaign"),
    DOC_EXPIRY("doc_expiry");

    private final String label;

    ReKycOrigin(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
