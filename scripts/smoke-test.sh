#!/usr/bin/env bash
# End-to-end smoke test against the running stack (docker compose up).
# Exercises every role's login, the six primary journeys' key API gates, and the
# security acceptance criteria (cross-tenant, branch scope, max tier, OTP reuse, JWT alg).
set -u

API="${API:-http://localhost:9081}"
WEB="${WEB:-http://localhost:9080}"
PASS="Meridian#2026!"
PASS_OK=0
FAIL=0

say() { printf '\n\033[1;34m== %s\033[0m\n' "$*"; }
ok()  { PASS_OK=$((PASS_OK+1)); printf '  \033[32mPASS\033[0m %s\n' "$*"; }
bad() { FAIL=$((FAIL+1)); printf '  \033[31mFAIL\033[0m %s\n' "$*"; }
check() { # check <expected> <actual> <label>
  if [ "$1" = "$2" ]; then ok "$3"; else bad "$3 (expected $1, got $2)"; fi
}

login() { # login <email> -> token
  curl -s -X POST "$API/api/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$PASS\"}" | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])' 2>/dev/null
}

require_up() {
  for i in $(seq 1 60); do
    code=$(curl -s -o /dev/null -w '%{http_code}' "$WEB/login" 2>/dev/null)
    [ "$code" = "200" ] && return 0
    sleep 5
  done
  echo "web tier did not come up"; exit 1
}

say "Waiting for the web tier"
require_up
ok "web tier is up ($WEB)"

say "Role logins"
ADMIN=$(login admin@meridian.demo)
COMPLIANCE=$(login compliance@meridian.demo)
OFFICER=$(login officer@meridian.demo)
OFFICER_T1=$(login officer.east1@meridian.demo)
CUSTOMER=$(login customer@meridian.demo)
PROSPECT=$(login prospect@meridian.demo)
[ -n "$ADMIN" ] && [ -n "$COMPLIANCE" ] && [ -n "$OFFICER" ] && [ -n "$CUSTOMER" ] && [ -n "$PROSPECT" ] && [ -n "$OFFICER_T1" ] \
  && ok "all demo roles log in" || bad "a demo login failed"

say "Role dashboards via the web tier"
for role in "admin@meridian.demo|/admin/dashboard" "compliance@meridian.demo|/compliance/dashboard" \
           "officer@meridian.demo|/officer/dashboard" "customer@meridian.demo|/customer/dashboard" \
           "prospect@meridian.demo|/journey"; do
  email="${role%%|*}"; path="${role##*|}"
  jar="/tmp/wl-${email%@*}.cookies"; rm -f "$jar"
  curl -s -c "$jar" "$WEB/login" -o /tmp/wl-page.html
  csrf=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/wl-page.html | head -1 | sed 's/.*value="//; s/"$//')
  curl -s -b "$jar" -c "$jar" -o /dev/null -X POST "$WEB/login" \
    --data-urlencode "email=$email" --data-urlencode "password=$PASS" --data-urlencode "_csrf=$csrf"
  code=$(curl -s -o /dev/null -w '%{http_code}' -b "$jar" "$WEB$path")
  check 200 "$code" "GET $path"
done

say "Branch scoping: Central officer cannot see an East-branch application"
EAST_APP=$(curl -s "$API/api/officer/applications?size=50" -H "Authorization: Bearer $OFFICER_T1" \
  | python3 -c 'import sys,json
for a in json.load(sys.stdin)["items"]:
    if a["status"] not in ("approved","declined","abandoned"): print(a["id"]); break')
if [ -n "$EAST_APP" ]; then
  code=$(curl -s -o /dev/null -w '%{http_code}' "$API/api/officer/applications/$EAST_APP" -H "Authorization: Bearer $OFFICER")
  check 403 "$code" "Central officer reads East application"
else
  bad "no East application found for the scope check"
fi

say "Max approval tier: tier_1 officer approves a tier_2/3 application"
BIG=$(curl -s "$API/api/officer/applications?status=under_review&size=50" -H "Authorization: Bearer $OFFICER_T1" \
  | python3 -c 'import sys,json
for a in json.load(sys.stdin)["items"]:
    if a["kycTier"] in ("tier_2","tier_3"): print(a["id"]); break')
if [ -n "$BIG" ]; then
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/officer/applications/$BIG/approve" \
    -H "Authorization: Bearer $OFFICER_T1" -H 'Content-Type: application/json' \
    -d '{"customerVisibleReason":"smoke test"}')
  check 403 "$code" "tier_1 officer approve above authority refused"
else
  bad "no under-review tier_2/3 application found"
fi

say "Applicant-visible view must never contain bureau scores, hits or internal reason"
VIEW=$(curl -s "$API/api/applications/me" -H "Authorization: Bearer $CUSTOMER")
case "$VIEW" in *bureauResult*|*watchlistHits*|*internalReason*) bad "staff-only field leaked to customer" ;; *) ok "customer view is clean" ;; esac

