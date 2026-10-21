package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.enums.AppStatus;

import java.time.Instant;
import java.util.List;

/** Search criteria for the application list (officer console, compliance, admin views). */
public record ApplicationSearchCriteria(
        Long branchId,
        AppStatus status,
        String kycTier,
        String query,
        Instant from,
        Instant to,
        String sortBy,
        boolean ascending,
        int page,
        int size) {

    public static ApplicationSearchCriteria defaults() {
        return new ApplicationSearchCriteria(null, null, null, null, null, null, "created", false, 0, 25);
    }
}
