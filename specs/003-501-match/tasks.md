# Tasks: 501 Match Scoring

**Input**: Design documents from `/specs/003-501-match/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: `X501RulesTest.kt` already exists and is failing (written before this task list, per
constitution Principle III / spec.md relationship note). No additional test tasks are generated
for UI screens, matching the precedent set by `001-dart-scoring-match`/`002-photo-scoring` (no
automated Compose UI tests for `LiveScoringScreen`/`PhotoScoringScreen` either — see
002's plan.md Complexity Tracking).

**Organization**: Tasks are grouped by user story from `spec.md` (US1 setup, US2 score/bust, US3
checkout/win) to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: Which user story this task belongs to

## Phase 1: Setup

- [X] T001 Create `match/src/main/java/com/bullseyestracker/match/rules/` package directory
      (holds `X501Rules.kt`; mirrors existing `match/.../model/`, `match/.../data/` package
      layout)
- [X] T002 Create `app/src/main/java/com/bullseyestracker/ui/match/` package directory (holds
      `MatchViewModel.kt`, `MatchSetupScreen.kt`, `FiveOOneScoreboardScreen.kt`)

## Phase 2: Foundational

No new foundational/blocking work — `Match`/`Player`/`Turn`/`Throw` models, Room entities, and
`MatchRepository` (create/saveTurn/observeInProgressMatch) already exist from
`001-dart-scoring-match` and are reused as-is (spec.md Assumptions; data-model.md "Reused
entities"). Proceed directly to User Story phases.

---

## Phase 3: User Story 2 - Score subtraction and bust detection (Priority: P1)

**Goal**: `X501Rules.resolveTurn` correctly subtracts a confirmed turn's total from a player's
remaining score, or marks the turn a bust (overshoot or landing on exactly 1) leaving the score
unchanged.

**Independent Test**: Run `match/src/test/java/com/bullseyestracker/match/rules/X501RulesTest.kt`
directly (`./gradlew :match:testDebugUnitTest --tests X501RulesTest`) — no UI needed.

> Built ahead of US1 (match setup) because it has zero UI dependency and is the highest-risk pure
> logic (constitution Principle III); US1 is still delivered before this feature is usable
> end-to-end (see Dependencies below).

- [X] T003 [US2] Implement `X501Rules.resolveTurn` core loop (running-total subtraction, overshoot
      bust, bust-on-exactly-1, `NORMAL` outcome, early-stop `throwsUsed`) in
      `match/src/main/java/com/bullseyestracker/match/rules/X501Rules.kt` — makes the
      `normal turn subtracts...`, `overshooting...is a bust...`,
      `landing on exactly 1...is a bust...`, and
      `turn resolves early on bust...` cases in `X501RulesTest` pass

**Checkpoint**: `X501RulesTest`'s non-checkout cases pass; `make test` still fails on the
checkout-related cases until Phase 4.

---

## Phase 4: User Story 3 - Checkout and match win detection (Priority: P1)

**Goal**: A turn that brings a player to exactly 0 via a double (or inner bullseye) resolves as
`CHECKOUT`; the same turn via a non-double is a `BUST` instead.

**Independent Test**: Remaining cases in `X501RulesTest` (`reaching exactly 0 on a double...`,
`...on the inner bull...`, `...on a non-double is a bust...`, `turn resolves early on
checkout...`) pass; `make test` is fully green for `X501RulesTest`.

- [X] T004 [US3] Extend `X501Rules.resolveTurn` with the exactly-0 branch — `CHECKOUT` when the
      zeroing dart's `ThrowRing` is `DOUBLE` or `INNER_BULL`, `BUST` otherwise — in
      `match/src/main/java/com/bullseyestracker/match/rules/X501Rules.kt` (depends on T003; same
      file/function, sequential not parallel)

**Checkpoint**: `./gradlew :match:testDebugUnitTest --tests X501RulesTest` fully green. Rule
engine complete and independently verified — Phase 5 only wires it into the app.

---

## Phase 5: User Story 1 - Set up a 501 match with named players (Priority: P1)

**Goal**: A player can start a new 501 match with 2-4 named players, each beginning at 501, with
turn confirmations from either capture mode driving `X501Rules` and updating the live scoreboard.

**Independent Test**: Per quickstart.md steps 1-2 — open match setup, add 2-4 names, start,
verify all players at 501 with the first marked active; verify <2 or >4 names blocks starting.
Steps 3+ (score/bust/checkout through the live UI) exercise this phase's wiring together with
Phases 3-4's rule engine.

- [X] T005 [P] [US1] Build `MatchSetupScreen` — text fields to add 2-4 player names, validation
      disabling "Start match" outside that range, calls `MatchRepository.createMatch(FIVE_O_ONE,
      names)` on start — in `app/src/main/java/com/bullseyestracker/ui/match/MatchSetupScreen.kt`
- [X] T006 [US1] Implement `MatchViewModel`: exposes in-progress `Match` as `StateFlow` (via
      `MatchRepository.observeInProgressMatch()`), `startMatch(names: List<String>)` delegating to
      `MatchRepository.createMatch`, and `confirmTurn(detectedThrows: List<DetectedThrow>)` that
      maps `DetectedThrow` → `match.Throw` (per research.md), calls
      `X501Rules.resolveTurn(activePlayer.remainingScore, throws)`, builds the resulting `Turn`,
      and calls `MatchRepository.saveTurn(...)` with the next player index and `winnerId` (set
      only on `CHECKOUT`) — in
      `app/src/main/java/com/bullseyestracker/ui/match/MatchViewModel.kt` (depends on T004, T005)
- [X] T007 [US1] Build `FiveOOneScoreboardScreen` — lists each player with remaining score,
      highlights the active player, shows a winner banner and blocks further turns once
      `Match.status == COMPLETED` (spec Edge Cases: "match already completed") — in
      `app/src/main/java/com/bullseyestracker/ui/match/FiveOOneScoreboardScreen.kt` (depends on
      T006)
- [X] T008 [US1] Wire `MainActivity`: construct `MatchViewModel` (via `AppContainer.matchRepository`),
      show `MatchSetupScreen` when no match is in progress and `FiveOOneScoreboardScreen` +
      capture screen (`LiveScoringScreen`/`PhotoScoringScreen`) when one is, and replace the
      discarded `onTurnConfirmed` stub with `matchViewModel::confirmTurn` — in
      `app/src/main/java/com/bullseyestracker/MainActivity.kt` (depends on T006, T007)

**Checkpoint**: All three user stories functional end-to-end — quickstart.md steps 1-9 all
runnable manually; `make build` (compile + unit tests + lint) passes.

---

## Phase 6: Polish & Cross-Cutting

- [ ] T009 Run quickstart.md steps 1-9 manually on a device/emulator and confirm SC-001 through
      SC-004 (full match playable; automated rule coverage; no win without double-out; identical
      outcome live-camera vs. photo) — no code change expected, verification only
      PARTIAL: installed on emulator-5554, confirmed MatchSetupScreen renders and "Start match"
      is correctly disabled with blank names (US1 Acceptance Scenario 2); steps 3-9 blocked by
      an AVD system overlay ("Try out your stylus" tutorial) that swallows injected touch input
      and by the pre-existing missing-fixture-photo gap (CLAUDE.md "Known gaps") — needs a real
      device or a clean AVD image to finish
- [X] T010 [P] `make lint` / ktlint format check over all new files
      (`X501Rules.kt`, `MatchViewModel.kt`, `MatchSetupScreen.kt`, `FiveOOneScoreboardScreen.kt`)
      — verified via `make build`, which runs ktlint + Android lint across all modules and passed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: None needed (skipped — see note above)
- **US2 (Phase 3)**: Depends on Setup (T001) only — pure logic, no UI dependency
- **US3 (Phase 4)**: Depends on US2 (T003) — same function, additive branch
- **US1 (Phase 5)**: Depends on US3 (T004, full `X501Rules`) for T006, and on Setup (T002) for
      file locations — this is the phase that makes the feature demoable end-to-end
- **Polish (Phase 6)**: Depends on Phase 5 complete

### Critical Path

T001 → T003 → T004 → T006 → T007 → T008 → T009

T002 and T005 can happen any time before T006/T008 respectively (different files, no logic
dependency).

## Parallel Execution Examples

- T001 and T002 (different directories) can run in parallel.
- T005 (`MatchSetupScreen`, no dependency beyond T002) can be built in parallel with Phase 3-4's
  `X501Rules` work — different files, `MatchSetupScreen` doesn't call `X501Rules` directly.
- T010 (lint) can run in parallel with T009 (manual verification) once Phase 5 lands.

## Implementation Strategy

**MVP first**: Phases 1, 3, 4 (T001, T003, T004) deliver a fully-tested, correct `X501Rules`
engine with zero UI risk — this is the constitutionally-mandated, highest-value slice
(Principle III) and can be merged/reviewed independently of any UI work.

**Incremental delivery**: Phase 5 (US1) is what makes the feature actually playable — it's a
single phase rather than split further because `MatchSetupScreen`/`MatchViewModel`/
`FiveOOneScoreboardScreen`/`MainActivity` wiring only becomes independently testable as a whole
(quickstart.md's manual scenarios all require the full loop: setup → confirm turn → see score
update). Land Phase 5 as one PR after Phases 1-4 are merged and green.
