# Phase 1 Data Model: DNN-Based Dart & Board Detection

All types below are plain Kotlin, added to (or reused from) `cv/src/main/java/com/bullseyestracker/cv/`.
None expose OpenCV or ONNX types across the `cv`/`app` boundary (constitution Principle II).

## `DetectionBackend` (new — crosses `cv`/`app` boundary)

```kotlin
enum class DetectionBackend { CLASSICAL, DNN }
```

Selects which `BoardDetector`/`DartDetector` implementation `CvEngineImpl` delegates to.
Read once per `calibrateBoard`/`detectThrows` call (not cached at construction) so a change
takes effect on the next capture (spec FR-003).

## `BoardCalibration` — reused unchanged

No field changes. The DNN backend's `OpenCvDnnBoardDetector` populates exactly the same fields
(`centerX`, `centerY`, `innerBullRadius`, `outerBullRadius`, `tripleRingInnerRadius/OuterRadius`,
`doubleRingInnerRadius/OuterRadius`, `rotationOffsetDegrees`) the classical backend does, derived
from the 4 detected calibration points per research.md §3.

## `RawDartDetection` — reused unchanged

No field changes. `OpenCvDnnDartDetector` populates `positionX`/`positionY`/`confidence` from
the decoded dart-tip detection, same normalized 0..1 space as the classical backend.

## `YoloV8Detection` (new — internal to `cv/opencv/dnn/`, never crosses the `app` boundary)

```kotlin
data class YoloV8Detection(
    val classId: Int,        // 0 = dart tip, 1..4 = calibration points (DeepDarts class layout)
    val centerX: Float,      // normalized 0..1, original-frame space (post-un-letterbox)
    val centerY: Float,
    val confidence: Float,
)
```

Output of `YoloV8OutputDecoder.decode(rawOutputTensor, letterboxInfo, confidenceThreshold, iouThreshold)`
— the unit-tested (test-first, `cv/src/test/`) piece that turns the raw ONNX output tensor into
usable detections (thresholding + NMS), independent of any OpenCV native call so it runs on a
plain JVM. `OpenCvDnnBoardDetector` filters `classId in 1..4`; `OpenCvDnnDartDetector` filters
`classId == 0`.

## `LetterboxInfo` (new — internal to `cv/opencv/dnn/`)

```kotlin
data class LetterboxInfo(
    val scale: Float,
    val padX: Float,
    val padY: Float,
    val originalWidth: Int,
    val originalHeight: Int,
)
```

Captured during preprocessing (research.md §2), consumed by `YoloV8OutputDecoder` to map model-
space coordinates back to original-frame normalized space.

## State / validation rules

- `OpenCvDnnBoardDetector.calibrate` MUST return `BoardCalibrationResult.NotFound` (not throw,
  not return a partial `BoardCalibration`) when fewer than 4 calibration-point detections survive
  thresholding/NMS — spec FR-006, mirrors the existing `BoardCalibrationResult` contract.
- `OpenCvDnnDartDetector.detect` MUST return an empty list (not an error) when no dart-tip class
  detections survive thresholding/NMS, consistent with `DartDetector`'s existing contract.
- The model asset (`cv/src/main/assets/models/deepdarts-yolov8.onnx`) failing to load MUST NOT
  crash the app — `CvEngineImpl` catches load failure at DNN-backend construction/first-use and
  falls back to behaving as if `DetectionBackend.DNN` were unavailable (spec Edge Cases), keeping
  `DetectionBackend.CLASSICAL` selectable/default (spec FR-004).
