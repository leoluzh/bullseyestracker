# Implementation Plan: Match History & Resume

**Branch**: `005-match-history` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-match-history/spec.md`

## Summary

Add a match history list/detail UI (spec FR-004–FR-007, User Story 2) reachable from the start
screen, backed entirely by `MatchRepository.observeCompletedMatches()`/`getMatch()` — both of
which already exist and require no changes. Code inspection during planning found that
resume-on-launch (spec FR-001–FR-003, User Story 1) is **already fully implemented**:
`MatchViewModel.init` already collects `MatchRepository.observeInProgressMatch()` into its
`match` `StateFlow`, and `MainActivity` already routes to the scoreboard whenever that `StateFlow`
is non-null, to `MatchSetupScreen` otherwise. This plan's US1 work is therefore verification
(quickstart scenario) rather than new code; the net-new implementation is entirely User Story 2
(history screens) plus the minimal in-app navigation needed to reach and leave them.

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged)

**Primary Dependencies**: No new external dependency. `app` module gains a `HistoryViewModel`
(plain `ViewModel` + `StateFlow`/`viewModelScope`, same pattern as `MatchViewModel`) and two
Compose screens (`MatchHistoryScreen`, `MatchHistoryDetailScreen`). Navigation between
setup/scoreboard/history/history-detail is handled with a small in-memory screen-state enum in
`MainActivity`, matching the existing `CaptureMode` state-switch pattern already used there — no
Jetpack Navigation library is introduced, since the screen graph is 4 flat destinations with no
deep-linking or back-stack complexity that would justify one.

**Storage**: Room via existing `MatchRepository` — `observeCompletedMatches()` (already
implemented, `MatchDao` query `WHERE status = 'COMPLETED' ORDER BY endedAt DESC`) and `getMatch(id)`
(already implemented) are reused as-is. No schema, DAO, or entity changes.

**Testing**: No constitution-mandated tests (Principle III applies only to
`ScoreMapper`/`BoardDetector`/`DartDetector`/`X501Rules`/`CricketRules`; this feature touches none
of them). Following this codebase's existing convention (`MatchViewModel`, `MainActivity`, and all
prior screens under `app/` have no unit/instrumented tests — see `app/src/test`,
`app/src/androidTest`, both absent), `HistoryViewModel` and the two new screens are validated via
the `quickstart.md` manual scenario, not new automated tests. Resume-on-launch (US1) is likewise
validated manually per quickstart, since it is pre-existing, untested code this feature does not
modify.

**Target Platform**: Android, minSdk 26 (unchanged)

**Project Type**: Mobile app — no new Gradle module; net-new code lives entirely in the existing
`app` module (`ui/history/`); no changes to `cv` or `match` modules.

**Performance Goals**: N/A — no live-camera/detection pipeline involved; history list/detail are
one-shot Room reads via a cold/observed `Flow`, not on the Principle IV critical path.

**Constraints**: Fully offline/on-device (Principle I — unchanged, Room is local-only, no network
call added); `app` reaches persistence only through `MatchRepository`, never Room types directly
(CLAUDE.md module-boundary rule — `HistoryViewModel` imports only `match.data.MatchRepository`/
`match.model.*`, never `androidx.room.*`).

**Scale/Scope**: One new `HistoryViewModel`, two new Compose screens
(`MatchHistoryScreen`/`MatchHistoryDetailScreen`), one small `MainActivity` navigation-state
addition to reach/leave them from `MatchSetupScreen`. No repository, DAO, or entity changes.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — history/resume are pure local Room reads; no network surface added |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module; `app` never touches Room directly | PASS — this feature touches no CV code at all; `HistoryViewModel` reaches persistence only via `MatchRepository`, matching the existing module-boundary rule |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | N/A — no scoring/rules logic in this feature (list/detail display + pre-existing resume routing only) |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | N/A — no live-frame pipeline touched |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | N/A — no new automatic detection; history is a read-only view of already-committed match results |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/005-match-history/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command) — thin; reuses 001's data model
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
app/
├── src/main/java/com/bullseyestracker/ui/history/
│   ├── HistoryViewModel.kt          # NEW — wraps MatchRepository.observeCompletedMatches()/
│   │                                #   getMatch() as StateFlow<List<Match>> / selected-match state
│   ├── MatchHistoryScreen.kt        # NEW — list of completed matches, most-recent-first, empty state
│   └── MatchHistoryDetailScreen.kt  # NEW — single match's final per-player results + winner
└── src/main/java/com/bullseyestracker/MainActivity.kt   # MODIFIED — adds a screen-state
    # (Setup/History/HistoryDetail/Match) so MatchSetupScreen can navigate into history and back;
    # no change to the existing resume-on-launch routing (already correct, see Summary)

match/                                  # UNTOUCHED — observeCompletedMatches()/getMatch() already exist
cv/                                     # UNTOUCHED
```

**Structure Decision**: Additive to the `app` module only, following the same pattern
003/004-established for new screens (`ui/<feature>/` package, plain `ViewModel` + Compose,
wired into `MainActivity`). No new Gradle module, no new persistence code — `match`'s existing
`MatchRepository` surface already covers everything this feature's UI needs to read.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
