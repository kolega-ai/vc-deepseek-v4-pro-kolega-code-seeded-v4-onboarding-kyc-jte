package gg.jte.generated.precompiled.common;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtenotificationsGenerated {
	public static final String JTE_NAME = "common/notifications.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,9,9,9,13,13,19,19,21,21,22,22,23,23,23,23,24,24,24,24,25,25,27,27,29,29,31,31,33,33,35,35,38,38,38,39,39,39,40,40,40,43,43,44,44,44,44,44,44,44,44,44,45,45,46,46,47,47,47,47,48,48,51,51,54,54,55,55,56,56,56,57,57,57,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode result) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Notifications", "notifications", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<div class=\"flex-between mb-2\">\n    <h1 class=\"mb-0\">Notifications</h1>\n    <form method=\"post\" action=\"/notifications/read-all\">\n        ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n        <button class=\"btn btn-ghost btn-sm\" type=\"submit\">Mark all as read</button>\n    </form>\n</div>\n<p class=\"muted small\">OTP codes for step-up actions arrive here (SMS delivery is mocked locally).</p>\n\n");
				if (result.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">◎</span><h3>No notifications</h3></div>\n");
				} else {
					jteOutput.writeContent("\n    ");
					for (var n : result.path("items")) {
						jteOutput.writeContent("\n        <div class=\"notification-item ");
						jteOutput.setContext("div", "class");
						jteOutput.writeUserContent(n.path("read").asBoolean() ? "" : "unread");
						jteOutput.setContext("div", null);
						jteOutput.writeContent("\">\n            <div class=\"n-icon ");
						jteOutput.setContext("div", "class");
						jteOutput.writeUserContent(n.path("type").asText());
						jteOutput.setContext("div", null);
						jteOutput.writeContent("\">\n                ");
						if (n.path("type").asText().equals("otp")) {
							jteOutput.writeContent("\n                    ◈\n                ");
						} else if (n.path("type").asText().equals("success")) {
							jteOutput.writeContent("\n                    ✓\n                ");
						} else if (n.path("type").asText().equals("warning")) {
							jteOutput.writeContent("\n                    ⚠\n                ");
						} else if (n.path("type").asText().equals("action_required")) {
							jteOutput.writeContent("\n                    !\n                ");
						} else {
							jteOutput.writeContent("\n                    ✦\n                ");
						}
						jteOutput.writeContent("\n            </div>\n            <div style=\"flex:1;\">\n                <div class=\"n-title\">");
						jteOutput.setContext("div", null);
						jteOutput.writeUserContent(n.path("title").asText());
						jteOutput.writeContent("</div>\n                <div class=\"n-body\">");
						jteOutput.setContext("div", null);
						jteOutput.writeUserContent(n.path("body").asText());
						jteOutput.writeContent("</div>\n                <div class=\"n-time\">");
						jteOutput.setContext("div", null);
						jteOutput.writeUserContent(fmtDateTime(n.path("createdAt").asText()));
						jteOutput.writeContent("</div>\n            </div>\n            <div>\n                ");
						if (n.path("link") != null && !n.path("link").asText().isEmpty()) {
							jteOutput.writeContent("\n                    <a class=\"btn btn-ghost btn-sm\"");
							var __jte_html_attribute_0 = n.path("link").asText();
							if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
								jteOutput.writeContent(" href=\"");
								jteOutput.setContext("a", "href");
								jteOutput.writeUserContent(__jte_html_attribute_0);
								jteOutput.setContext("a", null);
								jteOutput.writeContent("\"");
							}
							jteOutput.writeContent(">Open</a>\n                ");
						}
						jteOutput.writeContent("\n                ");
						if (!n.path("read").asBoolean()) {
							jteOutput.writeContent("\n                    <form method=\"post\" action=\"/notifications/");
							jteOutput.setContext("form", "action");
							jteOutput.writeUserContent(n.path("id").asText());
							jteOutput.setContext("form", null);
							jteOutput.writeContent("/read\">\n                        ");
							gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
							jteOutput.writeContent("\n                        <button class=\"btn btn-link btn-sm\" type=\"submit\">Mark read</button>\n                    </form>\n                ");
						}
						jteOutput.writeContent("\n            </div>\n        </div>\n    ");
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
