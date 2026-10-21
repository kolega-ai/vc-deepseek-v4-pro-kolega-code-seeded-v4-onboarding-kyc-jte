package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteauditGenerated {
	public static final String JTE_NAME = "admin/audit.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,16,16,16,16,16,16,16,16,21,21,21,21,21,21,21,21,21,22,22,22,22,22,22,22,22,22,23,23,23,23,23,23,23,23,23,28,28,30,30,35,35,37,37,37,38,38,38,38,38,38,39,39,39,40,40,40,40,40,40,41,41,41,42,42,42,43,43,43,45,45,50,50,50,50,50,50,50,50,50,51,51,52,52,52,52,52,52,52,52,52,52,52,52,53,53,54,54,55,55,55,55,55,55,55,55,55,55,55,55,56,56,58,58,59,59,59,60,60,60,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode result, Map<String, Object> filters) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Audit log", "admin-audit", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<div class=\"flex-between mb-2\">\n    <div>\n        <h1 class=\"mb-0\">Audit log</h1>\n        <span class=\"muted small\">Append-only: every state transition and privileged action. No role can modify or delete these records.</span>\n    </div>\n    <a class=\"btn btn-ghost\" href=\"/admin/audit/export.csv?action=");
				jteOutput.setContext("a", "href");
				jteOutput.writeUserContent(String.valueOf(filters.get("action")));
				jteOutput.setContext("a", null);
				jteOutput.writeContent("&actor=");
				jteOutput.setContext("a", "href");
				jteOutput.writeUserContent(String.valueOf(filters.get("actor")));
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\">Export CSV</a>\n</div>\n\n<div class=\"card\">\n    <form method=\"get\" action=\"/admin/audit\" class=\"filters\">\n        <input class=\"input\" type=\"text\" name=\"action\" placeholder=\"Action, e.g. APPLICATION_APPROVED\"");
				var __jte_html_attribute_0 = String.valueOf(filters.get("action"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <input class=\"input\" type=\"text\" name=\"actor\" placeholder=\"Actor email\"");
				var __jte_html_attribute_1 = String.valueOf(filters.get("actor"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <input class=\"input\" type=\"text\" name=\"entity\" placeholder=\"Entity id, e.g. ONB-2026-…\"");
				var __jte_html_attribute_2 = String.valueOf(filters.get("entity"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_2);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <button class=\"btn btn-primary\" type=\"submit\">Filter</button>\n    </form>\n</div>\n\n");
				if (result.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">≡</span><h3>No audit events</h3>No events match these filters.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"table-wrap\">\n        <table class=\"table\">\n            <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Entity</th><th>Before</th><th>After</th><th>Authorization basis</th></tr></thead>\n            <tbody>\n                ");
					for (var event : result.path("items")) {
						jteOutput.writeContent("\n                    <tr>\n                        <td class=\"cell-muted\" style=\"white-space:nowrap;\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDateTime(event.path("createdAt").asText()));
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(event.path("actor").asText());
						jteOutput.writeContent("<br><span class=\"cell-muted\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(event.path("actorType").asText());
						jteOutput.writeContent("</span></td>\n                        <td class=\"cell-strong\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(event.path("action").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(event.path("entityType").asText());
						jteOutput.writeContent("<br><span class=\"cell-muted mono\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(event.path("entityId").asText());
						jteOutput.writeContent("</span></td>\n                        <td class=\"cell-muted small\" style=\"max-width:220px;overflow-wrap:anywhere;\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(event.path("beforeValue").asText());
						jteOutput.writeContent("</td>\n                        <td class=\"cell-muted small\" style=\"max-width:220px;overflow-wrap:anywhere;\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(event.path("afterValue").asText());
						jteOutput.writeContent("</td>\n                        <td class=\"cell-muted small\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(event.path("authorizationBasis").asText());
						jteOutput.writeContent("</td>\n                    </tr>\n                ");
					}
					jteOutput.writeContent("\n            </tbody>\n        </table>\n    </div>\n    <div class=\"pagination\">\n        <span class=\"meta\">");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(result.path("total").asText());
					jteOutput.writeContent(" events · page ");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(result.path("page").asInt() + 1);
					jteOutput.writeContent(" of ");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(result.path("pages").asInt());
					jteOutput.writeContent("</span>\n        ");
					if (result.path("page").asInt() > 0) {
						jteOutput.writeContent("\n            <a href=\"?page=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(result.path("page").asInt() - 1);
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&action=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("action")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&actor=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("actor")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">← Previous</a>\n        ");
					}
					jteOutput.writeContent("\n        ");
					if (result.path("page").asInt() + 1 < result.path("pages").asInt()) {
						jteOutput.writeContent("\n            <a href=\"?page=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(result.path("page").asInt() + 1);
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&action=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("action")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&actor=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("actor")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">Next →</a>\n        ");
					}
					jteOutput.writeContent("\n    </div>\n");
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
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, result, filters);
	}
}
