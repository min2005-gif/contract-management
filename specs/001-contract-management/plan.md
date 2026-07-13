# Implementation Plan: Centralized Contract Management System (VATM)

**Branch**: `001-contract-management` | **Date**: 2026-06-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-contract-management/spec.md`

## Summary

Build a centralized, on-premises contract management system for the Vietnam Air Traffic
Management Corporation (VATM): the parent corporation (TCT) plus 10 subordinate units, ~100
internal users. Subordinate units enter and own their contracts (with attachments); contracts
flow through a Create → Check → Approve → Track → Liquidate lifecycle with unit-head and TCT
approvals; TCT gets consolidated cross-unit reporting/dashboards with Excel/PDF export;
automatic alerts flag contracts that are nearing expiry, unsigned, unpaid, or behind schedule;
and the system integrates with VATM's internal e-document, digital-signature, and accounting
systems.

**Technical approach**: A Spring Boot REST backend on PostgreSQL serves a React web app and a
Flutter mobile app (Android/iOS). Authentication federates to VATM's existing SSO/Active
Directory (OIDC/SAML), mapping directory groups to application roles. Attachments are stored in
on-prem S3-compatible object storage (MinIO). A scheduled job evaluates alert conditions.
Reporting uses server-side Excel/PDF generation with client-side dashboard charts. Everything is
containerized for deployment inside VATM's data center.

## Technical Context

**Language/Version**: Backend Java 21 (Spring Boot 3.3); Web TypeScript 5 (React 18 + Vite);
Mobile Dart 3 (Flutter 3.x)

**Primary Dependencies**: Spring Web, Spring Security (OAuth2 Resource Server + SAML/LDAP
federation), Spring Data JPA, Flyway (migrations), Apache POI (Excel), JasperReports/OpenPDF
(PDF), Quartz/Spring Scheduling (alerts); React Query + Recharts (web); Dio + Riverpod (mobile)

**Storage**: PostgreSQL 16 (relational core; JSONB for the extensible/growing contract field
set); MinIO (S3-compatible object storage) for attachments

**Testing**: Backend JUnit 5 + Spring Boot Test + Testcontainers (PostgreSQL/MinIO); Web Vitest
+ React Testing Library + Playwright (E2E); Mobile `flutter test` + integration_test

**Target Platform**: On-premises Linux servers in VATM's data center (Docker containers);
web (modern browsers) and native Android/iOS clients

**Project Type**: Web application + mobile — backend API, web frontend, mobile app (three
deployables in one repository)

**Performance Goals**: Common contract pages and dashboard load < 3 s under normal load
(SC-009); consolidated cross-unit report generated and exported to Excel/PDF < 1 min (SC-003);
API p95 < 500 ms for standard reads/writes at the target scale

**Constraints**: On-premises only (no external public-service portal / national DB dependency,
FR-022); internal VATM users only (FR-005); Vietnamese-first UI; immutable audit log for all
mutations (FR-015); data resident in VATM's data center (FR-024)

**Scale/Scope**: 11 organizational scopes (TCT + 10 units), ~100 concurrent-capable users
(SC-007); tens of thousands of contracts over time; 5 contract types; 4 roles; 5 user stories

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Evaluated against Contract Constitution v1.0.0:

| Principle | How this plan satisfies it | Status |
|-----------|---------------------------|--------|
| I. Code Quality | Enforced formatting/linting per stack (Spotless + Checkstyle for Java, ESLint + Prettier for web, `dart analyze` for Flutter), all run in CI; single-responsibility service/repository layering; mandatory review before merge; public API + non-obvious decisions documented | PASS |
| II. Testing Standards (NON-NEGOTIABLE) | Critical paths (permissions/data isolation, approval workflow, alert evaluation, report totals) covered by automated tests; contract tests against the OpenAPI spec; integration tests via Testcontainers; regression test required per bug fix; deterministic, isolated tests | PASS |
| III. UX Consistency | One shared design system and terminology (Vietnamese) across web and mobile; consistent interaction patterns; actionable, human-readable error messages; accessibility baselines; versioned REST API with no silent breaking changes | PASS |
| IV. Performance Requirements | Measurable targets defined in Technical Context and Success Criteria; hot paths (list/search, report generation) benchmarked with performance gates in CI; measurement-driven optimization; pagination + indexing + defined behavior under concurrent load | PASS |

**Post-design re-check**: Design in Phase 1 (data model, contracts, quickstart) introduces no new
constitutional violations. Layered architecture, versioned `/api/v1` contracts, and test-first
critical paths keep all four principles satisfied. **PASS** — no entries required in Complexity
Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-contract-management/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output — stack decisions & rationale
├── data-model.md        # Phase 1 output — entities, fields, relationships, state machine
├── quickstart.md        # Phase 1 output — run & validation guide
├── contracts/           # Phase 1 output — REST API contract (OpenAPI)
│   ├── README.md
│   └── openapi.yaml
├── checklists/
│   └── requirements.md  # Spec quality checklist (/speckit-specify output)
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

```text
backend/                        # Spring Boot REST API (Java 21)
├── src/main/java/vn/vatm/contract/
│   ├── config/                 # Security (SSO/OIDC/SAML), CORS, storage, scheduling
│   ├── org/                    # Organizational units & user/role management
│   ├── contract/               # Contract aggregate: entity, repository, service, controller
│   ├── attachment/             # Attachment upload/download (MinIO)
│   ├── workflow/               # Approval lifecycle & state transitions
│   ├── alert/                  # Alert rules + scheduled evaluation + notifications
│   ├── report/                 # Consolidated reports + Excel/PDF export
│   ├── integration/            # E-document, digital signature, accounting adapters
│   └── audit/                  # Immutable audit log
├── src/main/resources/db/migration/   # Flyway migrations
└── src/test/java/...           # JUnit 5 + Testcontainers (contract, integration, unit)

frontend/                       # React 18 + TypeScript web app (Vite)
├── src/
│   ├── api/                    # Generated API client from openapi.yaml
│   ├── components/             # Shared design-system components
│   ├── features/               # contracts, approvals, reports, alerts, admin
│   └── pages/
└── tests/                      # Vitest + Testing Library + Playwright E2E

mobile/                         # Flutter app (Android/iOS)
├── lib/
│   ├── api/
│   ├── features/               # contracts, approvals, alerts
│   └── shared/                 # design system, theming
└── test/                       # flutter test + integration_test

deploy/                         # Docker Compose / container manifests for VATM data center
```

**Structure Decision**: Web application + mobile. Three deployables — `backend/` (Spring Boot
API and the single source of business logic, permissions, workflow, and reporting), `frontend/`
(React web client), and `mobile/` (Flutter client) — share one versioned REST contract defined in
`contracts/openapi.yaml`. Business rules live only in the backend so web and mobile stay thin and
consistent (Principle III). `deploy/` holds on-prem container manifests.

## Complexity Tracking

No constitutional violations — this section is intentionally empty.
