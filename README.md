# Meridian Bank — Retail Onboarding & e-KYC Journey

A realistic multi-module banking application implementing a retail bank's digital onboarding
journey: registration with dual-channel OTP verification, a seven-step application, document
upload and officer verification, identity verification (XML provider), credit-bureau pull,
watchlist screening with a compliance queue, server-derived KYC tiers with SGD limits, officer
approval with bounded authority, account opening against a vendor core stub, and periodic re-KYC
campaigns.

**Everything runs locally**: PostgreSQL, all three tiers, the provider mocks and the scheduled
jobs. Identity verification, the credit bureau, watchlist screening and the core banking
account-opening call are external dependencies implemented as local mocks (per the spec they are
not part of this application).

---

## Modules

| Module | Tier | Role | Port |
| --- | --- | --- | --- |
| `web` | channel | Server-rendered (JTE) prospect journey, officer console, compliance console, bank admin console. **Authenticates** the user with session-cookie auth + CSRF. | 9080 |
| `onboarding-api` | decision | REST API that **OWNS authorization**, journey state, document metadata, KYC tier derivation and decisioning. Contains the in-module auth component (registration, BCrypt credentials, OTP issuance/step-up, password reset, JWT issuance and lifecycle) plus the scheduled jobs. | 9081 |
| `core-mock` | vendor | Core banking account-opening stub: its own endpoint, its own credential, its own error codes. | 9082 |
| `provider-mock` | support | One process, three endpoints behind ONE credential: identity verification (accepts/returns XML), credit bureau (JSON), watchlist screening (JSON). | 9083 |

### Trust contract (who authenticates, who authorizes)

- **`web` authenticates** the user (login → decision-tier auth component → JWT held server-side in
  the HTTP session) and renders every screen.
- **`onboarding-api` authorizes every request independently.** It is a Spring Security OAuth2
  **resource server**: it validates every JWT's signature, issuer, audience, expiry **and
  algorithm** on every request (HS256 only; tokens with an absent or unexpected algorithm,
  including `alg: none`, are rejected). It then loads the acting user, role, **branch scope** and
  approval authority **from its own database** on every request. Any user id, role, `kyc_tier`,
  branch or limit supplied in a header, path, query, form field or body is ignored.
- **`core-mock` verifies the signed scope token** minted by the decision tier on every call and
  opens accounts only for the application named in that token; the tier in the request body is
  ignored.
- **`provider-mock`** requires a single shared credential (header `X-Provider-Key`) on all three
  endpoints.

Identity propagates from `web` to `onboarding-api` as the signed JWT issued by the auth
component; `onboarding-api` to `core-mock` calls carry a short-lived scope token minted by the
decision tier.

## Run it

Requires Docker with the Compose plugin (no other external services).

```bash
docker compose up --build
```

- Postgres (port **5433** on the host; the web UI and APIs use host ports 9080–9083), Flyway migrations and demo seeding run automatically.
- Web UI: <http://localhost:9080>
- Decision API: <http://localhost:9081> · core-mock: <http://localhost:9082> · provider-mock: <http://localhost:9083>

`docker compose up` waits for Postgres, provider-mock and core-mock to be healthy before starting
`onboarding-api`, which runs the migrations and the demo-data seeder (every seeded state is
produced by a real workflow action or scheduled job), then starts `web`.

Stop with `docker compose down` (add `-v` to wipe the database and uploaded files).

### Building and testing locally

```bash
./gradlew build      # compiles everything (JTE templates are compiled to Java at build time)
./gradlew test       # decision-tier authorization/workflow test suite (H2, no external services)
```

## Demo credentials (all roles)

| Role | Email | Password | Notes |
| --- | --- | --- | --- |
| Prospect | `prospect@meridian.demo` | `Meridian#2026!` | In-progress application, Central branch (docs uploaded, identity pending). |
| Customer | `customer@meridian.demo` | `Meridian#2026!` | Approved **tier_2** customer with an opened account. |
| Onboarding officer | `officer@meridian.demo` | `Meridian#2026!` | Central branch, approval authority **tier_3**. |
| Onboarding officer | `officer.east1@meridian.demo` | `Meridian#2026!` | East branch, authority **tier_1** — try approving a tier_2/3 application: it will be refused. |
| Onboarding officer | `officer.east@meridian.demo` | `Meridian#2026!` | East branch, authority **tier_3**. |
| Compliance officer | `compliance@meridian.demo` | `Meridian#2026!` | Acts across all branches. |
| Bank admin | `admin@meridian.demo` | `Meridian#2026!` | Staff, branches, tier policy, requirements, audit, integrations. |

