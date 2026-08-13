---

description: "Task list template for feature implementation"
---

# Tasks: Dart Scoring & Match Tracking

**Input**: Design documents from `/specs/001-dart-scoring-match/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/cv-engine-contract.md, quickstart.md

**Tests**: Constitution Principle III (Test-First for Scoring Logic, NON-NEGOTIABLE) requires
unit tests written and failing before implementation for anything that maps a detected
position to a score. This applies to the `cv` module's `ScoreMapper`/`BoardDetector`/
`DartDetector` and the `match` module's `X501Rules`/`CricketRules`. Test tasks below covering
those units are therefore mandatory, not optional; other layers (UI, wiring) do not have a
constitution test mandate and are implementation-only tasks.

**Organization**: Tasks are grouped by user story (spec.md) to enable independent
implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US4)
- Package root assumed: `com.bullseyestracker` (adjust if a different applicationId is chosen)

## Path Conventions

Per plan.md Project Structure — 3 Gradle modules:
- `app/src/main/java/com/bullseyestracker/...` (UI, camera wiring) + `app/src/test/`, `app/src/androidTest/`
- `cv/src/main/java/com/bullseyestracker/cv/...` (vision engine) + `cv/src/test/`, `cv/src/androidTest/`
- `match/src/main/java/com/bullseyestracker/match/...` (rules, persistence) + `match/src/test/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and module scaffolding

