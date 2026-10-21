package gg.jte.generated.precompiled.common;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteinboxGenerated {
	public static final String JTE_NAME = "common/inbox.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,9,9,9,11,11,11,13,13,15,15,16,16,19,19,19,20,20,20,20,20,20,20,20,20,23,23,24,24,24,25,25,26,26,26,27,27,30,30,31,31,32,32,32,33,33,33,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode result) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Demo inbox", "inbox", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Demo inbox</h1>\n<p class=\"muted\">Outbound emails and SMS (mock) addressed to <b>");
				jteOutput.setContext("b", null);
				jteOutput.writeUserContent(user.email());
				jteOutput.writeContent("</b>. In production these go to a real mail gateway.</p>\n\n");
				if (result.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">✉</span><h3>Inbox is empty</h3></div>\n");
				} else {
					jteOutput.writeContent("\n    ");
					for (var email : result.path("items")) {
						jteOutput.writeContent("\n        <details class=\"email-item\">\n            <summary>\n                <span class=\"e-subject\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(email.path("subject").asText());
						jteOutput.writeContent("</span>\n                <span class=\"e-meta\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(email.path("purpose").asText());
						jteOutput.writeContent(" · ");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(fmtDateTime(email.path("createdAt").asText()));
						jteOutput.writeContent(" · ");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(email.path("status").asText());
						jteOutput.writeContent("</span>\n            </summary>\n            <div class=\"panel-body\">\n                ");
						if (email.path("purpose").asText().startsWith("otp")) {
							jteOutput.writeContent("\n                    <div class=\"e-otp\">");
							jteOutput.setContext("div", null);
							jteOutput.writeUserContent(email.path("body").asText());
							jteOutput.writeContent("</div>\n                ");
						} else {
							jteOutput.writeContent("\n                    ");
							jteOutput.setContext("div", null);
							jteOutput.writeUserContent(email.path("body").asText());
							jteOutput.writeContent("\n                ");
						}
						jteOutput.writeContent("\n            </div>\n        </details>\n    ");
					}
					jteOutput.writeContent("\n");
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
		JsonNode result = (JsonNode)params.get("result");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, result);
	}
}
