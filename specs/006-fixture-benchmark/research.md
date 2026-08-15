# Phase 0 Research: Fixture Image Set & Detection Accuracy Benchmark

No `NEEDS CLARIFICATION` markers remain in the Technical Context. The one open judgment call
raised during planning — whether to ship synthetic placeholder images alongside the harness — was
resolved by explicit user decision (harness/tooling only, no images) before this document was
written; it is recorded as a Decision below for traceability.

## Decision: `DetectionAccuracyBenchmarkTest` must be `androidTest`, not `test` (corrects T044)

**Rationale**: `specs/001-dart-scoring-match/tasks.md` T044 originally proposed
`cv/src/test/java/com/bullseyestracker/cv/DetectionAccuracyBenchmarkTest.kt` (plain JVM). But
computing detection accuracy requires actually running `OpenCvBoardDetector.calibrate()` and
`OpenCvDartDetector.detect()` against real photos, and both depend on OpenCV's native library,
which — per every existing detector test's own doc comment (`OpenCvBoardDetectorTest`,
`OpenCvDartDetectorTest`, `PerformanceBenchmarkTest`) and CLAUDE.md's module-boundary notes — only
loads on a device/emulator. A plain-JVM test calling either detector would fail immediately on
`OpenCVLoader.initLocal()` (or an unsatisfied native-lib link), not produce a real accuracy number.
`ScoreMapperTest` stays plain-JVM because it only exercises pure position→score math, never the
detectors themselves — that's a different, narrower scope than this benchmark's.

**Alternatives considered**: Keeping the original `cv/src/test/` path and mocking/stubbing the
detectors — rejected: an accuracy benchmark that doesn't run the real detector against real
photos measures nothing meaningful; the entire point (spec FR-005/SC-002) is to catch real
detection regressions.

## Decision: Ground truth as JSON sidecar files, parsed with `org.json`

**Rationale**: Each fixture photo (`<name>.png`) gets a matching `<name>.json` alongside it in
`cv/src/androidTest/assets/fixtures/`, listing its ground-truth darts (data-model.md). `org.json`
(`JSONObject`/`JSONArray`) ships as part of the Android platform SDK and is already available to
`androidTest` code with zero new Gradle dependency — consistent with this codebase's pattern of
avoiding new dependencies when the platform already provides what's needed (e.g., `cv/build.gradle.kts`
already only pulls in OpenCV + JUnit + Mockito + AndroidX test/benchmark, nothing else).

**Alternatives considered**: A JSON library like `kotlinx.serialization` or `moshi` — rejected,
unjustified new dependency for a handful of small, hand-authored fixture files; a custom
line-based/CSV format — rejected, JSON is more self-describing for a nested per-dart list and
`org.json` makes it free to use.

## Decision: Accuracy is recall against ground truth (matches ÷ total ground-truth darts)

**Rationale**: Spec FR-005 defines accuracy exactly this way: "darts scored with exactly correct
sector, ring, and multiplier, divided by the total ground-truth darts across the whole set." This
directly mirrors parent-spec SC-002's own wording ("correctly identifies sector, ring... for at
least 90% of clearly-visible, non-overlapping darts") — a recall-style metric over the
ground-truth darts, not a precision/F1 metric that would also penalize spurious extra detections.
Matching a fixture's detected darts against its ground-truth darts (when counts differ or
positions aren't 1:1 orderable) uses a straightforward greedy match: for each ground-truth dart,
consume the first not-yet-matched detected dart with an identical `(sectorNumber, ring)` pair
(multiplier is implied by ring per `ScoreCalculator`); unmatched ground-truth darts count as
incorrect. This keeps the metric exactly aligned with spec wording without inventing a more
complex scoring scheme the spec doesn't ask for.

