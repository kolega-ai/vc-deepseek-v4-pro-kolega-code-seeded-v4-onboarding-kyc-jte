@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode dashboard
@param JsonNode queue
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Officer dashboard", activeNav = "officer", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>${dashboard.path("branch").asText()} branch</h1>
@template.partials.flash(flash = flash)
<div class="grid cols-4">
    <div class="stat primary"><span class="label">Under review</span><span class="value">${dashboard.path("underReview").asText()}</span><span class="hint">your branch</span></div>
    <div class="stat accent"><span class="label">Documents to verify</span><span class="value">${dashboard.path("awaitingVerification").asText()}</span><span class="hint">uploaded state</span></div>
    <div class="stat info"><span class="label">Documents requested</span><span class="value">${dashboard.path("documentsRequested").asText()}</span><span class="hint">returned to applicants</span></div>
    <div class="stat"><span class="label">Approved / declined</span><span class="value">${dashboard.path("approved").asText()} / ${dashboard.path("declined").asText()}</span><span class="hint">approval authority: ${dashboard.path("maxApprovalTier").asText()}</span></div>
</div>

<div class="grid main-2">
    <div class="card">
        <div class="card-head">
            <h2 class="mb-0">Review queue</h2>
            <a class="btn btn-ghost btn-sm" href="/officer/applications?status=under_review">Open full list</a>
        </div>
        @if (queue.path("items").size() == 0)
            <div class="empty"><span class="glyph">▦</span><h3>Queue is clear</h3>Nothing waiting for review in your branch.</div>
        @else
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Reference</th><th>Applicant</th><th>Tier</th><th>Submitted</th></tr></thead>
                    <tbody>
                        @for (var app : queue.path("items"))
                            <tr>
                                <td><a class="cell-strong" href="/officer/applications/${app.path("id").asText()}">${app.path("reference").asText()}</a></td>
                                <td>${app.path("applicantName").asText()}</td>
                                <td><span class="badge ${tierClass(app.path("kycTier").asText())}"><span class="dot"></span>${app.path("kycTier").asText()}</span></td>
                                <td class="cell-muted">${fmtDateTime(app.path("submittedAt").asText())}</td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif
    </div>
    <div class="card">
        <div class="card-head"><h2 class="mb-0">Actions available</h2></div>
        <ul class="small" style="padding-left:18px;color:var(--muted);line-height:2.1;">
            <li><a href="/officer/applications?status=under_review">Review applications</a> — approve, decline or request documents.</li>
            <li><a href="/officer/applications?status=documents_requested">Returned applications</a> — customers re-uploading documents.</li>
            <li><a href="/officer/applications?status=in_progress">Document verification</a> — verify uploaded documents in your branch.</li>
            <li><a href="/officer/export.csv">Export branch applications (CSV)</a>.</li>
        </ul>
        <div class="notice-strip info">Approve and decline actions require a step-up OTP. You can approve up to tier <b>${dashboard.path("maxApprovalTier").asText()}</b>.</div>
    </div>
</div>
`)
