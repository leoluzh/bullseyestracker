# Feature Specification: Cricket Match Scoring

**Feature Branch**: `004-cricket-match`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Play a scored Cricket dart match with 2+ players (User Story 4 of
specs/001-dart-scoring-match/, tasks T035-T038): after setting up a Cricket match with named
players, confirmed turns (from either live-camera or photo scoring) register marks on numbers
15-20 and the bullseye. Three marks on a number closes it for that player. Once a player closes
a number, further marks on it while at least one opponent still has it open score points equal
to the number's value (bull's outer/inner both count as the bull number, standard Cricket bull
scoring) per additional mark; marks on a number all players already have closed are no-ops. The
match is won when a player has closed all of 15-20 and bull AND has the highest points among
players who have that number closed, at the moment the last number closes. Turn advances to the
next player after every confirmed turn."

**Relationship to 001-dart-scoring-match**: This feature is User Story 4 of the parent
`001-dart-scoring-match` spec (see `specs/001-dart-scoring-match/spec.md` and `tasks.md`
T035-T038), split into its own spec directory/branch (`004-cricket-match`) as a self-contained
slice of work, mirroring how `003-501-match` split out User Story 3. It depends on confirmed
turn data already produced by User Story 1 (live-camera scoring) and User Story 2 /
`002-photo-scoring` (photo scoring) — it does not detect or score individual darts itself, only
what a confirmed turn does to match state. It also reuses the `Match`/`Player`/`Turn`
foundation and `MatchRepository` built for `003-501-match`, following the same pattern as a
second game mode dispatched from `MatchViewModel`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Set up a Cricket match with named players (Priority: P1)

Before playing, a group of 2 or more players sets up a new Cricket match and gives each player a
name.

**Why this priority**: Without a way to start a match and register players, there is no match to
score — this is the entry point for every other story in this feature.

**Independent Test**: Open match setup, choose Cricket, add 2 or more player names, start the
match, and verify each player begins with all numbers (15-20, bull) open and no points, with the
app indicating whose turn it is.

**Acceptance Scenarios**:

1. **Given** the match setup screen, **When** the player chooses Cricket mode and adds 2 or more
   player names, **Then** a new match starts with every player's numbers 15-20 and bull all
   open (0 marks) and 0 points, and the first player in the list is marked as the active turn.
2. **Given** the match setup screen, **When** the player tries to start Cricket with fewer than
   2 names, **Then** the app prevents starting the match until there are at least 2 players.

---

### User Story 2 - Mark accumulation and closing a number (Priority: P1)

As each player's turn is confirmed, darts landing on 15-20 or the bull register marks for that
player on that number — 1 mark for a single hit (or the outer bull), 2 marks for a double hit
(or the inner bull), 3 marks for a triple hit. Once a player accumulates 3 or more marks on a
number, that number is closed for them.

**Why this priority**: This is the core bookkeeping mechanic Cricket depends on; without it,
confirmed turns have no effect on match state.

**Independent Test**: With a match in progress, confirm a turn with darts on 15-20/bull and
verify the active player's mark counts increase correctly per the single/double/triple/bull
weighting, and that a number flips to closed once its mark count reaches 3.

**Acceptance Scenarios**:

1. **Given** a player has 0 marks on 20, **When** a turn is confirmed with a single dart hitting
   triple 20, **Then** the player has 3 marks on 20 and it is closed.
2. **Given** a player has 1 mark on 19, **When** a turn is confirmed with a dart hitting double
   19, **Then** the player has 3 marks on 19 and it is closed.
3. **Given** a player has 2 marks on bull, **When** a turn is confirmed with a dart hitting the
   outer bull, **Then** the player has 3 marks on bull and it is closed.
4. **Given** a dart in a confirmed turn lands outside 15-20 and bull, **When** the turn is
   applied, **Then** that dart registers no marks on any number (a no-op for scoring purposes).

---

### User Story 3 - Score points and detect a match win (Priority: P1)

Once a player has closed a number, any further marks on it — for as long as at least one
opponent still has that number open — add points to that player equal to the number's value
(15-20 at face value, bull at 25) per additional mark. A number already closed by every player
in the match is a pure no-op for further marks (no points, since no opponent still has it open).
The match is won the moment a player closes their last remaining open number (completing all of
15-20 and bull) while holding the strictly highest point total among players who have that
number closed.

**Why this priority**: Points and win detection are what make Cricket a competitive game rather
than a checklist; without them there's no result to a match.

**Independent Test**: With a player who has already closed a number and at least one opponent
still open on it, confirm a turn with another mark on that number and verify points increase by
the number's value; separately, close a player's final remaining number while they hold the
highest points among players with that number closed and verify the app declares them the
winner and ends the match.

**Acceptance Scenarios**:

1. **Given** a player has closed 20 (3+ marks) and at least one opponent has fewer than 3 marks
   on 20, **When** a turn is confirmed with a single dart hitting 20, **Then** the player's
   points increase by 20 and their marks on 20 increase by 1.
2. **Given** a player has closed bull and at least one opponent has bull open, **When** a turn
   is confirmed with a dart hitting the inner bull, **Then** the player's points increase by 50
   (25 per mark × 2 marks) and their marks on bull increase by 2.
3. **Given** every player in the match has already closed 20, **When** a turn is confirmed with
   a dart hitting 20, **Then** no player's points change (no opponent has 20 open).
