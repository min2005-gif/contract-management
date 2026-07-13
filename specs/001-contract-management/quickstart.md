# Quickstart & Validation Guide: VATM Contract Management

This guide brings up the system locally and validates each user story end-to-end. It is a
run/validation guide — implementation details live in `tasks.md` and the code. See
[data-model.md](./data-model.md) and [contracts/openapi.yaml](./contracts/openapi.yaml) for schema
and endpoint details.

## Prerequisites

- Docker + Docker Compose
- Java 21 (JDK), Node 20+, Flutter 3.x SDK
- An OIDC test identity (local Keycloak container stands in for VATM SSO/AD in development)

## Bring up the stack (local)

```bash
# From repo root — starts PostgreSQL, MinIO, and a dev Keycloak (SSO stand-in)
docker compose -f deploy/docker-compose.dev.yml up -d

# Backend (applies Flyway migrations on boot, seeds TCT + sample units/roles)
cd backend && ./gradlew bootRun            # API on http://localhost:8080/api/v1

# Web
cd frontend && npm install && npm run dev  # http://localhost:5173

# Mobile (optional, against a running emulator/device)
cd mobile && flutter run
```

Health check: `curl http://localhost:8080/api/v1/health` → `200 OK`.

## Validation scenarios (map to spec user stories & success criteria)

### US1 — Enter and manage a unit's contracts (P1)

1. Sign in as a **DATA_ENTRY** user of a subordinate unit.
2. Create a contract with all required fields; upload one PDF attachment.
3. **Expected**: contract saved as `DRAFT`, owned by that unit, appears in its list; attachment
   downloads back intact. (SC-001: achievable in < 5 min.)
4. Sign in as a **DATA_ENTRY** user of a *different* unit and request the contract by id.
   **Expected**: `403` — no cross-unit access. (SC-002.)
5. Attempt to save with a required field blank. **Expected**: `400` naming the missing field.

### US2 — Review and approve (P1)

1. As the owner, `SUBMIT` the draft. **Expected**: status `PENDING_CHECK`.
2. As **UNIT_HEAD**, `CHECK_APPROVE`. **Expected**: `PENDING_TCT_APPROVAL` if `isOfficial`, else
   `ACTIVE`.
3. As a **TCT** approver, `TCT_APPROVE`. **Expected**: `ACTIVE`; a `WorkflowStep` and an
   `AuditLogEntry` are recorded with actor + timestamp. (SC-006.)
4. `REJECT` with a reason from any review step. **Expected**: back to `DRAFT`, reason visible to
   the owning unit.

### US3 — Consolidated oversight & reporting (P2)

1. As **MANAGEMENT/TCT**, open `GET /reports/summary`.
2. **Expected**: totals (count, value), nearing-expiry, in-progress, and per-unit breakdown; the
   sum of per-unit figures equals the totals. (SC-004.)
3. `GET /reports/export?format=xlsx` and `?format=pdf`. **Expected**: files whose figures match the
   summary, produced in < 1 min. (SC-003.) Dashboard renders charts from the same data.

### US4 — Automatic alerts (P2)

1. Seed contracts matching each condition: `termEnd` within the warning window; unsigned past
   `signDate`; `paymentStatus=UNPAID`; `progressPct < expectedProgressPct`.
2. Run the alert job (`POST` dev-only trigger, or wait for the schedule).
3. **Expected**: one `OPEN` alert per condition per contract (idempotent — re-running creates no
   duplicates), each notified to the person in charge and listed under `GET /alerts`. (SC-005.)

### US5 — Integrations (P3)

1. `POST /integrations/contracts/{id}/sign`. **Expected**: signed status/document recorded; if the
   signature service is down, `502` with a problem detail and no partial state.
2. `POST /integrations/contracts/{id}/accounting/reconcile`. **Expected**: reconciliation result
   returned; no external public-service/national-DB calls are made (FR-022).

## Automated test entry points

```bash
cd backend && ./gradlew test        # JUnit 5 + Testcontainers: permissions, workflow, alerts, reports
cd frontend && npm test             # Vitest + Testing Library
cd frontend && npm run test:e2e     # Playwright end-to-end
cd mobile && flutter test           # widget + integration tests
```

## Performance smoke check

- Load the contract list and dashboard with the seeded dataset; both should render in < 3 s
  (SC-009). A backend load test (e.g., k6) validates API p95 < 500 ms at ~100 users (SC-007).

## Done when

All five scenario groups pass, cross-unit access is denied, audit entries exist for every
mutation, and report totals reconcile — matching the Success Criteria in [spec.md](./spec.md).
