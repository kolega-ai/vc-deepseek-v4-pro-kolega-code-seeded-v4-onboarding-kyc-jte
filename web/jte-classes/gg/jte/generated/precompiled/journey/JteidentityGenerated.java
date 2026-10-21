package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteidentityGenerated {
	public static final String JTE_NAME = "journey/identity.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,20,20,26,26,28,28,29,29,30,30,32,32,33,33,33,34,34,35,35,35,36,36,37,37,37,39,39,39,39,39,39,40,40,43,43,43,45,45,45,46,46,47,47,48,48,50,50,52,52,53,53,56,56,56,57,57,57,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Identity verification", "journey", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Identity verification</h1>\n<p class=\"muted\">We verify your identity against our external identity provider. A match score of <b>80 or above</b> is required.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Run verification</h3>\n        <p class=\"muted small\">All required documents must be uploaded first. The provider returns an XML response with a match score.</p>\n        <form method=\"post\" action=\"/journey/identity/verify\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <button class=\"btn btn-primary\" type=\"submit\">Run identity verification</button>\n        </form>\n    </div>\n    <div class=\"card\">\n        <h3>Latest result</h3>\n        ");
				if (view.path("identityResults").size() == 0) {
					jteOutput.writeContent("\n            <p class=\"muted small\">No verification has been run yet.</p>\n        ");
				} else {
					jteOutput.writeContent("\n            ");
					for (var result : view.path("identityResults")) {
						jteOutput.writeContent("\n                ");
						if (result.path("status").asText().equals("SUCCESS")) {
							jteOutput.writeContent("\n                    <div class=\"flex-between mb-1\">\n                        ");
							if (result.path("matchScore").asInt() >= 80) {
								jteOutput.writeContent("\n                            <span class=\"badge badge-success\"><span class=\"dot\"></span>score ");
								jteOutput.setContext("span", null);
								jteOutput.writeUserContent(result.path("matchScore").asText());
								jteOutput.writeContent("</span>\n                        ");
							} else {
								jteOutput.writeContent("\n                            <span class=\"badge badge-danger\"><span class=\"dot\"></span>score ");
								jteOutput.setContext("span", null);
								jteOutput.writeUserContent(result.path("matchScore").asText());
								jteOutput.writeContent("</span>\n                        ");
							}
							jteOutput.writeContent("\n                        <span class=\"small faint\">");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(fmtDateTime(result.path("createdAt").asText()));
							jteOutput.writeContent("</span>\n                    </div>\n                    <p class=\"small muted\">Outcome: ");
							jteOutput.setContext("p", null);
							jteOutput.writeUserContent(result.path("outcome").asText());
							jteOutput.writeContent(" · provider ref <span class=\"mono\">");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(result.path("providerReference").asText());
							jteOutput.writeContent("</span></p>\n                ");
						} else {
							jteOutput.writeContent("\n                    <div class=\"flex-between mb-1\">\n                        <span class=\"badge badge-warning\"><span class=\"dot\"></span>provider error</span>\n                        <span class=\"small faint\">");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(fmtDateTime(result.path("createdAt").asText()));
							jteOutput.writeContent("</span>\n                    </div>\n                    <p class=\"small muted\">");
							jteOutput.setContext("p", null);
							jteOutput.writeUserContent(result.path("errorMessage").asText());
							jteOutput.writeContent("</p>\n                ");
						}
						jteOutput.writeContent("\n            ");
					}
					jteOutput.writeContent("\n            ");
					if (view.path("identityPassed").asBoolean()) {
						jteOutput.writeContent("\n                <div class=\"alert alert-success mt-2\"><span class=\"icon\">✓</span><span>Identity verification passed.</span></div>\n            ");
					} else {
						jteOutput.writeContent("\n                <div class=\"alert alert-error\"><span class=\"icon\">!</span><span>Score below 80 — the application cannot progress until verification passes.</span></div>\n            ");
					}
					jteOutput.writeContent("\n        ");
				}
				jteOutput.writeContent("\n    </div>\n</div>\n");
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
