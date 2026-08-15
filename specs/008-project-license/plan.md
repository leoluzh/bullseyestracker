# Implementation Plan: Project License

**Branch**: `008-project-license` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/008-project-license/spec.md`

## Summary

Add a standard, unmodified Apache License 2.0 `LICENSE` file at the repository root (identifying
copyright holder Leonardo Fernandes, 2026), update README.md's "License" section (currently "Not
yet decided") to state Apache License 2.0 and link to it, and remove the now-resolved "no LICENSE
chosen yet" bullet from CLAUDE.md's "Known gaps". No NOTICE file, no third-party attribution audit
(spec Assumptions — separate, narrower question, out of scope here).

## Technical Context

**Language/Version**: N/A — no application code touched (one legal text file + two doc edits).

**Primary Dependencies**: None.

**Storage**: N/A.

**Testing**: No automated test applies. Verification is: (a) `LICENSE` at repo root matches the
canonical Apache License 2.0 text byte-for-byte except the Appendix's bracketed copyright-notice
placeholder, filled in correctly; (b) README.md and CLAUDE.md read as expected (spec Acceptance
Scenarios 2-3).

**Target Platform**: N/A.

**Project Type**: Repository documentation/legal-file change — no Gradle module touched.

**Performance Goals**: N/A.

**Constraints**: None from the constitution apply — no CV/scoring/UI/performance surface touched.

**Scale/Scope**: One new file (`LICENSE`) at the repository root, plus two small edits
(`README.md`'s "License" section, one CLAUDE.md bullet removed).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Not applicable — the constitution governs the Android app's CV/scoring/UI/persistence
architecture; this change adds only a root-level legal text file and two doc edits, touching no
`app`/`cv`/`match` module code. All five principles are N/A for this change.

## Project Structure

### Documentation (this feature)

```text
specs/008-project-license/
├── plan.md              # This file (/speckit-plan command output)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

No `research.md`/`data-model.md`/`quickstart.md` — the one substantive question (which license,
who's the copyright holder) was already resolved before this plan was written (spec.md
Assumptions), and there is no data model or runnable end-to-end scenario beyond the acceptance
scenarios already in spec.md.

### Source Code (repository root)

```text
LICENSE                              # NEW — Apache License 2.0, full canonical text
README.md                            # MODIFIED — "License" section (spec FR-003)
CLAUDE.md                            # MODIFIED — "Known gaps" bullet removed (spec FR-004)
```

**Structure Decision**: No source tree changes — this is a repository-root legal file plus two
doc edits.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
