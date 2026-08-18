# Feature Specification: DNN-Based Dart & Board Detection

**Feature Branch**: `014-dnn-dart-detection`

**Created**: 2026-08-18

**Status**: Draft

**Input**: User description: "DNN-based dart/board detection using OpenCV's dnn module with a YOLOv8 object-detection model, added as a swappable alternative to the existing classical OpenCV (Hough circle / contour) BoardDetector and DartDetector implementations behind the existing CvEngine interface. Model source: train YOLOv8n/s locally on the DeepDarts dataset (Roboflow Universe, testing-zzmc9/deepdarts-yolov8, CC BY 4.0, 1902 images, 5 classes: dart tip + 4 board calibration points), export to ONNX, bundle the .onnx weights in the app, run fully offline via cv::dnn (no cloud inference, no network round-trip). Runtime-selectable between classical and DNN detection paths."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - More reliable auto-scoring via DNN detection (Priority: P1)

A player using the app under lighting or board conditions where classical detection
(Hough circle / contour) misfires — glare, low contrast between board and wall, partially
occluded sectors — gets a correct automatic score because the app uses a trained
object-detection model to find the board's calibration points and each dart's tip instead of
classical edge/contour heuristics.

**Why this priority**: The classical detector's failure modes are exactly what a trained model
is meant to fix. This is the entire value of the feature — without a measurable accuracy
improvement over classical-only detection, there is no reason to ship it.

**Independent Test**: Run the DNN backend against the existing fixture-image benchmark harness
(`specs/006-fixture-benchmark/`) and against a held-out set of real dartboard photos, and
confirm detection/scoring accuracy meets or exceeds the classical backend's on the same images.

**Acceptance Scenarios**:

1. **Given** a captured photo of a dartboard with darts thrown, **When** the DNN backend is
   active, **Then** the app detects the 4 board calibration points and each dart tip, maps them
   to a score, and displays it — without any network request being made.
2. **Given** a live camera view with the DNN backend active, **When** a new dart sticks in the
   board, **Then** the app detects it and updates the overlay/score within the same real-time
   latency budget as the classical backend.
3. **Given** an image where classical detection would misfire (e.g. glare on the board), **When**
   the DNN backend processes it, **Then** it still correctly identifies the calibration points
   and dart tip.

---

### User Story 2 - Switch between classical and DNN detection (Priority: P2)

A user picks which detection backend the app uses — classical OpenCV or the new DNN model —
and the choice takes effect immediately, without reinstalling or reconfiguring anything else.

**Why this priority**: The DNN backend is new and unproven on real-world photos (only trained
on the DeepDarts dataset so far); classical CV must remain available as a fallback the user can
choose if the DNN backend underperforms on their board/lighting/device.

**Independent Test**: Toggle the detection backend in settings and confirm subsequent
detections use the newly selected backend, verifiable by observing which detector code path
produced the result (e.g. via the existing overlay/debug info).

**Acceptance Scenarios**:

1. **Given** the app is set to classical detection, **When** the user switches to DNN detection
   in settings, **Then** the next capture/frame is processed by the DNN backend.
2. **Given** the DNN backend is active, **When** the user switches back to classical, **Then**
   the next capture/frame is processed by the classical backend with no restart required.

---

### User Story 3 - Detections remain explainable and correctable (Priority: P3)

Regardless of which backend produced a score, the user sees the same overlay of detected
positions on the board image and can manually correct any dart the detector got wrong before
it's committed to the match score.

**Why this priority**: This preserves the app's existing trust model (Constitution Principle V)
— probabilistic detection, DNN or classical, will misfire, and the user must always be able to
fix it.

**Independent Test**: With the DNN backend active, deliberately present an ambiguous/occluded
dart, confirm the app still shows an overlay and allows manual correction of the detected
sector/ring before the score is committed.

**Acceptance Scenarios**:

1. **Given** the DNN backend produced a detection, **When** the result is shown to the user,
   **Then** it is visually overlaid on the board image the same way classical detections are.
2. **Given** a DNN detection is wrong, **When** the user taps to correct it, **Then** the
   corrected value — not the DNN output — is what's committed to the match score.

---

### Edge Cases

- What happens when the DNN model finds fewer than 4 calibration points (can't establish board
  orientation/scale)? The app must not silently produce a wrong score — it must surface a
  "couldn't detect board" state consistent with how the classical backend handles the same
  failure today, and let the user fall back to manual entry or retry.
- What happens when the DNN backend detects a dart tip but the calibration points are missing
  or vice versa? Treat as a failed detection for that frame/photo, same as above.
- What happens on a low-end device where DNN inference can't stay under the latency budget?
  The app must not hang the UI thread; if the DNN backend can't keep up, the user must still be
  able to switch back to the classical backend (User Story 2) or use single-photo capture
  instead of live camera.
