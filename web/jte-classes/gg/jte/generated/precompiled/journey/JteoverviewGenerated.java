package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.List;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
import static com.meridianbank.web.controller.JourneyController.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteoverviewGenerated {
	public static final String JTE_NAME = "journey/overview.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,5,6,6,6,6,6,12,12,12,13,13,13,13,17,17,17,17,17,17,19,19,19,19,19,19,19,23,23,24,24,25,25,25,26,26,27,27,27,28,28,29,29,29,30,30,31,31,34,34,38,38,41,41,44,44,47,47,50,50,53,53,56,56,59,59,63,63,65,65,67,67,68,68,68,69,69,71,71,74,74,77,77,80,80,82,82,82,83,83,84,84,84,85,85,88,88,89,89,89,90,90,90,6,7,8,9,10,11,11,11,11};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, int journeyCssVersion) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Your application", "journey", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<link rel=\"stylesheet\" href=\"/css/app.css?v=");
				jteOutput.setContext("link", "href");
				jteOutput.writeUserContent(journeyCssVersion);
				jteOutput.setContext("link", null);
				jteOutput.writeContent("\">\n<div class=\"flex-between mb-2\">\n    <div>\n        <h1 class=\"mb-0\">Your application</h1>\n        <span class=\"muted small\">Reference <span class=\"mono\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("reference").asText());
				jteOutput.writeContent("</span> · Branch: ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("branch").asText());
				jteOutput.writeContent("</span>\n    </div>\n    <span class=\"badge ");
				jteOutput.setContext("span", "class");
				jteOutput.writeUserContent(statusClass(view.path("status").asText()));
				jteOutput.setContext("span", null);
				jteOutput.writeContent("\"><span class=\"dot\"></span>");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(statusLabel(view.path("status").asText()));
				jteOutput.writeContent("</span>\n</div>\n\n<ol class=\"stepper\">\n    ");
				for (var step : view.path("steps")) {
					jteOutput.writeContent("\n        ");
					if (step.path("completed").asBoolean()) {
						jteOutput.writeContent("\n            <li><span class=\"step done\">✓ ");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(step.path("key").asText().replace("_", " "));
						jteOutput.writeContent("</span></li>\n        ");
					} else if (step.path("key").asText().equals(view.path("currentStep").asText())) {
						jteOutput.writeContent("\n            <li><span class=\"step current\">● ");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(step.path("key").asText().replace("_", " "));
						jteOutput.writeContent("</span></li>\n        ");
					} else {
						jteOutput.writeContent("\n            <li><span class=\"step locked\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(step.path("key").asText().replace("_", " "));
						jteOutput.writeContent("</span></li>\n        ");
					}
					jteOutput.writeContent("\n    ");
				}
				jteOutput.writeContent("\n</ol>\n\n");
				if (view.path("status").asText().equals("in_progress")) {
					jteOutput.writeContent("\n    <div class=\"grid main-2\">\n        <div class=\"card\">\n            <h2>Next step</h2>\n            ");
					if (view.path("currentStep").asText().equals("contact_verified")) {
						jteOutput.writeContent("\n                <p class=\"muted\">Verify your mobile number before you continue. Both email and mobile verification are required before document upload.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/contact\">Verify mobile number</a>\n            ");
					} else if (view.path("currentStep").asText().equals("personal_details")) {
						jteOutput.writeContent("\n                <p class=\"muted\">Tell us who you are.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/personal\">Personal details</a>\n            ");
					} else if (view.path("currentStep").asText().equals("address_details")) {
						jteOutput.writeContent("\n                <p class=\"muted\">Where you live.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/address\">Address details</a>\n            ");
					} else if (view.path("currentStep").asText().equals("employment_income")) {
						jteOutput.writeContent("\n                <p class=\"muted\">Your employment and declared annual income.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/employment\">Employment &amp; income</a>\n            ");
					} else if (view.path("currentStep").asText().equals("documents_uploaded")) {
						jteOutput.writeContent("\n                <p class=\"muted\">Upload the required documents, then run identity verification.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/documents\">Documents</a>\n            ");
					} else if (view.path("currentStep").asText().equals("identity_verified")) {
						jteOutput.writeContent("\n                <p class=\"muted\">Run identity verification against our identity provider.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/identity\">Identity verification</a>\n            ");
					} else {
						jteOutput.writeContent("\n                <p class=\"muted\">Continue your journey.</p>\n                <a class=\"btn btn-primary\" href=\"/journey/status\">View status</a>\n            ");
					}
					jteOutput.writeContent("\n        </div>\n        <div class=\"card\">\n            <h2>Submission checklist</h2>\n            ");
					if (blockingReasons(view).isEmpty()) {
						jteOutput.writeContent("\n                <div class=\"alert alert-success\"><span class=\"icon\">✓</span><span>Everything is in place. When your documents are verified by an officer, you can submit.</span></div>\n            ");
					} else {
						jteOutput.writeContent("\n                <ul class=\"small\" style=\"padding-left:18px;color:var(--muted);line-height:1.9;\">\n                    ");
						for (var reason : blockingReasons(view)) {
							jteOutput.writeContent("\n                        <li>");
							jteOutput.setContext("li", null);
							jteOutput.writeUserContent(reason);
							jteOutput.writeContent("</li>\n                    ");
						}
						jteOutput.writeContent("\n                </ul>\n            ");
					}
					jteOutput.writeContent("\n        </div>\n    </div>\n");
				} else if (view.path("status").asText().equals("submitted")) {
					jteOutput.writeContent("\n    <div class=\"notice-strip info\">⏳ Your application was submitted and is queued for screening. This completes automatically within a minute — refresh this page to see the result.</div>\n    <a class=\"btn btn-ghost\" href=\"/journey/status\">Go to status</a>\n");
				} else if (view.path("status").asText().equals("documents_requested")) {
					jteOutput.writeContent("\n    <div class=\"notice-strip warn\">⚠ Additional documents were requested for your application. Please visit the documents step and upload fresh copies.</div>\n    <a class=\"btn btn-primary\" href=\"/journey/documents\">Upload requested documents</a>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <h2 class=\"mb-0\">Application status: ");
					jteOutput.setContext("h2", null);
					jteOutput.writeUserContent(statusLabel(view.path("status").asText()));
					jteOutput.writeContent("</h2>\n        ");
					if (view.path("customerVisibleReason") != null && !view.path("customerVisibleReason").isMissingNode()) {
						jteOutput.writeContent("\n            <p class=\"mt-1 muted\">");
						jteOutput.setContext("p", null);
						jteOutput.writeUserContent(view.path("customerVisibleReason").asText());
						jteOutput.writeContent("</p>\n        ");
					}
					jteOutput.writeContent("\n        <a class=\"btn btn-primary\" href=\"/journey/status\">View full status</a>\n    </div>\n");
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
		int journeyCssVersion = (int)params.get("journeyCssVersion");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, view, journeyCssVersion);
	}
}
