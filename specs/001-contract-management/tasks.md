---
description: "Task list for Centralized Contract Management System (VATM)"
---

# Tasks: Centralized Contract Management System (VATM)

**Input**: Design documents from `/specs/001-contract-management/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Test tasks ARE included. Constitution v1.0.0 Principle II (Testing Standards) is
NON-NEGOTIABLE and mandates automated coverage for critical paths (permissions/data isolation,
approval workflow, alert evaluation, report reconciliation) plus contract and integration tests,
and Principle III covers the web/mobile UX surfaces. Tests for a story are written before/alongside
its implementation and MUST fail first.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1–US5 map to the spec's user stories
- Exact file paths are included in each task

## Path Conventions

- Backend (Spring Boot): `backend/src/main/java/vn/vatm/contract/`, migrations in
  `backend/src/main/resources/db/migration/`, tests in `backend/src/test/java/vn/vatm/contract/`
- Web (React): `frontend/src/`, tests in `frontend/tests/`
- Mobile (Flutter): `mobile/lib/`, tests in `mobile/test/`
- Deploy: `deploy/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Repository, three deployables, tooling, and CI.

- [X] T001 Create the monorepo structure (`backend/`, `frontend/`, `mobile/`, `deploy/`, `specs/` already present) per plan.md Project Structure
- [X] T002 Initialize the Spring Boot backend in `backend/` (Gradle, Java 21, Spring Boot 3.3 with Web, Security, Data JPA, Flyway, Actuator dependencies)
- [X] T003 [P] Initialize the React + TypeScript web app in `frontend/` (Vite, React 18, React Query, Recharts)
- [ ] T004 [P] Initialize the Flutter app in `mobile/` (Flutter 3.x, Dio, Riverpod)
- [X] T005 [P] Create `deploy/docker-compose.dev.yml` with PostgreSQL 16, MinIO, and a dev Keycloak (SSO/AD stand-in)
- [X] T006 [P] Configure backend formatting/linting (Spotless + Checkstyle) with a Gradle `check` task in `backend/build.gradle`
- [ ] T007 [P] Configure web linting/formatting (ESLint + Prettier) and test tooling (Vitest + Testing Library + Playwright) in `frontend/`
- [ ] T008 [P] Configure Flutter static analysis in `mobile/analysis_options.yaml` and test tooling (`flutter test` + `integration_test`) in `mobile/`
- [ ] T009 [P] Create CI pipeline in `.github/workflows/ci.yml` running lint, backend/web/mobile tests, and a performance smoke gate

