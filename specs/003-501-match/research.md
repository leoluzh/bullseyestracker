# Phase 0 Research: 501 Match Scoring

No `NEEDS CLARIFICATION` markers remain in the Technical Context — this feature reuses
001-dart-scoring-match's stack (Kotlin/Compose/Room) and adds no new dependency. The items below
are the design decisions worth recording, not open unknowns.

## Decision: Rule engine is a pure function, not a class with state

**Decision**: `X501Rules.resolveTurn(remainingScoreBefore: Int, throws: List<Throw>): TurnResult`
is a stateless top-level function (object singleton), taking the pre-turn score and the turn's
throws, returning the new score + outcome + the subset of throws actually applied.

**Rationale**: Matches how `X501RulesTest.kt` was already written (test-first, per Principle
III) — every test calls `X501Rules.resolveTurn(...)` with explicit before-state and asserts
explicit after-state, no mutable engine instance. Keeps the rule logic trivially unit-testable on
plain JVM (`match/src/test/`, no Android runtime) and reusable identically from both live-camera
and photo-sourced turns (spec FR-009), since it has no dependency on how the throws were
captured.

**Alternatives considered**: A `X501Rules` instance holding running match state was rejected —
`Match`/`Player` already hold that state (Room-backed via `MatchRepository`), so a second
stateful copy would just be a synchronization hazard.

## Decision: Turn resolution stops applying darts as soon as outcome is determined

**Decision**: `resolveTurn` iterates throws in order, applying each to a running total; the
moment a dart would overshoot/bust or checkout, it stops and returns immediately —
`TurnResult.throwsUsed` may be shorter than the input `throws` list (spec Edge Cases, FR-007).

**Rationale**: Directly required by `X501RulesTest`'s "resolves early on checkout"/"resolves
early on bust" cases. Also matches real 501 play — darts thrown after a bust/checkout in the same
physical turn don't count.

**Alternatives considered**: Summing all throws first then checking outcome was rejected — it
can't distinguish "busted on dart 2, dart 3 irrelevant" from "busted on dart 3", which matters
for `throwsUsed` (used for persisting only the darts that actually counted, via
`MatchRepository.saveTurn`'s `turn.throws`).

## Decision: `TurnOutcome.CHECKOUT` (not `MATCH_WIN`) is what `X501Rules` returns

**Decision**: The rules engine returns `TurnOutcome.CHECKOUT` for a winning turn;
`TurnOutcome.MATCH_WIN` (already defined in `match.model.Turn`, unused by any test) is not
produced by `X501Rules` and is left for a possible future Cricket use, or removed if it proves
genuinely dead — not this feature's call to make since it's shared enum surface.

**Rationale**: `X501RulesTest`'s checkout tests (`double-out`, `inner-bull`) both assert
`TurnOutcome.CHECKOUT`, not `MATCH_WIN`. `MatchViewModel` (app layer) is what turns a `CHECKOUT`
outcome into ending the match (`Match.status = COMPLETED`, `winnerId` set) by calling
`MatchRepository.saveTurn(..., winnerId = player.id)` — the rules engine itself doesn't touch
`Match`, only a single player's score.

**Alternatives considered**: Having `X501Rules` return `MATCH_WIN` directly was rejected — it
would require the rules engine to know about `Match`-level concerns (whether the game mode
tracks a single winner) that are properly `MatchViewModel`'s job, and it would contradict the
already-written test expectations.

## Decision: `MatchViewModel` bridges `cv.DetectedThrow` → `match.Throw`

**Decision**: `MainActivity`'s existing `onTurnConfirmed: (List<DetectedThrow>) -> Unit` callback
is wired to `MatchViewModel.confirmTurn(detectedThrows: List<DetectedThrow>)`, which maps each
`DetectedThrow` (cv module: `sectorNumber`, `Ring`, `value`, `confidence`, board position) to a
`match.Throw` (id, sectorNumber, `ThrowRing`, value, confidence, `wasManuallyCorrected = false`),
then calls `X501Rules.resolveTurn`.

**Rationale**: `cv.Ring` and `match.ThrowRing` are deliberately separate enums (module isolation,
Principle II) — `cv` must not depend on `match` or vice versa. A small mapping function is the
established pattern (mirrors `match.data.Mappers.kt`'s entity↔domain mapping already in the
codebase) rather than merging the two enums.

**Alternatives considered**: Unifying `Ring`/`ThrowRing` into one shared enum in a common module
was rejected as out of scope — it would touch `cv`'s public contract
(`specs/001-dart-scoring-match/contracts/cv-engine-contract.md`) for a feature that doesn't need
CV changes at all.
