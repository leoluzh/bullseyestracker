# Quickstart: Fixture Image Set & Detection Accuracy Benchmark

Validates spec `006-fixture-benchmark`: the ground-truth format/loader (User Story 1) and the
accuracy benchmark (User Story 2). **This pass ships harness/tooling only — no photos** (plan.md
Summary, user decision during planning). Every scenario below that needs real fixture images is
explicitly marked as blocked until photos are supplied; that is the expected end state, not a
failure of this feature.

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `FixtureGroundTruth.kt` (parser/validator) implemented so `FixtureGroundTruthTest` passes.
- `DetectionAccuracyBenchmarkTest` implemented (this feature's core deliverable).

## 1. Ground-truth parser validation (automated, no device needed) — User Story 1

```bash
make test
```

Expected: `FixtureGroundTruthTest` (`cv/src/test/java/com/bullseyestracker/cv/fixtures/`) passes —
covers valid ground truth parsing (all fields, zero-dart case), and each rejection rule from
data-model.md's Validation rules section (bad `ring` name, out-of-range `sectorNumber`, a
sector/ring mismatch, bad `lighting` value, an orphaned `.png` or `.json`).

## 2. Accuracy benchmark — structural check (device/emulator, no real photos yet)

```bash
make connected-test
```

Expected today (spec Assumptions — no real fixture photos exist yet): `DetectionAccuracyBenchmarkTest`
fails with a clear, specific error naming the missing fixture set/ground truth (spec FR-007,
Acceptance Scenario 4) — **not** a generic crash, **not** a misleading pass. This is the correct,
expected state until real photos are added; confirming the failure message is clear (not cryptic)
is itself the validation for this step.

## 3. Accuracy benchmark — full validation (blocked until real fixture photos exist)

Once someone with a physical dartboard has captured and labeled the curated set per
`cv/src/androidTest/assets/fixtures/README.md` (this feature's FR-008 deliverable):

1. **Baseline unblock**: Run `make connected-test`. Expect: `OpenCvBoardDetectorTest`,
   `OpenCvDartDetectorTest`, and `PerformanceBenchmarkTest` all pass (spec SC-003) — they were
   already written and failed only on the missing-asset error this feature's fixtures resolve.
2. **Accuracy computed and reported**: `DetectionAccuracyBenchmarkTest` runs detection against
   every fixture and reports a single overall percentage (spec Acceptance Scenario 1).
3. **Passes at/above 90%**: If the reported accuracy is ≥90%, the test passes (spec Acceptance
   Scenario 2).
4. **Fails below 90%**: If accuracy drops below 90% (e.g., after a detector regression), the test
   fails and reports the actual percentage (spec Acceptance Scenario 3) — verify by temporarily
   pointing the benchmark at a deliberately-mislabeled fixture and confirming the run fails with
   the expected lower number, then revert.
5. **Extend without code changes**: Add one new labeled photo + ground truth JSON to the fixture
   set (per the README) and re-run — expect it to be picked up automatically (spec SC-001), no
   test-file edits needed.

## Success criteria mapping

- SC-001 (new fixture included without code changes): quickstart step 3.5.
- SC-002 (single accuracy percentage, fails below 90%): quickstart steps 3.2-3.4.
- SC-003 (three existing failing tests pass once fixtures exist): quickstart step 3.1.
- SC-004 (100% of ground truth validated before accuracy computed): quickstart step 1 (all
  rejection-rule cases) plus step 2 (clean failure, not a silent bad-accuracy number, when the set
  itself is incomplete).
