package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteintegrationsGenerated {
	public static final String JTE_NAME = "admin/integrations.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,19,19,22,22,22,22,22,22,22,22,22,27,27,27,27,27,27,27,27,27,30,30,32,32,34,34,43,43,43,44,44,44,52,52,54,54,59,59,61,61,61,62,62,62,64,64,66,66,68,68,70,70,70,71,71,71,73,73,77,77,79,79,79,80,80,80,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode integrations, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Integrations", "admin-integrations", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Integrations</h1>\n<p class=\"muted\">Outbound webhooks for decision events and the local vendor credentials used by the onboarding service.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Webhook configuration</h3>\n        <form method=\"post\" action=\"/admin/integrations/webhook\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"field\">\n                <label>Endpoint URL</label>\n                <input class=\"input\" type=\"text\" name=\"endpointUrl\" placeholder=\"https://your-system.example/hooks\"");
				var __jte_html_attribute_0 = integrations.path("webhookUrl").asText();
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n                <div class=\"help\">Decision events (approved, declined, tier overridden) are POSTed here with an HMAC-SHA256 signature.</div>\n            </div>\n            <div class=\"field\">\n                <label>Signing secret</label>\n                <input class=\"input\" type=\"password\" name=\"secret\"");
				var __jte_html_attribute_1 = integrations.path("webhookSecretSet").asBoolean() ? "•••••••• (set)" : "Choose a secret";
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" placeholder=\"");
					jteOutput.setContext("input", "placeholder");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n            </div>\n            <div class=\"checkbox-row mb-2\">\n                ");
				if (integrations.path("webhookEnabled").asBoolean()) {
					jteOutput.writeContent("\n                    <input type=\"checkbox\" id=\"webhookEnabled\" name=\"enabled\" value=\"true\" checked>\n                ");
				} else {
					jteOutput.writeContent("\n                    <input type=\"checkbox\" id=\"webhookEnabled\" name=\"enabled\" value=\"true\">\n                ");
				}
				jteOutput.writeContent("\n                <label for=\"webhookEnabled\">Enable webhooks</label>\n            </div>\n            <button class=\"btn btn-primary\" type=\"submit\">Save webhook settings</button>\n        </form>\n    </div>\n    <div class=\"card\">\n        <h3>Vendor credentials</h3>\n        <dl class=\"kv\">\n            <dt>Provider mock key</dt><dd class=\"mono\">");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(integrations.path("providerKey").asText());
				jteOutput.writeContent("</dd>\n            <dt>Core mock key</dt><dd class=\"mono\">");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(integrations.path("coreKey").asText());
				jteOutput.writeContent("</dd>\n        </dl>\n        <p class=\"small muted mt-2\">These credentials are shared with the local provider-mock and core-mock services via environment variables; the onboarding service authenticates with them on every call. The core additionally requires a decision-minted scope token per account opening.</p>\n    </div>\n</div>\n\n<div class=\"card\">\n    <h3>Webhook delivery attempts</h3>\n    ");
				if (integrations.path("deliveries").size() == 0) {
					jteOutput.writeContent("\n        <div class=\"empty\"><span class=\"glyph\">⇄</span><h3>No deliveries yet</h3>Deliveries are recorded once events fire (e.g. an application is approved).</div>\n    ");
				} else {
					jteOutput.writeContent("\n        <div class=\"table-wrap\">\n            <table class=\"table\">\n                <thead><tr><th>When</th><th>Event</th><th>Result</th><th>Response</th><th>Error</th></tr></thead>\n                <tbody>\n                    ");
					for (var delivery : integrations.path("deliveries")) {
						jteOutput.writeContent("\n                        <tr>\n                            <td class=\"cell-muted\" style=\"white-space:nowrap;\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDateTime(delivery.path("attemptedAt").asText()));
						jteOutput.writeContent("</td>\n                            <td class=\"cell-strong\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(delivery.path("eventType").asText());
						jteOutput.writeContent("</td>\n                            <td>\n                                ");
						if (delivery.path("success").asBoolean()) {
							jteOutput.writeContent("\n                                    <span class=\"badge badge-success\"><span class=\"dot\"></span>delivered</span>\n                                ");
						} else {
							jteOutput.writeContent("\n                                    <span class=\"badge badge-danger\"><span class=\"dot\"></span>failed</span>\n                                ");
						}
						jteOutput.writeContent("\n                            </td>\n                            <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(delivery.path("responseCode").asText());
						jteOutput.writeContent("</td>\n                            <td class=\"cell-muted small\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(delivery.path("error").asText());
						jteOutput.writeContent("</td>\n                        </tr>\n                    ");
					}
					jteOutput.writeContent("\n                </tbody>\n            </table>\n        </div>\n    ");
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
		JsonNode integrations = (JsonNode)params.get("integrations");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, integrations, flash);
	}
}
