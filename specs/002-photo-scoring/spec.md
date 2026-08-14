# Feature Specification: Photo-Based Dart Scoring

**Feature Branch**: `002-photo-scoring`

**Created**: 2026-08-13

**Status**: Implemented (retroactive spec)

**Input**: User description: "Retroactive spec for the already-implemented photo scoring
feature (User Story 2 of specs/001-dart-scoring-match/, tasks T026-T029): take a single photo
of the board after a turn instead of using live camera; all darts in the photo are detected and
scored via the existing CvEngine, reusing the same DetectionOverlay/CorrectionDialog and
manual-correction flow as live-camera scoring (US1). A capture-mode switch on the scoring entry
screen lets the player choose Photo vs Live Camera before a turn."

**Relationship to 001-dart-scoring-match**: This feature is User Story 2 of the parent
`001-dart-scoring-match` spec (see `specs/001-dart-scoring-match/spec.md` and `tasks.md`
T026-T029), split into its own spec directory/branch (`002-photo-scoring`) because it was
implemented as a self-contained slice of work. It depends on the `CvEngine`, `DetectionOverlay`,
and `CorrectionDialog` built for User Story 1 (live-camera scoring) and does not duplicate them.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Score a turn from a single photo (Priority: P1)

A player finishes their turn (up to 3 darts thrown) and, instead of having the app track each
dart live via the camera feed, takes one photo of the board after all darts have landed. The
app detects every dart visible in that photo and shows the same score overlay and manual-
correction flow used for live-camera scoring, so the player can confirm the turn's total.

**Why this priority**: Live-camera scoring requires holding the phone pointed at the board for
the whole turn, which is awkward in practice (mounting a phone, or a player standing in the
dart's flight path). A single after-the-fact photo is the natural alternative and was
prioritized alongside live-camera scoring (both P1 in the parent spec) as the two ways a turn
can be captured.

**Independent Test**: With the app in Photo mode, throw a turn's darts at a physical board,
take one photo, and verify the detected count and per-dart score match a known dart layout —
no live-camera session or match/player setup required.

**Acceptance Scenarios**:

1. **Given** the player has selected Photo mode and thrown their darts, **When** they take a
   photo of the board, **Then** the app detects every dart in the photo and displays a score
   overlay with a running total, matching what live-camera scoring would have produced for the
   same dart positions.
2. **Given** a scored photo with a low-confidence or incorrect detection, **When** the player
   taps that detection, **Then** the same correction dialog used in live-camera mode opens and
   lets them enter the correct sector/ring.
3. **Given** a scored photo, **When** the player confirms the turn, **Then** the confirmed
   throws are handed off exactly as they would be from live-camera mode (turn-confirmation
   downstream logic does not need to know which capture mode produced them).
4. **Given** a captured photo the player wants to discard, **When** they choose to retake it,
   **Then** the app returns to the camera preview with no detections carried over.

---

### User Story 2 - Choose capture mode before a turn (Priority: P1)

Before scoring a turn, the player picks whether this turn will be scored live (camera watches
the board as darts land) or by photo (one picture taken after the fact).

**Why this priority**: Without a mode switch, photo scoring would be unreachable — it's the
entry point that makes User Story 1 usable, so it ships as part of the same P1 slice.

**Independent Test**: Load the scoring entry screen and verify both Live Camera and Photo can
be selected, with the screen below switching to match the selected mode.

**Acceptance Scenarios**:

1. **Given** the scoring entry screen, **When** the player selects Photo, **Then** the photo-
   capture screen (viewfinder + "Take photo" button) is shown instead of the live-scoring
   screen.
2. **Given** the player is mid-turn in one mode, **When** they switch modes, **Then** any
   in-progress, unconfirmed detections from the previous mode are discarded.

---

### Edge Cases

- **No dartboard found in the photo**: the player is told to retake the photo with the board in
  frame; no score overlay is shown.
- **No darts detected in an otherwise valid photo**: the player sees a "no darts found" message
  and can retake the photo; "Confirm turn" stays disabled until at least one dart is detected.
- **Photo capture itself fails** (camera/hardware error): the player sees an error message and
  can retry without losing their place in the match.
- **More than 3 darts appear detected in a photo** (e.g. a stray detection): out of scope for
  this feature — the same detection/correction machinery as live-camera mode applies, and any
  cap on dart count per turn is the responsibility of the CvEngine/correction layer, not photo
  capture itself.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST let the player choose between Live Camera and Photo capture mode
  before scoring a turn.
- **FR-002**: In Photo mode, the system MUST let the player capture a single still photo of the
  board using the device camera.
- **FR-003**: The system MUST detect every dart visible in the captured photo and compute each
  dart's score using the same scoring logic used for live-camera detections (no separate,
  photo-specific scoring path).
- **FR-004**: The system MUST display detected darts as an overlay on the captured photo,
  reusing the same overlay presentation used in live-camera mode.
- **FR-005**: The system MUST let the player manually correct any detected dart's sector/ring
  using the same correction flow available in live-camera mode.
- **FR-006**: The system MUST let the player discard a captured photo and retake it before
  confirming the turn.
- **FR-007**: The system MUST let the player confirm the turn's detected (and any manually
  corrected) throws, after which they are handed off identically to a live-camera-confirmed
  turn.
- **FR-008**: The system MUST prevent confirming a turn with zero detected darts.
- **FR-009**: The system MUST tell the player when no dartboard is found in the captured photo,
  without presenting a score.

### Key Entities

- **Capture Mode**: Which of the two ways (Live Camera, Photo) the player is currently using to
  score a turn. Selected once per turn, before darts are thrown.
- **Captured Photo**: The single still image taken in Photo mode; discarded on retake, replaced
  by the detected-throws overlay once scored.
- **Detected Throw**: A single dart's scored result (sector, ring, value, confidence) — shared
  with live-camera mode, not specific to photo capture (see `specs/001-dart-scoring-match/data-model.md`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A player can score a full turn (up to 3 darts) from a single photo, from opening
  Photo mode to a confirmed turn, in under 15 seconds under normal conditions (excluding time
  spent manually correcting a detection).
- **SC-002**: Photo-mode scoring produces identical sector/ring/value results to live-camera
  scoring for the same dart positions (verified by automated test, not just manual QA).
- **SC-003**: 100% of manual corrections available in live-camera mode are also available in
  photo mode (no feature gap between the two capture modes' correction flow).
- **SC-004**: A player can switch between Live Camera and Photo mode without restarting the app
  or losing match/player state.

## Assumptions

- Photo mode reuses the same `CvEngine` calibration/detection pipeline as live-camera mode;
  no photo-specific board calibration or dart-detection model was built.
- Exactly one photo is taken per turn (no multi-photo stitching or retry-averaging).
- The device camera used for Photo mode is the same one used for Live Camera mode (no
  requirement to support an external camera or gallery-imported photo).
- Turn-confirmation downstream logic (match rules, persistence) is capture-mode-agnostic and
  was already built to accept a `List<DetectedThrow>` regardless of source — this feature does
  not change that contract.
