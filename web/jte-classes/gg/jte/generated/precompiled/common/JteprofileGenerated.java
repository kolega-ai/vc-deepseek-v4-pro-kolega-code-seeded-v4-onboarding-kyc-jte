package gg.jte.generated.precompiled.common;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteprofileGenerated {
	public static final String JTE_NAME = "common/profile.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,3,3,9,9,9,11,11,17,17,17,18,18,18,19,19,19,20,20,20,21,21,21,28,28,32,32,50,50,50,51,51,51,3,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode me, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Profile & security", "profile", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Profile &amp; security</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Account</h3>\n        <dl class=\"kv\">\n            <dt>Name</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(me.path("displayName").asText());
				jteOutput.writeContent("</dd>\n            <dt>Email</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(me.path("email").asText());
				jteOutput.writeContent("</dd>\n            <dt>Role</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(me.path("role").asText().replace("_", " "));
				jteOutput.writeContent("</dd>\n            <dt>Email verified</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(me.path("emailVerified").asBoolean() ? "✓ yes" : "no");
				jteOutput.writeContent("</dd>\n            <dt>Mobile verified</dt><dd>");
				jteOutput.setContext("dd", null);
				jteOutput.writeUserContent(me.path("mobileVerified").asBoolean() ? "✓ yes" : "no");
				jteOutput.writeContent("</dd>\n        </dl>\n    </div>\n    <div class=\"card\">\n        <h3>Change password</h3>\n        <p class=\"muted small\">Changing your password requires a step-up OTP.</p>\n        <form method=\"post\" action=\"/profile/password/otp\" class=\"mb-1\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <button class=\"btn btn-ghost btn-sm\" type=\"submit\">1 · Send step-up code</button>\n        </form>\n        <form method=\"post\" action=\"/profile/password\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"field\">\n                <label>Current password</label>\n                <input class=\"input\" type=\"password\" name=\"currentPassword\" autocomplete=\"current-password\" required>\n            </div>\n            <div class=\"field\">\n                <label>New password</label>\n                <input class=\"input\" type=\"password\" name=\"newPassword\" autocomplete=\"new-password\" required minlength=\"10\">\n                <div class=\"help\">At least 10 characters with an uppercase letter, a lowercase letter and a digit.</div>\n            </div>\n            <div class=\"field\">\n                <label>Step-up code (2)</label>\n                <input class=\"input\" type=\"text\" name=\"otpCode\" required inputmode=\"numeric\" maxlength=\"6\" placeholder=\"6-digit code\" data-otp>\n            </div>\n            <button class=\"btn btn-primary\" type=\"submit\">3 · Change password</button>\n        </form>\n    </div>\n</div>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode me = (JsonNode)params.get("me");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, me, flash);
	}
}
