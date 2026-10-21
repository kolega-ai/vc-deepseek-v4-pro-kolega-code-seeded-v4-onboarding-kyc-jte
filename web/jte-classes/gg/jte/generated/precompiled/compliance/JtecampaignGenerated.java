package gg.jte.generated.precompiled.compliance;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtecampaignGenerated {
	public static final String JTE_NAME = "compliance/campaign.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,13,14,14,14,14,14,14,18,18,23,23,23,23,24,24,34,34,36,36,41,41,43,43,43,44,44,44,45,45,45,46,46,46,46,46,46,46,47,47,47,48,48,48,50,50,54,54,55,55,55,56,56,56,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode campaign, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Re-KYC campaign", "compliance-rekyc", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<div class=\"flex-between mb-2\">\n    <div>\n        <h1 class=\"mb-0\">");
				jteOutput.setContext("h1", null);
				jteOutput.writeUserContent(campaign.path("name").asText());
				jteOutput.writeContent("</h1>\n        <span class=\"muted small\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(campaign.path("targetSegment").asText());
				jteOutput.writeContent(" · due ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(campaign.path("dueDays").asText());
				jteOutput.writeContent(" days after creation</span>\n    </div>\n    <a class=\"btn btn-ghost\" href=\"/compliance/rekyc\">Back to campaigns</a>\n</div>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <h3>Import customer list (CSV)</h3>\n    <p class=\"muted small\">One identifier per row: an approved customer's email or national identifier. Header <span class=\"mono\">identifier</span>. Rows with errors are reported.</p>\n    <form method=\"post\" action=\"/compliance/rekyc/campaigns/");
				jteOutput.setContext("form", "action");
				jteOutput.writeUserContent(campaign.path("id").asText());
				jteOutput.setContext("form", null);
				jteOutput.writeContent("/import\" enctype=\"multipart/form-data\">\n        ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n        <div class=\"dropzone\" style=\"max-width:520px;\">\n            <span class=\"big\">⇪</span>\n            <input type=\"file\" name=\"file\" accept=\".csv,text/csv\" required data-label=\"fileLabel\">\n            <div id=\"fileLabel\">Choose a CSV file…</div>\n        </div>\n        <button class=\"btn btn-primary mt-2\" type=\"submit\">Import cases</button>\n    </form>\n</div>\n\n");
				if (campaign.path("cases").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">↻</span><h3>No cases yet</h3>Import a customer list to create due cases.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"table-wrap\">\n        <table class=\"table\">\n            <thead><tr><th>Customer</th><th>Email</th><th>Application</th><th>Status</th><th>Due date</th><th>Origin</th></tr></thead>\n            <tbody>\n                ");
					for (var kase : campaign.path("cases")) {
						jteOutput.writeContent("\n                    <tr>\n                        <td class=\"cell-strong\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(kase.path("customerName").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(kase.path("email").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(kase.path("applicationReference").asText());
						jteOutput.writeContent("</td>\n                        <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(kase.path("status").asText().equals("overdue") ? "badge-danger" : kase.path("status").asText().equals("completed") ? "badge-success" : kase.path("status").asText().equals("in_progress") ? "badge-primary" : "badge-neutral");
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(kase.path("status").asText());
						jteOutput.writeContent("</span></td>\n                        <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDate(kase.path("dueDate").asText()));
						jteOutput.writeContent("</td>\n                        <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(kase.path("origin").asText());
						jteOutput.writeContent("</td>\n                    </tr>\n                ");
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
		JsonNode campaign = (JsonNode)params.get("campaign");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, campaign, flash);
	}
}
