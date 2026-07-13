# Feature Specification: Centralized Contract Management System (VATM)

**Feature Branch**: `001-contract-management`

**Created**: 2026-06-26

**Status**: Draft

**Input**: Requirements document "CẤU TRÚC BẢN MÔ TẢ YÊU CẦU QUẢN LÝ HỢP ĐỒNG.docx" — a proposal for a centralized contract management system for the Vietnam Air Traffic Management Corporation (VATM / TCT QLB Việt Nam).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Enter and manage a unit's contracts (Priority: P1)

A data-entry staff member at a subordinate unit records a new contract, fills in its key details, attaches the supporting documents (signed PDF, Word draft, scanned copy, appendices), and maintains it through its lifecycle. The unit owns and is responsible for the accuracy of its own contract data.

**Why this priority**: Capturing contract data in one place is the foundational value of the system — without reliable data entry, no reporting, approval, or alerting is possible. A single unit using this in isolation already delivers value (a searchable, attachment-backed contract register).

**Independent Test**: Log in as a data-entry user for one unit, create a contract with all required fields and at least one attachment, save it, then retrieve and edit it. The contract is persisted, scoped to that unit, and the attachment is downloadable.

**Acceptance Scenarios**:

1. **Given** an authenticated data-entry user, **When** they create a contract with contract number, name, party A/B, value, sign date, term, person in charge, and type, **Then** the contract is saved as a draft owned by their unit and appears in their unit's contract list.
2. **Given** a draft contract, **When** the user uploads a PDF, Word, or scanned attachment, **Then** the file is stored with the contract and can be downloaded later.
3. **Given** a contract belonging to another unit, **When** a data-entry user from a different subordinate unit attempts to view or edit it, **Then** access is denied.
4. **Given** a required field is left empty, **When** the user attempts to save, **Then** the system blocks the save and indicates which field is missing.

---

### User Story 2 - Review and approve contracts (Priority: P1)

A contract moves through an approval workflow: after entry it is checked and approved by the unit head, and an official contract must additionally be approved by the parent corporation (TCT). The parent corporation can also edit data when necessary.

**Why this priority**: Approval is what turns a recorded draft into an official, trusted contract record and enforces accountability. It is required for the system to be usable as the system of record rather than a scratch list.

**Independent Test**: Submit a draft for review as a data-entry user, approve it as a unit head, then approve it as a TCT approver, and confirm the contract status advances at each step and is logged.

**Acceptance Scenarios**:

1. **Given** a completed draft, **When** the data-entry user submits it for review, **Then** its status becomes "Pending check" and the unit head sees it in their review queue.
2. **Given** a contract pending check, **When** the unit head approves it, **Then** it advances to "Pending TCT approval" (for official contracts) or to active status per the configured rule.
3. **Given** a contract pending TCT approval, **When** a TCT approver approves it, **Then** it becomes an official/active contract and the approval action is recorded with user and timestamp.
4. **Given** any contract, **When** a reviewer rejects it with a reason, **Then** it returns to the originating unit for correction and the reason is visible to that unit.
5. **Given** an official contract, **When** a TCT user edits its data, **Then** the change is permitted and recorded in the audit log.

---

### User Story 3 - Consolidated oversight and reporting for TCT (Priority: P2)

A management or TCT user views all contracts across all units and generates consolidated reports: total number of contracts, total value, contracts nearing expiry, contracts in progress, and a breakdown by unit. Reports can be exported to Excel and PDF and viewed as dashboard charts.

**Why this priority**: Centralized visibility and reporting is the primary leadership benefit, but it depends on data and approvals existing first. High value, delivered after the data foundation.

**Independent Test**: As a TCT/management user, open the consolidated dashboard, confirm totals and the per-unit breakdown match the underlying contracts, then export the report to Excel and PDF.

**Acceptance Scenarios**:

1. **Given** contracts across multiple units, **When** a TCT user opens the consolidated view, **Then** they see contracts from all units with totals for count and value.
2. **Given** the consolidated report, **When** the user filters or groups by unit, **Then** figures are broken down per unit and reconcile to the overall totals.
3. **Given** a generated report, **When** the user exports it, **Then** an Excel file and a PDF file are produced reflecting the on-screen figures.
4. **Given** the dashboard, **When** it loads, **Then** it shows charts for contracts nearing expiry and contracts in progress.

---

### User Story 4 - Automatic contract alerts (Priority: P2)

The system automatically warns responsible users about contracts that are nearing expiry, not yet signed, not yet paid, or behind schedule, so issues are addressed proactively.

**Why this priority**: Alerts convert the stored data into proactive risk reduction, a stated benefit. Valuable but secondary to having and approving the data.

**Independent Test**: Create contracts that match each alert condition (e.g., expiry within the alert window, unsigned, unpaid, overdue), trigger the alert evaluation, and confirm the responsible users are notified and the contracts appear in the relevant alert list.

