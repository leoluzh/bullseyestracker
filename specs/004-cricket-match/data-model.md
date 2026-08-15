# Phase 1 Data Model: Cricket Match Scoring

This feature reuses `001-dart-scoring-match`'s domain model unchanged (`Match`, `Player`, `Turn`,
`Throw`, `CricketNumber`) — see `specs/001-dart-scoring-match/data-model.md`. No new entity,
field, or Room schema change is introduced; `Player.marks: Map<CricketNumber, Int>` and
`Player.points: Int` already exist and are already persisted (`Converters.marksToString`/
`stringToMarks`, `PlayerEntity.marks`/`points`). This document covers only the new non-persisted
value types this feature adds on top of that model.

## `CricketTurnResult` (new, `match.rules` package, not persisted)

Output of `CricketRules.resolveTurn`; the caller (`MatchViewModel`) turns it into persisted state
via `MatchRepository.saveTurn`.

| Field | Type | Notes |
|---|---|---|
| `newMarks` | `Map<CricketNumber, Int>` | Active player's mark counts after this turn (existing entries + marks applied by this turn's throws). |
| `newPoints` | `Int` | Active player's points after this turn (`points` before + points scored by this turn's throws). |
| `throwsUsed` | `List<Throw>` | All throws in the turn — unlike `X501Rules`, Cricket has no early-stop condition (no bust), so this is always the full input list (kept for symmetry with `TurnResult` and for turn persistence). |
| `isMatchWin` | `Boolean` | `true` if this turn's marks closed the active player's last remaining open number while their points are strictly higher than every other player who also has that number closed (FR-009). |

## `MarkApplication` (internal detail, not a public type)

Not a distinct data class — documented here as the per-dart algorithm `resolveTurn` runs
internally, since it's the crux of the feature (research.md "per-dart mark application"):

For each `Throw` in order:

1. Determine the target `CricketNumber?` — `Throw.sectorNumber` mapped to `CricketNumber` for
   15-20, or `CricketNumber.BULL` when `ring` is `OUTER_BULL`/`INNER_BULL`; `null` (no-op) for
   any other sector or `MISS` (FR-005).
2. If no target, continue to the next throw.
3. Determine mark weight from `ring`: `SINGLE`/`OUTER_BULL` → 1, `DOUBLE`/`INNER_BULL` → 2,
   `TRIPLE` → 3 (FR-004).
4. Apply marks to the running `marks[target]` **one mark at a time** (not the full weight in one
   step), so the closed/open transition is evaluated between marks within the same dart:
   - If `marks[target] < 3` (still open for this player): increment `marks[target]`.
   - If `marks[target] >= 3` (already closed for this player): only increment `points` by the
     number's value (15-20 face value, `BULL` = 25) — and only if at least one entry in
     `opponents` has `< 3` marks for `target` (FR-007/FR-008); `marks[target]` itself no
     longer changes once closed (a count is not needed past 3 for rule purposes, though nothing
     forbids tracking overflow marks if a future UI wants them — out of scope here).
5. After all throws are applied, check the win condition (FR-009): every `CricketNumber` has
   `newMarks[number] >= 3`, and `newPoints` is strictly greater than every opponent's points
   among opponents who also have that number's full set closed. (Per spec Assumptions, a tie
   means no win yet.)

## State transitions this feature drives (on existing `Match`/`Player` fields)

Mirrors `003-501-match` data-model.md's table, for Cricket:

| `CricketTurnResult` | `Player.marks` | `Player.points` | `Match.currentPlayerIndex` | `Match.status` / `winnerId` |
|---|---|---|---|---|
| Any (no bust concept) | set to `newMarks` | set to `newPoints` | advances to next player (mod player count) | `COMPLETED` + `winnerId` = active player's id, only if `isMatchWin` |

This table is the app-layer (`MatchViewModel`) contract; `CricketRules` itself never touches
`Match` — it only computes `CricketTurnResult` from one player's marks/points and a read-only
snapshot of opponents' marks (research.md).

## Reused entities (unchanged — for reference only)

- `Match` (`match.model.Match`) — `players`, `currentPlayerIndex`, `status`, `winnerId`,
  `gameMode` (now actually dispatched on, not just stored).
- `Player` (`match.model.Player`) — `marks: Map<CricketNumber, Int>`, `points: Int` (Cricket
  mode); `isNumberClosed(number)` helper already defined.
- `CricketNumber` (`match.model.CricketNumber`) — `FIFTEEN`..`TWENTY`, `BULL`, each with a
  `pointValue`.
- `Turn` (`match.model.Turn`) — `throws`, `outcome`. Cricket turns use `TurnOutcome.NORMAL` for
  every non-winning turn (no bust/checkout concept) and `TurnOutcome.CHECKOUT` when
  `isMatchWin` is true, matching how `003-501-match` already uses `CHECKOUT` to mean "this turn
  won the match" rather than adding a Cricket-specific outcome value.
- `Throw` (`match.model.Throw`) — `sectorNumber`, `ring` (`ThrowRing`), `value` (unused by
  Cricket scoring — see research.md).

See `specs/001-dart-scoring-match/data-model.md` for full field definitions and Room entity
mappings — none of that is modified here.
