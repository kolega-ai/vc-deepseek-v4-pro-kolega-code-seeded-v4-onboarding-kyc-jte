@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@import static com.meridianbank.web.controller.JourneyController.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode view
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Application status", activeNav = "status", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Application status</h1>
@template.partials.flash(flash = flash)

<div class="flex-between mb-2">
    <div class="flex">
        <span class="badge ${statusClass(view.path("status").asText())}"><span class="dot"></span>${statusLabel(view.path("status").asText())}</span>
        @if (view.path("decisionOutcome") != null && !view.path("decisionOutcome").isMissingNode())
            @if (view.path("decisionOutcome").asText().equals("approved"))
                <span class="badge badge-success"><span class="dot"></span>${view.path("decisionOutcome").asText()}</span>
            @else
                <span class="badge badge-danger"><span class="dot"></span>${view.path("decisionOutcome").asText()}</span>
            @endif
        @endif
        <span class="small muted">Reference <span class="mono">${view.path("reference").asText()}</span></span>
    </div>
    <a class="btn btn-ghost btn-sm" href="/customer/export">Download my data (JSON)</a>
</div>

@if (view.path("status").asText().equals("submitted"))
    <div class="notice-strip info">⏳ Screening is in progress. Your application is checked against watchlists and the credit bureau automatically — this usually completes within a minute. Refresh this page.</div>
@endif

@if (view.path("customerVisibleReason") != null && !view.path("customerVisibleReason").isMissingNode() && !view.path("customerVisibleReason").asText().isEmpty())
    <div class="card">
        <h3>What this decision means for you</h3>
        <p class="mb-0">${view.path("customerVisibleReason").asText()}</p>
    </div>
@endif

@if (view.path("account") != null && !view.path("account").isMissingNode())
    <div class="card">
        <h3>Your account</h3>
        <dl class="kv">
            <dt>Account number</dt><dd class="mono">${view.path("account").path("accountNumber").asText()}</dd>
            <dt>Product</dt><dd>${view.path("account").path("product").asText()}</dd>
            <dt>KYC tier</dt><dd><span class="badge ${tierClass(view.path("account").path("kycTier").asText())}"><span class="dot"></span>${tierLabel(view.path("account").path("kycTier").asText())} (${view.path("account").path("kycTier").asText()})</span></dd>
            <dt>Account status</dt>
            @if (view.path("account").path("status").asText().equals("active"))
                <dd><span class="badge badge-success"><span class="dot"></span>${view.path("account").path("status").asText()}</span></dd>
            @else
                <dd><span class="badge badge-warning"><span class="dot"></span>${view.path("account").path("status").asText()}</span></dd>
            @endif
        </dl>
        <a class="btn btn-primary mt-2" href="/customer/dashboard">Go to your dashboard</a>
    </div>
@endif

@if (view.path("status").asText().equals("in_progress") || view.path("status").asText().equals("documents_requested"))
    <div class="card">
        <h3>Journey progress</h3>
        <ol class="stepper">
            @for (var step : view.path("steps"))
                @if (step.path("completed").asBoolean())
                    <li><span class="step done">✓ ${step.path("key").asText().replace("_", " ")}</span></li>
                @else
                    <li><span class="step">${step.path("key").asText().replace("_", " ")}</span></li>
                @endif
            @endfor
        </ol>
        @if (view.path("status").asText().equals("documents_requested"))
            <div class="notice-strip warn">⚠ Documents were requested. Re-upload them and they will be re-verified; identity verification must be run again before re-submission.</div>
            <a class="btn btn-primary" href="/journey/documents">Upload requested documents</a>
        @else
            <a class="btn btn-primary" href="/journey">Continue your application</a>
        @endif
    </div>
@endif

@if (view.path("status").asText().equals("in_progress"))
    @if (!blockingReasons(view).isEmpty())
        <div class="card">
            <h3>Before you can submit</h3>
            <ul class="small" style="padding-left:18px;color:var(--muted);line-height:1.9;">
                @for (var reason : blockingReasons(view))
                    <li>${reason}</li>
                @endfor
            </ul>
            <form method="post" action="/journey/submit">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <button class="btn btn-accent" type="submit">Submit application</button>
            </form>
        </div>
    @else
        <div class="card">
            <h3>Ready to submit</h3>
            <p class="muted">All steps are complete and your documents are verified.</p>
            <form method="post" action="/journey/submit">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <button class="btn btn-accent btn-lg" type="submit">Submit application</button>
            </form>
        </div>
    @endif
@endif
`)
