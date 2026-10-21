package gg.jte.generated.precompiled.customer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtedashboardGenerated {
	public static final String JTE_NAME = "customer/dashboard.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,11,11,11,12,12,14,14,15,15,17,17,18,18,18,19,19,21,21,23,23,27,27,27,27,27,27,27,30,30,30,31,31,31,32,32,32,34,34,36,36,38,38,42,42,42,43,43,49,49,51,51,51,52,52,52,54,54,58,58,62,62,64,64,69,69,71,71,71,72,72,72,73,73,73,73,73,73,73,74,74,74,75,75,75,75,77,77,81,81,87,87,87,88,88,88,89,89,89,90,90,90,90,90,90,90,90,90,91,91,91,91,91,92,92,92,95,95,95,96,96,96,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Customer dashboard", "customer", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Welcome, ");
				jteOutput.setContext("h1", null);
				jteOutput.writeUserContent(view.path("profile").path("fullName").asText());
				jteOutput.writeContent("</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n");
				if (view.path("rekycCase") != null && !view.path("rekycCase").isMissingNode()) {
					jteOutput.writeContent("\n    ");
					if (view.path("rekycCase").path("status").asText().equals("overdue")) {
						jteOutput.writeContent("\n        <div class=\"notice-strip danger\">⚠ Your re-KYC is <b>overdue</b>. Your account may be limited until you complete the document refresh.</div>\n    ");
					} else {
						jteOutput.writeContent("\n        <div class=\"notice-strip warn\">↻ Re-KYC required: fresh documents due by ");
						jteOutput.setContext("div", null);
						jteOutput.writeUserContent(fmtDate(view.path("rekycCase").path("dueDate").asText()));
						jteOutput.writeContent(".</div>\n    ");
					}
					jteOutput.writeContent("\n    <div class=\"mb-2\"><a class=\"btn btn-primary\" href=\"/customer/rekyc\">Start / continue re-KYC</a></div>\n");
				}
				jteOutput.writeContent("\n\n");
				if (view.path("account") != null && !view.path("account").isMissingNode()) {
					jteOutput.writeContent("\n    <div class=\"grid cols-2\">\n        <div class=\"card\">\n            <div class=\"card-head\"><h2 class=\"mb-0\">Your account</h2>\n                <span class=\"badge ");
					jteOutput.setContext("span", "class");
					jteOutput.writeUserContent(tierClass(view.path("account").path("kycTier").asText()));
					jteOutput.setContext("span", null);
					jteOutput.writeContent("\"><span class=\"dot\"></span>");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(tierLabel(view.path("account").path("kycTier").asText()));
					jteOutput.writeContent("</span>\n            </div>\n            <dl class=\"kv\">\n                <dt>Account number</dt><dd class=\"mono\">");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("account").path("accountNumber").asText());
					jteOutput.writeContent("</dd>\n                <dt>Product</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("account").path("product").asText());
					jteOutput.writeContent("</dd>\n                <dt>Opened</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(fmtDate(view.path("account").path("openedAt").asText()));
					jteOutput.writeContent("</dd>\n                <dt>Status</dt>\n                ");
					if (view.path("account").path("suspended").asBoolean()) {
						jteOutput.writeContent("\n                    <dd><span class=\"badge badge-warning\"><span class=\"dot\"></span>suspended — pending re-KYC</span></dd>\n                ");
					} else {
						jteOutput.writeContent("\n                    <dd><span class=\"badge badge-success\"><span class=\"dot\"></span>active</span></dd>\n                ");
					}
					jteOutput.writeContent("\n            </dl>\n        </div>\n        <div class=\"card\">\n            <div class=\"card-head\"><h2 class=\"mb-0\">Your limits</h2><span class=\"small muted\">");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("account").path("limits").path("currency").asText());
					jteOutput.writeContent("</span></div>\n            ");
					if (view.path("account").path("suspended").asBoolean()) {
						jteOutput.writeContent("\n                <p class=\"muted\">Your tier limits are suspended until your re-KYC is completed.</p>\n                <div class=\"grid cols-2\">\n                    <div class=\"stat danger\"><span class=\"label\">Daily transaction limit</span><span class=\"value\">S$0.00</span></div>\n                    <div class=\"stat danger\"><span class=\"label\">Balance ceiling</span><span class=\"value\">S$0.00</span></div>\n                </div>\n            ");
					} else {
						jteOutput.writeContent("\n                <div class=\"grid cols-2\">\n                    <div class=\"stat primary\"><span class=\"label\">Daily transaction limit</span><span class=\"value\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(money(view.path("account").path("limits").path("dailyTransactionLimit").asText()));
						jteOutput.writeContent("</span></div>\n                    <div class=\"stat accent\"><span class=\"label\">Balance ceiling</span><span class=\"value\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(money(view.path("account").path("limits").path("balanceCeiling").asText()));
						jteOutput.writeContent("</span></div>\n                </div>\n            ");
					}
					jteOutput.writeContent("\n            <p class=\"small muted mt-2\">Limits follow your KYC tier policy and are refreshed automatically on re-KYC.</p>\n        </div>\n    </div>\n");
				}
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <div class=\"card-head\"><h2 class=\"mb-0\">Your documents</h2><a class=\"btn btn-ghost btn-sm\" href=\"/journey/status\">Application status</a></div>\n    ");
				if (view.path("documents").size() == 0) {
					jteOutput.writeContent("\n        <div class=\"empty\"><span class=\"glyph\">▤</span><h3>No documents</h3></div>\n    ");
				} else {
					jteOutput.writeContent("\n        <div class=\"table-wrap\">\n            <table class=\"table\">\n                <thead><tr><th>Document</th><th>Type</th><th>State</th><th>Expires</th><th></th></tr></thead>\n                <tbody>\n                    ");
					for (var doc : view.path("documents")) {
						jteOutput.writeContent("\n                        <tr>\n                            <td class=\"cell-strong\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(doc.path("filename").asText());
						jteOutput.writeContent("</td>\n                            <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(docTypeLabel(doc.path("type").asText()));
						jteOutput.writeContent("</td>\n                            <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(docStateClass(doc.path("state").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(doc.path("state").asText());
						jteOutput.writeContent("</span></td>\n                            <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(fmtDate(doc.path("expiryDate").asText()));
						jteOutput.writeContent("</td>\n                            <td class=\"actions\"><a class=\"btn btn-ghost btn-sm\" href=\"/documents/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(doc.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("/download\">Download</a></td>\n                        </tr>\n                    ");
					}
					jteOutput.writeContent("\n                </tbody>\n            </table>\n        </div>\n    ");
				}
				jteOutput.writeContent("\n</div>\n\n<div class=\"card\">\n    <div class=\"card-head\"><h2 class=\"mb-0\">Profile</h2><a class=\"btn btn-ghost btn-sm\" href=\"/profile\">Edit password</a></div>\n    <dl class=\"kv\">\n        <dt>Email</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("email").asText());
				jteOutput.writeContent("</dd>\n        <dt>Mobile</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("mobile").asText());
				jteOutput.writeContent("</dd>\n        <dt>National identifier</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("nationalIdentifier").asText());
				jteOutput.writeContent("</dd>\n        <dt>Address</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("addressLine1").asText());
				jteOutput.writeContent(", ");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("city").asText());
				jteOutput.writeContent(" ");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("postalCode").asText());
				jteOutput.writeContent("</dd>\n        <dt>Employment</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("employmentStatus").asText());
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("profile").path("employerName") != null && !view.path("profile").path("employerName").asText().isEmpty() ? " · " + view.path("profile").path("employerName").asText() : "");
				jteOutput.writeContent("</dd>\n        <dt>Declared annual income</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(money(view.path("profile").path("declaredAnnualIncome").asText()));
				jteOutput.writeContent("</dd>\n    </dl>\n</div>\n");
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
