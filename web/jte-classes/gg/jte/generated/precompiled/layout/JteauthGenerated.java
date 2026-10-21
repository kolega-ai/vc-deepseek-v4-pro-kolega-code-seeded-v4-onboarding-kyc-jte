package gg.jte.generated.precompiled.layout;
import gg.jte.Content;
@SuppressWarnings("unchecked")
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public final class JteauthGenerated {
	public static final String JTE_NAME = "layout/auth.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,1,10,10,10,10,38,38,38,44,44,44,1,2,3,4,4,4,4};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String pageTitle, String csrfToken, String csrfParameterName, Content content) {
		jteOutput.writeContent("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(pageTitle);
		jteOutput.writeContent(" · Meridian Bank</title>\n    <link rel=\"stylesheet\" href=\"/css/app.css\">\n    <link rel=\"icon\" type=\"image/svg+xml\" href=\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='46' fill='%230B6A5C'/%3E%3Ctext x='50' y='66' font-size='52' text-anchor='middle' fill='%23FDF8EF' font-family='Georgia'%3EM%3C/text%3E%3C/svg%3E\">\n</head>\n<body>\n<div class=\"auth-wrap\">\n    <div class=\"auth-brand-panel\">\n        <div class=\"brand-row\">\n            <div class=\"brand-mark\">M</div>\n            <div>\n                <div class=\"brand-name\" style=\"color:#FBF7EC;font-family:var(--font-display);font-size:1.05rem;\">Meridian Bank</div>\n                <div class=\"brand-sub\" style=\"color:#7E96AC;font-size:0.68rem;letter-spacing:0.14em;text-transform:uppercase;\">Digital Onboarding</div>\n            </div>\n        </div>\n        <div>\n            <h1>Open your account,<br>verified end to end.</h1>\n            <p class=\"lede\">A fully digital onboarding journey — identity verification, document checks, watchlist screening and KYC tiers, managed by our branch and compliance teams.</p>\n            <div class=\"points\">\n                <div class=\"point\"><span class=\"tick\">✓</span><span>Seven guided steps, from registration to account opening.</span></div>\n                <div class=\"point\"><span class=\"tick\">✓</span><span>KYC tiers with transaction and balance limits in SGD.</span></div>\n                <div class=\"point\"><span class=\"tick\">✓</span><span>OTP-verified contact details and step-up approvals.</span></div>\n                <div class=\"point\"><span class=\"tick\">✓</span><span>Periodic re-KYC keeps your account up to date.</span></div>\n            </div>\n        </div>\n        <div class=\"foot\">Demo environment · identity providers, credit bureau, watchlist and core banking are local mocks.</div>\n    </div>\n    <div class=\"auth-form-panel\">\n        <div class=\"auth-card\">\n            ");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(content);
		jteOutput.writeContent("\n        </div>\n    </div>\n</div>\n</body>\n</html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String pageTitle = (String)params.get("pageTitle");
		String csrfToken = (String)params.get("csrfToken");
		String csrfParameterName = (String)params.get("csrfParameterName");
		Content content = (Content)params.get("content");
		render(jteOutput, jteHtmlInterceptor, pageTitle, csrfToken, csrfParameterName, content);
	}
}
