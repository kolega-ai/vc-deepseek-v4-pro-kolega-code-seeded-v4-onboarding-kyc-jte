@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import java.util.List
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@import static com.meridianbank.web.controller.JourneyController.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode view
@param int journeyCssVersion
@template.layout.main(pageTitle = "Your application", activeNav = "journey", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<link rel="stylesheet" href="/css/app.css?v=${journeyCssVersion}">
<div class="flex-between mb-2">
    <div>
        <h1 class="mb-0">Your application</h1>
        <span class="muted small">Reference <span class="mono">${view.path("reference").asText()}</span> · Branch: ${view.path("branch").asText()}</span>
    </div>
    <span class="badge ${statusClass(view.path("status").asText())}"><span class="dot"></span>${statusLabel(view.path("status").asText())}</span>
</div>

<ol class="stepper">
    @for (var step : view.path("steps"))
        @if (step.path("completed").asBoolean())
            <li><span class="step done">✓ ${step.path("key").asText().replace("_", " ")}</span></li>
        @elseif (step.path("key").asText().equals(view.path("currentStep").asText()))
            <li><span class="step current">● ${step.path("key").asText().replace("_", " ")}</span></li>
        @else
            <li><span class="step locked">${step.path("key").asText().replace("_", " ")}</span></li>
        @endif
    @endfor
</ol>

@if (view.path("status").asText().equals("in_progress"))
    <div class="grid main-2">
        <div class="card">
            <h2>Next step</h2>
            @if (view.path("currentStep").asText().equals("contact_verified"))
                <p class="muted">Verify your mobile number before you continue. Both email and mobile verification are required before document upload.</p>
                <a class="btn btn-primary" href="/journey/contact">Verify mobile number</a>
            @elseif (view.path("currentStep").asText().equals("personal_details"))
                <p class="muted">Tell us who you are.</p>
                <a class="btn btn-primary" href="/journey/personal">Personal details</a>
            @elseif (view.path("currentStep").asText().equals("address_details"))
                <p class="muted">Where you live.</p>
                <a class="btn btn-primary" href="/journey/address">Address details</a>
            @elseif (view.path("currentStep").asText().equals("employment_income"))
                <p class="muted">Your employment and declared annual income.</p>
                <a class="btn btn-primary" href="/journey/employment">Employment &amp; income</a>
            @elseif (view.path("currentStep").asText().equals("documents_uploaded"))
                <p class="muted">Upload the required documents, then run identity verification.</p>
                <a class="btn btn-primary" href="/journey/documents">Documents</a>
            @elseif (view.path("currentStep").asText().equals("identity_verified"))
                <p class="muted">Run identity verification against our identity provider.</p>
                <a class="btn btn-primary" href="/journey/identity">Identity verification</a>
            @else
                <p class="muted">Continue your journey.</p>
                <a class="btn btn-primary" href="/journey/status">View status</a>
            @endif
        </div>
        <div class="card">
            <h2>Submission checklist</h2>
            @if (blockingReasons(view).isEmpty())
                <div class="alert alert-success"><span class="icon">✓</span><span>Everything is in place. When your documents are verified by an officer, you can submit.</span></div>
            @else
                <ul class="small" style="padding-left:18px;color:var(--muted);line-height:1.9;">
                    @for (var reason : blockingReasons(view))
                        <li>${reason}</li>
                    @endfor
                </ul>
            @endif
        </div>
    </div>
@elseif (view.path("status").asText().equals("submitted"))
    <div class="notice-strip info">⏳ Your application was submitted and is queued for screening. This completes automatically within a minute — refresh this page to see the result.</div>
    <a class="btn btn-ghost" href="/journey/status">Go to status</a>
@elseif (view.path("status").asText().equals("documents_requested"))
    <div class="notice-strip warn">⚠ Additional documents were requested for your application. Please visit the documents step and upload fresh copies.</div>
    <a class="btn btn-primary" href="/journey/documents">Upload requested documents</a>
@else
    <div class="card">
        <h2 class="mb-0">Application status: ${statusLabel(view.path("status").asText())}</h2>
        @if (view.path("customerVisibleReason") != null && !view.path("customerVisibleReason").isMissingNode())
            <p class="mt-1 muted">${view.path("customerVisibleReason").asText()}</p>
        @endif
        <a class="btn btn-primary" href="/journey/status">View full status</a>
    </div>
@endif
`)
