@import java.util.Map
@import com.fasterxml.jackson.databind.JsonNode
@import static com.meridianbank.web.template.Tpl.*
@param Map<String, Object> flash
@param String csrfToken
@param String csrfParameterName
@param JsonNode emails
@template.layout.auth(pageTitle = "Verify your email", csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h2>Verify your email</h2>
<p class="sub">Step 2 of 3 — enter the 6-digit code we sent to your email address.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/register/email-otp/verify">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <div class="field">
        <label for="code">One-time code</label>
        <div class="otp-boxes">
            <input class="input" type="text" id="code" name="code" inputmode="numeric" maxlength="6" required placeholder="······" data-otp>
        </div>
        <div class="help">The code expires after 10 minutes and can only be used once.</div>
    </div>
    <button class="btn btn-primary btn-lg" type="submit" style="width:100%;">Verify email</button>
</form>
<form method="post" action="/register/email-otp/issue" class="mt-2">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <button class="btn btn-ghost" type="submit">Resend code</button>
</form>
@if (emails != null && emails.path("items").size() > 0)
    <div class="divider"></div>
    <h3 style="font-size:0.95rem;">Your mock inbox</h3>
    <p class="small muted" style="margin-bottom:10px;">Email delivery is mocked locally — your messages appear here during registration.</p>
    @for (var email : emails.path("items"))
        <details class="panel" style="margin-bottom:8px;">
            <summary class="small">✉ ${email.path("subject").asText()} · ${fmtDateTime(email.path("createdAt").asText())}</summary>
            <div class="panel-body small" style="white-space:pre-wrap;">${email.path("body").asText()}</div>
        </details>
    @endfor
@endif
`)
