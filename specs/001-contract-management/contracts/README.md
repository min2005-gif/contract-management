# API Contracts

The backend exposes a versioned REST API under `/api/v1`, consumed by both the React web app and
the Flutter mobile app. `openapi.yaml` is the single source of truth for the contract; client code
is generated from it (Principle III: one contract, consistent clients).

## Conventions

- **Auth**: Bearer access token obtained via VATM SSO/AD federation (OIDC). Every endpoint except
  `/health` requires authentication; authorization is enforced per role and owning unit.
- **Scoping**: Subordinate-unit users are implicitly scoped to their own unit; TCT/management users
  may query across units. The server never returns cross-unit data to an unauthorized caller.
- **Errors**: RFC 7807 `application/problem+json` with a human-readable, actionable `detail`
  (Vietnamese-first) and a stable machine `code`.
- **Pagination**: List endpoints use `page`/`size` with a `total` count; default `size=20`.
- **Versioning**: Breaking changes require a new version prefix (`/api/v2`); no silent changes.

## Resource groups

| Group | Endpoints | Spec refs |
|-------|-----------|-----------|
| Units & Users (admin) | `/units`, `/users`, role assignment | FR-001–FR-004 |
| Contracts | CRUD + list/search, scoped by unit | FR-007–FR-011 |
| Attachments | upload/download/delete per contract | FR-010 |
| Workflow | submit / check-approve / tct-approve / reject / liquidate | FR-012–FR-014 |
| Alerts | list, acknowledge/resolve | FR-016–FR-017 |
| Reports | consolidated summary + Excel/PDF export | FR-018–FR-020 |
| Integrations | digital signature, e-document link, accounting reconcile | FR-021–FR-022 |
| Audit | read-only audit log query (admin) | FR-015, FR-025 |

See `openapi.yaml` for full schemas, parameters, and status codes.
