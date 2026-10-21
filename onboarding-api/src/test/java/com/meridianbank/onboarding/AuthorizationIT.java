package com.meridianbank.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import com.meridianbank.onboarding.service.JourneyService;
import com.meridianbank.onboarding.service.documents.DocumentService;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import com.meridianbank.onboarding.support.TestFixtures.Prospect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization boundaries: cross-tenant isolation, branch scoping, role restrictions on captured
 * data and decisioning, and staff-only field visibility.
 */
class AuthorizationIT extends IntegrationTestBase {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserAccountRepository userAccountRepository;

    private Branch central;
    private Branch east;
    private Prospect prospectA;
    private Prospect prospectB;
    private Document docA;
    private Document docB;

    @BeforeEach
    void setUp() throws Exception {
        central = fixtures.branch("MB-CEN-01");
        east = fixtures.branch("MB-EAS-02");
        prospectA = fixtures.prospect("tenant-a@meridian.test", central, "Tenant A",
                EmploymentStatus.EMPLOYED, "50000", true);
        prospectB = fixtures.prospect("tenant-b@meridian.test", east, "Tenant B",
                EmploymentStatus.EMPLOYED, "50000", true);
        docA = documentService.upload(prospectA.user(), prospectA.application().getId(),
                DocType.NATIONAL_ID.name(), "a.pdf", "application/pdf", "%PDF-1.4 a".getBytes(StandardCharsets.UTF_8));
        docB = documentService.upload(prospectB.user(), prospectB.application().getId(),
                DocType.NATIONAL_ID.name(), "b.pdf", "application/pdf", "%PDF-1.4 b".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void applicantCannotDownloadAnotherApplicantsDocument() throws Exception {
        mockMvc.perform(get("/api/documents/" + docB.getId() + "/download")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isForbidden());
    }

    @Test
    void applicantCannotReadAnotherApplicantsDocumentMetadata() throws Exception {
        mockMvc.perform(get("/api/applications/" + prospectB.application().getId()
                        + "/documents/" + docB.getId() + "/metadata")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isForbidden());
    }

    @Test
    void applicantCanDownloadOwnDocument() throws Exception {
        mockMvc.perform(get("/api/documents/" + docA.getId() + "/download")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isOk());
    }

    @Test
    void officerCannotSeeAnotherBranchesApplication() throws Exception {
        UserAccount officerCentral = officerAccount(fixtures.officer(central, "tier_3"));
        // East application: forbidden for the Central officer
        mockMvc.perform(get("/api/officer/applications/" + prospectB.application().getId())
                        .header("Authorization", "Bearer " + bearerFor(officerCentral)))
                .andExpect(status().isForbidden());
        // Central application: allowed
        mockMvc.perform(get("/api/officer/applications/" + prospectA.application().getId())
                        .header("Authorization", "Bearer " + bearerFor(officerCentral)))
                .andExpect(status().isOk());
    }

    @Test
    void officerCannotVerifyAnotherBranchesDocument() throws Exception {
        UserAccount officerCentral = officerAccount(fixtures.officer(central, "tier_3"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/officer/documents/" + docB.getId() + "/verify")
                        .header("Authorization", "Bearer " + bearerFor(officerCentral)))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerListAndSearchAreBranchScoped() throws Exception {
        UserAccount officerCentral = officerAccount(fixtures.officer(central, "tier_3"));
        String body = mockMvc.perform(get("/api/officer/applications")
                        .header("Authorization", "Bearer " + bearerFor(officerCentral)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        for (JsonNode item : root.get("items")) {
            assertNotEquals(prospectB.application().getId().longValue(), item.get("id").asLong(),
                    "East application leaked into Central officer search results");
        }
    }

    @Test
    void complianceCannotEditCapturedApplicantData() throws Exception {
        UserAccount compliance = fixtures.complianceUser(central);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/applications/me/personal")
                        .header("Authorization", "Bearer " + bearerFor(compliance))
                        .contentType("application/json")
                        .content(json(new JourneyService.PersonalDetailsForm("Evil Edit",
                                java.time.LocalDate.of(1990, 1, 1), "S1234567A"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotApproveDeclineVerifyOrDisposition() throws Exception {
        UserAccount admin = fixtures.adminUser(central);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/officer/applications/" + prospectA.application().getId() + "/approve")
                        .header("Authorization", "Bearer " + bearerFor(admin))
                        .contentType("application/json")
                        .content("{\"customerVisibleReason\":\"reason\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/officer/applications/" + prospectA.application().getId() + "/decline")
                        .header("Authorization", "Bearer " + bearerFor(admin))
                        .contentType("application/json")
                        .content("{\"customerVisibleReason\":\"reason\",\"internalReason\":\"reason\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/officer/documents/" + docA.getId() + "/verify")
                        .header("Authorization", "Bearer " + bearerFor(admin)))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/compliance/hits/1/disposition")
                        .header("Authorization", "Bearer " + bearerFor(admin))
                        .contentType("application/json")
                        .content("{\"disposition\":\"false_positive\",\"justification\":\"none\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void applicantViewNeverContainsStaffOnlyFields() throws Exception {
        String body = mockMvc.perform(get("/api/applications/me")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        assertFalse(root.has("bureauResult"));
        assertFalse(root.has("watchlistHits"));
        assertFalse(root.has("internalReason"));
        assertTrue(root.has("documents"));
    }

    @Test
    void applicantCannotReachOfficerOrComplianceEndpoints() throws Exception {
        mockMvc.perform(get("/api/officer/applications")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/compliance/hits")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownSortKeyFallsBackToDefaultWithoutError() throws Exception {
        UserAccount officerCentral = officerAccount(fixtures.officer(central, "tier_3"));
        mockMvc.perform(get("/api/officer/applications")
                        .param("sort", "status; DROP TABLE audit_event")
                        .param("dir", "asc")
                        .header("Authorization", "Bearer " + bearerFor(officerCentral)))
                .andExpect(status().isOk());
    }

    @Test
    void documentReAuthorizationAppliesToPreviouslyIssuedUrl() throws Exception {
        // A downloads its own document (URL works) ...
        mockMvc.perform(get("/api/documents/" + docA.getId() + "/download")
                        .header("Authorization", "Bearer " + bearerFor(prospectA.user())))
                .andExpect(status().isOk());
        // ... but the very same URL is rejected for a different applicant.
        mockMvc.perform(get("/api/documents/" + docA.getId() + "/download")
                        .header("Authorization", "Bearer " + bearerFor(prospectB.user())))
                .andExpect(status().isForbidden());
    }

    private UserAccount officerAccount(StaffUser staff) {
        return userAccountRepository.findByStaffId(staff.getId()).orElseThrow();
    }
}
