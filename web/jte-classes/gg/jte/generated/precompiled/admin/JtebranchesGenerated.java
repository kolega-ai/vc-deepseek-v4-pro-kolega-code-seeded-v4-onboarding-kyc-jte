package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtebranchesGenerated {
	public static final String JTE_NAME = "admin/branches.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,3,3,9,9,9,11,11,17,17,39,39,59,59,61,61,61,62,62,62,63,63,63,64,64,64,69,69,69,69,70,70,73,73,73,73,73,73,73,73,73,77,77,77,77,77,77,77,77,77,85,85,89,89,89,90,90,90,3,4,5,6,7,8,8,8,8};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode branches, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Branches", "admin-branches", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Branches</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Add a branch</h3>\n        <form method=\"post\" action=\"/admin/branches\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"form-row\">\n                <div class=\"field\">\n                    <label>Branch code</label>\n                    <input class=\"input\" type=\"text\" name=\"code\" required maxlength=\"20\" placeholder=\"MB-NOR-04\">\n                </div>\n                <div class=\"field\">\n                    <label>Name</label>\n                    <input class=\"input\" type=\"text\" name=\"name\" required maxlength=\"120\">\n                </div>\n            </div>\n            <div class=\"field\">\n                <label>Region</label>\n                <input class=\"input\" type=\"text\" name=\"region\" required maxlength=\"60\" placeholder=\"North\">\n            </div>\n            <button class=\"btn btn-primary\" type=\"submit\">Create branch</button>\n        </form>\n    </div>\n    <div class=\"card\">\n        <h3>Import branch reference data (XML)</h3>\n        <p class=\"muted small\">Benign reference-data XML import. The server parses it with a hardened parser — DTDs and external entities are rejected.</p>\n        <form method=\"post\" action=\"/admin/branches/import-xml\" enctype=\"multipart/form-data\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"dropzone\">\n                <span class=\"big\">⇪</span>\n                <input type=\"file\" name=\"file\" accept=\".xml,application/xml\" required data-label=\"fileLabel\">\n                <div id=\"fileLabel\">Choose an XML file…</div>\n            </div>\n            <button class=\"btn btn-ghost mt-2\" type=\"submit\">Import XML</button>\n        </form>\n        <pre class=\"small faint mt-2\" style=\"background:var(--paper-2);padding:10px;border-radius:8px;overflow:auto;\">&lt;reference-data&gt;\n  &lt;branches&gt;\n    &lt;branch&gt;&lt;code&gt;MB-NOR-04&lt;/code&gt;&lt;name&gt;Woodlands Central&lt;/name&gt;&lt;region&gt;North&lt;/region&gt;&lt;/branch&gt;\n  &lt;/branches&gt;\n&lt;/reference-data&gt;</pre>\n    </div>\n</div>\n\n<div class=\"table-wrap\">\n    <table class=\"table\">\n        <thead><tr><th>Code</th><th>Name</th><th>Region</th><th>Staff</th><th>Actions</th></tr></thead>\n        <tbody>\n            ");
				for (var branch : branches.path("items")) {
					jteOutput.writeContent("\n                <tr>\n                    <td class=\"cell-strong mono\">");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(branch.path("code").asText());
					jteOutput.writeContent("</td>\n                    <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(branch.path("name").asText());
					jteOutput.writeContent("</td>\n                    <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(branch.path("region").asText());
					jteOutput.writeContent("</td>\n                    <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(branch.path("staff").asText());
					jteOutput.writeContent("</td>\n                    <td>\n                        <details class=\"panel\" style=\"margin:0;\">\n                            <summary class=\"small\">Edit</summary>\n                            <div class=\"panel-body\">\n                                <form method=\"post\" action=\"/admin/branches/");
					jteOutput.setContext("form", "action");
					jteOutput.writeUserContent(branch.path("id").asText());
					jteOutput.setContext("form", null);
					jteOutput.writeContent("\">\n                                    ");
					gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
					jteOutput.writeContent("\n                                    <div class=\"field\">\n                                        <label>Name</label>\n                                        <input class=\"input\" type=\"text\" name=\"name\" required");
					var __jte_html_attribute_0 = branch.path("name").asText();
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_0);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n                                    </div>\n                                    <div class=\"field\">\n                                        <label>Region</label>\n                                        <input class=\"input\" type=\"text\" name=\"region\" required");
					var __jte_html_attribute_1 = branch.path("region").asText();
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("input", "value");
						jteOutput.writeUserContent(__jte_html_attribute_1);
						jteOutput.setContext("input", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">\n                                    </div>\n                                    <button class=\"btn btn-primary btn-sm\" type=\"submit\">Save</button>\n                                </form>\n                            </div>\n                        </details>\n                    </td>\n                </tr>\n            ");
				}
				jteOutput.writeContent("\n        </tbody>\n    </table>\n</div>\n");
			}
		});
		jteOutput.writeContent("\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		JsonNode branches = (JsonNode)params.get("branches");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, branches, flash);
	}
}
