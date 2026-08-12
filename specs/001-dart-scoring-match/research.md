# Phase 0 Research: Dart Scoring & Match Tracking

## Computer vision backend

**Decision**: OpenCV for Android (classical CV) as the v1 detection backend, entirely behind
the `CvEngine` interface (see `contracts/cv-engine-contract.md`).

**Rationale**: Board detection is a well-bounded classical-CV problem (a dartboard is a known
set of concentric circles/annuli with fixed angular sectors) — Hough circle transform to find
the board's outer/inner ring boundaries plus a homography/perspective correction from the
detected circle handles most non-perpendicular camera angles. Dart-tip detection against a
known, calibrated board (color/contrast delta between consecutive frames, or blob/contour
detection against the board-only baseline photo) avoids needing a trained model or labeled
dataset for v1. This satisfies constitution Principle I (on-device, no server) with a
deterministic, debuggable pipeline that's easy to unit-test with fixture images per Principle
III.

**Alternatives considered**:
- **TensorFlow Lite / ML Kit object detection (trained model)**: Better robustness to
  occlusion, odd angles, and lighting once trained, but requires a labeled dart-image dataset
  and training/eval infra before v1 can ship anything. Constitution Technology Constraints
  explicitly allow introducing this later behind the same `CvEngine` interface if classical CV
  proves insufficient in real-world testing — deferred, not rejected.
- **Cloud-based vision API (e.g., server-side detection)**: Rejected outright — violates
  constitution Principle I (on-device processing, non-negotiable) and spec FR-013 (fully
  offline).

## Camera capture (live feed + single photo)

**Decision**: CameraX, using its `ImageAnalysis` use case for the live-detection pipeline and
`ImageCapture` use case for the single-photo flow, sharing one `CvEngine.detect(frame)` call
so both capture modes hit identical detection/scoring logic (per spec Assumptions: photo and
live camera differ only in capture UX).

**Rationale**: CameraX is the current recommended Android camera API — handles device
fragmentation (varying camera HW/orientation quirks) that the older Camera1/Camera2 APIs push
onto app code, and its `ImageAnalysis` use case is designed for exactly this kind of
frame-by-frame ML/CV pipeline with built-in backpressure (drop frames instead of queuing) so
the UI thread is never blocked — directly supporting constitution Principle IV.

**Alternatives considered**:
- **Camera2 API directly**: More control, far more boilerplate and device-specific edge-case
  handling for no benefit at this app's scope. Rejected.
- **MediaPipe camera utilities**: Overlaps with CameraX for capture; MediaPipe's value is its
  own ML pipelines, which aren't needed for the classical-CV v1 approach. Rejected for now.

## Local persistence

**Decision**: Room (SQLite) for match/player/turn/throw state and completed-match history.

**Rationale**: Standard, well-supported Android persistence layer with first-class Kotlin
coroutines/Flow support for observing in-progress match state from Compose UI; satisfies FR-011
(resume in-progress match) and FR-014 (match history) without any network dependency.

**Alternatives considered**:
- **DataStore (proto/preferences)**: Fine for simple key-value settings but a poor fit for
  relational match/player/turn/throw history and querying past matches. Rejected as primary
  store (may still be used later for simple user preferences — out of scope here).
- **Plain file/JSON serialization**: Simpler to start but loses queryability for match history
  (FR-014) and safe partial-state updates for in-progress match resume (FR-011). Rejected.

## UI toolkit

**Decision**: Jetpack Compose.

**Rationale**: Current standard for new Android UI; its state-driven model fits the app's core
interaction loop well — camera preview + detection overlay redrawing as `CvEngine` emits new
throw detections, and match/score screens driven by Room `Flow`s.

**Alternatives considered**:
- **Classic Android View system (XML layouts)**: Still viable but more boilerplate for the
  overlay-on-camera-preview UI central to User Stories 1-2. Rejected in favor of Compose.

## Performance budget validation approach

**Decision**: The <200ms frame-to-result budget (constitution Principle IV) will be validated
against real mid-tier device hardware during implementation (instrumented benchmark test in the
`cv` module feeding fixture frames through the full detection pipeline), not assumed from
design alone.

**Rationale**: Classical CV pipeline cost (Hough transform, contour detection) is highly
dependent on input resolution and device CPU; a concrete target device tier and downscaling
strategy need real measurement rather than a priori estimation. This is a Phase 2
(tasks/implementation) concern, tracked here so it isn't lost.

**Open follow-up (non-blocking for planning)**: Pick a concrete minimum reference device (or
device class) to benchmark against once implementation starts; not required to unblock Phase 1
design.
