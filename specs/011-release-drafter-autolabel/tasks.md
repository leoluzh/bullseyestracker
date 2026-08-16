# Tasks: Release Drafter Autolabeling

**Input**: Design documents from `/specs/011-release-drafter-autolabel/`
**Prerequisites**: plan.md, spec.md

**Tests**: Not applicable — YAML config change, no automated test surface (plan.md Technical
Context). Verification is this change's own PR getting correctly labeled once pushed.

**Organization**: Single user story (spec.md US1) — one-slice change.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

No setup needed.

## Phase 2: Foundational

No foundational work.

---

## Phase 3: User Story 1 - A maintainer trusts the draft release's categorization (Priority: P1)

**Goal**: `.github/release-drafter.yml` autolabels PRs by conventional-commit-style title prefix
into its own existing categories, without touching `.github/labeler.yml`.

**Independent Test**: Push this branch's own PR (titled with a `fix:`/`feat:` prefix), let
release-drafter's workflow run, confirm the PR carries the matching label.

- [X] T001a [US1] Create the missing `chore` repo label (`gh label list` confirms it doesn't
      exist; needed for FR-004 — the "🔧 Maintenance" category has no existing label already
      wired to it, unlike Features/Bug Fixes/Documentation) — created via
      `gh label create chore`, confirmed present via `gh label list --search chore`
- [X] T001 [US1] Add an `autolabeler` section to `.github/release-drafter.yml` mapping PR title
      prefix `feat:` → label `enhancement` (FR-001, corrected from `feature` — see spec.md), `fix:`
      → label `bug` (FR-002, corrected from `fix`), `docs:` → label `documentation` (FR-003), and
      `chore:`/`test:` → label `chore` (FR-004), all case-insensitive, using `release-drafter`'s
      native `title` regex matcher — in `.github/release-drafter.yml` (FR-005: `.github/labeler.yml`
      and its workflow untouched) (depends on T001a) — YAML validated with PyYAML (UTF-8)

**Checkpoint**: Config change complete; verification happens once this branch's own PR is pushed
(Polish phase).

---

## Phase 4: Polish & Cross-Cutting

- [ ] T002 Push this branch with a PR titled starting with a conventional-commit prefix (this
      change's own PR), and after release-drafter's workflow runs, confirm via
      `gh pr view --json labels` that the matching label was applied automatically (spec SC-001) —
      no further code change expected, verification only
- [X] T003 [P] Confirm `.github/labeler.yml` and `.github/workflows/label.yml` are byte-for-byte
      unchanged by this branch (spec FR-005) — `git diff --stat` shows no modification to either

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A
- **Foundational (Phase 2)**: N/A
- **US1 (Phase 3)**: No dependencies — single task
- **Polish (Phase 4)**: T002 depends on T001 being pushed as a real PR (can only verify after
  push); T003 has no dependency, can run any time after T001

### Critical Path

T001 → (push as PR) → T002

T003 can run any time after T001, in parallel with waiting on T002.

## Parallel Execution Examples

None meaningful — this is a single small config edit; T003's verification can happen alongside
waiting for T002's CI-dependent check.

## Implementation Strategy

**Single pass**: T001 is the entire deliverable — land it as one commit, and this feature's own
PR (opened with a conventional-commit title) serves as the live verification for T002.
