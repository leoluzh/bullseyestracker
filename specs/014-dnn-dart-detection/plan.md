# Implementation Plan: DNN-Based Dart & Board Detection

**Branch**: `014-dnn-dart-detection` | **Date**: 2026-08-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/014-dnn-dart-detection/spec.md`

## Summary

Add a second, runtime-selectable `BoardDetector`/`DartDetector` implementation
(`cv/opencv/dnn/`) backed by a YOLOv8 model run through OpenCV's `dnn` module
(`org.opencv.dnn.Net`, already available — the project's `org.opencv:opencv:5.0.0.1` AAR
ships the full OpenCV build including `dnn`, so no new Gradle dependency is needed). The model
is trained offline (outside the app) on the DeepDarts dataset (Roboflow Universe,
`testing-zzmc9/deepdarts-yolov8`, CC BY 4.0, 5 classes: dart tip + 4 board calibration points),
exported to ONNX, and checked into `cv/src/main/assets/models/` as a bundled, offline asset.
`CvEngineImpl` gains a detection-backend selection point (`DetectionBackend.CLASSICAL` |
`DetectionBackend.DNN`) so `app` can switch backends without any `cv`/`match` code change,
per spec FR-003. The classical Hough/contour backend is untouched (FR-010).

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged)

**Primary Dependencies**: No new Gradle dependency — `org.opencv:opencv:5.0.0.1` (already a
`cv`-module dependency) includes `org.opencv.dnn`. New: a bundled ONNX model asset (not a
library dependency).

**Storage**: One bundled asset file (`cv/src/main/assets/models/deepdarts-yolov8.onnx`, on the
order of a few MB for YOLOv8n/s), plus one new persisted user setting (`DetectionBackend`
selection — spec Key Entities) using whatever preference storage `app` already uses for
settings (no new persistence mechanism).

**Testing**: `cv/src/test/` (plain JVM) for anything that maps a position to a score per
constitution Principle III/CLAUDE.md — specifically the calibration-point→`BoardCalibration`
derivation and the DNN output-decoding (box/keypoint parsing, confidence thresholding, NMS)
logic, written test-first against fixed synthetic model-output fixtures so it needs no real
inference. `cv/src/androidTest/` (instrumented, mirrors `OpenCvBoardDetectorTest`/
`OpenCvDartDetectorTest`) for the actual `Net.forward()` call against the bundled ONNX asset,
extending `DetectionAccuracyBenchmarkTest` (`specs/006-fixture-benchmark/`) to run against both
backends. Per that spec's known gap, only synthetic fixtures exist today — DNN accuracy
validation against real photos remains blocked on the same missing asset (spec Assumptions).

**Target Platform**: Android, minSdk unchanged. DNN inference runs on CPU via OpenCV's `dnn`
backend (`DNN_BACKEND_OPENCV` / `DNN_TARGET_CPU`) for baseline correctness; GPU/NNAPI
acceleration is a possible follow-up, not required by this feature's latency budget.

**Project Type**: Mobile app — additive to the existing `cv` module (`opencv/dnn/` package) and
small additive changes to `app` (a settings toggle) and `match`/`CvEngineImpl` (backend
selection plumbing). No new Gradle module.

**Performance Goals**: Meet constitution Principle IV's existing 200ms frame-to-result budget
on the plan's target mid-tier device (spec FR-008/SC-003) for live-camera mode; single-photo
mode has no hard per-frame budget today and is unaffected either way.

**Constraints**: Fully offline — `Net.readNetFromONNX` loads the bundled asset only, zero
network calls (Principle I, spec FR-002/SC-004). CV logic isolated from UI — new detector
classes live in `cv/opencv/dnn/`, never imported directly by `app` (Principle II, same
boundary `CvNativeInit`/`CvEngine` already enforce). Model asset license (CC BY 4.0) requires
attribution (spec FR-009).

**Scale/Scope**: Two new detector implementations (`OpenCvDnnBoardDetector`,
`OpenCvDnnDartDetector`), one new output-decoding unit (shared model-output parsing, since both
classes read the same YOLOv8 ONNX output tensor), one bundled model asset, one settings toggle
in `app`, and test additions in both `cv/src/test/` and `cv/src/androidTest/`. No changes to
`match` module's game rules; only `CvEngineImpl`'s wiring changes there (if the backend
selection is threaded through `MatchRepository.create(context)` — see research.md).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — `cv::dnn`/`org.opencv.dnn.Net` runs the bundled `.onnx` asset entirely on-device; no HTTP client, no cloud inference call anywhere in the new code (spec FR-002) |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — new detectors implement the existing `BoardDetector`/`DartDetector` interfaces and live under `cv/opencv/dnn/`; `app` only sees the existing `CvEngine`/`FrameInput`/`BoardCalibration`/`DetectedThrow` types plus one new plain enum (`DetectionBackend`) for the settings toggle — no OpenCV or `org.opencv.dnn.*` type crosses into `app` |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | GATE APPLIES — `ScoreMapper` itself is untouched and reused as-is, but the new calibration-point→`BoardCalibration` derivation (turns 4 DNN keypoints into the center/radii/rotation `ScoreMapper` consumes) is new logic that materially determines scoring geometry; it MUST have unit tests written and failing before implementation, same as `ScoreMapper`/`BoardDetector` today |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | GATE APPLIES — DNN inference is new per-frame cost; `PerformanceBenchmarkTest` (`specs/006-fixture-benchmark/`) must be extended to cover the DNN backend before it can be considered done (spec FR-008); if the budget can't be met in live-camera mode, DNN is restricted to single-photo capture only (spec FR-008 fallback), which keeps this gate satisfiable either way |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | PASS — DNN detections flow through the same `DetectedThrow`/overlay/correction path as classical detections (spec FR-007); no new commit path is introduced |

No violations requiring Complexity Tracking justification — the DNN backend is additive behind
existing interfaces, exactly the pattern the constitution's Technology Constraints section
anticipates ("a TensorFlow Lite/ML Kit object-detection model MAY be introduced later behind the
same CV interface"; OpenCV's `dnn` module + ONNX serves the same role here).

## Project Structure

### Documentation (this feature)

```text
specs/014-dnn-dart-detection/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
cv/
├── src/main/
│   ├── assets/models/
│   │   └── deepdarts-yolov8.onnx        # NEW — bundled model weights
│   └── java/com/bullseyestracker/cv/
│       ├── CvEngine.kt                   # + DetectionBackend enum (plain Kotlin, crosses boundary)
│       ├── CvEngineImpl.kt               # + backend selection: picks classical vs. dnn detector pair
│       └── opencv/
│           ├── OpenCvBoardDetector.kt    # unchanged (classical)
│           ├── OpenCvDartDetector.kt     # unchanged (classical)
│           └── dnn/                      # NEW package
│               ├── YoloV8OutputDecoder.kt        # NEW — shared ONNX output tensor -> boxes/keypoints (plain-JVM testable)
│               ├── OpenCvDnnBoardDetector.kt      # NEW — Net.forward() + decoder -> BoardCalibration
│               └── OpenCvDnnDartDetector.kt       # NEW — Net.forward() + decoder -> RawDartDetection
├── src/test/java/com/bullseyestracker/cv/
│   └── opencv/dnn/
│       └── YoloV8OutputDecoderTest.kt     # NEW — test-first, fixed tensor fixtures, no OpenCV native runtime
└── src/androidTest/java/com/bullseyestracker/cv/
    ├── OpenCvDnnBoardDetectorTest.kt      # NEW — mirrors OpenCvBoardDetectorTest, real Net.forward()
    ├── OpenCvDnnDartDetectorTest.kt       # NEW — mirrors OpenCvDartDetectorTest
    └── DetectionAccuracyBenchmarkTest.kt  # EXTENDED — parametrized over both backends

app/src/main/java/com/bullseyestracker/
└── ui/settings/                           # NEW or existing settings screen
    └── DetectionBackendSetting.kt         # NEW — reads/writes DetectionBackend via CvEngine, no OpenCV import
```

**Structure Decision**: Single project, additive to existing `cv` and `app` modules — no new
Gradle module. This matches every prior feature in this repo (`specs/001`–`013`) and the
constitution's module-boundary rules in `CLAUDE.md`: `app` still never imports `org.opencv.*`
directly, it only gains one new plain-Kotlin `DetectionBackend` enum choice surfaced through
`CvEngine`.

## Complexity Tracking

No violations — table omitted per template instructions.
