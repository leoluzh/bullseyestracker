# Implementation Plan: App Home Navigation

**Branch**: `013-app-home-navigation` | **Date**: 2026-08-16 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/013-app-home-navigation/spec.md`

## Summary

Replace the current ad-hoc entry point (a player-setup form with two unrelated buttons bolted on)
with a proper navigation flow: splash screen → home screen (New Game / Match History / Player
Stats / Test Calibrator) → destination screens. New Game now goes through a game-mode list screen
before the existing player-setup form (which loses its embedded mode picker). Test Calibrator is
a new standalone screen reusing the existing live-camera calibration pipeline and overlay with no
match/score side effects. All of this is a UI/navigation-only change in the `app` module; no `cv`
or `match` module changes are needed.

## Technical Context

**Language/Version**: Kotlin (Android), Jetpack Compose

**Primary Dependencies**: Jetpack Compose (Material3), CameraX (via existing `CameraController`),
existing `com.bullseyestracker.cv.CvEngine` interface, existing `MatchRepository`

**Storage**: N/A — no new persistence

**Testing**: No app-module test suite exists today (Compose screens are exercised manually via
`make build`/`make connected-test`); this feature does not introduce scoring-logic changes so
Constitution Principle III (test-first for scoring logic) does not apply

**Target Platform**: Android (same min/target SDK as rest of `app` module)

**Project Type**: mobile-app (single Android app with `app`/`cv`/`match` Gradle modules)

**Performance Goals**: N/A beyond existing camera-preview responsiveness already provided by
`CameraController`/`LiveDetectionAnalyzer`; the splash screen must not introduce a blocking delay
beyond ~1–1.5s (spec Assumptions)

**Constraints**: Fully offline (constitution Principle I) — splash duration is a fixed local
timer, not network-gated. Must not violate module boundaries (`app` may not import
`org.opencv.*`/`androidx.room.*` directly, per CLAUDE.md).

**Scale/Scope**: 6 screens touched/added (new: Splash, Home, GameModeList, CalibrationTest;
modified: `MainActivity` navigation state, `MatchSetupScreen` signature)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. On-Device Processing**: N/A change — Test Calibrator reuses the existing on-device
  `CvEngine.calibrateBoard`; no network calls introduced. PASS.
- **II. CV Logic Isolated From UI**: The new `CalibrationTestScreen` calls `CvEngine` /
  `CameraController` exactly the way `LiveScoringScreen` already does today (same interface, same
  module boundary) — no new coupling to `org.opencv.*`. PASS.
- **III. Test-First for Scoring Logic**: Not triggered — this feature adds no code that maps a
  detected position to a score; `ScoreMapper`/`BoardDetector`/`DartDetector`/`X501Rules`/
  `CricketRules` are untouched. N/A.
- **IV. Real-Time Performance Budget**: Not triggered — no changes to the detection pipeline
  itself; `CalibrationTestScreen` reuses `LiveDetectionAnalyzer`'s existing calibration callback
  path unmodified. N/A.
- **V. Explainable, Correctable Detection**: Test Calibrator continues to show the existing
  calibration boundary overlay (spec 012) live; it doesn't touch throw-correction UI at all
  (no throws are detected/scored on this screen by design, per FR-008). PASS.
- **Development Workflow (UI-only exemption)**: This is a UI-only feature per CLAUDE.md/
  constitution — exempt from CV-isolation depth checks and real-time-budget gates beyond the
  module-boundary check above.

No violations. Complexity Tracking section not needed.

## Project Structure

### Documentation (this feature)

```text
specs/013-app-home-navigation/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks — not created here)
```

### Source Code (repository root)

```text
app/src/main/java/com/bullseyestracker/
├── MainActivity.kt                          # MODIFIED: AppScreen sealed class gains
│                                             # Splash/Home/GameModeList/CalibrationTest states;
│                                             # splash-timer + resume-into-match logic
├── ui/
│   ├── home/                                # NEW package
│   │   ├── SplashScreen.kt                  # NEW
│   │   ├── HomeScreen.kt                    # NEW
│   │   └── GameModeListScreen.kt            # NEW
│   ├── calibration/                         # NEW package
│   │   └── CalibrationTestScreen.kt         # NEW — reuses CameraController +
│   │                                        # LiveDetectionAnalyzer's onCalibrated/
│   │                                        # onBoardNotFound callbacks + BullseyeOverlay
│   └── match/
│       └── MatchSetupScreen.kt              # MODIFIED: takes GameMode as an incoming
│                                             # parameter, drops the SegmentedButton mode picker
```

No `cv`/`match` module changes. No new Gradle modules, dependencies, or persisted entities.

**Structure Decision**: Single Android app, existing module layout (`app`/`cv`/`match`)
unchanged. New screens live under two new `app`-module UI packages (`ui.home`, `ui.calibration`)
alongside the existing `ui.detection`/`ui.history`/`ui.match`/`ui.stats` packages, keeping the
same per-feature-area package convention already used in this codebase.

## Complexity Tracking

*No violations — section not needed.*
