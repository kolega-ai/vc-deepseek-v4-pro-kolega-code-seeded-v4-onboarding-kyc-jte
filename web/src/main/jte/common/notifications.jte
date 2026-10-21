@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode result
@template.layout.main(pageTitle = "Notifications", activeNav = "notifications", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<div class="flex-between mb-2">
    <h1 class="mb-0">Notifications</h1>
    <form method="post" action="/notifications/read-all">
        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
        <button class="btn btn-ghost btn-sm" type="submit">Mark all as read</button>
    </form>
</div>
<p class="muted small">OTP codes for step-up actions arrive here (SMS delivery is mocked locally).</p>

@if (result.path("items").size() == 0)
    <div class="empty"><span class="glyph">◎</span><h3>No notifications</h3></div>
@else
    @for (var n : result.path("items"))
        <div class="notification-item ${n.path("read").asBoolean() ? "" : "unread"}">
            <div class="n-icon ${n.path("type").asText()}">
                @if (n.path("type").asText().equals("otp"))
                    ◈
                @elseif (n.path("type").asText().equals("success"))
                    ✓
                @elseif (n.path("type").asText().equals("warning"))
                    ⚠
                @elseif (n.path("type").asText().equals("action_required"))
                    !
                @else
                    ✦
                @endif
            </div>
            <div style="flex:1;">
                <div class="n-title">${n.path("title").asText()}</div>
                <div class="n-body">$unsafe{n.path("body").asText()}</div>
                <div class="n-time">${fmtDateTime(n.path("createdAt").asText())}</div>
            </div>
            <div>
                @if (n.path("link") != null && !n.path("link").asText().isEmpty())
                    <a class="btn btn-ghost btn-sm" href="${n.path("link").asText()}">Open</a>
                @endif
                @if (!n.path("read").asBoolean())
                    <form method="post" action="/notifications/${n.path("id").asText()}/read">
                        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                        <button class="btn btn-link btn-sm" type="submit">Mark read</button>
                    </form>
                @endif
            </div>
        </div>
    @endfor
@endif
`)
