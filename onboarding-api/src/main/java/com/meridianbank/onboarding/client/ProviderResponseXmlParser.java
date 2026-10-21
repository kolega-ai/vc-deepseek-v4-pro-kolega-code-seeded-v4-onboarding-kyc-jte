package com.meridianbank.onboarding.client;

import com.meridianbank.onboarding.service.error.ApiException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * ProviderResponseXmlParser — the identity-provider XML ingest.
 *
 * The identity provider (provider-mock) returns XML and this parser ingests it. Per the corpus
 * contract for this application this is the designated identity-provider response ingest path and
 * is deliberately implemented with a stock JAXP {@link DocumentBuilderFactory}; it is the named
 * counterpart of {@link SecureReferenceXmlParser}, which hardens a SEPARATE benign reference-data
 * XML import (DTDs and external entities disabled there).
 *
 * Provider responses carry a DOCTYPE declaration (as legacy vendor systems commonly do) and this
 * parser processes it — the ingest is therefore genuinely exercised on every identity check.
 */
public class ProviderResponseXmlParser {

    public record IdentityResponse(String providerReference, int matchScore, String outcome) {
    }

    public IdentityResponse parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            Element root = doc.getDocumentElement();
            String reference = textOf(root, "providerReference");
            int score = Integer.parseInt(textOf(root, "matchScore").trim());
            String outcome = textOf(root, "outcome");
            return new IdentityResponse(reference, score, outcome);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.upstream("Could not parse the identity provider response");
        }
    }

    private String textOf(Element root, String tag) {
        var nodes = root.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent() == null ? "" : nodes.item(0).getTextContent().trim();
    }
}
