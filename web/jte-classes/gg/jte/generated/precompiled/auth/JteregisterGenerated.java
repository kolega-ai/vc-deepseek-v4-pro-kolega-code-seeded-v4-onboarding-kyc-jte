package gg.jte.generated.precompiled.auth;
import java.util.Map;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteregisterGenerated {
	public static final String JTE_NAME = "auth/register.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,1,4,4,4,7,7,9,9,23,23,23,24,24,24,1,2,3,3,3,3};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, Object> flash, String csrfToken, String csrfParameterName) {
		gg.jte.generated.precompiled.layout.JteauthGenerated.render(jteOutput, jteHtmlInterceptor, "Start your application", csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h2>Start your application</h2>\n<p class=\"sub\">Step 1 of 3 — your contact details. We will verify your email and mobile with one-time codes.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/register\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"email\">Email address</label>\n        <input class=\"input\" type=\"email\" id=\"email\" name=\"email\" autocomplete=\"email\" required placeholder=\"you@example.com\">\n        <div class=\"help\">Used for sign-in and notifications.</div>\n    </div>\n    <div class=\"field\">\n        <label for=\"mobile\">Mobile number</label>\n        <input class=\"input\" id=\"mobile\" name=\"mobile\" required placeholder=\"+65 8123 4567\" inputmode=\"tel\">\n        <div class=\"help\">Singapore mobile — we verify it with a one-time code.</div>\n    </div>\n    <button class=\"btn btn-primary btn-lg\" type=\"submit\" style=\"width:100%;\">Continue</button>\n</form>\n<div class=\"mt-2 small muted\">Already have an account? <a href=\"/login\">Sign in</a></div>\n");
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
