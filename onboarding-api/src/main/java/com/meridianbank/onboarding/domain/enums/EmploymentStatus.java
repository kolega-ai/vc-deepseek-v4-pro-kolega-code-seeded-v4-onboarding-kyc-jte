package com.meridianbank.onboarding.domain.enums;

public enum EmploymentStatus {
    EMPLOYED("employed"),
    SELF_EMPLOYED("self_employed"),
    STUDENT("student"),
    RETIRED("retired"),
    UNEMPLOYED("unemployed");

    private final String label;

    EmploymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
