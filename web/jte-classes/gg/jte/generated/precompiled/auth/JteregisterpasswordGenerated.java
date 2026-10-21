package gg.jte.generated.precompiled.auth;
import java.util.Map;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteregisterpasswordGenerated {
	public static final String JTE_NAME = "auth/register-password.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,1,4,4,4,7,7,9,9,17,17,17,18,18,18,1,2,3,3,3,3};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, Object> flash, String csrfToken, String csrfParameterName) {
		gg.jte.generated.precompiled.layout.JteauthGenerated.render(jteOutput, jteHtmlInterceptor, "Create your password", csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h2>Create your password</h2>\n<p class=\"sub\">Step 3 of 3 — the last thing before your application begins.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/register/password\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"password\">Password</label>\n        <input class=\"input\" type=\"password\" id=\"password\" name=\"password\" autocomplete=\"new-password\" required minlength=\"10\">\n        <div class=\"help\">At least 10 characters with an uppercase letter, a lowercase letter and a digit.</div>\n    </div>\n    <button class=\"btn btn-primary btn-lg\" type=\"submit\" style=\"width:100%;\">Create account</button>\n</form>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		render(jteOutput, jteHtmlInterceptor, flash, csrfToken, csrfParameterName);
	}
}
