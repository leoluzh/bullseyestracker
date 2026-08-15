# Feature Specification: CLAUDE.md Refresh

**Feature Branch**: `009-claude-md-refresh`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Fix stale claims in CLAUDE.md's project-state sections. The
'Known gaps' section still says User Stories 2-4 (photo scoring, 501 match, Cricket match) are
not implemented, but specs 002/003/004 are all done and merged. Investigation found further
staleness in the same document beyond that one bullet: the 'What this is' section still points
at specs/001-dart-scoring-match/ as 'the active feature' even though eight features now exist
(001-008); the match module bullet still parenthetically says '(501, Cricket — not yet
implemented)'; the constitution-mandated-testing section still says X501Rules/CricketRules are
'(once built)'; and the fixture-images known gap doesn't reflect that a ground-truth
harness/accuracy-benchmark now exists (spec 006) — only the actual photos are still missing.
Scope: correct these stale project-state claims so CLAUDE.md reflects what's actually true today;
this is a documentation-only change to CLAUDE.md, not a rewrite of its structure or guidance."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - A future Claude Code session trusts CLAUDE.md's project-state claims (Priority: P1)

A developer (or a future Claude Code session reading this file at the start of every
conversation, per its own stated purpose) opens CLAUDE.md to understand what's built and what
isn't. Today several of its claims are stale — they describe the project as it stood right after
the very first feature shipped, not as it stands after eight. Trusting a stale claim wastes time
(e.g., someone might redundantly re-verify that Cricket rules need writing, or avoid referencing
spec 005-008 because the file implies only spec 001 is "active") or, worse, causes wrong
decisions made on outdated assumptions.

**Why this priority**: This is the entire scope of the change — every identified inaccuracy is
part of the same underlying problem (the file wasn't updated as work landed) and fixing them
together is not meaningfully riskier or larger than fixing one in isolation. There is no smaller
independently-valuable slice; a "future session" reading a partially-corrected file would still
hit a stale claim right next to a fixed one.

**Independent Test**: Read CLAUDE.md end-to-end after this change and cross-check each
project-state claim (what's implemented, what's not, where things live) against the actual
`specs/` directory and module contents; every claim matches current reality.

**Acceptance Scenarios**:

1. **Given** CLAUDE.md after this change, **When** a reader looks at the "Known gaps" section,
   **Then** it no longer claims User Stories 2-4 are unimplemented (since they're done), and its
   fixture-images gap accurately reflects that a ground-truth format/benchmark exists (spec
   006) while the actual photos are still the missing piece.
2. **Given** CLAUDE.md after this change, **When** a reader looks at the `match` module
   description, **Then** it no longer parenthetically says 501/Cricket rules are "not yet
   implemented."
3. **Given** CLAUDE.md after this change, **When** a reader looks at the constitution-mandated
   testing section, **Then** it no longer describes `X501Rules`/`CricketRules` as "(once built)."
4. **Given** CLAUDE.md after this change, **When** a reader looks at the "What this is" section,
   **Then** it no longer implies `specs/001-dart-scoring-match/` is the sole active feature, given
   eight feature specs now exist.

---

### Edge Cases

- What happens to guidance that isn't a factual project-state claim (module boundaries, build
  gotchas, commands)? Left untouched — this change corrects stated facts about what's
  built/where, not the file's structure, its non-time-sensitive guidance, or its tone.
- What if a "known gap" is only partially resolved (e.g., fixture harness exists, but real photos
  still don't)? The corrected text must reflect the actual partial state precisely — not simply
  deleted (which would hide the real remaining gap) and not left as fully unresolved (which would
  misstate progress already made).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: CLAUDE.md's "Known gaps" section MUST NOT claim User Stories 2-4 (photo scoring,
  501 match, Cricket match) are unimplemented.
- **FR-002**: CLAUDE.md's "Known gaps" section's fixture-images entry MUST distinguish between
  what now exists (a ground-truth format and an automated accuracy-benchmark harness) and what is
  still actually missing (the real dartboard photos themselves).
- **FR-003**: CLAUDE.md's description of the `match` module MUST NOT state that 501/Cricket rules
  are not yet implemented.
- **FR-004**: CLAUDE.md's constitution-mandated-testing section MUST NOT describe
  `X501Rules`/`CricketRules` as not yet built.
- **FR-005**: CLAUDE.md's "What this is" section MUST NOT imply that
  `specs/001-dart-scoring-match/` is the project's sole active feature.
- **FR-006**: This change MUST NOT alter CLAUDE.md's non-time-sensitive guidance (module
  boundaries, build-environment gotchas, commands) beyond the specific stale claims identified in
  FR-001 through FR-005.

### Key Entities

Not applicable — this change edits project documentation only; no application data or runtime
entities are involved.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every project-state claim in CLAUDE.md (what's implemented, what's outstanding,
  where the active work lives) matches the actual contents of `specs/` and the app's modules as of
  this change.
- **SC-002**: A reader of CLAUDE.md's "Known gaps" section can distinguish fully-resolved gaps
  (removed), partially-resolved gaps (described accurately), and fully-open gaps (left as-is,
  e.g. no LICENSE — already resolved separately — is not relevant here, but any genuinely still-
  open gap remains stated).

## Assumptions

- "Stale" is scoped strictly to factually incorrect statements about implementation state
  (what's built, what's not, where the active feature lives) — not to style, structure, or
  non-time-sensitive guidance, which are out of scope for this change (Edge Cases, FR-006).
- The five specific inaccuracies named in this spec's Input were identified by direct
  cross-reference against the `specs/` directory and module source before this spec was written;
  no further investigation is needed to know *what* to fix, only to write the corrected text.
