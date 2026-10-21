@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode tiers
@param Map<String, Object> flash
@template.layout.main(pageTitle = "KYC tier policy", activeNav = "admin-tiers", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>KYC tier policy</h1>
<p class="muted">Seeded reference policy in SGD. Limits are read server-side from this table at account opening and re-KYC — never accepted from the client. All changes are audited.</p>
@template.partials.flash(flash = flash)

<div class="grid cols-2">
    @for (var tier : tiers.path("items"))
        <div class="card">
            <div class="card-head">
                <h2 class="mb-0">${tierLabel(tier.path("tierCode").asText())} <span class="muted small mono">${tier.path("tierCode").asText()}</span></h2>
                <span class="small muted">${tier.path("currency").asText()}</span>
            </div>
            <dl class="kv">
                <dt>Minimum identity match</dt><dd>${tier.path("minIdentityMatch").asText()}</dd>
                <dt>Daily transaction limit</dt><dd class="cell-strong">${money(tier.path("dailyTransactionLimit").asText())}</dd>
                <dt>Balance ceiling</dt><dd class="cell-strong">${money(tier.path("balanceCeiling").asText())}</dd>
            </dl>
            <details class="panel mt-2">
                <summary class="small">Edit limits</summary>
                <div class="panel-body">
                    <form method="post" action="/admin/kyc-tiers/${tier.path("tierCode").asText()}">
                        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                        <div class="field">
                            <label>Daily transaction limit</label>
                            <input class="input" type="number" name="dailyTransactionLimit" min="0" step="0.01" required value="${tier.path("dailyTransactionLimit").asText()}">
                        </div>
                        <div class="field">
                            <label>Balance ceiling</label>
                            <input class="input" type="number" name="balanceCeiling" min="0" step="0.01" required value="${tier.path("balanceCeiling").asText()}">
                        </div>
                        <button class="btn btn-primary btn-sm" type="submit">Save limits</button>
                    </form>
                </div>
            </details>
        </div>
    @endfor
</div>
`)
