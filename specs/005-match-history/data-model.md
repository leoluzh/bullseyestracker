# Phase 1 Data Model: Match History & Resume

No new entities, fields, or persisted state. This feature reads existing data via existing
repository methods; it does not extend `match`'s Room schema or domain model.

## Reused entities (unchanged, from `001-dart-scoring-match` / `match` module)

### `Match` (`match/src/main/java/com/bullseyestracker/match/model/Match.kt`)

| Field | Type | Used by this feature for |
|---|---|---|
| `id` | `String` | Identifying a history-list row / detail lookup key |
| `gameMode` | `GameMode` (`FIVE_O_ONE` / `CRICKET`) | History list/detail mode label |
| `players` | `List<Player>` | History list player names; detail per-player final results |
| `status` | `MatchStatus` (`IN_PROGRESS` / `COMPLETED`) | Resume routing (already implemented, see research.md); history list only ever sees `COMPLETED` rows (query-filtered) |
| `winnerId` | `String?` | History list/detail winner display |
| `startedAt` / `endedAt` | `Long` / `Long?` | History list completion date, sort order (`endedAt DESC`) |

### `Player` (`match/src/main/java/com/bullseyestracker/match/model/Player.kt`)

| Field | Type | Used by this feature for |
|---|---|---|
| `name` | `String` | List/detail player display |
| `remainingScore` | `Int?` | 501 detail final score |
| `marks` | `Map<CricketNumber, Int>` | Cricket detail final marks |
| `points` | `Int` | Cricket detail final points |

## Reused repository surface (`match/src/main/java/com/bullseyestracker/match/data/MatchRepository.kt`)

- `observeInProgressMatch(): Flow<Match?>` — already wired into `MatchViewModel` for resume; no
  change.
- `observeCompletedMatches(): Flow<List<Match>>` — feeds `HistoryViewModel`'s list state
  (FR-004), already sorted `endedAt DESC` at the DAO level.
- `getMatch(matchId: String): Match?` — feeds `HistoryViewModel`'s detail lookup (FR-005) when a
  list row is tapped.

## New in-memory UI state (not persisted)

### `HistoryViewModel` state (`app/src/main/java/com/bullseyestracker/ui/history/HistoryViewModel.kt`)

- `completedMatches: StateFlow<List<Match>>` — sourced from `observeCompletedMatches()`, drives
  `MatchHistoryScreen`.
- `selectedMatch: StateFlow<Match?>` — set by calling `getMatch(matchId)` when a list row is
  tapped; drives `MatchHistoryDetailScreen`. Cleared on navigating back to the list.

### `MainActivity` screen state

A small enum/sealed state (e.g. `AppScreen`: `Setup`, `Play`, `History`, `HistoryDetail(matchId)`)
held the same way `captureMode` already is (`remember { mutableStateOf(...) }`) — not a
persisted entity, purely in-memory navigation state scoped to the Activity's Compose tree. Not a
data model concept; listed here only to be explicit that no new persisted "screen" or
"navigation" entity is introduced.
