# Implementation Plan: 501 Match Scoring

**Branch**: `003-501-match` | **Date**: 2026-08-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-501-match/spec.md`

## Summary

Add 501 match play on top of the existing match/player/turn foundation from
`001-dart-scoring-match`: a rule engine (`X501Rules`) that turns a confirmed turn's throws into a
new remaining score and outcome (normal/bust/checkout), plus a match-setup screen and a
match-play screen (remaining score per player, whose turn it is, winner banner) wired into
`MainActivity` so confirmed turns from either capture mode actually update match state instead
of being discarded. `Match`/`Player`/`Turn`/`MatchRepository` already exist and are reused as-is
(spec Assumptions) — this feature's new work is the 501 rule logic and the `match`-module/`app`
wiring around it.

## Technical Context

**Language/Version**: Kotlin 2.1.21 / JVM 17 (unchanged from 001/002)

**Primary Dependencies**: No new external dependency. `match` module gains a `rules` package
(`X501Rules`); `app` module gains a `MatchViewModel` (plain `ViewModel` +
`StateFlow`/`viewModelScope`, already a transitive Compose dependency — no new library) and two
Compose screens. Reuses `cv.DetectedThrow` → `match.Throw` mapping, `MatchRepository.createMatch`/
`saveTurn`, and the existing `onTurnConfirmed` callback slot already stubbed in `MainActivity`.

**Storage**: Room via existing `MatchRepository` (unchanged schema — `Match`/`Player`/`Turn`/
`Throw` entities from 001 already model everything 501 needs: `Player.remainingScore`,
`Match.currentPlayerIndex`, `Match.status`/`winnerId`, `Turn.outcome`).

**Testing**: JUnit on plain JVM — `X501RulesTest.kt` (`match/src/test/`, already written and
failing per constitution Principle III) covers normal subtraction, overshoot bust, bust-on-1,
double-out checkout, inner-bull checkout, non-double-on-0 bust, and early turn resolution
(checkout/bust ignoring later darts in the same turn).

**Target Platform**: Android, minSdk 26 (unchanged)

**Project Type**: Mobile app — no new Gradle module; new code in existing `match` (rules) and
`app` (ViewModel + screens) modules

**Performance Goals**: N/A beyond constitution Principle IV — `X501Rules.resolveTurn` is a pure,
synchronous function over at most 3 throws; no live-pipeline latency budget applies here (that
budget is owned by `cv`'s detection path, unchanged by this feature)

**Constraints**: Fully offline/on-device (Principle I — unchanged, Room is local-only); CV logic
isolated from UI (Principle II — `X501Rules` and `MatchViewModel` never import `org.opencv.*`;
`app` still only reaches persistence through `MatchRepository`, never Room types directly, per
CLAUDE.md module-boundary rule); test-first for scoring logic (Principle III — `X501RulesTest`
predates `X501Rules`); every automatic score user-correctable before commit (Principle V —
inherited from US1/US2's `CorrectionDialog`, unchanged; 501 rules only run *after* a turn is
already confirmed, so this feature adds no new detection surface needing correction)

**Scale/Scope**: One new `rules` unit (`X501Rules`), one new `MatchViewModel`, two new Compose
screens (match setup, match play/scoreboard), `MainActivity` wiring to route `onTurnConfirmed`
into the viewmodel instead of discarding it — the smallest slice that makes
`001-dart-scoring-match` User Story 3 playable end-to-end

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — 501 rules and match state are pure local computation + Room; no network surface added |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — `X501Rules` lives in `match`, has no CV/OpenCV/Room import; `MatchViewModel`/screens (app) reach persistence only via `MatchRepository`, matching the CLAUDE.md module-boundary rule |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | PASS — `X501RulesTest.kt` already exists and fails (no `X501Rules` yet); this plan's first implementation task is making it pass, not writing it |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | N/A — no live-frame pipeline in this feature; `resolveTurn` runs once per confirmed turn, off the critical detection path |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | PASS — inherited from US1/US2, unchanged; this feature consumes already-confirmed turns and adds no new automatic detection |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/003-501-match/
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
│   └── X501Rules.kt                   # NEW — resolveTurn(remainingScoreBefore, throws) -> TurnResult
└── src/test/java/com/bullseyestracker/match/rules/
    └── X501RulesTest.kt               # ALREADY EXISTS (written before this plan, per Principle III)

app/
├── src/main/java/com/bullseyestracker/ui/match/
│   ├── MatchViewModel.kt              # NEW — owns in-progress Match state, applies X501Rules on
│   │                                  #   confirmed turns, calls MatchRepository.createMatch/saveTurn
│   ├── MatchSetupScreen.kt            # NEW — 2-4 player name entry, start-match button
│   └── FiveOOneScoreboardScreen.kt    # NEW — per-player remaining score, active-turn indicator,
│                                      #   winner banner; wraps LiveScoringScreen/PhotoScoringScreen
└── src/main/java/com/bullseyestracker/MainActivity.kt   # MODIFIED — routes onTurnConfirmed into
    # MatchViewModel instead of discarding it; shows MatchSetupScreen before capture screens when
    # no match is in progress

match/                                  # rules package only; data/model/persistence UNTOUCHED
```

**Structure Decision**: No new Gradle module. Rule logic goes in `match` (alongside the
persistence/model code it operates on, consistent with 001's module boundary — `match` owns game
rules); ViewModel and screens go in `app` (Compose UI layer), matching how 002-photo-scoring
placed its new screen. This is additive to both existing modules, not a new architectural
boundary.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally omitted.