**Alternatives considered**: Position-based matching (pairing by nearest detected-vs-ground-truth
pixel coordinate) — rejected as unnecessary complexity; ground truth only needs to record *which*
sector/ring each dart lands in (data-model.md), not a pixel position, since the benchmark's
question is "did the detector get the score right," not "did it find the dart in the exact right
spot." A precision-aware metric (also penalizing extra/spurious detections) — rejected, not what
FR-005/SC-002 ask for; flagged in spec.md Edge Cases as future-extensible if ever needed.

## Decision: Harness and tooling only this pass — no image files (user decision)

**Rationale**: Presented as an explicit tradeoff during planning: ship synthetic
procedurally-drawn placeholder images to unblock the three existing failing tests structurally
today, or ship only the ground-truth format/loader/benchmark test and leave real photo capture as
a pure manual follow-up. User chose the latter. Synthetic images were correctly identified as
risky: OpenCV's Hough-circle board detection is *good* at finding clean, high-contrast synthetic
circles, so a synthetic fixture set could pass the benchmark while telling us nothing about
real-world accuracy — worse than no fixtures at all, since a passing (but meaningless) benchmark
looks like the constitution's Principle III/SC-002 quality bar is met when it isn't.

**Alternatives considered**: Synthetic placeholders — rejected per above (user decision, with
technical rationale confirming it was the right call, not just a preference). Deferring the whole
feature until real photos exist — rejected, the ground-truth format/loader/benchmark test are
independently valuable and buildable now; they're the long pole, not the photography.

## Decision: darts-bearing fixtures need a paired `<name>_baseline.png` (found during T007)

**Rationale**: Discovered while implementing `DetectionAccuracyBenchmarkTest` — re-reading
`OpenCvDartDetector.kt` (not re-examined closely enough during Phase 1) shows it detects darts
purely by frame-diffing against a baseline captured on the detector's *first* call; a lone photo
handed to a fresh detector always returns zero results on that first call (it's establishing the
baseline, per its own doc comment and `OpenCvDartDetectorTest`'s existing usage pattern: calibrate
+ baseline from `empty_board.png`, then detect against `one_dart.png`/`three_darts.png` using that
prior baseline). The originally-planned one-photo-per-fixture format (data-model.md as first
written) can't be scored for any fixture with 1+ darts. Fixed by requiring a same-setup, zero-dart
`<name>_baseline.png` alongside any `<name>.png`/`<name>.json` whose ground truth is non-empty —
the benchmark diffs the baseline photo first (establishing it), then the real photo (yielding
actual detections to score). Zero-dart fixtures need no pair since they're already a valid
baseline state on their own.

**Alternatives considered**: A single shared baseline photo reused across the whole curated set —
rejected, defeats spec FR-004's requirement for varied lighting/position per fixture, since the
diff would be comparing frames shot under different conditions instead of a genuine same-setup
before/after pair. Changing `OpenCvDartDetector` to support single-photo detection (e.g. static
background subtraction without a captured baseline) — rejected as out of scope: this feature
tests the existing, already-shipped detector (plan.md Constitution Check), it doesn't change how
detection works.

## Decision: `FixtureGroundTruth` parser lives in `cv/src/main/`, not a test source set

**Rationale**: Needed by both the plain-JVM unit test (parser/validator correctness, FR-002/
SC-004) and the instrumented benchmark (loading real fixtures, FR-005-007). Putting it in `main`
avoids duplicating parsing logic across `test`/`androidTest` (Gradle source sets can't easily
share code with each other directly, only with `main`). It has zero Android/OpenCV imports (pure
data classes + `org.json`), so this doesn't weaken constitution Principle II's CV/UI isolation —
it's data parsing, not vision logic, and mirrors where `ScoreMapper` already lives for the same
reason (used by both a plain-JVM test and, transitively, real detection code).

**Alternatives considered**: Duplicating a smaller parser in each source set — rejected,
unnecessary duplication for no benefit. A `testFixtures` Gradle source set — rejected, `cv/build.gradle.kts`
doesn't currently enable `java-test-fixtures` and this is a single small file; not worth the
build-config churn for one class.
