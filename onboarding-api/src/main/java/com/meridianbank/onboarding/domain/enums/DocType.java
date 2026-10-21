package com.meridianbank.onboarding.domain.enums;

public enum DocType {
    NATIONAL_ID("national_id", DocCategory.IDENTITY),
    PASSPORT("passport", DocCategory.IDENTITY),
    ADDRESS_PROOF("address_proof", DocCategory.ADDRESS),
    PROOF_OF_INCOME("proof_of_income", DocCategory.ADDRESS);

    private final String label;
    private final DocCategory category;

    DocType(String label, DocCategory category) {
        this.label = label;
        this.category = category;
    }

    public String getLabel() {
        return label;
    }

    public DocCategory getCategory() {
        return category;
    }
}
