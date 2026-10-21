package gg.jte.generated.precompiled.compliance;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtehitGenerated {
	public static final String JTE_NAME = "compliance/hit.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,13,13,13,13,13,15,15,21,21,21,22,22,22,23,23,23,24,24,24,25,25,26,26,26,27,27,32,32,33,33,33,33,34,34,49,49,50,50,50,51,51,55,55,58,58,58,59,59,59,59,59,59,59,62,62,62,62,62,62,63,63,63,64,64,64,64,64,64,64,67,67,73,73,75,75,75,76,76,76,77,77,77,78,78,78,78,78,78,78,80,80,84,84,86,86,89,89,89,89,90,90,103,103,107,107,107,107,108,108,109,109,109,109,112,112,112,112,113,113,114,114,114,114,137,137,138,138,138,139,139,139,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Watchlist hit", "compliance-hits", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<div class=\"flex-between mb-2\">\n    <h1 class=\"mb-0\">Watchlist hit</h1>\n    <span class=\"badge ");
				jteOutput.setContext("span", "class");
				jteOutput.writeUserContent(dispositionClass(view.path("disposition").asText()));
				jteOutput.setContext("span", null);
				jteOutput.writeContent("\"><span class=\"dot\"></span>");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(dispositionLabel(view.path("disposition").asText()));
				jteOutput.writeContent("</span>\n</div>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h2>Match details</h2>\n        <dl class=\"kv\">\n            <dt>Matched name</dt><dd class=\"cell-strong\">");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("matchedName").asText());
				jteOutput.writeContent("</dd>\n            <dt>List source</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("listSource").asText());
				jteOutput.writeContent("</dd>\n            <dt>Confidence</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(view.path("confidence").asText());
				jteOutput.writeContent("</dd>\n            <dt>Created</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(fmtDateTime(view.path("createdAt").asText()));
				jteOutput.writeContent("</dd>\n            ");
				if (view.path("justification") != null && !view.path("justification").asText().isEmpty()) {
					jteOutput.writeContent("\n                <dt>Justification</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("justification").asText());
					jteOutput.writeContent("</dd>\n            ");
				}
				jteOutput.writeContent("\n        </dl>\n    </div>\n    <div class=\"card\">\n        <h2>Disposition</h2>\n        ");
				if (view.path("disposition").asText().equals("pending") || view.path("disposition").asText().equals("escalated")) {
					jteOutput.writeContent("\n            <form method=\"post\" action=\"/compliance/hits/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/disposition\">\n                ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                <div class=\"field\">\n                    <label for=\"disposition\">Disposition</label>\n                    <select class=\"input\" id=\"disposition\" name=\"disposition\" required>\n                        <option value=\"false_positive\">False positive — namesake match</option>\n                        <option value=\"confirmed\">Confirmed — corroborated match</option>\n                        <option value=\"escalated\">Escalate for senior review</option>\n                    </select>\n                </div>\n                <div class=\"field\">\n                    <label for=\"justification\">Justification</label>\n                    <textarea class=\"input\" id=\"justification\" name=\"justification\" minlength=\"20\" placeholder=\"Required for a confirmed disposition (at least 20 characters)…\"></textarea>\n                </div>\n                <button class=\"btn btn-primary\" type=\"submit\">Record disposition</button>\n            </form>\n        ");
				} else {
					jteOutput.writeContent("\n            <p class=\"muted small\">This hit was already dispositioned ");
					jteOutput.setContext("p", null);
					jteOutput.writeUserContent(fmtDateTime(view.path("disposedAt").asText()));
					jteOutput.writeContent(".</p>\n        ");
				}
				jteOutput.writeContent("\n    </div>\n</div>\n\n");
				if (view.path("application") != null && !view.path("application").isMissingNode()) {
					jteOutput.writeContent("\n    <div class=\"card\">\n        <div class=\"card-head\">\n            <h2 class=\"mb-0\">Application ");
					jteOutput.setContext("h2", null);
					jteOutput.writeUserContent(view.path("application").path("reference").asText());
					jteOutput.writeContent("</h2>\n            <span class=\"badge ");
					jteOutput.setContext("span", "class");
					jteOutput.writeUserContent(statusClass(view.path("application").path("status").asText()));
					jteOutput.setContext("span", null);
					jteOutput.writeContent("\"><span class=\"dot\"></span>");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(statusLabel(view.path("application").path("status").asText()));
					jteOutput.writeContent("</span>\n        </div>\n        <dl class=\"kv\">\n            <dt>Applicant</dt><dd>");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("application").path("applicant").path("fullName").asText());
					jteOutput.writeContent(" · ");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("application").path("applicant").path("email").asText());
					jteOutput.writeContent("</dd>\n            <dt>National identifier</dt><dd class=\"mono\">");
					jteOutput.setContext("dd", null);
					jteOutput.writeUserContent(view.path("application").path("applicant").path("nationalIdentifier").asText());
					jteOutput.writeContent("</dd>\n            <dt>KYC tier</dt><dd><span class=\"badge ");
					jteOutput.setContext("span", "class");
					jteOutput.writeUserContent(tierClass(view.path("application").path("effectiveKycTier").asText()));
					jteOutput.setContext("span", null);
					jteOutput.writeContent("\"><span class=\"dot\"></span>");
					jteOutput.setContext("span", null);
					jteOutput.writeUserContent(view.path("application").path("effectiveKycTier").asText());
					jteOutput.writeContent("</span></dd>\n        </dl>\n\n        ");
					if (view.path("otherHits").size() > 0) {
						jteOutput.writeContent("\n            <div class=\"section-title\">Other hits on this application</div>\n            <div class=\"table-wrap\">\n                <table class=\"table\">\n                    <thead><tr><th>Matched name</th><th>Source</th><th>Confidence</th><th>Disposition</th></tr></thead>\n                    <tbody>\n                        ");
						for (var other : view.path("otherHits")) {
							jteOutput.writeContent("\n                            <tr>\n                                <td>");
							jteOutput.setContext("td", null);
							jteOutput.writeUserContent(other.path("matchedName").asText());
							jteOutput.writeContent("</td>\n                                <td>");
							jteOutput.setContext("td", null);
							jteOutput.writeUserContent(other.path("listSource").asText());
							jteOutput.writeContent("</td>\n                                <td>");
							jteOutput.setContext("td", null);
							jteOutput.writeUserContent(other.path("confidence").asText());
							jteOutput.writeContent("</td>\n                                <td><span class=\"badge ");
							jteOutput.setContext("span", "class");
							jteOutput.writeUserContent(dispositionClass(other.path("disposition").asText()));
							jteOutput.setContext("span", null);
							jteOutput.writeContent("\"><span class=\"dot\"></span>");
							jteOutput.setContext("span", null);
							jteOutput.writeUserContent(other.path("disposition").asText());
							jteOutput.writeContent("</span></td>\n                            </tr>\n                        ");
						}
						jteOutput.writeContent("\n                    </tbody>\n                </table>\n            </div>\n        ");
					}
					jteOutput.writeContent("\n\n        ");
					if (view.path("application").path("status").asText().equals("watchlist_hold")) {
						jteOutput.writeContent("\n            <div class=\"section-title\">Resolve the hold</div>\n            <p class=\"small muted\">Release when every hit is a false positive; the application then returns to officer review. A confirmed hit declines the application.</p>\n            <form method=\"post\" action=\"/compliance/applications/");
						jteOutput.setContext("form", "action");
						jteOutput.writeUserContent(view.path("application").path("id").asText());
						jteOutput.setContext("form", null);
						jteOutput.writeContent("/resolve-hold\">\n                ");
						gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
						jteOutput.writeContent("\n                <div class=\"form-row\">\n                    <div class=\"field\">\n                        <label>Customer-visible reason (if declining)</label>\n                        <textarea class=\"input\" name=\"customerVisibleReason\"></textarea>\n                    </div>\n                    <div class=\"field\">\n                        <label>Internal reason (if declining)</label>\n                        <textarea class=\"input\" name=\"internalReason\"></textarea>\n                    </div>\n                </div>\n                <button class=\"btn btn-primary\" type=\"submit\">Resolve hold</button>\n            </form>\n        ");
					}
					jteOutput.writeContent("\n\n        <div class=\"section-title\">KYC tier override</div>\n        <p class=\"small muted\">Overrides need a step-up OTP and a mandatory justification. Limits follow the tier policy.</p>\n        <form method=\"post\" action=\"/compliance/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("application").path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/kyc-tier/otp\" class=\"mb-1\">\n            ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n            <input type=\"hidden\" name=\"returnTo\" value=\"/compliance/hits/");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\">\n            <button class=\"btn btn-ghost btn-sm\" type=\"submit\">1 · Send override code</button>\n        </form>\n        <form method=\"post\" action=\"/compliance/applications/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(view.path("application").path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/kyc-tier\">\n            ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n            <input type=\"hidden\" name=\"returnTo\" value=\"/compliance/hits/");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(view.path("id").asText());
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\">\n            <div class=\"form-row\">\n                <div class=\"field\">\n                    <label>New tier</label>\n                    <select class=\"input\" name=\"tier\">\n                        <option value=\"tier_0\">tier_0</option>\n                        <option value=\"tier_1\">tier_1</option>\n                        <option value=\"tier_2\">tier_2</option>\n                        <option value=\"tier_3\">tier_3</option>\n                    </select>\n                </div>\n                <div class=\"field\">\n                    <label>Step-up code</label>\n                    <input class=\"input\" type=\"text\" name=\"otpCode\" required inputmode=\"numeric\" maxlength=\"6\" placeholder=\"2 · 6-digit code\" data-otp>\n                </div>\n            </div>\n            <div class=\"field\">\n                <label>Justification (at least 20 characters)</label>\n                <textarea class=\"input\" name=\"justification\" required minlength=\"20\"></textarea>\n            </div>\n            <button class=\"btn btn-accent\" type=\"submit\">3 · Override tier</button>\n        </form>\n    </div>\n");
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