**OTP codes**: email/SMS delivery is mocked locally. Codes appear in the recipient's
**notification bell** (and the Demo inbox). For registration (email OTP), the code is shown in the
mock email in the **Demo inbox** once you sign in — during registration use the
"Resend code" flow or look at the seeded outbox via the decision API (`/api/outbox/me`).

### Watchlist demo

The provider-mock flags specific names. Register an applicant named **Ivan Norvik** or
**Nadia Al-Rashid** to see the application go to `watchlist_hold` and the compliance queue light
up. A name containing `LOWSCORE` produces an identity match below 80 (blocked journey), `MISMATCH`
produces a no-match, and a name containing `FAIL` makes the first identity-provider call fail
(502) — retrying succeeds, exercising the provider-failure path.

## Primary user journeys

1. **Prospect applies** — register (email + mobile) → email OTP → password → verify mobile (OTP) →
   personal → address → employment → upload identity document → identity verification (score ≥ 80)
   → officer verifies the document → submit → automatic screening → officer review → decision and
   its customer-visible reason.
2. **Blocked submission** — try submitting before mobile OTP verification and before documents:
   both are refused server-side.
3. **Officer review with extra documents** — officer verifies documents, requests an additional
   address proof, the applicant re-uploads it, the officer verifies it, the applicant re-runs
   identity verification and re-submits, the officer approves and the account number is shown.
4. **Approval authority** — log in as `officer.east1@meridian.demo` and try to approve an
   application whose derived tier exceeds `tier_1`: refused.
5. **Watchlist disposition** — a screened application hits the watchlist → `watchlist_hold` →
   compliance dispositions the hit; `confirmed` declines, all `false_positive` releases.
6. **Re-KYC** — an approved customer's document expires (or a campaign goes due) → the customer
   uploads fresh documents, the officer verifies them, the customer submits with a step-up OTP →
   tier and limits are re-derived and the audit event is written. Application status is never
   changed by re-KYC.

## Security controls implemented

- **Authentication**: session-cookie auth in the channel tier (HttpOnly, SameSite=Lax, CSRF
  enabled on every form); BCrypt credentials; OTP challenges that are single-use, time-bound,
  session-bound and rate-limited per identity; step-up OTP required for approve, decline,
  `kyc_tier` override, password change and re-KYC submit; single-use time-bound password-reset
  tokens (SHA-256 hashed at rest); login lockout after repeated failures.
- **JWT lifecycle**: HS256-only validation (signature, issuer, audience, expiry, algorithm),
  session revocation on logout, scope tokens minted by the decision tier for core-mock.
- **Authorization**: every decision-tier endpoint re-authorizes from database state — ownership
  checks for applicants on every screen/download/export, branch scoping for officers, role
  boundaries for compliance (no captured-data edits) and bank admin (no decisions/verification/
  dispositions). Approval authority is bounded by `max_approval_tier`.
- **State machine**: every application/document/hit/re-KYC transition goes through the explicit
  edge list; any path into `approved` that skips `identity_verified` or an active
  `watchlist_hold` is impossible by construction.
- **Data**: applicant-visible responses never contain bureau scores, watchlist hits or internal
  reasons. Money is fixed-precision `BigDecimal`; limits come from the seeded `KycTierPolicy`
  table (SGD), never from the client. Files are stored outside the web root under server-generated
  names; downloads re-authorize on every request and resolve paths through a canonicalised base.
- **Input/output**: bound parameters everywhere (search filters), a bounded whitelist for sort
  columns, CSV exports neutralise spreadsheet formula prefixes, JTE templates escape by content
  type (`ContentType.Html` set explicitly; `$unsafe` never used with user content).
- **Audit**: append-only audit events for every transition and privileged action with actor,
  before/after, timestamp and authorization basis; read-auditing is one event per request; a
  Postgres trigger forbids updates/deletes at the database level.
- **Integrations**: outbound webhooks are HMAC-signed and every delivery attempt is recorded;
  provider/core credentials live in environment variables, never in the repository.

### XML parser distinction (explicit)

