package gg.jte.generated.precompiled.customer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JterekycGenerated {
	public static final String JTE_NAME = "customer/rekyc.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,15,15,17,17,21,21,21,21,21,21,21,24,24,24,27,27,28,28,28,29,29,36,36,36,36,37,37,60,60,62,62,67,67,69,69,69,70,70,70,70,70,70,70,71,71,71,72,72,72,72,74,74,78,78,84,84,84,84,85,85,88,88,88,88,89,89,94,94,95,95,95,96,96,96,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Re-KYC", "customer-rekyc", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Re-KYC document refresh</h1>\n<p class=\"muted\">Periodic re-verification keeps your account tier and limits up to date. You can only refresh documents here — your identity details and account number never change in this flow.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n");
				if (view.path("rekycCase") == null || view.path("rekycCase").isMissingNode()) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">✓</span><h3>Nothing due</h3>You have no open re-KYC case. The bank will notify you when a refresh is required.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"grid main-2\">\n        <div class=\"card\">\n            <div class=\"card-head\"><h2 class=\"mb-0\">Your case</h2>\n                <span class=\"badge ");
					jteOutput.setContext("span", "class");
					jteOutput.writeUserContent(view.path("rekycCase").path("status").asText().equals("overdue") ? "badge-danger" : "badge-accent");
					jteOutput.setContext("span", null);
					jteOutput.writeContent("\"><span class=\"dot\"></span>");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("rekycCase").path("status").asText());
					jteOutput.writeContent("</span>\n            </div>\n            <dl class=\"kv\">\n                <dt>Due date</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(fmtDate(view.path("rekycCase").path("dueDate").asText()));
					jteOutput.writeContent("</dd>\n                <dt>Required documents</dt>\n                <dd>\n                    ");
					for (var type : view.path("rekycCase").path("requiredTypes")) {
						jteOutput.writeContent("\n                        <span class=\"badge badge-neutral mr-1\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(docTypeLabel(type.asText()));
						jteOutput.writeContent("</span>\n                    ");
					}
					jteOutput.writeContent("\n                </dd>\n            </dl>\n            <div class=\"notice-strip info\">Fresh documents are verified by a bank officer before you can submit.</div>\n        </div>\n        <div class=\"card\">\n            <h3>Upload a fresh document</h3>\n            <form method=\"post\" action=\"/customer/rekyc/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("rekycCase").path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/documents\" enctype=\"multipart/form-data\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <div class=\"field\">\n                    <label for=\"docType\">Document type</label>\n                    <select class=\"input\" id=\"docType\" name=\"docType\">\n                        <option value=\"NATIONAL_ID\">National ID (NRIC)</option>\n                        <option value=\"PASSPORT\">Passport</option>\n                        <option value=\"ADDRESS_PROOF\">Address proof</option>\n                        <option value=\"PROOF_OF_INCOME\">Proof of income</option>\n                    </select>\n                    <div class=\"help\">Only documents required for your tier are accepted.</div>\n                </div>\n                <div class=\"dropzone\">\n                    <span class=\"big\">⇪</span>\n                    <input type=\"file\" name=\"file\" accept=\".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg\" required data-label=\"fileLabel\">\n                    <div id=\"fileLabel\">Choose a file…</div>\n                </div>\n                <button class=\"btn btn-primary mt-2\" type=\"submit\">Upload document</button>\n            </form>\n        </div>\n    </div>\n\n    <div class=\"card\">\n        <div class=\"card-head\"><h2 class=\"mb-0\">Documents for this refresh</h2></div>\n        ");
					if (view.path("rekycCase").path("documents").size() == 0) {
						jteOutput.writeContent("\n            <div class=\"empty\"><span class=\"glyph\">▤</span><h3>No documents yet</h3>Upload a fresh document to begin.</div>\n        ");
					} else {
						jteOutput.writeContent("\n            <div class=\"table-wrap\">\n                <table class=\"table\">\n                    <thead><tr><th>Document</th><th>State</th><th>Expires</th><th></th></tr></thead>\n                    <tbody>\n                        ");
						for (var doc : view.path("rekycCase").path("documents")) {
							jteOutput.writeContent("\n                            <tr>\n                                <td class=\"cell-strong\">");
							jteOutput.setContext("td", null);
							jteOutput.writeUserContent(doc.path("filename").asText());
							jteOutput.writeContent("</td>\n                                <td><span class=\"badge ");
							jteOutput.setContext("span", "class");
							jteOutput.writeUserContent(docStateClass(doc.path("state").asText()));
							jteOutput.setContext("span", null);
							jteOutput.writeContent("\"><span class=\"dot\"></span>");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(doc.path("state").asText());
							jteOutput.writeContent("</span></td>\n                                <td class=\"cell-muted\">");
							jteOutput.setContext("td", null);
							jteOutput.writeUserContent(fmtDate(doc.path("expiryDate").asText()));
							jteOutput.writeContent("</td>\n                                <td class=\"actions\"><a class=\"btn btn-ghost btn-sm\" href=\"/documents/");
							jteOutput.setContext("a", "href");
							jteOutput.writeUserContent(doc.path("id").asText());
							jteOutput.setContext("a", null);
							jteOutput.writeContent("/download\">Download</a></td>\n                            </tr>\n                        ");
						}
						jteOutput.writeContent("\n                    </tbody>\n                </table>\n            </div>\n        ");
					}
					jteOutput.writeContent("\n    </div>\n\n    <div class=\"card\">\n        <h3>Submit your refresh</h3>\n        <p class=\"muted small\">All required documents must be <b>verified by an officer</b> first. Submitting requires a step-up OTP.</p>\n        <form method=\"post\" action=\"/customer/rekyc/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("rekycCase").path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/otp\">\n            ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n            <button class=\"btn btn-ghost\" type=\"submit\">1 · Send step-up code</button>\n        </form>\n        <form method=\"post\" action=\"/customer/rekyc/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("rekycCase").path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/submit\" class=\"mt-1 flex flex-wrap\">\n            ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n            <input class=\"input\" type=\"text\" name=\"otpCode\" placeholder=\"2 · Enter 6-digit code\" inputmode=\"numeric\" maxlength=\"6\" required data-otp style=\"max-width:220px;\">\n            <button class=\"btn btn-accent\" type=\"submit\">3 · Submit re-KYC</button>\n        </form>\n    </div>\n");
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
