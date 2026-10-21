package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtepersonalGenerated {
	public static final String JTE_NAME = "journey/personal.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,15,15,18,18,18,18,18,18,18,18,18,23,23,23,23,23,23,23,23,23,28,28,28,28,28,28,28,28,28,36,36,36,37,37,37,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Personal details", "journey", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Personal details</h1>\n<p class=\"muted\">Your identity details are verified against official records during identity verification.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/journey/personal\" class=\"card\" style=\"max-width:640px;\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"fullName\">Full legal name</label>\n        <input class=\"input\" type=\"text\" id=\"fullName\" name=\"fullName\" required maxlength=\"120\"");
				var __jte_html_attribute_0 = view.path("applicant").path("fullName").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n    </div>\n    <div class=\"form-row\">\n        <div class=\"field\">\n            <label for=\"dateOfBirth\">Date of birth</label>\n            <input class=\"input\" type=\"date\" id=\"dateOfBirth\" name=\"dateOfBirth\" required");
				var __jte_html_attribute_1 = view.path("applicant").path("dateOfBirth").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n            <div class=\"help\">You must be at least 18 years old.</div>\n        </div>\n        <div class=\"field\">\n            <label for=\"nationalIdentifier\">National identifier (NRIC)</label>\n            <input class=\"input\" type=\"text\" id=\"nationalIdentifier\" name=\"nationalIdentifier\" required maxlength=\"40\"");
				var __jte_html_attribute_2 = view.path("applicant").path("nationalIdentifier").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_2);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        </div>\n    </div>\n    <div class=\"form-actions\">\n        <button class=\"btn btn-primary\" type=\"submit\">Save &amp; continue</button>\n        <a class=\"btn btn-ghost\" href=\"/journey\">Cancel</a>\n    </div>\n</form>\n");
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
