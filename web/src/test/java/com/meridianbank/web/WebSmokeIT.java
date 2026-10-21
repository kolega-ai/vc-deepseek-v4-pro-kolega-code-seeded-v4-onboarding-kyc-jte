package com.meridianbank.web;

import com.meridianbank.web.api.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Channel-tier smoke tests: login page, auth redirects and role-gated routing. */
@SpringBootTest
@AutoConfigureMockMvc
class WebSmokeIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiClient apiClient;

    @Test
    void loginPageRendersWithCsrfToken() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Welcome back")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("_csrf")));
    }

    @Test
    void registerPageRenders() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Start your application")));
    }

    @Test
    void unauthenticatedJourneyRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/journey"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void officerRouteRejectsProspectSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authToken", "fake-token");
        session.setAttribute("authRole", "prospect");
        session.setAttribute("authEmail", "p@meridian.test");
        session.setAttribute("authName", "Prospect");
        mockMvc.perform(get("/officer/dashboard").session(session))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void logoutClearsSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authToken", "fake-token");
        session.setAttribute("authRole", "prospect");
        session.setAttribute("authEmail", "p@meridian.test");
        session.setAttribute("authName", "Prospect");
        mockMvc.perform(post("/logout").session(session)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        org.junit.jupiter.api.Assertions.assertNull(session.getAttribute("authToken"));
    }
}