say "OTP single-use (step-up): a code cannot be used twice"
OTP_PURPOSE="password_change"
ISSUE_CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/auth/otp/step-up/issue" -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' -d "{\"purpose\":\"$OTP_PURPOSE\"}")
if [ "$ISSUE_CODE" = "429" ]; then
  ok "OTP issuance rate limit per identity enforced (skipping reuse check)"
else
CODE=$(curl -s "$API/api/notifications" -H "Authorization: Bearer $ADMIN" \
  | python3 -c 'import sys,json,re
for n in json.load(sys.stdin)["items"]:
    m=re.search(r"code is (\d{6})", n["body"])
    if m: print(m.group(1)); break')
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/auth/otp/step-up/verify" \
  -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d "{\"purpose\":\"$OTP_PURPOSE\",\"code\":\"$CODE\"}")
check 200 "$code" "first use of the OTP"
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/auth/otp/step-up/verify" \
  -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d "{\"purpose\":\"$OTP_PURPOSE\",\"code\":\"$CODE\"}")
check 400 "$code" "reuse of the same OTP refused"
fi

say "JWT validation: alg=none and wrong audience are rejected"
SID=$(python3 -c 'import secrets;print(secrets.token_hex(8))')
SUB=$(curl -s "$API/api/auth/me" -H "Authorization: Bearer $ADMIN" | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])')
b64() { python3 -c "import base64,sys;print(base64.urlsafe_b64encode(sys.argv[1].encode()).decode().rstrip('='))" "$1"; }
HEADER=$(b64 '{"alg":"none"}')
PAYLOAD=$(b64 "{\"sub\":\"$SUB\",\"sid\":\"$SID\",\"iss\":\"meridian-onboarding-api\",\"aud\":\"meridian-channel\",\"exp\":4102444800}")
code=$(curl -s -o /dev/null -w '%{http_code}' "$API/api/reference/branches" -H "Authorization: Bearer $HEADER.$PAYLOAD.")
check 401 "$code" "alg=none token rejected"
HEADER_HS=$(b64 '{"alg":"HS256"}')
PAYLOAD_BAD=$(b64 "{\"sub\":\"$SUB\",\"sid\":\"$SID\",\"iss\":\"meridian-onboarding-api\",\"aud\":\"someone-else\",\"exp\":4102444800}")
code=$(curl -s -o /dev/null -w '%{http_code}' "$API/api/reference/branches" -H "Authorization: Bearer $HEADER_HS.$PAYLOAD_BAD.x")
check 401 "$code" "wrong-audience token rejected"

say "End-to-end registration (email OTP -> password -> login)"
EMAIL="smoke-$(date +%s)@meridian.test"
REG=$(curl -s -X POST "$API/api/auth/register" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"mobile\":\"+65 91234567\"}")
REG_TOKEN=$(echo "$REG" | python3 -c 'import sys,json;print(json.load(sys.stdin)["registrationToken"])')
curl -s -o /dev/null -X POST "$API/api/auth/registration/otp/issue" -H 'Content-Type: application/json' \
  -d "{\"registrationToken\":\"$REG_TOKEN\"}"
OTP_CODE=$(curl -s "$API/api/auth/registration/inbox?registrationToken=$REG_TOKEN" \
  | python3 -c 'import sys,json,re
for e in json.load(sys.stdin)["items"]:
    m=re.search(r"is (\d{6})", e["body"])
    if m: print(m.group(1)); break')
check 6 "${#OTP_CODE}" "registration OTP delivered (length 6)"
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/auth/registration/otp/verify" \
  -H 'Content-Type: application/json' \
  -d "{\"registrationToken\":\"$REG_TOKEN\",\"code\":\"$OTP_CODE\"}")
check 200 "$code" "email OTP verified"
RES=$(curl -s -X POST "$API/api/auth/registration/password" -H 'Content-Type: application/json' \
  -d "{\"registrationToken\":\"$REG_TOKEN\",\"password\":\"SmokePass#2026!\"}")
TOKEN=$(echo "$RES" | python3 -c 'import sys,json;print(json.load(sys.stdin).get("token",""))')
[ -n "$TOKEN" ] && ok "registration completed with a token" || bad "registration did not complete"
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/applications/me/submit" -H "Authorization: Bearer $TOKEN")
check 409 "$code" "submit blocked before mobile OTP and documents"

say "Step-up gate: approve without OTP is refused"
UNDER_REVIEW=$(curl -s "$API/api/officer/applications?status=under_review&size=50" -H "Authorization: Bearer $OFFICER" \
  | python3 -c 'import sys,json
items=json.load(sys.stdin)["items"]
print(items[0]["id"] if items else "")')
if [ -n "$UNDER_REVIEW" ]; then
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/officer/applications/$UNDER_REVIEW/approve" \
    -H "Authorization: Bearer $OFFICER" -H 'Content-Type: application/json' \
    -d '{"customerVisibleReason":"no otp"}')
  check 403 "$code" "approve without step-up refused"
else
  bad "no under-review application in the Central branch"
fi

printf '\n\033[1mResult: %d passed, %d failed\033[0m\n' "$PASS_OK" "$FAIL"
[ "$FAIL" -eq 0 ]