- What happens if the bundled model asset is missing or fails to load? The app must not crash —
  it should behave as if the DNN backend is unavailable and keep the classical backend
  selectable/default.
- What happens with multiple darts close together or overlapping tips? Same ambiguity classical
  detection already faces — the user-correction flow (User Story 3) is the safety net, not a
  new requirement specific to DNN.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a DNN-based detection backend that implements the existing
  board-detection and dart-detection responsibilities (calibration-point detection, dart-tip
  detection) as an alternative to the classical OpenCV backend.
- **FR-002**: System MUST run all DNN inference entirely on-device, with no network request
  made as part of detection (consistent with the app's existing offline-only guarantee).
- **FR-003**: System MUST let the user select which detection backend (classical or DNN) is
  active, and MUST apply the selection to subsequent detections without requiring an app
  restart.
- **FR-004**: System MUST default to the classical detection backend until the DNN backend has
  a documented accuracy result (see SC-001) showing it is at least as accurate.
- **FR-005**: System MUST map DNN-detected calibration points and dart-tip positions to a score
  (sector, ring, multiplier) using the same score-mapping guarantees as the classical backend,
  including unit tests written before the mapping logic per the project's test-first
  requirement for anything that turns a detected position into a score.
- **FR-006**: System MUST treat a DNN detection that finds fewer than 4 calibration points, or
  finds a dart tip without valid calibration points, as a failed detection, and MUST surface
  that to the user the same way the classical backend surfaces a failed detection today (no
  silent wrong score).
- **FR-007**: System MUST overlay DNN-produced detections on the board image and allow the user
  to manually correct any detected dart's sector/ring/multiplier before it is committed to the
  match score, identically to the classical backend's existing correction flow.
- **FR-008**: System MUST keep DNN frame-to-result latency under the project's real-time
  performance budget (200ms on the plan's target mid-tier device) for the live-camera capture
  mode; if this cannot be met, the DNN backend MUST be restricted to single-photo capture mode
  only rather than degrading the live camera experience.
- **FR-009**: System MUST bundle the DNN model weights as an offline app asset (no on-demand
  download), sourced/trained from data whose license permits this redistribution, and MUST
  include the required attribution for that data in the app's licensing/credits.
- **FR-010**: System MUST NOT let the DNN backend's presence or failure affect the classical
  backend's existing behavior, tests, or performance.

### Key Entities

- **Detection Backend Selection**: A user-facing, persisted setting indicating which detector
  (classical or DNN) is currently active; read by the capture flow before each detection.
- **Board Calibration Point**: One of 4 reference points the DNN model detects that establish
  the board's position, scale, and rotation in the image — analogous to what the classical
  Hough-circle detector derives geometrically.
- **Dart Tip Detection**: A detected dart landing position (from the DNN model) that, combined
  with the board calibration points, is mapped to a sector/ring/multiplier score.
- **Bundled Detection Model**: The offline DNN model asset (weights + metadata) shipped with the
  app, including its data-source license/attribution.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: On the project's fixture/accuracy benchmark harness and on a held-out set of real
  dartboard photos, the DNN backend's scoring accuracy is greater than or equal to the classical
  backend's accuracy on the same images.
- **SC-002**: Users can switch detection backends in 2 taps or fewer, with the new backend
  active on the very next capture.
- **SC-003**: In live-camera mode with the DNN backend active, 95% of frames produce a
  detection result within the app's real-time performance budget on the reference mid-tier
  device.
- **SC-004**: Zero network requests are observed (via device network monitoring) during any DNN
  detection, on any capture mode.
- **SC-005**: When the DNN backend fails to detect a usable board/dart, 100% of those failures
  result in a clear "couldn't detect" state or fallback — never a silently wrong committed
  score.

## Assumptions

- The DNN model is trained offline (outside the app build) on the DeepDarts dataset
  (Roboflow Universe, CC BY 4.0) using YOLOv8n or YOLOv8s, then exported to ONNX and checked
  into the repo as a bundled asset; no on-device training occurs.
- "Mid-tier device" and the 200ms real-time budget reference the same target-device definition
  already established in the project constitution/plan for the classical backend — this feature
  does not redefine that budget, only must meet it.
- Real-world accuracy validation depends on the same real dartboard photo gap noted in
  `specs/006-fixture-benchmark/` (synthetic fixtures only, so far) — SC-001's "held-out set of
  real dartboard photos" assumes at least a small set becomes available before this feature can
  be considered done; if none is available, SC-001 is validated on the synthetic fixture
  benchmark only and this limitation is documented, matching the existing precedent for that
  harness.
- The existing single-photo and live-camera capture modes (`specs/001-dart-scoring-match/`,
  `specs/002-photo-scoring/`) both need to support backend selection; no new capture mode is
  introduced by this feature.
- Model attribution (CC BY 4.0 credit to the DeepDarts project / McNally et al.) is added to
  wherever the app already surfaces open-source/data attributions, or a new simple credits
  location is created if none exists.