**Checkpoint**: Repo builds, lints, and runs empty test suites on all three stacks; dev infra comes up via Docker Compose.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Auth, permissions, base entities, storage, logging — required before ANY user story.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T010 Configure Flyway and create the baseline migration in `backend/src/main/resources/db/migration/V1__baseline.sql` (extensions, schema)
- [X] T011 [P] Implement OrganizationalUnit entity + repository in `backend/src/main/java/vn/vatm/contract/org/` and migration `V2__org_units.sql`; seed TCT + 10 subordinate units
- [X] T012 [P] Implement User + Role/UserRole entities + repositories in `backend/src/main/java/vn/vatm/contract/org/` and migration `V3__users_roles.sql`
- [X] T013 Configure Spring Security as an OAuth2/OIDC resource server (SAML/LDAP fallback) with SSO/AD federation in `backend/src/main/java/vn/vatm/contract/config/SecurityConfig.java`; provision users from token claims
- [X] T014 Implement directory-group → role mapping and just-in-time user provisioning in `backend/src/main/java/vn/vatm/contract/config/GroupRoleMapper.java` (retain a break-glass local admin)
- [X] T015 Implement RBAC + owning-unit scoping enforcement (method security + a query filter) in `backend/src/main/java/vn/vatm/contract/config/AccessControl.java`
- [X] T016 [P] Implement the immutable **business** AuditLogEntry entity + repository + service-layer emission (FR-015) in `backend/src/main/java/vn/vatm/contract/audit/` and migration `V4__audit_log.sql`
- [X] T017 [P] Implement the **security/operations** log (FR-025) — auth events, failed access, role changes, downloads, exports, admin actions — in `backend/src/main/java/vn/vatm/contract/audit/SecurityEventLogger.java` and migration `V5__security_log.sql`
- [X] T018 [P] Configure MinIO client + storage abstraction (upload/download/delete, key strategy) in `backend/src/main/java/vn/vatm/contract/config/StorageConfig.java`
- [X] T019 [P] Implement RFC 7807 `application/problem+json` global exception handling with Vietnamese-first messages (incl. validation, conflict, and forbidden mappings) in `backend/src/main/java/vn/vatm/contract/config/ProblemHandler.java`
- [X] T020 Wire `/api/v1` versioning + generate the OpenAPI doc from `contracts/openapi.yaml` (springdoc) in `backend/src/main/java/vn/vatm/contract/config/OpenApiConfig.java`
- [X] T021 [P] Build the web app shell in `frontend/src/` (OIDC login flow, protected routing, generated API client from `contracts/openapi.yaml`, Vietnamese i18n, shared design-system components)
- [ ] T022 [P] Build the mobile app shell in `mobile/lib/` (OIDC login, generated API client, shared theme/design system, Vietnamese i18n)
- [X] T023 Set up the backend test harness (JUnit 5 + Spring Boot Test + Testcontainers for PostgreSQL/MinIO) in `backend/src/test/java/vn/vatm/contract/support/`

**Checkpoint**: A federated user can authenticate, roles/units resolve, permissions, business audit,
and security logging are enforced, storage works — user stories can now begin.

---

## Phase 3: User Story 1 - Enter and manage a unit's contracts (Priority: P1) 🎯 MVP

**Goal**: A data-entry user records, edits, and attaches files to their unit's contracts; data is
unit-scoped, contract numbers are unique per unit, and attachments are type/size validated.

**Independent Test**: Create a contract with all required fields + one attachment, retrieve/edit
it, download the attachment; a different unit's user gets 403; a duplicate contract number in the
same unit is rejected.

### Tests for User Story 1 ⚠️ (write first, must fail)

- [X] T024 [P] [US1] Contract test for `/contracts` POST/GET/PUT against `contracts/openapi.yaml` in `backend/src/test/java/vn/vatm/contract/contract/ContractApiContractTest.java`
- [X] T025 [P] [US1] Integration test for owning-unit scoping (cross-unit access → 403) in `backend/src/test/java/vn/vatm/contract/contract/ContractScopingTest.java`
- [X] T026 [P] [US1] Integration test for required-field validation (FR-011), per-unit contract-number uniqueness (FR-026), and optimistic-concurrency conflict on concurrent edit in `backend/src/test/java/vn/vatm/contract/contract/ContractValidationTest.java`
- [X] T027 [P] [US1] Integration test for attachment upload/download plus type/size-limit rejection (FR-010) in `backend/src/test/java/vn/vatm/contract/attachment/AttachmentTest.java`
- [X] T028 [P] [US1] Web test — contract form validation, list rendering, and unit-scoped visibility (Vitest + Testing Library, Playwright E2E) in `frontend/tests/contracts.spec.ts`
- [ ] T029 [P] [US1] Mobile test — contract create/list widget + integration test in `mobile/test/contracts_test.dart`

### Implementation for User Story 1

