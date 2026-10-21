@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode me
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Profile & security", activeNav = "profile", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Profile &amp; security</h1>
@template.partials.flash(flash = flash)

<div class="grid main-2">
    <div class="card">
        <h3>Account</h3>
        <dl class="kv">
            <dt>Name</dt><dd>${me.path("displayName").asText()}</dd>
            <dt>Email</dt><dd>${me.path("email").asText()}</dd>
            <dt>Role</dt><dd>${me.path("role").asText().replace("_", " ")}</dd>
            <dt>Email verified</dt><dd>${me.path("emailVerified").asBoolean() ? "✓ yes" : "no"}</dd>
            <dt>Mobile verified</dt><dd>${me.path("mobileVerified").asBoolean() ? "✓ yes" : "no"}</dd>
        </dl>
    </div>
    <div class="card">
        <h3>Change password</h3>
        <p class="muted small">Changing your password requires a step-up OTP.</p>
        <form method="post" action="/profile/password/otp" class="mb-1">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <button class="btn btn-ghost btn-sm" type="submit">1 · Send step-up code</button>
        </form>
        <form method="post" action="/profile/password">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="field">
                <label>Current password</label>
                <input class="input" type="password" name="currentPassword" autocomplete="current-password" required>
            </div>
            <div class="field">
                <label>New password</label>
                <input class="input" type="password" name="newPassword" autocomplete="new-password" required minlength="10">
                <div class="help">At least 10 characters with an uppercase letter, a lowercase letter and a digit.</div>
            </div>
            <div class="field">
                <label>Step-up code (2)</label>
                <input class="input" type="text" name="otpCode" required inputmode="numeric" maxlength="6" placeholder="6-digit code" data-otp>
            </div>
            <button class="btn btn-primary" type="submit">3 · Change password</button>
        </form>
    </div>
</div>
`)
