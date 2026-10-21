package com.meridianbank.onboarding.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Auth auth = new Auth();
    private Storage storage = new Storage();
    private Providers providers = new Providers();
    private CoreMock coreMock = new CoreMock();
    private Screening screening = new Screening();
    private Jobs jobs = new Jobs();
    private Seed seed = new Seed();
    private Webhook webhook = new Webhook();
    private Provisioning provisioning = new Provisioning();

    /** Public base URL of the channel (web) tier, used for links in mock emails. */
    private String webUrl;

    @Getter
    @Setter
    public static class Jwt {
        /** HMAC-SHA256 signing secret (must be at least 32 bytes). */
        private String secret;
        private String issuer;
        private String audience;
        private Duration ttl;
    }

    @Getter
    @Setter
    public static class Auth {
        private Duration otpTtl;
        private Duration otpIssueWindow;
        private int otpMaxIssuesPerWindow;
        private int otpMaxVerifyAttempts;
        private Duration stepUpTtl;
        private Duration resetTokenTtl;
        private int maxFailedLogins;
        private int loginLockMinutes;
    }

    @Getter
    @Setter
    public static class Storage {
        private String dir;
    }

    @Getter
    @Setter
    public static class Providers {
        private String baseUrl;
        private String apiKey;
    }

    @Getter
    @Setter
    public static class CoreMock {
        private String baseUrl;
        private String apiKey;
        private String tokenSecret;
    }

    @Getter
    @Setter
    public static class Screening {
        private boolean jobEnabled;
        private Duration queueDelay;
    }

    @Getter
    @Setter
    public static class Jobs {
        private String documentExpiryCron;
        private String abandonmentCron;
        private String rekycDueCron;
        private String housekeepingCron;
    }

    @Getter
    @Setter
    public static class Seed {
        private boolean enabled;
        private Duration screeningDelayForSeededSubmitted;
    }

    @Getter
    @Setter
    public static class Webhook {
        private long deliveryTimeoutMs;
    }

    @Getter
    @Setter
    public static class Provisioning {
        /** Bootstrap provisioning message applied on startup. */
        private String message;
    }
}
