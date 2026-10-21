package gg.jte.generated.precompiled.partials;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteflashGenerated {
	public static final String JTE_NAME = "partials/flash.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,1,2,2,3,3,3,3,4,4,4,5,5,5,7,7,8,8,8,1,1,1,1};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> flash) {
		if (flash != null) {
			jteOutput.writeContent("\n    <div class=\"alert alert-");
			jteOutput.setContext("div", "class");
			jteOutput.writeUserContent(String.valueOf(flash.get("kind")));
			jteOutput.setContext("div", null);
			jteOutput.writeContent("\">\n        <span class=\"icon\">");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(flashIcon(String.valueOf(flash.get("kind"))));
			jteOutput.writeContent("</span>\n        <span>");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(String.valueOf(flash.get("message")));
			jteOutput.writeContent("</span>\n    </div>\n");
		}
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		java.util.Map<String, Object> flash = (java.util.Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, flash);
	}
}