- [X] T030 [P] [US1] Create the Contract entity (incl. `extra_fields` JSONB, `@Version` optimistic-lock column, indexes, unique `(owning_unit_id, contract_number)`) + migration `V6__contracts.sql` in `backend/src/main/java/vn/vatm/contract/contract/Contract.java`
- [X] T031 [P] [US1] Create the Attachment entity + migration `V7__attachments.sql` in `backend/src/main/java/vn/vatm/contract/attachment/Attachment.java`
- [X] T032 [US1] Implement ContractRepository with unit-scoped queries + free-text search in `backend/src/main/java/vn/vatm/contract/contract/ContractRepository.java`
- [X] T033 [US1] Implement ContractService (create as DRAFT, update, list/search, required-field validation, per-unit number uniqueness, optimistic concurrency, unit scoping, business-audit emit) in `backend/src/main/java/vn/vatm/contract/contract/ContractService.java`
- [X] T034 [US1] Implement AttachmentService (MinIO put/get/delete, content-type allowlist + max-size validation per FR-010) in `backend/src/main/java/vn/vatm/contract/attachment/AttachmentService.java`
- [X] T035 [US1] Implement ContractController (`/contracts` CRUD + list/search) in `backend/src/main/java/vn/vatm/contract/contract/ContractController.java`
- [X] T036 [US1] Implement AttachmentController (`/contracts/{id}/attachments` upload/list/download/delete; downloads emit a security-log event) in `backend/src/main/java/vn/vatm/contract/attachment/AttachmentController.java`
- [X] T037 [P] [US1] Web: contract list + search page in `frontend/src/features/contracts/ContractListPage.tsx`
- [X] T038 [P] [US1] Web: contract create/edit form with client + server validation (incl. duplicate-number and conflict messages) in `frontend/src/features/contracts/ContractForm.tsx`
- [X] T039 [P] [US1] Web: attachment upload/download component with type/size feedback in `frontend/src/features/contracts/Attachments.tsx`
- [ ] T040 [P] [US1] Mobile: contract list, create/edit, and attachment screens in `mobile/lib/features/contracts/`

**Checkpoint**: US1 fully functional and independently testable — a unit can maintain its contract
register with validated attachments (delivers the MVP).

---

## Phase 4: User Story 2 - Review and approve contracts (Priority: P1)

**Goal**: Contracts flow Create → Check → Approve (unit head, then TCT for official) → Track →
Liquidate, with reject-with-reason and full audit.

**Independent Test**: Submit a draft, approve as unit head, approve as TCT; status advances and is
logged; reject returns it to DRAFT with a visible reason; the official/threshold rule (FR-013) gates
TCT approval.

### Tests for User Story 2 ⚠️ (write first, must fail)

- [X] T041 [P] [US2] State-machine + transition-legality tests (illegal transition → 409) in `backend/src/test/java/vn/vatm/contract/workflow/WorkflowStateMachineTest.java`
- [X] T042 [P] [US2] Role/workflow tests: unit-head check, official/threshold rule requires TCT approval (FR-013), reject returns to DRAFT, business audit recorded in `backend/src/test/java/vn/vatm/contract/workflow/WorkflowRulesTest.java`
- [ ] T043 [P] [US2] Web E2E test — review queue approve/reject-with-reason and TCT approval flow in `frontend/tests/approvals.spec.ts`
- [ ] T044 [P] [US2] Mobile test — approval queue approve/reject actions in `mobile/test/approvals_test.dart`

### Implementation for User Story 2

- [X] T045 [P] [US2] Create the WorkflowStep entity + migration `V8__workflow_steps.sql` in `backend/src/main/java/vn/vatm/contract/workflow/WorkflowStep.java`
- [X] T046 [US2] Implement WorkflowService (state machine, configurable official/threshold rule per FR-013, reject reason, status updates, business-audit emit) in `backend/src/main/java/vn/vatm/contract/workflow/WorkflowService.java`
- [X] T047 [US2] Implement WorkflowController (`POST /contracts/{id}/workflow`) in `backend/src/main/java/vn/vatm/contract/workflow/WorkflowController.java`
- [X] T048 [P] [US2] Web: unit-head review queue with approve/reject (reason) in `frontend/src/features/approvals/ReviewQueue.tsx`
- [X] T049 [P] [US2] Web: TCT approval view for official contracts in `frontend/src/features/approvals/TctApproval.tsx`
- [X] T050 [P] [US2] Web: contract detail status timeline (WorkflowStep history) in `frontend/src/features/contracts/StatusTimeline.tsx`
- [ ] T051 [P] [US2] Mobile: approval queue + approve/reject actions in `mobile/lib/features/approvals/`

