# Phase 0 Research: App Home Navigation

No NEEDS CLARIFICATION markers remain in the Technical Context (this is a UI-only feature reusing
existing infra). Decisions below record the choices made and why, for anyone picking this up
later.

## Navigation mechanism

- **Decision**: Extend the existing `AppScreen` sealed class + `remember { mutableStateOf(...) }`
  pattern already in `MainActivity.kt` (no navigation library).
- **Rationale**: The codebase already uses this exact pattern for Setup/History/HistoryDetail/
  Stats; introducing Jetpack Navigation Compose for one more layer of screens would be a new
  dependency for no behavioral gain, and conflicts with "don't add abstractions beyond what the
  task requires."
- **Alternatives considered**: Jetpack Navigation Compose (NavHost/NavController) — rejected as
  unnecessary complexity for ~4 additional states in an already-small sealed class.

## Splash screen timing

- **Decision**: A local `LaunchedEffect(Unit) { delay(...) ; advance to next state }` timer
  (~1.2s), gated by nothing external.
- **Rationale**: App is fully offline (constitution Principle I) — there is nothing to wait on
  (no auth check, no remote config). A fixed local delay is the simplest correct implementation.
- **Alternatives considered**: Tap-to-dismiss splash — rejected, spec FR-002 explicitly requires
  automatic dismissal with no user interaction required.

## Test Calibrator screen implementation

- **Decision**: Reuse `CameraController.bindLiveDetection` + `LiveDetectionAnalyzer` exactly as
  `LiveScoringScreen` does, but ignore the `onDetections` callback entirely (no `DetectedThrow`
  accumulation, no "Confirm turn" button, no `MatchViewModel` involvement) and drop the
  `DetectionOverlay`/`CorrectionDialog` UI. Only `onCalibrated`/`onBoardNotFound` drive the
  `BullseyeOverlay` + status text.
- **Rationale**: `LiveDetectionAnalyzer` already separates calibration callbacks from detection
  callbacks (see `app/src/main/java/com/bullseyestracker/camera/LiveDetectionAnalyzer.kt`), so
  "calibration only, no scoring" is achievable by simply not wiring the detection callback to any
  UI state, with zero changes to `cv`/`camera` code. This satisfies FR-008 (no scoring on this
  screen) without new interfaces.
- **Alternatives considered**: A new `CvEngine` method restricted to calibration-only —
  unnecessary; `calibrateBoard` is already a distinct method from `detectThrows`, and
  `LiveDetectionAnalyzer` already calls calibration independently per frame.

## Game-mode list → player-setup handoff

- **Decision**: `MatchSetupScreen` gains a required `gameMode: GameMode` parameter (replacing its
  internal `var gameMode by remember { mutableStateOf(GameMode.FIVE_O_ONE) }` + the
  `SingleChoiceSegmentedButtonRow` picker). The new `GameModeListScreen` is the only place that
  picks a `GameMode`, via `onModeSelected: (GameMode) -> Unit`.
- **Rationale**: Spec FR-006 requires the mode picker to move to the new list screen and not
  appear twice. `GameMode` already exists as a small enum (`FIVE_O_ONE`, `CRICKET`) in
  `match.model`, reused as-is.
- **Alternatives considered**: Keep the picker in `MatchSetupScreen` and have the list screen just
  set an initial value — rejected, spec explicitly says "no mode-switching control shown" on the
  setup screen (FR-006).