**Acceptance Scenarios**:

1. **Given** a contract whose term ends within the configured warning window, **When** alerts are evaluated, **Then** it appears in the "nearing expiry" alert list and the person in charge is notified.
2. **Given** a contract marked unsigned past its sign date, **When** alerts are evaluated, **Then** it is flagged as unsigned.
3. **Given** a contract with an outstanding payment, **When** alerts are evaluated, **Then** it is flagged as unpaid.
4. **Given** a contract past its planned progress, **When** alerts are evaluated, **Then** it is flagged as behind schedule.

---

### User Story 5 - Integration with internal systems (Priority: P3)

The system integrates with VATM's internal electronic document system, digital signature, and accounting software so contract data is consistent with related internal records. Connection to external public-service portals or national databases is explicitly out of scope.

**Why this priority**: Integration increases efficiency and data consistency but the system delivers value standalone; integrations are an enhancement layered on a working core.

**Independent Test**: With integration configured, perform a digital signing action on a contract and confirm the signed status/document is reflected; confirm a contract value can be reconciled with the accounting system reference.

**Acceptance Scenarios**:

1. **Given** a contract ready for signature, **When** it is signed via the digital signature integration, **Then** its signed status and signed document are recorded against the contract.
2. **Given** the electronic document system integration, **When** a related document exists, **Then** it can be linked to or referenced from the contract.
3. **Given** the accounting integration, **When** a contract has financial data, **Then** the relevant figures can be shared with or reconciled against the accounting system.

---

### Edge Cases

- What happens when a duplicate contract number is entered within the same unit (or across units)?
- How does the system handle an attachment that exceeds the allowed size or uses a disallowed file type?
- What happens to a contract's status and alerts when its term is extended or the contract is amended via an appendix?
- How are contracts handled when a unit is reorganized or a responsible person leaves?
- What happens when a reviewer rejects a contract that another user is simultaneously editing?
- How does the system behave when an integrated system (e-document, digital signature, accounting) is unavailable?
- How are partially paid or multi-installment contracts represented for the "unpaid" alert?

## Requirements *(mandatory)*

### Functional Requirements

**Organization, users, and permissions**

- **FR-001**: System MUST support the parent corporation (TCT) and its subordinate units as distinct organizational scopes, with each contract owned by exactly one unit.
- **FR-002**: System MUST support the roles: data-entry staff, unit head (approver), management (report viewer), and system administrator, each with distinct permissions.
- **FR-003**: System MUST restrict subordinate-unit users to viewing and editing only their own unit's contracts.
- **FR-004**: System MUST allow TCT users to view all contracts across all units, and to approve and edit contract data when necessary.
- **FR-005**: System MUST be restricted to internal VATM users; external partners MUST NOT have direct access.
- **FR-006**: System MUST authenticate users via single sign-on (SSO) and enforce role- and unit-based permissions on every action.

**Contract data and attachments**

- **FR-007**: System MUST allow recording contracts of the managed types: purchase/sale, service, construction, lease, and labor.
- **FR-008**: System MUST capture at minimum the following contract fields: contract number, contract name, party A, party B, value, sign date, term/expiry, person in charge, contract type, and status.
- **FR-009**: System MUST allow additional contract fields to be added over time without redesign (the field set is expected to grow).
- **FR-010**: System MUST allow attaching files to a contract in PDF (`.pdf`), Word (`.doc`, `.docx`), and scanned-image (`.jpg`, `.png`, `.tif`) formats, including contract appendices, and allow them to be downloaded. Each file MUST NOT exceed a configurable maximum size (default 50 MB); files exceeding the limit or of a disallowed type MUST be rejected with a clear message.
- **FR-011**: System MUST validate required fields before a contract can be submitted for review.
- **FR-026**: System MUST enforce that a contract number is unique within its owning unit; an attempt to create or update a contract with a duplicate number in the same unit MUST be rejected with a clear message. (Contract numbers need not be unique across different units.)

**Workflow and status**

- **FR-012**: System MUST move contracts through the lifecycle: Create → Check → Approve → Track execution → Liquidate, with a status reflecting the current stage.
- **FR-013**: System MUST require that an official contract is approved by TCT before it becomes active/official. "Official" is determined by a configurable rule — default: a contract flagged as official OR with a value at or above a configurable threshold (default 100,000,000 VND) requires TCT approval; other contracts become active on unit-head approval.
- **FR-014**: System MUST allow a reviewer to reject a contract with a reason and return it to the originating unit for correction.
- **FR-015**: System MUST record every business change to contract data — create, edit, status change, approval, and rejection — in an immutable **business audit log** with user identity and timestamp.

**Alerts**

