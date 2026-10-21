@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode campaign
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Re-KYC campaign", activeNav = "compliance-rekyc", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<div class="flex-between mb-2">
    <div>
        <h1 class="mb-0">${campaign.path("name").asText()}</h1>
        <span class="muted small">${campaign.path("targetSegment").asText()} · due ${campaign.path("dueDays").asText()} days after creation</span>
    </div>
    <a class="btn btn-ghost" href="/compliance/rekyc">Back to campaigns</a>
</div>
@template.partials.flash(flash = flash)

<div class="card">
    <h3>Import customer list (CSV)</h3>
    <p class="muted small">One identifier per row: an approved customer's email or national identifier. Header <span class="mono">identifier</span>. Rows with errors are reported.</p>
    <form method="post" action="/compliance/rekyc/campaigns/${campaign.path("id").asText()}/import" enctype="multipart/form-data">
        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
        <div class="dropzone" style="max-width:520px;">
            <span class="big">⇪</span>
            <input type="file" name="file" accept=".csv,text/csv" required data-label="fileLabel">
            <div id="fileLabel">Choose a CSV file…</div>
        </div>
        <button class="btn btn-primary mt-2" type="submit">Import cases</button>
    </form>
</div>

@if (campaign.path("cases").size() == 0)
    <div class="empty"><span class="glyph">↻</span><h3>No cases yet</h3>Import a customer list to create due cases.</div>
@else
    <div class="table-wrap">
        <table class="table">
            <thead><tr><th>Customer</th><th>Email</th><th>Application</th><th>Status</th><th>Due date</th><th>Origin</th></tr></thead>
            <tbody>
                @for (var kase : campaign.path("cases"))
                    <tr>
                        <td class="cell-strong">${kase.path("customerName").asText()}</td>
                        <td>${kase.path("email").asText()}</td>
                        <td>${kase.path("applicationReference").asText()}</td>
                        <td><span class="badge ${kase.path("status").asText().equals("overdue") ? "badge-danger" : kase.path("status").asText().equals("completed") ? "badge-success" : kase.path("status").asText().equals("in_progress") ? "badge-primary" : "badge-neutral"}"><span class="dot"></span>${kase.path("status").asText()}</span></td>
                        <td class="cell-muted">${fmtDate(kase.path("dueDate").asText())}</td>
                        <td class="cell-muted">${kase.path("origin").asText()}</td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
@endif
`)
