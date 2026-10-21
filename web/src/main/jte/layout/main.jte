@import gg.jte.Content
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param String pageTitle
@param String activeNav
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param Content content
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} · Meridian Bank</title>
    <link rel="stylesheet" href="/css/app.css">
    <link rel="icon" type="image/svg+xml" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='46' fill='%230B6A5C'/%3E%3Ctext x='50' y='66' font-size='52' text-anchor='middle' fill='%23FDF8EF' font-family='Georgia'%3EM%3C/text%3E%3C/svg%3E">
</head>
<body>
<div class="shell">
    <aside class="sidebar" id="sidebar">
        <div class="brand">
            <div class="brand-mark">M</div>
            <div>
                <div class="brand-name">Meridian Bank</div>
                <div class="brand-sub">Digital Onboarding</div>
            </div>
        </div>
        <nav class="nav">
            @if (user != null)
                @if (user.role().equals("prospect"))
                    <div class="nav-label">Your application</div>
                    <a href="/journey" class="${navClass(activeNav, "journey")}"><span class="nav-icon">✦</span>Journey</a>
                    <a href="/journey/documents" class="${navClass(activeNav, "documents")}"><span class="nav-icon">▤</span>Documents</a>
                    <a href="/journey/status" class="${navClass(activeNav, "status")}"><span class="nav-icon">◷</span>Status</a>
                @elseif (user.role().equals("customer"))
                    <div class="nav-label">Your accounts</div>
                    <a href="/customer/dashboard" class="${navClass(activeNav, "customer")}"><span class="nav-icon">◈</span>Dashboard</a>
                    <a href="/customer/rekyc" class="${navClass(activeNav, "customer-rekyc")}"><span class="nav-icon">↻</span>Re-KYC</a>
                    <a href="/journey/status" class="${navClass(activeNav, "status")}"><span class="nav-icon">◷</span>Application</a>
                @elseif (user.role().equals("onboarding_officer"))
                    <div class="nav-label">Officer console</div>
                    <a href="/officer/dashboard" class="${navClass(activeNav, "officer")}"><span class="nav-icon">◈</span>Dashboard</a>
                    <a href="/officer/applications" class="${navClass(activeNav, "officer-apps")}"><span class="nav-icon">▦</span>Applications</a>
                @elseif (user.role().equals("compliance_officer"))
                    <div class="nav-label">Compliance console</div>
                    <a href="/compliance/dashboard" class="${navClass(activeNav, "compliance")}"><span class="nav-icon">◈</span>Dashboard</a>
                    <a href="/compliance/hits" class="${navClass(activeNav, "compliance-hits")}"><span class="nav-icon">⚠</span>Watchlist queue</a>
                    <a href="/compliance/rekyc" class="${navClass(activeNav, "compliance-rekyc")}"><span class="nav-icon">↻</span>Re-KYC campaigns</a>
                @elseif (user.role().equals("bank_admin"))
                    <div class="nav-label">Bank administration</div>
                    <a href="/admin/dashboard" class="${navClass(activeNav, "admin")}"><span class="nav-icon">◈</span>Dashboard</a>
                    <a href="/admin/staff" class="${navClass(activeNav, "admin-staff")}"><span class="nav-icon">◉</span>Staff</a>
                    <a href="/admin/branches" class="${navClass(activeNav, "admin-branches")}"><span class="nav-icon">⌂</span>Branches</a>
                    <a href="/admin/kyc-tiers" class="${navClass(activeNav, "admin-tiers")}"><span class="nav-icon">▤</span>KYC tier policy</a>
                    <a href="/admin/requirements" class="${navClass(activeNav, "admin-reqs")}"><span class="nav-icon">▥</span>Document requirements</a>
                    <a href="/admin/audit" class="${navClass(activeNav, "admin-audit")}"><span class="nav-icon">≡</span>Audit log</a>
                    <a href="/admin/integrations" class="${navClass(activeNav, "admin-integrations")}"><span class="nav-icon">⇄</span>Integrations</a>
                @endif
                <div class="nav-label">Account</div>
                <a href="/notifications" class="${navClass(activeNav, "notifications")}"><span class="nav-icon">◎</span>Notifications</a>
                <a href="/inbox" class="${navClass(activeNav, "inbox")}"><span class="nav-icon">✉</span>Demo inbox</a>
                <a href="/profile" class="${navClass(activeNav, "profile")}"><span class="nav-icon">●</span>Profile &amp; security</a>
            @endif
        </nav>
        <div class="sidebar-foot">
            Meridian Bank SG · demo environment<br>
            All provider and core systems are local mocks.
        </div>
    </aside>

    <div class="main">
        <header class="topbar">
            <div class="flex">
                <button class="icon-button menu-toggle" id="menu-toggle" aria-label="Open navigation">☰</button>
                <div>
                    <div class="crumb">Meridian Bank · Digital Onboarding</div>
                    <div class="page-title">${pageTitle}</div>
                </div>
            </div>
            <div class="topbar-right">
                @if (user != null)
                    <a class="icon-button" href="/notifications" aria-label="Notifications">
                        ◎
                        @if (unread > 0)
                            <span class="dot">${unread}</span>
                        @endif
                    </a>
                    <a class="user-chip" href="/profile">
                        <span class="avatar">${firstInitial(user.displayName())}</span>
                        <span class="who">
                            <span class="name">${user.displayName()}</span>
                            <span class="role">${user.role().replace("_", " ")}</span>
                        </span>
                    </a>
                    <form method="post" action="/logout">
                        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                        <button class="btn btn-ghost btn-sm" type="submit">Sign out</button>
                    </form>
                @endif
            </div>
        </header>
        <main class="content">
            ${content}
        </main>
    </div>
</div>
<script src="/js/app.js"></script>
</body>
</html>
