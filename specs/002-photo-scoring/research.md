# Phase 0 Research: Photo-Based Dart Scoring

Retroactive record of the design decisions made while implementing this feature. No
`NEEDS CLARIFICATION` markers remained in the Technical Context (plan.md), so this documents
the choices actually made rather than resolving open unknowns.

## Decision: Reuse `CvEngine.detectThrows()` unchanged, no photo-specific CV path

**Rationale**: `CvEngine`'s contract (`cv/src/main/.../CvEngine.kt`) already takes a
`FrameInput` (a `Bitmap` + rotation) and returns `List<DetectedThrow>`, with no notion of
"live" vs. "photo" baked in — it's a pure function of one frame. A photo capture and a single
frame pulled from the live `ImageAnalysis` stream are both just a `Bitmap`. Adding a
photo-specific method or scoring path would have duplicated `ScoreMapper`/`BoardDetector`/
`DartDetector` logic for no behavioral difference.

**Alternatives considered**:
- A separate `CvEngine.detectFromPhoto()` method — rejected: would fork the scoring logic
  constitution Principle III requires be single-source-of-truth-tested, for zero behavioral
  gain.
- Running detection continuously while the photo-mode viewfinder is open (treating "photo mode"
  as a live pipeline that just happens to show one frame) — rejected: defeats the purpose of
  photo mode (a deliberate single capture after the throw, not a continuous scan) and would
  reintroduce the live-mode performance budget (constitution Principle IV) for no reason.

**Validation**: `PhotoDetectionTest.kt` asserts a photo-sourced `FrameInput` and an equivalent
live-sourced `FrameInput` produce identical `detectThrows()` output, proving the reuse is safe.

## Decision: CameraX `ImageCapture` use case for the photo, not a frame grab from `ImageAnalysis`

**Rationale**: CameraX's `ImageCapture` use case is purpose-built for a deliberate,
user-triggered single photo (full-resolution `takePicture()` callback), separate from the
continuous `ImageAnalysis` stream `CameraController` already binds for live mode. Grabbing the
latest `ImageAnalysis` frame instead would tie photo quality to whatever resolution/format live
detection uses and couple the two capture modes' lifecycles together unnecessarily.

**Alternatives considered**:
- Reusing the live `ImageAnalysis` stream's last frame — rejected: lower resolution than a
  dedicated capture, and couples photo-mode's image quality to live-mode's analysis pipeline
  tuning.

## Decision: Reuse `DetectionOverlay`/`CorrectionDialog` composables unchanged

**Rationale**: Both composables already take detections as plain data (`List<DetectedThrow>`)
and a tap callback — nothing about them is live-camera-specific. Spec FR-004/FR-005 (and
constitution Principle V) require the same overlay/correction experience regardless of capture
mode; reusing them directly guarantees that rather than requiring two implementations to be
kept in sync.

**Alternatives considered**: A photo-specific overlay variant — rejected, no requirement
motivated one and it would risk the two modes' correction UX drifting apart over time.

## Decision: Material3 `SingleChoiceSegmentedButtonRow`/`SegmentedButton` for the mode toggle

**Rationale**: Standard Material3 component for an exclusive two-choice toggle, matching the
rest of the app's Material3 usage. Required `@OptIn(ExperimentalMaterial3Api::class)` since
this API is still experimental in the pinned Compose BOM — a known, accepted tradeoff (missing
this annotation was the one compile-blocking bug hit during implementation, since fixed).

**Alternatives considered**: A plain `Row` of two `Button`s — rejected as a marginal choice;
`SegmentedButtonRow` was picked for the built-in exclusive-selection semantics and closer match
to platform conventions for a mode switch, not because the plain-`Row` alternative was
inadequate.

## Decision: Turn confirmation handoff stays capture-mode-agnostic

**Rationale**: Both `LiveScoringScreen` and `PhotoScoringScreen` call the same
`onTurnConfirmed: (List<DetectedThrow>) -> Unit` callback supplied by `MainActivity`. Match
rules / persistence (`match` module) never need to know which capture mode produced a turn's
throws — this was already the shape of the callback from `001-dart-scoring-match`'s Phase 2
foundational work (T012 AppContainer wiring), so no change was needed there.

**Alternatives considered**: Tagging confirmed throws with their originating capture mode —
rejected, no requirement (spec FR-007 explicitly requires identical downstream handling) and
no `match`-module use case needs to distinguish the two.
