# Tasks: Player Statistics

**Input**: Design documents from `/specs/010-player-stats/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: `PlayerStatsCalculatorTest` is written before `PlayerStatsCalculator` (T004 before T005)
following this codebase's `match`-module convention (`X501RulesTest`/`CricketRulesTest` both
precede their implementations) — not constitution-Principle-III-mandated (plan.md Constitution
Check), but consistent practice for `match`-module domain logic. `PlayerStatsViewModel`/
`PlayerStatsScreen`/`MainActivity` wiring get no new tests, matching every other screen in `app`
(no `app/src/test`, no `app/src/androidTest` exists in this codebase).

**Organization**: Single user story (spec.md US1) — one-slice feature.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

- [X] T001 [P] Create `match/src/main/java/com/bullseyestracker/match/stats/` package directory
      (holds `PlayerStatsCalculator.kt`; mirrors existing `match/.../rules/` package layout)
- [X] T002 [P] Create `match/src/test/java/com/bullseyestracker/match/stats/` package directory
      (holds `PlayerStatsCalculatorTest.kt`)
- [X] T003 [P] Create `app/src/main/java/com/bullseyestracker/ui/stats/` package directory (holds
      `PlayerStatsViewModel.kt`, `PlayerStatsScreen.kt`; mirrors existing `app/.../ui/history/`
      package layout)

## Phase 2: Foundational

No new foundational/blocking work — `Match`/`Player` models and
`MatchRepository.observeCompletedMatches()` already exist and are reused as-is (data-model.md
"Reused").

---

## Phase 3: User Story 1 - See how often each player wins (Priority: P1)

**Goal**: A statistics screen lists every player name from completed matches with correct
matches-played/wins/win-rate, ordered highest win rate first, reachable from and back to the start
screen.

**Independent Test**: quickstart.md — `make test` for aggregation correctness, then the manual
walkthrough (empty state, repeated-name aggregation, never-won-still-shown, ordering, case/
whitespace insensitivity, combined-across-modes, in-progress-excluded, back nav, live update).

- [X] T004 [P] [US1] Write `PlayerStatsCalculatorTest` — covers: a name repeated across two
      matches (one win, one loss) aggregates to 2 played/1 win/50% (spec Acceptance Scenario 1); a
      name that's only ever lost still appears at 0% (Acceptance Scenario 2); name matching is
      trimmed and case-insensitive (Edge Cases); output is ordered win-rate descending with a
      matches-played-descending tie-break (FR-006, Edge Cases); an empty input list produces an
      empty result — in
      `match/src/test/java/com/bullseyestracker/match/stats/PlayerStatsCalculatorTest.kt` (depends
      on T002; written first per this feature's testing convention, expected to fail until T005)
- [X] T005 [US1] Implement `PlayerStats` data class (`name`, `matchesPlayed`, `matchesWon`,
      computed `winRate`) and `PlayerStatsCalculator.compute(matches: List<Match>): List<PlayerStats>`
      per data-model.md's algorithm (sort by `startedAt`, group by trimmed-lowercased name keeping
      first-seen trimmed display casing, count played/won, sort win-rate desc then matches-played
      desc) — in `match/src/main/java/com/bullseyestracker/match/stats/PlayerStatsCalculator.kt`
      (depends on T001, T004) — makes `PlayerStatsCalculatorTest` pass
- [X] T006 [US1] Implement `PlayerStatsViewModel` — exposes `playerStats: StateFlow<List<PlayerStats>>`
      sourced from `MatchRepository.observeCompletedMatches()` piped through
      `PlayerStatsCalculator.compute` (same `init`-collect shape as `HistoryViewModel`), plus a
      `PlayerStatsViewModelFactory` — in
      `app/src/main/java/com/bullseyestracker/ui/stats/PlayerStatsViewModel.kt` (depends on T005,
      T003)
- [X] T007 [P] [US1] Build `PlayerStatsScreen` — renders `PlayerStatsViewModel.playerStats` as a
      list (name, matches played, wins, win-rate percentage per row), an empty-state message when
      the list is empty (spec FR-005), and a back button calling `onBack()` — in
      `app/src/main/java/com/bullseyestracker/ui/stats/PlayerStatsScreen.kt` (depends on T003)
- [X] T008 [US1] Wire `MainActivity`: extend the `AppScreen` sealed class with `AppScreen.Stats`,
      add a "Player stats" entry point next to the existing "Match history" button on the `Setup`
      branch, construct `PlayerStatsViewModel` (via `AppContainer.matchRepository`, same factory
      pattern as `HistoryViewModelFactory`), and route `Stats` → `PlayerStatsScreen` → (on back) →
      `Setup`, without altering the existing `History`/`HistoryDetail`/resume routing — in
      `app/src/main/java/com/bullseyestracker/MainActivity.kt` (depends on T006, T007)

**Checkpoint**: Feature functional end-to-end — quickstart.md steps 1-9 all runnable manually;
`make build` (compile + unit tests + lint) passes.

---

## Phase 4: Polish & Cross-Cutting

- [ ] T009 Run quickstart.md steps 1-9 manually on a device/emulator and confirm SC-001 through
      SC-003 (any player's win rate findable quickly; 100% correct/zero miscounts; always current,
      no stale snapshot) — no code change expected, verification only
      PARTIAL: no device/emulator attached in this environment (`adb devices` returns none), same
      blocker as specs 005/006. Step 1 (`make test`, aggregation correctness) done — see T004/T005;
      `make build` also confirms the feature compiles cleanly end-to-end. Steps 2-9 (on-device
      screen walkthrough) still need a real device or emulator to close out.
- [X] T010 [P] `make lint` / ktlint format check over all new files (`PlayerStatsCalculator.kt`,
      `PlayerStatsCalculatorTest.kt`, `PlayerStatsViewModel.kt`, `PlayerStatsScreen.kt`) and the
      modified `MainActivity.kt` — verified via `./gradlew build`, which runs ktlint + Android
      lint across all modules; BUILD SUCCESSFUL, no findings against the new/modified files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — T001-T003 are independent (different directories)
- **Foundational (Phase 2)**: None needed (skipped — see note above)
- **US1 (Phase 3)**: Depends on Setup; T004→T005 sequential (same file, test precedes impl); T007
  depends only on T003 (independent of T005/T006's file); T006 depends on T005; T008 depends on
  both T006 and T007
- **Polish (Phase 4)**: Depends on Phase 3 complete

### Critical Path

T001/T002 → T004 → T005 → T006 → T008 → T009

T003 → T007 can happen any time before T008, in parallel with T004-T006's work (different files).

## Parallel Execution Examples

- T001, T002, T003 (different directories) can all run in parallel.
- T007 (`PlayerStatsScreen`, no dependency beyond T003) can be built in parallel with T004-T006's
  calculator/ViewModel work — different files, `PlayerStatsScreen` doesn't call the calculator
  directly.
- T010 (lint) can run in parallel with T009 (manual verification) once Phase 3 lands.

## Implementation Strategy

**MVP first**: Phases 1, and T004-T005 within Phase 3 deliver a fully-tested, correct
`PlayerStatsCalculator` with zero UI risk — mirrors how `003-501-match` sequenced its rule engine
ahead of UI wiring, and can be reviewed independently of any screen work.

**Incremental delivery**: T006-T008 (ViewModel, screen, `MainActivity` wiring) is what makes the
feature actually visible/usable — land the whole of Phase 3 as one PR once T005 is green, since
quickstart.md's manual scenarios all require the full loop (completed match → stats screen →
correct numbers).
