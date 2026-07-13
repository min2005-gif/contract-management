# Phase 1 Data Model: Centralized Contract Management System (VATM)

Derived from the spec's Key Entities and Functional Requirements. Field types are indicative
(PostgreSQL). The extensible contract field set (FR-009) is held in a JSONB `extra_fields` column.
All monetary values use `NUMERIC(18,2)`; all timestamps are `timestamptz`.

## Entity: OrganizationalUnit

Represents TCT (parent) or a subordinate unit. Scopes contract ownership and access (FR-001).

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| code | text, unique | Short unit code |
| name | text | Vietnamese display name |
| type | enum(`TCT`,`SUBORDINATE`) | Exactly one `TCT` row |
| active | boolean | |
| created_at / updated_at | timestamptz | |

**Rules**: A contract belongs to exactly one unit. TCT can see all units (FR-004); subordinate
users see only their own (FR-003).

## Entity: User

An internal VATM person, identity federated from VATM SSO/AD (FR-006).

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| external_subject | text, unique | Subject/UPN from the IdP |
| full_name | text | |
| email | text | |
| unit_id | UUID (FK → OrganizationalUnit) | Home unit |
| active | boolean | |
| created_at / updated_at | timestamptz | |

## Entity: Role & UserRole

Four roles (FR-002): `DATA_ENTRY`, `UNIT_HEAD`, `MANAGEMENT`, `ADMIN`. Assigned per user
(optionally scoped to a unit for TCT-wide vs unit-local roles). Directory groups map to roles.

| UserRole field | Type | Notes |
|----------------|------|-------|
| user_id | UUID (FK → User) | |
| role | enum | One of the four roles |
| scope_unit_id | UUID (FK, nullable) | Null = TCT-wide (management/admin) |

## Entity: Contract

The central aggregate (FR-007, FR-008).

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| contract_number | text | Unique within owning unit (see edge case) |
| name | text | |
| type | enum(`PURCHASE_SALE`,`SERVICE`,`CONSTRUCTION`,`LEASE`,`LABOR`) | FR-007 |
| party_a | text | |
| party_b | text | |
| value | numeric(18,2) | |
| sign_date | date | |
| term_end | date | Expiry/term end — drives expiry alerts |
| person_in_charge | UUID (FK → User) or text | Responsible person |
| status | enum (see state machine) | |
| is_official | boolean | Drives TCT-approval requirement (FR-013) |
| owning_unit_id | UUID (FK → OrganizationalUnit) | FR-001 |
| signed | boolean | For "unsigned" alert |
| payment_status | enum(`UNPAID`,`PARTIAL`,`PAID`) | For "unpaid" alert |
| progress_pct | smallint | For "behind schedule" alert |
| expected_progress_pct | smallint | Planned progress baseline |
| extra_fields | jsonb | Extensible field set (FR-009) |
| created_by / updated_by | UUID (FK → User) | |
| created_at / updated_at | timestamptz | |

**Validation** (FR-011): `contract_number`, `name`, `type`, `party_a`, `party_b`, `value`,
`sign_date`, `term_end`, `person_in_charge`, `status` required before submit. `value ≥ 0`;
`term_end ≥ sign_date`.

**Indexes**: `(owning_unit_id, status)`, `(term_end)`, `(payment_status)`, GIN on `extra_fields`,
unique `(owning_unit_id, contract_number)`.

### Contract state machine (FR-012, FR-013)

```text
DRAFT ──submit──▶ PENDING_CHECK ──unit head approve──▶ PENDING_TCT_APPROVAL* ──TCT approve──▶ ACTIVE
  ▲                    │                                      │                                  │
  └──────reject────────┴──────────────reject─────────────────┘                            track execution
                                                                                                 │
                                                              ACTIVE ──▶ IN_PROGRESS ──▶ COMPLETED ──▶ LIQUIDATED
```

- `PENDING_TCT_APPROVAL` is entered only when `is_official` is true (or value ≥ configured
  threshold); non-official contracts go from `PENDING_CHECK` straight to `ACTIVE` on unit-head
  approval.
- Any reviewer may `reject` with a reason, returning the contract to `DRAFT` for the owning unit
  (FR-014).
- Terminal-ish `EXPIRED` is a derived/flagged condition from `term_end`, surfaced via alerts rather
  than a hard status transition.

## Entity: Attachment

Files linked to a contract (FR-010). Binary stored in MinIO; metadata in DB.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| contract_id | UUID (FK → Contract) | |
| filename | text | Original name |
| content_type | enum/text | PDF, Word, image(scan); validated server-side |
| kind | enum(`MAIN`,`SCAN`,`APPENDIX`,`OTHER`) | |
| size_bytes | bigint | Size-limit enforced |
| object_key | text | MinIO object key |
| uploaded_by | UUID (FK → User) | |
| uploaded_at | timestamptz | |

## Entity: WorkflowStep

Recorded workflow action (FR-012, FR-014).

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| contract_id | UUID (FK → Contract) | |
| action | enum(`SUBMIT`,`CHECK_APPROVE`,`TCT_APPROVE`,`REJECT`,`LIQUIDATE`) | |
| from_status / to_status | enum | |
| actor_id | UUID (FK → User) | |
| reason | text (nullable) | Required for `REJECT` |
| created_at | timestamptz | |

## Entity: Alert

A flag raised against a contract (FR-016, FR-017).

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| contract_id | UUID (FK → Contract) | |
| type | enum(`NEARING_EXPIRY`,`UNSIGNED`,`UNPAID`,`BEHIND_SCHEDULE`) | |
| status | enum(`OPEN`,`ACKNOWLEDGED`,`RESOLVED`) | |
| raised_at | timestamptz | |
| notified_user_id | UUID (FK → User) | Person in charge |

**Rules**: Evaluation is idempotent — re-running does not duplicate an `OPEN` alert of the same
type for the same contract. Unique partial index on `(contract_id, type)` where `status='OPEN'`.

## Entity: AuditLogEntry

Immutable record of every mutation (FR-015, FR-025). Append-only.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| actor_id | UUID (FK → User) | |
| action | text | e.g., `CONTRACT_CREATE`, `CONTRACT_UPDATE`, `STATUS_CHANGE`, `APPROVE`, `REJECT` |
| entity_type / entity_id | text / UUID | Target |
| summary | jsonb | Before/after diff or key fields |
| created_at | timestamptz | |

## Report (derived, not stored)

Consolidated views computed on demand (FR-018): total contracts, total value, nearing-expiry
count, in-progress count, and per-unit breakdown. Backed by aggregate queries over `Contract`;
exported to Excel/PDF (FR-019) and charted on the dashboard (FR-020). Totals MUST reconcile to the
sum of per-unit figures (SC-004).

## Relationships summary

- OrganizationalUnit 1─* User; OrganizationalUnit 1─* Contract
- User 1─* UserRole; User 1─* Contract (as person_in_charge / created_by)
- Contract 1─* Attachment; Contract 1─* WorkflowStep; Contract 1─* Alert
- Every mutating action on any entity 1─* AuditLogEntry
