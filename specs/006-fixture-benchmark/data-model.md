# Phase 1 Data Model: Fixture Image Set & Detection Accuracy Benchmark

## New: Ground-truth JSON format (spec FR-001)

One JSON sidecar file per fixture photo, same base name (`three_darts.png` → `three_darts.json`),
stored alongside it in `cv/src/androidTest/assets/fixtures/`.

```json
{
  "scenario": "three darts, normal lighting",
  "lighting": "normal",
  "darts": [
    { "sectorNumber": 20, "ring": "TRIPLE" },
    { "sectorNumber": 5, "ring": "SINGLE" },
    { "sectorNumber": null, "ring": "INNER_BULL" }
  ]
}
```

| Field | Type | Notes |
|---|---|---|
| `scenario` | `String` | Free-text human label (e.g. "empty board", "three darts, dim lighting") — documentation only, not validated beyond non-blank |
| `lighting` | `String` | One of `"normal"` / `"dim"` (spec FR-004) — validated against this fixed set |
| `darts` | `Array<DartGroundTruth>` | May be empty (spec FR-001's zero-dart case, e.g. `empty_board.json`) |
| `darts[].sectorNumber` | `Int?` | `1`-`20`, or `null` when `ring` is `INNER_BULL`/`OUTER_BULL`/`MISS` — mirrors `DetectedThrow.sectorNumber` exactly (`cv/src/main/.../CvEngine.kt`) |
| `darts[].ring` | `String` | One of `Ring`'s enum names (`SINGLE`/`DOUBLE`/`TRIPLE`/`OUTER_BULL`/`INNER_BULL`/`MISS`) |

### Baseline photo pairing (discovered during implementation — see research.md)

`OpenCvDartDetector` detects darts by diffing a frame against a same-setup baseline frame with no
darts (`cv/src/main/.../opencv/OpenCvDartDetector.kt`); a single standalone photo can't be scored
on its own. Any fixture whose `darts` array is non-empty MUST have a matching
`<name>_baseline.png` in the same directory (no separate ground truth — it's diffed against, not
itself scored). A fixture with an empty `darts` array needs no baseline pair (it already
represents a zero-dart reference state). This pairing is documented in
`cv/src/androidTest/assets/fixtures/README.md` and enforced by `DetectionAccuracyBenchmarkTest`.

### Validation rules (spec FR-002, SC-004)

A fixture's ground truth is **rejected** (loader throws, with a message naming the fixture and the
problem) when:

- `ring` is not one of the six valid `Ring` names.
- `sectorNumber` is non-null but outside `1..20`.
- `sectorNumber` is non-null while `ring` is `INNER_BULL`, `OUTER_BULL`, or `MISS` (those rings
  never have a sector — same invariant `DetectedThrow` already encodes).
- `sectorNumber` is null while `ring` is `SINGLE`, `DOUBLE`, or `TRIPLE` (those rings always need
  a sector).
- `lighting` is not `"normal"` or `"dim"`.
- A `<name>.png` exists with no matching `<name>.json`, or vice versa (spec Edge Cases).

## New: In-memory model (`FixtureGroundTruth.kt`, `cv/src/main/.../fixtures/`)

```kotlin
data class DartGroundTruth(val sectorNumber: Int?, val ring: Ring)

data class FixtureGroundTruth(
    val fixtureName: String,       // e.g. "three_darts" (no extension)
    val scenario: String,
    val lighting: Lighting,
    val darts: List<DartGroundTruth>,
)

enum class Lighting { NORMAL, DIM }
```

`Ring` is reused as-is from `cv/src/main/.../CvEngine.kt` — no new enum, the ground-truth format's
`ring` strings are `Ring.entries` names.

## Reused (unchanged)

- `Ring`, `DetectedThrow`, `FrameInput`, `BoardCalibration`, `BoardCalibrationResult` —
  `cv/src/main/java/com/bullseyestracker/cv/CvEngine.kt`.
- `ScoreCalculator.valueFor(sectorNumber, ring)` — used by the benchmark to derive an expected
  point value from ground truth for reporting, though matching itself compares `(sectorNumber,
  ring)` pairs directly (research.md).
- `OpenCvBoardDetector`/`OpenCvDartDetector` — the detectors under test; no changes.

## New: Accuracy Benchmark Result (in-memory only, not persisted)

```kotlin
data class FixtureAccuracyResult(
    val fixtureName: String,
    val expectedDartCount: Int,
    val matchedDartCount: Int,
)

data class AccuracyBenchmarkResult(
    val perFixture: List<FixtureAccuracyResult>,
    val totalExpectedDarts: Int,
    val totalMatchedDarts: Int,
) {
    val accuracy: Float get() = if (totalExpectedDarts == 0) 1f else totalMatchedDarts.toFloat() / totalExpectedDarts
}
```

Computed once per `DetectionAccuracyBenchmarkTest` run, asserted against the 90% threshold (spec
FR-006), then discarded — this is a test-time value, not an app entity, and is not written to disk
or Room (no relation to `match`'s persistence at all).
