@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode requirements
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Document requirements", activeNav = "admin-reqs", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Document requirements</h1>
<p class="muted">Required documents per KYC tier. Conditions add a proof-of-income requirement for self-employed applicants or income of S$100,000+.</p>
@template.partials.flash(flash = flash)

<div class="card">
    <h3>Add a requirement</h3>
    <form method="post" action="/admin/requirements">
        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
        <div class="form-row">
            <div class="field">
                <label>KYC tier</label>
                <select class="input" name="kycTier" required>
                    <option value="tier_1">tier_1</option>
                    <option value="tier_2">tier_2</option>
                    <option value="tier_3">tier_3</option>
                </select>
            </div>
            <div class="field">
                <label>Document type</label>
                <select class="input" name="docType" required>
                    <option value="IDENTITY_DOC">Identity document (NRIC or passport)</option>
                    <option value="ADDRESS_PROOF">Address proof</option>
                    <option value="PROOF_OF_INCOME">Proof of income</option>
                </select>
            </div>
            <div class="field">
                <label>Condition (optional)</label>
                <select class="input" name="conditionType">
                    <option value="">Unconditional</option>
                    <option value="SELF_EMPLOYED">Self-employed only</option>
                    <option value="INCOME_GE_100K">Income ≥ S$100,000</option>
                </select>
            </div>
        </div>
        <button class="btn btn-primary" type="submit">Add requirement</button>
    </form>
</div>

<div class="table-wrap">
    <table class="table">
        <thead><tr><th>KYC tier</th><th>Document type</th><th>Condition</th><th></th></tr></thead>
        <tbody>
            @for (var req : requirements.path("items"))
                <tr>
                    <td class="cell-strong mono">${req.path("kycTier").asText()}</td>
                    <td>${docTypeLabel(req.path("docType").asText())}</td>
                    <td>
                        @if (req.path("conditionType").asText().isEmpty())
                            <span class="badge badge-neutral"><span class="dot"></span>unconditional</span>
                        @else
                            <span class="badge badge-accent"><span class="dot"></span>${req.path("conditionType").asText()}</span>
                        @endif
                    </td>
                    <td class="actions">
                        <form method="post" action="/admin/requirements/${req.path("id").asText()}/delete" data-confirm="Delete this requirement?">
                            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                            <button class="btn btn-ghost-danger btn-sm" type="submit">Delete</button>
                        </form>
                    </td>
                </tr>
            @endfor
        </tbody>
    </table>
</div>
`)
