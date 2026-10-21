package gg.jte.generated.precompiled.error;
import java.util.Map;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class Jte403Generated {
	public static final String JTE_NAME = "error/403.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,1,15,15,15,15,21,21,21,1,1,1,1};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String message) {
		jteOutput.writeContent("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Access denied · Meridian Bank</title>\n    <link rel=\"stylesheet\" href=\"/css/app.css\">\n</head>\n<body style=\"display:grid;place-items:center;min-height:100vh;background:var(--paper);\">\n    <div class=\"card\" style=\"max-width:460px;text-align:center;\">\n        <div class=\"empty\" style=\"border:0;\">\n            <span class=\"glyph\">⛔</span>\n            <h3>Access denied</h3>\n            ");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(message);
		jteOutput.writeContent("\n        </div>\n        <a class=\"btn btn-primary\" href=\"/\">Back to safety</a>\n    </div>\n</body>\n</html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String message = (String)params.get("message");
		render(jteOutput, jteHtmlInterceptor, message);
	}
}
