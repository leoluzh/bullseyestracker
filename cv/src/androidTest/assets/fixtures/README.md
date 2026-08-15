# Dartboard detection fixtures

This directory is empty except for this README. It needs real photos of a physical dartboard —
capturing them is a manual step (spec `006-fixture-benchmark` Assumptions); nothing in this repo
can do it for you. Until they're added, `OpenCvBoardDetectorTest`, `OpenCvDartDetectorTest`,
`PerformanceBenchmarkTest`, and `DetectionAccuracyBenchmarkTest` all fail on a missing-asset error
— that's expected, not a bug.

## What to add

Every fixture is a `<name>.png` photo plus a `<name>.json` ground-truth file with the same base
name, both directly in this directory (no subfolders).

### Dart detection needs a same-setup empty reference photo

`OpenCvDartDetector` finds darts by diffing a frame against a baseline — it can't score a single
standalone photo on its own. **Any fixture whose ground truth has one or more darts must be paired
with `<name>_baseline.png`**: the same camera framing/lighting, but with zero darts in the board.
No `.json` needed for the baseline photo itself — it isn't scored, only diffed against.
Zero-dart fixtures (`darts: []`, e.g. `empty_board.png`) don't need a baseline pair — they *are*
one.

Example: `three_darts.png` + `three_darts.json` (ground truth) + `three_darts_baseline.png` (same
board/camera, no darts).

### Required baseline fixtures (unblocks the three existing detector tests)

| File | Scenario |
|---|---|
| `board_calibrated.png` / `.json` | A clear, well-lit, unobstructed board — must calibrate successfully |
| `no_board.png` / `.json` | A frame with no dartboard visible at all (e.g. a wall, a table) |
| `empty_board.png` / `.json` | The board, calibratable, with zero darts in it (doubles as `one_dart`/`three_darts`'s baseline pair if shot from the same setup) |
| `one_dart.png` / `.json` + `one_dart_baseline.png` | The board with exactly one dart stuck in it |
| `three_darts.png` / `.json` + `three_darts_baseline.png` | The board with exactly three darts stuck in it |

### Additional curated variety (for a meaningful accuracy benchmark)

Add more fixtures beyond the baseline five, varying:

- **Dart count**: 0 through 3+ darts.
- **Position**: different sectors, different rings (single/double/triple/bull), and at least one
  fixture with two darts landing close together (adjacent segments) — the ambiguous case
  detection most needs to be measured against.
- **Lighting**: at least one `"normal"` fixture and one `"dim"` (low-light) fixture per scenario
  where practical.

A good starting target is 15-20 total labeled photos (research.md's default assumption) — more is
fine, there's no upper limit, and the benchmark picks up new fixtures automatically (no test code
changes needed, spec SC-001).

## Ground-truth JSON format

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

- `scenario`: free-text label, must not be blank.
- `lighting`: exactly `"normal"` or `"dim"`.
- `darts`: one entry per dart actually stuck in the board photo, `[]` for a zero-dart fixture.
  - `ring`: one of `SINGLE`, `DOUBLE`, `TRIPLE`, `OUTER_BULL`, `INNER_BULL`, `MISS`.
  - `sectorNumber`: `1`-`20` for `SINGLE`/`DOUBLE`/`TRIPLE`; `null` for
    `OUTER_BULL`/`INNER_BULL`/`MISS` (those never have a sector).

A fixture with an invalid combination (unknown ring name, a sector present on a bull/miss ring, a
missing sector on a single/double/triple ring, a sector outside `1..20`, or an invalid `lighting`
value) is rejected at load time with a specific error — see
`cv/src/main/java/com/bullseyestracker/cv/fixtures/FixtureGroundTruth.kt`.

## How to capture and label a fixture

1. Set up a standard, regulation-pattern dartboard (parent spec's Assumptions) with good, even
   lighting for `"normal"` fixtures, or noticeably dimmer/uneven lighting for `"dim"` ones.
2. Throw (or place) the darts you want for the scenario, then photograph the board straight-on,
   filling most of the frame, the same way the app's own capture screens expect (parent spec
   Assumptions: "a reasonably direct, unobstructed view of the full board face").
3. If the scenario has one or more darts, also take a second photo of the same setup with the
   darts removed (camera untouched) — save it as `<name>_baseline.png` (see above).
4. Save the darts photo as `<name>.png` in this directory, choosing a name that describes the
   scenario (e.g. `two_darts_triple20_and_bull_dim.png`).
5. By hand, determine the correct sector + ring for every dart in the photo (you're the ground
   truth — this is exactly what the benchmark measures the detector against) and write the
   matching `<name>.json` per the format above.
6. Run `make connected-test` — a malformed ground-truth file, or a fixture missing its baseline
   photo, fails immediately with a specific error; a well-formed, complete fixture gets picked up
   by `DetectionAccuracyBenchmarkTest` automatically.
