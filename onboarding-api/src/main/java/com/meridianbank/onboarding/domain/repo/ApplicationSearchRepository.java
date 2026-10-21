package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Application;

import java.util.List;

public interface ApplicationSearchRepository {

    /** Search with bound filter parameters and a bounded, whitelisted sort column. */
    List<Application> search(ApplicationSearchCriteria criteria);

    long count(ApplicationSearchCriteria criteria);
}
