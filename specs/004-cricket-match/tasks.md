# Tasks: Cricket Match Scoring

**Input**: Design documents from `/specs/004-cricket-match/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: `CricketRulesTest.kt` does not exist yet (unlike `003-501-match`, where it predated
planning) — this task list writes it first, per constitution Principle III ("test-first for
scoring logic", non-negotiable). No additional test tasks are generated for UI screens, matching
the precedent set by `001`/`002`/`003` (no automated Compose UI tests for capture or match
screens either).

**Organization**: Tasks are grouped by user story from `spec.md` (US1 setup, US2 marks/closing,
US3 points/win) to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

No new package directories needed — `match/.../rules/` and `app/.../ui/match/` already exist
from `003-501-match`. Proceed directly to Foundational.

## Phase 2: Foundational

No new foundational/blocking work — `Match`/`Player`/`Turn`/`Throw`/`CricketNumber` models, Room
entities (including `Player.marks`/`Player.points` persistence via `Converters.kt`), and
`MatchRepository` already exist and are reused as-is (spec.md Assumptions; data-model.md "Reused
entities"). Proceed directly to User Story phases.

---

## Phase 3: User Story 2 - Mark accumulation and closing a number (Priority: P1)

**Goal**: `CricketRules.resolveTurn` correctly turns a turn's darts into mark increments on
15-20/bull (weighted by single/double/triple/outer-bull/inner-bull), closing a number once its
mark count reaches 3, and ignoring darts outside 15-20/bull.

**Independent Test**: Run `match/src/test/java/com/bullseyestracker/match/rules/
CricketRulesTest.kt` directly (`./gradlew :match:testDebugUnitTest --tests CricketRulesTest`) —
no UI needed.

> Built ahead of US1 (match setup) for the same reason `003-501-match` built its rules engine
> first: zero UI dependency, highest-risk pure logic (constitution Principle III). US1 is still
> required before this feature is usable end-to-end (see Dependencies below).

- [X] T001 [P] [US2] Write failing unit tests for mark accumulation in
      `match/src/test/java/com/bullseyestracker/match/rules/CricketRulesTest.kt`: a single dart
      on a number adds 1 mark, a double adds 2, a triple adds 3, outer bull adds 1 mark to BULL,
      inner bull adds 2 marks to BULL; a number closes once marks reach 3 (including a case where
      one dart's marks push a partially-marked number from open to closed); a dart outside
      15-20/bull registers zero marks and changes no state
- [X] T002 [US2] Implement `CricketRules.resolveTurn`'s per-dart mark-application loop (dart →
      target `CricketNumber?` → mark weight from `ThrowRing` → apply marks one at a time, per
      data-model.md's `MarkApplication` algorithm) in
      `match/src/main/java/com/bullseyestracker/match/rules/CricketRules.kt` — makes T001's mark-
      accumulation and closing cases pass (points/win logic still pending, Phase 4)

**Checkpoint**: `CricketRulesTest`'s mark-accumulation cases pass; points/no-op/win cases still
pending until Phase 4.

---

## Phase 4: User Story 3 - Score points and detect a match win (Priority: P1)

**Goal**: Marks applied to an already-closed number score points (per mark, at the number's
value) while at least one opponent still has it open; marks on a number every player has closed
are a pure no-op; the match ends the instant a player closes their last number while holding a
strict points lead among players who also have that number closed.

**Independent Test**: Remaining cases in `CricketRulesTest` (points scored while opponent open,
points on a dart that both closes a number and scores overflow points, no-op once every player
has closed a number, win declared on strictly-higher points, no win without the points lead) all
pass; `make test` is fully green for `CricketRulesTest`.

- [X] T003 [US3] Extend `CricketRulesTest.kt` with points-while-opponent-open, both-close-and-
      score-in-one-dart, universal-closed no-op, win-detection (strict points lead), and no-win
      (tied/behind on points) cases in
      `match/src/test/java/com/bullseyestracker/match/rules/CricketRulesTest.kt` (depends on
      T001 — same file, additive)
- [X] T004 [US3] Extend `CricketRules.resolveTurn` to accept `points: Int` and
      `opponents: List<OpponentState>` (marks + points per opponent), add the already-closed
      branch (score points per mark only when some opponent is still open on that number,
      FR-007/FR-008), and add post-turn win detection (all `CricketNumber`s closed + strictly
      highest points among players who also have that number closed, FR-009) returning
      `CricketTurnResult.isMatchWin` in
      `match/src/main/java/com/bullseyestracker/match/rules/CricketRules.kt` (depends on T002,
      T003; same file, sequential not parallel)

**Checkpoint**: `./gradlew :match:testDebugUnitTest --tests CricketRulesTest` fully green. Rule
engine complete and independently verified — Phase 5 only wires it into the app.

---

## Phase 5: User Story 1 - Set up a Cricket match with named players (Priority: P1)

**Goal**: A player can start a new Cricket match with 2+ named players, each beginning with every
number open and 0 points, with turn confirmations from either capture mode driving
`CricketRules` and updating a Cricket-specific scoreboard.

**Independent Test**: Per quickstart.md steps 1-2 — open match setup, choose Cricket, add 2+
names, start, verify all players begin with every number open and 0 points, first player active;
verify starting with fewer than 2 names is blocked. Steps 3+ (marks/points/win through the live
UI) exercise this phase's wiring together with Phases 3-4's rule engine.

- [X] T005 [US1] Add a game-mode selector (501 / Cricket) to `MatchSetupScreen`; Cricket path
      enforces only a 2-player minimum (no 4-player cap beyond what `Match.init` already
      enforces, per research.md), calls `MatchRepository.createMatch(CRICKET, names)` on start —
      in `app/src/main/java/com/bullseyestracker/ui/match/MatchSetupScreen.kt`
- [X] T006 [US1] Extend `MatchViewModel.confirmTurn` to branch on `currentMatch.gameMode`:
      `FIVE_O_ONE` keeps calling `X501Rules.resolveTurn` unchanged; `CRICKET` calls
      `CricketRules.resolveTurn` with the active player's `marks`/`points` and a snapshot of
      every other player's `marks`, applies the resulting `newMarks`/`newPoints` to the active
      player, and sets `winnerId` when `isMatchWin` is true — in
      `app/src/main/java/com/bullseyestracker/ui/match/MatchViewModel.kt` (depends on T004, T005)
- [X] T007 [US1] Build `CricketScoreboardScreen` — a marks grid (15-20 + bull columns) per player
      showing 0-3 mark state (e.g. open/single-mark/double-mark/closed) and each player's points,
      highlights the active player, shows a winner banner and blocks further turns once
      `Match.status == COMPLETED` (spec Edge Cases: "match already completed") — in
      `app/src/main/java/com/bullseyestracker/ui/match/CricketScoreboardScreen.kt` (depends on
      T006)
- [X] T008 [US1] Wire `MainActivity` to route to `CricketScoreboardScreen` vs.
      `FiveOOneScoreboardScreen` by `match.gameMode` (both already reachable from the same
      in-progress-match state introduced in `003-501-match`) — in
      `app/src/main/java/com/bullseyestracker/MainActivity.kt` (depends on T007)

**Checkpoint**: All three user stories functional end-to-end — quickstart.md steps 1-9 all
runnable manually; `make build` (compile + unit tests + lint) passes.

---

## Phase 6: Polish & Cross-Cutting

- [ ] T009 Run quickstart.md steps 1-9 manually on a device/emulator and confirm SC-001 through
      SC-004 (full match playable; automated rule coverage; no premature/incorrect win; identical
      outcome live-camera vs. photo) — no code change expected, verification only
      PARTIAL: installed on emulator-5554, confirmed the 501/Cricket mode selector renders and
      switches the setup screen's title correctly ("New 501 match" / "New Cricket match", US1
      Acceptance Scenario 1 partial); steps 3-9 blocked by the same AVD "Try out your stylus"
      system overlay hit during 003-501-match's T009 (swallows injected touch input) — needs a
      real device or a clean AVD image to finish, same as 003's outstanding item
- [X] T010 [P] `make lint` / ktlint format check over all new/modified files
      (`CricketRules.kt`, `MatchSetupScreen.kt`, `MatchViewModel.kt`, `CricketScoreboardScreen.kt`,
      `MainActivity.kt`) — verified via `make build`, which runs ktlint + Android lint across all
      modules and passed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: None (skipped — directories already exist)
- **Foundational (Phase 2)**: None needed (skipped — see note above)
- **US2 (Phase 3)**: No dependencies — pure logic, no UI dependency
- **US3 (Phase 4)**: Depends on US2 (T001-T002) — same function, additive branch
- **US1 (Phase 5)**: Depends on US3 (T004, full `CricketRules`) for T006 — this is the phase
      that makes the feature demoable end-to-end
- **Polish (Phase 6)**: Depends on Phase 5 complete

### Critical Path

T001 → T002 → T003 → T004 → T006 → T007 → T008 → T009

T005 can happen any time before T006 (different file, no logic dependency).

## Parallel Execution Examples

- T001 (test-writing) has no dependency and can start immediately.
- T005 (`MatchSetupScreen` mode selector) can be built in parallel with Phase 3-4's
  `CricketRules` work — different file, no direct call into `CricketRules`.
- T010 (lint) can run in parallel with T009 (manual verification) once Phase 5 lands.

## Implementation Strategy

**MVP first**: Phases 3-4 (T001-T004) deliver a fully-tested, correct `CricketRules` engine with
zero UI risk — the constitutionally-mandated, highest-value slice (Principle III), reviewable and
mergeable independently of any UI work, exactly as `003-501-match` did for `X501Rules`.

**Incremental delivery**: Phase 5 (US1) is what makes the feature actually playable — kept as one
phase since `MatchSetupScreen`/`MatchViewModel`/`CricketScoreboardScreen`/`MainActivity` wiring
only becomes independently testable as a whole (quickstart.md's manual scenarios all require the
full loop: setup → confirm turn → see marks/points update). Land Phase 5 as one PR after Phases
3-4 are merged and green.
