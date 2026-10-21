package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteaddressGenerated {
	public static final String JTE_NAME = "journey/address.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,3,3,9,9,9,12,12,14,14,17,17,17,17,17,17,17,17,17,21,21,21,21,21,21,21,21,21,26,26,26,26,26,26,26,26,26,30,30,30,30,30,30,30,30,30,35,35,35,35,35,35,35,35,35,42,42,42,43,43,43,3,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Address details", "journey", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Address details</h1>\n<p class=\"muted\">Your residential address. You will verify it with a recent address-proof document later.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/journey/address\" class=\"card\" style=\"max-width:640px;\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"addressLine1\">Address line 1</label>\n        <input class=\"input\" type=\"text\" id=\"addressLine1\" name=\"addressLine1\" required maxlength=\"160\"");
				var __jte_html_attribute_0 = view.path("applicant").path("addressLine1").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n    </div>\n    <div class=\"field\">\n        <label for=\"addressLine2\">Address line 2 (optional)</label>\n        <input class=\"input\" type=\"text\" id=\"addressLine2\" name=\"addressLine2\" maxlength=\"160\"");
				var __jte_html_attribute_1 = view.path("applicant").path("addressLine2").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n    </div>\n    <div class=\"form-row\">\n        <div class=\"field\">\n            <label for=\"city\">City</label>\n            <input class=\"input\" type=\"text\" id=\"city\" name=\"city\" required");
				var __jte_html_attribute_2 = view.path("applicant").path("city").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_2);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        </div>\n        <div class=\"field\">\n            <label for=\"postalCode\">Postal code</label>\n            <input class=\"input\" type=\"text\" id=\"postalCode\" name=\"postalCode\" required maxlength=\"12\"");
				var __jte_html_attribute_3 = view.path("applicant").path("postalCode").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_3)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_3);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        </div>\n    </div>\n    <div class=\"field\">\n        <label for=\"country\">Country</label>\n        <input class=\"input\" type=\"text\" id=\"country\" name=\"country\" required");
				var __jte_html_attribute_4 = view.path("applicant").path("country").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_4)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_4);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n    </div>\n    <div class=\"form-actions\">\n        <button class=\"btn btn-primary\" type=\"submit\">Save &amp; continue</button>\n        <a class=\"btn btn-ghost\" href=\"/journey\">Cancel</a>\n    </div>\n</form>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode view = (JsonNode)params.get("view");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, view, flash);
	}
}
