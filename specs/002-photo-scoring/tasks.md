
---

description: "Task list template for feature implementation"
---

# Tasks: Photo-Based Dart Scoring

**Input**: Design documents from `/specs/002-photo-scoring/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Note**: This tasks.md is retroactive — the feature was already implemented (see
`specs/001-dart-scoring-match/tasks.md` T026-T029, and branch `002-photo-scoring` / PR #27)
before this document was written. Tasks below are marked `[X]` to reflect what was actually
built, not a forward plan.

**Tests**: Constitution Principle III (Test-First for Scoring Logic) requires a test proving
photo-sourced detection behaves identically to live-sourced detection, since this feature's
only scoring-adjacent risk is that equivalence — not a new scoring algorithm.

**Organization**: Tasks are grouped by user story per spec.md (US1 = score-from-photo, US2 =
capture-mode switch). Both are P1 — US2 is the entry point that makes US1 reachable, so they
shipped together.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2)

## Path Conventions

Reuses the 3-module layout from `specs/001-dart-scoring-match/plan.md` — no new modules:
- `app/src/main/java/com/bullseyestracker/...` (UI, camera)
- `cv/src/test/java/com/bullseyestracker/cv/...` (unit tests)

---

## Phase 1: Setup (Shared Infrastructure)

**N/A** — this feature adds no new project/module setup. It builds entirely on the Gradle
modules, dependencies, and tooling already established by `specs/001-dart-scoring-match/tasks.md`
Phase 1.

---

## Phase 2: Foundational (Blocking Prerequisites)

**N/A** — this feature depends on `specs/001-dart-scoring-match/tasks.md` Phase 2 (in
particular T007's `CvEngine`/`FrameInput`/`DetectedThrow` types and T012's `AppContainer`
wiring) and Phase 3's US1 UI components (`DetectionOverlay`, `CorrectionDialog`,
`LiveScoringScreen`), all already complete before this feature started. No new foundational
work was needed.

---

## Phase 3: User Story 1 - Score a turn from a single photo (Priority: P1) 🎯 MVP

**Goal**: Detect and score every dart in a single captured photo, using the same
`CvEngine`/overlay/correction machinery as live-camera scoring.

**Independent Test**: With 3 darts already in the board, take one photo and verify all 3 are
detected with correct scores, no live-camera session involved.

### Tests for User Story 1 (mandatory per constitution Principle III) ⚠️

- [X] T001 [P] [US1] Unit test proving a photo-sourced `FrameInput` scores identically to an
  equivalent live-sourced `FrameInput` via `CvEngine.detectThrows()`, reusing
  `CvEngineContractTest`'s fakes, in `cv/src/test/java/com/bullseyestracker/cv/PhotoDetectionTest.kt`

### Implementation for User Story 1

- [X] T002 [US1] Wire a CameraX `ImageCapture` use case for single-photo capture (bound
  alongside the existing `PreviewView`) in
  `app/src/main/java/com/bullseyestracker/camera/PhotoCaptureController.kt` (depends on T001)
- [X] T003 [US1] Build `PhotoScoringScreen` — viewfinder until capture, then
  `cvEngine.calibrateBoard()`/`detectThrows()` on the captured `Bitmap`, reusing
  `DetectionOverlay`/`CorrectionDialog` unchanged, with retake/confirm actions — in
  `app/src/main/java/com/bullseyestracker/ui/detection/PhotoScoringScreen.kt` (depends on T002)

**Checkpoint**: User Story 1 is fully functional and testable independently (photo mode reachable
via direct composable use, even before US2's switch exists)

---

## Phase 4: User Story 2 - Choose capture mode before a turn (Priority: P1)

**Goal**: Let the player pick Live Camera vs. Photo before scoring a turn.

**Independent Test**: Load the scoring entry screen, verify both modes are selectable and the
screen below switches to match the selected mode.

### Implementation for User Story 2

- [X] T004 [US2] Add a `CaptureMode` (`LIVE_CAMERA`, `PHOTO`) Material3
  `SingleChoiceSegmentedButtonRow` toggle in
  `app/src/main/java/com/bullseyestracker/ui/detection/CaptureModeSelector.kt`, and wire
  `MainActivity.kt` to hold `captureMode` state and switch between `LiveScoringScreen` and
  `PhotoScoringScreen` (US1, T003) behind one shared `onTurnConfirmed` callback (depends on T003)

**Checkpoint**: User Stories 1 and 2 both work independently and together — photo mode is
reachable from the app's normal entry point

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup / Foundational**: N/A — inherited from `001-dart-scoring-match` (already complete)
- **User Story 1 (Phase 3)**: Depends only on `001-dart-scoring-match`'s foundation + US1
  (`CvEngine`, `DetectionOverlay`, `CorrectionDialog`)
- **User Story 2 (Phase 4)**: Depends on this feature's own US1 (T003, `PhotoScoringScreen`
  must exist to switch to)

### Parallel Opportunities

- T001 has no dependency on T002/T003 and could run in parallel with other test-writing work,
  but T002/T003 depend on it existing first per constitution Principle III (test-first)
- T002 and T003 are sequential (T003 calls into `PhotoCaptureController`), not parallelizable

---

## Implementation Strategy

Actual delivery order (already executed): T001 → T002 → T003 → T004, i.e. prove the
scoring-equivalence risk first (constitution-mandated), then build capture → screen →
mode-switch entry point, matching the phase order above exactly.

---

## Notes

- All 4 tasks are complete and merged to branch `002-photo-scoring` (PR #27,
  https://github.com/leoluzh/bullseyestracker/pull/27).
- No Polish-phase tasks were added for this feature — `quickstart.md`'s scenarios still need a
  manual on-device validation pass (not yet performed), tracked the same way
  `specs/001-dart-scoring-match/tasks.md` T042 tracks its own full quickstart validation.
