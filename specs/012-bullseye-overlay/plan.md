# Implementation Plan: Bullseye Calibration Overlay

**Branch**: `012-bullseye-overlay` | **Date**: 2026-08-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/012-bullseye-overlay/spec.md`

## Summary

Draw the already-computed `BoardCalibration`'s inner/outer bull boundaries as two concentric
circle outlines on both `LiveScoringScreen` and `PhotoScoringScreen`. Both screens currently
compute a full `BoardCalibration` on successful calibration but discard it after use
(`LiveScoringScreen`'s `onCalibrated = { _, confidence -> ... }` literally discards the
calibration parameter; `PhotoScoringScreen` uses it only to call `detectThrows`, never stores it).
This feature keeps that calibration in each screen's state and renders it via a new
`BullseyeOverlay` composable — no new `cv`-module code, no new detection algorithm (spec FR-005).

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged)

**Primary Dependencies**: No new dependency. Uses Compose Foundation's `Canvas`/`drawCircle`
(already a transitive Compose dependency, same library family `DetectionOverlay` already uses for
its marker positioning math) with `Stroke` style for outlines.

**Storage**: N/A — calibration is already-in-memory, transient UI state (spec Key Entities); no
persistence involved.

**Testing**: No automated test applies — this is Compose UI over already-tested data
(`BoardCalibration` itself, and the coordinate math it feeds, are exercised by
`cv`'s existing `OpenCvBoardDetectorTest`/`CvEngineContractTest`). Matches this codebase's
existing convention: `DetectionOverlay`, `LiveScoringScreen`, `PhotoScoringScreen` have no
automated tests today (no `app/src/test`, no `app/src/androidTest`); validated via quickstart.md
manual walkthrough instead, same as every other `app`-module screen in this project.

**Target Platform**: Android, minSdk 26 (unchanged)

**Project Type**: Mobile app — no new Gradle module; additive to the existing `app` module's
`ui/detection/` package only. No `cv`/`match` changes.

**Performance Goals**: N/A beyond constitution Principle IV, which this feature doesn't touch —
drawing two circles is a Compose recomposition/draw-phase operation triggered once per
calibration change, not a new per-frame `cv`-pipeline computation (spec SC-003).

**Constraints**: Fully offline (Principle I — unchanged, no network anywhere near this); CV logic
isolated from UI (Principle II — this feature adds zero `cv`-module code and no new
`org.opencv.*` import anywhere in `app`; it only reads fields already present on the existing
`BoardCalibration` data class exposed by `CvEngine`); no new automatic detection (Principle V —
this visualizes an already-user-visible calibration status, it doesn't add a new automatic
score/detection needing correction).

**Scale/Scope**: One new composable (`BullseyeOverlay`, `app/.../ui/detection/`), plus small state
additions to `LiveScoringScreen` and `PhotoScoringScreen` (keep the `BoardCalibration` they already
compute instead of discarding it).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — pure Compose draw over already-local data; no network surface added |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — this feature adds zero `cv`-module code; `app` only reads fields already on the existing `BoardCalibration` type it already receives from `CvEngine` |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | N/A — no scoring/rules logic introduced; this draws already-computed, already-tested calibration data |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | PASS — no change to the `cv` pipeline's per-frame cost; drawing two circles from state already held is negligible and happens on the UI thread exactly like the existing dart markers already do |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | PASS (reinforces) — this feature is itself an explainability improvement (spec Summary); it adds no new automatic detection needing its own correction flow |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/012-bullseye-overlay/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

No `data-model.md` — no new domain entity (spec Key Entities: the overlay is transient UI state
derived directly from the existing `BoardCalibration` type, not a new persisted or modeled
entity).

### Source Code (repository root)

```text
app/
└── src/main/java/com/bullseyestracker/ui/detection/
    ├── BullseyeOverlay.kt        # NEW — draws two concentric circle outlines (inner/outer bull)
    │                             #   from a nullable BoardCalibration, in the same normalized
    │                             #   0..1 coordinate space DetectionOverlay's markers already use
    ├── LiveScoringScreen.kt      # MODIFIED — keeps the BoardCalibration onCalibrated already
    │                             #   receives (currently discarded) as state; clears it on
    │                             #   onBoardNotFound; renders BullseyeOverlay
    └── PhotoScoringScreen.kt     # MODIFIED — keeps the BoardCalibration scorePhoto already
                                  #   computes (currently discarded after use) as state; clears
                                  #   it on retake()/NotFound; renders BullseyeOverlay

cv/                               # UNTOUCHED — no new detection algorithm (spec FR-005)
match/                            # UNTOUCHED
```

**Structure Decision**: Additive to `app`'s existing `ui/detection/` package only — same location
`DetectionOverlay`/`CorrectionDialog` already live in, since this is the same UI layer solving a
closely related problem (visualizing detection-adjacent state). No new package, no new module.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
