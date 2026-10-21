@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode integrations
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Integrations", activeNav = "admin-integrations", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Integrations</h1>
<p class="muted">Outbound webhooks for decision events and the local vendor credentials used by the onboarding service.</p>
@template.partials.flash(flash = flash)

<div class="grid main-2">
    <div class="card">
        <h3>Webhook configuration</h3>
        <form method="post" action="/admin/integrations/webhook">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="field">
                <label>Endpoint URL</label>
                <input class="input" type="text" name="endpointUrl" placeholder="https://your-system.example/hooks" value="${integrations.path("webhookUrl").asText()}">
                <div class="help">Decision events (approved, declined, tier overridden) are POSTed here with an HMAC-SHA256 signature.</div>
            </div>
            <div class="field">
                <label>Signing secret</label>
                <input class="input" type="password" name="secret" placeholder="${integrations.path("webhookSecretSet").asBoolean() ? "•••••••• (set)" : "Choose a secret"}">
            </div>
            <div class="checkbox-row mb-2">
                @if (integrations.path("webhookEnabled").asBoolean())
                    <input type="checkbox" id="webhookEnabled" name="enabled" value="true" checked>
                @else
                    <input type="checkbox" id="webhookEnabled" name="enabled" value="true">
                @endif
                <label for="webhookEnabled">Enable webhooks</label>
            </div>
            <button class="btn btn-primary" type="submit">Save webhook settings</button>
        </form>
    </div>
    <div class="card">
        <h3>Vendor credentials</h3>
        <dl class="kv">
            <dt>Provider mock key</dt><dd class="mono">${integrations.path("providerKey").asText()}</dd>
            <dt>Core mock key</dt><dd class="mono">${integrations.path("coreKey").asText()}</dd>
        </dl>
        <p class="small muted mt-2">These credentials are shared with the local provider-mock and core-mock services via environment variables; the onboarding service authenticates with them on every call. The core additionally requires a decision-minted scope token per account opening.</p>
    </div>
</div>

<div class="card">
    <h3>Webhook delivery attempts</h3>
    @if (integrations.path("deliveries").size() == 0)
        <div class="empty"><span class="glyph">⇄</span><h3>No deliveries yet</h3>Deliveries are recorded once events fire (e.g. an application is approved).</div>
    @else
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>When</th><th>Event</th><th>Result</th><th>Response</th><th>Error</th></tr></thead>
                <tbody>
                    @for (var delivery : integrations.path("deliveries"))
                        <tr>
                            <td class="cell-muted" style="white-space:nowrap;">${fmtDateTime(delivery.path("attemptedAt").asText())}</td>
                            <td class="cell-strong">${delivery.path("eventType").asText()}</td>
                            <td>
                                @if (delivery.path("success").asBoolean())
                                    <span class="badge badge-success"><span class="dot"></span>delivered</span>
                                @else
                                    <span class="badge badge-danger"><span class="dot"></span>failed</span>
                                @endif
                            </td>
                            <td class="cell-muted">${delivery.path("responseCode").asText()}</td>
                            <td class="cell-muted small">${delivery.path("error").asText()}</td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        </div>
    @endif
</div>
`)
