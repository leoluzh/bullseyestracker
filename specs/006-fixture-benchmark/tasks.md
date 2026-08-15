# Tasks: Fixture Image Set & Detection Accuracy Benchmark

**Input**: Design documents from `/specs/006-fixture-benchmark/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: This feature *is* test infrastructure (plan.md Summary). `FixtureGroundTruthTest`
(plain-JVM, parser/validator correctness) is written before `FixtureGroundTruth.kt` following this
codebase's existing convention for `cv`-module logic (e.g. `ScoreMapperTest`), even though it
isn't constitution-Principle-III-mandated (plan.md Constitution Check — no new scoring logic).
`DetectionAccuracyBenchmarkTest` has nothing to precede it test-first (it *is* the test); it's
built once the parser it depends on exists and passes.

**Organization**: Tasks are grouped by user story from `spec.md` (US1 ground-truth format/harness,
US2 accuracy benchmark) to enable independent implementation and testing.

**Scope note**: Per explicit user decision during `/speckit-plan` (research.md), this task list
ships harness/tooling only — **no image files**. `DetectionAccuracyBenchmarkTest` and the three
pre-existing detector tests (`OpenCvBoardDetectorTest`, `OpenCvDartDetectorTest`,
`PerformanceBenchmarkTest`) remain failing on a missing-asset error after every task below is
complete — that is the correct, expected end state (quickstart.md section 2), not a gap in this
task list. Real fixture photo capture is external follow-up work (T006's README documents it).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

- [X] T001 [P] Create `cv/src/main/java/com/bullseyestracker/cv/fixtures/` package directory
      (holds `FixtureGroundTruth.kt`; mirrors existing `cv/.../opencv/` package layout)
- [X] T002 [P] Create `cv/src/test/java/com/bullseyestracker/cv/fixtures/` package directory
      (holds `FixtureGroundTruthTest.kt`)
- [X] T003 [P] Create `cv/src/androidTest/assets/fixtures/` directory (does not exist yet — `ls`
      confirms `cv/src/androidTest/` currently has no `assets/` subtree at all; will hold the
      README this feature adds now, and the images/ground-truth JSON added later by whoever
      captures real photos)

## Phase 2: Foundational

No new foundational/blocking work — the ground-truth format reuses `Ring` as-is from
`cv/src/main/java/com/bullseyestracker/cv/CvEngine.kt` (data-model.md "Reused"). Proceed directly
to User Story phases.

---

## Phase 3: User Story 1 - Curate and document ground-truth fixture photos (Priority: P1)

**Goal**: A documented, validated ground-truth format exists so a fixture photo's correct
sector/ring/multiplier per dart can be recorded and loaded reliably; the manual steps to actually
capture and label a fixture are documented for whoever has physical access to a board.

**Independent Test**: `FixtureGroundTruthTest` (`cv/src/test/.../fixtures/`) — parses valid ground
truth (including the zero-dart case) and rejects every invalid case from data-model.md's
Validation rules, with no detection code or device involved.

- [X] T004 [P] [US1] Write `FixtureGroundTruthTest` — unit tests for: valid ground truth with
      multiple darts, valid ground truth with zero darts (`empty_board` case), and each rejection
      rule (unknown `ring` name, `sectorNumber` outside 1-20, a sector present with a
      bull/miss ring or absent with a single/double/triple ring, an invalid `lighting` value, a
      `.png` with no matching `.json`) — in
      `cv/src/test/java/com/bullseyestracker/cv/fixtures/FixtureGroundTruthTest.kt` (depends on
      T002; written first per this feature's testing convention, expected to fail until T005)
- [X] T005 [US1] Implement `FixtureGroundTruth`/`DartGroundTruth`/`Lighting` data classes plus an
      `org.json`-based parser/validator implementing every data-model.md Validation rule (plus
      `findOrphanedFixtures`, a pure set-comparison helper) — in
      `cv/src/main/java/com/bullseyestracker/cv/fixtures/FixtureGroundTruth.kt` (depends on T001,
      T004) — makes `FixtureGroundTruthTest` pass
      CORRECTION: `android.jar`'s `org.json` classes are compile-only stubs that throw at runtime
      outside a real device/emulator, so `FixtureGroundTruthTest` couldn't actually run as a
      plain-JVM test without a real implementation. Added `testImplementation("org.json:json:20240303")`
      to `cv/build.gradle.kts` (test-only, never shipped in the app) — doesn't contradict
      research.md's "no new dependency" framing, which was about production/androidTest code path.
- [X] T006 [US1] Write `cv/src/androidTest/assets/fixtures/README.md` documenting: the
      ground-truth JSON format and every validation rule (data-model.md), the required baseline
      fixtures already referenced by existing tests (`board_calibrated`, `no_board`,
      `empty_board`, `one_dart`, `three_darts` — spec FR-003), the additional variety needed
      (dart count 0-3+, varied sector/ring including close-together darts, normal + dim lighting —
      spec FR-004), and step-by-step manual capture/labeling instructions (spec FR-008) — in
      `cv/src/androidTest/assets/fixtures/README.md` (depends on T003, T005)
      CORRECTION: also documents the `<name>_baseline.png` pairing requirement discovered while
      implementing T007 (data-model.md/research.md — `OpenCvDartDetector` needs a same-setup
      zero-dart reference frame to diff against; a lone photo can't be scored on its own).

**Checkpoint**: Ground-truth format is documented, implemented, and unit-tested. The curated
images themselves are still absent (expected — see Scope note above); the README is the map for
whoever supplies them next.

---

## Phase 4: User Story 2 - Automated detection-accuracy benchmark (Priority: P2)

**Goal**: An instrumented test loads every fixture + its ground truth, runs the app's real
detection pipeline against each, computes overall sector+ring+multiplier accuracy across the
whole set, and enforces the 90% bar from spec 001 SC-002 — failing clearly (not silently) when the
fixture set itself is missing or incomplete.

**Independent Test**: quickstart.md section 2 — with no real fixtures present yet, running
`DetectionAccuracyBenchmarkTest` produces a clear "fixture set missing/incomplete" failure, not a
crash or a misleading pass/fail number.

- [X] T007 [US2] Implement `DetectionAccuracyBenchmarkTest` — loads all fixtures via
      `FixtureGroundTruth` (fails clearly if the directory is empty/missing or has orphaned
      png/json, spec FR-007), and for each fixture: calibrates the board (`OpenCvBoardDetector`),
      detects darts (`OpenCvDartDetector`, diffed against a `<name>_baseline.png` for any fixture
      with 1+ ground-truth darts — see T006's CORRECTION), greedily matches detected
      `(sectorNumber, ring)` pairs against the fixture's ground-truth darts (research.md —
      unordered, no partial credit), accumulates matched/total counts across every fixture,
      computes overall accuracy, and asserts it is `>= 0.90f` while always reporting the computed
      percentage (spec FR-005, FR-006) — in
      `cv/src/androidTest/java/com/bullseyestracker/cv/DetectionAccuracyBenchmarkTest.kt` (depends
      on T005, T003)

**Checkpoint**: Both user stories' code is complete. `DetectionAccuracyBenchmarkTest` and the
three pre-existing detector tests remain failing on the missing-asset error until real fixture
photos are added — this is the expected, documented end state of this feature (see Scope note).

---

## Phase 5: Polish & Cross-Cutting

- [ ] T008 Run quickstart.md sections 1-2 (`make test` for the parser unit tests; `make
      connected-test` to confirm `DetectionAccuracyBenchmarkTest` fails with a clear
      missing-fixture message rather than a crash or misleading result) — no code change expected,
      verification only
      PARTIAL: `make test` (quickstart section 1) done — `./gradlew build` ran
      `FixtureGroundTruthTest`, all 10 cases pass (`cv/build/test-results/testDebugUnitTest/
      TEST-com.bullseyestracker.cv.fixtures.FixtureGroundTruthTest.xml`), confirming the
      `org.json` test-runtime fix (T005 CORRECTION) actually works, not just compiles. Quickstart
      section 2 (`make connected-test`, confirming `DetectionAccuracyBenchmarkTest`'s specific
      failure message) needs a device/emulator — none attached in this environment (`adb devices`
      returns none), same blocker as spec 005's T002/T007.
- [X] T009 [P] `make lint` / ktlint format check over all new files (`FixtureGroundTruth.kt`,
      `FixtureGroundTruthTest.kt`, `DetectionAccuracyBenchmarkTest.kt`) — verified via
      `./gradlew build`, which runs ktlint + Android lint across all modules; BUILD SUCCESSFUL, no
      findings against the new files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately; T001-T003 are independent (different
  directories)
- **Foundational (Phase 2)**: None needed (skipped — see note above)
- **US1 (Phase 3)**: Depends on Setup (T001, T002 for T004/T005; T003 for T006)
- **US2 (Phase 4)**: Depends on US1 (T005's parser, T003's directory) — the benchmark can't load
  fixtures without the ground-truth loader existing first
- **Polish (Phase 5)**: Depends on Phase 4 complete

### Critical Path

T001/T002 → T004 → T005 → T007 → T008

T003 → T006 (README) can happen any time before/alongside T004-T005; T006 has no code dependency
on T007.

## Parallel Execution Examples

- T001, T002, T003 (different directories) can all run in parallel.
- T006 (README, no code dependency beyond T003/T005 for accuracy of what it documents) can be
  written in parallel with T007 once T005 lands.
- T009 (lint) can run in parallel with T008 (manual verification) once Phase 4 lands.

## Implementation Strategy

**MVP first**: Phase 3 (T004-T006) delivers a fully-tested ground-truth format and the manual
capture guide — independently valuable even before any benchmark exists, since it's the
prerequisite anyone needs before they can add a single real fixture.

**Incremental delivery**: Phase 4 (T007) is what turns the curated set (once it exists) into an
enforced quality gate. Land Phases 3-4 together as one PR — this feature's own tests
(`FixtureGroundTruthTest`) are green either way, but `DetectionAccuracyBenchmarkTest` and the three
pre-existing detector tests only turn green once someone supplies real fixture photos per T006's
README, which is explicitly out of scope for this implementation pass (Scope note above).
