package gg.jte.generated.precompiled.journey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtecontactGenerated {
	public static final String JTE_NAME = "journey/contact.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,10,10,10,13,13,17,17,17,20,20,24,24,40,40,42,42,44,44,48,48,48,49,49,49,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode view, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Contact verification", "journey", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Verify your mobile number</h1>\n<p class=\"muted\">Both your email and mobile must be verified before you can upload documents.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Mobile number</h3>\n        <p class=\"mono\">");
				jteOutput.setContext("p", null);
				jteOutput.writeUserContent(view.path("applicant").path("mobile").asText());
				jteOutput.writeContent("</p>\n        <div class=\"notice-strip info\">SMS delivery is mocked locally: after you press “Send code”, the code appears in your <a href=\"/notifications\">notification bell</a> and demo inbox.</div>\n        <form method=\"post\" action=\"/journey/contact/issue\" class=\"mb-2\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <button class=\"btn btn-accent\" type=\"submit\">Send code</button>\n        </form>\n        <form method=\"post\" action=\"/journey/contact/verify\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"field\">\n                <label for=\"code\">One-time code</label>\n                <div class=\"otp-boxes\">\n                    <input class=\"input\" type=\"text\" id=\"code\" name=\"code\" inputmode=\"numeric\" maxlength=\"6\" required placeholder=\"······\" data-otp>\n                </div>\n                <div class=\"help\">Single use, expires in 10 minutes.</div>\n            </div>\n            <button class=\"btn btn-primary\" type=\"submit\">Verify mobile</button>\n        </form>\n    </div>\n    <div class=\"card\">\n        <h3>What has been verified</h3>\n        <dl class=\"kv\">\n            <dt>Email</dt><dd>✓ verified</dd>\n            <dt>Mobile</dt>\n            ");
				if (view.path("applicant").path("mobile") != null && !view.path("applicant").path("mobile").asText().isEmpty()) {
					jteOutput.writeContent("\n                <dd><span class=\"badge badge-success\"><span class=\"dot\"></span>verified</span></dd>\n            ");
				} else {
					jteOutput.writeContent("\n                <dd><span class=\"badge badge-neutral\"><span class=\"dot\"></span>pending</span></dd>\n            ");
				}
				jteOutput.writeContent("\n        </dl>\n    </div>\n</div>\n");
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
