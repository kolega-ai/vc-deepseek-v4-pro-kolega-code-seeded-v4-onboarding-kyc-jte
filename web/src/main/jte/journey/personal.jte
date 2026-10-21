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
@template.layout.main(pageTitle = "Personal details", activeNav = "journey", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Personal details</h1>
<p class="muted">Your identity details are verified against official records during identity verification.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/journey/personal" class="card" style="max-width:640px;">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <div class="field">
        <label for="fullName">Full legal name</label>
        <input class="input" type="text" id="fullName" name="fullName" required maxlength="120" value="${view.path("applicant").path("fullName").asText()}">
    </div>
    <div class="form-row">
        <div class="field">
            <label for="dateOfBirth">Date of birth</label>
            <input class="input" type="date" id="dateOfBirth" name="dateOfBirth" required value="${view.path("applicant").path("dateOfBirth").asText()}">
            <div class="help">You must be at least 18 years old.</div>
        </div>
        <div class="field">
            <label for="nationalIdentifier">National identifier (NRIC)</label>
            <input class="input" type="text" id="nationalIdentifier" name="nationalIdentifier" required maxlength="40" value="${view.path("applicant").path("nationalIdentifier").asText()}">
        </div>
    </div>
    <div class="form-actions">
        <button class="btn btn-primary" type="submit">Save &amp; continue</button>
        <a class="btn btn-ghost" href="/journey">Cancel</a>
    </div>
</form>
`)
