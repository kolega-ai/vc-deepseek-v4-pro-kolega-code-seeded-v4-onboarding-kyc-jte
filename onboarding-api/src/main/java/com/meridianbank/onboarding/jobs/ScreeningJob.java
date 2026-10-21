package com.meridianbank.onboarding.jobs;

import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.repo.ApplicationRepository;
import com.meridianbank.onboarding.service.ScreeningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Picks up submitted applications whose screening queue delay has elapsed and screens them. */
@Component
public class ScreeningJob {

    private static final Logger log = LoggerFactory.getLogger(ScreeningJob.class);

    private final ApplicationRepository applicationRepository;
    private final ScreeningService screeningService;
    private final AppProperties props;

    public ScreeningJob(ApplicationRepository applicationRepository,
                        ScreeningService screeningService,
                        AppProperties props) {
        this.applicationRepository = applicationRepository;
        this.screeningService = screeningService;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "10000", initialDelayString = "15000")
    public void run() {
        if (!props.getScreening().isJobEnabled()) {
            return;
        }
        var due = applicationRepository.findDueForScreening(Instant.now());
        for (var application : due) {
            try {
                screeningService.screen(application);
            } catch (Exception e) {
                log.error("Screening failed for application {}", application.getReference(), e);
            }
        }
    }
}
