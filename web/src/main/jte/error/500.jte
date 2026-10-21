@import java.util.Map
@param String message
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Something went wrong · Meridian Bank</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body style="display:grid;place-items:center;min-height:100vh;background:var(--paper);">
    <div class="card" style="max-width:460px;text-align:center;">
        <div class="empty" style="border:0;">
            <span class="glyph">⚠</span>
            <h3>Something went wrong</h3>
            ${message}
        </div>
        <a class="btn btn-primary" href="/">Back to safety</a>
    </div>
</body>
</html>