**Checkpoint**: US1 + US2 work together — contracts can be recorded and driven through official
approval.

---

## Phase 5: User Story 3 - Consolidated oversight and reporting for TCT (Priority: P2)

**Goal**: TCT/management see all contracts and consolidated reports (totals, nearing-expiry,
in-progress, per-unit), export to Excel/PDF, and view dashboard charts.

**Independent Test**: Open the consolidated summary; per-unit figures reconcile to totals; export
xlsx and pdf matching on-screen figures.

### Tests for User Story 3 ⚠️ (write first, must fail)

- [X] T052 [P] [US3] Report reconciliation test — per-unit sums equal totals (SC-004) + access control (TCT/management only) in `backend/src/test/java/vn/vatm/contract/report/ReportSummaryTest.java`
- [X] T053 [P] [US3] Export test — xlsx and pdf generated with figures matching the summary in `backend/src/test/java/vn/vatm/contract/report/ReportExportTest.java`
- [ ] T054 [P] [US3] Web E2E test — dashboard renders per-unit breakdown + charts and triggers xlsx/pdf export in `frontend/tests/reports.spec.ts`

### Implementation for User Story 3

- [X] T055 [US3] Implement ReportService aggregate queries (totals, nearing-expiry, in-progress, per-unit) in `backend/src/main/java/vn/vatm/contract/report/ReportService.java`
- [X] T056 [US3] Implement ReportController (`GET /reports/summary`) in `backend/src/main/java/vn/vatm/contract/report/ReportController.java`
- [X] T057 [P] [US3] Implement Excel export (Apache POI) in `backend/src/main/java/vn/vatm/contract/report/ExcelExporter.java`
- [X] T058 [P] [US3] Implement PDF export (JasperReports/OpenPDF) in `backend/src/main/java/vn/vatm/contract/report/PdfExporter.java`
- [X] T059 [US3] Implement export endpoint (`GET /reports/export?format=`; emits a security-log export event) in `backend/src/main/java/vn/vatm/contract/report/ReportController.java`
- [X] T060 [US3] Web: consolidated dashboard with Recharts charts, per-unit breakdown, and export buttons in `frontend/src/features/reports/Dashboard.tsx`

**Checkpoint**: Leadership gets cross-unit visibility and exportable reports.

---

## Phase 6: User Story 4 - Automatic contract alerts (Priority: P2)

**Goal**: The system flags and notifies on contracts nearing expiry, unsigned, unpaid, or behind
schedule, using the configurable definitions in FR-016.

**Independent Test**: Seed contracts matching each condition, run the evaluation, confirm one OPEN
alert per condition (idempotent) and notification to the person in charge.

### Tests for User Story 4 ⚠️ (write first, must fail)

- [X] T061 [P] [US4] Alert evaluation tests for all four conditions (using default thresholds) + idempotency (no duplicate OPEN) in `backend/src/test/java/vn/vatm/contract/alert/AlertEvaluationTest.java`
- [X] T062 [P] [US4] Notification routing test — alert goes to the contract's person in charge in `backend/src/test/java/vn/vatm/contract/alert/AlertNotificationTest.java`
- [ ] T063 [P] [US4] Web test — alerts list rendering + navbar badge in `frontend/tests/alerts.spec.ts`
- [ ] T064 [P] [US4] Mobile test — alerts list in `mobile/test/alerts_test.dart`

### Implementation for User Story 4