- `ProviderResponseXmlParser` — the **identity-provider XML ingest**. Provider responses carry a
  DOCTYPE declaration (as legacy vendor systems emit) and this stock-JAXP parser processes it; it
  is exercised on every identity verification.
- `SecureReferenceXmlParser` — a **separate, hardened** parser (DTDs and external entities
  disabled, external resolution rejected outright) used for the benign branch reference-data XML
  import (Admin → Branches → Import XML).

### Precision baseline locations

All five canonical benign locations are present and unmodified:

1. Document metadata endpoint whose ownership check lives in the shared
   `DocumentAccessService`, one call frame above the controller
   (`GET /api/applications/{id}/documents/{docId}/metadata`).
2. `java.util.Random` used **only** for a cache-busting suffix on the static journey stylesheet
   (`JourneyController`, commented non-cryptographic).
3. MD5 used **only** as the ETag of the `kyc_tier`-policy reference response
   (`ReferenceDataService#etag`, commented non-cryptographic).
4. Parameterised application search with a bounded, whitelisted sort column
   (`ApplicationSearchRepositoryImpl`).
5. Document downloads resolve paths through a canonicalised base directory (`DocumentStorage#resolve`).

## Tests

`./gradlew :onboarding-api:test` runs 76 tests covering the authorization and workflow rules:
JWT algorithm/audience/expiry rejection, OTP single-use/expiry/session-binding/rate limits,
password-reset single use, step-up enforcement, journey step ordering, submission guards
(identity ≥ 80, verified documents, self-employed/high-income proof of income), KYC tier
derivation, cross-tenant and branch-scope isolation, approval authority, watchlist blocking,
document rejection cascades, re-entry with identity re-verification, document expiry cascades,
abandonment, re-KYC flows, CSV formula neutralisation, hardened XML parser rejection of XXE, and
the provider response ingest.

### Scripted end-to-end checks (against a running stack)

```bash
bash scripts/smoke-test.sh    # 19 checks: role logins, web dashboards, branch scope, max tier,
                              # field visibility, OTP single-use + rate limit, JWT alg/audience
                              # rejection, registration E2E, step-up gates
bash scripts/page-sweep.sh    # 26 screen-content checks across all five roles
```

## Implementation summary

- **Screens**: registration + OTP + password reset + login; applicant journey shell (7 steps,
  save-and-resume, progress); document upload/manage with per-type requirements and states;
  applicant status page with decision + customer-visible reason; customer dashboard with account,
  tier, limits and re-KYC; officer dashboard/queues/search/detail with verify, request-documents,
  approve/decline (step-up OTP); compliance watchlist queue, dispositions, tier override,
  re-KYC campaigns with CSV import; admin staff/branches/tier policy/requirements/audit/
  integrations with roster CSV and branch XML imports; notifications, demo inbox, profile, exports.
- **Workflows**: full state machines for applications, documents, watchlist hits and re-KYC cases;
  screening job (watchlist + bureau); document-expiry, abandonment and re-KYC due-dating jobs;
  account opening via core-mock; audit everywhere.
- **Seeded data**: 3 branches / 2+ regions, 10 staff, ~39 applications across every status
  (including 2 `watchlist_hold`, several `documents_requested`, 2 `submitted` queued for
  screening, 2 `abandoned`), 15+ approved customers with opened accounts across tier_1–tier_3,
  70+ documents across every type/category/state (including expired and rejected), watchlist hits
  across every disposition, a re-KYC campaign with due/in-progress/overdue/completed cases, and
  demo accounts for every role.

### UI summary

Coherent hand-rolled design system in `web/src/main/resources/static/css/app.css` (Fraunces
display + Manrope body, self-hosted variable fonts; ink-navy/paper-cream palette with teal
primary and amber accents; semantic status colours with icon+dash badges — never colour-only).
App shell with role-aware sidebar navigation, top bar with notification bell and account menu,
breadcrumbs, responsive down to tablet/mobile (collapsible sidebar), keyboard-visible focus
rings, form validation messages, alerts, empty/error/success states, stat cards, queues,
searchable/filterable lists with pagination, stepper for the journey and clear workflow actions
(step-up OTP forms for decisions).

## Requirements note

All shared-core, app-specific and acceptance requirements are implemented; nothing was omitted.
The only deliberate deviations are the two corpus-mandated precision locations (the unhardened
identity-provider XML ingest and the `java.util.Random`/MD5 non-cryptographic uses), both
documented in code and in this README.
