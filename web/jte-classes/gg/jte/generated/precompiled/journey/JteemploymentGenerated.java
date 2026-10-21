package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteemploymentGenerated {
	public static final String JTE_NAME = "journey/employment.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,3,3,9,9,9,12,12,14,14,27,27,27,27,27,27,27,27,27,32,32,32,32,32,32,32,32,32,39,39,39,40,40,40,3,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Employment & income", "journey", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Employment &amp; income</h1>\n<p class=\"muted\">Self-employed applicants, and anyone declaring S$100,000 or more, need a verified proof of income for any account tier.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/journey/employment\" class=\"card\" style=\"max-width:640px;\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"employmentStatus\">Employment status</label>\n        <select class=\"input\" id=\"employmentStatus\" name=\"employmentStatus\" required>\n            <option value=\"employed\">Employed</option>\n            <option value=\"self_employed\">Self-employed</option>\n            <option value=\"student\">Student</option>\n            <option value=\"retired\">Retired</option>\n            <option value=\"unemployed\">Unemployed</option>\n        </select>\n    </div>\n    <div class=\"field\">\n        <label for=\"employerName\">Employer name</label>\n        <input class=\"input\" type=\"text\" id=\"employerName\" name=\"employerName\" maxlength=\"160\"");
				var __jte_html_attribute_0 = view.path("applicant").path("employerName").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <div class=\"help\">Required if you are employed or self-employed.</div>\n    </div>\n    <div class=\"field\">\n        <label for=\"declaredAnnualIncome\">Declared annual income (SGD)</label>\n        <input class=\"input\" type=\"number\" id=\"declaredAnnualIncome\" name=\"declaredAnnualIncome\" required min=\"0\" step=\"0.01\"");
				var __jte_html_attribute_1 = view.path("applicant").path("declaredAnnualIncome").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_1);
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
