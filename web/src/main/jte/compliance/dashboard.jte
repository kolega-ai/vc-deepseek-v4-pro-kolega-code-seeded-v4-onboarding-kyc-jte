@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode dashboard
@param JsonNode hits
@param JsonNode escalated
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Compliance dashboard", activeNav = "compliance", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Compliance overview</h1>
<p class="muted">You act across all branches.</p>
@template.partials.flash(flash = flash)

<div class="grid cols-4">
    <div class="stat danger"><span class="label">Pending hits</span><span class="value">${dashboard.path("pendingHits").asText()}</span><span class="hint">awaiting disposition</span></div>
    <div class="stat accent"><span class="label">Escalated</span><span class="value">${dashboard.path("escalatedHits").asText()}</span><span class="hint">senior review</span></div>
    <div class="stat info"><span class="label">Applications on hold</span><span class="value">${dashboard.path("onHold").asText()}</span><span class="hint">watchlist holds</span></div>
    <div class="stat primary"><span class="label">Open re-KYC cases</span><span class="value">${dashboard.path("activeRekycCases").asText()}</span><span class="hint">across campaigns</span></div>
</div>

<div class="grid main-2">
    <div class="card">
        <div class="card-head">
            <h2 class="mb-0">Pending dispositions</h2>
            <a class="btn btn-ghost btn-sm" href="/compliance/hits">Open queue</a>
        </div>
        @if (hits.path("items").size() == 0)
            <div class="empty"><span class="glyph">⚠</span><h3>No pending hits</h3></div>
        @else
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Applicant</th><th>Matched name</th><th>Source</th><th>Confidence</th></tr></thead>
                    <tbody>
                        @for (var hit : hits.path("items"))
                            <tr>
                                <td><a class="cell-strong" href="/compliance/hits/${hit.path("id").asText()}">${hit.path("applicantName").asText()}</a></td>
                                <td>${hit.path("matchedName").asText()}</td>
                                <td>${hit.path("listSource").asText()}</td>
                                <td>${hit.path("confidence").asText()}</td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif
    </div>
    <div class="card">
        <div class="card-head">
            <h2 class="mb-0">Escalated</h2>
            <a class="btn btn-ghost btn-sm" href="/compliance/hits?disposition=escalated">Open queue</a>
        </div>
        @if (escalated.path("items").size() == 0)
            <div class="empty"><span class="glyph">✓</span><h3>Nothing escalated</h3></div>
        @else
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Applicant</th><th>Matched name</th><th>Source</th></tr></thead>
                    <tbody>
                        @for (var hit : escalated.path("items"))
                            <tr>
                                <td><a class="cell-strong" href="/compliance/hits/${hit.path("id").asText()}">${hit.path("applicantName").asText()}</a></td>
                                <td>${hit.path("matchedName").asText()}</td>
                                <td>${hit.path("listSource").asText()}</td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif
    </div>
</div>

<div class="card">
    <div class="card-head">
        <h2 class="mb-0">Re-KYC campaigns</h2>
        <a class="btn btn-primary btn-sm" href="/compliance/rekyc">Manage campaigns</a>
    </div>
    <p class="muted small">Create periodic document-refresh campaigns and import customer lists (CSV).</p>
</div>
`)