- **FR-016**: System MUST automatically flag and notify on contracts that are nearing expiry, unsigned, unpaid, or behind schedule, using configurable definitions — defaults: *nearing expiry* = term ends within 30 days; *unsigned* = not signed past its sign date; *unpaid* = payment status is UNPAID or PARTIAL past a due milestone; *behind schedule* = actual progress below expected progress.
- **FR-017**: System MUST direct each alert to the responsible person(s) for the affected contract and surface it in a corresponding alert list.

**Reporting**

- **FR-018**: System MUST provide TCT/management with consolidated reports including total number of contracts, total value, contracts nearing expiry, contracts in progress, and a per-unit breakdown.
- **FR-019**: System MUST allow reports to be exported to Excel and PDF.
- **FR-020**: System MUST present key figures as dashboard charts.

**Integration**

- **FR-021**: System MUST integrate with VATM's internal electronic document system, digital signature, and accounting software.
- **FR-022**: System MUST NOT require connection to external public-service portals or national databases.

**Platform and operations**

- **FR-023**: System MUST be accessible as a web application and provide a mobile application for Android and iOS.
- **FR-024**: System MUST store data on servers within VATM's data center.
- **FR-025**: System MUST maintain a **security/operations log** — distinct from the business audit log (FR-015) — capturing authentication and authorization events (logins, failed access attempts, permission/role changes) and sensitive operational actions (attachment downloads, report exports, admin actions) for security review.

### Key Entities *(include if feature involves data)*

- **Organizational Unit**: The TCT parent corporation or one of its subordinate units; owns contracts and scopes user access.
- **User**: An internal VATM person with one or more roles (data-entry, unit head, management, administrator) and an associated unit.
- **Contract**: The central record — contract number, name, party A/B, value, sign date, term, person in charge, type, status, owning unit, and an extensible set of additional fields.
- **Attachment**: A file (PDF, Word, scan, appendix) associated with a contract.
- **Approval / Workflow Step**: A recorded action (check, approve, reject) by a user moving a contract through its lifecycle.
- **Alert**: A flag/notification raised against a contract for a condition (nearing expiry, unsigned, unpaid, behind schedule).
- **Report**: A consolidated, exportable view of contract data for oversight (totals, per-unit breakdown, dashboard).
- **Audit Log Entry**: An immutable record of a user action (who, what, when) for accountability and security.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A data-entry user can create and save a complete contract (with one attachment) in under 5 minutes.
- **SC-002**: 100% of contracts are owned by, and visible only to, the correct unit (plus TCT), with zero cross-unit data leakage in access testing.
- **SC-003**: A TCT user can produce a consolidated cross-unit report and export it to Excel and PDF in under 1 minute.
- **SC-004**: Consolidated report totals reconcile exactly to the sum of underlying per-unit contracts (0 discrepancies).
- **SC-005**: 100% of contracts meeting an alert condition are flagged and notified within one alert evaluation cycle of becoming eligible.
- **SC-006**: Every create, edit, approval, and rejection is recorded in the audit log (100% coverage) and is attributable to a specific user and time.
- **SC-007**: The system supports at least 100 users and the full contract volume of the 10 subordinate units plus TCT without degraded responsiveness.
- **SC-008**: The system is successfully piloted at one unit (Phase 1) and then rolled out across all units (Phase 2) using the same configuration.
- **SC-009**: Common contract pages and the dashboard load in under 3 seconds for a typical user under normal load.

## Assumptions

- The authoritative scope is the refined "Đề xuất phần mềm quản lý hợp đồng – VATM" section of the source document: the parent corporation (TCT) plus **10** subordinate units (the earlier draft's "11 units" is superseded).
- Estimated usage is approximately **100 users** spanning data-entry staff, leadership, and administrators.
- The user interface and reports are primarily in **Vietnamese**, matching the internal VATM user base.
- "Official contract requires TCT approval" applies to formal/official contracts; the exact set of contract types or value thresholds that count as "official" will be configured per VATM policy (reasonable default: all contracts above a configurable value or flagged as official require TCT approval).
- The contract field set listed is a **minimum**; additional fields will be supplied later and the data model is expected to accommodate them.
- Alert thresholds and definitions (the "nearing expiry" window, "unpaid", "behind schedule" — see FR-016) are configurable; the stated defaults apply pending VATM confirmation.
- Concurrent edits to the same contract are handled with optimistic concurrency (last write is rejected with a conflict message rather than silently overwritten), covering the edit-during-reject edge case.
- Handling of unit reorganization and a responsible person leaving (reassigning ownership / person-in-charge) is deferred to a later phase; for v1 an administrator can manually reassign the owning unit and person in charge.
- "Track execution" includes monitoring progress and payment status to support the behind-schedule and unpaid alerts.
- Hosting is on-premises within VATM's data center; the cloud-hosting option referenced in the earlier draft is not selected.
- Connections to external public-service portals and national databases are explicitly out of scope.
- Standard security practices apply (encrypted transport, access control, audit logging) consistent with an internal government-corporation system.
