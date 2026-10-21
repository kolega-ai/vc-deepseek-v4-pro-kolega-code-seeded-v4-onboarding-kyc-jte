@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode dashboard
@param JsonNode staff
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Admin dashboard", activeNav = "admin", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Bank administration</h1>
@template.partials.flash(flash = flash)
<div class="grid cols-4">
    <div class="stat primary"><span class="label">Staff</span><span class="value">${dashboard.path("staffCount").asText()}</span></div>
    <div class="stat info"><span class="label">Branches</span><span class="value">${dashboard.path("branches").asText()}</span></div>
    <div class="stat accent"><span class="label">Audit events</span><span class="value">${dashboard.path("auditEvents").asText()}</span><span class="hint">append-only</span></div>
    <div class="stat"><span class="label">Tier policies / requirements</span><span class="value">${dashboard.path("kycTiers").asText()} / ${dashboard.path("requirements").asText()}</span></div>
</div>

<div class="grid main-2">
    <div class="card">
        <div class="card-head">
            <h2 class="mb-0">Staff</h2>
            <a class="btn btn-primary btn-sm" href="/admin/staff">Manage staff</a>
        </div>
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Name</th><th>Role</th><th>Branch</th><th>Max tier</th></tr></thead>
                <tbody>
                    @for (var s : staff.path("items"))
                        <tr>
                            <td class="cell-strong">${s.path("fullName").asText()}</td>
                            <td>${s.path("role").asText()}</td>
                            <td>${s.path("branchName").asText()}</td>
                            <td>${s.path("maxApprovalTier").asText()}</td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        </div>
    </div>
    <div class="card">
        <div class="card-head"><h2 class="mb-0">Configuration shortcuts</h2></div>
        <ul class="small" style="padding-left:18px;color:var(--muted);line-height:2.2;">
            <li><a href="/admin/staff">Staff management</a> — roles, branches, approval tiers, enable/disable, roster CSV import.</li>
            <li><a href="/admin/branches">Branches</a> — create branches or import reference XML.</li>
            <li><a href="/admin/kyc-tiers">KYC tier policy</a> — transaction limits and balance ceilings (SGD).</li>
            <li><a href="/admin/requirements">Document requirements</a> — required documents per tier and condition.</li>
            <li><a href="/admin/audit">Audit log</a> — read-only, append-only, exportable.</li>
            <li><a href="/admin/integrations">Integrations</a> — webhooks and provider credentials.</li>
        </ul>
        <div class="notice-strip info">Bank admins manage configuration and read audits — they cannot approve, decline, verify documents or disposition hits.</div>
    </div>
</div>
`)
