package com.meridianbank.onboarding.domain.enums;

/**
 * Step-up OTP purposes. Exactly these sensitive actions require a verified step-up OTP:
 * approve, decline, kyc_tier override, password change, re-KYC submit.
 */
public enum OtpPurpose {
    APPROVE("approve"),
    DECLINE("decline"),
    KYC_TIER_OVERRIDE("kyc_tier_override"),
    PASSWORD_CHANGE("password_change"),
    REKYC_SUBMIT("rekyc_submit");

    private final String label;

    OtpPurpose(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
