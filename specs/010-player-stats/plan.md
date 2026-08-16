# Implementation Plan: Player Statistics

**Branch**: `010-player-stats` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/010-player-stats/spec.md`

## Summary

Add a player-statistics screen (win rate per player name, highest first) reachable from the start
screen, following the same shape as `005-match-history`'s history screen. The one piece of real
logic — aggregating `MatchRepository.observeCompletedMatches()`'s output into per-player-name
matches-played/wins/win-rate — is a new pure function, `PlayerStatsCalculator`, placed in `match`
(alongside `X501Rules`/`CricketRules`, since it's domain logic over `Match`/`Player`, not UI
presentation logic) and unit-tested there. No repository/DAO/schema changes (spec Input) — the UI
layer (`app`) only adds a `PlayerStatsViewModel` + `PlayerStatsScreen` + `MainActivity` wiring,
mirroring `HistoryViewModel`/`MatchHistoryScreen`/`MainActivity`'s existing pattern.

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged)

**Primary Dependencies**: No new external dependency. `match` module gains a `stats` package
(`PlayerStatsCalculator`, `PlayerStats`); `app` module gains a `PlayerStatsViewModel` (same
`StateFlow`/`viewModelScope` shape as `HistoryViewModel`) and one Compose screen.

**Storage**: Reuses `MatchRepository.observeCompletedMatches()` as-is (already implemented,
already used by `005-match-history`). No schema, DAO, or entity changes — `PlayerStatsCalculator`
consumes the existing assembled `Match`/`Player` domain objects.

**Testing**: `PlayerStatsCalculatorTest` (plain JVM, `match/src/test/`) — not constitution-Principle-
III-mandated (that principle covers only `ScoreMapper`/`BoardDetector`/`DartDetector`/
`X501Rules`/`CricketRules`), but written following this codebase's established convention for
`match`-module domain logic (`X501RulesTest`, `CricketRulesTest` both exist for the same reason:
it's a pure function worth testing directly, and SC-002 ("zero miscounts") specifically demands
correctness). `PlayerStatsViewModel`/`PlayerStatsScreen` get no new tests, matching the precedent
set by `MatchViewModel`/`HistoryViewModel` and every screen in `app` (no `app/src/test`,
no `app/src/androidTest` exists in this codebase today — UI/wiring layers are validated via
quickstart.md, not automated tests).

**Target Platform**: Android, minSdk 26 (unchanged)

**Project Type**: Mobile app — no new Gradle module; new code in existing `match` (stats
aggregation) and `app` (ViewModel + screen + `MainActivity` wiring) modules.

**Performance Goals**: N/A — `PlayerStatsCalculator.compute` is a pure, synchronous function over
however many completed matches exist locally (bounded by one device's own play history); no
live-pipeline latency budget applies (that's `cv`'s concern, unchanged by this feature).

**Constraints**: Fully offline/on-device (Principle I — unchanged, reads local Room data only);
`app` reaches persistence only through `MatchRepository`, never Room types directly (CLAUDE.md
module-boundary rule — `PlayerStatsViewModel` imports only `match.data.MatchRepository`/
`match.stats.*`, never `androidx.room.*`); CV logic isolated from UI (Principle II — this feature
touches no CV code at all).

**Scale/Scope**: One new pure calculator + model (`match/.../stats/`), one new `ViewModel`, one
new Compose screen (`app/.../ui/stats/`), one small `MainActivity` addition to the existing
`AppScreen` sealed class and its `when` branch (a third destination alongside `Setup`/`History`).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — pure local Room reads + in-memory aggregation; no network surface added |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module; `app` never touches Room directly | PASS — this feature touches no CV code; `PlayerStatsViewModel` reaches persistence only via `MatchRepository`, matching the existing module-boundary rule |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | N/A — `PlayerStatsCalculator` is match-history aggregation, not position→score mapping; tested anyway per this codebase's broader `match`-module convention, but not a Principle III scoring-logic gate |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | N/A — no live-frame pipeline touched |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | N/A — no new automatic detection; statistics are a read-only view of already-committed match results |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/010-player-stats/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
match/
├── src/main/java/com/bullseyestracker/match/stats/
│   └── PlayerStatsCalculator.kt        # NEW — PlayerStats data class + compute(matches) -> List<PlayerStats>
└── src/test/java/com/bullseyestracker/match/stats/
    └── PlayerStatsCalculatorTest.kt    # NEW — grouping/aggregation/ordering/edge-case coverage

app/
├── src/main/java/com/bullseyestracker/ui/stats/
│   ├── PlayerStatsViewModel.kt          # NEW — wraps MatchRepository.observeCompletedMatches()
│   │                                     #   through PlayerStatsCalculator as StateFlow<List<PlayerStats>>
│   └── PlayerStatsScreen.kt             # NEW — list of player stats, empty state, back navigation
└── src/main/java/com/bullseyestracker/MainActivity.kt   # MODIFIED — adds AppScreen.Stats,
    # a "Player stats" entry point alongside the existing "Match history" one, and its when-branch

cv/                                      # UNTOUCHED
```

**Structure Decision**: Additive to `match` (new `stats` package, alongside `rules` — both are
domain logic over `Match`/`Player`, not UI) and `app` (new `ui/stats/` package, following the
identical pattern `ui/history/` already established). No new Gradle module, no persistence
changes — `MatchRepository.observeCompletedMatches()` already covers everything this feature
needs to read.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
