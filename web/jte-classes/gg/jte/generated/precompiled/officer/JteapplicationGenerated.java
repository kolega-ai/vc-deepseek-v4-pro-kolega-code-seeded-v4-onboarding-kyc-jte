package gg.jte.generated.precompiled.officer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteapplicationGenerated {
	public static final String JTE_NAME = "officer/application.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,13,14,14,14,14,14,14,14,14,14,17,17,17,17,17,17,17,18,18,18,18,18,18,18,21,21,23,23,25,25,31,31,31,32,32,32,33,33,33,34,34,34,35,35,35,35,35,35,35,35,35,36,36,36,36,36,36,37,37,37,44,44,45,45,45,45,45,45,45,45,45,45,46,46,48,48,50,50,51,51,51,51,51,51,52,52,54,54,56,56,56,58,58,64,64,65,65,65,65,65,65,65,65,65,66,66,70,70,74,74,81,81,83,83,83,84,84,84,85,85,85,86,86,86,86,86,86,86,87,87,87,89,89,94,94,98,98,100,100,102,102,105,105,105,106,106,106,106,106,106,106,109,109,109,110,110,110,111,111,111,113,113,114,114,114,115,115,117,117,117,117,118,118,119,119,119,119,120,120,121,121,121,121,124,124,124,124,125,125,126,126,126,126,129,129,132,132,134,134,137,137,141,141,141,141,142,142,160,160,160,160,161,161,164,164,164,164,165,165,177,177,177,177,178,178,181,181,181,181,182,182,199,199,201,201,204,204,204,206,206,207,207,207,208,208,208,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Application review", "officer-apps", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<div class=\"flex-between mb-2\">\n    <div>\n        <h1 class=\"mb-0\">");
				jteOutput.setContext("h1", null);
				jteOutput.writeUserContent(view.path("reference").asText());
				jteOutput.writeContent("</h1>\n        <span class=\"muted small\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("applicant").path("fullName").asText());
				jteOutput.writeContent(" · ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("applicant").path("email").asText());
				jteOutput.writeContent(" · ");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("branch").asText());
				jteOutput.writeContent("</span>\n    </div>\n    <div class=\"flex\">\n        <span class=\"badge ");
				jteOutput.setContext("span", "class");
				jteOutput.writeUserContent(statusClass(view.path("status").asText()));
				jteOutput.setContext("span", null);
				jteOutput.writeContent("\"><span class=\"dot\"></span>");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(statusLabel(view.path("status").asText()));
				jteOutput.writeContent("</span>\n        <span class=\"badge ");
				jteOutput.setContext("span", "class");
				jteOutput.writeUserContent(tierClass(view.path("effectiveKycTier").asText()));
				jteOutput.setContext("span", null);
				jteOutput.writeContent("\"><span class=\"dot\"></span>");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(view.path("effectiveKycTier").asText());
				jteOutput.writeContent("</span>\n    </div>\n</div>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n");
				if (view.path("status").asText().equals("watchlist_hold")) {
					jteOutput.writeContent("\n    <div class=\"notice-strip danger\">⚠ This application is on watchlist hold. Compliance must disposition the hits before any decision.</div>\n");
				}
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h2>Applicant</h2>\n        <dl class=\"kv\">\n            <dt>Name</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("fullName").asText());
				jteOutput.writeContent("</dd>\n            <dt>Date of birth</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("dateOfBirth").asText());
				jteOutput.writeContent("</dd>\n            <dt>National identifier</dt><dd class=\"mono\">");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("nationalIdentifier").asText());
				jteOutput.writeContent("</dd>\n            <dt>Mobile</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("mobile").asText());
				jteOutput.writeContent("</dd>\n            <dt>Address</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("addressLine1").asText());
				jteOutput.writeContent(", ");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("city").asText());
				jteOutput.writeContent(" ");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("postalCode").asText());
				jteOutput.writeContent("</dd>\n            <dt>Employment</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("employmentStatus").asText());
				jteOutput.writeContent(" · ");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("applicant").path("employerName").asText());
				jteOutput.writeContent("</dd>\n            <dt>Declared annual income</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(money(view.path("applicant").path("declaredAnnualIncome").asText()));
				jteOutput.writeContent("</dd>\n        </dl>\n    </div>\n    <div class=\"card\">\n        <h2>Checks</h2>\n        <dl class=\"kv\">\n            <dt>Identity match</dt>\n            ");
				if (view.path("identityResult") != null && !view.path("identityResult").isMissingNode()) {
					jteOutput.writeContent("\n                <dd><span class=\"badge ");
					jteOutput.setContext("span", "class");
					jteOutput.writeUserContent(view.path("identityResult").path("matchScore").asInt() >= 80 ? "badge-success" : "badge-danger");
					jteOutput.setContext("span", null);
					jteOutput.writeContent("\"><span class=\"dot\"></span>score ");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("identityResult").path("matchScore").asText());
					jteOutput.writeContent(" · ");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("identityResult").path("outcome").asText());
					jteOutput.writeContent("</span></dd>\n            ");
				} else {
					jteOutput.writeContent("\n                <dd><span class=\"badge badge-neutral\"><span class=\"dot\"></span>not run</span></dd>\n            ");
				}
				jteOutput.writeContent("\n            <dt>Bureau score</dt>\n            ");
				if (view.path("bureauResult") != null && !view.path("bureauResult").isMissingNode()) {
					jteOutput.writeContent("\n                <dd><span class=\"badge badge-info\"><span class=\"dot\"></span>");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("bureauResult").path("score").asText());
					jteOutput.writeContent("</span> <span class=\"small faint\">pulled ");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(fmtDateTime(view.path("bureauResult").path("pulledAt").asText()));
					jteOutput.writeContent("</span></dd>\n            ");
				} else {
					jteOutput.writeContent("\n                <dd><span class=\"badge badge-neutral\"><span class=\"dot\"></span>not available</span></dd>\n            ");
				}
				jteOutput.writeContent("\n            <dt>Watchlist hits</dt>\n            <dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("watchlistHits").size());
				jteOutput.writeContent(" hit(s)</dd>\n        </dl>\n        ");
				if (view.path("bureauResult") != null && !view.path("bureauResult").isMissingNode() && view.path("bureauResult").path("liabilities") != null) {
					jteOutput.writeContent("\n            <div class=\"section-title\">Reported liabilities</div>\n            <div class=\"table-wrap\">\n                <table class=\"table\">\n                    <thead><tr><th>Type</th><th>Amount</th></tr></thead>\n                    <tbody>\n                        ");
					for (var li : view.path("bureauResult").path("liabilities")) {
						jteOutput.writeContent("\n                            <tr><td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(li.path("type").asText());
						jteOutput.writeContent("</td><td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(money(li.path("amount").asText()));
						jteOutput.writeContent(" ");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(li.path("currency").asText());
						jteOutput.writeContent("</td></tr>\n                        ");
					}
					jteOutput.writeContent("\n                    </tbody>\n                </table>\n            </div>\n        ");
				}
				jteOutput.writeContent("\n    </div>\n</div>\n\n");
				if (view.path("watchlistHits").size() > 0) {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <h2>Watchlist hits</h2>\n        <div class=\"table-wrap\">\n            <table class=\"table\">\n                <thead><tr><th>Matched name</th><th>List source</th><th>Confidence</th><th>Disposition</th><th>Justification</th></tr></thead>\n                <tbody>\n                    ");
					for (var hit : view.path("watchlistHits")) {
						jteOutput.writeContent("\n                        <tr>\n                            <td class=\"cell-strong\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("matchedName").asText());
						jteOutput.writeContent("</td>\n                            <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("listSource").asText());
						jteOutput.writeContent("</td>\n                            <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("confidence").asText());
						jteOutput.writeContent("</td>\n                            <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(dispositionClass(hit.path("disposition").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(dispositionLabel(hit.path("disposition").asText()));
						jteOutput.writeContent("</span></td>\n                            <td class=\"cell-muted\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(hit.path("justification").asText());
						jteOutput.writeContent("</td>\n                        </tr>\n                    ");
					}
					jteOutput.writeContent("\n                </tbody>\n            </table>\n        </div>\n    </div>\n");
				}
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <h2>Documents</h2>\n    ");
				if (view.path("documents").size() == 0) {
					jteOutput.writeContent("\n        <div class=\"empty\"><span class=\"glyph\">▤</span><h3>No documents</h3></div>\n    ");
				} else {
					jteOutput.writeContent("\n        <div class=\"doc-grid\">\n            ");
					for (var doc : view.path("documents")) {
						jteOutput.writeContent("\n                <div class=\"doc-card\">\n                    <div class=\"flex-between\">\n                        <span class=\"doc-name\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(docTypeLabel(doc.path("type").asText()));
						jteOutput.writeContent("</span>\n                        <span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(docStateClass(doc.path("state").asText()));
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(doc.path("state").asText());
						jteOutput.writeContent("</span>\n                    </div>\n                    <div class=\"doc-meta\">\n                        <span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(doc.path("filename").asText());
						jteOutput.writeContent("</span>\n                        <span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(doc.path("sizeBytes").asText());
						jteOutput.writeContent(" bytes</span>\n                        <span>expires ");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(fmtDate(doc.path("expiryDate").asText()));
						jteOutput.writeContent("</span>\n                    </div>\n                    ");
						if (doc.path("verifiedBy") != null && !doc.path("verifiedBy").asText().isEmpty()) {
							jteOutput.writeContent("\n                        <div class=\"doc-meta\"><span>verified by ");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(doc.path("verifiedBy").asText());
							jteOutput.writeContent("</span></div>\n                    ");
						}
						jteOutput.writeContent("\n                    <div class=\"doc-actions\">\n                        <a class=\"btn btn-ghost btn-sm\" href=\"/documents/");
						jteOutput.setContext("a", "href");
						jteOutput.writeUserContent(doc.path("id").asText());
						jteOutput.setContext("a", null);
						jteOutput.writeContent("/download\">Download</a>\n                        ");
						if (doc.path("state").asText().equals("uploaded")) {
							jteOutput.writeContent("\n                            <form method=\"post\" action=\"/officer/documents/");
							jteOutput.setContext("form", "action");
							jteOutput.writeUserContent(doc.path("id").asText());
							jteOutput.setContext("form", null);
							jteOutput.writeContent("/verify\">\n                                ");
							gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
							jteOutput.writeContent("\n                                <input type=\"hidden\" name=\"returnTo\" value=\"/officer/applications/");
							jteOutput.setContext("input", "value");
							jteOutput.writeUserContent(view.path("id").asText());
							jteOutput.setContext("input", null);
							jteOutput.writeContent("\">\n                                <button class=\"btn btn-success btn-sm\" type=\"submit\">Verify</button>\n                            </form>\n                            <form method=\"post\" action=\"/officer/documents/");
							jteOutput.setContext("form", "action");
							jteOutput.writeUserContent(doc.path("id").asText());
							jteOutput.setContext("form", null);
							jteOutput.writeContent("/reject\">\n                                ");
							gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
							jteOutput.writeContent("\n                                <input type=\"hidden\" name=\"returnTo\" value=\"/officer/applications/");
							jteOutput.setContext("input", "value");
							jteOutput.writeUserContent(view.path("id").asText());
							jteOutput.setContext("input", null);
							jteOutput.writeContent("\">\n                                <button class=\"btn btn-ghost-danger btn-sm\" type=\"submit\">Reject</button>\n                            </form>\n                        ");
						}
						jteOutput.writeContent("\n                    </div>\n                </div>\n            ");
					}
					jteOutput.writeContent("\n        </div>\n    ");
				}
				jteOutput.writeContent("\n</div>\n\n");
				if (view.path("status").asText().equals("under_review")) {
					jteOutput.writeContent("\n    <div class=\"grid main-2\">\n        <div class=\"card\">\n            <h3>Request more documents</h3>\n            <form method=\"post\" action=\"/officer/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/request-documents\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <div class=\"field\">\n                    <label>Document types</label>\n                    <div class=\"checkbox-row\"><input type=\"checkbox\" name=\"types\" value=\"NATIONAL_ID\" id=\"t1\"><label for=\"t1\">National ID</label></div>\n                    <div class=\"checkbox-row\"><input type=\"checkbox\" name=\"types\" value=\"PASSPORT\" id=\"t2\"><label for=\"t2\">Passport</label></div>\n                    <div class=\"checkbox-row\"><input type=\"checkbox\" name=\"types\" value=\"ADDRESS_PROOF\" id=\"t3\"><label for=\"t3\">Address proof</label></div>\n                    <div class=\"checkbox-row\"><input type=\"checkbox\" name=\"types\" value=\"PROOF_OF_INCOME\" id=\"t4\"><label for=\"t4\">Proof of income</label></div>\n                </div>\n                <div class=\"field\">\n                    <label for=\"requestMessage\">Message to applicant</label>\n                    <textarea class=\"input\" id=\"requestMessage\" name=\"message\" required minlength=\"5\"></textarea>\n                </div>\n                <button class=\"btn btn-ghost\" type=\"submit\">Request documents</button>\n            </form>\n        </div>\n        <div class=\"card\">\n            <h3>Decision</h3>\n            <p class=\"small muted\">Decisions need a step-up OTP. Approval is limited to your maximum approval tier.</p>\n            <form method=\"post\" action=\"/officer/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/approve/otp\" class=\"mb-1\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <button class=\"btn btn-ghost btn-sm\" type=\"submit\">1 · Send approve code</button>\n            </form>\n            <form method=\"post\" action=\"/officer/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/approve\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <div class=\"field\">\n                    <label>Step-up code</label>\n                    <input class=\"input\" type=\"text\" name=\"otpCode\" required inputmode=\"numeric\" maxlength=\"6\" placeholder=\"2 · 6-digit code\" data-otp>\n                </div>\n                <div class=\"field\">\n                    <label for=\"approveReason\">Customer-visible reason</label>\n                    <textarea class=\"input\" id=\"approveReason\" name=\"customerVisibleReason\" required minlength=\"5\">Identity and documents verified; account approved.</textarea>\n                </div>\n                <button class=\"btn btn-success\" type=\"submit\">3 · Approve &amp; open account</button>\n            </form>\n            <div class=\"divider\"></div>\n            <form method=\"post\" action=\"/officer/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/decline/otp\" class=\"mb-1\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <button class=\"btn btn-ghost btn-sm\" type=\"submit\">1 · Send decline code</button>\n            </form>\n            <form method=\"post\" action=\"/officer/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/decline\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <div class=\"field\">\n                    <label>Step-up code</label>\n                    <input class=\"input\" type=\"text\" name=\"otpCode\" required inputmode=\"numeric\" maxlength=\"6\" placeholder=\"2 · 6-digit code\" data-otp>\n                </div>\n                <div class=\"field\">\n                    <label for=\"declineReason\">Customer-visible reason</label>\n                    <textarea class=\"input\" id=\"declineReason\" name=\"customerVisibleReason\" required minlength=\"5\"></textarea>\n                </div>\n                <div class=\"field\">\n                    <label for=\"internalReason\">Internal reason (never shown to the applicant)</label>\n                    <textarea class=\"input\" id=\"internalReason\" name=\"internalReason\" required minlength=\"5\"></textarea>\n                </div>\n                <button class=\"btn btn-danger\" type=\"submit\">3 · Decline</button>\n            </form>\n        </div>\n    </div>\n");
				}
				jteOutput.writeContent("\n\n");
				if (view.path("internalReason") != null && !view.path("internalReason").asText().isEmpty()) {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <h3>Internal notes</h3>\n        <p class=\"mb-0 small\">");
					jteOutput.setContext("p", null);
					jteOutput.writeUserContent(view.path("internalReason").asText());
					jteOutput.writeContent("</p>\n    </div>\n");
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
