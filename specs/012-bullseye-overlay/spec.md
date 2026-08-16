# Feature Specification: Bullseye Calibration Overlay

**Feature Branch**: `012-bullseye-overlay`

**Created**: 2026-08-16

**Status**: Draft

**Input**: User description: "Detect the edges/border of the bullseye and draw it. Clarified
scope: this is a visualization of the board calibration the app already computes, not a new
detection algorithm. The existing OpenCvBoardDetector already calibrates the board (Hough-circle
detection) and computes innerBullRadius/outerBullRadius as part of BoardCalibration
(cv/src/main/.../CvEngine.kt), but neither LiveScoringScreen nor PhotoScoringScreen currently
keep or display that calibration — LiveScoringScreen discards the BoardCalibration entirely
(onCalibrated = { _, confidence -> ... }), and PhotoScoringScreen only uses it internally to call
detectThrows without storing it in UI state. This feature surfaces that already-computed data by
drawing the bullseye's inner and outer boundary circles on the detection overlay, on both the
live-camera and photo capture screens."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - See the detected bullseye boundary while scoring (Priority: P1)

A player pointing the camera at the board (or reviewing a captured photo) wants visual
confirmation that the app has correctly found the board's center and bullseye — not just a text
status message ("Board calibrated") but something they can actually check against the physical
board in front of them. After this feature, once the board is calibrated, two concentric circle
outlines appear on the camera/photo view marking the detected inner and outer bullseye
boundaries, so a misaligned or wrong-sized calibration is immediately obvious rather than
discovered later through a wrong score.

**Why this priority**: This is the entire scope of the change — a single visualization improving
trust in and debuggability of calibration that's already computed but currently invisible. There
is no smaller independently-valuable slice.

**Independent Test**: Point the camera at a physical dartboard (or review a captured photo of
one) until calibration succeeds; verify two concentric circle outlines appear centered on the
board's bullseye, one at the inner bull boundary and one at the outer bull boundary, matching
what's visible on the physical board.

**Acceptance Scenarios**:

1. **Given** the live camera is pointed at a dartboard and calibration succeeds, **When** the
   player looks at the camera view, **Then** two concentric circle outlines are drawn over the
   live feed, positioned and sized to match the detected inner and outer bullseye boundaries.
2. **Given** a captured photo is calibrated successfully, **When** the player reviews the photo,
   **Then** the same two concentric circle outlines are drawn over the photo, in the same
   position they'd appear for an equivalent live frame (consistent with this app's existing
   "photo scores identically to live" principle).
3. **Given** no board has been calibrated yet (live camera hasn't found one, or a photo's board
   wasn't found), **When** the player looks at the view, **Then** no bullseye circles are drawn —
   only calibrated boundaries are ever shown, never a guess.
4. **Given** the live camera loses the board after having calibrated it (e.g., the camera is
   moved away), **When** that happens, **Then** the previously-drawn bullseye circles are removed,
   consistent with the app no longer having a valid calibration to show.
5. **Given** the bullseye circles are being drawn, **When** dart detection markers are also
   present on the same view, **Then** both are visible simultaneously without one obscuring the
   other's usability (the existing dart markers remain tappable for correction).

---

### Edge Cases

- What happens if the inner and outer bull radii are very close together (rare but
  geometrically possible from a detection standpoint)? Both circles are still drawn as computed —
  this feature draws whatever the existing calibration data says, it doesn't second-guess or
  smooth it.
- What happens on a low-confidence calibration (already surfaced today via a status message)?
  The bullseye circles are still drawn from that calibration — low confidence is an existing,
  separate signal (status text), not a reason to hide the boundary being visualized.
- What happens when the player rotates the phone or the frame's rotation changes? The circles are
  drawn in the same normalized board-position coordinate space [DetectedThrow] markers already
  use, so they inherit the same rotation-handling behavior already relied on for dart markers —
  no new coordinate-mapping logic needed.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The live-camera scoring screen MUST draw the detected bullseye's inner and outer
  boundary as two concentric circle outlines over the camera preview once calibration succeeds.
- **FR-002**: The photo-capture scoring screen MUST draw the same two concentric circle outlines
  over the captured photo once its calibration succeeds, matching the live-camera behavior (spec
  001's "photo scores identically to live" principle).
- **FR-003**: The bullseye boundary circles MUST NOT be drawn before a board has been
  successfully calibrated, and MUST be removed if a live calibration is lost/reset.
- **FR-004**: The bullseye boundary circles MUST be visually distinguishable from (not obscure the
  usability of) existing dart-detection markers when both are present on screen simultaneously.
- **FR-005**: This feature MUST NOT introduce a new board-detection algorithm or change how
  calibration is computed — it only visualizes the `BoardCalibration` data the app already
  computes.

### Key Entities

- **Bullseye Boundary Overlay**: A transient, UI-only visual element (not persisted, not a new
  domain entity) — two circle outlines derived directly from the current `BoardCalibration`'s
  center and inner/outer bull radii, redrawn whenever calibration changes and cleared when there
  is none.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A player can visually confirm correct calibration (or spot a wrong one) within
  seconds of pointing the camera at the board, without needing to throw a dart first.
- **SC-002**: The drawn boundary circles match the underlying `BoardCalibration` data with no
  positional or size discrepancy — verified by comparing the circles against the same
  calibration's already-used dart-position mapping (both draw from the same normalized
  coordinate space).
- **SC-003**: Adding this visualization introduces no perceptible change to the live-camera
  detection loop's responsiveness (constitution Principle IV's <200ms frame-to-result budget is
  a `cv`-module concern; this is a Compose-side draw operation on already-computed data, not a
  new per-frame computation).

## Assumptions

- "Border of the bullseye" refers to both the inner bull (50-point) and outer bull (25-point)
  boundaries together — the two concentric circles that together make up what's colloquially
  called "the bullseye" on a dartboard — not just one or the other.
- This feature draws circle outlines only (not filled regions), so the underlying camera feed or
  photo, and the existing dart markers, remain fully visible through the marked area.
- No new user-facing controls are introduced (no toggle to show/hide the overlay) — it always
  shows automatically whenever a valid calibration exists, mirroring how dart-detection markers
  already behave unconditionally once darts are detected.
