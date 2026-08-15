# Feature Specification: Match History & Resume

**Feature Branch**: `005-match-history`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Match history and resume: add a match history list/detail screen (FR-014) showing past completed matches with per-player results, and wire resume-on-launch (FR-011) so the app observes MatchRepository on startup and restores an in-progress match if one exists. Covers tasks T039/T040 from specs/001-dart-scoring-match/tasks.md polish phase."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Resume an in-progress match on app launch (Priority: P1)

A player is mid-match (501 or Cricket), closes the app (backgrounds it, force-quits it, or the
phone restarts), then reopens it later. Instead of landing on a blank start screen, the app
takes them straight back to the scoreboard for the match they were playing, with all scores,
marks, and whose-turn-it-is state exactly as it was.

**Why this priority**: FR-011/SC-005 from the parent spec are already committed requirements
that no code currently fulfills — a mid-match app close today loses the player's place. This is
the single biggest trust gap in an otherwise-complete scoring app, and it is P1 because without
it every other polish item is cosmetic by comparison.

**Independent Test**: Start a match (501 or Cricket), confirm at least one turn, force-close the
app, reopen it, and verify the app opens directly on the scoreboard for that match with the
correct scores/marks/turn — no dependency on the history screen (User Story 2).

**Acceptance Scenarios**:

1. **Given** a 501 or Cricket match is in progress, **When** the player force-closes and reopens
   the app, **Then** the app opens directly on that match's scoreboard with the same remaining
   scores/marks and current-turn indicator as before it was closed.
2. **Given** no match is currently in progress (none started, or the last one completed), **When**
   the app is launched, **Then** the app opens on the normal start/match-setup screen.
3. **Given** a match is in progress, **When** the player finishes it (win condition reached) and
   later reopens the app, **Then** the app opens on the start screen, not the now-completed
   match, since a completed match is no longer "in progress".

---

### User Story 2 - Browse past match results (Priority: P2)

A player wants to see how previous matches went — who played, which mode, final scores, and who
won — after the fact, without needing to remember details of the games.

**Why this priority**: Fulfills FR-014, a stated but unimplemented requirement. It is P2 rather
than P1 because it is a look-back convenience feature; the app is fully usable for live play
without it, whereas Resume (US1) is a data-loss risk. Depends on completed-match records already
being persisted (they are, per existing `MatchRepository`/Room schema).

**Independent Test**: Complete two or more matches (mixing 501 and Cricket), open the match
history screen, and verify both appear with correct mode/players/winner/date, most recent first;
tap one and verify its detail view shows full per-player final results.

**Acceptance Scenarios**:

1. **Given** one or more completed matches exist, **When** the player opens the match history
   screen, **Then** they see a list of past matches ordered most-recent-first, each showing game
   mode, player names, winner, and completion date.
2. **Given** no matches have ever been completed, **When** the player opens the match history
   screen, **Then** they see a message indicating there is no history yet, not a blank/broken
   list.
3. **Given** a match history list is shown, **When** the player taps a specific match, **Then**
   they see that match's detail view with every player's final score/state (e.g., 501 remaining
   or Cricket marks-and-points) and the recorded winner.
4. **Given** the player is on the match history screen, **When** they navigate back, **Then**
   they return to wherever they came from (start screen) without losing that screen's state.

---

### Edge Cases

- What happens when a match is in progress but its data is incomplete/corrupt (e.g., a partially
  written turn from a crash mid-save)? The app MUST fall back to the start screen rather than
  crash or show a broken scoreboard.
- What happens when a match ends in a state with no clear single winner (e.g., app force-quit
  before a win condition was recorded)? It MUST NOT appear in history as "in progress" forever —
  see Assumptions for how completion state is determined.
- What happens when the player deletes/uninstalls-then-reinstalls the app? History and in-progress
  state are local-only (per FR-011/FR-014 in the parent spec) and are expected to be lost; this is
  out of scope to solve here.
- What happens when there are a very large number of completed matches (hundreds)? The history
  list MUST remain scrollable and responsive; exact pagination behavior is an implementation
  detail, not a user-facing requirement change.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: On every app launch, the system MUST check for a persisted match whose status is
  in-progress and, if one exists, navigate directly to that match's scoreboard instead of the
  start screen.
- **FR-002**: If no in-progress match exists at launch, the system MUST show the normal start/
  match-setup screen.
- **FR-003**: The resumed scoreboard MUST reflect the exact persisted state (per-player scores or
  marks, current turn/player, turn history) with no loss of data compared to how the match stood
  when the app was last closed.
- **FR-004**: The system MUST provide a match history screen listing all completed matches,
  ordered most-recently-completed first, showing at minimum: game mode, player names, winner, and
  completion date.
- **FR-005**: The system MUST provide a match detail view, reachable from the history list, showing
  each player's final result for that match (final 501 remaining score or Cricket marks/points as
  applicable) and the declared winner.
- **FR-006**: The system MUST show an empty-state message on the history screen when no matches
  have been completed yet, rather than an empty or broken list.
- **FR-007**: The system MUST make the match history screen reachable from the start screen.

### Key Entities

- **Match**: Existing entity (parent spec) — this feature reads its `status` (in-progress vs.
  completed) to decide launch-time routing and history-list membership; no new fields are
  introduced.
- **Match History Entry**: A read-only projection of a completed `Match` for list display (mode,
  players, winner, date) — not a new persisted entity, derived from existing `Match`/`Player`/
  `Turn` data.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of app launches with a genuinely in-progress match land the player on that
  match's scoreboard, with zero score/marks/turn discrepancy versus the state before the app was
  closed (fulfills parent-spec SC-005).
- **SC-002**: A player can locate and open the detail view of any specific past match (out of at
  least 20 completed matches) in under 10 seconds from app launch.
- **SC-003**: 100% of completed matches appear in the history list; 0% of in-progress or abandoned
  matches appear in the completed-history list.

## Assumptions

- A match is considered "in-progress" (resumable) exactly when its persisted `status` is not
  `completed` — i.e., the existing `Match.status` field (parent spec's Key Entities) is the
  source of truth; no new status values are introduced by this feature.
- Only one match can be in-progress at a time (consistent with the parent spec's single-active-
  match assumption); if that ever changes, resume behavior would need to pick the most recent one.
- Match history is unbounded in this feature (no auto-deletion/archiving of old matches); pruning
  old history is out of scope unless requested later.
- The match history screen is a new top-level destination reachable from the start screen; it is
  not itself a game mode and has no editing capability (history is read-only).
