@import gg.jte.Content
@param String pageTitle
@param String csrfToken
@param String csrfParameterName
@param Content content
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} · Meridian Bank</title>
    <link rel="stylesheet" href="/css/app.css">
    <link rel="icon" type="image/svg+xml" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='46' fill='%230B6A5C'/%3E%3Ctext x='50' y='66' font-size='52' text-anchor='middle' fill='%23FDF8EF' font-family='Georgia'%3EM%3C/text%3E%3C/svg%3E">
</head>
<body>
<div class="auth-wrap">
    <div class="auth-brand-panel">
        <div class="brand-row">
            <div class="brand-mark">M</div>
            <div>
                <div class="brand-name" style="color:#FBF7EC;font-family:var(--font-display);font-size:1.05rem;">Meridian Bank</div>
                <div class="brand-sub" style="color:#7E96AC;font-size:0.68rem;letter-spacing:0.14em;text-transform:uppercase;">Digital Onboarding</div>
            </div>
        </div>
        <div>
            <h1>Open your account,<br>verified end to end.</h1>
            <p class="lede">A fully digital onboarding journey — identity verification, document checks, watchlist screening and KYC tiers, managed by our branch and compliance teams.</p>
            <div class="points">
                <div class="point"><span class="tick">✓</span><span>Seven guided steps, from registration to account opening.</span></div>
                <div class="point"><span class="tick">✓</span><span>KYC tiers with transaction and balance limits in SGD.</span></div>
                <div class="point"><span class="tick">✓</span><span>OTP-verified contact details and step-up approvals.</span></div>
                <div class="point"><span class="tick">✓</span><span>Periodic re-KYC keeps your account up to date.</span></div>
            </div>
        </div>
        <div class="foot">Demo environment · identity providers, credit bureau, watchlist and core banking are local mocks.</div>
    </div>
    <div class="auth-form-panel">
        <div class="auth-card">
            ${content}
        </div>
    </div>
</div>
</body>
</html>
