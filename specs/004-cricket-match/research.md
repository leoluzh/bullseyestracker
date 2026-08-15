# Phase 0 Research: Cricket Match Scoring

No `NEEDS CLARIFICATION` markers remain in the Technical Context — this feature reuses
`003-501-match`'s stack and dispatch pattern and adds no new dependency. The items below are the
design decisions worth recording.

## Decision: `CricketRules.resolveTurn` is a pure function operating per-dart, like `X501Rules`

**Decision**: `CricketRules.resolveTurn(marks: Map<CricketNumber, Int>, points: Int,
opponents: List<OpponentState>, throws: List<Throw>): CricketTurnResult` iterates throws in
order, applying each dart's marks to a running `marks`/`points` state, returning the final
marks/points plus whether the turn produced a match win. `OpponentState(marks, points)` is a
small read-only pair, not just marks — win detection (FR-009) needs to compare the active
player's point total against opponents' points, not just their closed/open state.

**Rationale**: Mirrors `X501Rules`'s already-established, test-first, stateless-function shape
(see `003-501-match` research.md) — takes explicit before-state, returns explicit after-state, no
engine instance. `opponents` (a read-only snapshot of every other player's marks *and* points at
the start of the turn) is needed because "points while opponent open" (FR-007), "closed by every
player" (FR-008), and the win check's points comparison (FR-009) all depend on other players'
state, unlike 501 where each turn only ever touches the active player's own score. (An earlier
draft of this decision passed only opponents' marks; implementing the win check surfaced that
points were needed too — see `CricketRulesTest`'s win-detection cases.)

**Alternatives considered**: Passing the whole `Match` object into `CricketRules` was rejected —
it would give the rules engine a dependency on `Match`/`MatchViewModel`-level concerns (whose
turn is next, persistence) that aren't its job, matching the same boundary `X501Rules` draws
(research.md: "the rules engine itself doesn't touch `Match`, only a single player's score").
`opponents: List<OpponentState>` is the minimal slice `CricketRules` actually needs.

## Decision: per-dart mark application, not per-turn aggregate

**Decision**: Like `X501Rules`, `resolveTurn` applies each dart's marks one at a time in a loop,
re-checking closed state after each dart — not by summing a turn's marks first and checking
closed/points once at the end.

**Rationale**: Directly required by spec Edge Cases ("a single dart can both close a number and
score points") and Acceptance Scenario coverage in `CricketRulesTest` — e.g. a player with 2
marks on a number who throws a triple: the first of the 3 marks closes the number, the remaining
2 (if an opponent is still open) score points. A turn-level aggregate can't distinguish "which
marks arrived before vs. after closing" within one dart, let alone across darts in the same turn.

**Alternatives considered**: Per-turn aggregation (sum marks, then compute points/closing once)
was rejected for the reason above — it produces wrong point totals whenever a number transitions
from open to closed mid-turn.

## Decision: mark weight comes from `ThrowRing`, not `Throw.value`

**Decision**: Mark count per dart is derived from `ThrowRing` — `SINGLE`/`OUTER_BULL` → 1,
`DOUBLE`/`INNER_BULL` → 2, `TRIPLE` → 3, `MISS` → 0 — not from `Throw.value` (which holds the
501-style computed score, e.g. triple-20 = 60). The target number is `Throw.sectorNumber` for
15-20, or `CricketNumber.BULL` when `ring` is `OUTER_BULL`/`INNER_BULL`; any other sector (1-14,
plus MISS) registers zero marks (FR-005).

**Rationale**: Cricket marks are a count (1/2/3), not a point value, so reusing `Throw.value`
directly would be wrong — a triple-20 has `value = 60` but should register 3 marks, not 60.
Deriving from `ring` keeps `CricketRules` independent of `X501Rules`'s value semantics while
reusing the exact same `Throw`/`ThrowRing` types (no new CV-facing type needed), consistent with
`003-501-match` research.md's `cv.Ring`→`match.ThrowRing` mapping already handling ring
translation upstream in `MatchViewModel`.

**Alternatives considered**: Adding a separate `markCount: Int` field to `Throw` was rejected as
unnecessary duplication — `ring` already fully determines mark weight via a simple lookup, and
`Throw` is shared with 501 where this field would be meaningless.

## Decision: `MatchViewModel` dispatches on `Match.gameMode`, `X501Rules` path untouched

**Decision**: `MatchViewModel.confirmTurn` branches on `currentMatch.gameMode`: `FIVE_O_ONE`
keeps calling `X501Rules.resolveTurn` exactly as `003-501-match` built it; `CRICKET` calls
`CricketRules.resolveTurn` with the active player's marks/points and a snapshot of every other
player's marks, then applies the result to `Player.marks`/`Player.points` and checks for a win.

**Rationale**: This is the extension point `003-501-match`'s own plan.md flagged (parent
`tasks.md` T037: "Extend `MatchViewModel` to dispatch to `CricketRules` when `Match.gameMode ==
CRICKET`"). Keeps the two rule engines fully independent and unit-testable in isolation, while
sharing one turn-confirmation entry point in the UI layer.

**Alternatives considered**: A shared `GameRules` interface with one `resolveTurn` signature for
both modes was rejected — 501 and Cricket have fundamentally different inputs (one player's
remaining score vs. one player's marks plus every opponent's marks) and outputs (bust/checkout vs.
marks/points/closed-numbers), so a unified interface would force an awkward least-common-denominator
shape onto both for no real benefit; `MatchViewModel`'s `when (gameMode)` branch is simpler and
matches the parent plan's own framing.

## Decision: Cricket setup allows 2+ players, no upper bound imposed by this feature

**Decision**: `MatchSetupScreen`'s Cricket path only enforces a minimum of 2 players (FR-001) —
it does not add a Cricket-specific maximum. The existing `Match` model's `init` block already
requires `players.size in 2..4` (`003-501-match`/001's shared invariant), so the practical
ceiling stays 4 unless a future feature explicitly changes `Match`.

**Rationale**: The feature description says "2+" and spec Assumptions explicitly defer to the
existing `Match` model rather than inventing a new cap. Changing `Match.init`'s `2..4` invariant
is out of scope for this feature (it's shared with 501 and touches `003-501-match`'s already-
shipped code) — if a genuinely unbounded Cricket player count is wanted later, that's a separate,
explicit decision.

**Alternatives considered**: Loosening `Match.init` to allow more than 4 players for Cricket only
was rejected as scope creep — no acceptance scenario in this spec requires more than 4 players,
and doing so would require a mode-conditional invariant in a model class both features share.
