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
@template.layout.main(pageTitle = "Re-KYC", activeNav = "customer-rekyc", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Re-KYC document refresh</h1>
<p class="muted">Periodic re-verification keeps your account tier and limits up to date. You can only refresh documents here — your identity details and account number never change in this flow.</p>
@template.partials.flash(flash = flash)

@if (view.path("rekycCase") == null || view.path("rekycCase").isMissingNode())
    <div class="empty"><span class="glyph">✓</span><h3>Nothing due</h3>You have no open re-KYC case. The bank will notify you when a refresh is required.</div>
@else
    <div class="grid main-2">
        <div class="card">
            <div class="card-head"><h2 class="mb-0">Your case</h2>
                <span class="badge ${view.path("rekycCase").path("status").asText().equals("overdue") ? "badge-danger" : "badge-accent"}"><span class="dot"></span>${view.path("rekycCase").path("status").asText()}</span>
            </div>
            <dl class="kv">
                <dt>Due date</dt><dd>${fmtDate(view.path("rekycCase").path("dueDate").asText())}</dd>
                <dt>Required documents</dt>
                <dd>
                    @for (var type : view.path("rekycCase").path("requiredTypes"))
                        <span class="badge badge-neutral mr-1">${docTypeLabel(type.asText())}</span>
                    @endfor
                </dd>
            </dl>
            <div class="notice-strip info">Fresh documents are verified by a bank officer before you can submit.</div>
        </div>
        <div class="card">
            <h3>Upload a fresh document</h3>
            <form method="post" action="/customer/rekyc/${view.path("rekycCase").path("id").asText()}/documents" enctype="multipart/form-data">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <div class="field">
                    <label for="docType">Document type</label>
                    <select class="input" id="docType" name="docType">
                        <option value="NATIONAL_ID">National ID (NRIC)</option>
                        <option value="PASSPORT">Passport</option>
                        <option value="ADDRESS_PROOF">Address proof</option>
                        <option value="PROOF_OF_INCOME">Proof of income</option>
                    </select>
                    <div class="help">Only documents required for your tier are accepted.</div>
                </div>
                <div class="dropzone">
                    <span class="big">⇪</span>
                    <input type="file" name="file" accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg" required data-label="fileLabel">
                    <div id="fileLabel">Choose a file…</div>
                </div>
                <button class="btn btn-primary mt-2" type="submit">Upload document</button>
            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-head"><h2 class="mb-0">Documents for this refresh</h2></div>
        @if (view.path("rekycCase").path("documents").size() == 0)
            <div class="empty"><span class="glyph">▤</span><h3>No documents yet</h3>Upload a fresh document to begin.</div>
        @else
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Document</th><th>State</th><th>Expires</th><th></th></tr></thead>
                    <tbody>
                        @for (var doc : view.path("rekycCase").path("documents"))
                            <tr>
                                <td class="cell-strong">${doc.path("filename").asText()}</td>
                                <td><span class="badge ${docStateClass(doc.path("state").asText())}"><span class="dot"></span>${doc.path("state").asText()}</span></td>
                                <td class="cell-muted">${fmtDate(doc.path("expiryDate").asText())}</td>
                                <td class="actions"><a class="btn btn-ghost btn-sm" href="/documents/${doc.path("id").asText()}/download">Download</a></td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif
    </div>

    <div class="card">
        <h3>Submit your refresh</h3>
        <p class="muted small">All required documents must be <b>verified by an officer</b> first. Submitting requires a step-up OTP.</p>
        <form method="post" action="/customer/rekyc/${view.path("rekycCase").path("id").asText()}/otp">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <button class="btn btn-ghost" type="submit">1 · Send step-up code</button>
        </form>
        <form method="post" action="/customer/rekyc/${view.path("rekycCase").path("id").asText()}/submit" class="mt-1 flex flex-wrap">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <input class="input" type="text" name="otpCode" placeholder="2 · Enter 6-digit code" inputmode="numeric" maxlength="6" required data-otp style="max-width:220px;">
            <button class="btn btn-accent" type="submit">3 · Submit re-KYC</button>
        </form>
    </div>
@endif
`)
