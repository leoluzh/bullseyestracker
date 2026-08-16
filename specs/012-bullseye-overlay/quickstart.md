# Quickstart: Bullseye Calibration Overlay

Validates spec `012-bullseye-overlay` end-to-end: bullseye boundary visualization on both capture
modes.

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `BullseyeOverlay` implemented and wired into `LiveScoringScreen`/`PhotoScoringScreen` (this
  feature's deliverable).
- No `make test` prerequisite — this feature adds no new automated tests (plan.md Technical
  Context, matching every other `app`-module screen in this project); validation here is
  manual/device-based only.

## 1. Live-camera calibration boundary (device/emulator)

1. **No board yet**: Launch the live-camera scoring screen before pointing at a board. Expect: no
   bullseye circles drawn (spec Acceptance Scenario 3).
2. **Board calibrated**: Point the camera at a physical dartboard until the status reads "Board
   calibrated" (or "low confidence — hold steady"). Expect: two concentric circle outlines appear
   over the live feed, centered on the board's bullseye, matching the physical inner/outer bull
   rings visible on the board (Acceptance Scenario 1).
3. **Board lost**: Move the camera away from the board until it's no longer visible. Expect: the
   bullseye circles disappear (Acceptance Scenario 4).
4. **Coexists with dart markers**: With the board calibrated, throw a dart (or otherwise trigger a
   detection). Expect: the dart marker appears, remains tappable for correction, and doesn't
   disappear or become untappable because the bullseye circles are also drawn (Acceptance
   Scenario 5).

## 2. Photo calibration boundary (device/emulator)

1. **Board found**: Take a photo of a dartboard with a clear, unobstructed view. Expect: after
   scoring, two concentric circle outlines appear over the captured photo at the detected
   bullseye position (Acceptance Scenario 2).
2. **Board not found**: Take a photo where no board is visible (e.g., pointed at a wall). Expect:
   no bullseye circles are drawn — only the existing "No dartboard found" status message
   (Acceptance Scenario 3).
3. **Retake clears it**: After a successful calibrated photo, tap "Retake". Expect: returning to
   the live preview shows no leftover bullseye circles from the previous photo.

## Success criteria mapping

- SC-001 (visually confirm calibration quickly): steps 1.2 and 2.1 — the circles should be
  immediately visible and comparable against the physical board.
- SC-002 (circles match the same coordinate space as dart markers): step 1.4 — a dart marker and
  the bullseye circles, both derived from the same calibration, should visibly align (e.g. an
  inner-bull dart's marker should land inside the drawn inner-bull circle).
- SC-003 (no perceptible responsiveness change): informal — the live camera preview should feel
  exactly as responsive as before this feature, since no `cv`-pipeline change occurred.
