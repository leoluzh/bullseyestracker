# Feature Specification: Player Statistics

**Feature Branch**: `010-player-stats`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Add a match statistics screen showing win rate per player,
reachable from the start screen. Investigation of the existing data model found there is no
persistent cross-match player identity — Player.id is a fresh UUID generated per match, and the
only stable link between the same real person across different matches is the name they type in
at match setup (MatchSetupScreen). So win-rate aggregation must key on player name (trimmed,
case-insensitive), not player id, across every completed match returned by the existing
MatchRepository.observeCompletedMatches() (already used by spec 005's match history feature) - no
new repository/DAO/schema changes needed, this is purely a new read/aggregation over existing
data."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - See how often each player wins (Priority: P1)

A group of regular players wants to know who's actually winning the most over time — not just
who won the last game, but overall bragging rights. They open a statistics screen and see, for
every name that's ever played a completed match, how many matches they've played, how many
they've won, and their win rate.

**Why this priority**: This is the entire scope of the change — a single, self-contained view
over data that's already being recorded (completed matches). There is no smaller
independently-valuable slice.

**Independent Test**: Complete several matches under a mix of player names (some repeated across
matches, some one-off), open the statistics screen, and verify each name shows the correct
matches-played/wins/win-rate, matching what a manual tally of the completed-match history would
show.

**Acceptance Scenarios**:

1. **Given** two or more completed matches exist, some sharing a player name (e.g., "Alice" played
   and won one match, then played and lost another), **When** the player opens the statistics
   screen, **Then** "Alice" appears once with matches-played = 2, wins = 1, and win rate = 50%.
2. **Given** a player name has appeared in completed matches but never won one, **When** the
   statistics screen is viewed, **Then** that name still appears, showing a 0% win rate — not
   hidden or omitted.
3. **Given** no completed matches exist yet, **When** the player opens the statistics screen,
   **Then** they see a message indicating there's no statistics yet, not a blank or broken screen.
4. **Given** the statistics screen is shown with two or more players, **When** the list is
   displayed, **Then** players are ordered highest win rate first, so the most successful player
   is immediately visible.
5. **Given** the player is on the statistics screen, **When** they navigate back, **Then** they
   return to the start screen.

---

### Edge Cases

- What happens when the same name is typed with different capitalization or extra spaces across
  matches (e.g., "Alice" vs. "alice " vs. " ALICE")? They MUST be treated as the same player for
  statistics purposes (trimmed, case-insensitive comparison — this feature's Input).
- What happens when two different real people happen to use the same name across different
  matches? They are treated as one statistical "player" — this is a known, accepted limitation
  (name is the only identity signal the app has; see Assumptions), not a defect this feature needs
  to solve.
- What happens to a match that's still in progress? It MUST NOT count toward any player's
  matches-played or win totals until it's completed (consistent with spec 005's history scope,
  which also only surfaces completed matches).
- What happens when two or more players are tied on win rate? Order between tied players is not
  significant to the user (e.g., break ties by matches played, most first) — not a
  [NEEDS CLARIFICATION] item, since no reasonable user expectation is violated either way.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a statistics screen listing every player name that has
  appeared in at least one completed match, showing for each: total matches played, total matches
  won, and win rate (wins ÷ matches played).
- **FR-002**: Player identity for statistics purposes MUST be the name entered at match setup,
  compared trimmed and case-insensitive — there is no other persistent player identity in the app
  (Assumptions).
- **FR-003**: Only completed matches MUST count toward a player's statistics; in-progress or
  abandoned matches MUST NOT affect matches-played, wins, or win rate.
- **FR-004**: The statistics screen MUST be reachable from the start screen and MUST support
  navigating back to it.
- **FR-005**: The system MUST show an empty-state message on the statistics screen when no
  completed matches exist yet, rather than a blank or broken list.
- **FR-006**: Players on the statistics screen MUST be ordered by win rate, highest first.
- **FR-007**: A player with zero wins MUST still appear on the statistics screen with a 0% win
  rate, not be hidden.

### Key Entities

- **Player Statistics Entry**: A read-only, computed-on-the-fly summary for one player name —
  matches played, matches won, win rate — derived entirely from existing completed `Match`/
  `Player` records (spec 001's Key Entities); not a new persisted entity, and introduces no new
  fields to `Match` or `Player`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A player can find any specific player's win rate within seconds of opening the app,
  out of at least 10 completed matches spanning at least 5 distinct player names.
- **SC-002**: 100% of player names that appear in at least one completed match show correct,
  verifiable matches-played/wins/win-rate figures (spec Acceptance Scenario 1) — zero omissions,
  zero miscounts.
- **SC-003**: Win-rate figures update to reflect a newly completed match without requiring the
  player to take any action beyond finishing that match (the statistics screen always reflects
  current completed-match history, not a stale snapshot).

## Assumptions

- There is no persistent, cross-match player identity in this app today (parent spec's data
  model: `Player.id` is generated fresh per match). This feature does not add one — it aggregates
  by name as a pragmatic best-effort identity signal, which is a real limitation (Edge Cases) the
  user has already accepted by requesting this feature built on the existing data model.
- Win rate is computed across all game modes combined (501 and Cricket together), not split per
  mode — the simplest, most directly useful "who wins the most" view; a per-mode breakdown is a
  natural future enhancement, not required here.
- This feature is read-only: it introduces no way to rename, merge, or manually correct a
  player's name/identity after the fact. If that's ever needed, it's separate follow-up work.
