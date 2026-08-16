# Phase 1 Data Model: Player Statistics

No new persisted entities, fields, or schema changes. This feature reads existing `Match`/`Player`
data via the existing `MatchRepository.observeCompletedMatches()` and computes a read-only,
in-memory summary from it.

## New: `PlayerStats` (`match/src/main/java/com/bullseyestracker/match/stats/PlayerStatsCalculator.kt`)

```kotlin
data class PlayerStats(
    val name: String,          // first-seen trimmed spelling (research.md)
    val matchesPlayed: Int,
    val matchesWon: Int,
) {
    val winRate: Float get() = if (matchesPlayed == 0) 0f else matchesWon.toFloat() / matchesPlayed
}
```

| Field | Type | Notes |
|---|---|---|
| `name` | `String` | Display name — the trimmed spelling from the earliest (`Match.startedAt`) completed match this player name appeared in (research.md) |
| `matchesPlayed` | `Int` | Count of completed matches this name appeared in (any game mode — spec Assumptions: combined, not per-mode) |
| `matchesWon` | `Int` | Count of those matches where this player's `Player.id == Match.winnerId` |
| `winRate` | `Float` (computed) | `matchesWon / matchesPlayed`, `0f` when `matchesPlayed == 0` (never actually emitted — every `PlayerStats` in the output has `matchesPlayed >= 1` by construction) |

## New: `PlayerStatsCalculator` (same file)

```kotlin
object PlayerStatsCalculator {
    /**
     * [matches] MUST already be completed-only (e.g. MatchRepository.observeCompletedMatches()'s
     * output) — this function does not filter by MatchStatus itself (research.md).
     */
    fun compute(matches: List<Match>): List<PlayerStats>
}
```

Algorithm (research.md decisions):

1. Sort input `matches` by `startedAt` ascending (establishes first-seen order for display names
   and stable tie-breaking).
2. For each match, for each `Player`: compute identity key `player.name.trim().lowercase()`;
   look up or create that key's running total, using `player.name.trim()` as the display name the
   *first* time the key is seen.
3. Increment that key's `matchesPlayed`; increment `matchesWon` too if
   `player.id == match.winnerId`.
4. Emit one `PlayerStats` per distinct key, sorted by `winRate` descending, then `matchesPlayed`
   descending (spec FR-006, Edge Cases tie-break — research.md).

## Reused (unchanged)

- `Match`, `Player`, `MatchStatus`, `GameMode` — `match/src/main/java/com/bullseyestracker/match/model/`.
- `MatchRepository.observeCompletedMatches(): Flow<List<Match>>` — already implemented (used by
  `005-match-history`); the sole data source for this feature, no changes.

## New in-memory UI state (not persisted)

### `PlayerStatsViewModel` state (`app/src/main/java/com/bullseyestracker/ui/stats/PlayerStatsViewModel.kt`)

- `playerStats: StateFlow<List<PlayerStats>>` — sourced from `observeCompletedMatches()` piped
  through `PlayerStatsCalculator.compute`, drives `PlayerStatsScreen`. Recomputes automatically on
  every new emission from the underlying `Flow` (spec SC-003 — always current, no stale snapshot).
