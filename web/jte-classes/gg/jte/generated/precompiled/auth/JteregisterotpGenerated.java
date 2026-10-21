package gg.jte.generated.precompiled.auth;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteregisterotpGenerated {
	public static final String JTE_NAME = "auth/register-otp.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,3,3,7,7,7,10,10,12,12,23,23,26,26,30,30,32,32,32,32,32,32,33,33,33,35,35,36,36,37,37,37,38,38,38,3,4,5,6,6,6,6};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, Object> flash, String csrfToken, String csrfParameterName, JsonNode emails) {
		gg.jte.generated.precompiled.layout.JteauthGenerated.render(jteOutput, jteHtmlInterceptor, "Verify your email", csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h2>Verify your email</h2>\n<p class=\"sub\">Step 2 of 3 — enter the 6-digit code we sent to your email address.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/register/email-otp/verify\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"code\">One-time code</label>\n        <div class=\"otp-boxes\">\n            <input class=\"input\" type=\"text\" id=\"code\" name=\"code\" inputmode=\"numeric\" maxlength=\"6\" required placeholder=\"······\" data-otp>\n        </div>\n        <div class=\"help\">The code expires after 10 minutes and can only be used once.</div>\n    </div>\n    <button class=\"btn btn-primary btn-lg\" type=\"submit\" style=\"width:100%;\">Verify email</button>\n</form>\n<form method=\"post\" action=\"/register/email-otp/issue\" class=\"mt-2\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <button class=\"btn btn-ghost\" type=\"submit\">Resend code</button>\n</form>\n");
				if (emails != null && emails.path("items").size() > 0) {
					jteOutput.writeContent("\n    <div class=\"divider\"></div>\n    <h3 style=\"font-size:0.95rem;\">Your mock inbox</h3>\n    <p class=\"small muted\" style=\"margin-bottom:10px;\">Email delivery is mocked locally — your messages appear here during registration.</p>\n    ");
					for (var email : emails.path("items")) {
						jteOutput.writeContent("\n        <details class=\"panel\" style=\"margin-bottom:8px;\">\n            <summary class=\"small\">✉ ");
						jteOutput.setContext("summary", null);
						jteOutput.writeUserContent(email.path("subject").asText());
						jteOutput.writeContent(" · ");
						jteOutput.setContext("summary", null);
						jteOutput.writeUserContent(fmtDateTime(email.path("createdAt").asText()));
						jteOutput.writeContent("</summary>\n            <div class=\"panel-body small\" style=\"white-space:pre-wrap;\">");
						jteOutput.setContext("div", null);
						jteOutput.writeUserContent(email.path("body").asText());
						jteOutput.writeContent("</div>\n        </details>\n    ");
					}
					jteOutput.writeContent("\n");
				}
				jteOutput.writeContent("\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode emails = (JsonNode)params.get("emails");
		render(jteOutput, jteHtmlInterceptor, flash, csrfToken, csrfParameterName, emails);
	}
}
