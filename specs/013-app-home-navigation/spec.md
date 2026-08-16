# Feature Specification: App Home Navigation

**Feature Branch**: `013-app-home-navigation`

**Created**: 2026-08-16

**Status**: Draft

**Input**: User description: "App home navigation: add a splash screen shown briefly at launch, a
main-menu/home screen for feature selection (New Game, Match History, Player Stats, Test
Calibrator), a game-mode list screen (501 / Cricket) that precedes the existing player-setup
screen when starting a new game, and a new standalone 'test the calibrator' screen (live camera,
runs board calibration and draws the existing BullseyeOverlay, shows calibration status text, no
scoring/match created) reachable from the home screen. Match History and Player Stats screens
already exist (specs 005 and 010) and just need to be wired into the new home screen's navigation
instead of a stray button on the old setup screen."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Home screen replaces ad-hoc entry point (Priority: P1)

A player opens the app and, instead of landing straight on a cluttered player-setup form with two
unrelated buttons bolted onto it, sees a single home screen listing the app's features: New Game,
Match History, Player Stats, and Test Calibrator. They pick the one they want.

**Why this priority**: This is the foundational navigation hub every other story in this feature
depends on — without it, none of the other screens have a discoverable entry point.

**Independent Test**: Launch the app (past the splash screen), confirm a home screen appears with
four clearly labeled options, and confirm tapping each one navigates to the correct existing or
new screen.

**Acceptance Scenarios**:

1. **Given** the app has just finished its splash screen, **When** no match is in progress,
   **Then** the home screen is shown with options for New Game, Match History, Player Stats, and
   Test Calibrator.
2. **Given** the home screen is shown, **When** the player taps Match History, **Then** the
   existing match history list screen opens.
3. **Given** the home screen is shown, **When** the player taps Player Stats, **Then** the
   existing player stats screen opens.
4. **Given** the player is on any screen reached from the home screen (Match History, Player
   Stats, Test Calibrator, or the game-mode list), **When** they use that screen's back action,
   **Then** they return to the home screen.

---

### User Story 2 - Splash screen at launch (Priority: P2)

A player launches the app and briefly sees an opening/branding screen before the home screen
appears, giving the app a proper start rather than jumping straight into a form.

**Why this priority**: Purely cosmetic/orienting — valuable for polish but the app is fully
usable without it, so it ranks below the home screen itself.

**Independent Test**: Cold-launch the app and confirm a splash screen is visible immediately,
then automatically gives way to the home screen without requiring user input.

**Acceptance Scenarios**:

1. **Given** the app process is cold-started, **When** `MainActivity` first renders, **Then** the
   splash screen is shown.
2. **Given** the splash screen is showing, **When** a short, fixed duration elapses, **Then** the
   app automatically transitions to the home screen (or camera-permission prompt, if permission
   was not yet granted) without requiring a tap.

---

### User Story 3 - Choose a game mode before player setup (Priority: P1)

A player taps New Game from the home screen and sees a list of the available game modes (501,
Cricket) before being asked for player names, instead of picking the mode via a control embedded
inside the player-setup form.

**Why this priority**: Directly requested and, together with Story 1, replaces the most-used flow
in the app (starting a match), so it carries the same priority as the home screen.

**Independent Test**: From the home screen, tap New Game, confirm a screen listing 501 and
Cricket appears, tap one, and confirm the player-setup screen opens with that mode already
selected (no mode picker shown there anymore).

**Acceptance Scenarios**:

1. **Given** the home screen is shown, **When** the player taps New Game, **Then** a screen listing
   the available game modes (501, Cricket) is shown.
2. **Given** the game-mode list is shown, **When** the player selects a mode, **Then** the
   player-setup screen opens with that mode pre-selected and no mode-switching control shown.
3. **Given** the player is on the game-mode list screen, **When** they use the back action,
   **Then** they return to the home screen.

---

### User Story 4 - Test the calibrator without starting a match (Priority: P2)

A player wants to check that the app can see the dartboard correctly — for example after
repositioning the camera or board — without going through match setup and without any darts being
scored. They tap Test Calibrator from the home screen, point the camera at the board, and see the
same calibration boundary overlay and status messaging used during real scoring, live, with no
match or score created.

**Why this priority**: A genuinely new capability (today calibration is only visible mid-match),
but it's a diagnostic/setup aid rather than a core scoring flow, so it ranks below the home screen
and new-game stories.

**Independent Test**: From the home screen, tap Test Calibrator, point the camera at a dartboard,
and confirm the calibration boundary overlay and status text appear/update live; confirm no match
is created and no throw is scored regardless of what the camera sees; confirm the back action
returns to the home screen leaving no match behind.

