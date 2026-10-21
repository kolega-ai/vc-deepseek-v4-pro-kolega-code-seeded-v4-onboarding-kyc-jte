package gg.jte.generated.precompiled.auth;
import java.util.Map;
import java.util.List;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteloginGenerated {
	public static final String JTE_NAME = "auth/login.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,2,2,2,2,5,5,5,8,8,10,10,37,37,37,38,38,38,2,3,4,4,4,4};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, Object> flash, String csrfToken, String csrfParameterName) {
		gg.jte.generated.precompiled.layout.JteauthGenerated.render(jteOutput, jteHtmlInterceptor, "Sign in", csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h2>Welcome back</h2>\n<p class=\"sub\">Sign in to continue your application or manage onboarding work.</p>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n<form method=\"post\" action=\"/login\">\n    ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n    <div class=\"field\">\n        <label for=\"email\">Email address</label>\n        <input class=\"input\" type=\"email\" id=\"email\" name=\"email\" autocomplete=\"username\" required placeholder=\"you@example.com\">\n    </div>\n    <div class=\"field\">\n        <label for=\"password\">Password</label>\n        <input class=\"input\" type=\"password\" id=\"password\" name=\"password\" autocomplete=\"current-password\" required>\n    </div>\n    <button class=\"btn btn-primary btn-lg\" type=\"submit\" style=\"width:100%;\">Sign in</button>\n</form>\n<div class=\"mt-2 flex flex-wrap\">\n    <a href=\"/reset-password\" class=\"small\">Forgot password?</a>\n    <span class=\"right small muted\">New here? <a href=\"/register\">Start an application</a></span>\n</div>\n<div class=\"divider\"></div>\n<details class=\"panel\">\n    <summary class=\"small\">Demo credentials (every role)</summary>\n    <div class=\"panel-body small\" style=\"line-height:1.9;\">\n        <b>Prospect</b> — prospect@meridian.demo / Meridian#2026!<br>\n        <b>Customer</b> — customer@meridian.demo / Meridian#2026!<br>\n        <b>Officer</b> (Central, tier 3) — officer@meridian.demo / Meridian#2026!<br>\n        <b>Officer</b> (East, tier 1) — officer.east1@meridian.demo / Meridian#2026!<br>\n        <b>Compliance</b> — compliance@meridian.demo / Meridian#2026!<br>\n        <b>Admin</b> — admin@meridian.demo / Meridian#2026!\n    </div>\n</details>\n");
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
