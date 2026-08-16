# Phase 1 Data Model: App Home Navigation

No new persisted entities. This feature is navigation/UI-only and reuses existing types.

## Navigation state (in-memory only, `MainActivity.kt`)

Extends the existing `private sealed class AppScreen` with four new states, alongside the
existing `Setup` / `History` / `HistoryDetail` / `Stats`:

- `Splash` — shown once on cold start, replaces the current implicit "start on Setup" behavior.
- `Home` — the new default post-splash state when no match is in progress (replaces `Setup` as
  the landing state; `Setup` is renamed/repurposed to require an incoming `GameMode`, see below).
- `GameModeList` — shown when the player taps "New Game" from `Home`.
- `CalibrationTest` — shown when the player taps "Test Calibrator" from `Home`.

`Setup` becomes `data class Setup(val gameMode: GameMode)` (previously a bare `data object`) so
`MatchSetupScreen` receives its mode from navigation state instead of an internal picker.

## Reused existing types (unchanged)

- `com.bullseyestracker.match.model.GameMode` (`FIVE_O_ONE`, `CRICKET`) — selected on
  `GameModeList`, carried into `Setup`.
- `com.bullseyestracker.cv.BoardCalibration` / `BoardCalibrationResult` — consumed as-is by the
  new `CalibrationTestScreen` via the existing `CvEngine.calibrateBoard` path
  (`LiveDetectionAnalyzer`'s `onCalibrated`/`onBoardNotFound` callbacks).

No changes to `match` module persistence (Room entities/DAOs) or `cv` module types.