4. **Given** a player has every number except bull closed and currently holds the highest points
   among players who have bull closed (trivially true if they're the first to close it), **When**
   a turn is confirmed that brings their bull marks to 3, **Then** the app declares that player
   the match winner and ends the match.
5. **Given** a player closes their last remaining number but another player who has already
   closed that same number holds strictly higher points, **When** the turn is confirmed, **Then**
   the match continues (no winner declared) — the player who closed all numbers first without the
   lead does not automatically win.

---

### Edge Cases

- **Turn has fewer than 3 darts**: identical to `003-501-match` — a turn may end with fewer than
  3 recorded darts (e.g., manual correction removed one); every dart present is still applied in
  order.
- **A single dart can both close a number and score points**: e.g., a player with 2 marks throws
  a triple — the first mark closes the number, the remaining 2 marks (if at least one opponent
  is still open on that number at that moment) score points. Marks within one dart's hit are
  applied one at a time, in order, so this transition is evaluated correctly (spec Assumptions).
- **All players close the match's last open number simultaneously**: not possible under this
  feature's turn-based model — turns are confirmed one player at a time, so only one player's
  marks are ever being applied at once; whichever confirmed turn first satisfies the win
  condition ends the match.
- **Match already completed**: no further turns can be confirmed once a match has a winner (same
  rule as `003-501-match` FR-008).
- **Fewer than 2 players at setup**: rejected before the match can start (see User Story 1).
  Unlike 501 (2-4 players), Cricket has no stated upper bound on player count in this feature's
  scope (see Assumptions).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST let a player start a new Cricket match with 2 or more named
  players before any turn can be scored.
- **FR-002**: The system MUST track whose turn is active and advance to the next player after
  every confirmed turn.
- **FR-003**: The system MUST apply a confirmed turn's darts to the active player's mark state
  one dart at a time, in order, so a number's closed/open state can change mid-turn and affect
  how later darts in the same turn are scored (points vs. marks).
- **FR-004**: For darts landing on 15, 16, 17, 18, 19, 20, or the bull, the system MUST add marks
  to the active player's count for that number: 1 mark for a single hit or the outer bull, 2
  marks for a double hit or the inner bull, 3 marks for a triple hit.
- **FR-005**: The system MUST ignore (register zero marks for) any dart that lands outside
  15-20 and the bull.
- **FR-006**: The system MUST close a number for a player once their mark count on it reaches 3
  or more.
- **FR-007**: For each mark applied to a number the active player has already closed, the system
  MUST add points equal to that number's value (15-20 at face value, bull at 25) to the active
  player's total — but only while at least one other player in the match still has that number
  open (fewer than 3 marks).
- **FR-008**: The system MUST NOT award points for marks applied to a number that every player
  in the match has already closed.
- **FR-009**: The system MUST declare the active player the match winner and end the match the
  moment their marks close their last remaining open number (15-20 and bull all closed) while
  their point total is strictly higher than every other player who also has that number closed.
- **FR-010**: The system MUST NOT accept further turn confirmations once a match has ended.
- **FR-011**: The system MUST work identically regardless of whether a turn's throws came from
  live-camera or photo scoring.

### Key Entities

- **Cricket Match**: A match session in Cricket mode; has 2 or more players, an active-turn
  pointer, and a status (in progress / completed with a winner). See
  `specs/001-dart-scoring-match/data-model.md` `Match` entity — this feature does not redefine
  it.
- **Player Marks**: Each player's mark count (0-3+) per number (15-20, bull) and accumulated
  points. See `specs/001-dart-scoring-match/data-model.md` `Player.marks`/`Player.points` —
  already modeled by the parent feature (`CricketNumber` enum, `Player.isNumberClosed`).
- **Turn Outcome**: The result of applying one confirmed turn to the match. Cricket has no
  concept of a bust — every dart either registers marks, scores points, or is a no-op.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A full Cricket match between 2 players, from setup to a declared winner, can be
  played using only confirmed-turn input, with mark and point state updating correctly after
  every turn.
- **SC-002**: Every mark, point award, no-op, and win detection across a played match matches
  standard Cricket rules (verified by automated test against known mark/points/turn
  combinations, not just manual play).
- **SC-003**: A match never ends before every number (15-20, bull) is closed by the winning
  player, and never declares a winner who doesn't hold the strictly highest points among players
  with that number closed at the moment of closing.
- **SC-004**: 100% of confirmed turns — from either capture mode — are scored by the same rule
  logic (no divergence in outcome between a live-camera-sourced and a photo-sourced turn with
  identical throws).

## Assumptions

- Cricket has no stated maximum player count in this feature's scope — unlike 501's 2-4 player
  range (parent spec FR-007/SC-004 is 501-specific), Cricket only requires "2+"; the existing
  `Match` model already supports 2-4 without a Cricket-specific upper bound decision needed
  here, so this feature imposes no additional cap beyond what `Match` already enforces.
- "Bull" is a single trackable number (matching the existing `CricketNumber.BULL` model): the
  outer bull scores 1 mark, the inner bull scores 2 marks, both toward the same "bull" slot —
  standard Cricket house rules, consistent with `003-501-match`'s inner-bull-counts-as-double
  assumption for 501.
- A tie in points when a number closes (two or more players holding the same highest point
  total, including 0-0) means no winner is declared yet — the match continues until one player's
  points are strictly ahead, per the user's own framing ("just require strictly highest for a
  clean win condition").
- A turn is 1-3 darts, per the parent feature's existing turn-confirmation guarantee (same as
  `003-501-match` Assumptions) — this feature does not re-validate that.
- Match/player/turn persistence (Room-backed `MatchRepository`) and the `MatchViewModel`
  dispatch pattern already exist from `003-501-match`'s work and are reused as-is; this feature
  only adds Cricket-specific rule logic and UI to set up/observe a Cricket match.
