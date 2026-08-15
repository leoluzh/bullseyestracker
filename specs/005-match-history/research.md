# Phase 0 Research: Match History & Resume

No `NEEDS CLARIFICATION` markers remain in the Technical Context (see plan.md) — the spec, prior
`003-501-match`/`004-cricket-match` plans, and direct code inspection resolved every unknown
before this document was written. Findings below record the decisions made and why, per the
Phase 0 template.

## Decision: Resume-on-launch (FR-001–FR-003) needs no new implementation

**Rationale**: Read `MatchViewModel.kt` (app) and `MatchRepository.kt`/`MatchDao.kt` (match).
`MatchViewModel.init` already does:

```kotlin
init {
    viewModelScope.launch {
        matchRepository.observeInProgressMatch().collect { inProgress ->
            if (inProgress != null) _match.value = inProgress
        }
    }
}
```

and `MainActivity` already renders `MatchSetupScreen` when `match == null`, or the appropriate
scoreboard otherwise. `MatchDao.observeInProgressMatch()` is `SELECT * FROM matches WHERE status =
'IN_PROGRESS' LIMIT 1` — it structurally cannot return a completed match, so a fresh app launch
after a match finished naturally lands on `MatchSetupScreen` (spec Acceptance Scenario 3), and a
launch mid-match lands on the scoreboard with full persisted state (Acceptance Scenarios 1-2, since
`assembleMatch` reads all persisted `Player` rows for the match). This was almost certainly built
alongside 003/004's `MatchViewModel` but never called out as fulfilling parent-spec FR-011.

**Alternatives considered**: Re-implementing resume logic in a new class was rejected — it would
duplicate exactly what `MatchViewModel.init` already does, with no behavior difference, violating
the "don't add abstractions beyond what the task requires" norm.

## Decision: No new persistence/query work for history (FR-004–FR-006)

**Rationale**: `MatchDao.observeCompletedMatches()` (`SELECT * FROM matches WHERE status =
'COMPLETED' ORDER BY endedAt DESC`) and `MatchRepository.observeCompletedMatches()`/`getMatch(id)`
already exist and already return fully assembled `Match` domain objects (game mode, players with
final `remainingScore`/`marks`/`points`, `winnerId`, `startedAt`/`endedAt`) — everything the spec's
FR-004/FR-005 need. No new DAO query, entity field, or repository method is required.

**Alternatives considered**: A dedicated read-only "history projection" DTO (separate from
`Match`) was considered per the spec's "Match History Entry" key entity, but rejected — `Match` as
returned by `observeCompletedMatches()` already carries every field the list/detail screens need;
introducing a parallel type would be an unjustified abstraction over data that already fits.

## Decision: In-app navigation via a local screen-state enum, not Jetpack Navigation

**Rationale**: `MainActivity` currently switches UI purely on local `remember { mutableStateOf(...) }`
state (see `captureMode`). The full destination graph this feature needs is 4 flat screens
(setup, scoreboard, history list, history detail) with no deep links, no multi-level back stack
beyond "detail → list → setup", and no need to survive process death independent of the
already-Room-backed match/history data. Introducing `androidx.navigation:navigation-compose` for
this shape would add a dependency and a NavHost/NavGraph the app doesn't otherwise use, for no
behavior Compose's own state can't already provide.

**Alternatives considered**: Jetpack Navigation Compose — rejected per above (unjustified
complexity for 4 flat screens); a `Navigator` singleton in `AppContainer` — rejected, `MainActivity`
already owns equivalent UI-only state (`captureMode`) directly, so screen state follows the same
existing pattern rather than introducing a new one.

## Decision: HistoryViewModel follows the existing MatchViewModel pattern

**Rationale**: `MatchViewModel` is a plain `ViewModel` wrapping `MatchRepository` Flows into a
`StateFlow`, constructed via a small `ViewModelProvider.Factory` (`MatchViewModelFactory`), wired
in `MainActivity` via `viewModel(factory = ...)`. `HistoryViewModel` reuses the identical shape
(`StateFlow<List<Match>>` for the list, plus a selected-match-id/detail lookup) for consistency
and because there is no reason to deviate — same module boundary, same testing posture (see plan.md
Technical Context).

**Alternatives considered**: Deriving history state directly in `MainActivity` via
`matchRepository.observeCompletedMatches().collectAsState()` without a ViewModel — rejected,
inconsistent with the existing `MatchViewModel` precedent and would leak repository wiring into
the Activity beyond what `AppContainer`/factory pattern already establishes.
