<!--
SYNC IMPACT REPORT
==================
Version change: (template, unversioned) → 1.0.0
Rationale: Initial ratification of a concrete constitution from the template.
  MAJOR bump to 1.0.0 because this is the first adopted version with binding principles.

Modified principles:
  - [PRINCIPLE_1_NAME] → I. Code Quality
  - [PRINCIPLE_2_NAME] → II. Testing Standards (NON-NEGOTIABLE)
  - [PRINCIPLE_3_NAME] → III. User Experience Consistency
  - [PRINCIPLE_4_NAME] → IV. Performance Requirements
  - [PRINCIPLE_5_NAME] → (removed; user requested 4 principles)

Added sections:
  - Quality Standards & Constraints (was [SECTION_2_NAME])
  - Development Workflow & Quality Gates (was [SECTION_3_NAME])

Removed sections:
  - Fifth principle slot (intentionally; user specified exactly four focus areas)

Templates requiring updates:
  - .specify/templates/plan-template.md ✅ aligned (Constitution Check gate is generic,
    derives gates from this file; no change needed)
  - .specify/templates/spec-template.md ✅ aligned (Success Criteria + User Scenarios &
    Testing already capture UX and performance measurables)
  - .specify/templates/tasks-template.md ⚠ pending review: template marks tests as
    "OPTIONAL"; Principle II requires tests for critical paths. Reconcile wording when the
    first feature is planned (constitution is authoritative).
  - .specify/templates/checklist-template.md ✅ aligned (no principle-specific content)

Follow-up TODOs: none
-->

# Contract Constitution

## Core Principles

### I. Code Quality

Code MUST be clear, consistent, and maintainable before it is considered complete.

- All code MUST pass automated linting and formatting checks; style is enforced by tooling,
  not debated in review.
- Functions and modules MUST have a single, well-defined responsibility; duplication MUST be
  refactored away once a third occurrence appears.
- Every change MUST be reviewed by at least one other person (or a documented self-review
  checklist for solo work) before merge.
- Public interfaces, non-obvious decisions, and known limitations MUST be documented at the
  point of use.

**Rationale**: Quality is cheapest to enforce at authoring time. Consistent, reviewed code
lowers the cost of every future change and prevents defect accumulation.

### II. Testing Standards (NON-NEGOTIABLE)

Correctness MUST be demonstrated by automated tests, not asserted by inspection.

- Every critical path (core business logic, data integrity, security boundaries) MUST have
  automated test coverage before merge.
- Contract and integration tests MUST exist for new interfaces, changed contracts, and
  inter-component communication.
- Tests MUST be deterministic and isolated; flaky tests MUST be fixed or quarantined, never
  ignored.
- A bug fix MUST include a regression test that fails before the fix and passes after.

**Rationale**: Tests are the executable specification of intended behavior. They enable safe
change, catch regressions early, and document expected outcomes.

### III. User Experience Consistency

The product MUST behave predictably and uniformly across every surface it exposes.

- Interaction patterns, terminology, and visual/textual conventions MUST be consistent across
  all features and entry points (UI, CLI, API).
- Error messages MUST be actionable, human-readable, and stated in terms the user understands.
- Accessibility baselines (keyboard navigation, sufficient contrast, screen-reader labels for
  UI; clear help text and exit codes for CLI) MUST be met for user-facing surfaces.
- Breaking changes to user-facing behavior MUST be versioned and communicated; silent behavior
  changes are prohibited.

**Rationale**: Consistency is what makes a product learnable and trustworthy. Predictable
behavior reduces cognitive load, support burden, and user error.

### IV. Performance Requirements

Performance MUST be specified, measured, and defended as a feature, not left to chance.

- Every user-facing feature MUST define measurable performance targets (e.g., p95 latency,
  throughput, memory ceiling) in its specification before implementation.
- Performance-sensitive paths MUST be benchmarked; regressions beyond an agreed threshold
  MUST block merge.
- Optimization MUST be driven by measurement, not assumption; premature optimization that
  harms clarity is rejected under Principle I.
- Resource limits and degradation behavior under load MUST be defined for any feature serving
  concurrent users.

**Rationale**: Performance that is not measured will silently regress. Explicit targets make
trade-offs visible and keep the product responsive as it grows.

## Quality Standards & Constraints

- Automated checks (lint, format, test suite, performance gates) MUST run in CI on every change
  and MUST pass before merge.
- Dependencies MUST be justified; each new third-party dependency requires a noted reason and a
  review of its maintenance and security posture.
- Security-relevant events and operational signals MUST be logged in a structured, queryable
  form to support debugging and incident response.
- Any deviation from these principles MUST be recorded in the plan's Complexity Tracking with a
  justification and the simpler alternative that was rejected.

## Development Workflow & Quality Gates

- Specifications MUST declare measurable success criteria (including UX and performance targets)
  before planning begins.
- The plan's Constitution Check gate MUST be evaluated before Phase 0 research and re-checked
  after Phase 1 design; unresolved violations block progress.
- Pull requests MUST verify compliance with all four core principles; reviewers MUST reject
  changes that introduce unjustified complexity or skip required tests.
- Releases MUST follow semantic versioning, and user-facing breaking changes MUST be documented
  in release notes.

## Governance

This constitution supersedes all other development practices. When guidance conflicts, this
document is authoritative.

- **Amendments**: Proposed in writing with rationale and impact analysis, reviewed and approved
  by project maintainers, and accompanied by a migration note for any breaking governance
  change.
- **Versioning policy**: This constitution is versioned with semantic versioning. MAJOR for
  backward-incompatible governance or principle removal/redefinition; MINOR for a new principle
  or materially expanded section; PATCH for clarifications and non-semantic wording fixes.
- **Compliance review**: Every plan and pull request MUST verify compliance with these
  principles. Recurring violations MUST trigger either remediation or a documented, approved
  amendment — not silent drift.
- **Runtime guidance**: Agent-specific and day-to-day development guidance lives in the active
  plan and `CLAUDE.md`; those documents MUST remain consistent with this constitution.

**Version**: 1.0.0 | **Ratified**: 2026-06-26 | **Last Amended**: 2026-06-26
