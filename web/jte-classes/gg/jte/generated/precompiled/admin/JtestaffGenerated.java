package gg.jte.generated.precompiled.admin;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtestaffGenerated {
	public static final String JTE_NAME = "admin/staff.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,4,4,4,4,4,12,12,12,14,14,18,18,18,18,18,18,18,18,18,33,33,62,62,63,63,63,63,63,63,63,63,63,63,63,63,64,64,85,85,96,96,98,98,103,103,105,105,105,105,105,105,106,106,106,107,107,107,108,108,108,109,109,109,110,110,110,110,110,110,110,115,115,115,115,116,116,128,128,129,129,129,129,129,129,129,129,129,129,129,129,130,130,155,155,159,159,160,160,160,161,161,161,4,5,6,7,8,9,10,11,11,11,11};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WebUser user, long unread, String csrfToken, String csrfParameterName, JsonNode staff, JsonNode branches, Map<String, Object> filters, Map<String, Object> flash) {
		gg.jte.generated.precompiled.layout.JtemainGenerated.render(jteOutput, jteHtmlInterceptor, "Staff management", "admin-staff", user, unread, csrfToken, csrfParameterName, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n<h1>Staff management</h1>\n");
				gg.jte.generated.precompiled.partials.JteflashGenerated.render(jteOutput, jteHtmlInterceptor, flash);
				jteOutput.writeContent("\n\n<div class=\"card\">\n    <form method=\"get\" action=\"/admin/staff\" class=\"filters\">\n        <input class=\"input\" type=\"text\" name=\"q\" placeholder=\"Search name or employee id…\"");
				var __jte_html_attribute_0 = String.valueOf(filters.get("q"));
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" value=\"");
					jteOutput.setContext("input", "value");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("input", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent(">\n        <select class=\"input\" name=\"role\">\n            <option value=\"\">All roles</option>\n            <option value=\"ONBOARDING_OFFICER\">Onboarding officer</option>\n            <option value=\"COMPLIANCE_OFFICER\">Compliance officer</option>\n            <option value=\"BANK_ADMIN\">Bank admin</option>\n        </select>\n        <button class=\"btn btn-primary\" type=\"submit\">Filter</button>\n    </form>\n</div>\n\n<div class=\"grid main-2\">\n    <div class=\"card\">\n        <h3>Add a staff member</h3>\n        <form method=\"post\" action=\"/admin/staff\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"form-row\">\n                <div class=\"field\">\n                    <label>Employee ID</label>\n                    <input class=\"input\" type=\"text\" name=\"employeeId\" required maxlength=\"20\">\n                </div>\n                <div class=\"field\">\n                    <label>Full name</label>\n                    <input class=\"input\" type=\"text\" name=\"fullName\" required maxlength=\"120\">\n                </div>\n            </div>\n            <div class=\"form-row\">\n                <div class=\"field\">\n                    <label>Email</label>\n                    <input class=\"input\" type=\"email\" name=\"email\" required>\n                </div>\n                <div class=\"field\">\n                    <label>Role</label>\n                    <select class=\"input\" name=\"role\" required>\n                        <option value=\"ONBOARDING_OFFICER\">Onboarding officer</option>\n                        <option value=\"COMPLIANCE_OFFICER\">Compliance officer</option>\n                        <option value=\"BANK_ADMIN\">Bank admin</option>\n                    </select>\n                </div>\n            </div>\n            <div class=\"form-row\">\n                <div class=\"field\">\n                    <label>Home branch</label>\n                    <select class=\"input\" name=\"branchCode\" required>\n                        ");
				for (var branch : branches.path("items")) {
					jteOutput.writeContent("\n                            <option");
					var __jte_html_attribute_1 = branch.path("code").asText();
					if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
						jteOutput.writeContent(" value=\"");
						jteOutput.setContext("option", "value");
						jteOutput.writeUserContent(__jte_html_attribute_1);
						jteOutput.setContext("option", null);
						jteOutput.writeContent("\"");
					}
					jteOutput.writeContent(">");
					jteOutput.setContext("option", null);
					jteOutput.writeUserContent(branch.path("name").asText());
					jteOutput.writeContent("</option>\n                        ");
				}
				jteOutput.writeContent("\n                    </select>\n                </div>\n                <div class=\"field\">\n                    <label>Max approval tier (officers)</label>\n                    <select class=\"input\" name=\"maxApprovalTier\">\n                        <option value=\"tier_0\">tier_0</option>\n                        <option value=\"tier_1\">tier_1</option>\n                        <option value=\"tier_2\">tier_2</option>\n                        <option value=\"tier_3\">tier_3</option>\n                    </select>\n                </div>\n            </div>\n            <button class=\"btn btn-primary\" type=\"submit\">Create staff account</button>\n            <div class=\"help\">A set-password invitation is sent to the staff member's email (mock).</div>\n        </form>\n    </div>\n    <div class=\"card\">\n        <h3>Import a staff &amp; branch roster (CSV)</h3>\n        <p class=\"muted small\">Header: <span class=\"mono\">employee_id,full_name,role,branch_code,max_approval_tier,email</span>. Roles: ONBOARDING_OFFICER, COMPLIANCE_OFFICER, BANK_ADMIN. Row errors are reported.</p>\n        <form method=\"post\" action=\"/admin/staff/import\" enctype=\"multipart/form-data\">\n            ");
				gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
				jteOutput.writeContent("\n            <div class=\"dropzone\">\n                <span class=\"big\">⇪</span>\n                <input type=\"file\" name=\"file\" accept=\".csv,text/csv\" required data-label=\"fileLabel\">\n                <div id=\"fileLabel\">Choose a CSV file…</div>\n            </div>\n            <button class=\"btn btn-ghost mt-2\" type=\"submit\">Import roster</button>\n        </form>\n    </div>\n</div>\n\n");
				if (staff.path("items").size() == 0) {
					jteOutput.writeContent("\n    <div class=\"empty\"><span class=\"glyph\">◉</span><h3>No staff</h3></div>\n");
				} else {
					jteOutput.writeContent("\n    <div class=\"table-wrap\">\n        <table class=\"table\">\n            <thead><tr><th>Employee</th><th>Email</th><th>Role</th><th>Branch</th><th>Max tier</th><th>Status</th><th>Actions</th></tr></thead>\n            <tbody>\n                ");
					for (var s : staff.path("items")) {
						jteOutput.writeContent("\n                    <tr>\n                        <td class=\"cell-strong\">");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(s.path("fullName").asText());
						jteOutput.writeContent("<br><span class=\"cell-muted\">");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(s.path("employeeId").asText());
						jteOutput.writeContent("</span></td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(s.path("email").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(s.path("role").asText().replace("_", " "));
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(s.path("branchName").asText());
						jteOutput.writeContent("</td>\n                        <td>");
						jteOutput.setContext("td", null);
						jteOutput.writeUserContent(s.path("maxApprovalTier").asText());
						jteOutput.writeContent("</td>\n                        <td><span class=\"badge ");
						jteOutput.setContext("span", "class");
						jteOutput.writeUserContent(s.path("status").asText().equals("active") ? "badge-success" : "badge-danger");
						jteOutput.setContext("span", null);
						jteOutput.writeContent("\"><span class=\"dot\"></span>");
						jteOutput.setContext("span", null);
						jteOutput.writeUserContent(s.path("status").asText());
						jteOutput.writeContent("</span></td>\n                        <td>\n                            <details class=\"panel\" style=\"margin:0;\">\n                                <summary class=\"small\">Edit</summary>\n                                <div class=\"panel-body\">\n                                    <form method=\"post\" action=\"/admin/staff/");
						jteOutput.setContext("form", "action");
						jteOutput.writeUserContent(s.path("id").asText());
						jteOutput.setContext("form", null);
						jteOutput.writeContent("\">\n                                        ");
						gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
						jteOutput.writeContent("\n                                        <div class=\"field\">\n                                            <label>Role</label>\n                                            <select class=\"input\" name=\"role\">\n                                                <option value=\"ONBOARDING_OFFICER\">Onboarding officer</option>\n                                                <option value=\"COMPLIANCE_OFFICER\">Compliance officer</option>\n                                                <option value=\"BANK_ADMIN\">Bank admin</option>\n                                            </select>\n                                        </div>\n                                        <div class=\"field\">\n                                            <label>Branch</label>\n                                            <select class=\"input\" name=\"branchCode\">\n                                                ");
						for (var branch : branches.path("items")) {
							jteOutput.writeContent("\n                                                    <option");
							var __jte_html_attribute_2 = branch.path("code").asText();
							if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
								jteOutput.writeContent(" value=\"");
								jteOutput.setContext("option", "value");
								jteOutput.writeUserContent(__jte_html_attribute_2);
								jteOutput.setContext("option", null);
								jteOutput.writeContent("\"");
							}
							jteOutput.writeContent(">");
							jteOutput.setContext("option", null);
							jteOutput.writeUserContent(branch.path("name").asText());
							jteOutput.writeContent("</option>\n                                                ");
						}
						jteOutput.writeContent("\n                                            </select>\n                                        </div>\n                                        <div class=\"field\">\n                                            <label>Max approval tier</label>\n                                            <select class=\"input\" name=\"maxApprovalTier\">\n                                                <option value=\"tier_0\">tier_0</option>\n                                                <option value=\"tier_1\">tier_1</option>\n                                                <option value=\"tier_2\">tier_2</option>\n                                                <option value=\"tier_3\">tier_3</option>\n                                            </select>\n                                        </div>\n                                        <div class=\"field\">\n                                            <label>Status</label>\n                                            <select class=\"input\" name=\"status\">\n                                                <option value=\"active\">Active</option>\n                                                <option value=\"disabled\">Disabled</option>\n                                            </select>\n                                        </div>\n                                        <button class=\"btn btn-primary btn-sm\" type=\"submit\">Save changes</button>\n                                    </form>\n                                </div>\n                            </details>\n                        </td>\n                    </tr>\n                ");
					}
					jteOutput.writeContent("\n            </tbody>\n        </table>\n    </div>\n");
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
		JsonNode staff = (JsonNode)params.get("staff");
		JsonNode branches = (JsonNode)params.get("branches");
		Map<String, Object> filters = (Map<String, Object>)params.get("filters");
		Map<String, Object> flash = (Map<String, Object>)params.get("flash");
		render(jteOutput, jteHtmlInterceptor, user, unread, csrfToken, csrfParameterName, staff, branches, filters, flash);
	}
}
