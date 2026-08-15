# Phase 1 Data Model: 501 Match Scoring

This feature reuses `001-dart-scoring-match`'s domain model unchanged (`Match`, `Player`, `Turn`,
`Throw` — see `specs/001-dart-scoring-match/data-model.md`). No new entity, field, or Room schema
change is introduced. This document covers only the new non-persisted value types this feature
adds on top of that model.

## `TurnResult` (new, `match.rules` package, not persisted)

Output of `X501Rules.resolveTurn`; the caller (`MatchViewModel`) is responsible for turning it
into persisted state via `MatchRepository.saveTurn`.

| Field | Type | Notes |
|---|---|---|
| `newRemainingScore` | `Int` | Player's remaining score after this turn. Equals `remainingScoreBefore` unchanged on `BUST`; `0` on `CHECKOUT`; decremented on `NORMAL`. |
| `outcome` | `TurnOutcome` | One of `NORMAL`, `BUST`, `CHECKOUT` (see research.md — `MATCH_WIN` is not produced here). |
| `throwsUsed` | `List<Throw>` | Prefix of the input `throws` actually applied — shorter than the input when a bust/checkout ends the turn early (spec Edge Cases). |

**Validation rules** (enforced inside `resolveTurn`, mirrored by `X501RulesTest`):

- A running total below 0 after a dart → `BUST`, `newRemainingScore` reverts to
  `remainingScoreBefore`, turn stops at that dart (FR-004).
- A running total of exactly 1 after a dart → `BUST` (a double can never finish from 1),
  same revert/stop behavior (FR-004).
- A running total of exactly 0 after a dart where that dart's `ring` is `DOUBLE` or
  `INNER_BULL` → `CHECKOUT`, turn stops at that dart (FR-005, FR-006).
- A running total of exactly 0 after a dart whose `ring` is neither `DOUBLE` nor `INNER_BULL` →
  `BUST`, revert/stop (FR-005).
- Otherwise (running total between 2 and `remainingScoreBefore - 1` inclusive, or the turn's
  darts are exhausted without hitting any of the above) → `NORMAL`, `newRemainingScore` is the
  final running total, all input throws are in `throwsUsed`.

## State transitions this feature drives (on existing `Match`/`Player` fields)

No new fields — this documents which existing fields `MatchViewModel` writes, and when, driven by
`TurnResult.outcome`:

| `TurnResult.outcome` | `Player.remainingScore` | `Match.currentPlayerIndex` | `Match.status` / `winnerId` |
|---|---|---|---|
| `NORMAL` | set to `newRemainingScore` | advances to next player (mod player count) | unchanged |
| `BUST` | unchanged (stays at `remainingScoreBefore`) | advances to next player | unchanged |
| `CHECKOUT` | set to `0` | advances to next player (FR-002 — turn still advances even though match ends) | `status = COMPLETED`, `winnerId` = the checking-out player's id |

This table is the app-layer (`MatchViewModel`) contract; `X501Rules` itself never touches `Match`
or `Player` — it only computes `TurnResult` for one player's score in isolation (research.md).

## Reused entities (unchanged — for reference only)

- `Match` (`match.model.Match`) — `players`, `currentPlayerIndex`, `status`, `winnerId`.
- `Player` (`match.model.Player`) — `remainingScore` (501 mode).
- `Turn` (`match.model.Turn`) — `throws`, `outcome` (persisted `TurnOutcome`, set from
  `TurnResult.outcome`).
- `Throw` (`match.model.Throw`) — `sectorNumber`, `ring` (`ThrowRing`), `value`.

See `specs/001-dart-scoring-match/data-model.md` for full field definitions and Room entity
mappings (`match.data.*Entity`, `match.data.Mappers.kt`) — none of that is modified here.
