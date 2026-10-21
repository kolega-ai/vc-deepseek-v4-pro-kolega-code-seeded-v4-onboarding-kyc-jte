package gg.jte.generated.precompiled.officer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteapplicationsGenerated {
	public static final String JTE_NAME = "officer/applications.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,11,11,11,14,14,36,36,36,36,36,36,36,36,36,37,37,37,37,37,37,37,37,37,38,38,38,38,38,38,38,38,38,47,47,47,47,51,51,53,53,58,58,60,60,60,60,60,60,60,61,61,61,61,61,61,62,62,62,62,62,62,62,63,63,63,63,63,63,63,64,64,64,65,65,65,66,66,66,66,68,68,73,73,73,73,73,73,73,73,73,74,74,75,75,75,75,75,75,75,75,75,75,75,75,75,75,75,75,76,76,77,77,78,78,78,78,78,78,78,78,78,78,78,78,78,78,78,78,79,79,81,81,82,82,82,83,83,83,4,5,6,7,8,9,10,10,10,10};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode result, Map<String, Object> filters, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Applications", "officer-apps", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Applications</h1>\n<p class=\"muted\">Branch-scoped view: only applications from your branch are shown and downloadable.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <form method=\"get\" action=\"/officer/applications\" class=\"filters\">\n        <select class=\"input\" name=\"status\">\n            <option value=\"\">All statuses</option>\n            <option value=\"in_progress\">In progress</option>\n            <option value=\"submitted\">Submitted</option>\n            <option value=\"under_review\">Under review</option>\n            <option value=\"documents_requested\">Documents requested</option>\n            <option value=\"watchlist_hold\">Watchlist hold</option>\n            <option value=\"approved\">Approved</option>\n            <option value=\"declined\">Declined</option>\n            <option value=\"abandoned\">Abandoned</option>\n        </select>\n        <select class=\"input\" name=\"kycTier\">\n            <option value=\"\">All tiers</option>\n            <option value=\"tier_0\">tier_0</option>\n            <option value=\"tier_1\">tier_1</option>\n            <option value=\"tier_2\">tier_2</option>\n            <option value=\"tier_3\">tier_3</option>\n        </select>\n        <input class=\"input\" type=\"text\" name=\"q\" placeholder=\"Search reference, name, email…\"");
				var __jte_html_attribute_0 = String.valueOf(filters.get("q"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <input class=\"input\" type=\"date\" name=\"from\"");
				var __jte_html_attribute_1 = String.valueOf(filters.get("from"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <input class=\"input\" type=\"date\" name=\"to\"");
				var __jte_html_attribute_2 = String.valueOf(filters.get("to"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_2);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <select class=\"input\" name=\"sort\">\n            <option value=\"created\">Newest first</option>\n            <option value=\"reference\">Reference</option>\n            <option value=\"applicant\">Applicant</option>\n            <option value=\"tier\">KYC tier</option>\n            <option value=\"status\">Status</option>\n        </select>\n        <button class=\"btn btn-primary\" type=\"submit\">Filter</button>\n        <a class=\"btn btn-ghost\" href=\"/officer/export.csv?status=");
				jteOutput.setContext("a", "href");
				jteOutput.writeUserContent(String.valueOf(filters.get("status")));
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\">Export CSV</a>\n    </form>\n</div>\n\n");
				if (result.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">▦</span><h3>No applications</h3>No applications match these filters in your branch.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"table-wrap\">\n        <table class=\"table\">\n            <thead><tr><th>Reference</th><th>Applicant</th><th>Status</th><th>KYC tier</th><th>Created</th><th>Submitted</th><th></th></tr></thead>\n            <tbody>\n                ");
					for (var app : result.path("items")) {
						jteOutput.writeContent("\n                    <tr>\n                        <td><a class=\"cell-strong\" href=\"/officer/applications/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(app.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(app.path("reference").asText());
						jteOutput.writeContent("</a></td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(app.path("applicantName").asText());
						jteOutput.writeContent("<br><span class=\"cell-muted\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(app.path("applicantEmail").asText());
						jteOutput.writeContent("</span></td>\n                        <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(statusClass(app.path("status").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(statusLabel(app.path("status").asText()));
						jteOutput.writeContent("</span></td>\n                        <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(tierClass(app.path("kycTier").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(app.path("kycTier").asText());
						jteOutput.writeContent("</span></td>\n                        <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDate(app.path("createdAt").asText()));
						jteOutput.writeContent("</td>\n                        <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDateTime(app.path("submittedAt").asText()));
						jteOutput.writeContent("</td>\n                        <td class=\"actions\"><a class=\"btn btn-primary btn-sm\" href=\"/officer/applications/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(app.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">Review</a></td>\n                    </tr>\n                ");
					}
					jteOutput.writeContent("\n            </tbody>\n        </table>\n    </div>\n    <div class=\"pagination\">\n        <span class=\"meta\">");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(result.path("total").asText());
					jteOutput.writeContent(" applications · page ");
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
						jteOutput.writeContent("&status=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("status")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&q=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("q")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&sort=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("sort")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">← Previous</a>\n        ");
					}
					jteOutput.writeContent("\n        ");
					if (result.path("page").asInt() + 1 < result.path("pages").asInt()) {
						jteOutput.writeContent("\n            <a href=\"?page=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(result.path("page").asInt() + 1);
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&status=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("status")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&q=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("q")));
						jteOutput.setContext("a", null);
						jteOutput.writeContent("&sort=");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(String.valueOf(filters.get("sort")));
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
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, result, filters, flash);
	}
}
