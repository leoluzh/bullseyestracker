# Feature Specification: Dart Scoring & Match Tracking

**Feature Branch**: `001-dart-scoring-match`

**Created**: 2026-08-12

**Status**: Draft

**Input**: User description: "App Android para contagem automática de dardos em um alvo usando visão computacional. Captura tanto por foto única quanto por câmera ao vivo. Pontuação completa (setor, anel, duplo, triplo, bullseye) detectada automaticamente a partir da imagem. Suporta partidas com múltiplos jogadores e modos de jogo (501, Cricket), usando a contagem automática para atualizar o placar."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Auto-score a single throw via live camera (Priority: P1)

A player points their phone's camera at the dartboard during their turn. As each dart lands,
the app detects it, overlays the detected position on the live camera view, and shows the
resulting score (sector, ring, multiplier) without the player touching the screen.

**Why this priority**: This is the core value proposition — automatic, hands-free scoring is
what differentiates the app from a manual counter. Without this, there is no product.

**Independent Test**: Point the camera at a physical dartboard with darts already thrown,
verify the app detects each dart and displays the correct sector/ring/score overlay, and can
be demoed standalone even without match/player features.

**Acceptance Scenarios**:

1. **Given** the live camera view is pointed at a dartboard with no darts thrown yet, **When**
   a dart is thrown and sticks in the board, **Then** the app detects the new dart within the
   performance budget and displays its score overlaid on the camera image.
2. **Given** three darts are already detected and scored in the current turn, **When** the
   player confirms the turn, **Then** the app records the sum of the three detected scores
   against the current player.
3. **Given** a detected dart position is ambiguous or wrong (e.g., occluded by another dart),
   **When** the player taps the overlaid detection, **Then** the app lets them manually correct
   the sector/ring/multiplier before the score is committed.

---

### User Story 2 - Auto-score from a single photo (Priority: P1)

A player who prefers not to keep the camera open during the whole game instead takes one photo
of the board after their 3 darts are thrown. The app processes the photo and returns the score
for all darts found in the image.

**Why this priority**: Equally core to the product per the stated scope (photo AND live
camera); some users will prefer not to run live camera continuously (battery, stability,
setup). Independently valuable and independently testable from live camera.

**Independent Test**: Feed the app a single photo of a dartboard with a known number of darts
at known positions and verify it returns the correct count and per-dart score, with no live
camera session involved.

**Acceptance Scenarios**:

1. **Given** the player is on the photo-capture screen, **When** they take a photo of the board
   with darts in it, **Then** the app detects every dart in the image and shows their scores
   overlaid on the captured photo.
2. **Given** the app returns a detected score list for a photo, **When** the player reviews it,
   **Then** they can correct any dart's detected sector/ring/multiplier before confirming, the
   same as in the live-camera flow.

---

### User Story 3 - Play a scored match (501) with multiple players (Priority: P2)

A group of players sets up a 501 match, adds player names, and takes turns. After each
player's 3 darts are auto-scored (via photo or live camera), the app subtracts the turn total
from that player's remaining score, following standard 501 rules (bust on overshoot/exact-zero
requiring a double-out, checkout detection), and advances to the next player.

**Why this priority**: Turns the auto-scoring capability into a complete game experience,
which is the stated scope beyond a bare counter. Depends on User Story 1 and/or 2 for the
per-turn score input.

**Independent Test**: Start a 501 match with 2+ named players, manually confirm turn scores
(reusing US1/US2 detection), and verify the running score, bust handling, and win detection
match standard 501 rules — testable end-to-end without needing every edge case of detection
accuracy solved.

**Acceptance Scenarios**:

1. **Given** a new 501 match with 2 players, **When** the match starts, **Then** both players
   begin at 501 and the app indicates whose turn it is.
2. **Given** a player has 40 points remaining, **When** their turn total would take them below
   zero or to exactly 1, **Then** the app marks the turn a "bust", reverts their score to the
   value before the turn, and advances to the next player.
3. **Given** a player reaches exactly 0 with their final dart being a double, **When** the turn
   completes, **Then** the app declares that player the winner and ends the match.

---

### User Story 4 - Play a scored match (Cricket) with multiple players (Priority: P3)

A group of players sets up a Cricket match. The app tracks each player's marks on numbers
15-20 and bullseye, using auto-scored throws to register marks and closed/points-scored state
per standard Cricket rules.

**Why this priority**: Second game mode explicitly requested; adds breadth after 501 (P2)
proves the match-tracking + auto-scoring integration works. Independently testable once
auto-scoring (US1/US2) exists.

**Independent Test**: Start a Cricket match with 2+ players, confirm turn scores, and verify
marks/closed-number/points state updates per standard Cricket rules, independent of 501 logic.

**Acceptance Scenarios**:

1. **Given** a new Cricket match, **When** a player hits three marks on a number (e.g., three
   throws in the 20 sector across turns), **Then** the app marks that number "closed" for that
   player.
2. **Given** a number is closed by a player but still open for at least one opponent, **When**
   that player hits the number again, **Then** the app adds points equal to the number's value
   for each additional mark, per opponents-still-open Cricket scoring.
3. **Given** all numbers (15-20, bullseye) are closed by a player and they have the highest or
   tied-highest points among players with the number closed, **When** the last number is
   closed, **Then** the app declares that player the winner and ends the match.

---

### Edge Cases

