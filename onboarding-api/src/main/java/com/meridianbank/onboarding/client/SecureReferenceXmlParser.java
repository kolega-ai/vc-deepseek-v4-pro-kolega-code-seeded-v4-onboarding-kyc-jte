package com.meridianbank.onboarding.client;

import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * SecureReferenceXmlParser — hardened parser for a SEPARATE, benign reference-data XML import
 * (branch reference data uploaded by bank admins).
 *
 * DTDs and external entity resolution are explicitly disabled, so a crafted document cannot read
 * local files or reach the network. This parser is intentionally distinct from
 * {@link ProviderResponseXmlParser}, the identity-provider response ingest.
 */
@Component
public class SecureReferenceXmlParser {

    public record BranchRow(String code, String name, String region) {
    }

    public List<BranchRow> parseBranches(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            // Reject every external lookup outright; no file or network access is possible.
            builder.setEntityResolver((publicId, systemId) -> {
                throw new SAXParseException("External entity resolution is forbidden", null, null, -1, -1);
            });

            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            Element root = doc.getDocumentElement();
            if (!"reference-data".equals(root.getNodeName())) {
                throw ApiException.badRequest("Expected a <reference-data> root element");
            }
            List<BranchRow> rows = new ArrayList<>();
            NodeList branchNodes = root.getElementsByTagName("branch");
            for (int i = 0; i < branchNodes.getLength(); i++) {
                Element branch = (Element) branchNodes.item(i);
                rows.add(new BranchRow(
                        textOf(branch, "code"),
                        textOf(branch, "name"),
                        textOf(branch, "region")));
            }
            return rows;
        } catch (ApiException e) {
            throw e;
        } catch (SAXParseException e) {
            throw ApiException.badRequest("XML rejected: external entities and DTDs are not allowed");
        } catch (SAXException e) {
            throw ApiException.badRequest("Invalid XML document: " + e.getMessage());
        } catch (Exception e) {
            throw ApiException.badRequest("Could not parse the reference XML document");
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
