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
@template.layout.main(pageTitle = "Identity verification", activeNav = "journey", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Identity verification</h1>
<p class="muted">We verify your identity against our external identity provider. A match score of <b>80 or above</b> is required.</p>
@template.partials.flash(flash = flash)

<div class="grid main-2">
    <div class="card">
        <h3>Run verification</h3>
        <p class="muted small">All required documents must be uploaded first. The provider returns an XML response with a match score.</p>
        <form method="post" action="/journey/identity/verify">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <button class="btn btn-primary" type="submit">Run identity verification</button>
        </form>
    </div>
    <div class="card">
        <h3>Latest result</h3>
        @if (view.path("identityResults").size() == 0)
            <p class="muted small">No verification has been run yet.</p>
        @else
            @for (var result : view.path("identityResults"))
                @if (result.path("status").asText().equals("SUCCESS"))
                    <div class="flex-between mb-1">
                        @if (result.path("matchScore").asInt() >= 80)
                            <span class="badge badge-success"><span class="dot"></span>score ${result.path("matchScore").asText()}</span>
                        @else
                            <span class="badge badge-danger"><span class="dot"></span>score ${result.path("matchScore").asText()}</span>
                        @endif
                        <span class="small faint">${fmtDateTime(result.path("createdAt").asText())}</span>
                    </div>
                    <p class="small muted">Outcome: ${result.path("outcome").asText()} · provider ref <span class="mono">${result.path("providerReference").asText()}</span></p>
                @else
                    <div class="flex-between mb-1">
                        <span class="badge badge-warning"><span class="dot"></span>provider error</span>
                        <span class="small faint">${fmtDateTime(result.path("createdAt").asText())}</span>
                    </div>
                    <p class="small muted">${result.path("errorMessage").asText()}</p>
                @endif
            @endfor
            @if (view.path("identityPassed").asBoolean())
                <div class="alert alert-success mt-2"><span class="icon">✓</span><span>Identity verification passed.</span></div>
            @else
                <div class="alert alert-error"><span class="icon">!</span><span>Score below 80 — the application cannot progress until verification passes.</span></div>
            @endif
        @endif
    </div>
</div>
`)
