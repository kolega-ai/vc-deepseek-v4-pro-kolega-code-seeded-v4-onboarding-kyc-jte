package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
import static com.meridianbank.web.controller.JourneyController.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtestatusGenerated {
	public static final String JTE_NAME = "journey/status.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,5,5,5,5,5,11,11,11,13,13,17,17,17,17,17,17,17,18,18,19,19,20,20,20,21,21,22,22,22,23,23,24,24,25,25,25,30,30,32,32,34,34,37,37,37,39,39,41,41,45,45,45,46,46,46,47,47,47,47,47,47,47,47,47,47,49,49,50,50,50,51,51,52,52,52,53,53,57,57,59,59,63,63,64,64,65,65,65,66,66,67,67,67,68,68,69,69,71,71,74,74,76,76,78,78,80,80,81,81,85,85,86,86,86,87,87,90,90,94,94,99,99,103,103,104,104,105,105,105,106,106,106,5,6,7,8,9,10,10,10,10};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Application status", "status", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Application status</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"flex-between mb-2\">\n    <div class=\"flex\">\n        <span class=\"badge ");
				jteOutput.setContext("span", "class");
				jteOutput.writeUserContent(statusClass(view.path("status").asText()));
				jteOutput.setContext("span", null);
				jteOutput.writeContent("\"><span class=\"dot\"></span>");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(statusLabel(view.path("status").asText()));
				jteOutput.writeContent("</span>\n        ");
				if (view.path("decisionOutcome") != null && !view.path("decisionOutcome").isMissingNode()) {
					jteOutput.writeContent("\n            ");
					if (view.path("decisionOutcome").asText().equals("approved")) {
						jteOutput.writeContent("\n                <span class=\"badge badge-success\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(view.path("decisionOutcome").asText());
						jteOutput.writeContent("</span>\n            ");
					} else {
						jteOutput.writeContent("\n                <span class=\"badge badge-danger\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(view.path("decisionOutcome").asText());
						jteOutput.writeContent("</span>\n            ");
					}
					jteOutput.writeContent("\n        ");
				}
				jteOutput.writeContent("\n        <span class=\"small muted\">Reference <span class=\"mono\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("reference").asText());
				jteOutput.writeContent("</span></span>\n    </div>\n    <a class=\"btn btn-ghost btn-sm\" href=\"/customer/export\">Download my data (JSON)</a>\n</div>\n\n");
				if (view.path("status").asText().equals("submitted")) {
					jteOutput.writeContent("\n    <div class=\"notice-strip info\">⏳ Screening is in progress. Your application is checked against watchlists and the credit bureau automatically — this usually completes within a minute. Refresh this page.</div>\n");
				}
				jteOutput.writeContent("\n\n");
				if (view.path("customerVisibleReason") != null && !view.path("customerVisibleReason").isMissingNode() && !view.path("customerVisibleReason").asText().isEmpty()) {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <h3>What this decision means for you</h3>\n        <p class=\"mb-0\">");
					jteOutput.setContext("p", null);
					jteOutput.writeUserContent(view.path("customerVisibleReason").asText());
					jteOutput.writeContent("</p>\n    </div>\n");
				}
				jteOutput.writeContent("\n\n");
				if (view.path("account") != null && !view.path("account").isMissingNode()) {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <h3>Your account</h3>\n        <dl class=\"kv\">\n            <dt>Account number</dt><dd class=\"mono\">");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("account").path("accountNumber").asText());
					jteOutput.writeContent("</dd>\n            <dt>Product</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("account").path("product").asText());
					jteOutput.writeContent("</dd>\n            <dt>KYC tier</dt><dd><span class=\"badge ");
					jteOutput.setContext("span", "class");
					jteOutput.writeUserContent(tierClass(view.path("account").path("kycTier").asText()));
					jteOutput.setContext("span", null);
					jteOutput.writeContent("\"><span class=\"dot\"></span>");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(tierLabel(view.path("account").path("kycTier").asText()));
					jteOutput.writeContent(" (");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("account").path("kycTier").asText());
					jteOutput.writeContent(")</span></dd>\n            <dt>Account status</dt>\n            ");
					if (view.path("account").path("status").asText().equals("active")) {
						jteOutput.writeContent("\n                <dd><span class=\"badge badge-success\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(view.path("account").path("status").asText());
						jteOutput.writeContent("</span></dd>\n            ");
					} else {
						jteOutput.writeContent("\n                <dd><span class=\"badge badge-warning\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(view.path("account").path("status").asText());
						jteOutput.writeContent("</span></dd>\n            ");
					}
					jteOutput.writeContent("\n        </dl>\n        <a class=\"btn btn-primary mt-2\" href=\"/customer/dashboard\">Go to your dashboard</a>\n    </div>\n");
				}
				jteOutput.writeContent("\n\n");
				if (view.path("status").asText().equals("in_progress") || view.path("status").asText().equals("documents_requested")) {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <h3>Journey progress</h3>\n        <ol class=\"stepper\">\n            ");
					for (var step : view.path("steps")) {
						jteOutput.writeContent("\n                ");
						if (step.path("completed").asBoolean()) {
							jteOutput.writeContent("\n                    <li><span class=\"step done\">✓ ");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(step.path("key").asText().replace("_", " "));
							jteOutput.writeContent("</span></li>\n                ");
						} else {
							jteOutput.writeContent("\n                    <li><span class=\"step\">");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(step.path("key").asText().replace("_", " "));
							jteOutput.writeContent("</span></li>\n                ");
						}
						jteOutput.writeContent("\n            ");
					}
					jteOutput.writeContent("\n        </ol>\n        ");
					if (view.path("status").asText().equals("documents_requested")) {
						jteOutput.writeContent("\n            <div class=\"notice-strip warn\">⚠ Documents were requested. Re-upload them and they will be re-verified; identity verification must be run again before re-submission.</div>\n            <a class=\"btn btn-primary\" href=\"/journey/documents\">Upload requested documents</a>\n        ");
					} else {
						jteOutput.writeContent("\n            <a class=\"btn btn-primary\" href=\"/journey\">Continue your application</a>\n        ");
					}
					jteOutput.writeContent("\n    </div>\n");
				}
				jteOutput.writeContent("\n\n");
				if (view.path("status").asText().equals("in_progress")) {
					jteOutput.writeContent("\n    ");
					if (!blockingReasons(view).isEmpty()) {
						jteOutput.writeContent("\n        <div class=\"card\">\n            <h3>Before you can submit</h3>\n            <ul class=\"small\" style=\"padding-left:18px;color:var(--muted);line-height:1.9;\">\n                ");
						for (var reason : blockingReasons(view)) {
							jteOutput.writeContent("\n                    <li>");
							jteOutput.setContext("li", null);
							jteOutput.writeUserContent(reason);
							jteOutput.writeContent("</li>\n                ");
						}
						jteOutput.writeContent("\n            </ul>\n            <form method=\"post\" action=\"/journey/submit\">\n                ");
						gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
						jteOutput.writeContent("\n                <button class=\"btn btn-accent\" type=\"submit\">Submit application</button>\n            </form>\n        </div>\n    ");
					} else {
						jteOutput.writeContent("\n        <div class=\"card\">\n            <h3>Ready to submit</h3>\n            <p class=\"muted\">All steps are complete and your documents are verified.</p>\n            <form method=\"post\" action=\"/journey/submit\">\n                ");
						gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
						jteOutput.writeContent("\n                <button class=\"btn btn-accent btn-lg\" type=\"submit\">Submit application</button>\n            </form>\n        </div>\n    ");
					}
					jteOutput.writeContent("\n");
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
		JsonNode view = (JsonNode)params.get("view");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, view, flash);
	}
}
