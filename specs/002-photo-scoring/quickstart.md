# Quickstart: Photo-Based Dart Scoring

Validation guide proving this feature works end-to-end. Not an implementation guide — code is
already built; see `specs/001-dart-scoring-match/tasks.md` T026-T029 for what was implemented.
This scenario is a more detailed expansion of `specs/001-dart-scoring-match/quickstart.md`
Scenario 3.

## Prerequisites

- Physical Android device (emulator camera feed is not representative for CV validation),
  minSdk 26+, with rear camera.
- A standard, regulation-pattern dartboard, indoor lighting, camera roughly perpendicular to
  the board face.
- App built and installed: `./gradlew :app:installDebug`.

## Scenario 1 — Switch to Photo mode (validates FR-001)

1. Open the app (Live Camera mode is the default entry state).
2. Tap "Photo" on the mode selector at the top of the scoring screen.
3. Expect: the screen below switches from the live camera preview to the photo-capture
   viewfinder with a "Take photo" button; no crash, no state carried over from Live mode.

## Scenario 2 — Score a turn from a photo (validates User Story 1, FR-002/003/004)

1. In Photo mode, throw 3 darts at the board.
2. Tap "Take photo".
3. Expect: the app detects all 3 darts and overlays each one's position/score on the captured
   image, with a running total shown (matches Scenario 2 of the parent quickstart for
   live-camera mode).

## Scenario 3 — Correct a detection in photo mode (validates FR-005)

1. From Scenario 2's scored photo, tap one of the overlaid detections.
2. Expect: the same correction dialog used in live-camera mode opens.
3. Change the sector/ring and confirm. Expect: the score total updates immediately.

## Scenario 4 — Retake a photo (validates FR-006)

1. From Scenario 2's scored photo, tap "Retake".
2. Expect: the app returns to the viewfinder; no darts/overlay from the previous photo remain.

## Scenario 5 — No dartboard found (edge case)

1. In Photo mode, take a photo of a blank wall (no board in frame).
2. Expect: the app reports no dartboard found and shows no score overlay (validates FR-009,
   not a wrong/garbage calibration).

## Scenario 6 — Confirm turn hands off identically to live mode (validates FR-007, SC-004)

1. Complete Scenario 2, tap "Confirm turn".
2. Expect: the confirmed turn is handed to the same downstream flow a live-camera-confirmed
   turn would use (visible via whatever match-mode UI is active — e.g. 501 scoreboard turn
   advance, once User Story 3 is built) — no separate "photo turn" code path is user-visible.
