# Phase 1 Data Model: Photo-Based Dart Scoring

This feature introduces no new persisted entities and does not change the domain model defined
in `specs/001-dart-scoring-match/data-model.md` (`Match`, `Player`, `Turn`, `Throw`,
`Detection Frame`). It reuses that model as-is:

- **Detection Frame**: `data-model.md`'s `captureMode` field (`PHOTO` \| `LIVE_CAMERA`) already
  anticipated this feature — a photo-scored turn simply sets `captureMode = PHOTO` on its
  source frame.
- **Throw** / **Turn**: identical shape and validation rules regardless of which capture mode
  produced them (spec FR-007 requires this explicitly).

## New transient (non-persisted) state

These exist only in the `app` module's UI state, not in the Room schema:

| Concept | Represented as | Lifetime |
|---|---|---|
| Capture Mode | `CaptureMode` enum (`LIVE_CAMERA`, `PHOTO`) — `app/.../ui/detection/CaptureModeSelector.kt` | Compose `remember` state in `MainActivity`, reset never (persists across turns within a session, no requirement to remember across app restarts) |
| Captured Photo | `Bitmap` held in `PhotoScoringScreen`'s local state | From `ImageCapture.takePicture()` callback until "Confirm turn" or "Retake" clears it — never written to disk/Room directly by this feature (persisting the photo as a `DetectionFrame.imageRef` is existing `MatchRepository.saveDetectionFrame` responsibility, unchanged by this feature) |

## Relationships

No change to `specs/001-dart-scoring-match/data-model.md`'s relationship diagram. This feature
adds a second *producer* of `Turn`/`Throw`/`Detection Frame` instances (photo capture, alongside
live capture) but no new entity types or relationships.
