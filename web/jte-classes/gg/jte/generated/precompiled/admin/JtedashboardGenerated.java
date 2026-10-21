package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtedashboardGenerated {
	public static final String JTE_NAME = "admin/dashboard.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,11,11,11,13,13,15,15,15,16,16,16,17,17,17,18,18,18,18,18,18,31,31,33,33,33,34,34,34,35,35,35,36,36,36,38,38,56,56,56,57,57,57,4,5,6,7,8,9,10,10,10,10};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode dashboard, JsonNode staff, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Admin dashboard", "admin", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Bank administration</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<div class=\"grid cols-4\">\n    <div class=\"stat primary\"><span class=\"label\">Staff</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("staffCount").asText());
				jteOutput.writeContent("</span></div>\n    <div class=\"stat info\"><span class=\"label\">Branches</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("branches").asText());
				jteOutput.writeContent("</span></div>\n    <div class=\"stat accent\"><span class=\"label\">Audit events</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("auditEvents").asText());
				jteOutput.writeContent("</span><span class=\"hint\">append-only</span></div>\n    <div class=\"stat\"><span class=\"label\">Tier policies / requirements</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("kycTiers").asText());
				jteOutput.writeContent(" / ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("requirements").asText());
				jteOutput.writeContent("</span></div>\n</div>\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <div class=\"card-head\">\n            <h2 class=\"mb-0\">Staff</h2>\n            <a class=\"btn btn-primary btn-sm\" href=\"/admin/staff\">Manage staff</a>\n        </div>\n        <div class=\"table-wrap\">\n            <table class=\"table\">\n                <thead><tr><th>Name</th><th>Role</th><th>Branch</th><th>Max tier</th></tr></thead>\n                <tbody>\n                    ");
				for (var s : staff.path("items")) {
					jteOutput.writeContent("\n                        <tr>\n                            <td class=\"cell-strong\">");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(s.path("fullName").asText());
					jteOutput.writeContent("</td>\n                            <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(s.path("role").asText());
					jteOutput.writeContent("</td>\n                            <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(s.path("branchName").asText());
					jteOutput.writeContent("</td>\n                            <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(s.path("maxApprovalTier").asText());
					jteOutput.writeContent("</td>\n                        </tr>\n                    ");
				}
				jteOutput.writeContent("\n                </tbody>\n            </table>\n        </div>\n    </div>\n    <div class=\"card\">\n        <div class=\"card-head\"><h2 class=\"mb-0\">Configuration shortcuts</h2></div>\n        <ul class=\"small\" style=\"padding-left:18px;color:var(--muted);line-height:2.2;\">\n            <li><a href=\"/admin/staff\">Staff management</a> — roles, branches, approval tiers, enable/disable, roster CSV import.</li>\n            <li><a href=\"/admin/branches\">Branches</a> — create branches or import reference XML.</li>\n            <li><a href=\"/admin/kyc-tiers\">KYC tier policy</a> — transaction limits and balance ceilings (SGD).</li>\n            <li><a href=\"/admin/requirements\">Document requirements</a> — required documents per tier and condition.</li>\n            <li><a href=\"/admin/audit\">Audit log</a> — read-only, append-only, exportable.</li>\n            <li><a href=\"/admin/integrations\">Integrations</a> — webhooks and provider credentials.</li>\n        </ul>\n        <div class=\"notice-strip info\">Bank admins manage configuration and read audits — they cannot approve, decline, verify documents or disposition hits.</div>\n    </div>\n</div>\n");
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
		JsonNode staff = (JsonNode)params.get("staff");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, dashboard, staff, flash);
	}
}
