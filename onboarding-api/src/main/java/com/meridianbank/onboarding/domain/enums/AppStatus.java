package com.meridianbank.onboarding.domain.enums;

/**
 * Application lifecycle statuses. All transitions must go through the explicit edge list in
 * {@code com.meridianbank.onboarding.service.state.ApplicationStateMachine}.
 */
public enum AppStatus {
    IN_PROGRESS("in_progress"),
    SUBMITTED("submitted"),
    UNDER_REVIEW("under_review"),
    DOCUMENTS_REQUESTED("documents_requested"),
    WATCHLIST_HOLD("watchlist_hold"),
    APPROVED("approved"),
    DECLINED("declined"),
    ABANDONED("abandoned");

    private final String label;

    AppStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