- [X] T065 [P] [US4] Create the Alert entity + migration `V9__alerts.sql` with a unique partial index on `(contract_id, type)` where `status='OPEN'` in `backend/src/main/java/vn/vatm/contract/alert/Alert.java`
- [X] T066 [US4] Implement AlertEvaluationService (four configurable rules per FR-016, idempotent raise/update) in `backend/src/main/java/vn/vatm/contract/alert/AlertEvaluationService.java`
- [X] T067 [US4] Implement the scheduled evaluation job + dev-only trigger endpoint in `backend/src/main/java/vn/vatm/contract/alert/AlertScheduler.java`
- [X] T068 [P] [US4] Implement NotificationService (in-app + email to person in charge) in `backend/src/main/java/vn/vatm/contract/alert/NotificationService.java`
- [X] T069 [US4] Implement AlertController (`GET /alerts`, `PATCH /alerts/{id}` acknowledge/resolve) in `backend/src/main/java/vn/vatm/contract/alert/AlertController.java`
- [X] T070 [P] [US4] Web: alerts list + navbar badge in `frontend/src/features/alerts/AlertsPage.tsx`
- [ ] T071 [P] [US4] Mobile: alerts list (+ push notification hook) in `mobile/lib/features/alerts/`

**Checkpoint**: Proactive risk alerts are delivered to responsible users.

---

## Phase 7: User Story 5 - Integration with internal systems (Priority: P3)

**Goal**: Integrate digital signature, internal e-document, and accounting; no external
public-service/national-DB dependency.

**Independent Test**: Trigger digital signing (records signed status/document); a failed
integration returns 502 with no partial state; accounting reconcile returns a result.

### Tests for User Story 5 ⚠️ (write first, must fail)

- [X] T072 [P] [US5] Digital-signature adapter tests — success records status; failure → 502, no partial state in `backend/src/test/java/vn/vatm/contract/integration/SignatureIntegrationTest.java`
- [X] T073 [P] [US5] Accounting-reconcile test + guard that no external portal/national-DB calls occur (FR-022) in `backend/src/test/java/vn/vatm/contract/integration/AccountingIntegrationTest.java`
- [ ] T074 [P] [US5] Web test — sign / reconcile actions on the contract detail page in `frontend/tests/integrations.spec.ts`

### Implementation for User Story 5

- [X] T075 [P] [US5] Define integration adapter interfaces + configuration in `backend/src/main/java/vn/vatm/contract/integration/adapters/`
- [X] T076 [US5] Implement the digital-signature adapter + `POST /integrations/contracts/{id}/sign` in `backend/src/main/java/vn/vatm/contract/integration/SignatureController.java`
- [X] T077 [P] [US5] Implement e-document linking (reference/link a related document) in `backend/src/main/java/vn/vatm/contract/integration/EDocumentService.java`
- [X] T078 [US5] Implement the accounting adapter + `POST /integrations/contracts/{id}/accounting/reconcile` in `backend/src/main/java/vn/vatm/contract/integration/AccountingController.java`
- [X] T079 [P] [US5] Web: sign / reconcile actions on the contract detail page in `frontend/src/features/contracts/IntegrationActions.tsx`

**Checkpoint**: All user stories complete and independently functional.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Performance, accessibility, security, and docs across all stories.

- [ ] T080 [P] Performance: verify pagination/indexing and run a k6 load test to confirm API p95 < 500 ms at ~100 users (SC-007) and page/dashboard load < 3 s (SC-009) in `backend/perf/api-load.js`
- [ ] T081 [P] Performance: benchmark consolidated report generation + Excel/PDF export to confirm < 1 min (SC-003) in `backend/perf/report-export-bench.js`
- [ ] T082 [P] Accessibility pass (keyboard nav, contrast, screen-reader labels) on web (`frontend/`) and mobile (`mobile/`)
- [ ] T083 [P] Admin query UI for the business audit log (`GET /audit`) and the security/operations log in `frontend/src/features/admin/AuditLog.tsx`
- [ ] T084 [P] Vietnamese i18n completeness review across `frontend/src/` and `mobile/lib/`
- [ ] T085 [P] Security hardening review (authz coverage, upload validation, secrets, data-residency assertion per FR-024) and document findings in `deploy/SECURITY.md`
- [ ] T086 [P] Write the deployment guide (on-prem Docker) in `deploy/README.md` and update root `README.md`
- [ ] T087 Run the `quickstart.md` validation end-to-end and confirm all Success Criteria, explicitly measuring create-and-save < 5 min (SC-001) and export < 1 min (SC-003)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories.
- **User Stories (Phases 3–7)**: All depend on Foundational.
  - US2 (approval) builds on US1's Contract entity/service.
  - US3 (reports) and US4 (alerts) depend on US1 data; US4 also uses US2's status/payment fields.
  - US5 (integrations) depends on US1 (contract) and benefits from US2 (status).
  - Recommended order follows priority: US1 → US2 → US3 → US4 → US5.
