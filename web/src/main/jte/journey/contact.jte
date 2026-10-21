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
@template.layout.main(pageTitle = "Contact verification", activeNav = "journey", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Verify your mobile number</h1>
<p class="muted">Both your email and mobile must be verified before you can upload documents.</p>
@template.partials.flash(flash = flash)
<div class="grid main-2">
    <div class="card">
        <h3>Mobile number</h3>
        <p class="mono">${view.path("applicant").path("mobile").asText()}</p>
        <div class="notice-strip info">SMS delivery is mocked locally: after you press “Send code”, the code appears in your <a href="/notifications">notification bell</a> and demo inbox.</div>
        <form method="post" action="/journey/contact/issue" class="mb-2">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <button class="btn btn-accent" type="submit">Send code</button>
        </form>
        <form method="post" action="/journey/contact/verify">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="field">
                <label for="code">One-time code</label>
                <div class="otp-boxes">
                    <input class="input" type="text" id="code" name="code" inputmode="numeric" maxlength="6" required placeholder="······" data-otp>
                </div>
                <div class="help">Single use, expires in 10 minutes.</div>
            </div>
            <button class="btn btn-primary" type="submit">Verify mobile</button>
        </form>
    </div>
    <div class="card">
        <h3>What has been verified</h3>
        <dl class="kv">
            <dt>Email</dt><dd>✓ verified</dd>
            <dt>Mobile</dt>
            @if (view.path("applicant").path("mobile") != null && !view.path("applicant").path("mobile").asText().isEmpty())
                <dd><span class="badge badge-success"><span class="dot"></span>verified</span></dd>
            @else
                <dd><span class="badge badge-neutral"><span class="dot"></span>pending</span></dd>
            @endif
        </dl>
    </div>
</div>
`)
