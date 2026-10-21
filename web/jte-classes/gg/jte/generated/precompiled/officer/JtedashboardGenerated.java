package gg.jte.generated.precompiled.officer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtedashboardGenerated {
	public static final String JTE_NAME = "officer/dashboard.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,11,11,11,12,12,12,13,13,15,15,15,16,16,16,17,17,17,18,18,18,18,18,18,18,18,18,27,27,29,29,34,34,36,36,36,36,36,36,36,37,37,37,38,38,38,38,38,38,38,39,39,39,41,41,45,45,55,55,55,58,58,58,59,59,59,4,5,6,7,8,9,10,10,10,10};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode dashboard, JsonNode queue, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Officer dashboard", "officer", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>");
				jteOutput.setContext("h1", null);
				jteOutput.writeUserContent(dashboard.path("branch").asText());
				jteOutput.writeContent(" branch</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<div class=\"grid cols-4\">\n    <div class=\"stat primary\"><span class=\"label\">Under review</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("underReview").asText());
				jteOutput.writeContent("</span><span class=\"hint\">your branch</span></div>\n    <div class=\"stat accent\"><span class=\"label\">Documents to verify</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("awaitingVerification").asText());
				jteOutput.writeContent("</span><span class=\"hint\">uploaded state</span></div>\n    <div class=\"stat info\"><span class=\"label\">Documents requested</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("documentsRequested").asText());
				jteOutput.writeContent("</span><span class=\"hint\">returned to applicants</span></div>\n    <div class=\"stat\"><span class=\"label\">Approved / declined</span><span class=\"value\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("approved").asText());
				jteOutput.writeContent(" / ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("declined").asText());
				jteOutput.writeContent("</span><span class=\"hint\">approval authority: ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dashboard.path("maxApprovalTier").asText());
				jteOutput.writeContent("</span></div>\n</div>\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <div class=\"card-head\">\n            <h2 class=\"mb-0\">Review queue</h2>\n            <a class=\"btn btn-ghost btn-sm\" href=\"/officer/applications?status=under_review\">Open full list</a>\n        </div>\n        ");
				if (queue.path("items").size() == 0) {
					jteOutput.writeContent("\n            <div class=\"empty\"><span class=\"glyph\">▦</span><h3>Queue is clear</h3>Nothing waiting for review in your branch.</div>\n        ");
				} else {
					jteOutput.writeContent("\n            <div class=\"table-wrap\">\n                <table class=\"table\">\n                    <thead><tr><th>Reference</th><th>Applicant</th><th>Tier</th><th>Submitted</th></tr></thead>\n                    <tbody>\n                        ");
					for (var app : queue.path("items")) {
						jteOutput.writeContent("\n                            <tr>\n                                <td><a class=\"cell-strong\" href=\"/officer/applications/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(app.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("\">");
						jteOutput.setContext("a", null);
						jteOutput.writeUserContent(app.path("reference").asText());
						jteOutput.writeContent("</a></td>\n                                <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(app.path("applicantName").asText());
						jteOutput.writeContent("</td>\n                                <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(tierClass(app.path("kycTier").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(app.path("kycTier").asText());
						jteOutput.writeContent("</span></td>\n                                <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDateTime(app.path("submittedAt").asText()));
						jteOutput.writeContent("</td>\n                            </tr>\n                        ");
					}
					jteOutput.writeContent("\n                    </tbody>\n                </table>\n            </div>\n        ");
				}
				jteOutput.writeContent("\n    </div>\n    <div class=\"card\">\n        <div class=\"card-head\"><h2 class=\"mb-0\">Actions available</h2></div>\n        <ul class=\"small\" style=\"padding-left:18px;color:var(--muted);line-height:2.1;\">\n            <li><a href=\"/officer/applications?status=under_review\">Review applications</a> — approve, decline or request documents.</li>\n            <li><a href=\"/officer/applications?status=documents_requested\">Returned applications</a> — customers re-uploading documents.</li>\n            <li><a href=\"/officer/applications?status=in_progress\">Document verification</a> — verify uploaded documents in your branch.</li>\n            <li><a href=\"/officer/export.csv\">Export branch applications (CSV)</a>.</li>\n        </ul>\n        <div class=\"notice-strip info\">Approve and decline actions require a step-up OTP. You can approve up to tier <b>");
				jteOutput.setContext("b", null);
				jteOutput.writeUserContent(dashboard.path("maxApprovalTier").asText());
				jteOutput.writeContent("</b>.</div>\n    </div>\n</div>\n");
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
		JsonNode queue = (JsonNode)params.get("queue");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, dashboard, queue, flash);
	}
}
