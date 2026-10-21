package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtekyctiersGenerated {
	public static final String JTE_NAME = "admin/kyc-tiers.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,16,16,19,19,19,19,19,19,20,20,20,23,23,23,24,24,24,25,25,25,30,30,30,30,31,31,34,34,34,34,34,34,34,34,34,38,38,38,38,38,38,38,38,38,45,45,47,47,47,48,48,48,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode tiers, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "KYC tier policy", "admin-tiers", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>KYC tier policy</h1>\n<p class=\"muted\">Seeded reference policy in SGD. Limits are read server-side from this table at account opening and re-KYC — never accepted from the client. All changes are audited.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid cols-2\">\n    ");
				for (var tier : tiers.path("items")) {
					jteOutput.writeContent("\n        <div class=\"card\">\n            <div class=\"card-head\">\n                <h2 class=\"mb-0\">");
					jteOutput.setContext("h2", null);
					jteOutput.writeUserContent(tierLabel(tier.path("tierCode").asText()));
					jteOutput.writeContent(" <span class=\"muted small mono\">");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(tier.path("tierCode").asText());
					jteOutput.writeContent("</span></h2>\n                <span class=\"small muted\">");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(tier.path("currency").asText());
					jteOutput.writeContent("</span>\n            </div>\n            <dl class=\"kv\">\n                <dt>Minimum identity match</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(tier.path("minIdentityMatch").asText());
					jteOutput.writeContent("</dd>\n                <dt>Daily transaction limit</dt><dd class=\"cell-strong\">");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(money(tier.path("dailyTransactionLimit").asText()));
					jteOutput.writeContent("</dd>\n                <dt>Balance ceiling</dt><dd class=\"cell-strong\">");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(money(tier.path("balanceCeiling").asText()));
					jteOutput.writeContent("</dd>\n            </dl>\n            <details class=\"panel mt-2\">\n                <summary class=\"small\">Edit limits</summary>\n                <div class=\"panel-body\">\n                    <form method=\"post\" action=\"/admin/kyc-tiers/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(tier.path("tierCode").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("\">\n                        ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                        <div class=\"field\">\n                            <label>Daily transaction limit</label>\n                            <input class=\"input\" type=\"number\" name=\"dailyTransactionLimit\" min=\"0\" step=\"0.01\" required");
					var __jte_html_attribute_0 = tier.path("dailyTransactionLimit").asText();
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_0);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n                        </div>\n                        <div class=\"field\">\n                            <label>Balance ceiling</label>\n                            <input class=\"input\" type=\"number\" name=\"balanceCeiling\" min=\"0\" step=\"0.01\" required");
					var __jte_html_attribute_1 = tier.path("balanceCeiling").asText();
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_1);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n                        </div>\n                        <button class=\"btn btn-primary btn-sm\" type=\"submit\">Save limits</button>\n                    </form>\n                </div>\n            </details>\n        </div>\n    ");
				}
				jteOutput.writeContent("\n</div>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode tiers = (JsonNode)params.get("tiers");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, tiers, flash);
	}
}
