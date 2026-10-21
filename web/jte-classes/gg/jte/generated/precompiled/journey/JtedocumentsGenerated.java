package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtedocumentsGenerated {
	public static final String JTE_NAME = "journey/documents.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,19,19,45,45,47,47,52,52,54,54,56,56,59,59,59,60,60,60,60,60,60,60,63,63,63,64,64,64,67,67,67,67,68,68,69,69,69,69,70,70,73,73,76,76,78,78,79,79,79,80,80,80,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Documents", "documents", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Documents</h1>\n<p class=\"muted\">PDF, PNG or JPEG up to 10 MB. Uploaded documents are verified by a bank officer in your branch.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Upload a document</h3>\n        <form method=\"post\" action=\"/journey/documents\" enctype=\"multipart/form-data\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"field\">\n                <label for=\"docType\">Document type</label>\n                <select class=\"input\" id=\"docType\" name=\"docType\">\n                    <option value=\"NATIONAL_ID\">National ID (NRIC)</option>\n                    <option value=\"PASSPORT\">Passport</option>\n                    <option value=\"ADDRESS_PROOF\">Address proof (utility bill / bank statement)</option>\n                    <option value=\"PROOF_OF_INCOME\">Proof of income (payslip / tax notice)</option>\n                </select>\n            </div>\n            <div class=\"dropzone\">\n                <span class=\"big\">⇪</span>\n                <input type=\"file\" name=\"file\" accept=\".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg\" required data-label=\"fileLabel\">\n                <div id=\"fileLabel\">Choose a file…</div>\n            </div>\n            <button class=\"btn btn-primary mt-2\" type=\"submit\">Upload document</button>\n        </form>\n    </div>\n    <div class=\"card\">\n        <h3>What is required</h3>\n        <ul class=\"small\" style=\"padding-left:18px;color:var(--muted);line-height:2;\">\n            <li>A verified identity document (NRIC or passport) for <b>tier 1</b>.</li>\n            <li>Plus a verified address proof for <b>tier 2</b>.</li>\n            <li>Plus a verified proof of income for <b>tier 3</b>.</li>\n            <li>Self-employed applicants, or income of S$100,000+, need proof of income for <b>any</b> tier.</li>\n        </ul>\n        ");
				if (view.path("requiredDocumentsBlocked").asBoolean()) {
					jteOutput.writeContent("\n            <div class=\"notice-strip warn\">⚠ Based on your employment and income, a proof of income is required before you can submit.</div>\n        ");
				}
				jteOutput.writeContent("\n    </div>\n</div>\n\n<h2 class=\"mt-3\">Uploaded documents</h2>\n");
				if (view.path("documents").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">▤</span><h3>No documents yet</h3>Upload your first document to continue.</div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"doc-grid\">\n        ");
					for (var doc : view.path("documents")) {
						jteOutput.writeContent("\n            <div class=\"doc-card\">\n                <div class=\"flex-between\">\n                    <span class=\"doc-name\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(docTypeLabel(doc.path("type").asText()));
						jteOutput.writeContent("</span>\n                    <span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(docStateClass(doc.path("state").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(doc.path("state").asText());
						jteOutput.writeContent("</span>\n                </div>\n                <div class=\"doc-meta\">\n                    <span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(doc.path("filename").asText());
						jteOutput.writeContent("</span>\n                    <span>expires ");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(fmtDate(doc.path("expiryDate").asText()));
						jteOutput.writeContent("</span>\n                </div>\n                <div class=\"doc-actions\">\n                    <a class=\"btn btn-ghost btn-sm\" href=\"/documents/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(doc.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("/download\">Download</a>\n                    ");
						if (view.path("status").asText().equals("in_progress") && !doc.path("state").asText().equals("verified")) {
							jteOutput.writeContent("\n                        <form method=\"post\" action=\"/journey/documents/");
							jteOutput.setContext("form", "action");
							jteOutput.writeUserContent(doc.path("id").asText());
							jteOutput.setContext("form", null);
							jteOutput.writeContent("/delete\" data-confirm=\"Remove this document? You can upload a replacement.\">\n                            ");
							gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
							jteOutput.writeContent("\n                            <button class=\"btn btn-ghost-danger btn-sm\" type=\"submit\">Delete</button>\n                        </form>\n                    ");
						}
						jteOutput.writeContent("\n                </div>\n            </div>\n        ");
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
		JsonNode view = (JsonNode)params.get("view");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, view, flash);
	}
}
