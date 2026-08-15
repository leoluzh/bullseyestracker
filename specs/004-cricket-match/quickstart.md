# Quickstart: Cricket Match Scoring

Validates spec `004-cricket-match` end-to-end: match setup → marks/points → win.

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `CricketRules` implemented (this feature's core deliverable) so `CricketRulesTest` passes.
- `MatchViewModel` dispatches to `CricketRules` on `Match.gameMode == CRICKET`; `MatchSetupScreen`
  offers a Cricket option; `CricketScoreboardScreen` wired into `MainActivity`.

## 1. Rule-logic validation (automated, no device needed)

```bash
make test
```

Expected: `CricketRulesTest` (`match/src/test/java/com/bullseyestracker/match/rules/`) passes —
covers mark accumulation per ring weight (single/double/triple/outer-bull/inner-bull), closing at
3 marks, points scored only while an opponent is still open, no-op when every player has closed a
number, a single dart both closing a number and scoring overflow points, and win detection
(strictly highest points among players with the number closed).

## 2. End-to-end manual validation (device/emulator)

Run the app and walk:

1. **Setup**: Launch app → match setup screen. Choose Cricket, enter 2 player names ("Alice",
   "Bob") → start match. Expect: both players shown with all of 15-20/bull open, 0 points, Alice
   active (spec US1 Acceptance Scenario 1).
2. **Reject too few players**: Restart setup, try starting Cricket with 1 name. Expect: start is
   blocked (spec US1 Acceptance Scenario 2).
3. **Mark accumulation**: With Alice active, confirm a turn with a single dart on triple 20.
   Expect: Alice has 3 marks on 20, shown as closed (spec US2 Acceptance Scenario 1).
4. **No-op dart**: Confirm a turn with a dart outside 15-20/bull (e.g. single 5). Expect: no
   marks change for anyone (spec US2 Acceptance Scenario 4).
5. **Points while opponent open**: With Alice's 20 closed and Bob's 20 still open, confirm a turn
   for Alice with a single dart on 20. Expect: Alice's points increase by 20 (spec US3 Acceptance
   Scenario 1).
6. **No-op once all closed**: Get both players' 20 closed (a few turns), then confirm another dart
   on 20 for either player. Expect: no points change for anyone (spec US3 Acceptance Scenario 3).
7. **Win detection**: Get Alice to close every number except bull while ahead on points among
   players who've closed bull (trivial if she's first), then confirm a turn closing her bull.
   Expect: winner banner, match ends (spec US3 Acceptance Scenario 4).
8. **No win without the points lead**: Reproduce a case where a player closes their last number
   but another player who already has that number closed holds strictly higher points. Expect:
   match continues, no winner declared (spec US3 Acceptance Scenario 5).
9. **Match ends**: After a win, confirm no further turn can be started/confirmed for that match
   (spec Edge Cases — "match already completed").

## Success criteria mapping

- SC-001 (full match playable, setup → winner): steps 1, 3, 5, 7-9.
- SC-002 (marks/points/no-op/win match standard Cricket rules via automated test): step 1
  (`make test`).
- SC-003 (never ends before all numbers closed by the winner with the points lead): steps 7-8.
- SC-004 (identical outcome regardless of capture mode): manually repeat step 3 via both Live
  Camera and Photo capture modes and confirm identical mark/point deltas.
