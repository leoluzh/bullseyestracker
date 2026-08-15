# Quickstart: 501 Match Scoring

Validates spec `003-501-match` end-to-end: match setup → scored turns → bust/checkout → win.

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `X501Rules` implemented (this feature's core deliverable) so `X501RulesTest` passes.
- `MatchViewModel` + `MatchSetupScreen`/`MatchPlayScreen` wired into `MainActivity` (routes
  `onTurnConfirmed` into the viewmodel instead of discarding it).

## 1. Rule-logic validation (automated, no device needed)

```bash
make test
```

Expected: `X501RulesTest` (`match/src/test/java/com/bullseyestracker/match/rules/`) passes —
covers normal subtraction, overshoot bust, bust-on-1, double-out checkout, inner-bull checkout,
non-double-on-0 bust, and early turn resolution on both checkout and bust.

## 2. End-to-end manual validation (device/emulator)

```bash
make connected-test   # optional — instrumented tests, if any touch match UI
```

Then run the app (Android Studio or `adb install`) and walk:

1. **Setup**: Launch app → match setup screen appears (no in-progress match). Enter 2 player
   names ("Alice", "Bob") → start match. Expect: both players shown at 501, Alice marked active
   (spec US1 Acceptance Scenario 1).
2. **Reject invalid player count**: Restart setup, try starting with 1 name or 5 names. Expect:
   start is blocked (spec US1 Acceptance Scenario 2).
3. **Normal turn**: With Alice active, score/confirm a turn totaling less than her remaining
   score (e.g. three single-20s = 60 via photo mode). Expect: Alice's remaining score drops by
   60, turn passes to Bob (spec US2 Acceptance Scenario 1).
4. **Bust — overshoot**: With a player close to 0 (use several turns to get there, or a short
   manual test match), confirm a turn totaling more than their remaining score. Expect: score
   unchanged, turn still passes to the next player (spec US2 Acceptance Scenario 2).
5. **Bust — leaves exactly 1**: Get a player to 3 remaining, confirm a turn leaving them at 1.
   Expect: bust, score stays at 3 (spec US2 Acceptance Scenario 3).
6. **Checkout — double**: Get a player to 40 remaining, confirm a turn whose final dart is
   double-20. Expect: winner banner, match ends (spec US3 Acceptance Scenario 1).
7. **Checkout — inner bull**: Get a player to 50 remaining, confirm a turn ending on inner bull.
   Expect: same as above — inner bull counts as double-out (spec US3 Acceptance Scenario 2).
8. **Non-double on 0 is a bust, not a win**: Get a player to 20 remaining, confirm a turn ending
   on single-20 (reaches 0 but not via a double). Expect: bust, not a win, score reverts to 20
   (spec US3 Acceptance Scenario 3).
9. **Match ends**: After a checkout, confirm no further turn can be started/confirmed for that
   match (spec Edge Cases — "match already completed").

## Success criteria mapping

- SC-001 (full match playable, setup → winner): steps 1, 3, 6-9.
- SC-002 (bust/checkout matches standard rules via automated test): step 1 (`make test`).
- SC-003 (never ends without double-out, never continues past checkout): steps 6-9.
- SC-004 (identical outcome regardless of capture mode): manually repeat step 3 via both Live
  Camera and Photo capture modes (`CaptureModeSelector`) and confirm identical score deltas.
