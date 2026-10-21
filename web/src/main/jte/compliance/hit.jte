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
@template.layout.main(pageTitle = "Watchlist hit", activeNav = "compliance-hits", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<div class="flex-between mb-2">
    <h1 class="mb-0">Watchlist hit</h1>
    <span class="badge ${dispositionClass(view.path("disposition").asText())}"><span class="dot"></span>${dispositionLabel(view.path("disposition").asText())}</span>
</div>
@template.partials.flash(flash = flash)

<div class="grid main-2">
    <div class="card">
        <h2>Match details</h2>
        <dl class="kv">
            <dt>Matched name</dt><dd class="cell-strong">${view.path("matchedName").asText()}</dd>
            <dt>List source</dt><dd>${view.path("listSource").asText()}</dd>
            <dt>Confidence</dt><dd>${view.path("confidence").asText()}</dd>
            <dt>Created</dt><dd>${fmtDateTime(view.path("createdAt").asText())}</dd>
            @if (view.path("justification") != null && !view.path("justification").asText().isEmpty())
                <dt>Justification</dt><dd>${view.path("justification").asText()}</dd>
            @endif
        </dl>
    </div>
    <div class="card">
        <h2>Disposition</h2>
        @if (view.path("disposition").asText().equals("pending") || view.path("disposition").asText().equals("escalated"))
            <form method="post" action="/compliance/hits/${view.path("id").asText()}/disposition">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <div class="field">
                    <label for="disposition">Disposition</label>
                    <select class="input" id="disposition" name="disposition" required>
                        <option value="false_positive">False positive — namesake match</option>
                        <option value="confirmed">Confirmed — corroborated match</option>
                        <option value="escalated">Escalate for senior review</option>
                    </select>
                </div>
                <div class="field">
                    <label for="justification">Justification</label>
                    <textarea class="input" id="justification" name="justification" minlength="20" placeholder="Required for a confirmed disposition (at least 20 characters)…"></textarea>
                </div>
                <button class="btn btn-primary" type="submit">Record disposition</button>
            </form>
        @else
            <p class="muted small">This hit was already dispositioned ${fmtDateTime(view.path("disposedAt").asText())}.</p>
        @endif
    </div>
</div>

@if (view.path("application") != null && !view.path("application").isMissingNode())
    <div class="card">
        <div class="card-head">
            <h2 class="mb-0">Application ${view.path("application").path("reference").asText()}</h2>
            <span class="badge ${statusClass(view.path("application").path("status").asText())}"><span class="dot"></span>${statusLabel(view.path("application").path("status").asText())}</span>
        </div>
        <dl class="kv">
            <dt>Applicant</dt><dd>${view.path("application").path("applicant").path("fullName").asText()} · ${view.path("application").path("applicant").path("email").asText()}</dd>
            <dt>National identifier</dt><dd class="mono">${view.path("application").path("applicant").path("nationalIdentifier").asText()}</dd>
            <dt>KYC tier</dt><dd><span class="badge ${tierClass(view.path("application").path("effectiveKycTier").asText())}"><span class="dot"></span>${view.path("application").path("effectiveKycTier").asText()}</span></dd>
        </dl>

        @if (view.path("otherHits").size() > 0)
            <div class="section-title">Other hits on this application</div>
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Matched name</th><th>Source</th><th>Confidence</th><th>Disposition</th></tr></thead>
                    <tbody>
                        @for (var other : view.path("otherHits"))
                            <tr>
                                <td>${other.path("matchedName").asText()}</td>
                                <td>${other.path("listSource").asText()}</td>
                                <td>${other.path("confidence").asText()}</td>
                                <td><span class="badge ${dispositionClass(other.path("disposition").asText())}"><span class="dot"></span>${other.path("disposition").asText()}</span></td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif

        @if (view.path("application").path("status").asText().equals("watchlist_hold"))
            <div class="section-title">Resolve the hold</div>
            <p class="small muted">Release when every hit is a false positive; the application then returns to officer review. A confirmed hit declines the application.</p>
            <form method="post" action="/compliance/applications/${view.path("application").path("id").asText()}/resolve-hold">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <div class="form-row">
                    <div class="field">
                        <label>Customer-visible reason (if declining)</label>
                        <textarea class="input" name="customerVisibleReason"></textarea>
                    </div>
                    <div class="field">
                        <label>Internal reason (if declining)</label>
                        <textarea class="input" name="internalReason"></textarea>
                    </div>
                </div>
                <button class="btn btn-primary" type="submit">Resolve hold</button>
            </form>
        @endif

        <div class="section-title">KYC tier override</div>
        <p class="small muted">Overrides need a step-up OTP and a mandatory justification. Limits follow the tier policy.</p>
        <form method="post" action="/compliance/applications/${view.path("application").path("id").asText()}/kyc-tier/otp" class="mb-1">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <input type="hidden" name="returnTo" value="/compliance/hits/${view.path("id").asText()}">
            <button class="btn btn-ghost btn-sm" type="submit">1 · Send override code</button>
        </form>
        <form method="post" action="/compliance/applications/${view.path("application").path("id").asText()}/kyc-tier">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <input type="hidden" name="returnTo" value="/compliance/hits/${view.path("id").asText()}">
            <div class="form-row">
                <div class="field">
                    <label>New tier</label>
                    <select class="input" name="tier">
                        <option value="tier_0">tier_0</option>
                        <option value="tier_1">tier_1</option>
                        <option value="tier_2">tier_2</option>
                        <option value="tier_3">tier_3</option>
                    </select>
                </div>
                <div class="field">
                    <label>Step-up code</label>
                    <input class="input" type="text" name="otpCode" required inputmode="numeric" maxlength="6" placeholder="2 · 6-digit code" data-otp>
                </div>
            </div>
            <div class="field">
                <label>Justification (at least 20 characters)</label>
                <textarea class="input" name="justification" required minlength="20"></textarea>
            </div>
            <button class="btn btn-accent" type="submit">3 · Override tier</button>
        </form>
    </div>
@endif
`)
