@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode result
@template.layout.main(pageTitle = "Demo inbox", activeNav = "inbox", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Demo inbox</h1>
<p class="muted">Outbound emails and SMS (mock) addressed to <b>${user.email()}</b>. In production these go to a real mail gateway.</p>

@if (result.path("items").size() == 0)
    <div class="empty"><span class="glyph">✉</span><h3>Inbox is empty</h3></div>
@else
    @for (var email : result.path("items"))
        <details class="email-item">
            <summary>
                <span class="e-subject">${email.path("subject").asText()}</span>
                <span class="e-meta">${email.path("purpose").asText()} · ${fmtDateTime(email.path("createdAt").asText())} · ${email.path("status").asText()}</span>
            </summary>
            <div class="panel-body">
                @if (email.path("purpose").asText().startsWith("otp"))
                    <div class="e-otp">${email.path("body").asText()}</div>
                @else
                    ${email.path("body").asText()}
                @endif
            </div>
        </details>
    @endfor
@endif
`)
