# Tasks: CLAUDE.md Refresh

**Input**: Design documents from `/specs/009-claude-md-refresh/`
**Prerequisites**: plan.md, spec.md

**Tests**: Not applicable — documentation-only edit, no automated test surface (plan.md Technical
Context). Verification is manual per the Polish phase below.

**Organization**: Single user story (spec.md US1) — one-slice change, not a multi-story feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

No setup needed.

## Phase 2: Foundational

No foundational work.

---

## Phase 3: User Story 1 - A future Claude Code session trusts CLAUDE.md's project-state claims (Priority: P1)

**Goal**: All five identified stale claims in CLAUDE.md are corrected; nothing else in the file
changes.

**Independent Test**: Re-read CLAUDE.md end-to-end; every project-state claim matches current
`specs/` contents and module source (spec Acceptance Scenarios 1-4).

- [X] T001 [US1] Correct the "Known gaps" section's User-Stories-2-4 bullet — remove the claim
      that photo scoring/501/Cricket are unimplemented (spec FR-001) — in `CLAUDE.md`
- [X] T002 [US1] Correct the "Known gaps" section's fixture-images bullet — state that a
      ground-truth format and automated accuracy-benchmark harness now exist (spec 006), while the
      real dartboard photos themselves are still the missing piece (spec FR-002) — in `CLAUDE.md`
      (merged into the same bullet as T001 — both described the same "Known gaps" entry pair)
- [X] T003 [US1] Correct the `match` module description — remove the "(501, Cricket — not yet
      implemented)" parenthetical (spec FR-003) — in `CLAUDE.md`
- [X] T004 [US1] Correct the constitution-mandated-testing section — remove the "(once built)"
      qualifier on `X501Rules`/`CricketRules` (spec FR-004) — in `CLAUDE.md`
- [X] T005 [US1] Correct the "What this is" section — remove or reword the implication that
      `specs/001-dart-scoring-match/` is the project's sole active feature, given eight feature
      specs now exist (spec FR-005) — in `CLAUDE.md`

**Checkpoint**: CLAUDE.md's project-state claims all match reality; no other content changed.

---

## Phase 4: Polish & Cross-Cutting

- [X] T006 Re-read CLAUDE.md end-to-end to confirm: (a) all five corrections landed, (b) no other
      content was altered beyond them (spec FR-006), (c) no new staleness was introduced by the
      edits themselves — confirmed via full-file read; only the five targeted sections changed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A
- **Foundational (Phase 2)**: N/A
- **US1 (Phase 3)**: No dependencies — T001-T005 all touch the same file but different, 
  non-overlapping sections; sequential edits to avoid clobbering each other, not a true blocking
  dependency
- **Polish (Phase 4)**: Depends on Phase 3 complete

### Critical Path

T001 → T002 → T003 → T004 → T005 → T006 (sequential — same file, kept in reading order rather
than parallelized to avoid overlapping edits)

## Parallel Execution Examples

None — all five corrections land in the same file; sequential editing avoids conflicting
in-flight edits to the same document.

## Implementation Strategy

**Single pass**: Land T001-T005 as one commit/PR — there's no meaningful smaller increment for
correcting five related stale claims in one short file.
