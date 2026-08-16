# Implementation Plan: Release Drafter Autolabeling

**Branch**: `011-release-drafter-autolabel` | **Date**: 2026-08-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/011-release-drafter-autolabel/spec.md`

## Summary

Add an `autolabeler` section to `.github/release-drafter.yml` matching PR title prefixes
(`feat:`/`fix:`/`docs:`/`chore:`/`test:`, case-insensitive) to release-drafter's own category
labels (`feature`/`fix`/`documentation`/`chore`), so PRs following this repo's existing
conventional-commit-style naming get correctly categorized in the draft release without manual
labeling. `.github/labeler.yml` (path-based labels) is untouched — different mechanism, different
purpose, both apply independently to the same PR.

## Technical Context

**Language/Version**: N/A — GitHub Actions/release-drafter YAML configuration only, no
application code touched.

**Primary Dependencies**: None added. `release-drafter/release-drafter@v7` (already in use via
`.github/workflows/release-drafter.yml`) natively supports an `autolabeler` config block — no new
Action, no new workflow file.

**Storage**: N/A.

**Testing**: No automated test applies — this is a YAML config change with no unit-testable
surface. Verification is: this change's own pull request is titled with a conventional-commit
prefix, and after release-drafter's workflow runs on it (already triggers on
`pull_request: [opened, reopened, synchronize]`), the PR carries the matching label — checked via
`gh pr view --json labels` post-push, and the label's presence is itself proof the config works
(spec SC-001).

**Target Platform**: N/A.

**Project Type**: Repository configuration change — no Gradle module touched.

**Performance Goals**: N/A.

**Constraints**: None from the constitution apply (no CV/scoring/UI/performance surface touched —
this is outside the Android app entirely, same category as `007-release-tooling-cleanup`).

**Scale/Scope**: One targeted addition to one existing file (`.github/release-drafter.yml`); no
new files, no code.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Not applicable — the constitution governs the Android app's CV/scoring/UI/persistence
architecture; this change touches only GitHub Actions configuration, no `app`/`cv`/`match` module
files.

## Project Structure

### Documentation (this feature)

```text
specs/011-release-drafter-autolabel/
├── plan.md              # This file (/speckit-plan command output)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

No `research.md`/`data-model.md`/`quickstart.md` — the investigation (which two label
vocabularies exist, why they don't overlap) was already done in conversation before spec.md was
written; there's no data model, and the only "runnable scenario" is this change's own PR
acquiring the correct label, already captured in spec.md's Acceptance Scenarios.

### Source Code (repository root)

```text
.github/release-drafter.yml          # MODIFIED — adds an `autolabeler` section (spec FR-001-004)

# UNCHANGED:
.github/labeler.yml
.github/workflows/label.yml
.github/workflows/release-drafter.yml
```

**Structure Decision**: Single-file config edit at `.github/release-drafter.yml`; no source tree
changes.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
