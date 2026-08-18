# Contract: DNN Detection Backend

Extends the existing `cv-engine-contract.md` (`specs/001-dart-scoring-match/contracts/`) — the
`CvEngine` interface itself is unchanged. This contract covers the new `DetectionBackend`
selection surface and the `BoardDetector`/`DartDetector` implementations it can select.

## `DetectionBackend` selection

```
enum class DetectionBackend { CLASSICAL, DNN }
```

`CvEngineImpl` (or its factory, `MatchRepository.create(context)`) accepts/exposes the current
`DetectionBackend`. Per spec FR-003, the effective backend for a `calibrateBoard`/`detectThrows`
call MUST be whatever was selected as of that call — no restart, no re-construction required to
observe a change.

**Contract rules**:
- Both `CLASSICAL` and `DNN` MUST conform to the exact same `BoardDetector`/`DartDetector`
  interfaces — callers (`CvEngineImpl`, and transitively `app`/`match`) MUST NOT need to know
  which backend produced a result.
- If `DNN` is selected but the bundled model asset fails to load, `CvEngineImpl` MUST behave as
  if `CLASSICAL` were selected for that session rather than throwing or returning empty/garbage
  results (spec Edge Cases, data-model.md state rules).
- `CLASSICAL`'s behavior, performance, and existing test suite MUST be unaffected by `DNN`'s
  presence (spec FR-010) — no shared mutable state between the two backends.

## `OpenCvDnnBoardDetector : BoardDetector`

**Purpose**: DNN equivalent of `OpenCvBoardDetector`, corresponds to spec FR-001/FR-004.

**Input**: `FrameInput` — identical contract to the classical `BoardDetector.calibrate`.

**Output**: `BoardCalibrationResult`, same two cases as the classical backend:
- `Calibrated(calibration: BoardCalibration, confidence: Float)` — only when all 4 calibration-
  point classes are detected above the confidence threshold (data-model.md validation rules).
- `NotFound` — otherwise. MUST NOT throw for a frame with no board, same as the classical
  contract.

**Contract rules** (in addition to the inherited `BoardDetector` contract):
- `confidence` SHOULD reflect the DNN model's own detection confidences for the 4 calibration
  points (e.g. their mean or minimum), giving the same low-confidence-indicator behavior
  (FR-012 in `specs/001-dart-scoring-match/spec.md`) regardless of backend.
- The derived `BoardCalibration` fields MUST be computed by the tested, deterministic
  calibration-point→geometry logic in research.md §3 — no ad hoc/undocumented derivation.

## `OpenCvDnnDartDetector : DartDetector`

**Purpose**: DNN equivalent of `OpenCvDartDetector`, corresponds to spec FR-001.

**Input**: `FrameInput` + `BoardCalibration` — identical contract to the classical
`DartDetector.detect`.

**Output**: `List<RawDartDetection>` — one entry per dart-tip-class detection surviving
thresholding/NMS. MUST return an empty list (not an error) when none are found, same as the
classical contract.

## `YoloV8OutputDecoder` (internal, `cv/opencv/dnn/`, not part of the `app`-facing boundary)

**Purpose**: Turn a raw ONNX output tensor into `List<YoloV8Detection>` — the one piece of new
logic that is unit-tested test-first (constitution Principle III / CLAUDE.md), since it
determines which raw pixels become calibration points vs. dart tips before any score is derived.

**Contract rules**:
- Pure function of `(rawOutputTensor, letterboxInfo, confidenceThreshold, iouThreshold)` — no
  OpenCV native calls, no I/O, runs on a plain JVM (`cv/src/test/`).
- MUST apply confidence thresholding before NMS, and MUST apply per-class NMS (calibration
  points and dart tips must not suppress each other).
- Coordinates in the returned `YoloV8Detection` MUST already be un-letterboxed into the
  original frame's normalized 0..1 space — callers never see model-input-space coordinates.
