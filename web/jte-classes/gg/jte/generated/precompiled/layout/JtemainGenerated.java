package gg.jte.generated.precompiled.layout;
import gg.jte.Content;
import com.meridianbank.web.auth.WebUser;
import static com.meridianbank.web.template.Tpl.*;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JtemainGenerated {
	public static final String JTE_NAME = "layout/main.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,3,3,3,3,3,15,15,15,15,30,30,31,31,33,33,33,33,33,33,33,33,33,34,34,34,34,34,34,34,34,34,35,35,35,35,35,35,35,35,35,36,36,38,38,38,38,38,38,38,38,38,39,39,39,39,39,39,39,39,39,40,40,40,40,40,40,40,40,40,41,41,43,43,43,43,43,43,43,43,43,44,44,44,44,44,44,44,44,44,45,45,47,47,47,47,47,47,47,47,47,48,48,48,48,48,48,48,48,48,49,49,49,49,49,49,49,49,49,50,50,52,52,52,52,52,52,52,52,52,53,53,53,53,53,53,53,53,53,54,54,54,54,54,54,54,54,54,55,55,55,55,55,55,55,55,55,56,56,56,56,56,56,56,56,56,57,57,57,57,57,57,57,57,57,58,58,58,58,58,58,58,58,58,59,59,61,61,61,61,61,61,61,61,61,62,62,62,62,62,62,62,62,62,63,63,63,63,63,63,63,63,63,64,64,78,78,78,82,82,85,85,86,86,86,87,87,90,90,90,92,92,92,93,93,93,97,97,100,100,104,104,104,111,111,111,3,4,5,6,7,8,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String pageTitle, String activeNav, WebUser user, long unread, String csrfToken, String csrfParameterName, Content content) {
		jteOutput.writeContent("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(pageTitle);
		jteOutput.writeContent(" · Meridian Bank</title>\n    <link rel=\"stylesheet\" href=\"/css/app.css\">\n    <link rel=\"icon\" type=\"image/svg+xml\" href=\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='46' fill='%230B6A5C'/%3E%3Ctext x='50' y='66' font-size='52' text-anchor='middle' fill='%23FDF8EF' font-family='Georgia'%3EM%3C/text%3E%3C/svg%3E\">\n</head>\n<body>\n<div class=\"shell\">\n    <aside class=\"sidebar\" id=\"sidebar\">\n        <div class=\"brand\">\n            <div class=\"brand-mark\">M</div>\n            <div>\n                <div class=\"brand-name\">Meridian Bank</div>\n                <div class=\"brand-sub\">Digital Onboarding</div>\n            </div>\n        </div>\n        <nav class=\"nav\">\n            ");
		if (user != null) {
			jteOutput.writeContent("\n                ");
			if (user.role().equals("prospect")) {
				jteOutput.writeContent("\n                    <div class=\"nav-label\">Your application</div>\n                    <a href=\"/journey\"");
				var __jte_html_attribute_0 = navClass(activeNav, "journey");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_0);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">✦</span>Journey</a>\n                    <a href=\"/journey/documents\"");
				var __jte_html_attribute_1 = navClass(activeNav, "documents");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_1);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">▤</span>Documents</a>\n                    <a href=\"/journey/status\"");
				var __jte_html_attribute_2 = navClass(activeNav, "status");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_2);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◷</span>Status</a>\n                ");
			} else if (user.role().equals("customer")) {
				jteOutput.writeContent("\n                    <div class=\"nav-label\">Your accounts</div>\n                    <a href=\"/customer/dashboard\"");
				var __jte_html_attribute_3 = navClass(activeNav, "customer");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_3)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_3);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◈</span>Dashboard</a>\n                    <a href=\"/customer/rekyc\"");
				var __jte_html_attribute_4 = navClass(activeNav, "customer-rekyc");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_4)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_4);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">↻</span>Re-KYC</a>\n                    <a href=\"/journey/status\"");
				var __jte_html_attribute_5 = navClass(activeNav, "status");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_5)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_5);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◷</span>Application</a>\n                ");
			} else if (user.role().equals("onboarding_officer")) {
				jteOutput.writeContent("\n                    <div class=\"nav-label\">Officer console</div>\n                    <a href=\"/officer/dashboard\"");
				var __jte_html_attribute_6 = navClass(activeNav, "officer");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_6)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_6);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◈</span>Dashboard</a>\n                    <a href=\"/officer/applications\"");
				var __jte_html_attribute_7 = navClass(activeNav, "officer-apps");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_7)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_7);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">▦</span>Applications</a>\n                ");
			} else if (user.role().equals("compliance_officer")) {
				jteOutput.writeContent("\n                    <div class=\"nav-label\">Compliance console</div>\n                    <a href=\"/compliance/dashboard\"");
				var __jte_html_attribute_8 = navClass(activeNav, "compliance");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_8)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_8);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◈</span>Dashboard</a>\n                    <a href=\"/compliance/hits\"");
				var __jte_html_attribute_9 = navClass(activeNav, "compliance-hits");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_9)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_9);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">⚠</span>Watchlist queue</a>\n                    <a href=\"/compliance/rekyc\"");
				var __jte_html_attribute_10 = navClass(activeNav, "compliance-rekyc");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_10)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_10);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">↻</span>Re-KYC campaigns</a>\n                ");
			} else if (user.role().equals("bank_admin")) {
				jteOutput.writeContent("\n                    <div class=\"nav-label\">Bank administration</div>\n                    <a href=\"/admin/dashboard\"");
				var __jte_html_attribute_11 = navClass(activeNav, "admin");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_11)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_11);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◈</span>Dashboard</a>\n                    <a href=\"/admin/staff\"");
				var __jte_html_attribute_12 = navClass(activeNav, "admin-staff");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_12)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_12);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">◉</span>Staff</a>\n                    <a href=\"/admin/branches\"");
				var __jte_html_attribute_13 = navClass(activeNav, "admin-branches");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_13)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_13);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">⌂</span>Branches</a>\n                    <a href=\"/admin/kyc-tiers\"");
				var __jte_html_attribute_14 = navClass(activeNav, "admin-tiers");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_14)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_14);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">▤</span>KYC tier policy</a>\n                    <a href=\"/admin/requirements\"");
				var __jte_html_attribute_15 = navClass(activeNav, "admin-reqs");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_15)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_15);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">▥</span>Document requirements</a>\n                    <a href=\"/admin/audit\"");
				var __jte_html_attribute_16 = navClass(activeNav, "admin-audit");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_16)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_16);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">≡</span>Audit log</a>\n                    <a href=\"/admin/integrations\"");
				var __jte_html_attribute_17 = navClass(activeNav, "admin-integrations");
				if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_17)) {
					jteOutput.writeContent(" class=\"");
					jteOutput.setContext("a", "class");
					jteOutput.writeUserContent(__jte_html_attribute_17);
					jteOutput.setContext("a", null);
					jteOutput.writeContent("\"");
				}
				jteOutput.writeContent("><span class=\"nav-icon\">⇄</span>Integrations</a>\n                ");
			}
			jteOutput.writeContent("\n                <div class=\"nav-label\">Account</div>\n                <a href=\"/notifications\"");
			var __jte_html_attribute_18 = navClass(activeNav, "notifications");
			if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_18)) {
				jteOutput.writeContent(" class=\"");
				jteOutput.setContext("a", "class");
				jteOutput.writeUserContent(__jte_html_attribute_18);
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\"");
			}
			jteOutput.writeContent("><span class=\"nav-icon\">◎</span>Notifications</a>\n                <a href=\"/inbox\"");
			var __jte_html_attribute_19 = navClass(activeNav, "inbox");
			if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_19)) {
				jteOutput.writeContent(" class=\"");
				jteOutput.setContext("a", "class");
				jteOutput.writeUserContent(__jte_html_attribute_19);
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\"");
			}
			jteOutput.writeContent("><span class=\"nav-icon\">✉</span>Demo inbox</a>\n                <a href=\"/profile\"");
			var __jte_html_attribute_20 = navClass(activeNav, "profile");
			if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_20)) {
				jteOutput.writeContent(" class=\"");
				jteOutput.setContext("a", "class");
				jteOutput.writeUserContent(__jte_html_attribute_20);
				jteOutput.setContext("a", null);
				jteOutput.writeContent("\"");
			}
			jteOutput.writeContent("><span class=\"nav-icon\">●</span>Profile &amp; security</a>\n            ");
		}
		jteOutput.writeContent("\n        </nav>\n        <div class=\"sidebar-foot\">\n            Meridian Bank SG · demo environment<br>\n            All provider and core systems are local mocks.\n        </div>\n    </aside>\n\n    <div class=\"main\">\n        <header class=\"topbar\">\n            <div class=\"flex\">\n                <button class=\"icon-button menu-toggle\" id=\"menu-toggle\" aria-label=\"Open navigation\">☰</button>\n                <div>\n                    <div class=\"crumb\">Meridian Bank · Digital Onboarding</div>\n                    <div class=\"page-title\">");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(pageTitle);
		jteOutput.writeContent("</div>\n                </div>\n            </div>\n            <div class=\"topbar-right\">\n                ");
		if (user != null) {
			jteOutput.writeContent("\n                    <a class=\"icon-button\" href=\"/notifications\" aria-label=\"Notifications\">\n                        ◎\n                        ");
			if (unread > 0) {
				jteOutput.writeContent("\n                            <span class=\"dot\">");
				jteOutput.setContext("span", null);
				jteOutput.writeUserContent(unread);
				jteOutput.writeContent("</span>\n                        ");
			}
			jteOutput.writeContent("\n                    </a>\n                    <a class=\"user-chip\" href=\"/profile\">\n                        <span class=\"avatar\">");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(firstInitial(user.displayName()));
			jteOutput.writeContent("</span>\n                        <span class=\"who\">\n                            <span class=\"name\">");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(user.displayName());
			jteOutput.writeContent("</span>\n                            <span class=\"role\">");
			jteOutput.setContext("span", null);
			jteOutput.writeUserContent(user.role().replace("_", " "));
			jteOutput.writeContent("</span>\n                        </span>\n                    </a>\n                    <form method=\"post\" action=\"/logout\">\n                        ");
			gg.jte.generated.precompiled.partials.JtecsrfGenerated.render(jteOutput, jteHtmlInterceptor, csrfToken, csrfParameterName);
			jteOutput.writeContent("\n                        <button class=\"btn btn-ghost btn-sm\" type=\"submit\">Sign out</button>\n                    </form>\n                ");
		}
		jteOutput.writeContent("\n            </div>\n        </header>\n        <main class=\"content\">\n            ");
		jteOutput.setContext("main", null);
		jteOutput.writeUserContent(content);
		jteOutput.writeContent("\n        </main>\n    </div>\n</div>\n<script src=\"/js/app.js\"></script>\n</body>\n</html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String pageTitle = (String)params.get("pageTitle");
		String activeNav = (String)params.get("activeNav");
		WebUser user = (WebUser)params.get("user");
		long unread = (long)params.get("unread");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		Content content = (Content)params.get("content");
		render(jteOutput, jteHtmlInterceptor, pageTitle, activeNav, user, unread, csrfToken, csrfParameterName, content);
	}
}
