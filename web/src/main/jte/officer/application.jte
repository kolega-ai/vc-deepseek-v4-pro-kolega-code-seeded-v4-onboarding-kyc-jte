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
@template.layout.main(pageTitle = "Application review", activeNav = "officer-apps", user = user, unread = unread, csrfToken = csrfToken, csrfParameterName = csrfParameterName, content = @`
<div class="flex-between mb-2">
    <div>
        <h1 class="mb-0">${view.path("reference").asText()}</h1>
        <span class="muted small">${view.path("applicant").path("fullName").asText()} · ${view.path("applicant").path("email").asText()} · ${view.path("branch").asText()}</span>
    </div>
    <div class="flex">
        <span class="badge ${statusClass(view.path("status").asText())}"><span class="dot"></span>${statusLabel(view.path("status").asText())}</span>
        <span class="badge ${tierClass(view.path("effectiveKycTier").asText())}"><span class="dot"></span>${view.path("effectiveKycTier").asText()}</span>
    </div>
</div>
@template.partials.flash(flash = flash)

@if (view.path("status").asText().equals("watchlist_hold"))
    <div class="notice-strip danger">⚠ This application is on watchlist hold. Compliance must disposition the hits before any decision.</div>
@endif

<div class="grid main-2">
    <div class="card">
        <h2>Applicant</h2>
        <dl class="kv">
            <dt>Name</dt><dd>${view.path("applicant").path("fullName").asText()}</dd>
            <dt>Date of birth</dt><dd>${view.path("applicant").path("dateOfBirth").asText()}</dd>
            <dt>National identifier</dt><dd class="mono">${view.path("applicant").path("nationalIdentifier").asText()}</dd>
            <dt>Mobile</dt><dd>${view.path("applicant").path("mobile").asText()}</dd>
            <dt>Address</dt><dd>${view.path("applicant").path("addressLine1").asText()}, ${view.path("applicant").path("city").asText()} ${view.path("applicant").path("postalCode").asText()}</dd>
            <dt>Employment</dt><dd>${view.path("applicant").path("employmentStatus").asText()} · ${view.path("applicant").path("employerName").asText()}</dd>
            <dt>Declared annual income</dt><dd>${money(view.path("applicant").path("declaredAnnualIncome").asText())}</dd>
        </dl>
    </div>
    <div class="card">
        <h2>Checks</h2>
        <dl class="kv">
            <dt>Identity match</dt>
            @if (view.path("identityResult") != null && !view.path("identityResult").isMissingNode())
                <dd><span class="badge ${view.path("identityResult").path("matchScore").asInt() >= 80 ? "badge-success" : "badge-danger"}"><span class="dot"></span>score ${view.path("identityResult").path("matchScore").asText()} · ${view.path("identityResult").path("outcome").asText()}</span></dd>
            @else
                <dd><span class="badge badge-neutral"><span class="dot"></span>not run</span></dd>
            @endif
            <dt>Bureau score</dt>
            @if (view.path("bureauResult") != null && !view.path("bureauResult").isMissingNode())
                <dd><span class="badge badge-info"><span class="dot"></span>${view.path("bureauResult").path("score").asText()}</span> <span class="small faint">pulled ${fmtDateTime(view.path("bureauResult").path("pulledAt").asText())}</span></dd>
            @else
                <dd><span class="badge badge-neutral"><span class="dot"></span>not available</span></dd>
            @endif
            <dt>Watchlist hits</dt>
            <dd>${view.path("watchlistHits").size()} hit(s)</dd>
        </dl>
        @if (view.path("bureauResult") != null && !view.path("bureauResult").isMissingNode() && view.path("bureauResult").path("liabilities") != null)
            <div class="section-title">Reported liabilities</div>
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Type</th><th>Amount</th></tr></thead>
                    <tbody>
                        @for (var li : view.path("bureauResult").path("liabilities"))
                            <tr><td>${li.path("type").asText()}</td><td>${money(li.path("amount").asText())} ${li.path("currency").asText()}</td></tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif
    </div>
</div>

@if (view.path("watchlistHits").size() > 0)
    <div class="card">
        <h2>Watchlist hits</h2>
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Matched name</th><th>List source</th><th>Confidence</th><th>Disposition</th><th>Justification</th></tr></thead>
                <tbody>
                    @for (var hit : view.path("watchlistHits"))
                        <tr>
                            <td class="cell-strong">${hit.path("matchedName").asText()}</td>
                            <td>${hit.path("listSource").asText()}</td>
                            <td>${hit.path("confidence").asText()}</td>
                            <td><span class="badge ${dispositionClass(hit.path("disposition").asText())}"><span class="dot"></span>${dispositionLabel(hit.path("disposition").asText())}</span></td>
                            <td class="cell-muted">${hit.path("justification").asText()}</td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        </div>
    </div>
@endif

<div class="card">
    <h2>Documents</h2>
    @if (view.path("documents").size() == 0)
        <div class="empty"><span class="glyph">▤</span><h3>No documents</h3></div>
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
                        <span>${doc.path("sizeBytes").asText()} bytes</span>
                        <span>expires ${fmtDate(doc.path("expiryDate").asText())}</span>
                    </div>
                    @if (doc.path("verifiedBy") != null && !doc.path("verifiedBy").asText().isEmpty())
                        <div class="doc-meta"><span>verified by ${doc.path("verifiedBy").asText()}</span></div>
                    @endif
                    <div class="doc-actions">
                        <a class="btn btn-ghost btn-sm" href="/documents/${doc.path("id").asText()}/download">Download</a>
                        @if (doc.path("state").asText().equals("uploaded"))
                            <form method="post" action="/officer/documents/${doc.path("id").asText()}/verify">
                                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                                <input type="hidden" name="returnTo" value="/officer/applications/${view.path("id").asText()}">
                                <button class="btn btn-success btn-sm" type="submit">Verify</button>
                            </form>
                            <form method="post" action="/officer/documents/${doc.path("id").asText()}/reject">
                                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                                <input type="hidden" name="returnTo" value="/officer/applications/${view.path("id").asText()}">
                                <button class="btn btn-ghost-danger btn-sm" type="submit">Reject</button>
                            </form>
                        @endif
                    </div>
                </div>
            @endfor
        </div>
    @endif
</div>

@if (view.path("status").asText().equals("under_review"))
    <div class="grid main-2">
        <div class="card">
            <h3>Request more documents</h3>
            <form method="post" action="/officer/applications/${view.path("id").asText()}/request-documents">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <div class="field">
                    <label>Document types</label>
                    <div class="checkbox-row"><input type="checkbox" name="types" value="NATIONAL_ID" id="t1"><label for="t1">National ID</label></div>
                    <div class="checkbox-row"><input type="checkbox" name="types" value="PASSPORT" id="t2"><label for="t2">Passport</label></div>
                    <div class="checkbox-row"><input type="checkbox" name="types" value="ADDRESS_PROOF" id="t3"><label for="t3">Address proof</label></div>
                    <div class="checkbox-row"><input type="checkbox" name="types" value="PROOF_OF_INCOME" id="t4"><label for="t4">Proof of income</label></div>
                </div>
                <div class="field">
                    <label for="requestMessage">Message to applicant</label>
                    <textarea class="input" id="requestMessage" name="message" required minlength="5"></textarea>
                </div>
                <button class="btn btn-ghost" type="submit">Request documents</button>
            </form>
        </div>
        <div class="card">
            <h3>Decision</h3>
            <p class="small muted">Decisions need a step-up OTP. Approval is limited to your maximum approval tier.</p>
            <form method="post" action="/officer/applications/${view.path("id").asText()}/approve/otp" class="mb-1">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <button class="btn btn-ghost btn-sm" type="submit">1 · Send approve code</button>
            </form>
            <form method="post" action="/officer/applications/${view.path("id").asText()}/approve">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <div class="field">
                    <label>Step-up code</label>
                    <input class="input" type="text" name="otpCode" required inputmode="numeric" maxlength="6" placeholder="2 · 6-digit code" data-otp>
                </div>
                <div class="field">
                    <label for="approveReason">Customer-visible reason</label>
                    <textarea class="input" id="approveReason" name="customerVisibleReason" required minlength="5">Identity and documents verified; account approved.</textarea>
                </div>
                <button class="btn btn-success" type="submit">3 · Approve &amp; open account</button>
            </form>
            <div class="divider"></div>
            <form method="post" action="/officer/applications/${view.path("id").asText()}/decline/otp" class="mb-1">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <button class="btn btn-ghost btn-sm" type="submit">1 · Send decline code</button>
            </form>
            <form method="post" action="/officer/applications/${view.path("id").asText()}/decline">
                @template.partials.csrf(token = csrfToken, parameterName = csrfParameterName)
                <div class="field">
                    <label>Step-up code</label>
                    <input class="input" type="text" name="otpCode" required inputmode="numeric" maxlength="6" placeholder="2 · 6-digit code" data-otp>
                </div>
                <div class="field">
                    <label for="declineReason">Customer-visible reason</label>
                    <textarea class="input" id="declineReason" name="customerVisibleReason" required minlength="5"></textarea>
                </div>
                <div class="field">
                    <label for="internalReason">Internal reason (never shown to the applicant)</label>
                    <textarea class="input" id="internalReason" name="internalReason" required minlength="5"></textarea>
                </div>
                <button class="btn btn-danger" type="submit">3 · Decline</button>
            </form>
        </div>
    </div>
@endif

@if (view.path("internalReason") != null && !view.path("internalReason").asText().isEmpty())
    <div class="card">
        <h3>Internal notes</h3>
        <p class="mb-0 small">${view.path("internalReason").asText()}</p>
    </div>
@endif
`)