**Acceptance Scenarios**:

1. **Given** the home screen is shown, **When** the player taps Test Calibrator, **Then** a
   live-camera screen opens showing calibration status text and no game controls (no turn/score
   UI).
2. **Given** the Test Calibrator screen is open and the camera finds a dartboard, **Then** the
   calibration boundary overlay is drawn over the live camera feed and the status text reflects a
   calibrated state.
3. **Given** the Test Calibrator screen is open and the camera does not find a dartboard, **Then**
   no overlay is drawn and the status text says no board was found.
4. **Given** the Test Calibrator screen is open, **When** the player uses the back action,
   **Then** they return to the home screen and no match/game record was created.

---

### Edge Cases

- A match is already in progress (e.g., the app was backgrounded mid-match and reopened): the
  home screen MUST NOT be shown — the app resumes directly into the in-progress match's scoring
  screen, unchanged from current behavior.
- Camera permission has not been granted: after the splash screen, the existing
  permission-required message takes precedence over the home screen (including when reached via
  Test Calibrator).
- The player backgrounds/foregrounds the app while the splash screen is showing: the splash
  screen does not re-appear on resume, only on a fresh cold start.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST show a splash screen immediately when `MainActivity` first renders on
  a cold start.
- **FR-002**: The splash screen MUST automatically dismiss after a short, fixed duration and
  requires no user interaction to proceed.
- **FR-003**: After the splash screen (and once camera permission is resolved), the app MUST show
  a home screen when no match is in progress, listing exactly four options: New Game, Match
  History, Player Stats, Test Calibrator.
- **FR-004**: Selecting Match History from the home screen MUST open the existing match history
  list screen (spec 005); selecting Player Stats MUST open the existing player stats screen (spec
  010). Neither screen's own behavior changes.
- **FR-005**: Selecting New Game from the home screen MUST show a game-mode list screen offering
  501 and Cricket.
- **FR-006**: Selecting a mode on the game-mode list screen MUST open the player-setup screen with
  that mode already selected; the player-setup screen MUST NOT show its own mode-selection control
  anymore (mode is chosen once, on the list screen).
- **FR-007**: Selecting Test Calibrator from the home screen MUST open a screen that runs live
  board calibration against the camera feed and draws the existing calibration boundary overlay,
  with status text describing whether a board is currently calibrated.
- **FR-008**: The Test Calibrator screen MUST NOT create a match, record a score, or run dart-tip
  detection/scoring — only board calibration and its overlay/status are active.
- **FR-009**: Every screen reachable from the home screen (Match History, Player Stats, Test
  Calibrator, game-mode list) MUST provide a back action that returns to the home screen.
- **FR-010**: If a match is already in progress when the app is opened or resumed, the app MUST
  go straight to that match's scoreboard/scoring screen, bypassing the splash and home screens
  entirely (unchanged from current resume behavior).

### Key Entities

- No new persisted data entities. This feature is purely navigational/UI; it reuses existing
  match, player-stats, and board-calibration data already produced by other features.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: From a cold app launch, a first-time player can identify and reach any of the four
  home-screen features (New Game, Match History, Player Stats, Test Calibrator) without
  instruction, in one tap from the home screen.
- **SC-002**: Starting a new match takes the same or fewer taps than before this feature (home →
  New Game → mode → player setup → start), with the mode chosen on its own dedicated step rather
  than a control embedded in the player-setup form.
- **SC-003**: A player can verify the camera correctly sees the dartboard (calibration overlay
  visible, status says calibrated) without creating or affecting any match record, 100% of the
  time the Test Calibrator screen is used.
- **SC-004**: Every screen introduced by this feature can be exited back to the home screen in
  exactly one back action.

## Assumptions

- The splash screen's fixed duration is short enough not to feel like a delay (target ~1–1.5
  seconds) and does not block on any network or long-running work, since the app is fully
  offline (constitution Principle I).
- "Test Calibrator" reuses the existing live-camera calibration pipeline (`CvEngine.calibrateBoard`
  and the existing calibration boundary overlay from spec 012) rather than introducing any new
  detection logic — this is a UI-only feature per the constitution's Development Workflow section
  (UI-only changes are exempt from the CV-isolation and real-time-budget gates but must not reach
  into CV internals directly).
- The player-setup screen's existing 2–4 player name entry and Start Match behavior are unchanged
  by this feature other than removing its embedded mode picker and accepting the mode as an
  incoming selection.
- No new persistence is required; this feature only adds/reorganizes navigation and one new
  read-only diagnostic screen.
