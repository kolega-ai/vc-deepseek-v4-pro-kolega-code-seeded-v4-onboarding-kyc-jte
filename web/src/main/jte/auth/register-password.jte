@import java.util.Map
@param Map<String, Object> flash
@param String csrfToken
@param String csrfParameterName
@template.layout.auth(pageTitle = "Create your password", csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h2>Create your password</h2>
<p class="sub">Step 3 of 3 — the last thing before your application begins.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/register/password">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <div class="field">
        <label for="password">Password</label>
        <input class="input" type="password" id="password" name="password" autocomplete="new-password" required minlength="10">
        <div class="help">At least 10 characters with an uppercase letter, a lowercase letter and a digit.</div>
    </div>
    <button class="btn btn-primary btn-lg" type="submit" style="width:100%;">Create account</button>
</form>
`)
