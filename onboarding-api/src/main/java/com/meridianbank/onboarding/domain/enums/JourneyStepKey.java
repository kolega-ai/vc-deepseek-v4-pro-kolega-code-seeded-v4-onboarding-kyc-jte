package com.meridianbank.onboarding.domain.enums;

/**
 * Ordered journey steps. A step may only be completed when every prerequisite step is complete;
 * the order is enforced server-side on every submission ({@code JourneyService}).
 */
public enum JourneyStepKey {
    REGISTERED("registered", 0),
    CONTACT_VERIFIED("contact_verified", 1),
    PERSONAL_DETAILS("personal_details", 2),
    ADDRESS_DETAILS("address_details", 3),
    EMPLOYMENT_INCOME("employment_income", 4),
    DOCUMENTS_UPLOADED("documents_uploaded", 5),
    IDENTITY_VERIFIED("identity_verified", 6);

    private final String label;
    private final int order;

    JourneyStepKey(String label, int order) {
        this.label = label;
        this.order = order;
    }

    public String getLabel() {
        return label;
    }

    public int getOrder() {
        return order;
    }

    public JourneyStepKey previous() {
        return order == 0 ? null : values()[order - 1];
    }
}