- What happens when the app cannot detect the dartboard itself in the frame/photo (bad angle,
  too far, board not visible)? App MUST tell the user no board was found instead of guessing.
- What happens when a dart bounces out or falls before being scanned? Player must be able to
  manually declare "0 / no dart" for that throw slot.
- What happens when two darts land very close together (adjacent segments) and detection
  merges or splits them? Manual correction (US1/US2 Acceptance Scenario 3) must let the player
  fix both count and per-dart score.
- What happens when lighting is poor or the board is partially in shadow? Detection may fail
  or be low-confidence; the app MUST surface a low-confidence indicator rather than silently
  showing a wrong score.
- What happens when a player pauses mid-match and reopens the app later? The in-progress match
  state (scores, whose turn, marks) MUST be restored.
- What happens when a dart lands outside all scoring regions (miss)? It MUST be scored as 0
  and still counted as one of the 3 thrown darts.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST detect the dartboard within a live camera feed or a captured photo
  and identify its scoring geometry (center, ring boundaries, sector boundaries).
- **FR-002**: System MUST detect individual darts stuck in the board from a live camera feed,
  updating detections as new darts are thrown during a turn.
- **FR-003**: System MUST detect individual darts from a single captured photo.
- **FR-004**: System MUST compute, for each detected dart, the resulting score: sector number
  (1-20), ring (single/double/triple), or special zones (outer bull = 25, inner bull = 50), or
  miss (0).
- **FR-005**: System MUST visually overlay each detected dart's position and computed score on
  the corresponding camera view or photo.
- **FR-006**: Users MUST be able to manually correct any detected dart's sector/ring/score, and
  MUST be able to manually add a dart the system missed or remove one it detected in error,
  before the throw is committed to a match score.
- **FR-007**: System MUST let a user start a match, choose a game mode (501 or Cricket), and
  add 2 or more named players before play begins.
- **FR-008**: System MUST use the (possibly corrected) per-turn detected scores to update each
  player's match state automatically, following the rules of the selected game mode.
- **FR-009**: System MUST track whose turn it is and advance turns automatically once a turn
  (3 darts, or fewer if the player chooses to end early) is confirmed.
- **FR-010**: System MUST detect and enforce game-mode-specific win conditions (501 checkout
  with double-out and bust handling; Cricket closing all numbers with points/marks rules) and
  declare a winner, ending the match.
- **FR-011**: System MUST persist an in-progress match's full state locally so it survives the
  app being closed and reopened.
- **FR-012**: System MUST indicate when dartboard or dart detection confidence is low, rather
  than silently presenting a possibly-wrong automatic score.
- **FR-013**: System MUST function fully without a network connection — detection, scoring,
  and match tracking all work offline.
- **FR-014**: System MUST maintain a history of completed matches (players, mode, final scores,
  winner, date) viewable after the fact.

### Key Entities

- **Match**: A single game session; has a game mode (501/Cricket), an ordered list of players,
  current turn/player index, status (in-progress/completed), winner, and start/end time.
- **Player**: A participant in a match; has a name and mode-specific state (e.g., remaining
  score for 501; marks-per-number and points for Cricket).
- **Turn**: One player's set of up to 3 throws within a match; has an ordered list of detected
  (and possibly corrected) throw results and a computed turn outcome (normal/bust/checkout).
- **Throw**: A single dart's detected or corrected result; has a sector (1-20 or bull), a ring
  (single/double/triple/outer-bull/inner-bull/miss), the resulting point value, a detection
  confidence, and whether it was manually corrected.
- **Detection Frame**: The captured photo or live-camera frame a set of throws was derived
  from, kept for the overlay/review UI.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A player can complete a full turn (3 darts thrown, scored, and confirmed) in
  under 15 seconds using live camera detection under normal lighting, without touching the
  screen except to confirm.
- **SC-002**: Automatic detection correctly identifies sector, ring, and resulting score for at
  least 90% of clearly-visible, non-overlapping darts in normal indoor lighting.
- **SC-003**: 100% of automatically detected scores can be manually corrected by the user
  before being committed to the match, with the correction taking under 10 seconds per dart.
- **SC-004**: Users can set up a new 2-4 player match (choosing mode and adding players) in
  under 30 seconds.
- **SC-005**: A match resumes with fully correct state (scores, turn, marks) 100% of the time
  after the app is closed and reopened mid-match.
- **SC-006**: All scoring and match-tracking functionality works with the device in airplane
  mode / no network connectivity.

## Assumptions

- Target users own a standard, regulation-pattern steel-tip or soft-tip dartboard (standard 20
  sector layout with single/double/triple rings and inner/outer bull); non-standard or
  novelty boards are out of scope for v1.
- The device camera is positioned by the user with a reasonably direct, unobstructed view of
  the full board face (not a strict requirement of a mounted rig, but an angled/partial view
  may reduce detection confidence per FR-012).
- "Photo" and "live camera" both feed the same underlying detection capability described in
  FR-001-FR-006; they differ only in capture UX, not in scoring logic.
- Match history (FR-014) is local-only for v1; sharing/exporting match results is out of scope
  unless requested later.
- Only 501 and Cricket are in scope for v1 game modes; other variants (e.g., Around the Clock)
  are out of scope unless requested later.
- A "turn" is up to 3 darts; a player may confirm early with fewer than 3 (e.g., checkout on
  the 2nd dart in 501).
