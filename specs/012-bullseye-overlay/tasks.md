# Tasks: Bullseye Calibration Overlay

**Input**: Design documents from `/specs/012-bullseye-overlay/`
**Prerequisites**: plan.md, spec.md, research.md, quickstart.md

**Tests**: No constitution-mandated tests apply (Principle III covers only scoring/rules logic;
this feature adds none). Matching the precedent set by `DetectionOverlay`/`LiveScoringScreen`/
`PhotoScoringScreen` (no automated Compose UI tests exist for any of them), no test tasks are
generated; validation is the quickstart.md manual walkthrough.

**Organization**: Single user story (spec.md US1) — one-slice feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

No setup needed — `BullseyeOverlay.kt` lands directly in the existing
`app/src/main/java/com/bullseyestracker/ui/detection/` package (no new directory).

## Phase 2: Foundational

No new foundational/blocking work — `BoardCalibration` already exists
(`cv/src/main/java/com/bullseyestracker/cv/CvEngine.kt`) and is reused as-is.

---

## Phase 3: User Story 1 - See the detected bullseye boundary while scoring (Priority: P1)

**Goal**: Both capture screens draw the calibrated bullseye's inner/outer boundary as concentric
circle outlines once calibration succeeds, and remove them when calibration is absent/lost.

**Independent Test**: quickstart.md sections 1-2 — live camera and photo, calibrated/not-found/
lost-board cases, coexistence with dart markers.

- [X] T001 [P] [US1] Build `BullseyeOverlay` — takes a nullable `BoardCalibration` and draws
      nothing when null (spec FR-003), otherwise draws two concentric circle outlines (`Canvas`/
      `drawCircle` with `Stroke` style — research.md) at the calibration's normalized center,
      sized to `innerBullRadius` and `outerBullRadius`, converted to pixel space the same way
      `DetectionOverlay`'s markers already are (`constraints.maxWidth`/`maxHeight`) — in
      `app/src/main/java/com/bullseyestracker/ui/detection/BullseyeOverlay.kt`
- [X] T002 [US1] Wire `LiveScoringScreen`: add `calibration: BoardCalibration?` state, set it in
      the existing `onCalibrated` callback (currently discards its `BoardCalibration` parameter),
      clear it to `null` in the existing `onBoardNotFound` callback (spec FR-003/Acceptance
      Scenario 4), and render `BullseyeOverlay(calibration, ...)` alongside the existing
      `DetectionOverlay` (spec Acceptance Scenario 5: both visible, dart markers stay tappable) —
      in `app/src/main/java/com/bullseyestracker/ui/detection/LiveScoringScreen.kt` (depends on
      T001)
- [X] T003 [US1] Wire `PhotoScoringScreen`: add `photoCalibration: BoardCalibration?` state, set
      it from `scorePhoto`'s existing `BoardCalibrationResult.Calibrated` branch (currently used
      only to call `detectThrows`, then discarded), clear it to `null` in the existing
      `BoardCalibrationResult.NotFound` branch and in the existing `retake()` function, and render
      `BullseyeOverlay(photoCalibration, ...)` alongside the existing `DetectionOverlay` — in
      `app/src/main/java/com/bullseyestracker/ui/detection/PhotoScoringScreen.kt` (depends on
      T001)

**Checkpoint**: Feature functional end-to-end on both capture modes — quickstart.md sections 1-2
all runnable manually; `make build` (compile + unit tests + lint) passes.

---

## Phase 4: Polish & Cross-Cutting

- [ ] T004 Run quickstart.md sections 1-2 manually on a device/emulator and confirm SC-001 through
      SC-003 (quick visual confirmation; circles align with dart-marker coordinate space; no
      responsiveness regression) — no code change expected, verification only
      PARTIAL: no device/emulator attached in this environment (`adb devices` returns none), same
      blocker as prior features this session. `make build` confirms the feature compiles cleanly
      end-to-end and the radius-normalization basis was cross-checked against
      `OpenCvBoardDetector`'s own doc comment (width-only normalization) rather than assumed.
      Visual on-device confirmation still needs a real device or emulator to close out.
- [X] T005 [P] `make lint` / ktlint format check over all new/modified files
      (`BullseyeOverlay.kt`, `LiveScoringScreen.kt`, `PhotoScoringScreen.kt`) — verified via
      `./gradlew build`, which runs ktlint + Android lint across all modules; BUILD SUCCESSFUL,
      no findings against the new/modified files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A
- **Foundational (Phase 2)**: N/A
- **US1 (Phase 3)**: T001 first (both screens depend on it); T002/T003 are independent of each
  other (different files) once T001 lands
- **Polish (Phase 4)**: Depends on Phase 3 complete

### Critical Path

T001 → {T002, T003} → T004

## Parallel Execution Examples

- T002 and T003 (different files, both depend only on T001) can be built in parallel.
- T005 (lint) can run in parallel with T004 (manual verification) once Phase 3 lands.

## Implementation Strategy

**Single pass**: Land T001-T003 as one commit/PR — `BullseyeOverlay` has no independent value
until at least one screen renders it, and both screens are small enough wiring changes that
splitting them into separate PRs would only add overhead.
