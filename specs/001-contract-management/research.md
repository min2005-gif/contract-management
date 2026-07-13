# Phase 0 Research: Centralized Contract Management System (VATM)

The source requirements document is deliberately technology-agnostic ("Nền tảng: Web/ứng dụng di
động", on-prem server, SSO). The stack decisions below were confirmed with the stakeholder and are
recorded here with rationale and alternatives. Items the spec left configurable are also resolved
with sensible defaults, flagged where VATM confirmation is still advisable.

## Decision 1: Backend framework — Java 21 + Spring Boot 3.3

- **Decision**: Spring Boot (Spring Web, Spring Security, Spring Data JPA, Flyway).
- **Rationale**: Dominant in Vietnamese enterprise/government environments; mature on-prem
  deployment story; first-class support for RBAC, OIDC/SAML/LDAP federation, PKI/digital-signature
  integration, and enterprise reporting (Apache POI/JasperReports). Large hiring pool for VATM.
- **Alternatives considered**: .NET (excellent, chosen against unless the data center is
  Microsoft-centric); Node/NestJS (lighter but weaker enterprise-integration ecosystem);
  Python/Django (weaker PKI and heavy-Excel story).

## Decision 2: Database — PostgreSQL 16

- **Decision**: PostgreSQL as the system of record; core relational schema plus a JSONB column for
  the extensible/growing contract field set (FR-009).
- **Rationale**: Strong relational integrity for contracts, approvals, and audit; JSONB cleanly
  absorbs "additional fields to be provided later" without schema churn; excellent on-prem
  operations and free licensing.
- **Alternatives considered**: SQL Server (fine if Microsoft-centric); Oracle (common in VN SOEs
  but heavy/licensed); MySQL/MariaDB (weaker JSON + indexing story for the extensible fields).

## Decision 3: Mobile — Flutter 3 (single codebase, Android + iOS)

- **Decision**: Flutter for both native mobile platforms.
- **Rationale**: One codebase for Android + iOS (FR-023) at near-native performance minimizes
  effort for an internal ~100-user app; good offline/caching and push-notification support for
  alerts.
- **Alternatives considered**: React Native (viable, chosen against to keep mobile independent of
  web tooling); PWA-only (defers native alert push); native Kotlin+Swift (≈2× effort, unjustified).

## Decision 4: Authentication/SSO — federate to VATM's existing SSO / Active Directory

- **Decision**: The application is an OAuth2/OIDC resource server (with SAML fallback) that trusts
  VATM's existing identity provider / Active Directory. Directory groups map to the four
  application roles; no local passwords for normal users.
- **Rationale**: Delivers true organization-wide single sign-on (FR-006) and centralizes account
  lifecycle in VATM IT; satisfies "internal users only" (FR-005). Role mapping keeps authorization
  in the app while identity stays with the IdP.
- **Open item (confirm with VATM)**: Exact IdP protocol and endpoints (OIDC vs SAML vs direct
  LDAP/AD bind) and the group→role mapping. A break-glass local admin account is retained for
  bootstrap and IdP-outage recovery.
- **Alternatives considered**: Stand up Keycloak (fallback if no reusable IdP exists); custom
  built-in auth (rejected — not real SSO).

## Decision 5: Attachment storage — MinIO (S3-compatible, on-prem)

- **Decision**: Store attachment files (PDF, Word, scans, appendices — FR-010) in MinIO; keep only
  metadata + object keys in PostgreSQL.
- **Rationale**: Keeps large binaries out of the database, supports streaming up/download and
  lifecycle policies, deploys on-prem in VATM's data center (FR-024), S3 API is well supported.
- **Alternatives considered**: Bytea in PostgreSQL (bloats DB/backups); plain filesystem (harder
  to scale/replicate). Allowed types/size limits are validated server-side.

## Decision 6: Reporting & export — Apache POI (Excel) + JasperReports/OpenPDF (PDF) + client charts

- **Decision**: Server generates Excel and PDF exports; the web dashboard renders charts client
  side (Recharts) from the same aggregated data (FR-018–FR-020).
- **Rationale**: POI/Jasper are the enterprise-standard, template-friendly Java libraries; server
  side export guarantees identical figures across web/mobile and meets the < 1 min target (SC-003).
- **Alternatives considered**: Client-only export (inconsistent, memory-bound); a separate BI tool
  (overkill for the fixed report set).

## Decision 7: Alert evaluation — scheduled batch job

- **Decision**: A scheduled job (Spring Scheduling/Quartz) periodically evaluates the four alert
  conditions (nearing expiry, unsigned, unpaid, behind schedule) and raises/updates alerts plus
  notifications to responsible users (FR-016–FR-017).
- **Rationale**: Alert conditions are time-based and don't need real-time streaming; a daily/hourly
  batch is simple, testable, and idempotent. Thresholds are configurable (per Assumptions).
- **Open item (confirm with VATM)**: Alert windows (e.g., "nearing expiry" = 30 days) and the
  definition of "unpaid"/"behind schedule" against tracked progress and payment data.
- **Alternatives considered**: Event-driven/real-time evaluation (unnecessary complexity for
  date-based rules).

## Decision 8: Audit log — append-only table

- **Decision**: Every create/edit/status-change/approval/rejection writes an immutable audit
  record (actor, action, entity, before/after summary, timestamp) (FR-015, FR-025).
- **Rationale**: Simple, queryable, and sufficient for internal accountability and security review;
  write path is enforced in the service layer so no mutation bypasses it.
- **Alternatives considered**: External SIEM only (still keep an in-app trail); DB triggers
  (harder to test and reason about than service-layer emission).

## Decision 9: Deployment — Docker containers in VATM data center

- **Decision**: Containerized backend, web, PostgreSQL, and MinIO deployed on-prem; mobile apps
  distributed via internal/enterprise channels.
- **Rationale**: Meets on-prem residency (FR-024); reproducible across the Phase 1 pilot unit and
  the Phase 2 full rollout (SC-008) with the same configuration.

## Resolved spec deferrals (defaults; confirm during clarify/implementation)

- **"Official contract" rule**: default — contracts flagged official or above a configurable value
  threshold require TCT approval (FR-013). Configurable per VATM policy.
- **Contract field set**: the ten listed fields are the minimum; additional fields land in the
  JSONB extension bag (FR-008/FR-009).
- **Alert thresholds & UI language**: Vietnamese-first UI; alert windows configurable with sensible
  defaults.

All NEEDS CLARIFICATION items from Technical Context are resolved. Two open items (IdP protocol,
alert threshold values) are non-blocking configuration questions, not architectural unknowns.
