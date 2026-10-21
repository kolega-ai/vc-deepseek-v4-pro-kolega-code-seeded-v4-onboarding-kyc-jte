@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode result
@param Map<String, Object> filters
@template.layout.main(pageTitle = "Audit log", activeNav = "admin-audit", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<div class="flex-between mb-2">
    <div>
        <h1 class="mb-0">Audit log</h1>
        <span class="muted small">Append-only: every state transition and privileged action. No role can modify or delete these records.</span>
    </div>
    <a class="btn btn-ghost" href="/admin/audit/export.csv?action=${String.valueOf(filters.get("action"))}&actor=${String.valueOf(filters.get("actor"))}">Export CSV</a>
</div>

<div class="card">
    <form method="get" action="/admin/audit" class="filters">
        <input class="input" type="text" name="action" placeholder="Action, e.g. APPLICATION_APPROVED" value="${String.valueOf(filters.get("action"))}">
        <input class="input" type="text" name="actor" placeholder="Actor email" value="${String.valueOf(filters.get("actor"))}">
        <input class="input" type="text" name="entity" placeholder="Entity id, e.g. ONB-2026-…" value="${String.valueOf(filters.get("entity"))}">
        <button class="btn btn-primary" type="submit">Filter</button>
    </form>
</div>

@if (result.path("items").size() == 0)
    <div class="empty"><span class="glyph">≡</span><h3>No audit events</h3>No events match these filters.</div>
@else
    <div class="table-wrap">
        <table class="table">
            <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Entity</th><th>Before</th><th>After</th><th>Authorization basis</th></tr></thead>
            <tbody>
                @for (var event : result.path("items"))
                    <tr>
                        <td class="cell-muted" style="white-space:nowrap;">${fmtDateTime(event.path("createdAt").asText())}</td>
                        <td>${event.path("actor").asText()}<br><span class="cell-muted">${event.path("actorType").asText()}</span></td>
                        <td class="cell-strong">${event.path("action").asText()}</td>
                        <td>${event.path("entityType").asText()}<br><span class="cell-muted mono">${event.path("entityId").asText()}</span></td>
                        <td class="cell-muted small" style="max-width:220px;overflow-wrap:anywhere;">${event.path("beforeValue").asText()}</td>
                        <td class="cell-muted small" style="max-width:220px;overflow-wrap:anywhere;">${event.path("afterValue").asText()}</td>
                        <td class="cell-muted small">${event.path("authorizationBasis").asText()}</td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
    <div class="pagination">
        <span class="meta">${result.path("total").asText()} events · page ${result.path("page").asInt() + 1} of ${result.path("pages").asInt()}</span>
        @if (result.path("page").asInt() > 0)
            <a href="?page=${result.path("page").asInt() - 1}&action=${String.valueOf(filters.get("action"))}&actor=${String.valueOf(filters.get("actor"))}">← Previous</a>
        @endif
        @if (result.path("page").asInt() + 1 < result.path("pages").asInt())
            <a href="?page=${result.path("page").asInt() + 1}&action=${String.valueOf(filters.get("action"))}&actor=${String.valueOf(filters.get("actor"))}">Next →</a>
        @endif
    </div>
@endif
`)
