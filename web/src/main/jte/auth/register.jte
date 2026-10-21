@import java.util.Map
@param Map<String, Object> flash
@param String csrfToken
@param String csrfParameterName
@template.layout.auth(pageTitle = "Start your application", csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h2>Start your application</h2>
<p class="sub">Step 1 of 3 — your contact details. We will verify your email and mobile with one-time codes.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/register">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <div class="field">
        <label for="email">Email address</label>
        <input class="input" type="email" id="email" name="email" autocomplete="email" required placeholder="you@example.com">
        <div class="help">Used for sign-in and notifications.</div>
    </div>
    <div class="field">
        <label for="mobile">Mobile number</label>
        <input class="input" id="mobile" name="mobile" required placeholder="+65 8123 4567" inputmode="tel">
        <div class="help">Singapore mobile — we verify it with a one-time code.</div>
    </div>
    <button class="btn btn-primary btn-lg" type="submit" style="width:100%;">Continue</button>
</form>
<div class="mt-2 small muted">Already have an account? <a href="/login">Sign in</a></div>
`)
