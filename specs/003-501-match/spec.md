# Feature Specification: 501 Match Scoring

**Feature Branch**: `003-501-match`

**Created**: 2026-08-13

**Status**: Draft

**Input**: User description: "Play a scored 501 dart match with 2-4 players (User Story 3 of
specs/001-dart-scoring-match/, tasks T030-T034): after setting up a match with 2-4 named
players, confirmed turns (from either live-camera or photo scoring) drive standard 501 rules —
subtract the turn's total from the active player's remaining score, detect a bust when a turn
would take the score below 0 or leave exactly 1 remaining (since a double is required to finish
and you can never checkout from 1), require the final scoring dart of a turn to be a double (or
the inner bullseye, which counts as double) when it brings the player to exactly 0, and detect
the match win when a player successfully checks out. Turn advances to the next player after
every turn (normal, bust, or checkout)."

**Relationship to 001-dart-scoring-match**: This feature is User Story 3 of the parent
`001-dart-scoring-match` spec (see `specs/001-dart-scoring-match/spec.md` and `tasks.md`
T030-T034), split into its own spec directory/branch (`003-501-match`) as a self-contained
slice of work. It depends on confirmed turn data already produced by User Story 1 (live-camera
scoring) and User Story 2 / `002-photo-scoring` (photo scoring) — it does not detect or score
individual darts itself, only what a confirmed turn does to match state.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Set up a 501 match with named players (Priority: P1)

Before playing, a group of 2-4 players sets up a new 501 match and gives each player a name.

**Why this priority**: Without a way to start a match and register players, there is no match
to score — this is the entry point for every other story in this feature.

**Independent Test**: Open match setup, choose 501, add 2-4 player names, start the match, and
verify each player begins at 501 with the app indicating whose turn it is.

**Acceptance Scenarios**:

1. **Given** the match setup screen, **When** the player chooses 501 mode and adds 2-4 player
   names, **Then** a new match starts with every player at 501 remaining and the first player
   in the list is marked as the active turn.
2. **Given** the match setup screen, **When** the player tries to start with fewer than 2 or
   more than 4 names, **Then** the app prevents starting the match until the player count is
   within range.

---

### User Story 2 - Score subtraction and bust detection (Priority: P1)

As each player's turn is confirmed (from live-camera or photo scoring), their remaining score
is reduced by the turn's total — unless doing so would overshoot zero or leave them on exactly
1, in which case the turn is a bust and their score is unchanged.

**Why this priority**: This is the core rule that makes 501 playable at all; without it,
confirmed turns have no effect on the match.

**Independent Test**: With a match in progress, confirm a turn whose total is less than the
active player's remaining score and verify the score decreases by exactly that amount; then
confirm a turn that would overshoot (or land exactly on 1) and verify the score is unchanged
and the turn is flagged as a bust.

**Acceptance Scenarios**:

1. **Given** a player has 81 remaining, **When** a turn totaling 60 is confirmed, **Then** their
   remaining score becomes 21 and the turn advances to the next player.
2. **Given** a player has 40 remaining, **When** a turn totaling more than 40 is confirmed,
   **Then** the turn is marked a bust, their remaining score stays at 40, and the turn advances
   to the next player.
3. **Given** a player has 3 remaining, **When** a turn leaves them at exactly 1 remaining,
   **Then** the turn is marked a bust (a double can never finish from 1) and their score is
   unchanged.

---

### User Story 3 - Checkout and match win detection (Priority: P1)

When a player's turn brings their remaining score to exactly 0 and the scoring dart that did
so was a double (or the inner bullseye, which counts as a double), that player wins the match
immediately.

**Why this priority**: Winning is the point of playing a match — without checkout detection,
501 has no end state.

**Independent Test**: With a player at a double-out-reachable score (e.g. 40 remaining),
confirm a turn that finishes exactly on a double and verify the app declares that player the
winner and ends the match; separately, confirm a turn that reaches exactly 0 on a non-double
and verify it is a bust instead of a win.

**Acceptance Scenarios**:

