# Quickstart: DNN-Based Dart & Board Detection

Validates spec `014-dnn-dart-detection`: the DNN detection backend (User Story 1), backend
switching (User Story 2), and overlay/correction parity (User Story 3).

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `cv/src/main/assets/models/deepdarts-yolov8.onnx` present (see step 0 — trained/exported
  outside the app build, checked into the repo as a bundled asset).
- `OpenCvDnnBoardDetector`/`OpenCvDnnDartDetector`/`YoloV8OutputDecoder` implemented.
- `DetectionBackend` settings toggle implemented in `app`.

## 0. Produce the model asset (one-time, outside the app build)

Not part of `make build` — done once (and re-done only if retraining) to produce the bundled
`.onnx` checked into the repo. `scripts/train_deepdarts_yolov8.ipynb` runs every step below
end-to-end (including validation-metric checks and an OpenCV-`dnn` load verification) — use it
directly rather than re-deriving these commands:

1. Download the DeepDarts YOLOv8 dataset (Roboflow Universe, `testing-zzmc9/deepdarts-yolov8`,
   CC BY 4.0, 1,902 images / 5 classes).
2. Train YOLOv8n locally with Ultralytics (`yolo detect train ...`) against the dataset.
3. Export: `yolo export model=best.pt format=onnx opset=12 simplify=True nms=False imgsz=640`.
4. Copy the resulting `.onnx` to `cv/src/main/assets/models/deepdarts-yolov8.onnx`.
5. Add the CC BY 4.0 attribution (DeepDarts / McNally et al., dataset URL) to the app's
   credits/licenses location (spec FR-009).

Expected: a single `.onnx` file a few MB in size, loadable by `Dnn.readNetFromONNX` — verified
in step 2 below.

## 1. Output decoder — unit tests (automated, no device needed) — supports User Story 1

```bash
make test
```

Expected: `YoloV8OutputDecoderTest` (`cv/src/test/java/com/bullseyestracker/cv/opencv/dnn/`)
passes against fixed synthetic tensor fixtures — confidence thresholding, per-class NMS, and
letterbox coordinate un-mapping, written test-first per constitution Principle III.

## 2. DNN detectors load the model and detect — instrumented (device/emulator) — User Story 1

```bash
make connected-test
```

Expected: `OpenCvDnnBoardDetectorTest` and `OpenCvDnnDartDetectorTest`
(`cv/src/androidTest/java/com/bullseyestracker/cv/`) pass against the existing synthetic
fixtures (`cv/src/androidTest/assets/fixtures/`, `specs/006-fixture-benchmark/`) — confirms the
bundled `.onnx` loads via `Dnn.readNetFromONNX` and produces `Calibrated`/dart detections on at
least the synthetic set. Real-photo accuracy remains blocked on the same missing-real-fixtures
gap noted in `specs/006-fixture-benchmark/README.md` (spec Assumptions) — document rather than
fail the feature over it.

## 3. Accuracy parity — DNN vs. classical — User Story 1, spec SC-001

```bash
make connected-test
```

Expected: the extended `DetectionAccuracyBenchmarkTest` reports accuracy for both
`DetectionBackend.CLASSICAL` and `DetectionBackend.DNN` on the same fixture set; DNN's accuracy
is ≥ classical's (SC-001). If real fixture photos still aren't available, this validates against
the synthetic set only — same documented limitation as `specs/006-fixture-benchmark/`.

## 4. Latency budget — User Story 1, spec SC-003/FR-008

```bash
make connected-test
```

Expected: the extended `PerformanceBenchmarkTest` shows ≥95% of DNN-backend frames completing
under 200ms on the reference mid-tier device. If not met, confirm the app falls back to
restricting `DetectionBackend.DNN` to single-photo capture only (FR-008) rather than degrading
the live-camera experience — inspect `CvEngineImpl`'s capture-mode gating for this case.

## 5. Backend switching — manual walkthrough (device/emulator) — User Story 2

1. Launch the app, open Settings, confirm `DetectionBackend` defaults to Classical (FR-004).
2. Switch to DNN. Capture/photo-score a dartboard. Confirm the result came from the DNN path
   (e.g. via debug/overlay info) without restarting the app.
3. Switch back to Classical. Capture again. Confirm the result reverts to the classical path
   immediately.

Expected: switching takes 2 taps or fewer and applies to the very next capture (SC-002).

## 6. Overlay & correction parity — manual walkthrough — User Story 3

1. With `DetectionBackend.DNN` active, capture a dartboard photo (ideally one with an ambiguous
   dart position — near a ring boundary or partially occluded).
2. Confirm the detected dart(s) are visually overlaid on the image, same as the classical
   backend produces.
3. Tap to manually correct a detected dart's sector/ring.
4. Confirm the corrected value — not the DNN model's raw output — is what gets committed to the
   match score.

## 7. Offline guarantee — manual/tooling check — spec SC-004

1. Enable device airplane mode (or otherwise block network access).
2. With `DetectionBackend.DNN` active, run through steps 5-6 above.
3. Confirm detection still works and no network request is attempted (e.g. via
   `adb shell` network stats, or Android Studio's Network Profiler) during any DNN detection.

## 8. No regression to the classical backend — spec FR-010

```bash
make build
```

Expected: `OpenCvBoardDetectorTest`, `OpenCvDartDetectorTest`, and all existing classical-backend
tests/behavior are unaffected by the DNN backend's presence — this is the existing test suite
from `specs/001`-`013`, it should need zero changes to keep passing.
