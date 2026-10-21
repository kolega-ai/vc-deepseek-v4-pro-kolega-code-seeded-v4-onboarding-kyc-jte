package com.meridianbank.onboarding.domain.enums;

public enum Role {
    PROSPECT("prospect"),
    CUSTOMER("customer"),
    ONBOARDING_OFFICER("onboarding_officer"),
    COMPLIANCE_OFFICER("compliance_officer"),
    BANK_ADMIN("bank_admin");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isStaff() {
        return this == ONBOARDING_OFFICER || this == COMPLIANCE_OFFICER || this == BANK_ADMIN;
    }

    public boolean isApplicantSide() {
        return this == PROSPECT || this == CUSTOMER;
    }
}
