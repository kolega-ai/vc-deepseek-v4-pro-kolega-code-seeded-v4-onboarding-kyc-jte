package com.meridianbank.onboarding.client;

/**
 * Builds the XML request sent to the identity provider mock. All applicant-supplied values are
 * XML-escaped before interpolation so they cannot break out of element content.
 */
public final class IdentityRequestXmlBuilder {

    private IdentityRequestXmlBuilder() {
    }

    public static String build(String fullName, String nationalIdentifier, String dateOfBirth) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<identity-verification-request>\n"
                + "  <applicant>\n"
                + "    <fullName>" + escape(fullName) + "</fullName>\n"
                + "    <nationalId>" + escape(nationalIdentifier) + "</nationalId>\n"
                + "    <dateOfBirth>" + escape(dateOfBirth) + "</dateOfBirth>\n"
                + "  </applicant>\n"
                + "  <channel>MERIDIAN_ONBOARDING</channel>\n"
                + "</identity-verification-request>";
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
