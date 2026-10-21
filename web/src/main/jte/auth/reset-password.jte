@import java.util.Map
@param Map<String, Object> flash
@param String token
@param String csrfToken
@param String csrfParameterName
@template.layout.auth(pageTitle = "Reset your password", csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h2>Reset your password</h2>
<p class="sub">Reset links are single-use and valid for 30 minutes.</p>
@template.partials.flash(flash = flash)
@if (token != null && !token.isEmpty())
    <form method="post" action="/reset-password/complete">
        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
        <input type="hidden" name="token" value="${token}">
        <div class="field">
            <label for="newPassword">New password</label>
            <input class="input" type="password" id="newPassword" name="newPassword" autocomplete="new-password" required minlength="10">
            <div class="help">At least 10 characters with an uppercase letter, a lowercase letter and a digit.</div>
        </div>
        <button class="btn btn-primary btn-lg" type="submit" style="width:100%;">Set new password</button>
    </form>
@else
    <form method="post" action="/reset-password/request">
        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
        <div class="field">
            <label for="email">Email address</label>
            <input class="input" type="email" id="email" name="email" autocomplete="username" required>
        </div>
        <button class="btn btn-primary btn-lg" type="submit" style="width:100%;">Send reset link</button>
    </form>
    <div class="mt-2 small muted"><a href="/login">Back to sign in</a></div>
@endif
`)
