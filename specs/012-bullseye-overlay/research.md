# Phase 0 Research: Bullseye Calibration Overlay

No `NEEDS CLARIFICATION` markers remain — spec Assumptions already resolved the interpretation
question (both inner and outer bull boundaries). Decisions below cover implementation-level
choices made during planning.

## Decision: `BullseyeOverlay` is a separate composable from `DetectionOverlay`, same package

**Rationale**: `DetectionOverlay` renders a `List<DetectedThrow>` as tappable markers — its whole
API and internal logic is shaped around that list and tap handling. The bullseye boundary is a
different, independent piece of state (a nullable single `BoardCalibration`, not a list) with no
tap interaction at all. Keeping them as separate composables avoids overloading
`DetectionOverlay`'s parameters/responsibilities and lets each screen compose them independently
(spec Acceptance Scenario 5: both visible simultaneously, neither blocking the other). Same
package (`ui/detection/`) because it's the same UI layer solving a closely related problem, not a
different concern deserving its own package.

**Alternatives considered**: Extending `DetectionOverlay` with an optional `calibration` parameter
— rejected, mixes two independent concerns (tappable dart list vs. non-interactive calibration
boundary) into one composable's API for no reuse benefit, since nothing about their rendering or
interaction logic overlaps.

## Decision: circles drawn with Compose `Canvas`/`drawCircle` (`Stroke` style), not `Box`+`clip`

**Rationale**: `DetectionOverlay`'s existing markers are filled circles (`Box` + `CircleShape` +
`background`), which is the right tool for a small filled/tappable marker. A boundary *outline*
that shouldn't obscure the camera feed or existing markers underneath it (spec Assumptions: "draw
circle outlines only... camera feed... and existing dart markers remain fully visible") needs an
unfilled stroke — Compose's `Canvas.drawCircle(color, radius, center, style = Stroke(width))` is
the direct, idiomatic way to draw that, and needs no new dependency (same `androidx.compose.ui`
graphics APIs already in use).

**Alternatives considered**: A `Box` with a `border()` modifier shaped as a circle — technically
possible but more awkward for a precisely-centered, precisely-radius-sized circle at an arbitrary
pixel position computed from normalized coordinates; `Canvas.drawCircle` takes center/radius
directly in the same pixel space `DetectionOverlay` already computes with
`constraints.maxWidth`/`maxHeight`, and is a common enough case there's no reason to solve it
through a modifier chain instead.

## Decision: `LiveScoringScreen`/`PhotoScoringScreen` each hold their own calibration state locally

**Rationale**: Neither screen has a shared ViewModel today (per CLAUDE.md/constitution — they're
standalone, independently-testable-by-design per spec 001's User Stories 1/2); each already holds
analogous local `remember { mutableStateOf(...) }` state for its own status text and detected
throws. Adding a third piece of co-located state (`BoardCalibration?`) follows the exact same
pattern already established in both files, rather than introducing a new state-holder class for
one small addition.

**Alternatives considered**: A shared `CalibrationState` object/ViewModel used by both screens —
rejected as premature abstraction; the two screens' existing calibration flows are already
different enough (continuous `onCalibrated`/`onBoardNotFound` callbacks for live vs. one-shot
`scorePhoto` result for photo) that a shared holder would need to bridge two different shapes for
no current reuse benefit.

## Decision: clearing calibration on `onBoardNotFound` (live) / retake or NotFound (photo)

**Rationale**: Directly required by spec FR-003/Acceptance Scenario 4 ("previously-drawn bullseye
circles are removed" when the board is lost). `LiveDetectionAnalyzer` already calls
`onBoardNotFound()` distinctly from `onCalibrated(...)` — wiring that existing callback to also
clear the screen's calibration state requires no analyzer changes, just one more state write in
the callback lambda already present in `LiveScoringScreen`. `PhotoScoringScreen`'s equivalent is
setting calibration to `null` in the existing `BoardCalibrationResult.NotFound` branch and in
`retake()`, both of which already exist and already reset other state (`currentThrows`, `status`).

**Alternatives considered**: Leaving the last-known calibration visible even after board loss —
rejected, directly contradicts spec Acceptance Scenario 4's explicit requirement and would show a
stale, no-longer-true boundary as if it were current.
