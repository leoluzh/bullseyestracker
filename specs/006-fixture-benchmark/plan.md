# Implementation Plan: Fixture Image Set & Detection Accuracy Benchmark

**Branch**: `006-fixture-benchmark` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-fixture-benchmark/spec.md`

## Summary

Build the ground-truth format, loader/validator, and an automated `DetectionAccuracyBenchmarkTest`
that runs the existing `cv` module's detection pipeline (`OpenCvBoardDetector` +
`OpenCvDartDetector` + `ScoreMapper`, already implemented) against a curated set of real dartboard
photos and asserts ≥90% sector+ring+multiplier accuracy (spec 001 SC-002). Per user decision
during planning, this pass ships **harness and tooling only** — no image files. Actual dartboard
photo capture is a manual step outside AI capability (spec Assumptions) and is left for the user
to supply afterward; `OpenCvBoardDetectorTest`/`OpenCvDartDetectorTest`/`PerformanceBenchmarkTest`
and the new `DetectionAccuracyBenchmarkTest` all remain failing on a missing-asset error until
real photos + ground truth are added — this is the expected, documented state at the end of this
feature, not a defect in it.

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged)

**Primary Dependencies**: No new external dependency. Ground truth is parsed with `org.json`
(bundled in the Android platform SDK, already available to `androidTest` — no new Gradle
dependency) rather than introducing a JSON library, matching this codebase's low-dependency norm.

**Storage**: N/A — fixtures (images) and their ground truth (JSON sidecar files) are static
`androidTest` assets under `cv/src/androidTest/assets/fixtures/`, loaded read-only at test time;
no runtime persistence.

**Testing**: This feature *is* test infrastructure. `DetectionAccuracyBenchmarkTest` must be an
**instrumented** test (`cv/src/androidTest/`), not the plain-JVM `cv/src/test/` location tasks.md
T044 originally proposed — `OpenCvBoardDetector`/`OpenCvDartDetector` require OpenCV's native
library, which only loads on a device/emulator (same constraint documented on every existing
detector test; see research.md for the correction). Ground-truth parsing/validation logic itself
*is* plain-JVM-testable (no Android/OpenCV dependency) and gets its own `cv/src/test/` unit test.

**Target Platform**: Android instrumented test (device/emulator with OpenCV native lib available)
— same requirement as the three existing detector tests this feature sits alongside. Unlike
`PerformanceBenchmarkTest`, accuracy is not latency-sensitive, so no specific reference-device
class is required here.

**Project Type**: Mobile app — additive to the `cv` module only (`androidTest` source set plus one
plain-JVM unit test for the ground-truth parser); no new Gradle module, no `app`/`match` changes,
no production (`main`) source changes.

**Performance Goals**: N/A — this feature measures detection *accuracy*, not latency; the
existing `PerformanceBenchmarkTest` (constitution Principle IV) is unaffected and unchanged.

**Constraints**: Fully offline/on-device (Principle I — fixtures are bundled test assets, zero
network); CV logic isolated from UI (Principle II — this feature only adds test code exercising
the existing `cv` module's own interfaces, no new cross-module surface); Test-First for Scoring
Logic (Principle III) — `ScoreMapper`/`BoardDetector`/`DartDetector` already have their own
dedicated tests written test-first (T013-T016); this benchmark is *additional* regression coverage
layered on already-implemented, already-tested logic, not a first test for new logic, so Principle
III's "write failing test before implementation" sequencing doesn't apply to it directly — there
is no new production code for it to precede.

**Scale/Scope**: A ground-truth JSON format + loader/validator (new, `cv/src/main/` or
`cv/src/androidTest/` — see data-model.md for the format, plan below for placement), one new
`DetectionAccuracyBenchmarkTest` (`cv/src/androidTest/`), one new unit test for the loader/
validator (`cv/src/test/`), and documentation of the manual fixture-capture procedure (FR-008). No
image assets are added by this pass (see Summary) — `assets/fixtures/` gains only whatever ground
-truth JSON files are needed to describe fixtures once photos exist, plus a README documenting the
format and the currently-missing files.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — fixtures are bundled test assets; benchmark runs entirely on-device, zero network |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — this feature adds no UI code and no new `cv` surface beyond a test-only ground-truth loader; it consumes the existing `CvEngine`-adjacent detector interfaces exactly as `OpenCvDartDetectorTest` already does |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | N/A — no new scoring/rules production code is introduced; this is additional regression coverage over already-tested, already-implemented `BoardDetector`/`DartDetector`/`ScoreMapper` |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | N/A — this feature measures accuracy, not latency; `PerformanceBenchmarkTest` (already covers this gate) is untouched |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | N/A — no new automatic detection surface; this is a test harness measuring the existing detector's output against ground truth, not a new UI-facing detection path |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/006-fixture-benchmark/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command) — ground-truth format
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
cv/
├── src/main/java/com/bullseyestracker/cv/fixtures/
│   └── FixtureGroundTruth.kt          # NEW — data classes + org.json parser/validator for the
│                                       #   ground-truth format (data-model.md); plain Kotlin, no
│                                       #   Android/OpenCV dependency — lives in main so both the
│                                       #   androidTest benchmark and its plain-JVM unit test can
│                                       #   use it without duplicating parsing logic
├── src/test/java/com/bullseyestracker/cv/fixtures/
│   └── FixtureGroundTruthTest.kt      # NEW — plain-JVM unit test: valid/invalid ground truth
│                                       #   parsing (FR-002), matches SC-004
├── src/androidTest/java/com/bullseyestracker/cv/
│   └── DetectionAccuracyBenchmarkTest.kt   # NEW — loads every fixture + its ground truth,
│                                       #   runs the existing detection pipeline, computes and
│                                       #   asserts overall accuracy (FR-005, FR-006, FR-007)
└── src/androidTest/assets/fixtures/
    └── README.md                      # NEW — documents the ground-truth format, the currently
                                        #   -missing base + curated images (FR-003, FR-004), and
                                        #   the manual capture/labeling procedure (FR-008); no
                                        #   image files are added by this feature (see plan.md
                                        #   Summary) — this file is the map for whoever adds them

# UNCHANGED — no modifications:
app/                                   # no UI/app-module changes
match/                                 # no changes
cv/src/main/java/com/bullseyestracker/cv/opencv/     # detector implementations unchanged
cv/src/androidTest/java/.../OpenCvBoardDetectorTest.kt      # unchanged — already references the
cv/src/androidTest/java/.../OpenCvDartDetectorTest.kt       #   fixture filenames this feature's
cv/src/androidTest/java/.../PerformanceBenchmarkTest.kt     #   README documents (SC-003)
```

**Structure Decision**: Additive to the `cv` module only. `FixtureGroundTruth.kt` (the parser/
validator) goes in `cv/src/main/` rather than a test source set specifically so it's reusable from
both the plain-JVM unit test (parser correctness, FR-002/SC-004) and the instrumented benchmark
(accuracy computation, FR-005-007) without duplicating logic — this mirrors how `ScoreMapper`
already sits in `main` and is exercised by both plain-JVM (`ScoreMapperTest`) and, indirectly,
instrumented tests. It has zero Android/OpenCV imports, so its presence in `main` doesn't weaken
Principle II's isolation in any way — it's pure data-class parsing, not vision logic.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
