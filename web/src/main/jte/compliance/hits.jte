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
@template.layout.main(pageTitle = "Watchlist queue", activeNav = "compliance-hits", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<div class="flex-between mb-2">
    <h1 class="mb-0">Watchlist queue</h1>
    <a class="btn btn-ghost" href="/compliance/export/hits.csv">Export hits (CSV)</a>
</div>
@template.partials.flash(flash = flash)

<div class="card">
    <form method="get" action="/compliance/hits" class="filters">
        <select class="input" name="disposition">
            <option value="">All dispositions</option>
            <option value="pending">Pending</option>
            <option value="escalated">Escalated</option>
            <option value="confirmed">Confirmed</option>
            <option value="false_positive">False positive</option>
        </select>
        <input class="input" type="text" name="q" placeholder="Search name, reference, applicant…" value="${String.valueOf(filters.get("q"))}">
        <button class="btn btn-primary" type="submit">Filter</button>
    </form>
</div>

@if (result.path("items").size() == 0)
    <div class="empty"><span class="glyph">⚠</span><h3>No hits</h3>No watchlist hits match these filters.</div>
@else
    <div class="table-wrap">
        <table class="table">
            <thead><tr><th>Application</th><th>Applicant</th><th>Matched name</th><th>List source</th><th>Confidence</th><th>Disposition</th><th></th></tr></thead>
            <tbody>
                @for (var hit : result.path("items"))
                    <tr>
                        <td><a class="cell-strong" href="/compliance/hits/${hit.path("id").asText()}">${hit.path("applicationReference").asText()}</a></td>
                        <td>${hit.path("applicantName").asText()}</td>
                        <td>${hit.path("matchedName").asText()}</td>
                        <td>${hit.path("listSource").asText()}</td>
                        <td>${hit.path("confidence").asText()}</td>
                        <td><span class="badge ${dispositionClass(hit.path("disposition").asText())}"><span class="dot"></span>${dispositionLabel(hit.path("disposition").asText())}</span></td>
                        <td class="actions"><a class="btn btn-primary btn-sm" href="/compliance/hits/${hit.path("id").asText()}">Review</a></td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
@endif
`)
