# Phase 0 Research: DNN-Based Dart & Board Detection

## 1. ONNX export format for OpenCV's `dnn` module

**Decision**: Export YOLOv8n or YOLOv8s (Ultralytics) to ONNX with `opset=12`, static input
shape (e.g. `640x640x3`), `simplify=True`, and NMS left **out** of the graph (Ultralytics'
`export(format="onnx", nms=False)` default) — decode raw output and run NMS in Kotlin instead.

**Rationale**: OpenCV's `dnn` ONNX importer supports a known-good subset of ops; the
Ultralytics YOLOv8 export path (opset 12, no dynamic axes, no built-in NMS layer) is the
combination the OpenCV community has verified loads cleanly via
`Dnn.readNetFromONNX`/`readNet` (dynamic-shape and some newer-opset graphs are a common source
of load failures). Doing NMS ourselves also gives a single, unit-testable Kotlin decoder
(`YoloV8OutputDecoder`) instead of depending on graph-embedded NMS behaving identically across
OpenCV versions.

**Alternatives considered**: TFLite via ML Kit (constitution explicitly allows this as an
alternative) — rejected for this feature because the user-specified requirement is OpenCV's
`dnn` module specifically, and the project already depends on OpenCV (no second ML runtime to
integrate/keep in sync).

## 2. Preprocessing / letterboxing

**Decision**: Standard YOLO letterbox — resize the frame to fit the model's square input
(preserving aspect ratio, padding with gray `114,114,114`), track the scale + padding offsets,
and un-letterbox output coordinates back into the original frame's normalized 0..1 space
(the same space `BoardCalibration`/`RawDartDetection` already use) before handing them to the
rest of the pipeline.

**Rationale**: This is the standard YOLOv8 preprocessing contract; deviating from it would
require retraining. `Imgproc.resize` + `Core.copyMakeBorder` (already available via the
project's OpenCV dependency) implement it without new dependencies.

## 3. Reconciling DeepDarts' 4 calibration points with the existing circular `BoardCalibration` model

**Problem**: `ScoreMapper` (unchanged by this feature) computes sector/ring purely from polar
distance + angle relative to a single `centerX/centerY` and a set of ring radii — i.e. it
assumes the board renders as concentric circles in the frame (implicitly a near-fronto-parallel
camera angle). DeepDarts' 4 calibration points are the classic "4-point homography" scheme from
the original paper (McNally et al.), designed to also correct for off-axis/perspective camera
angles — a strictly more general model than what `BoardCalibration`/`ScoreMapper` support today.

**Decision**: For this feature, derive `BoardCalibration`'s existing fields (center, ring
radii, rotation offset) directly from the 4 detected calibration points under the same
near-fronto-parallel assumption the classical Hough-circle detector already makes — center =
centroid of the 4 points (or intersection of the two diagonals), radii = distances from center
to the calibration points scaled by the known real-board ratio between the calibration-point
ring and each scoring ring, rotation = angle of one known calibration point relative to its
canonical sector position. Do **not** implement full perspective/homography correction in this
feature.

**Rationale**: This keeps `ScoreMapper` (and every other downstream consumer of
`BoardCalibration`) completely unchanged, satisfying spec FR-005/FR-010 and keeping the DNN
backend a drop-in alternative behind the existing interfaces rather than a parallel scoring
pipeline. It is not a capability regression versus the classical backend, which makes the same
fronto-parallel assumption today. It does leave perspective-corrected scoring (the DNN model's
main theoretical advantage for angled cameras) on the table — documented as a follow-up, not a
requirement of this feature (spec doesn't call for off-axis camera support).

**Alternatives considered**: Full homography warp of the dart-tip point into a canonical
top-down board space, with `BoardCalibration` carrying a fixed canonical geometry — rejected as
larger scope (would need a new coordinate-transform stage between calibration and
`ScoreMapper`, plus its own test-first mapping logic) than this feature's stated goal of a
swappable detector behind the existing interface; can be a fast-follow feature if angled-camera
support becomes a priority.

## 4. Backend selection: where it lives and how it's persisted

**Decision**: Add `enum class DetectionBackend { CLASSICAL, DNN }` to `CvEngine.kt` (plain
Kotlin, safe to cross the `app`/`cv` boundary per Principle II). `CvEngineImpl` takes the
selected backend (constructor parameter or a settable property) and picks the classical vs.
DNN `BoardDetector`/`DartDetector` pair accordingly. `app` persists the user's choice using
whatever mechanism it already uses for other user-facing settings (checked at implementation
time — e.g. `SharedPreferences`/DataStore, consistent with existing `app`-module conventions)
and passes it to `MatchRepository.create(context)` / `CvEngine` construction.

**Rationale**: Matches spec FR-003 ("without requiring an app restart") — a settable property
read at the start of each `calibrateBoard`/`detectThrows` call (rather than baked in at
construction time) lets the toggle take effect on the very next capture.

## 5. Model asset size / latency budget risk

**Decision**: Start with YOLOv8n (nano, ~3-6MB as ONNX at 640x640), not YOLOv8s, to give the
best chance of meeting the 200ms live-camera budget (spec FR-008) on CPU-only OpenCV `dnn`
inference on a mid-tier device. Re-benchmark with YOLOv8s only if YOLOv8n's accuracy on the
fixture/real-photo benchmark (SC-001) is insufficient.

**Rationale**: Directly serves FR-008's explicit fallback ("if this cannot be met, the DNN
backend MUST be restricted to single-photo capture mode only") — starting with the smaller,
faster model maximizes the chance live-camera mode stays supported at all, which is the more
valuable of the two capture modes (spec's own User Story 1 priority in
`specs/001-dart-scoring-match/`).

**Alternatives considered**: YOLOv8s/m for higher accuracy — deferred; `PerformanceBenchmarkTest`
(existing, `specs/006-fixture-benchmark/`) will be the actual gate, not a guess made here.