- **Polish (Phase 8)**: Depends on the targeted user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Foundational only — the base everything else builds on.
- **US2 (P1)**: Needs US1 (Contract). Independently testable once US1 exists.
- **US3 (P2)**: Needs US1 data. Independent of US2/US4.
- **US4 (P2)**: Needs US1; uses status/payment/progress fields (US2 enriches status). Independently testable.
- **US5 (P3)**: Needs US1 (+US2 status). Independently testable.

### Within Each User Story

- Tests (backend + web + mobile) are written first and MUST fail before implementation.
- Entities/migrations → repository → service → controller → web/mobile UI.
- Story complete before moving to the next priority.

### Parallel Opportunities

- All Setup tasks marked [P] (T003–T009) run in parallel after T001–T002.
- Foundational [P] tasks (T011, T012, T016, T017, T018, T019, T021, T022) run in parallel where they touch different files.
- Within each story, [P] test/entity/UI tasks run in parallel; backend service/controller tasks on the same files are sequential.
- Web and mobile test + UI tasks for a story run in parallel with each other and with unrelated backend polish.

---

## Parallel Example: User Story 1

```bash
# Write the failing tests together (backend + web + mobile):
Task: "T024 Contract API contract test in backend/src/test/.../ContractApiContractTest.java"
Task: "T025 Unit-scoping integration test in backend/src/test/.../ContractScopingTest.java"
Task: "T026 Validation + uniqueness + concurrency test in backend/src/test/.../ContractValidationTest.java"
Task: "T027 Attachment type/size test in backend/src/test/.../AttachmentTest.java"
Task: "T028 Web contracts spec in frontend/tests/contracts.spec.ts"
Task: "T029 Mobile contracts test in mobile/test/contracts_test.dart"

# Then create entities in parallel:
Task: "T030 Contract entity + migration V6__contracts.sql"
Task: "T031 Attachment entity + migration V7__attachments.sql"

# UI tasks in parallel once the API is up:
Task: "T037 Web contract list page"
Task: "T038 Web contract form"
Task: "T039 Web attachments component"
Task: "T040 Mobile contract screens"
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Phase 1: Setup → 2. Phase 2: Foundational (CRITICAL) → 3. Phase 3: US1.
4. **STOP and VALIDATE**: one unit maintains its contract register with validated attachments and unit scoping.
5. Deploy the Phase 1 pilot (single unit) per SC-008.

### Incremental Delivery

1. Setup + Foundational → foundation ready.
2. Add US1 → validate → pilot (MVP).
3. Add US2 (approval) → validate → deploy.
4. Add US3 (reporting) and US4 (alerts) → validate → deploy.
5. Add US5 (integrations) → validate → full rollout (Phase 2, all units).

### Parallel Team Strategy

After Foundational, with the backend contract in place, split: backend service work, web UI, and
mobile UI can proceed per story in parallel; US3 and US4 can be staffed concurrently once US1 lands.

---

## Notes

- [P] = different files, no incomplete dependencies.
- [Story] label maps each task to its user story for traceability.
- Tests are required for critical paths and web/mobile surfaces per Constitution Principles II & III (verify they fail first).
- Every mutating task must emit a business-audit entry (FR-015); sensitive access (downloads, exports, admin) emits a security-log event (FR-025) — both covered by Foundational services (T016, T017).
- Commit after each task or logical group; stop at any checkpoint to validate a story independently.
