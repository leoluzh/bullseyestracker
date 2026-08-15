# Feature Specification: Fixture Image Set & Detection Accuracy Benchmark

**Feature Branch**: `006-fixture-benchmark`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Curated fixture image set and detection-accuracy benchmark: build a
curated set of real dartboard photos (varied dart counts/positions/lighting) with ground-truth
sector/ring labels, and an automated accuracy benchmark test asserting >=90% correct sector+ring
detection (spec 001 SC-002). This also unblocks the currently-failing instrumented tests that
reference cv/src/androidTest/assets/fixtures/*.png (OpenCvBoardDetectorTest,
OpenCvDartDetectorTest, PerformanceBenchmarkTest) which fail today on a missing-asset error.
Covers task T044 from specs/001-dart-scoring-match/tasks.md polish phase. Note: actual photo
capture of a physical dartboard is a manual step outside AI capability - the deliverable includes
the harness/tooling/ground-truth format and the benchmark test, with fixture capture as an
explicit manual prerequisite/assumption."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Curate and document ground-truth fixture photos (Priority: P1)

A developer maintaining the app captures a set of real dartboard photos — varying how many darts
are stuck in the board, where they land, and the lighting — and records, for every photo, the
correct sector/ring/multiplier for each dart present. This turns a folder of photos into a
trustworthy reference dataset the rest of the app's automated checks can be measured against,
instead of relying on manual eyeballing.

**Why this priority**: Every other capability in this feature — the accuracy benchmark, and
unblocking the three currently-failing detector tests — depends on a curated, labeled photo set
existing first. Without it, nothing else here can run.

**Independent Test**: Given the documented ground-truth format, a developer can add a photo plus
its labels to the curated set and have it load and validate successfully (parsed into a correct
per-dart record) without any detection code running at all.

**Acceptance Scenarios**:

1. **Given** a new dartboard photo and its intended per-dart labels, **When** a developer adds it
   to the curated set following the documented format, **Then** the set recognizes it as a new
   fixture with its ground truth correctly associated, with no code changes required elsewhere.
2. **Given** a fixture's ground truth references an impossible dart (e.g., a sector outside
   1-20/bull, or a nonsensical ring), **When** the set is loaded, **Then** the problem is reported
   clearly and the invalid fixture is rejected rather than silently accepted.
3. **Given** the curated set, **When** it is inspected, **Then** it includes at minimum: a clearly
   visible board, a photo with no board present, an empty board, one dart, and three darts —
   the baseline scenarios today's detector tests already expect but currently lack.

---

### User Story 2 - Automated detection-accuracy benchmark (Priority: P2)

Using the curated fixture set from User Story 1, an automated check runs the app's dart detection
against every fixture and reports how often it gets the sector, ring, and multiplier exactly
right, failing loudly if that accuracy drops below the 90% bar the app is expected to meet.

**Why this priority**: Turns the curated set into an enforceable, repeatable quality gate — a
permanent regression check — rather than a one-off manual sanity check. It depends on User Story
1's fixtures existing, so it is sequenced after it.

**Independent Test**: With the curated set in place, run the benchmark; it reports a single
accuracy percentage across all fixtures and fails the run if that percentage is below 90%, and
also fails clearly (not with a misleading pass) if the fixture set is missing or incomplete.

**Acceptance Scenarios**:

1. **Given** the curated fixture set is present and complete, **When** the benchmark runs,
   **Then** it reports the overall percentage of darts scored with exactly correct sector, ring,
   and multiplier across every fixture.
2. **Given** the computed accuracy is at or above 90%, **When** the benchmark completes, **Then**
   it passes.
3. **Given** the computed accuracy is below 90%, **When** the benchmark completes, **Then** it
   fails and reports the actual percentage, so a real detection regression cannot pass unnoticed.
4. **Given** the curated fixture set or its ground truth is missing or incomplete, **When** the
   benchmark runs, **Then** it fails with a clear message identifying what's missing, rather than
   reporting a misleadingly high or low accuracy number.

---

### Edge Cases

- What happens when the detector finds a different number of darts than the ground truth for a
  given fixture (extra or missing darts)? Every ground-truth dart with no matching correct
  detection MUST count against accuracy — no partial credit for "close" matches.
- What happens when two darts in a fixture are labeled as landing in the same very small area
  (near-adjacent segments)? The ground truth format MUST still allow each to be labeled and
  scored independently, since this is exactly the ambiguous case detection needs to be measured
  against (parent spec Edge Cases).
- What happens if someone adds a new fixture photo but forgets to add its ground truth (or vice
  versa)? The set MUST fail to load with a clear error rather than silently skipping the fixture.
- What happens when the curated set grows over time? Adding a new labeled fixture MUST require no
  changes to the benchmark test itself — only to the fixture set and its ground truth.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST define a documented ground-truth format for dartboard fixture photos
  that records, per photo, every dart present (sector, ring, multiplier) and the total dart count,
  including the case of zero darts.
- **FR-002**: System MUST validate a fixture set's ground truth when it is loaded and reject, with
  a clear error, any entry referencing an impossible dart (sector outside 1-20/bull, or an invalid
  ring/multiplier combination).
- **FR-003**: The curated fixture set MUST include, at minimum, the baseline scenarios already
  required by the app's existing detector tests: a clearly visible board, a frame with no board,
  an empty board, one dart, and three darts — so those tests stop failing on a missing-asset error.
- **FR-004**: The curated fixture set MUST include photos that vary dart count (zero through at
  least three), dart position (different sectors/rings, including darts placed close together),
  and lighting condition (at minimum normal and dim/low-light).
- **FR-005**: System MUST provide an automated benchmark that runs detection against every fixture
  in the curated set and computes an overall accuracy: darts scored with exactly correct sector,
  ring, and multiplier, divided by the total ground-truth darts across the whole set.
- **FR-006**: The benchmark MUST fail when computed accuracy is below 90% (spec
  001-dart-scoring-match's SC-002 threshold) and MUST report the computed percentage either way.
- **FR-007**: The benchmark MUST fail with a clear, actionable message if the curated fixture set
  or its ground truth is missing or incomplete, instead of computing a misleading accuracy number.
- **FR-008**: System MUST document the steps required to capture and label an additional fixture
  photo so the curated set can be extended over time without modifying the benchmark itself
  (Edge Cases).

### Key Entities

- **Fixture Photo**: An image of a physical dartboard (with zero or more darts stuck in it),
  tagged with its lighting condition and intended test scenario (e.g., "three darts, dim
  lighting").
- **Ground Truth Record**: Per fixture photo, the authoritative list of darts present, each with
  its correct sector (1-20 or bull), ring (single/double/triple/outer-bull/inner-bull), or a
  miss.
- **Accuracy Benchmark Result**: The computed match rate between detected and ground-truth darts
  across the entire curated set, and the pass/fail verdict against the 90% target.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can add a new labeled fixture photo to the curated set and have it
  included in the next benchmark run without modifying any test code.
- **SC-002**: The benchmark reports a single accuracy percentage across the full curated set and
  fails when that percentage is below 90%, matching the parent spec's SC-002 threshold.
- **SC-003**: The three currently-failing detector tests pass once the curated set's baseline
  fixtures are in place, with zero changes to those test files (they already reference this
  fixture set by name).
- **SC-004**: 100% of ground-truth entries in the curated set are validated — and any invalid
  entry rejected — before any accuracy is computed, so a bad label can never silently inflate or
  deflate the reported accuracy.

## Assumptions

- Physically photographing a real dartboard (with darts thrown into it at varied positions, dart
  counts, and lighting) is a manual, human step outside this feature's automatable scope. This
  feature delivers the ground-truth format, validation, loading, and the benchmark test itself;
  actual fixture photo capture is an explicit external prerequisite this feature depends on but
  does not itself perform — tracked as a follow-up step for whoever has physical access to a
  board.
- A minimum of roughly 15-20 labeled photos is assumed sufficient to produce a statistically
  meaningful accuracy figure against the 90% target, covering the required baseline scenarios
  (FR-003) plus meaningful variation (FR-004), without requiring an exhaustive dataset. This is a
  starting-point default, not a hard cap — the set is expected to grow over time (SC-001).
- Ground truth is authored and reviewed by a human, not generated by another detection model — no
  auto-labeling tooling is in scope for this feature.
- The accuracy benchmark measures the app's existing, already-shipped detection pipeline; it does
  not itself change how detection works. If measured accuracy comes in under 90% against real
  fixtures, improving the detector is separate follow-up work, not part of this feature.
