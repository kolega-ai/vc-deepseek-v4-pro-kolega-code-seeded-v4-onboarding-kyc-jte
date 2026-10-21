@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode view
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Address details", activeNav = "journey", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Address details</h1>
<p class="muted">Your residential address. You will verify it with a recent address-proof document later.</p>
@template.partials.flash(flash = flash)
<form method="post" action="/journey/address" class="card" style="max-width:640px;">
    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
    <div class="field">
        <label for="addressLine1">Address line 1</label>
        <input class="input" type="text" id="addressLine1" name="addressLine1" required maxlength="160" value="${view.path("applicant").path("addressLine1").asText()}">
    </div>
    <div class="field">
        <label for="addressLine2">Address line 2 (optional)</label>
        <input class="input" type="text" id="addressLine2" name="addressLine2" maxlength="160" value="${view.path("applicant").path("addressLine2").asText()}">
    </div>
    <div class="form-row">
        <div class="field">
            <label for="city">City</label>
            <input class="input" type="text" id="city" name="city" required value="${view.path("applicant").path("city").asText()}">
        </div>
        <div class="field">
            <label for="postalCode">Postal code</label>
            <input class="input" type="text" id="postalCode" name="postalCode" required maxlength="12" value="${view.path("applicant").path("postalCode").asText()}">
        </div>
    </div>
    <div class="field">
        <label for="country">Country</label>
        <input class="input" type="text" id="country" name="country" required value="${view.path("applicant").path("country").asText()}">
    </div>
    <div class="form-actions">
        <button class="btn btn-primary" type="submit">Save &amp; continue</button>
        <a class="btn btn-ghost" href="/journey">Cancel</a>
    </div>
</form>
`)
