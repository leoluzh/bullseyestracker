# Tasks: Match History & Resume

**Input**: Design documents from `/specs/005-match-history/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: No constitution-mandated tests apply (Principle III covers only
`ScoreMapper`/`BoardDetector`/`DartDetector`/`X501Rules`/`CricketRules`; this feature adds none of
those). Matching the precedent set by every prior UI-only phase (`001`/`002`/`003`/`004` — no
automated Compose UI tests, no `MatchViewModel` unit tests), no test tasks are generated; validation
is the quickstart.md manual walkthrough (Phase "Polish" below).

**Organization**: Tasks are grouped by user story from `spec.md` (US1 resume, US2 history) to
enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

- [X] T001 Create `app/src/main/java/com/bullseyestracker/ui/history/` package directory (holds
      `HistoryViewModel.kt`, `MatchHistoryScreen.kt`, `MatchHistoryDetailScreen.kt`; mirrors
      existing `app/.../ui/match/`, `app/.../ui/detection/` package layout)

## Phase 2: Foundational

No new foundational/blocking work — `Match`/`Player` models and
`MatchRepository.observeCompletedMatches()`/`getMatch()` already exist from
`001-dart-scoring-match` and are reused as-is (data-model.md "Reused entities/repository
surface"). Proceed directly to User Story phases.

---

## Phase 3: User Story 1 - Resume an in-progress match on app launch (Priority: P1)

**Goal**: Reopening the app mid-match lands the player back on that match's scoreboard with exact
persisted state; reopening with no in-progress match lands on the start screen.

**Independent Test**: quickstart.md section 1, steps 1-4 — force-close/reopen mid-501-match,
mid-Cricket-match, with-no-match, and after-a-completed-match; verify routing and state each time.

> Code inspection during `/speckit-plan` (research.md) found this already fully implemented by
> `MatchViewModel.init` (collects `MatchRepository.observeInProgressMatch()`) and `MainActivity`'s
> existing null-check routing — built alongside `003-501-match`/`004-cricket-match` but never
> called out as fulfilling parent-spec FR-011. No code task is needed; this phase is
> verification-only.

- [ ] T002 [US1] Manually run quickstart.md section 1 (steps 1-4) on a device/emulator and confirm
      resume-on-launch holds for 501, Cricket, no-match, and completed-match cases (spec
      Acceptance Scenarios 1-3) — no code change expected, verification only
      PARTIAL: no device/emulator attached in this environment (`adb devices` returns none) to run
      the manual walkthrough. Verified at the code level instead (research.md): `MatchDao.
      observeInProgressMatch()` is `WHERE status = 'IN_PROGRESS' LIMIT 1`, so it structurally
      cannot emit a completed match (covers scenario 4); `MatchViewModel.init` only overwrites
      `_match` when that flow emits non-null (covers scenarios 1-2 for both game modes since
      `assembleMatch` reads all persisted `Player` rows); `_match` starts `null` so a fresh launch
      with nothing in-progress falls through to `MatchSetupScreen` (covers scenario 3). Needs a
      real device/emulator run to close out fully.

**Checkpoint**: US1 confirmed working (pre-existing code); no regression introduced by this
feature so far.

---

## Phase 4: User Story 2 - Browse past match results (Priority: P2)

**Goal**: A player can open a match history list (most-recent-first, mode/players/winner/date),
tap a row to see that match's full per-player final results, and navigate back — reachable from
the start screen.

**Independent Test**: quickstart.md section 2, steps 1-5 — empty state, populated list ordering,
501 detail, Cricket detail, back navigation.

- [X] T003 [P] [US2] Implement `HistoryViewModel` — exposes `completedMatches: StateFlow<List<Match>>`
      sourced from `MatchRepository.observeCompletedMatches()`, and `selectedMatch: StateFlow<Match?>`
      set via a `selectMatch(matchId: String)` function calling `MatchRepository.getMatch(matchId)`,
      cleared via `clearSelection()` — in
      `app/src/main/java/com/bullseyestracker/ui/history/HistoryViewModel.kt` (depends on T001)
- [X] T004 [P] [US2] Build `MatchHistoryScreen` — renders `HistoryViewModel.completedMatches` as a
      scrollable list (game mode, player names, winner, completion date per row, most-recent-first
      order already guaranteed by the repository query), shows an empty-state message when the list
      is empty (spec Acceptance Scenario 2), calls `onMatchSelected(matchId)` on row tap — in
      `app/src/main/java/com/bullseyestracker/ui/history/MatchHistoryScreen.kt` (depends on T001)
- [X] T005 [P] [US2] Build `MatchHistoryDetailScreen` — renders a single `Match`'s every player
      final result (501 `remainingScore` or Cricket `marks`/`points`, matching the display logic
      already used by `FiveOOneScoreboardScreen`/`CricketScoreboardScreen`) and the recorded winner
      (spec Acceptance Scenario 3), calls `onBack()` on back navigation — in
      `app/src/main/java/com/bullseyestracker/ui/history/MatchHistoryDetailScreen.kt` (depends on
      T001)
- [X] T006 [US2] Wire `MainActivity`: add a screen-state (`Setup` / `Play` / `History` /
      `HistoryDetail`) alongside the existing `captureMode` state, construct `HistoryViewModel` (via
      `AppContainer.matchRepository`, same factory pattern as `MatchViewModelFactory`), add a
      "History" entry point on `MatchSetupScreen`'s state branch, route `History` →
      `MatchHistoryScreen` → (on row tap) `HistoryDetail` → `MatchHistoryDetailScreen` → (on back)
      `History` → (on back) `Setup`, without altering the existing resume/in-progress-match routing
      branch — in `app/src/main/java/com/bullseyestracker/MainActivity.kt` (depends on T003, T004,
      T005)

**Checkpoint**: Both user stories functional end-to-end — quickstart.md sections 1-2 all runnable
manually; `make build` (compile + unit tests + lint) passes.

---

## Phase 5: Polish & Cross-Cutting

- [ ] T007 Run quickstart.md sections 1-2 fully (all 9 steps) manually on a device/emulator and
      confirm SC-001 through SC-003 (resume always correct; any past match reachable in under 10s;
      100%/0% history-list correctness) — no code change expected, verification only
      PARTIAL: no device/emulator attached in this environment — same blocker as T002. `make build`
      (compile + unit tests + ktlint + Android lint, all modules) passes clean, confirming the
      feature compiles and lints correctly; the manual on-device walkthrough still needs a real
      device or emulator to close out SC-001/SC-002/SC-003.
- [X] T008 [P] `make lint` / ktlint format check over all new files (`HistoryViewModel.kt`,
      `MatchHistoryScreen.kt`, `MatchHistoryDetailScreen.kt`) and the modified `MainActivity.kt`
      — verified via `make build` (`./gradlew build`), which runs ktlint + Android lint across all
      modules; BUILD SUCCESSFUL, no findings against the new/modified files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: None needed (skipped — see note above)
- **US1 (Phase 3)**: No dependency beyond existing code; can be verified any time, independent of
  Phase 1/US2
- **US2 (Phase 4)**: Depends on Setup (T001) for file location; T003-T005 are independent of each
  other (different files), T006 depends on all three
- **Polish (Phase 5)**: Depends on Phase 4 complete (and benefits from Phase 3's verification
  already having run)

### Critical Path

T001 → {T003, T004, T005} → T006 → T007

T002 (US1 verification) can happen any time, in parallel with all of the above — it exercises
pre-existing code this feature doesn't modify.

## Parallel Execution Examples

- T003, T004, T005 (different files, `HistoryViewModel`/`MatchHistoryScreen`/
  `MatchHistoryDetailScreen`) can be built in parallel once T001 lands.
- T002 (US1 manual verification) can run in parallel with all of Phase 4 — no shared files, no
  logic dependency.
- T008 (lint) can run in parallel with T007 (manual verification) once Phase 4 lands.

## Implementation Strategy

**MVP first**: Phase 3 (T002) costs nothing to "ship" — it's confirmation that a
parent-spec-mandated requirement (FR-011) already works, worth doing first since a regression
there would be the highest-severity finding possible for this feature.

**Incremental delivery**: Phase 4 (US2) is the feature's actual deliverable — T003-T005 can be
built and reviewed in parallel (three independent files with no cross-calls), then T006 wires
them together into a single navigable flow. Land Phase 4 as one PR after Phase 1 is in place.
