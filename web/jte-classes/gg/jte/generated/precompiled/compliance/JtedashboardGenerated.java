package gg.jte.generated.precompiled.compliance;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtedashboardGenerated {
	public static final String JTE_NAME = "compliance/dashboard.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,12,12,12,15,15,18,18,18,19,19,19,20,20,20,21,21,21,30,30,32,32,37,37,39,39,39,39,39,39,39,40,40,40,41,41,41,42,42,42,44,44,48,48,55,55,57,57,62,62,64,64,64,64,64,64,64,65,65,65,66,66,66,68,68,72,72,83,83,83,84,84,84,4,5,6,7,8,9,10,11,11,11,11};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode dashboard, JsonNode hits, JsonNode escalated, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Compliance dashboard", "compliance", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Compliance overview</h1>\n<p class=\"muted\">You act across all branches.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid cols-4\">\n    <div class=\"stat danger\"><span class=\"label\">Pending hits</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("pendingHits").asText());
				jteOutput.writeContent("</span><span class=\"hint\">awaiting disposition</span></div>\n    <div class=\"stat accent\"><span class=\"label\">Escalated</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("escalatedHits").asText());
				jteOutput.writeContent("</span><span class=\"hint\">senior review</span></div>\n    <div class=\"stat info\"><span class=\"label\">Applications on hold</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("onHold").asText());
				jteOutput.writeContent("</span><span class=\"hint\">watchlist holds</span></div>\n    <div class=\"stat primary\"><span class=\"label\">Open re-KYC cases</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("activeRekycCases").asText());
				jteOutput.writeContent("</span><span class=\"hint\">across campaigns</span></div>\n</div>\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <div class=\"card-head\">\n            <h2 class=\"mb-0\">Pending dispositions</h2>\n            <a class=\"btn btn-ghost btn-sm\" href=\"/compliance/hits\">Open queue</a>\n        </div>\n        ");
				if (hits.path("items").size() == 0) {
					jteOutput.writeContent("\n            <div class=\"empty\"><span class=\"glyph\">⚠</span><h3>No pending hits</h3></div>\n        ");
				} else {
					jteOutput.writeContent("\n            <div class=\"table-wrap\">\n                <table class=\"table\">\n                    <thead><tr><th>Applicant</th><th>Matched name</th><th>Source</th><th>Confidence</th></tr></thead>\n                    <tbody>\n                        ");
					for (var hit : hits.path("items")) {
						jteOutput.writeContent("\n                            <tr>\n                                <td><a class=\"cell-strong\" href=\"/compliance/hits/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(hit.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(hit.path("applicantName").asText());
						jteOutput.writeContent("</a></td>\n                                <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("matchedName").asText());
						jteOutput.writeContent("</td>\n                                <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("listSource").asText());
						jteOutput.writeContent("</td>\n                                <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("confidence").asText());
						jteOutput.writeContent("</td>\n                            </tr>\n                        ");
					}
					jteOutput.writeContent("\n                    </tbody>\n                </table>\n            </div>\n        ");
				}
				jteOutput.writeContent("\n    </div>\n    <div class=\"card\">\n        <div class=\"card-head\">\n            <h2 class=\"mb-0\">Escalated</h2>\n            <a class=\"btn btn-ghost btn-sm\" href=\"/compliance/hits?disposition=escalated\">Open queue</a>\n        </div>\n        ");
				if (escalated.path("items").size() == 0) {
					jteOutput.writeContent("\n            <div class=\"empty\"><span class=\"glyph\">✓</span><h3>Nothing escalated</h3></div>\n        ");
				} else {
					jteOutput.writeContent("\n            <div class=\"table-wrap\">\n                <table class=\"table\">\n                    <thead><tr><th>Applicant</th><th>Matched name</th><th>Source</th></tr></thead>\n                    <tbody>\n                        ");
					for (var hit : escalated.path("items")) {
						jteOutput.writeContent("\n                            <tr>\n                                <td><a class=\"cell-strong\" href=\"/compliance/hits/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(hit.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(hit.path("applicantName").asText());
						jteOutput.writeContent("</a></td>\n                                <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("matchedName").asText());
						jteOutput.writeContent("</td>\n                                <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("listSource").asText());
						jteOutput.writeContent("</td>\n                            </tr>\n                        ");
					}
					jteOutput.writeContent("\n                    </tbody>\n                </table>\n            </div>\n        ");
				}
				jteOutput.writeContent("\n    </div>\n</div>\n\n<div class=\"card\">\n    <div class=\"card-head\">\n        <h2 class=\"mb-0\">Re-KYC campaigns</h2>\n        <a class=\"btn btn-primary btn-sm\" href=\"/compliance/rekyc\">Manage campaigns</a>\n    </div>\n    <p class=\"muted small\">Create periodic document-refresh campaigns and import customer lists (CSV).</p>\n</div>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode dashboard = (JsonNode)params.get("dashboard");
		JsonNode hits = (JsonNode)params.get("hits");
		JsonNode escalated = (JsonNode)params.get("escalated");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, dashboard, hits, escalated, flash);
	}
}
