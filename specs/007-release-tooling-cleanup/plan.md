# Implementation Plan: Release Tooling Cleanup

**Branch**: `007-release-tooling-cleanup` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/007-release-tooling-cleanup/spec.md`

## Summary

Delete `release-please-config.json` and `.release-please-manifest.json` — confirmed dead config
(research.md/spec.md Assumptions: no workflow file ever wired `release-please` up, and
`CHANGELOG.md`'s stale content confirms it never ran). Update CLAUDE.md's "Known gaps" bullet to
remove the now-resolved release-drafter/release-please overlap note. `release-drafter`'s config
and workflow are untouched (FR-002); `CHANGELOG.md` is untouched (FR-004, out of scope).

## Technical Context

**Language/Version**: N/A — no application code touched (config files + Markdown docs only).

**Primary Dependencies**: None added or removed. `release-drafter`'s existing GitHub Action
(`.github/workflows/release-drafter.yml`) is unaffected.

**Storage**: N/A.

**Testing**: No automated test applies — this is config/doc deletion and a doc edit. Verification
is: (a) the two deleted files are gone and nothing references them, (b) the next PR merged still
gets a release-drafter draft update in CI (spec SC-002 — verified post-merge, not something a
pre-merge test can assert), (c) CLAUDE.md no longer states the overlap.

**Target Platform**: N/A.

**Project Type**: Repository configuration/documentation change — no Gradle module touched.

**Performance Goals**: N/A.

**Constraints**: None from the constitution apply (no CV/scoring/UI/performance surface touched
— this is outside the Android app entirely).

**Scale/Scope**: Two file deletions (`release-please-config.json`,
`.release-please-manifest.json`) plus a one-bullet edit to `CLAUDE.md`. No new files, no code.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Not applicable — the constitution (`.specify/memory/constitution.md`) governs the Android app's
CV/scoring/UI/persistence architecture; this change touches none of it (no `app`/`cv`/`match`
module files). All five principles are N/A for this change.

## Project Structure

### Documentation (this feature)

```text
specs/007-release-tooling-cleanup/
├── plan.md              # This file (/speckit-plan command output)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

No `research.md`/`data-model.md`/`quickstart.md` — there are no unresolved unknowns to research
(spec.md Assumptions already cover the investigation), no entities/data model (spec.md Key
Entities: not applicable), and no runnable end-to-end scenario beyond the acceptance scenarios
already in spec.md (a file-deletion diff plus one CI observation on the next PR).

### Source Code (repository root)

```text
release-please-config.json           # DELETED
.release-please-manifest.json        # DELETED
CLAUDE.md                            # MODIFIED — "Known gaps" bullet updated (FR-003)

# UNCHANGED:
.github/release-drafter.yml
.github/workflows/release-drafter.yml
CHANGELOG.md
```

**Structure Decision**: No source tree changes — this is repository root config/doc only.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
