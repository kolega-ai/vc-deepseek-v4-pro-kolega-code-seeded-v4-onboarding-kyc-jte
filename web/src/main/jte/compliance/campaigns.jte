@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode campaigns
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Re-KYC campaigns", activeNav = "compliance-rekyc", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Re-KYC campaigns</h1>
<p class="muted">Create periodic document-refresh campaigns for existing customers, then import customer lists (CSV: one email or national identifier per row).</p>
@template.partials.flash(flash = flash)

<div class="card">
    <h3>Create a campaign</h3>
    <form method="post" action="/compliance/rekyc/campaigns">
        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
        <div class="form-row">
            <div class="field">
                <label for="name">Campaign name</label>
                <input class="input" type="text" id="name" name="name" required maxlength="160" placeholder="H2 2026 Periodic Refresh">
            </div>
            <div class="field">
                <label for="targetSegment">Target segment</label>
                <input class="input" type="text" id="targetSegment" name="targetSegment" placeholder="tier_2,tier_3 customers">
            </div>
        </div>
        <div class="form-row">
            <div class="field">
                <label for="targetKycTier">Target tier (optional)</label>
                <select class="input" id="targetKycTier" name="targetKycTier">
                    <option value="">Use each customer's current tier</option>
                    <option value="tier_1">tier_1</option>
                    <option value="tier_2">tier_2</option>
                    <option value="tier_3">tier_3</option>
                </select>
            </div>
            <div class="field">
                <label for="dueDays">Due period (days)</label>
                <input class="input" type="number" id="dueDays" name="dueDays" required min="7" max="365" value="90">
            </div>
        </div>
        <button class="btn btn-primary" type="submit">Create campaign</button>
    </form>
</div>

@if (campaigns.path("items").size() == 0)
    <div class="empty"><span class="glyph">↻</span><h3>No campaigns yet</h3>Create your first re-KYC campaign above.</div>
@else
    <div class="table-wrap">
        <table class="table">
            <thead><tr><th>Name</th><th>Segment</th><th>Target tier</th><th>Due period</th><th>Cases</th><th>Open</th><th>Created</th><th></th></tr></thead>
            <tbody>
                @for (var campaign : campaigns.path("items"))
                    <tr>
                        <td><a class="cell-strong" href="/compliance/rekyc/campaigns/${campaign.path("id").asText()}">${campaign.path("name").asText()}</a></td>
                        <td>${campaign.path("targetSegment").asText()}</td>
                        <td>${campaign.path("targetKycTier").asText()}</td>
                        <td>${campaign.path("dueDays").asText()} days</td>
                        <td>${campaign.path("cases").asText()}</td>
                        <td>${campaign.path("openCases").asText()}</td>
                        <td class="cell-muted">${fmtDate(campaign.path("createdAt").asText())}</td>
                        <td class="actions"><a class="btn btn-primary btn-sm" href="/compliance/rekyc/campaigns/${campaign.path("id").asText()}">Open</a></td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
@endif
`)
