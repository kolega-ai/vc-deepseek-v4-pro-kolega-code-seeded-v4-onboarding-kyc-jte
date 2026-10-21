package com.meridianbank.onboarding.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridianbank.onboarding.auth.JwtService;
import com.meridianbank.onboarding.client.CoreMockClient;
import com.meridianbank.onboarding.client.ProviderMockClient;
import com.meridianbank.onboarding.domain.entity.AuthSession;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.repo.AuthSessionRepository;
import com.meridianbank.onboarding.support.TestFixtures.FixtureFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFixtures.class)
public abstract class IntegrationTestBase {

    private static final Pattern CODE = Pattern.compile("code is (\\d{6})");

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected FixtureFactory fixtures;
    @Autowired
    protected AuthSessionRepository authSessionRepository;
    @Autowired
    protected JwtService jwtService;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockitoBean
    protected ProviderMockClient providerMockClient;

    @MockitoBean
    protected CoreMockClient coreMockClient;

    /** Wipes transactional data before each test; seeded reference data is preserved. */
    @BeforeEach
    void cleanDatabase() {
        for (String table : List.of(
                "webhook_delivery", "audit_event", "notification", "outbox_email",
                "step_up_grant", "otp_challenge", "password_reset_token", "auth_session",
                "identity_verification_result", "bureau_result", "watchlist_hit", "document",
                "journey_step", "rekyc_case", "account", "onb_application", "applicant",
                "user_account", "rekyc_campaign", "staff_user")) {
            try {
                jdbcTemplate.execute("DELETE FROM " + table);
            } catch (Exception ignored) {
                // table may not exist yet in this context
            }
        }
        try {
            jdbcTemplate.execute("ALTER TABLE user_account ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE applicant ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE onb_application ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE staff_user ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE document ALTER COLUMN id RESTART WITH 1");
        } catch (Exception ignored) {
            // sequence restart is best-effort
        }
    }

    protected String bearerFor(UserAccount user) {
        return bearerFor(user, "test-session-" + UUID.randomUUID());
    }

    protected String bearerFor(UserAccount user, String sid) {
        AuthSession session = new AuthSession();
        session.setId(sid);
        session.setUserAccountId(user.getId());
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(Instant.now().plusSeconds(3600));
        authSessionRepository.save(session);
        return jwtService.issue(user, sid);
    }

    protected ResultActions getAs(UserAccount user, String url) throws Exception {
        return mockMvc.perform(get(url).header("Authorization", "Bearer " + bearerFor(user)));
    }

    protected ResultActions postAs(UserAccount user, String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + bearerFor(user))
                .contentType("application/json")
                .content(json));
    }

    protected ResultActions postAsToken(String token, String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(json));
    }

    protected String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected static String extractCode(String text) {
        Matcher matcher = CODE.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }
}
