#!/usr/bin/env bash
# Page-content sweep: log in as each role through the web tier and verify key screens render expected content.
set -u
WEB="${WEB:-http://localhost:9080}"
PASS="Meridian#2026!"
FAIL=0
check_page() { # <email> <path> <expected-content> <label>
  local email="$1" path="$2" expect="$3" label="$4"
  local jar="/tmp/pc-${email%@*}.cookies"
  rm -f "$jar"
  curl -s -c "$jar" "$WEB/login" -o /tmp/pc-page.html
  local csrf; csrf=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/pc-page.html | head -1 | sed 's/.*value="//; s/"$//')
  curl -s -b "$jar" -c "$jar" -o /dev/null -X POST "$WEB/login" \
    --data-urlencode "email=$email" --data-urlencode "password=$PASS" --data-urlencode "_csrf=$csrf"
  local body; body=$(curl -s -b "$jar" -c "$jar" "$WEB$path")
  if echo "$body" | grep -q "$expect"; then
    printf '  \033[32mPASS\033[0m %s\n' "$label"
  else
    printf '  \033[31mFAIL\033[0m %s (missing: %s)\n' "$label" "$expect"
    FAIL=$((FAIL+1))
  fi
}

echo "== Prospect screens"
check_page prospect@meridian.demo /journey "Your application" "journey overview"
check_page prospect@meridian.demo /journey/personal "Personal details" "personal form"
check_page prospect@meridian.demo /journey/documents "Upload a document" "documents page"
check_page prospect@meridian.demo /journey/identity "Identity verification" "identity page"
check_page prospect@meridian.demo /journey/status "Application status" "status page"
check_page prospect@meridian.demo /notifications "Notifications" "notifications page"

echo "== Customer screens"
check_page customer@meridian.demo /customer/dashboard "Welcome," "customer dashboard"
check_page customer@meridian.demo /customer/dashboard "Daily transaction limit" "limits shown"
check_page customer@meridian.demo /customer/rekyc "Re-KYC document refresh" "rekyc page"
check_page customer@meridian.demo /journey/status "Your account" "account number on status"

echo "== Officer screens"
check_page officer@meridian.demo /officer/dashboard "Officer dashboard" "officer dashboard"
check_page officer@meridian.demo /officer/applications "Applications" "officer list"
check_page officer@meridian.demo /officer/applications "Under review" "queue counts"

echo "== Compliance screens"
check_page compliance@meridian.demo /compliance/dashboard "Compliance overview" "compliance dashboard"
check_page compliance@meridian.demo /compliance/hits "Watchlist queue" "watchlist queue"
check_page compliance@meridian.demo /compliance/rekyc "Re-KYC campaigns" "campaigns page"

echo "== Admin screens"
check_page admin@meridian.demo /admin/dashboard "Bank administration" "admin dashboard"
check_page admin@meridian.demo /admin/staff "Staff management" "staff page"
check_page admin@meridian.demo /admin/branches "Branches" "branches page"
check_page admin@meridian.demo /admin/kyc-tiers "KYC tier policy" "tier policy page"
check_page admin@meridian.demo /admin/requirements "Document requirements" "requirements page"
check_page admin@meridian.demo /admin/audit "Audit log" "audit page"
check_page admin@meridian.demo /admin/integrations "Integrations" "integrations page"
check_page admin@meridian.demo /inbox "Demo inbox" "mock inbox"

echo "== Result: $FAIL failures"
[ "$FAIL" -eq 0 ]
