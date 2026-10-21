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
@template.layout.main(pageTitle = "Documents", activeNav = "documents", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<h1>Documents</h1>
<p class="muted">PDF, PNG or JPEG up to 10 MB. Uploaded documents are verified by a bank officer in your branch.</p>
@template.partials.flash(flash = flash)

<div class="grid main-2">
    <div class="card">
        <h3>Upload a document</h3>
        <form method="post" action="/journey/documents" enctype="multipart/form-data">
            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
            <div class="field">
                <label for="docType">Document type</label>
                <select class="input" id="docType" name="docType">
                    <option value="NATIONAL_ID">National ID (NRIC)</option>
                    <option value="PASSPORT">Passport</option>
                    <option value="ADDRESS_PROOF">Address proof (utility bill / bank statement)</option>
                    <option value="PROOF_OF_INCOME">Proof of income (payslip / tax notice)</option>
                </select>
            </div>
            <div class="dropzone">
                <span class="big">⇪</span>
                <input type="file" name="file" accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg" required data-label="fileLabel">
                <div id="fileLabel">Choose a file…</div>
            </div>
            <button class="btn btn-primary mt-2" type="submit">Upload document</button>
        </form>
    </div>
    <div class="card">
        <h3>What is required</h3>
        <ul class="small" style="padding-left:18px;color:var(--muted);line-height:2;">
            <li>A verified identity document (NRIC or passport) for <b>tier 1</b>.</li>
            <li>Plus a verified address proof for <b>tier 2</b>.</li>
            <li>Plus a verified proof of income for <b>tier 3</b>.</li>
            <li>Self-employed applicants, or income of S$100,000+, need proof of income for <b>any</b> tier.</li>
        </ul>
        @if (view.path("requiredDocumentsBlocked").asBoolean())
            <div class="notice-strip warn">⚠ Based on your employment and income, a proof of income is required before you can submit.</div>
        @endif
    </div>
</div>

<h2 class="mt-3">Uploaded documents</h2>
@if (view.path("documents").size() == 0)
    <div class="empty"><span class="glyph">▤</span><h3>No documents yet</h3>Upload your first document to continue.</div>
@else
    <div class="doc-grid">
        @for (var doc : view.path("documents"))
            <div class="doc-card">
                <div class="flex-between">
                    <span class="doc-name">${docTypeLabel(doc.path("type").asText())}</span>
                    <span class="badge ${docStateClass(doc.path("state").asText())}"><span class="dot"></span>${doc.path("state").asText()}</span>
                </div>
                <div class="doc-meta">
                    <span>${doc.path("filename").asText()}</span>
                    <span>expires ${fmtDate(doc.path("expiryDate").asText())}</span>
                </div>
                <div class="doc-actions">
                    <a class="btn btn-ghost btn-sm" href="/documents/${doc.path("id").asText()}/download">Download</a>
                    @if (view.path("status").asText().equals("in_progress") && !doc.path("state").asText().equals("verified"))
                        <form method="post" action="/journey/documents/${doc.path("id").asText()}/delete" data-confirm="Remove this document? You can upload a replacement.">
                            @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                            <button class="btn btn-ghost-danger btn-sm" type="submit">Delete</button>
                        </form>
                    @endif
                </div>
            </div>
        @endfor
    </div>
@endif
`)
