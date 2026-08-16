# Phase 0 Research: Player Statistics

No `NEEDS CLARIFICATION` markers remain — the spec's Assumptions already resolved the one real
judgment call (combined vs. per-mode win rate). The decisions below cover implementation-level
choices made during planning.

## Decision: `PlayerStatsCalculator` lives in `match`, not `app`

**Rationale**: It operates purely on `match.model.Match`/`Player` domain types and computes a
domain-level fact (how often has this player won), the same category of logic as
`X501Rules`/`CricketRules` (also in `match/.../rules/`, also plain Kotlin, also unit-tested in
`match/src/test/`). Keeping it there — rather than inline in a `ViewModel` in `app` — keeps `app`
as thin presentation glue (its existing convention: `MatchViewModel`/`HistoryViewModel` both only
orchestrate `MatchRepository` calls and expose `StateFlow`s, with no non-trivial computation of
their own) and gives the aggregation logic a natural, already-precedented test location.

**Alternatives considered**: Inline aggregation inside `PlayerStatsViewModel` (`app`) — rejected,
breaks the established convention that `app`'s ViewModels don't contain non-trivial computation,
and `app` has no unit-test source set today (plan.md Technical Context), so the logic would ship
untested despite SC-002's explicit zero-miscounts requirement.

## Decision: identity key is `name.trim().lowercase()`; display name is first-seen trimmed original

**Rationale**: Spec FR-002 mandates trimmed, case-insensitive matching for the identity key. For
*display*, showing the first chronologically-seen trimmed spelling (by `Match.startedAt` ascending
across the input match list) is simple, deterministic, and avoids an arbitrary "last write wins"
flicker if display casing were recomputed differently each time new matches complete.

**Alternatives considered**: Displaying the most-recent spelling — rejected, no meaningful
advantage over first-seen and adds a "why did the name change" surprise if someone's casing
varies between matches. Normalizing display to a fixed case (e.g., title-case) — rejected,
overwrites a real person's actual name spelling for no requirement-driven reason.

## Decision: tie-break order is matches-played descending, then insertion order

**Rationale**: Spec Edge Cases explicitly says tie order isn't significant to the user, only that
*some* deterministic order exists (so re-rendering the same data doesn't visibly reshuffle).
Breaking ties by matches-played (most-active players surface first among equals) is a reasonable,
simple secondary sort; remaining ties fall back to the calculator's stable insertion order
(first-seen-by-`startedAt`), which Kotlin's `sortedWith`/`compareBy` guarantees is stable.

**Alternatives considered**: Alphabetical tie-break — equally valid per the spec's own "not
significant" framing; matches-played was chosen only because it surfaces more information (who's
played more) for free, not because alphabetical would be wrong.

## Decision: `PlayerStatsCalculator.compute` trusts its input is already completed-only

**Rationale**: Mirrors `HistoryViewModel`'s existing pattern — `MatchRepository.observeCompletedMatches()`
is already filtered at the DAO level (`WHERE status = 'COMPLETED'`), so re-filtering by
`MatchStatus` inside the calculator would be redundant. The function's doc comment states this
precondition explicitly (documented in data-model.md) rather than silently re-deriving it, so a
future caller passing an unfiltered match list gets a clear contract to follow, not silent
wrong behavior.

**Alternatives considered**: Defensively filtering by `status == COMPLETED` inside `compute` —
rejected as unnecessary defensive coding against a precondition every existing caller already
satisfies (`app`'s own conventions instruct against validating scenarios that can't happen).
