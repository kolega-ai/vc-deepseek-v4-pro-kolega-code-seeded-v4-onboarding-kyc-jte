@import java.util.Map
@import java.util.List
@param Map<String, Object> flash
@param String csrfToken
@param String csrfParameterName
@param String next
@template.layout.auth(pageTitle = "Sign in", csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h2>Welcome back</h2>
<p class="sub">Sign in to continue your application or manage onboarding work.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/login">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    @if (next != null && !next.isEmpty())
        <input type="hidden" name="next" value="${next}">
    @endif
    <div class="field">
        <label for="email">Email address</label>
        <input class="input" type="email" id="email" name="email" autocomplete="username" required placeholder="you@example.com">
    </div>
    <div class="field">
        <label for="password">Password</label>
        <input class="input" type="password" id="password" name="password" autocomplete="current-password" required>
    </div>
    <button class="btn btn-primary btn-lg" type="submit" style="width:100%;">Sign in</button>
</form>
<div class="mt-2 flex flex-wrap">
    <a href="/reset-password" class="small">Forgot password?</a>
    <span class="right small muted">New here? <a href="/register">Start an application</a></span>
</div>
<div class="divider"></div>
<details class="panel">
    <summary class="small">Demo credentials (every role)</summary>
    <div class="panel-body small" style="line-height:1.9;">
        <b>Prospect</b> — prospect@meridian.demo / Meridian#2026!<br>
        <b>Customer</b> — customer@meridian.demo / Meridian#2026!<br>
        <b>Officer</b> (Central, tier 3) — officer@meridian.demo / Meridian#2026!<br>
        <b>Officer</b> (East, tier 1) — officer.east1@meridian.demo / Meridian#2026!<br>
        <b>Compliance</b> — compliance@meridian.demo / Meridian#2026!<br>
        <b>Admin</b> — admin@meridian.demo / Meridian#2026!
    </div>
</details>
`)
