# Quickstart: Match History & Resume

Validates spec `005-match-history` end-to-end: app-relaunch resume, and browsing completed-match
history.

## Prerequisites

- `make doctor` passes (java/gradle/adb/ANDROID_HOME sane).
- `HistoryViewModel` + `MatchHistoryScreen`/`MatchHistoryDetailScreen` wired into `MainActivity`
  with a way to reach history from `MatchSetupScreen` (this feature's deliverable).
- No `make test` prerequisite — this feature adds no constitution-mandated rule/scoring tests
  (see plan.md Technical Context); validation here is manual/device-based only.

## 1. Resume-on-launch validation (device/emulator) — spec User Story 1

This behavior is pre-existing (`MatchViewModel.init` + `MainActivity` routing, see research.md);
this section validates it still holds, it does not exercise new code.

1. **Mid-match relaunch — 501**: Start a 501 match with 2 players, confirm at least one turn so
   scores differ from the 501 default, then force-close the app (recent-apps swipe-away, not just
   backgrounding) and reopen it. Expect: app opens directly on the 501 scoreboard with the same
   remaining scores and active-player indicator as before closing (spec Acceptance Scenario 1).
2. **Mid-match relaunch — Cricket**: Repeat step 1 with a Cricket match, confirming at least one
   marked number. Expect: app opens on the Cricket scoreboard with the same marks/points/active
   player.
3. **No in-progress match**: With no match started (fresh install, or after a match has been
   completed and the app fully closed), launch the app. Expect: app opens on `MatchSetupScreen`
   (spec Acceptance Scenario 2).
4. **Completed match does not resume**: Finish a match (reach a checkout/Cricket win), fully
   close the app, reopen it. Expect: app opens on `MatchSetupScreen`, not the finished match's
   scoreboard (spec Acceptance Scenario 3).

## 2. Match history validation (device/emulator) — spec User Story 2

1. **Empty state**: On a fresh install (or after clearing app data), open the match history
   screen from the start screen. Expect: an empty-state message, not a blank or broken list
   (spec Acceptance Scenario 2).
2. **List populated, most-recent-first**: Complete two matches — one 501, one Cricket, in that
   order — then open match history. Expect: both appear, Cricket match (completed second) listed
   above the 501 match, each row showing game mode, player names, winner, and completion date
   (spec Acceptance Scenario 1).
3. **Detail view — 501**: Tap the 501 match's row. Expect: detail view shows every player's final
   remaining score and the recorded winner (spec Acceptance Scenario 3).
4. **Detail view — Cricket**: Go back to the list, tap the Cricket match's row. Expect: detail
   view shows every player's final marks-per-number and points, and the recorded winner.
5. **Back navigation**: From a detail view, navigate back — expect to land on the history list
   (not the start screen); from the history list, navigate back — expect to land on the start
   screen with its own state intact (spec Acceptance Scenario 4).

## Success criteria mapping

- SC-001 (100% of in-progress-match launches resume correctly): section 1, steps 1-2.
- SC-002 (any past match's detail reachable in under 10s): section 2, steps 2-4 (manually time the
  list-open → row-tap → detail-visible sequence).
- SC-003 (100% of completed matches in history; 0% of in-progress/abandoned matches in it):
  section 2 step 2 combined with section 1 steps 3-4 (an in-progress or freshly-relaunched-blank
  state must never produce a history row).
