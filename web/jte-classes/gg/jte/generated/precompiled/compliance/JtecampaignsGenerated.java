package gg.jte.generated.precompiled.compliance;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtecampaignsGenerated {
	public static final String JTE_NAME = "compliance/campaigns.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,18,18,48,48,50,50,55,55,57,57,57,57,57,57,57,58,58,58,59,59,59,60,60,60,61,61,61,62,62,62,63,63,63,64,64,64,64,66,66,70,70,71,71,71,72,72,72,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode campaigns, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Re-KYC campaigns", "compliance-rekyc", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Re-KYC campaigns</h1>\n<p class=\"muted\">Create periodic document-refresh campaigns for existing customers, then import customer lists (CSV: one email or national identifier per row).</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <h3>Create a campaign</h3>\n    <form method=\"post\" action=\"/compliance/rekyc/campaigns\">\n        ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n        <div class=\"form-row\">\n            <div class=\"field\">\n                <label for=\"name\">Campaign name</label>\n                <input class=\"input\" type=\"text\" id=\"name\" name=\"name\" required maxlength=\"160\" placeholder=\"H2 2026 Periodic Refresh\">\n            </div>\n            <div class=\"field\">\n                <label for=\"targetSegment\">Target segment</label>\n                <input class=\"input\" type=\"text\" id=\"targetSegment\" name=\"targetSegment\" placeholder=\"tier_2,tier_3 customers\">\n            </div>\n        </div>\n        <div class=\"form-row\">\n            <div class=\"field\">\n                <label for=\"targetKycTier\">Target tier (optional)</label>\n                <select class=\"input\" id=\"targetKycTier\" name=\"targetKycTier\">\n                    <option value=\"\">Use each customer's current tier</option>\n                    <option value=\"tier_1\">tier_1</option>\n                    <option value=\"tier_2\">tier_2</option>\n                    <option value=\"tier_3\">tier_3</option>\n                </select>\n            </div>\n            <div class=\"field\">\n                <label for=\"dueDays\">Due period (days)</label>\n                <input class=\"input\" type=\"number\" id=\"dueDays\" name=\"dueDays\" required min=\"7\" max=\"365\" value=\"90\">\n            </div>\n        </div>\n        <button class=\"btn btn-primary\" type=\"submit\">Create campaign</button>\n    </form>\n</div>\n\n");
				if (campaigns.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">↻</span><h3>No campaigns yet</h3>Create your first re-KYC campaign above.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"table-wrap\">\n        <table class=\"table\">\n            <thead><tr><th>Name</th><th>Segment</th><th>Target tier</th><th>Due period</th><th>Cases</th><th>Open</th><th>Created</th><th></th></tr></thead>\n            <tbody>\n                ");
					for (var campaign : campaigns.path("items")) {
						jteOutput.writeContent("\n                    <tr>\n                        <td><a class=\"cell-strong\" href=\"/compliance/rekyc/campaigns/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(campaign.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(campaign.path("name").asText());
						jteOutput.writeContent("</a></td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(campaign.path("targetSegment").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(campaign.path("targetKycTier").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(campaign.path("dueDays").asText());
						jteOutput.writeContent(" days</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(campaign.path("cases").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(campaign.path("openCases").asText());
						jteOutput.writeContent("</td>\n                        <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDate(campaign.path("createdAt").asText()));
						jteOutput.writeContent("</td>\n                        <td class=\"actions\"><a class=\"btn btn-primary btn-sm\" href=\"/compliance/rekyc/campaigns/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(campaign.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">Open</a></td>\n                    </tr>\n                ");
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
		JsonNode campaigns = (JsonNode)params.get("campaigns");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, campaigns, flash);
	}
}
