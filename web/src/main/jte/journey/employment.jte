@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode view
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Employment & income", activeNav = "journey", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Employment &amp; income</h1>
<p class="muted">Self-employed applicants, and anyone declaring S$100,000 or more, need a verified proof of income for any account tier.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/journey/employment" class="card" style="max-width:640px;">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <div class="field">
        <label for="employmentStatus">Employment status</label>
        <select class="input" id="employmentStatus" name="employmentStatus" required>
            <option value="employed">Employed</option>
            <option value="self_employed">Self-employed</option>
            <option value="student">Student</option>
            <option value="retired">Retired</option>
            <option value="unemployed">Unemployed</option>
        </select>
    </div>
    <div class="field">
        <label for="employerName">Employer name</label>
        <input class="input" type="text" id="employerName" name="employerName" maxlength="160" value="${view.path("applicant").path("employerName").asText()}">
        <div class="help">Required if you are employed or self-employed.</div>
    </div>
    <div class="field">
        <label for="declaredAnnualIncome">Declared annual income (SGD)</label>
        <input class="input" type="number" id="declaredAnnualIncome" name="declaredAnnualIncome" required min="0" step="0.01" value="${view.path("applicant").path("declaredAnnualIncome").asText()}">
    </div>
    <div class="form-actions">
        <button class="btn btn-primary" type="submit">Save &amp; continue</button>
        <a class="btn btn-ghost" href="/journey">Cancel</a>
    </div>
</form>
`)