- [X] T001 Create root Gradle project with `app`, `cv`, `match` modules per plan.md Project Structure (`settings.gradle.kts`, root `build.gradle.kts`, Kotlin 1.9+/JVM 17, minSdk 26)
- [X] T002 [P] Add OpenCV Android dependency and native-lib packaging config to `cv/build.gradle.kts`
- [X] T003 [P] Add CameraX dependencies (core, camera2, lifecycle, view) to `app/build.gradle.kts`
- [X] T004 [P] Add Jetpack Compose dependencies + compiler config to `app/build.gradle.kts`
- [X] T005 [P] Add Room dependencies (runtime, ktx, compiler/KSP) to `match/build.gradle.kts`
- [X] T006 [P] Configure ktlint/detekt across all 3 modules in root `build.gradle.kts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared types and infrastructure every user story builds on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T007 Define `CvEngine` interface, `FrameInput`, `BoardCalibration`, `BoardCalibrationResult`, `DetectedThrow` types in `cv/src/main/java/com/bullseyestracker/cv/CvEngine.kt` per `contracts/cv-engine-contract.md` (also added `BoardDetector.kt`/`DartDetector.kt` interfaces + `ScoreCalculator.kt` — see Phase 3 note)
- [X] T008 [P] Define domain model classes `Match`, `Player`, `Turn`, `Throw`, `DetectionFrame` in `match/src/main/java/com/bullseyestracker/match/model/` per data-model.md
- [X] T009 Define Room entities + DAOs (`MatchEntity`, `PlayerEntity`, `TurnEntity`, `ThrowEntity`, `MatchDao`) in `match/src/main/java/com/bullseyestracker/match/data/` (depends on T008)
- [X] T010 Implement `MatchRepository` (CRUD + Kotlin `Flow` observers for in-progress match) in `match/src/main/java/com/bullseyestracker/match/data/MatchRepository.kt` (depends on T009)
- [X] T011 [P] Implement `CameraController` scaffolding (CameraX `ProcessCameraProvider` bind/unbind, `PreviewView` wiring) in `app/src/main/java/com/bullseyestracker/camera/CameraController.kt`
- [X] T012 [P] Set up manual DI wiring (module-level singletons/factories connecting `cv`, `match`, `app`) in `app/src/main/java/com/bullseyestracker/di/AppContainer.kt`

**Checkpoint**: Foundation ready — user story implementation can now begin

---

## Phase 3: User Story 1 - Auto-score a single throw via live camera (Priority: P1) 🎯 MVP

**Goal**: Point the camera at the board; each thrown dart is detected, overlaid, and scored
with manual-correction fallback.

**Independent Test**: Point the camera at a physical board with darts already thrown; verify
detection + score overlay works standalone, no match/player features required.

### Tests for User Story 1 (mandatory per constitution Principle III) ⚠️

> Write these tests FIRST, ensure they FAIL before implementation

**Implementation note (discovered during /speckit-implement)**: `BoardDetector`/`DartDetector`
were split into plain interfaces (in `cv/src/main/.../BoardDetector.kt`, `DartDetector.kt`)
with OpenCV-backed implementations under `cv/src/main/.../opencv/`. OpenCV's native library
only loads on a device/emulator, so tests that exercise the OpenCV implementations directly
had to move to `androidTest` (T014/T015 below); `CvEngineImpl`'s own orchestration (T016)
stays a plain-JVM test by depending only on the interfaces, using fakes.

- [X] T013 [P] [US1] Unit test `ScoreMapper` position→sector/ring/value mapping (including bull, miss, ring boundaries) against fixture coordinates in `cv/src/test/java/com/bullseyestracker/cv/ScoreMapperTest.kt`
- [X] T014 [P] [US1] Instrumented test `OpenCvBoardDetector.calibrate()` against fixture images: board found (returns calibration+confidence) and board not found (`NotFound`) in `cv/src/androidTest/java/com/bullseyestracker/cv/OpenCvBoardDetectorTest.kt` — **needs fixture images not yet supplied, see T044**
- [X] T015 [P] [US1] Instrumented test `OpenCvDartDetector` against fixture images with 0/1/3 known darts in `cv/src/androidTest/java/com/bullseyestracker/cv/OpenCvDartDetectorTest.kt` — **needs fixture images not yet supplied, see T044**
- [X] T016 [P] [US1] Contract test asserting `CvEngine.detectThrows()` always populates `confidence` and `boardPosition`, returns empty list (not error) when no darts found, per `contracts/cv-engine-contract.md` in `cv/src/test/java/com/bullseyestracker/cv/CvEngineContractTest.kt`

### Implementation for User Story 1

- [X] T017 [US1] Implement `OpenCvBoardDetector` (Hough-circle board localization + calibration, implements `BoardDetector`) in `cv/src/main/java/com/bullseyestracker/cv/opencv/OpenCvBoardDetector.kt` (depends on T014)
- [X] T018 [US1] Implement `OpenCvDartDetector` (frame-diff/contour detection against calibrated board, implements `DartDetector`) in `cv/src/main/java/com/bullseyestracker/cv/opencv/OpenCvDartDetector.kt` (depends on T015)
- [X] T019 [US1] Implement `ScoreMapper` (position→sector/ring/value) in `cv/src/main/java/com/bullseyestracker/cv/ScoreMapper.kt` (depends on T013)
- [X] T020 [US1] Implement `CvEngineImpl` wiring `BoardDetector` + `DartDetector` + `ScoreMapper` behind the `CvEngine` interface in `cv/src/main/java/com/bullseyestracker/cv/CvEngineImpl.kt` (depends on T017, T018, T019, T016)
- [X] T021 [US1] Wire CameraX `ImageAnalysis` use case to invoke `CvEngine` off the UI thread (auto-calibrates, then detects) in `app/src/main/java/com/bullseyestracker/camera/LiveDetectionAnalyzer.kt` (depends on T020, T011)
- [X] T022 [US1] Build `DetectionOverlay` composable rendering dart positions/scores + low-confidence indicator (FR-012) over the camera preview in `app/src/main/java/com/bullseyestracker/ui/detection/DetectionOverlay.kt`
- [X] T023 [US1] Build `LiveScoringScreen` combining camera preview + `DetectionOverlay` + turn-confirm control in `app/src/main/java/com/bullseyestracker/ui/detection/LiveScoringScreen.kt` (depends on T021, T022)
- [X] T024 [US1] Implement manual correction UI (tap a detection → edit sector/ring/multiplier) in `app/src/main/java/com/bullseyestracker/ui/detection/CorrectionDialog.kt` (depends on T022)
- [X] T025 [US1] Instrumented benchmark test validating <200ms frame-to-result budget (constitution Principle IV) on the reference device class pinned in research.md (Pixel 6a / Galaxy A54-class or equivalent) in `cv/src/androidTest/java/com/bullseyestracker/cv/PerformanceBenchmarkTest.kt` (depends on T020; blocked on access to a matching device/emulator profile per research.md) — **needs fixture images not yet supplied, see T044**

**Checkpoint**: User Story 1 fully functional and testable independently (MVP)

---

## Phase 4: User Story 2 - Auto-score from a single photo (Priority: P1)

**Goal**: Take one photo of the board after a turn; all darts in it are detected and scored,
reusing the same `CvEngine`/overlay/correction machinery as US1.

**Independent Test**: Feed a single fixture photo with a known dart layout; verify correct
count and per-dart score, no live-camera session involved.

### Tests for User Story 2 (mandatory per constitution Principle III) ⚠️

- [ ] T026 [P] [US2] Unit test `CvEngine.detectThrows()` against a captured-photo fixture, confirming identical scoring output as the equivalent live-frame fixture in `cv/src/test/java/com/bullseyestracker/cv/PhotoDetectionTest.kt`

### Implementation for User Story 2

- [ ] T027 [US2] Wire CameraX `ImageCapture` use case for single-photo capture in `app/src/main/java/com/bullseyestracker/camera/PhotoCaptureController.kt` (depends on T011)
- [ ] T028 [US2] Build `PhotoScoringScreen` reusing `DetectionOverlay`/`CorrectionDialog` from US1 in `app/src/main/java/com/bullseyestracker/ui/detection/PhotoScoringScreen.kt` (depends on T022, T024, T027, T020)
- [ ] T029 [US2] Add capture-mode switch (Photo vs Live Camera) to the scoring entry screen in `app/src/main/java/com/bullseyestracker/ui/detection/CaptureModeSelector.kt` (depends on T023, T028)

**Checkpoint**: User Stories 1 and 2 both work independently

---

## Phase 5: User Story 3 - Play a scored match (501) with multiple players (Priority: P2)

**Goal**: Set up a 501 match with 2-4 players; confirmed turns (from US1/US2) drive standard
501 scoring — subtraction, bust, double-out checkout, win detection.

**Independent Test**: Start a 501 match, manually confirm turn scores, verify running score/
bust/win behavior matches standard rules end-to-end.

### Tests for User Story 3 (mandatory per constitution Principle III) ⚠️

- [ ] T030 [P] [US3] Unit test `X501Rules` — score subtraction, bust on overshoot/exact-1, double-out checkout, win detection in `match/src/test/java/com/bullseyestracker/match/rules/X501RulesTest.kt`

### Implementation for User Story 3

- [ ] T031 [US3] Implement `X501Rules` in `match/src/main/java/com/bullseyestracker/match/rules/X501Rules.kt` (depends on T030, T008)
- [ ] T032 [US3] Build `MatchSetupScreen` (choose game mode, add 2-4 named players) in `app/src/main/java/com/bullseyestracker/ui/match/MatchSetupScreen.kt` (depends on T008)
- [ ] T033 [US3] Implement `MatchViewModel` turn-confirmation flow: confirmed `DetectedThrow`s → active game-mode rules → `MatchRepository` (depends on T031, T010, T020) in `app/src/main/java/com/bullseyestracker/ui/match/MatchViewModel.kt`
- [ ] T034 [US3] Build 501 scoreboard screen (remaining score per player, current-turn indicator, bust/checkout feedback) in `app/src/main/java/com/bullseyestracker/ui/match/FiveOOneScoreboardScreen.kt` (depends on T033)

**Checkpoint**: User Stories 1-3 all independently functional

---

## Phase 6: User Story 4 - Play a scored match (Cricket) with multiple players (Priority: P3)

**Goal**: Set up a Cricket match; confirmed turns drive marks on numbers 15-20 + bull, closed/
points-scored state per standard Cricket rules.

**Independent Test**: Start a Cricket match, confirm turn scores, verify marks/closed-number/
points state updates per standard rules, independent of 501 logic.

### Tests for User Story 4 (mandatory per constitution Principle III) ⚠️

- [ ] T035 [P] [US4] Unit test `CricketRules` — marks accumulation, number-closed state, points-while-opponent-open, win detection in `match/src/test/java/com/bullseyestracker/match/rules/CricketRulesTest.kt`

### Implementation for User Story 4

- [ ] T036 [US4] Implement `CricketRules` in `match/src/main/java/com/bullseyestracker/match/rules/CricketRules.kt` (depends on T035, T008)
- [ ] T037 [US4] Extend `MatchViewModel` to dispatch to `CricketRules` when `Match.gameMode == CRICKET` in `app/src/main/java/com/bullseyestracker/ui/match/MatchViewModel.kt` (depends on T036, T033)
- [ ] T038 [US4] Build Cricket scoreboard screen (marks grid for 15-20 + bull, points per player) in `app/src/main/java/com/bullseyestracker/ui/match/CricketScoreboardScreen.kt` (depends on T037)

**Checkpoint**: All four user stories independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Requirements spanning multiple stories (FR-011, FR-014) and final validation

- [ ] T039 [P] Implement match history list/detail screens (FR-014) in `app/src/main/java/com/bullseyestracker/ui/history/`
- [ ] T040 Wire match-resume-on-launch (FR-011): app start observes `MatchRepository` for an in-progress match and restores it in `app/src/main/java/com/bullseyestracker/MainActivity.kt` (depends on T010)
- [ ] T041 [P] Manual validation pass of quickstart.md Scenario 6 (airplane mode, FR-013)
- [ ] T042 Run full quickstart.md validation (all 7 scenarios) end-to-end on a physical device
- [X] T043 [P] Write `README.md` with build/run instructions for the 3-module project
- [ ] T044 Build curated fixture image set (varied dart counts/positions/lighting, ground-truth sector/ring labels) and an accuracy benchmark test asserting ≥90% correct sector+ring detection (spec SC-002) in `cv/src/test/java/com/bullseyestracker/cv/DetectionAccuracyBenchmarkTest.kt` (depends on T020)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational; US1/US2 are both P1 and share the
  `CvEngine` build-out (US2 depends on US1's `CvEngineImpl`, T020). US3 depends on a completed
  turn-confirmation source (US1 or US2) to be demoed end-to-end, though its rules logic
  (T030-T031) can be built and unit-tested independently. US4 mirrors US3's dependency shape.
- **Polish (Phase 7)**: Depends on desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational — no dependency on other stories
- **US2 (P1)**: Reuses `CvEngine` (T020) and overlay/correction UI (T022, T024) built in US1 —
  build after or alongside US1, not fully independent at the implementation level (independent
  at the *test* level per T026)
- **US3 (P2)**: Rules engine (T030-T031) independently testable; end-to-end demo needs US1 or
  US2's turn-confirmation flow
- **US4 (P3)**: Same shape as US3, independent rules engine from US3's

### Parallel Opportunities

- All Setup tasks marked [P] (T002-T006)
- Foundational T008, T011, T012 in parallel once T007 lands (T009-T010 are sequential on T008)
- All [P] test tasks within a story phase (e.g., T013-T016 together)
- US3's T030 and US4's T035 can be written in parallel with each other and with US1/US2 work
  (different files, no shared dependency beyond Foundational)

---

## Parallel Example: User Story 1

```bash
# Tests together first (constitution III — must fail before implementation):
Task: "Unit test ScoreMapper in cv/src/test/.../ScoreMapperTest.kt"
Task: "Unit test BoardDetector in cv/src/test/.../BoardDetectorTest.kt"
Task: "Unit test DartDetector in cv/src/test/.../DartDetectorTest.kt"
Task: "Contract test CvEngine.detectThrows() in cv/src/test/.../CvEngineContractTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) + Phase 2 (Foundational)
2. Complete Phase 3 (US1 — live-camera auto-scoring)
3. **STOP and VALIDATE**: run quickstart.md Scenarios 1-2 on a physical device
4. Demo: hands-free live scoring against a real board, no match tracking yet

### Incremental Delivery

1. Foundation ready → US1 (live camera) → validate → demo (MVP)
2. Add US2 (photo mode) → validate (quickstart Scenario 3) → demo
3. Add US3 (501 match) → validate (quickstart Scenario 4) → demo
4. Add US4 (Cricket match) → validate (quickstart Scenario 5) → demo
5. Polish: history, resume, offline validation, full quickstart pass

---

## Notes

- Tests for `ScoreMapper`, `BoardDetector`, `DartDetector`, `X501Rules`, `CricketRules` are
  constitution-mandated (Principle III) and MUST be written and failing before their
  implementation tasks.
- UI, wiring, and camera-integration tasks have no constitution test mandate; add tests for
  them only if the team wants broader coverage.
- Commit after each task or logical group.
- Stop at each phase checkpoint to validate that story independently before continuing.
