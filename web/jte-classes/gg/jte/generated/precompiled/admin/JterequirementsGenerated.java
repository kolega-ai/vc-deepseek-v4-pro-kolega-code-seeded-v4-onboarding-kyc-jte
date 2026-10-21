package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JterequirementsGenerated {
	public static final String JTE_NAME = "admin/requirements.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,18,18,53,53,55,55,55,56,56,56,58,58,60,60,61,61,61,62,62,65,65,65,65,66,66,71,71,75,75,75,76,76,76,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode requirements, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Document requirements", "admin-reqs", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Document requirements</h1>\n<p class=\"muted\">Required documents per KYC tier. Conditions add a proof-of-income requirement for self-employed applicants or income of S$100,000+.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <h3>Add a requirement</h3>\n    <form method=\"post\" action=\"/admin/requirements\">\n        ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n        <div class=\"form-row\">\n            <div class=\"field\">\n                <label>KYC tier</label>\n                <select class=\"input\" name=\"kycTier\" required>\n                    <option value=\"tier_1\">tier_1</option>\n                    <option value=\"tier_2\">tier_2</option>\n                    <option value=\"tier_3\">tier_3</option>\n                </select>\n            </div>\n            <div class=\"field\">\n                <label>Document type</label>\n                <select class=\"input\" name=\"docType\" required>\n                    <option value=\"IDENTITY_DOC\">Identity document (NRIC or passport)</option>\n                    <option value=\"ADDRESS_PROOF\">Address proof</option>\n                    <option value=\"PROOF_OF_INCOME\">Proof of income</option>\n                </select>\n            </div>\n            <div class=\"field\">\n                <label>Condition (optional)</label>\n                <select class=\"input\" name=\"conditionType\">\n                    <option value=\"\">Unconditional</option>\n                    <option value=\"SELF_EMPLOYED\">Self-employed only</option>\n                    <option value=\"INCOME_GE_100K\">Income ≥ S$100,000</option>\n                </select>\n            </div>\n        </div>\n        <button class=\"btn btn-primary\" type=\"submit\">Add requirement</button>\n    </form>\n</div>\n\n<div class=\"table-wrap\">\n    <table class=\"table\">\n        <thead><tr><th>KYC tier</th><th>Document type</th><th>Condition</th><th></th></tr></thead>\n        <tbody>\n            ");
				for (var req : requirements.path("items")) {
					jteOutput.writeContent("\n                <tr>\n                    <td class=\"cell-strong mono\">");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(req.path("kycTier").asText());
					jteOutput.writeContent("</td>\n                    <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(docTypeLabel(req.path("docType").asText()));
					jteOutput.writeContent("</td>\n                    <td>\n                        ");
					if (req.path("conditionType").asText().isEmpty()) {
						jteOutput.writeContent("\n                            <span class=\"badge badge-neutral\"><span class=\"dot\"></span>unconditional</span>\n                        ");
					} else {
						jteOutput.writeContent("\n                            <span class=\"badge badge-accent\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(req.path("conditionType").asText());
						jteOutput.writeContent("</span>\n                        ");
					}
					jteOutput.writeContent("\n                    </td>\n                    <td class=\"actions\">\n                        <form method=\"post\" action=\"/admin/requirements/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(req.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("/delete\" data-confirm=\"Delete this requirement?\">\n                            ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                            <button class=\"btn btn-ghost-danger btn-sm\" type=\"submit\">Delete</button>\n                        </form>\n                    </td>\n                </tr>\n            ");
				}
				jteOutput.writeContent("\n        </tbody>\n    </table>\n</div>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode requirements = (JsonNode)params.get("requirements");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, requirements, flash);
	}
}
