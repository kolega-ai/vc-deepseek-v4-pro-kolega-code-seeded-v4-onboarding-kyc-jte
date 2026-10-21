package gg.jte.generated.precompiled.auth;
import java.util.Map;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteresetpasswordGenerated {
	public static final String JTE_NAME = "auth/reset-password.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,1,5,5,5,8,8,9,9,11,11,12,12,12,12,12,12,12,12,12,20,20,22,22,30,30,31,31,31,32,32,32,1,2,3,4,4,4,4};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, Object> flash, String token, String csrfToken, String csrfParameterName) {
		gg.jte.generated.precompiled.layout.JteauthGenerated.render(jteOutput, jteHtmlInterceptor, "Reset your password", csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h2>Reset your password</h2>\n<p class=\"sub\">Reset links are single-use and valid for 30 minutes.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n");
				if (token != null && !token.isEmpty()) {
					jteOutput.writeContent("\n    <form method=\"post\" action=\"/reset-password/complete\">\n        ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n        <input type=\"hidden\" name=\"token\"");
					var __jte_html_attribute_0 = token;
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_0);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n        <div class=\"field\">\n            <label for=\"newPassword\">New password</label>\n            <input class=\"input\" type=\"password\" id=\"newPassword\" name=\"newPassword\" autocomplete=\"new-password\" required minlength=\"10\">\n            <div class=\"help\">At least 10 characters with an uppercase letter, a lowercase letter and a digit.</div>\n        </div>\n        <button class=\"btn btn-primary btn-lg\" type=\"submit\" style=\"width:100%;\">Set new password</button>\n    </form>\n");
				} else {
					jteOutput.writeContent("\n    <form method=\"post\" action=\"/reset-password/request\">\n        ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n        <div class=\"field\">\n            <label for=\"email\">Email address</label>\n            <input class=\"input\" type=\"email\" id=\"email\" name=\"email\" autocomplete=\"username\" required>\n        </div>\n        <button class=\"btn btn-primary btn-lg\" type=\"submit\" style=\"width:100%;\">Send reset link</button>\n    </form>\n    <div class=\"mt-2 small muted\"><a href=\"/login\">Back to sign in</a></div>\n");
				}
				jteOutput.writeContent("\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		String token = (String)params.get("token");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		render(jteOutput, jteHtmlInterceptor, flash, token, csrfToken, csrfParameterName);
	}
}