1. **Given** a player has 40 remaining, **When** their turn's final scoring dart is a double 20
   (bringing them to exactly 0), **Then** the app declares that player the match winner and
   ends the match.
2. **Given** a player has 50 remaining, **When** their turn's final scoring dart is the inner
   bullseye (bringing them to exactly 0), **Then** the app declares that player the match
   winner (inner bull counts as a double-out).
3. **Given** a player has 20 remaining, **When** their turn's final scoring dart is a single 20
   (bringing them to exactly 0 on a non-double), **Then** the turn is a bust, not a win, and
   their score reverts.

---

### Edge Cases

- **Turn ends early on checkout**: a turn may have fewer than 3 recorded darts if the checkout
  happens on the 1st or 2nd dart — any darts after the checkout are not applied.
- **Turn ends early on bust**: once a dart within a turn causes a bust, any further darts in
  that same turn are not applied to the score (the bust is final for that turn).
- **Match already completed**: no further turns can be confirmed once a match has a winner.
- **Fewer than 2 or more than 4 players at setup**: rejected before the match can start (see
  User Story 1).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST let a player start a new 501 match with 2-4 named players before
  any turn can be scored.
- **FR-002**: The system MUST track whose turn is active and advance to the next player after
  every confirmed turn, regardless of that turn's outcome (normal, bust, or checkout).
- **FR-003**: The system MUST subtract a confirmed turn's total score from the active player's
  remaining score when the result would be greater than 1.
- **FR-004**: The system MUST mark a turn a bust — leaving the active player's remaining score
  unchanged — when applying it would take their score below 0 or to exactly 1.
- **FR-005**: The system MUST mark a turn a bust when it brings the active player's remaining
  score to exactly 0 unless the final scoring dart of that turn was a double or the inner
  bullseye.
- **FR-006**: The system MUST declare the active player the match winner and end the match when
  their turn brings their remaining score to exactly 0 with the final scoring dart being a
  double or the inner bullseye.
- **FR-007**: The system MUST stop applying darts within a turn once that turn's outcome (bust
  or checkout) is determined, ignoring any subsequent darts in the same confirmed turn.
- **FR-008**: The system MUST NOT accept further turn confirmations once a match has ended.
- **FR-009**: The system MUST work identically regardless of whether a turn's throws came from
  live-camera or photo scoring.

### Key Entities

- **501 Match**: A match session in 501 mode; has 2-4 players, an active-turn pointer, and a
  status (in progress / completed with a winner). See `specs/001-dart-scoring-match/data-model.md`
  `Match` entity — this feature does not redefine it.
- **Player Remaining Score**: Each player's points left to reach exactly 0, starting at 501.
- **Turn Outcome**: The result of applying one confirmed turn to the match — normal
  (subtraction applied), bust (score reverted), or checkout (match won). See
  `specs/001-dart-scoring-match/data-model.md` `Turn.outcome`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A full 501 match between 2 players, from setup to a declared winner, can be
  played using only confirmed-turn input, with the running score updating correctly after every
  turn.
- **SC-002**: Every bust and every checkout across a played match matches standard 501 rules
  (verified by automated test against known score/turn combinations, not just manual play).
- **SC-003**: A match never ends without a double-out (or inner-bull) checkout, and never
  continues past a valid checkout.
- **SC-004**: 100% of confirmed turns — from either capture mode — are scored by the same rule
  logic (no divergence in outcome between a live-camera-sourced and a photo-sourced turn with
  identical throws).

## Assumptions

- "Double-out" includes the inner bullseye (50 points) as a valid finish, consistent with
  common 501 house rules; the outer bullseye (25, a single) does not count as a double.
- A turn is 1-3 darts; downstream turn-confirmation (User Stories 1/2 of the parent feature)
  already guarantees a turn's throw list is non-empty and capped at 3 — this feature does not
  re-validate that.
- Match/player/turn persistence (Room-backed `MatchRepository`) already exists from the parent
  feature's foundational work and is reused as-is; this feature only adds the 501-specific rule
  logic and the UI to set up/observe a 501 match.
- Only 2-4 players per match, per the parent spec's FR-007/SC-004 — unchanged here.
