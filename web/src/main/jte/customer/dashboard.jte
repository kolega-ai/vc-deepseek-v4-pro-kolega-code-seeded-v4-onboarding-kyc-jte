@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode view
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Customer dashboard", activeNav = "customer", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Welcome, ${view.path("profile").path("fullName").asText()}</h1>
@template.partials.flash(flash = flash)

@if (view.path("rekycCase") != null && !view.path("rekycCase").isMissingNode())
    @if (view.path("rekycCase").path("status").asText().equals("overdue"))
        <div class="notice-strip danger">⚠ Your re-KYC is <b>overdue</b>. Your account may be limited until you complete the document refresh.</div>
    @else
        <div class="notice-strip warn">↻ Re-KYC required: fresh documents due by ${fmtDate(view.path("rekycCase").path("dueDate").asText())}.</div>
    @endif
    <div class="mb-2"><a class="btn btn-primary" href="/customer/rekyc">Start / continue re-KYC</a></div>
@endif

@if (view.path("account") != null && !view.path("account").isMissingNode())
    <div class="grid cols-2">
        <div class="card">
            <div class="card-head"><h2 class="mb-0">Your account</h2>
                <span class="badge ${tierClass(view.path("account").path("kycTier").asText())}"><span class="dot"></span>${tierLabel(view.path("account").path("kycTier").asText())}</span>
            </div>
            <dl class="kv">
                <dt>Account number</dt><dd class="mono">${view.path("account").path("accountNumber").asText()}</dd>
                <dt>Product</dt><dd>${view.path("account").path("product").asText()}</dd>
                <dt>Opened</dt><dd>${fmtDate(view.path("account").path("openedAt").asText())}</dd>
                <dt>Status</dt>
                @if (view.path("account").path("suspended").asBoolean())
                    <dd><span class="badge badge-warning"><span class="dot"></span>suspended — pending re-KYC</span></dd>
                @else
                    <dd><span class="badge badge-success"><span class="dot"></span>active</span></dd>
                @endif
            </dl>
        </div>
        <div class="card">
            <div class="card-head"><h2 class="mb-0">Your limits</h2><span class="small muted">${view.path("account").path("limits").path("currency").asText()}</span></div>
            @if (view.path("account").path("suspended").asBoolean())
                <p class="muted">Your tier limits are suspended until your re-KYC is completed.</p>
                <div class="grid cols-2">
                    <div class="stat danger"><span class="label">Daily transaction limit</span><span class="value">S$0.00</span></div>
                    <div class="stat danger"><span class="label">Balance ceiling</span><span class="value">S$0.00</span></div>
                </div>
            @else
                <div class="grid cols-2">
                    <div class="stat primary"><span class="label">Daily transaction limit</span><span class="value">${money(view.path("account").path("limits").path("dailyTransactionLimit").asText())}</span></div>
                    <div class="stat accent"><span class="label">Balance ceiling</span><span class="value">${money(view.path("account").path("limits").path("balanceCeiling").asText())}</span></div>
                </div>
            @endif
            <p class="small muted mt-2">Limits follow your KYC tier policy and are refreshed automatically on re-KYC.</p>
        </div>
    </div>
@endif

<div class="card">
    <div class="card-head"><h2 class="mb-0">Your documents</h2><a class="btn btn-ghost btn-sm" href="/journey/status">Application status</a></div>
    @if (view.path("documents").size() == 0)
        <div class="empty"><span class="glyph">▤</span><h3>No documents</h3></div>
    @else
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Document</th><th>Type</th><th>State</th><th>Expires</th><th></th></tr></thead>
                <tbody>
                    @for (var doc : view.path("documents"))
                        <tr>
                            <td class="cell-strong">${doc.path("filename").asText()}</td>
                            <td>${docTypeLabel(doc.path("type").asText())}</td>
                            <td><span class="badge ${docStateClass(doc.path("state").asText())}"><span class="dot"></span>${doc.path("state").asText()}</span></td>
                            <td class="cell-muted">${fmtDate(doc.path("expiryDate").asText())}</td>
                            <td class="actions"><a class="btn btn-ghost btn-sm" href="/documents/${doc.path("id").asText()}/download">Download</a></td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        </div>
    @endif
</div>

<div class="card">
    <div class="card-head"><h2 class="mb-0">Profile</h2><a class="btn btn-ghost btn-sm" href="/profile">Edit password</a></div>
    <dl class="kv">
        <dt>Email</dt><dd>${view.path("profile").path("email").asText()}</dd>
        <dt>Mobile</dt><dd>${view.path("profile").path("mobile").asText()}</dd>
        <dt>National identifier</dt><dd>${view.path("profile").path("nationalIdentifier").asText()}</dd>
        <dt>Address</dt><dd>${view.path("profile").path("addressLine1").asText()}, ${view.path("profile").path("city").asText()} ${view.path("profile").path("postalCode").asText()}</dd>
        <dt>Employment</dt><dd>${view.path("profile").path("employmentStatus").asText()}${view.path("profile").path("employerName") != null && !view.path("profile").path("employerName").asText().isEmpty() ? " · " + view.path("profile").path("employerName").asText() : ""}</dd>
        <dt>Declared annual income</dt><dd>${money(view.path("profile").path("declaredAnnualIncome").asText())}</dd>
    </dl>
</div>
`)
