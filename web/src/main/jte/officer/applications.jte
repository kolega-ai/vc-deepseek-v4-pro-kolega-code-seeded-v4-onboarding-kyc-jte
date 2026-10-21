@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode result
@param Map<String, Object> filters
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Applications", activeNav = "officer-apps", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Applications</h1>
<p class="muted">Branch-scoped view: only applications from your branch are shown and downloadable.</p>
@template.partials.flash(flash = flash)

<div class="card">
    <form method="get" action="/officer/applications" class="filters">
        <select class="input" name="status">
            <option value="">All statuses</option>
            <option value="in_progress">In progress</option>
            <option value="submitted">Submitted</option>
            <option value="under_review">Under review</option>
            <option value="documents_requested">Documents requested</option>
            <option value="watchlist_hold">Watchlist hold</option>
            <option value="approved">Approved</option>
            <option value="declined">Declined</option>
            <option value="abandoned">Abandoned</option>
        </select>
        <select class="input" name="kycTier">
            <option value="">All tiers</option>
            <option value="tier_0">tier_0</option>
            <option value="tier_1">tier_1</option>
            <option value="tier_2">tier_2</option>
            <option value="tier_3">tier_3</option>
        </select>
        <input class="input" type="text" name="q" placeholder="Search reference, name, email…" value="${String.valueOf(filters.get("q"))}">
        <input class="input" type="date" name="from" value="${String.valueOf(filters.get("from"))}">
        <input class="input" type="date" name="to" value="${String.valueOf(filters.get("to"))}">
        <select class="input" name="sort">
            <option value="created">Newest first</option>
            <option value="reference">Reference</option>
            <option value="applicant">Applicant</option>
            <option value="tier">KYC tier</option>
            <option value="status">Status</option>
        </select>
        <button class="btn btn-primary" type="submit">Filter</button>
        <a class="btn btn-ghost" href="/officer/export.csv?status=${String.valueOf(filters.get("status"))}">Export CSV</a>
    </form>
</div>

@if (result.path("items").size() == 0)
    <div class="empty"><span class="glyph">▦</span><h3>No applications</h3>No applications match these filters in your branch.</div>
@else
    <div class="table-wrap">
        <table class="table">
            <thead><tr><th>Reference</th><th>Applicant</th><th>Status</th><th>KYC tier</th><th>Created</th><th>Submitted</th><th></th></tr></thead>
            <tbody>
                @for (var app : result.path("items"))
                    <tr>
                        <td><a class="cell-strong" href="/officer/applications/${app.path("id").asText()}">${app.path("reference").asText()}</a></td>
                        <td>${app.path("applicantName").asText()}<br><span class="cell-muted">${app.path("applicantEmail").asText()}</span></td>
                        <td><span class="badge ${statusClass(app.path("status").asText())}"><span class="dot"></span>${statusLabel(app.path("status").asText())}</span></td>
                        <td><span class="badge ${tierClass(app.path("kycTier").asText())}"><span class="dot"></span>${app.path("kycTier").asText()}</span></td>
                        <td class="cell-muted">${fmtDate(app.path("createdAt").asText())}</td>
                        <td class="cell-muted">${fmtDateTime(app.path("submittedAt").asText())}</td>
                        <td class="actions"><a class="btn btn-primary btn-sm" href="/officer/applications/${app.path("id").asText()}">Review</a></td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
    <div class="pagination">
        <span class="meta">${result.path("total").asText()} applications · page ${result.path("page").asInt() + 1} of ${result.path("pages").asInt()}</span>
        @if (result.path("page").asInt() > 0)
            <a href="?page=${result.path("page").asInt() - 1}&status=${String.valueOf(filters.get("status"))}&q=${String.valueOf(filters.get("q"))}&sort=${String.valueOf(filters.get("sort"))}">← Previous</a>
        @endif
        @if (result.path("page").asInt() + 1 < result.path("pages").asInt())
            <a href="?page=${result.path("page").asInt() + 1}&status=${String.valueOf(filters.get("status"))}&q=${String.valueOf(filters.get("q"))}&sort=${String.valueOf(filters.get("sort"))}">Next →</a>
        @endif
    </div>
@endif
`)
