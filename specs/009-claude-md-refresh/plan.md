# Implementation Plan: CLAUDE.md Refresh

**Branch**: `009-claude-md-refresh` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/009-claude-md-refresh/spec.md`

## Summary

Correct five stale project-state claims in CLAUDE.md (spec FR-001-005): the "Known gaps" section's
US2-4-unimplemented claim and its fixture-images entry, the `match` module description's
501/Cricket "not yet implemented" parenthetical, the constitution-testing section's "(once built)"
qualifier on `X501Rules`/`CricketRules`, and the "What this is" section's implication that
`specs/001-dart-scoring-match/` is the sole active feature. No other content in CLAUDE.md changes
(FR-006).

## Technical Context

**Language/Version**: N/A — documentation-only change, no application code touched.

**Primary Dependencies**: None.

**Storage**: N/A.

**Testing**: No automated test applies. Verification is: re-read CLAUDE.md end-to-end and
cross-check each corrected claim against `specs/` directory contents and the `match`/`cv` module
source (spec Acceptance Scenarios 1-4).

**Target Platform**: N/A.

**Project Type**: Documentation change — no Gradle module touched.

**Performance Goals**: N/A.

**Constraints**: None from the constitution apply — no CV/scoring/UI/performance surface touched.

**Scale/Scope**: Five targeted edits within one existing file (`CLAUDE.md`); no new files.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Not applicable — this change edits only CLAUDE.md's own descriptive text; it touches no
`app`/`cv`/`match` module code, so none of the five constitution principles apply.

## Project Structure

### Documentation (this feature)

```text
specs/009-claude-md-refresh/
├── plan.md              # This file (/speckit-plan command output)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

No `research.md`/`data-model.md`/`quickstart.md` — the investigation (which claims are stale, and
what the correct text should say) was already done before spec.md was written; there's no data
model, and no runnable scenario beyond re-reading the file (spec Acceptance Scenarios).

### Source Code (repository root)

```text
CLAUDE.md                            # MODIFIED — five targeted corrections (spec FR-001-005)
```

**Structure Decision**: Single-file edit at the repository root; no source tree changes.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
