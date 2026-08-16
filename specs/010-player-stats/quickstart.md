# Quickstart: Player Statistics

Validates spec `010-player-stats` end-to-end: aggregation correctness and the statistics screen.

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `PlayerStatsCalculator` implemented (this feature's core deliverable) so
  `PlayerStatsCalculatorTest` passes.
- `PlayerStatsViewModel` + `PlayerStatsScreen` wired into `MainActivity`, reachable from the start
  screen alongside "Match history".

## 1. Aggregation-logic validation (automated, no device needed)

```bash
make test
```

Expected: `PlayerStatsCalculatorTest` (`match/src/test/java/com/bullseyestracker/match/stats/`)
passes — covers a repeated name across matches (spec Acceptance Scenario 1: 2 played, 1 won, 50%),
a name that's only ever lost (Acceptance Scenario 2: 0% win rate, still present), name matching
that's trimmed/case-insensitive (spec Edge Cases), win-rate-descending ordering with a
matches-played tie-break (spec FR-006, Edge Cases), and an empty input list producing an empty
result (backs Acceptance Scenario 3 at the calculator level).

## 2. End-to-end manual validation (device/emulator)

Run the app (Android Studio or `adb install`) and walk:

1. **Empty state**: On a fresh install (or after clearing app data), open the player-stats screen
   from the start screen. Expect: an empty-state message, not a blank or broken list (spec
   Acceptance Scenario 3).
2. **Repeated name across matches**: Complete two 501 matches with the same two player names,
   winning one and losing the other as "Alice". Open player stats. Expect: "Alice" appears once
   with 2 matches played, 1 win, 50% win rate (spec Acceptance Scenario 1).
3. **Never-won player still shown**: Complete a match where "Bob" loses. Open player stats.
   Expect: "Bob" appears with a 0% win rate, not hidden (spec Acceptance Scenario 2).
4. **Ordering**: With 3+ distinct player names at different win rates, open player stats. Expect:
   highest win rate listed first (spec Acceptance Scenario 4).
5. **Case/whitespace insensitivity**: Play one match as "Charlie" and another as " charlie " (extra
   spaces, different case). Expect: both count toward the same "Charlie" entry, not two separate
   ones (spec Edge Cases).
6. **Combined across game modes**: Play one 501 match and one Cricket match under the same name,
   winning one of each. Expect: a single entry combining both (2 played, 1 win, 50%) — not split
   per mode (spec Assumptions).
7. **In-progress match excluded**: Start a match and leave it in progress (don't finish it). Open
   player stats. Expect: that match's players' figures are unaffected by the in-progress match
   (spec FR-003).
8. **Back navigation**: From the player-stats screen, navigate back. Expect: return to the start
   screen (spec Acceptance Scenario 5).
9. **Live update**: With player stats open in one flow, finish a new match, then return to player
   stats. Expect: the new match's result is already reflected, with no extra action needed beyond
   finishing the match (spec SC-003).

## Success criteria mapping

- SC-001 (find any player's win rate quickly, out of 10+ matches/5+ names): step 4, scaled up.
- SC-002 (100% correct, zero miscounts): step 1 (`make test`) plus steps 2-3, 5-7 manually.
- SC-003 (always current, no stale snapshot): step 9.
