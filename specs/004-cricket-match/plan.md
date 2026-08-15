# Implementation Plan: Cricket Match Scoring

**Branch**: `004-cricket-match` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-cricket-match/spec.md`

## Summary

Add Cricket match play as a second game mode on top of the `Match`/`Player`/`Turn` foundation
`003-501-match` established: a rule engine (`CricketRules`) that turns a confirmed turn's throws
into per-dart mark/point updates (mirroring `X501Rules`'s per-dart resolution, but with no bust
concept), plus a Cricket-specific setup path and scoreboard screen. `MatchViewModel` is extended
to dispatch to `CricketRules` when `Match.gameMode == CRICKET` instead of `X501Rules`, reusing
its existing `DetectedThrow`→`Throw` mapping and `MatchRepository.saveTurn` call unchanged.
`Player.marks`/`Player.points`/`CricketNumber` already exist in `match.model.Player` from
001's foundational work — this feature's only new code is the rule logic and the Cricket UI.

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged from 001/002/003)

**Primary Dependencies**: No new external dependency. `match` module gains `CricketRules` in the
existing `rules` package (alongside `X501Rules`). `app` module gains a Cricket setup screen and
scoreboard screen, plus a small extension to the existing `MatchViewModel` and `MatchSetupScreen`
to select a game mode. Reuses `MatchRepository`, `MatchViewModel`'s `DetectedThrow`→`Throw`
mapping, and the `CricketNumber`/`Player.marks`/`Player.points` model already defined in
`match.model.Player` (001-dart-scoring-match).

**Storage**: Room via existing `MatchRepository` (unchanged schema — `Player.marks` is already
persisted as a `Map<CricketNumber, Int>` via `Converters.kt`, `Player.points` as `Int`; see
`specs/001-dart-scoring-match/data-model.md`).

**Testing**: JUnit on plain JVM — `CricketRulesTest.kt` (`match/src/test/`, written before
`CricketRules` per constitution Principle III) covers mark accumulation per ring
(single/double/triple/outer-bull/inner-bull), closing at 3 marks, points-while-opponent-open,
no-op when every player has closed a number, and win detection (all numbers closed + strictly
highest points among players with that number closed).

**Target Platform**: Android, minSdk 26 (unchanged)

**Project Type**: Mobile app — no new Gradle module; new code in existing `match` (rules) and
`app` (setup screen, scoreboard screen, `MatchViewModel` extension) modules

**Performance Goals**: N/A beyond constitution Principle IV — `CricketRules.resolveTurn` is a
pure, synchronous function over at most 3 throws and up to 4 players' mark state; no live-pipeline
latency budget applies here (owned by `cv`'s detection path, unchanged by this feature)

**Constraints**: Fully offline/on-device (Principle I — unchanged); CV logic isolated from UI
(Principle II — `CricketRules` never imports `org.opencv.*`; `app` still only reaches persistence
through `MatchRepository`); test-first for scoring logic (Principle III — `CricketRulesTest`
predates `CricketRules`); every automatic score user-correctable before commit (Principle V —
inherited from US1/US2, unchanged; Cricket rules only run after a turn is already confirmed)

**Scale/Scope**: One new `rules` unit (`CricketRules`), one `MatchViewModel` dispatch extension,
one game-mode selector added to `MatchSetupScreen`, one new Compose screen
(`CricketScoreboardScreen`), `MainActivity` wiring to route to the right scoreboard by
`Match.gameMode` — the smallest slice that makes `001-dart-scoring-match` User Story 4 playable
end-to-end

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — Cricket rules and match state are pure local computation + Room; no network surface added |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — `CricketRules` lives in `match`, no CV/OpenCV/Room import; `MatchViewModel`/screens (app) reach persistence only via `MatchRepository` |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | PASS — `CricketRulesTest.kt` written first; first implementation task is making it pass |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | N/A — no live-frame pipeline in this feature; `resolveTurn` runs once per confirmed turn |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | PASS — inherited from US1/US2, unchanged; this feature consumes already-confirmed turns |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/004-cricket-match/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command) — thin; reuses 001's data model
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # /speckit-specify output
└── tasks.md              # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
match/
├── src/main/java/com/bullseyestracker/match/rules/
│   └── CricketRules.kt                # NEW — resolveTurn(marks, points, opponents, throws)
│                                       #   -> CricketTurnResult, alongside existing X501Rules.kt
└── src/test/java/com/bullseyestracker/match/rules/
    └── CricketRulesTest.kt            # NEW — written before CricketRules per Principle III

app/
├── src/main/java/com/bullseyestracker/ui/match/
│   ├── MatchSetupScreen.kt            # MODIFIED — add 501/Cricket mode selector, Cricket allows
│   │                                  #   2+ players (no 4-player cap) vs. 501's 2-4
│   ├── MatchViewModel.kt              # MODIFIED — dispatch to CricketRules when
│   │                                  #   Match.gameMode == CRICKET, else X501Rules (unchanged
│   │                                  #   501 path)
│   └── CricketScoreboardScreen.kt     # NEW — marks grid (15-20 + bull) and points per player
└── src/main/java/com/bullseyestracker/MainActivity.kt   # MODIFIED — route to
    # CricketScoreboardScreen vs. FiveOOneScoreboardScreen by Match.gameMode

match/                                  # rules package + reused model/data, otherwise UNTOUCHED
```

**Structure Decision**: No new Gradle module. Follows `003-501-match`'s placement exactly:
rule logic in `match` (alongside `X501Rules`), UI additions in `app`. `MatchViewModel` becomes a
dispatcher across both rule engines by `gameMode`, which is the extension point
`003-501-match`'s plan already anticipated (parent `tasks.md` T037).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
