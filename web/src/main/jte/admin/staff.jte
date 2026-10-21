@import com.fasterxml.jackson.databind.JsonNode
@import java.util.Map
@import com.meridianbank.web.auth.WebUser
@import static com.meridianbank.web.template.Tpl.*
@param WebUser user
@param long unread
@param String csrfToken
@param String csrfParameterName
@param JsonNode staff
@param JsonNode branches
@param Map<String, Object> filters
@param Map<String, Object> flash
@template.layout.main(pageTitle = "Staff management", activeNav = "admin-staff", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Staff management</h1>
@template.partials.flash(flash = flash)

<div class="card">
    <form method="get" action="/admin/staff" class="filters">
        <input class="input" type="text" name="q" placeholder="Search name or employee id…" value="${String.valueOf(filters.get("q"))}">
        <select class="input" name="role">
            <option value="">All roles</option>
            <option value="ONBOARDING_OFFICER">Onboarding officer</option>
            <option value="COMPLIANCE_OFFICER">Compliance officer</option>
            <option value="BANK_ADMIN">Bank admin</option>
        </select>
        <button class="btn btn-primary" type="submit">Filter</button>
    </form>
</div>

<div class="grid main-2">
    <div class="card">
        <h3>Add a staff member</h3>
        <form method="post" action="/admin/staff">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="form-row">
                <div class="field">
                    <label>Employee ID</label>
                    <input class="input" type="text" name="employeeId" required maxlength="20">
                </div>
                <div class="field">
                    <label>Full name</label>
                    <input class="input" type="text" name="fullName" required maxlength="120">
                </div>
            </div>
            <div class="form-row">
                <div class="field">
                    <label>Email</label>
                    <input class="input" type="email" name="email" required>
                </div>
                <div class="field">
                    <label>Role</label>
                    <select class="input" name="role" required>
                        <option value="ONBOARDING_OFFICER">Onboarding officer</option>
                        <option value="COMPLIANCE_OFFICER">Compliance officer</option>
                        <option value="BANK_ADMIN">Bank admin</option>
                    </select>
                </div>
            </div>
            <div class="form-row">
                <div class="field">
                    <label>Home branch</label>
                    <select class="input" name="branchCode" required>
                        @for (var branch : branches.path("items"))
                            <option value="${branch.path("code").asText()}">${branch.path("name").asText()}</option>
                        @endfor
                    </select>
                </div>
                <div class="field">
                    <label>Max approval tier (officers)</label>
                    <select class="input" name="maxApprovalTier">
                        <option value="tier_0">tier_0</option>
                        <option value="tier_1">tier_1</option>
                        <option value="tier_2">tier_2</option>
                        <option value="tier_3">tier_3</option>
                    </select>
                </div>
            </div>
            <button class="btn btn-primary" type="submit">Create staff account</button>
            <div class="help">A set-password invitation is sent to the staff member's email (mock).</div>
        </form>
    </div>
    <div class="card">
        <h3>Import a staff &amp; branch roster (CSV)</h3>
        <p class="muted small">Header: <span class="mono">employee_id,full_name,role,branch_code,max_approval_tier,email</span>. Roles: ONBOARDING_OFFICER, COMPLIANCE_OFFICER, BANK_ADMIN. Row errors are reported.</p>
        <form method="post" action="/admin/staff/import" enctype="multipart/form-data">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="dropzone">
                <span class="big">⇪</span>
                <input type="file" name="file" accept=".csv,text/csv" required data-label="fileLabel">
                <div id="fileLabel">Choose a CSV file…</div>
            </div>
            <button class="btn btn-ghost mt-2" type="submit">Import roster</button>
        </form>
    </div>
</div>

@if (staff.path("items").size() == 0)
    <div class="empty"><span class="glyph">◉</span><h3>No staff</h3></div>
@else
    <div class="table-wrap">
        <table class="table">
            <thead><tr><th>Employee</th><th>Email</th><th>Role</th><th>Branch</th><th>Max tier</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
                @for (var s : staff.path("items"))
                    <tr>
                        <td class="cell-strong">${s.path("fullName").asText()}<br><span class="cell-muted">${s.path("employeeId").asText()}</span></td>
                        <td>${s.path("email").asText()}</td>
                        <td>${s.path("role").asText().replace("_", " ")}</td>
                        <td>${s.path("branchName").asText()}</td>
                        <td>${s.path("maxApprovalTier").asText()}</td>
                        <td><span class="badge ${s.path("status").asText().equals("active") ? "badge-success" : "badge-danger"}"><span class="dot"></span>${s.path("status").asText()}</span></td>
                        <td>
                            <details class="panel" style="margin:0;">
                                <summary class="small">Edit</summary>
                                <div class="panel-body">
                                    <form method="post" action="/admin/staff/${s.path("id").asText()}">
                                        @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                                        <div class="field">
                                            <label>Role</label>
                                            <select class="input" name="role">
                                                <option value="ONBOARDING_OFFICER">Onboarding officer</option>
                                                <option value="COMPLIANCE_OFFICER">Compliance officer</option>
                                                <option value="BANK_ADMIN">Bank admin</option>
                                            </select>
                                        </div>
                                        <div class="field">
                                            <label>Branch</label>
                                            <select class="input" name="branchCode">
                                                @for (var branch : branches.path("items"))
                                                    <option value="${branch.path("code").asText()}">${branch.path("name").asText()}</option>
                                                @endfor
                                            </select>
                                        </div>
                                        <div class="field">
                                            <label>Max approval tier</label>
                                            <select class="input" name="maxApprovalTier">
                                                <option value="tier_0">tier_0</option>
                                                <option value="tier_1">tier_1</option>
                                                <option value="tier_2">tier_2</option>
                                                <option value="tier_3">tier_3</option>
                                            </select>
                                        </div>
                                        <div class="field">
                                            <label>Status</label>
                                            <select class="input" name="status">
                                                <option value="active">Active</option>
                                                <option value="disabled">Disabled</option>
                                            </select>
                                        </div>
                                        <button class="btn btn-primary btn-sm" type="submit">Save changes</button>
                                    </form>
                                </div>
                            </details>
                        </td>
                    </tr>
                @endfor
            </tbody>
        </table>
    </div>
@endif
`)
