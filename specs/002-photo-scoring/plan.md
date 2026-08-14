# Implementation Plan: Photo-Based Dart Scoring

**Branch**: `002-photo-scoring` | **Date**: 2026-08-13 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-photo-scoring/spec.md`

**Note**: This is a retroactive plan — the feature was implemented before this document was
written (see `specs/001-dart-scoring-match/tasks.md` T026-T029). It documents the design
decisions actually made, not a forward-looking proposal.

## Summary

Add a second turn-capture mode — single photo instead of live camera — to the existing
dart-scoring app. Reuses the `cv` module's `CvEngine` (`calibrateBoard`/`detectThrows`)
unchanged: a captured photo is just another `FrameInput`, so no new CV API, detection path, or
scoring logic was needed. New work is entirely in the `app` module: a CameraX `ImageCapture`
use case, a `PhotoScoringScreen` that reuses US1's `DetectionOverlay`/`CorrectionDialog`, and a
`CaptureModeSelector` toggle wired into `MainActivity`.

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged from 001-dart-scoring-match)

**Primary Dependencies**: CameraX `ImageCapture` use case (new — `camera-core` already a
project dependency from US1's `PreviewView`/`ImageAnalysis` usage); Jetpack Compose Material3
`SegmentedButton`/`SingleChoiceSegmentedButtonRow` (new — required an
`@OptIn(ExperimentalMaterial3Api::class)`, that API is still experimental in the Compose BOM
this project pins). No new dependency on `cv` or `match` module APIs — `CvEngine`,
`DetectedThrow`, `FrameInput` are reused as-is.

**Storage**: N/A for this feature — turn confirmation hands off to the same
`onTurnConfirmed: (List<DetectedThrow>) -> Unit` callback US1 already had wired to
`MatchRepository`/match logic (that wiring itself is outside this feature's scope).

**Testing**: JUnit on plain JVM — `PhotoDetectionTest.kt` (`cv/src/test/`) proves
`detectThrows()` gives identical results for a photo-sourced vs. live-sourced `FrameInput`,
reusing the same `FakeBoardDetector`/`FakeDartDetector` fakes as `CvEngineContractTest`. No new
Compose UI test was added for `PhotoScoringScreen`/`CaptureModeSelector` (manual verification
only) — see Complexity Tracking.

**Target Platform**: Android, minSdk 26 (unchanged)

**Project Type**: Mobile app — no new Gradle module; all new code lives in the existing `app`
module (`cv`/`match` untouched)

**Performance Goals**: N/A beyond constitution Principle IV — photo mode is single-shot
(`detectThrows()` runs once per captured photo, not per frame), so it carries no live-pipeline
latency budget of its own.

**Constraints**: Fully offline/on-device (constitution Principle I — inherited, no change); CV
logic isolated from UI (constitution Principle II — `PhotoScoringScreen` calls `CvEngine`
through the same interface US1 uses, never touches OpenCV types); every automatic score
user-correctable before commit (constitution Principle V — `CorrectionDialog` reused unchanged)

**Scale/Scope**: One new screen, one new controller, one new UI toggle — the smallest slice of
`001-dart-scoring-match`'s User Story 2

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — photo capture and scoring both stay on-device; no new network surface |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — `PhotoScoringScreen` (app module) only calls `CvEngine.calibrateBoard`/`detectThrows`; no `org.opencv.*` import added to `app` |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | PASS — `PhotoDetectionTest.kt` written against the existing `CvEngineImpl`/fakes; no new scoring logic was introduced (photo mode reuses US1's `ScoreMapper` path entirely), so no new scoring unit was required beyond the equivalence test |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | N/A — photo mode is single-shot, not a live per-frame pipeline; the <200ms live-detection budget doesn't apply. `ImageCapture.takePicture()` runs on CameraX's own executor, not the UI thread |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | PASS — `DetectionOverlay`/`CorrectionDialog` reused unchanged from US1; spec FR-004/FR-005 |

No violations requiring Complexity Tracking justification beyond the one item noted below.

## Project Structure

### Documentation (this feature)

```text
specs/002-photo-scoring/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command) — thin; reuses 001's data model
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # (not generated separately — this feature maps 1:1 to
                           #  specs/001-dart-scoring-match/tasks.md T026-T029)
```

### Source Code (repository root)

```text
app/
├── src/main/java/com/bullseyestracker/camera/
│   └── PhotoCaptureController.kt      # NEW — CameraX ImageCapture wiring
├── src/main/java/com/bullseyestracker/ui/detection/
│   ├── PhotoScoringScreen.kt          # NEW — photo capture + score/correct/confirm screen
│   └── CaptureModeSelector.kt         # NEW — Live Camera / Photo toggle
├── src/main/java/com/bullseyestracker/MainActivity.kt   # MODIFIED — captureMode state, switch
└── src/main/java/com/bullseyestracker/camera/CameraController.kt  # MODIFIED — shared preview
    bind path reused by both live and photo screens

cv/
└── src/test/java/com/bullseyestracker/cv/
    └── PhotoDetectionTest.kt          # NEW — photo/live equivalence unit test

match/                                  # UNTOUCHED by this feature
```

**Structure Decision**: No new module. This feature is additive within `app` (one new screen,
one new controller, one new UI component) plus one new `cv` unit test proving no behavioral
divergence; it does not warrant its own module because it introduces no new architectural
boundary — it's a second UI entry point onto the module boundary (`CvEngine`) that
`001-dart-scoring-match`'s plan already established.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| No Compose UI/instrumented test for `PhotoScoringScreen`/`CaptureModeSelector` | Constitution's test-first mandate (Principle III) applies to scoring-logic mapping, not UI wiring — `PhotoDetectionTest.kt` covers the actual scoring-equivalence risk. Adding Espresso/Compose UI tests for the screen itself was judged out of scope for this slice, matching `001-dart-scoring-match`'s tasks.md (only T044's manual quickstart validation covers UI end-to-end, not automated UI tests per screen) | A per-screen Compose UI test was considered but deferred with the same rationale `tasks.md` used for US1's `LiveScoringScreen` — no automated UI test exists for that screen either, so adding one only for photo mode would be an inconsistent, unrequested scope increase |
