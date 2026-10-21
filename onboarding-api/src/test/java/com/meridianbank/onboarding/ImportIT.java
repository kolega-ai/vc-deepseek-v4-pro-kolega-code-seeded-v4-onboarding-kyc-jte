package com.meridianbank.onboarding;

import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.repo.BranchRepository;
import com.meridianbank.onboarding.domain.repo.StaffUserRepository;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import com.meridianbank.onboarding.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Admin imports: staff roster CSV and branch reference-data XML (hardened parser). */
class ImportIT extends IntegrationTestBase {

    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private StaffUserRepository staffUserRepository;
    @Autowired
    private BranchRepository branchRepository;

    private UserAccount admin;

    @BeforeEach
    void setUp() {
        fixtures.branch("MB-CEN-01");
        fixtures.branch("MB-EAS-02");
        admin = fixtures.adminUser(fixtures.branch("MB-CEN-01"));
    }

    @Test
    void staffRosterCsvCreatesStaffWithAccounts() throws Exception {
        String csv = """
                employee_id,full_name,role,branch_code,max_approval_tier,email
                IMP-901,Import One,ONBOARDING_OFFICER,MB-CEN-01,tier_2,import.one@meridian.test
                IMP-902,Import Two,COMPLIANCE_OFFICER,MB-EAS-02,tier_0,import.two@meridian.test
                """;
        var file = new MockMultipartFile("file", "roster.csv", "text/csv", csv.getBytes());
        mockMvc.perform(multipart("/api/admin/staff/import").file(file)
                        .header("Authorization", "Bearer " + bearerFor(admin)))
                .andExpect(status().isOk());
        assertTrue(staffUserRepository.findByEmployeeId("IMP-901").isPresent());
        assertTrue(userAccountRepository.findByEmailIgnoreCase("import.one@meridian.test").isPresent());
    }

    @Test
    void staffRosterCsvReportsRowErrors() throws Exception {
        String csv = """
                employee_id,full_name,role,branch_code,max_approval_tier,email
                BAD-1,Bad Role,TELLER,MB-CEN-01,tier_1,bad.role@meridian.test
                BAD-2,Bad Branch,ONBOARDING_OFFICER,MB-NOPE,tier_1,bad.branch@meridian.test
                OK-1,Good One,ONBOARDING_OFFICER,MB-CEN-01,tier_2,good.one@meridian.test
                """;
        var file = new MockMultipartFile("file", "roster.csv", "text/csv", csv.getBytes());
        String body = mockMvc.perform(multipart("/api/admin/staff/import").file(file)
                        .header("Authorization", "Bearer " + bearerFor(admin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var errors = objectMapper.readTree(body).get("errors");
        assertEquals(2, errors.size());
        assertTrue(staffUserRepository.findByEmployeeId("OK-1").isPresent());
    }

    @Test
    void branchXmlImportUsesHardenedParser() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <reference-data>
                  <branches>
                    <branch><code>MB-IMP-01</code><name>Imported Branch</name><region>North</region></branch>
                  </branches>
                </reference-data>
                """;
        var file = new MockMultipartFile("file", "branches.xml", "application/xml", xml.getBytes());
        mockMvc.perform(multipart("/api/admin/branches/import-xml").file(file)
                        .header("Authorization", "Bearer " + bearerFor(admin)))
                .andExpect(status().isOk());
        assertTrue(branchRepository.findByCode("MB-IMP-01").isPresent());
    }

    @Test
    void branchXmlImportRejectsXxe() throws Exception {
        String attack = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE reference-data [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <reference-data>
                  <branches>
                    <branch><code>&xxe;</code><name>Pwned</name><region>Central</region></branch>
                  </branches>
                </reference-data>
                """;
        var file = new MockMultipartFile("file", "evil.xml", "application/xml", attack.getBytes());
        mockMvc.perform(multipart("/api/admin/branches/import-xml").file(file)
                        .header("Authorization", "Bearer " + bearerFor(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminCannotImport() throws Exception {
        var prospect = fixtures.prospect("importer@meridian.test", fixtures.branch("MB-CEN-01"),
                "Importer", com.meridianbank.onboarding.domain.enums.EmploymentStatus.EMPLOYED,
                "50000", true);
        var file = new MockMultipartFile("file", "branches.xml", "application/xml",
                "<reference-data><branches/></reference-data>".getBytes());
        mockMvc.perform(multipart("/api/admin/branches/import-xml").file(file)
                        .header("Authorization", "Bearer " + bearerFor(prospect.user())))
                .andExpect(status().isForbidden());
    }
}
