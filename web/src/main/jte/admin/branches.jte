@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode branches
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Branches", activeNav = "admin-branches", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Branches</h1>
@template.partials.flash(flash = flash)

<div class="grid main-2">
    <div class="card">
        <h3>Add a branch</h3>
        <form method="post" action="/admin/branches">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="form-row">
                <div class="field">
                    <label>Branch code</label>
                    <input class="input" type="text" name="code" required maxlength="20" placeholder="MB-NOR-04">
                </div>
                <div class="field">
                    <label>Name</label>
                    <input class="input" type="text" name="name" required maxlength="120">
                </div>
            </div>
            <div class="field">
                <label>Region</label>
                <input class="input" type="text" name="region" required maxlength="60" placeholder="North">
            </div>
            <button class="btn btn-primary" type="submit">Create branch</button>
        </form>
    </div>
    <div class="card">
        <h3>Import branch reference data (XML)</h3>
        <p class="muted small">Benign reference-data XML import. The server parses it with a hardened parser — DTDs and external entities are rejected.</p>
        <form method="post" action="/admin/branches/import-xml" enctype="multipart/form-data">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="dropzone">
                <span class="big">⇪</span>
                <input type="file" name="file" accept=".xml,application/xml" required data-label="fileLabel">
                <div id="fileLabel">Choose an XML file…</div>
            </div>
            <button class="btn btn-ghost mt-2" type="submit">Import XML</button>
        </form>
        <pre class="small faint mt-2" style="background:var(--paper-2);padding:10px;border-radius:8px;overflow:auto;">&lt;reference-data&gt;
  &lt;branches&gt;
    &lt;branch&gt;&lt;code&gt;MB-NOR-04&lt;/code&gt;&lt;name&gt;Woodlands Central&lt;/name&gt;&lt;region&gt;North&lt;/region&gt;&lt;/branch&gt;
  &lt;/branches&gt;
&lt;/reference-data&gt;</pre>
    </div>
</div>

<div class="table-wrap">
    <table class="table">
        <thead><tr><th>Code</th><th>Name</th><th>Region</th><th>Staff</th><th>Actions</th></tr></thead>
        <tbody>
            @for (var branch : branches.path("items"))
                <tr>
                    <td class="cell-strong mono">${branch.path("code").asText()}</td>
                    <td>${branch.path("name").asText()}</td>
                    <td>${branch.path("region").asText()}</td>
                    <td>${branch.path("staff").asText()}</td>
                    <td>
                        <details class="panel" style="margin:0;">
                            <summary class="small">Edit</summary>
                            <div class="panel-body">
                                <form method="post" action="/admin/branches/${branch.path("id").asText()}">
                                    @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                                    <div class="field">
                                        <label>Name</label>
                                        <input class="input" type="text" name="name" required value="${branch.path("name").asText()}">
                                    </div>
                                    <div class="field">
                                        <label>Region</label>
                                        <input class="input" type="text" name="region" required value="${branch.path("region").asText()}">
                                    </div>
                                    <button class="btn btn-primary btn-sm" type="submit">Save</button>
                                </form>
                            </div>
                        </details>
                    </td>
                </tr>
            @endfor
        </tbody>
    </table>
</div>
`)
