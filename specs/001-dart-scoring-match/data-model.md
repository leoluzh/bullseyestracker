# Phase 1 Data Model: Dart Scoring & Match Tracking

Derived from the Key Entities section of [spec.md](spec.md). Field lists are conceptual
(persistence-layer types are a `match` module implementation detail, not fixed here).

## Match

Represents one game session from start to finish.

| Field | Description |
|---|---|
| id | Unique identifier |
| gameMode | `FIVE_O_ONE` \| `CRICKET` |
| players | Ordered list of `Player` (turn order) |
| currentPlayerIndex | Index into `players` for whose turn it is |
| status | `IN_PROGRESS` \| `COMPLETED` |
| winnerId | Player id, set only when `status == COMPLETED` |
| startedAt / endedAt | Timestamps |

**Validation rules**: `players.size` MUST be 2-4 (spec SC-004, FR-007). `winnerId` MUST be null
while `status == IN_PROGRESS` and MUST reference a player in `players` when set.

**State transitions**: `IN_PROGRESS -> COMPLETED` (one-way, triggered by a game-mode win
condition per FR-010). No `COMPLETED -> IN_PROGRESS` transition (a finished match is immutable;
starting again means creating a new `Match`).

## Player

A participant within one `Match`.

| Field | Description |
|---|---|
| id | Unique identifier (scoped to the match) |
| name | Display name |
| remainingScore | 501 mode only: starts at 501, decremented per turn |
| marks | Cricket mode only: map of number (15-20, bull) -> mark count (0-3+) |
| points | Cricket mode only: accumulated points from marks on numbers closed by an opponent still open for this player |

**Validation rules**: Exactly one of `remainingScore` (501) or `marks`/`points` (Cricket)
is meaningful per match, determined by `Match.gameMode`. `remainingScore` MUST NOT go negative
(overshoot is a bust, handled at `Turn` resolution, not stored as a negative value).

## Turn

One player's set of up to 3 throws within a `Match`.

| Field | Description |
|---|---|
| id | Unique identifier |
| matchId / playerId | Owning match and player |
| throws | Ordered list of `Throw` (1-3 entries) |
| outcome | `NORMAL` \| `BUST` (501) \| `CHECKOUT` (501 win) \| `MATCH_WIN` (Cricket win) |
| sourceFrameId | Reference to the `Detection Frame` the throws were derived from (nullable if fully manual) |

**Validation rules**: `throws.size` MUST be between 1 and 3 (spec Assumptions: turn may end
early on checkout). `outcome` is computed by the active game-mode's rule engine from
`throws` and the player's pre-turn state — never set directly by the UI.

## Throw

A single dart's detected or corrected result.

| Field | Description |
|---|---|
| id | Unique identifier |
| sector | 1-20, or `BULL`, or `MISS` |
| ring | `SINGLE` \| `DOUBLE` \| `TRIPLE` \| `OUTER_BULL` \| `INNER_BULL` \| `MISS` |
| value | Computed point value (e.g., sector 20 + `TRIPLE` = 60; `INNER_BULL` = 50; `MISS` = 0) |
| confidence | Detection confidence score, `null` if fully manual |
| wasManuallyCorrected | Boolean — true if the user edited the auto-detected result (FR-006) |

**Validation rules**: `value` MUST be derivable deterministically from `sector` + `ring` (no
independent storage of a value that could disagree with sector/ring — single source of truth
for the score-mapping logic that constitution Principle III requires be unit-tested).
`confidence` below an app-defined low-confidence threshold MUST surface the low-confidence
indicator required by FR-012, regardless of `wasManuallyCorrected`.

## Detection Frame

The captured photo or live-camera frame a set of throws was derived from; retained for the
overlay/review UI (spec FR-005) and as the `Turn.sourceFrameId` reference.

| Field | Description |
|---|---|
| id | Unique identifier |
| captureMode | `PHOTO` \| `LIVE_CAMERA` |
| imageRef | Local reference to the stored/cached image data |
| capturedAt | Timestamp |
| boardDetected | Boolean — whether `CvEngine` located the board in this frame (edge case: no board found) |

## Relationships

```
Match 1───* Player
Match 1───* Turn
Turn   *───1 Player     (each Turn belongs to exactly one Player)
Turn   1───* Throw      (1-3 Throws per Turn)
Turn   *───0..1 Detection Frame   (a Turn may be fully manual, with no source frame)
```
