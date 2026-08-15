# Tasks: Release Tooling Cleanup

**Input**: Design documents from `/specs/007-release-tooling-cleanup/`
**Prerequisites**: plan.md, spec.md

**Tests**: Not applicable — no automated test surface for a config-file deletion + doc edit
(plan.md Technical Context). Verification is manual per the Polish phase below.

**Organization**: Single user story (spec.md US1) — this is a one-slice cleanup, not a
multi-story feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

No setup needed — nothing to scaffold for a deletion.

## Phase 2: Foundational

No foundational work — grep confirmed (during `/speckit-tasks`) that nothing outside
`release-please-config.json`, `.release-please-manifest.json`, and CLAUDE.md itself references
`release-please`; no other file needs updating as a prerequisite.

---

## Phase 3: User Story 1 - A maintainer trusts the release-notes config they see (Priority: P1)

**Goal**: Only `release-drafter`'s configuration remains in the repository; CLAUDE.md no longer
claims an overlap that no longer exists.

**Independent Test**: `git status` shows the two files deleted; `grep -ri release-please` over
the repo (excluding this feature's own spec/plan/tasks docs, which correctly retain the
investigation as history) returns nothing; `.github/workflows/release-drafter.yml` is unchanged.

- [X] T001 [P] [US1] Delete `release-please-config.json` (spec FR-001)
- [X] T002 [P] [US1] Delete `.release-please-manifest.json` (spec FR-001)
- [X] T003 [US1] Update CLAUDE.md's "Known gaps" section: remove the bullet stating
      release-drafter and release-please overlap (currently reads "Both `release-drafter` and
      `release-please` are configured; they overlap (both draft releases/changelogs from merged
      PRs) — only one should probably stay active long-term.") — in `CLAUDE.md` (spec FR-003)

**Checkpoint**: Repository has exactly one release-notes tool's configuration; CLAUDE.md reflects
reality.

---

## Phase 4: Polish & Cross-Cutting

- [X] T004 Confirm `.github/release-drafter.yml` and `.github/workflows/release-drafter.yml` are
      byte-for-byte unchanged by this branch (spec FR-002) — `git diff --stat` shows no
      modification to either file
- [X] T005 Confirm `CHANGELOG.md` is unchanged by this branch (spec FR-004) — `git diff --stat`
      shows no modification
- [ ] T006 After this branch merges, confirm the next pull request still receives a
      release-drafter draft-release update in CI (spec SC-002) — cannot be verified pre-merge,
      tracked as a post-merge observation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A
- **Foundational (Phase 2)**: N/A (verified during planning, no task needed)
- **US1 (Phase 3)**: No dependencies — T001/T002 are independent deletions; T003 is independent
  of both (different file)
- **Polish (Phase 4)**: Depends on Phase 3 complete; T006 additionally depends on this branch
  being merged (post-merge check, not part of this PR's own verification)

### Critical Path

T001, T002, T003 (all parallel) → T004, T005 (verification) → T006 (post-merge, separate from
this PR)

## Parallel Execution Examples

- T001, T002, T003 — three independent file operations, no shared file, can all happen in any
  order or simultaneously.
- T004 and T005 can run in parallel (different files being checked).

## Implementation Strategy

**Single pass**: This is small enough that Phase 3 is the entire deliverable — land T001-T003 as
one commit/PR. T006 is explicitly a post-merge observation (spec SC-002 can only be confirmed once
a real PR merges against the cleaned-up config), not a blocker for merging this one.
