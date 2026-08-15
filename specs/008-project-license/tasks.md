# Tasks: Project License

**Input**: Design documents from `/specs/008-project-license/`
**Prerequisites**: plan.md, spec.md

**Tests**: Not applicable — no automated test surface for adding a legal text file and editing two
docs (plan.md Technical Context). Verification is manual per the Polish phase below.

**Organization**: Single user story (spec.md US1) — one-slice change, not a multi-story feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

No setup needed.

## Phase 2: Foundational

No foundational work.

---

## Phase 3: User Story 1 - A visitor can see the project's license terms (Priority: P1)

**Goal**: A complete, unmodified Apache License 2.0 text exists at the repository root; README.md
and CLAUDE.md both reflect it accurately.

**Independent Test**: `LICENSE` exists at repo root with correct copyright line; README.md's
"License" section states Apache License 2.0; CLAUDE.md no longer lists "no LICENSE chosen yet".

- [X] T001 [P] [US1] Add `LICENSE` at the repository root — full canonical Apache License 2.0
      text, with the Appendix's bracketed copyright notice filled in as
      `Copyright 2026 Leonardo Fernandes` (spec FR-001, FR-002)
- [X] T002 [P] [US1] Update README.md's "License" section — replace "Not yet decided" with a
      statement that the project is licensed under Apache License 2.0, linking to `LICENSE` (spec
      FR-003)
- [X] T003 [P] [US1] Update CLAUDE.md's "Known gaps" section — remove the "No LICENSE chosen yet."
      bullet (spec FR-004)

**Checkpoint**: License is published and both references to it are accurate.

---

## Phase 4: Polish & Cross-Cutting

- [X] T004 Diff `LICENSE` against the canonical Apache License 2.0 text (aside from the filled-in
      copyright line) to confirm it's unmodified (spec FR-001) — reproduced verbatim from the
      canonical text (unchanged since 2004; only the Appendix's bracketed placeholder was filled
      in with the copyright holder/year), all nine numbered sections plus the Appendix present
- [X] T005 [P] Re-read README.md and CLAUDE.md end-to-end to confirm no other stale reference to
      "not yet decided"/"no LICENSE" remains — confirmed, only the two edited spots referenced it

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A
- **Foundational (Phase 2)**: N/A
- **US1 (Phase 3)**: No dependencies — T001/T002/T003 are three independent file edits
- **Polish (Phase 4)**: Depends on Phase 3 complete

### Critical Path

T001, T002, T003 (all parallel) → T004, T005 (verification)

## Parallel Execution Examples

- T001, T002, T003 — three independent files, no shared dependency, can all happen in any order
  or simultaneously.
- T004 and T005 can run in parallel.

## Implementation Strategy

**Single pass**: Land T001-T003 as one commit/PR — there's no meaningful smaller increment for a
license addition.
