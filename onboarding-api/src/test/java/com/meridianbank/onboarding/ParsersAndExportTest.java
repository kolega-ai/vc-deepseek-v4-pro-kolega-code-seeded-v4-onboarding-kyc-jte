package com.meridianbank.onboarding;

import com.meridianbank.onboarding.client.ProviderResponseXmlParser;
import com.meridianbank.onboarding.client.SecureReferenceXmlParser;
import com.meridianbank.onboarding.domain.entity.Branch;
import com.meridianbank.onboarding.service.ExportService;
import com.meridianbank.onboarding.service.documents.DocumentStorage;
import com.meridianbank.onboarding.service.error.ApiException;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Precision locations and hardening checks:
 * - the hardened reference-data XML parser rejects DTDs and external entities;
 * - the identity-provider response ingest parses DOCTYPE-bearing provider XML;
 * - CSV export neutralises spreadsheet formula prefixes;
 * - document storage resolves paths through a canonicalised base directory.
 */
class ParsersAndExportTest {

    @Test
    void hardenedReferenceXmlParserParsesCleanDocuments() {
        SecureReferenceXmlParser parser = new SecureReferenceXmlParser();
        var rows = parser.parseBranches("""
                <?xml version="1.0" encoding="UTF-8"?>
                <reference-data>
                  <branches>
                    <branch><code>MB-NEW-01</code><name>Orchard Gateway</name><region>Central</region></branch>
                  </branches>
                </reference-data>
                """);
        assertEquals(1, rows.size());
        assertEquals("MB-NEW-01", rows.get(0).code());
        assertEquals("Orchard Gateway", rows.get(0).name());
    }

    @Test
    void hardenedReferenceXmlParserRejectsExternalEntities() {
        SecureReferenceXmlParser parser = new SecureReferenceXmlParser();
        String attack = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE reference-data [
                  <!ENTITY xxe SYSTEM "file:///etc/passwd">
                ]>
                <reference-data>
                  <branches>
                    <branch><code>&xxe;</code><name>Pwned</name><region>Central</region></branch>
                  </branches>
                </reference-data>
                """;
        ApiException ex = assertThrows(ApiException.class, () -> parser.parseBranches(attack));
        assertTrue(ex.getMessage().contains("external entities") || ex.getMessage().contains("DOCTYPE")
                || ex.getMessage().contains("forbidden"));
    }

    @Test
    void hardenedReferenceXmlParserRejectsDoctypeDeclarations() {
        SecureReferenceXmlParser parser = new SecureReferenceXmlParser();
        String withDoctype = """
                <?xml version="1.0"?>
                <!DOCTYPE reference-data [<!ENTITY a "b">]>
                <reference-data><branches><branch><code>X</code><name>Y</name><region>Z</region></branch></branches></reference-data>
                """;
        assertThrows(ApiException.class, () -> parser.parseBranches(withDoctype));
    }

    @Test
    void providerResponseIngestParsesDoctypeBearingVendorXml() {
        // The identity provider emits a DOCTYPE with an internal entity; the designated
        // provider-response ingest must process it.
        ProviderResponseXmlParser parser = new ProviderResponseXmlParser();
        String vendor = "<?xml version=\"1.0\"?><!DOCTYPE identity-verification-response "
                + "[<!ENTITY providerName \"ScreenSure Identity Services\">]>"
                + "<identity-verification-response>"
                + "<providerReference>SCR-ABC</providerReference>"
                + "<providerName>&providerName;</providerName>"
                + "<matchScore>87</matchScore><outcome>MATCH</outcome>"
                + "</identity-verification-response>";
        var result = parser.parse(vendor);
        assertEquals("SCR-ABC", result.providerReference());
        assertEquals(87, result.matchScore());
        assertEquals("MATCH", result.outcome());
    }

    @Test
    void csvExportNeutralisesFormulaPrefixes() {
        assertEquals("'=cmd()", ExportService.neutralizeFormula("=cmd()"));
        assertEquals("'+1+1", ExportService.neutralizeFormula("+1+1"));
        assertEquals("'-2+3", ExportService.neutralizeFormula("-2+3"));
        assertEquals("'@SUM(A1)", ExportService.neutralizeFormula("@SUM(A1)"));
        assertEquals("'\t=HYPERLINK(...)", ExportService.neutralizeFormula("\t=HYPERLINK(...)"));
        assertEquals("'  =x", ExportService.neutralizeFormula("  =x"));
        assertEquals("Jane Doe", ExportService.neutralizeFormula("Jane Doe"));
        assertEquals("", ExportService.neutralizeFormula(null));
    }

    @Test
    void documentStorageResolvesThroughCanonicalisedBaseDirectory() throws Exception {
        // Any storage name that escapes the canonical base is rejected.
        com.meridianbank.onboarding.config.AppProperties props =
                new com.meridianbank.onboarding.config.AppProperties();
        props.setStorage(new com.meridianbank.onboarding.config.AppProperties.Storage());
        props.getStorage().setDir("/tmp");
        DocumentStorage storage = new DocumentStorage(props);
        var doc = new com.meridianbank.onboarding.domain.entity.Document();
        doc.setStorageName("../../etc/passwd");
        assertThrows(ApiException.class, () -> storage.resolve(doc));
        doc.setStorageName("nonexistent-file-xyz.pdf");
        assertThrows(ApiException.class, () -> storage.resolve(doc));
    }

    @Test
    void documentStorageSanitizesUploadedFilenames() {
        assertEquals("passport.pdf", DocumentStorage.sanitizeFilename("C:\\fakepath\\passport.pdf"));
        assertEquals("passport.pdf", DocumentStorage.sanitizeFilename("../../passport.pdf"));
        assertEquals("document", DocumentStorage.sanitizeFilename(null));
    }
}
