package gg.jte.generated.precompiled.compliance;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtehitsGenerated {
	public static final String JTE_NAME = "compliance/hits.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,11,11,11,16,16,27,27,27,27,27,27,27,27,27,32,32,34,34,39,39,41,41,41,41,41,41,41,42,42,42,43,43,43,44,44,44,45,45,45,46,46,46,46,46,46,46,47,47,47,47,49,49,53,53,54,54,54,55,55,55,4,5,6,7,8,9,10,10,10,10};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode result, Map<String, Object> filters, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Watchlist queue", "compliance-hits", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<div class=\"flex-between mb-2\">\n    <h1 class=\"mb-0\">Watchlist queue</h1>\n    <a class=\"btn btn-ghost\" href=\"/compliance/export/hits.csv\">Export hits (CSV)</a>\n</div>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <form method=\"get\" action=\"/compliance/hits\" class=\"filters\">\n        <select class=\"input\" name=\"disposition\">\n            <option value=\"\">All dispositions</option>\n            <option value=\"pending\">Pending</option>\n            <option value=\"escalated\">Escalated</option>\n            <option value=\"confirmed\">Confirmed</option>\n            <option value=\"false_positive\">False positive</option>\n        </select>\n        <input class=\"input\" type=\"text\" name=\"q\" placeholder=\"Search name, reference, applicant…\"");
				var __jte_html_attribute_0 = String.valueOf(filters.get("q"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <button class=\"btn btn-primary\" type=\"submit\">Filter</button>\n    </form>\n</div>\n\n");
				if (result.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">⚠</span><h3>No hits</h3>No watchlist hits match these filters.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"table-wrap\">\n        <table class=\"table\">\n            <thead><tr><th>Application</th><th>Applicant</th><th>Matched name</th><th>List source</th><th>Confidence</th><th>Disposition</th><th></th></tr></thead>\n            <tbody>\n                ");
					for (var hit : result.path("items")) {
						jteOutput.writeContent("\n                    <tr>\n                        <td><a class=\"cell-strong\" href=\"/compliance/hits/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(hit.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(hit.path("applicationReference").asText());
						jteOutput.writeContent("</a></td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("applicantName").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("matchedName").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("listSource").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("confidence").asText());
						jteOutput.writeContent("</td>\n                        <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(dispositionClass(hit.path("disposition").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(dispositionLabel(hit.path("disposition").asText()));
						jteOutput.writeContent("</span></td>\n                        <td class=\"actions\"><a class=\"btn btn-primary btn-sm\" href=\"/compliance/hits/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(hit.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">Review</a></td>\n                    </tr>\n                ");
					}
					jteOutput.writeContent("\n            </tbody>\n        </table>\n    </div>\n");
				}
				jteOutput.writeContent("\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode result = (JsonNode)params.get("result");
		Map<String, Object> filters = (Map<String, Object>)params.get("filters");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, result, filters, flash);
	}
}
