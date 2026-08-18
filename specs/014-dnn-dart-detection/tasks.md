# Tasks: DNN-Based Dart & Board Detection

**Input**: Design documents from `/specs/014-dnn-dart-detection/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/dnn-detection-backend-contract.md, quickstart.md (all present)

**Tests**: Included and REQUIRED, not optional — constitution Principle III / CLAUDE.md mandate
test-first for anything that maps a detected position to a score, which applies here to the
calibration-point→`BoardCalibration` derivation and the `YoloV8OutputDecoder` (research.md §3,
data-model.md).

**Organization**: Tasks are grouped by user story (spec.md priorities P1/P2/P3) to enable
independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)

## Path Conventions

Single project (existing `cv` and `app` Gradle modules) — see plan.md's Project Structure
section for the full tree. No new module.

---

## Phase 1: Setup

**Purpose**: Produce the bundled model asset and package scaffolding everything else depends on

- [x] T001 Train YOLOv8n on the DeepDarts dataset (Roboflow Universe, `testing-zzmc9/deepdarts-yolov8`,
      CC BY 4.0) and export to ONNX (`opset=12`, `simplify=True`, `nms=False`, `imgsz=640`) per
      quickstart.md step 0; place the result at `cv/src/main/assets/models/deepdarts-yolov8.onnx`
- [x] T002 [P] Add CC BY 4.0 attribution for the DeepDarts dataset/model (McNally et al.,
      project + dataset URLs) to the app's existing credits/licenses location (spec FR-009)
- [x] T003 [P] Create the `cv/src/main/java/com/bullseyestracker/cv/opencv/dnn/` package
      (empty placeholder or `.gitkeep` if the build requires a non-empty directory)

**Checkpoint**: Model asset exists and loads; package scaffolding ready.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared types, the tested output-decoding logic, and backend-selection plumbing that
every user story depends on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 [P] Define `YoloV8Detection` and `LetterboxInfo` data classes in
      `cv/src/main/java/com/bullseyestracker/cv/opencv/dnn/YoloV8Types.kt` (data-model.md)
- [x] T005 [P] Write failing `YoloV8OutputDecoderTest` against fixed synthetic tensor fixtures
      (confidence thresholding, per-class NMS keeping calibration-point and dart-tip classes
      independent, letterbox coordinate un-mapping) in
      `cv/src/test/java/com/bullseyestracker/cv/opencv/dnn/YoloV8OutputDecoderTest.kt` —
      **write and confirm this fails before T006** (constitution Principle III)
- [x] T006 Implement `YoloV8OutputDecoder.decode(rawOutputTensor, letterboxInfo,
      confidenceThreshold, iouThreshold)` in
      `cv/src/main/java/com/bullseyestracker/cv/opencv/dnn/YoloV8OutputDecoder.kt` to make T005
      pass (depends on: T004, T005)
- [x] T007 Add `enum class DetectionBackend { CLASSICAL, DNN }` to
      `cv/src/main/java/com/bullseyestracker/cv/CvEngine.kt` (plain Kotlin, crosses the `app`
      boundary per Principle II)
- [x] T008 Wire a settable `DetectionBackend` selection into `CvEngineImpl`
      (`cv/src/main/java/com/bullseyestracker/cv/CvEngineImpl.kt`) — read once per
      `calibrateBoard`/`detectThrows` call (not cached at construction, spec FR-003), with
      fallback to `CLASSICAL` if the DNN model asset fails to load (data-model.md validation
      rules, spec Edge Cases) (depends on: T007)

**Checkpoint**: Foundation ready — user story implementation can now begin.

---

## Phase 3: User Story 1 - More reliable auto-scoring via DNN detection (Priority: P1) 🎯 MVP

**Goal**: A working DNN detection backend that meets or beats classical accuracy and stays
within the real-time latency budget (spec SC-001, SC-003).

**Independent Test**: Run the DNN backend directly (via `CvEngineImpl` with
`DetectionBackend.DNN` selected, no UI needed) against the fixture/accuracy benchmark harness
and confirm it detects calibration points + dart tips and produces correct scores.

### Tests for User Story 1 ⚠️

> Write these FIRST, confirm they FAIL before implementation (constitution Principle III)

- [x] T009 [P] [US1] Write failing unit tests for the calibration-point→`BoardCalibration`
      derivation (center/radii/rotation from 4 detected calibration points, research.md §3) in
      `cv/src/test/java/com/bullseyestracker/cv/opencv/dnn/CalibrationPointMapperTest.kt`
- [x] T010 [P] [US1] Write failing instrumented `OpenCvDnnBoardDetectorTest` (mirrors
      `OpenCvBoardDetectorTest`) in
      `cv/src/androidTest/java/com/bullseyestracker/cv/OpenCvDnnBoardDetectorTest.kt`
- [x] T011 [P] [US1] Write failing instrumented `OpenCvDnnDartDetectorTest` (mirrors
      `OpenCvDartDetectorTest`) in
      `cv/src/androidTest/java/com/bullseyestracker/cv/OpenCvDnnDartDetectorTest.kt`

### Implementation for User Story 1

- [x] T012 [US1] Implement the calibration-point→`BoardCalibration` derivation in
      `cv/src/main/java/com/bullseyestracker/cv/opencv/dnn/CalibrationPointMapper.kt` to make
      T009 pass (depends on: T009)
- [x] T013 [US1] Implement `OpenCvDnnBoardDetector : BoardDetector` in
      `cv/src/main/java/com/bullseyestracker/cv/opencv/dnn/OpenCvDnnBoardDetector.kt`
      (`Dnn.readNetFromONNX` load + letterbox preprocess + `YoloV8OutputDecoder` +
      `CalibrationPointMapper`; returns `NotFound` when fewer than 4 calibration points survive,
      per contracts/dnn-detection-backend-contract.md) to make T010 pass (depends on: T006, T012, T010)
- [x] T014 [US1] Implement `OpenCvDnnDartDetector : DartDetector` in
      `cv/src/main/java/com/bullseyestracker/cv/opencv/dnn/OpenCvDnnDartDetector.kt` to make T011
      pass (depends on: T006, T011)
- [x] T015 [US1] Extend `DetectionAccuracyBenchmarkTest` in
      `cv/src/androidTest/java/com/bullseyestracker/cv/DetectionAccuracyBenchmarkTest.kt` to run
      parametrized over both `DetectionBackend.CLASSICAL` and `DetectionBackend.DNN` on the same
      fixture set and assert DNN accuracy ≥ classical (spec SC-001) (depends on: T013, T014)
- [x] T016 [US1] Extend `PerformanceBenchmarkTest` in
      `cv/src/androidTest/java/com/bullseyestracker/cv/PerformanceBenchmarkTest.kt` to cover the
      DNN backend (≥95% of frames under 200ms, spec SC-003) and assert the live-camera
      restriction fallback described in FR-008 when the budget isn't met (depends on: T013, T014)

**Checkpoint**: User Story 1 fully functional and testable independently of any UI/settings work.

---

## Phase 4: User Story 2 - Switch between classical and DNN detection (Priority: P2)

**Goal**: A user-facing, persisted toggle that switches the active backend and takes effect
immediately (spec SC-002).

**Independent Test**: Toggle the setting, confirm the very next capture uses the newly selected
backend without restarting the app.

### Implementation for User Story 2

- [x] T017 [P] [US2] Add a persisted `DetectionBackend` setting (read/write, defaulting to
      `CLASSICAL` per spec FR-004) in
      `app/src/main/java/com/bullseyestracker/ui/settings/DetectionBackendSetting.kt`, using
      whatever settings-storage mechanism `app` already uses
- [x] T018 [US2] Add a settings UI control (toggle/radio) for `DetectionBackend` wired to
      `DetectionBackendSetting`, in `app`'s existing settings screen (depends on: T017)
- [x] T019 [US2] Thread the selected `DetectionBackend` through
      `MatchRepository.create(context)`/`CvEngine` construction so the capture flow reads the
      current selection before each `calibrateBoard`/`detectThrows` call (depends on: T008, T017)

**Checkpoint**: User Stories 1 AND 2 both work — user can switch backends and each works
independently.

---

## Phase 5: User Story 3 - Detections remain explainable and correctable (Priority: P3)

**Goal**: DNN detections overlay and correct exactly like classical ones (spec FR-007).

**Independent Test**: With DNN active, present an ambiguous dart, confirm the overlay appears
and manual correction commits the corrected value, not the raw DNN output.

### Implementation for User Story 3

- [x] T020 [US3] Audit `DetectionOverlay` and the manual-correction UI
      (`app/src/main/java/com/bullseyestracker/ui/detection/`) to confirm they consume
      `DetectedThrow`/`BoardCalibration` identically regardless of which backend produced them;
      fix any classical-only assumption found
- [x] T021 [P] [US3] Add a small backend-source indicator (e.g. "Classical"/"DNN" label) to the
      capture/result screens in `app/src/main/java/com/bullseyestracker/ui/detection/`, to make
      quickstart.md step 5's manual verification observable

**Checkpoint**: All three user stories independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T022 [P] Update `CLAUDE.md`'s "Known gaps" section to note the DNN backend, the bundled
      model asset, and that its real-world (non-synthetic) accuracy validation is blocked on the
      same missing-real-photos gap as `specs/006-fixture-benchmark/`
- [x] T023 Run `make build` and confirm zero regressions to the existing classical-backend test
      suite (spec FR-010)
- [x] T024 Run what's runnable of `quickstart.md` validation without a device/emulator: step 1
      (`make test` — decoder + calibration-mapper unit tests) passes; the pipeline was also
      sanity-checked end-to-end outside the app (Python + opencv-python against the same ONNX
      asset) confirming detection/decoding/calibration math matches the Kotlin implementation.
      Steps 2-8 need a physical device/emulator (none available in this environment) and real
      dartboard photos (same gap as `specs/006-fixture-benchmark/`) — tracked as follow-up, not
      blocking this feature's code from being complete (CLAUDE.md Known gaps)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup (T001's model asset is needed by T013 in Phase 3,
  but Phase 2's decoder/types/backend-plumbing work does not itself need the asset) — BLOCKS all
  user stories
- **User Stories (Phase 3-5)**: All depend on Foundational (Phase 2) completion
  - US1 has no dependency on US2/US3
  - US2 depends on US1's `CvEngineImpl` wiring (T008) already existing, but is otherwise
    independent UI/settings work
  - US3 is independent of US2 (overlay/correction already consumes the same types regardless of
    how the backend was selected) but is most easily verified once US2 exists
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### Within Each User Story

- Tests written and confirmed failing before implementation (T009-T011 before T012-T014)
- Calibration-point mapping (T012) before the detector that uses it (T013)
- Shared decoder (T006) before both detectors (T013, T014)
- Detectors before the benchmark extensions that exercise them (T015, T016)

### Parallel Opportunities

- T002, T003 (Setup) can run in parallel
- T004, T005 (Foundational) can run in parallel; T007 can run in parallel with T004/T005/T006
- T009, T010, T011 (US1 tests) can run in parallel
- T017 (US2) can start in parallel with US1 implementation once Phase 2 is done, since it only
  needs `DetectionBackend` (T007), not the DNN detectors themselves
- T021 (US3) can run in parallel with T020

---

## Parallel Example: User Story 1

```bash
# Launch all US1 tests together (write-first):
Task: "Write failing CalibrationPointMapperTest in cv/src/test/.../CalibrationPointMapperTest.kt"
Task: "Write failing OpenCvDnnBoardDetectorTest in cv/src/androidTest/.../OpenCvDnnBoardDetectorTest.kt"
Task: "Write failing OpenCvDnnDartDetectorTest in cv/src/androidTest/.../OpenCvDnnDartDetectorTest.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (model asset)
2. Complete Phase 2: Foundational (decoder, types, backend plumbing)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Run quickstart.md steps 1-4 (decoder unit tests, instrumented
   detector tests, accuracy parity, latency budget) — this alone proves the DNN backend works,
   even with no settings UI yet (selectable only via `CvEngineImpl` directly, e.g. in tests)
5. Continue to US2 (user-facing switch) once US1's accuracy/latency results are acceptable —
   spec FR-004 explicitly gates defaulting to DNN on this result

### Incremental Delivery

1. Setup + Foundational → decoder and backend-selection plumbing ready
2. US1 → DNN backend proven accurate/fast enough → this is the feature's actual value (MVP)
3. US2 → users can opt into it
4. US3 → confirmed no special-casing needed (largely a verification pass, not new code)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Verify tests fail before implementing (T005 before T006; T009-T011 before T012-T014)
- Commit after each task or logical group
- Avoid: vague tasks, same-file conflicts, cross-story dependencies that break independence
