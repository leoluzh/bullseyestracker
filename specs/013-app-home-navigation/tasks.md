---
description: "Task list for App Home Navigation"
---

# Tasks: App Home Navigation

**Input**: Design documents from `/specs/013-app-home-navigation/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: Not requested — this is a UI-only feature with no scoring-logic changes (constitution
Principle III test-first gate does not apply; see plan.md Technical Context). No app-module test
suite exists in this repo today. `quickstart.md` is the manual validation guide instead.

**Organization**: Tasks are grouped by user story per spec.md priorities (US1/US3 = P1, US2/US4 =
P2).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to

## Path Conventions

Single Android app, existing `app`/`cv`/`match` Gradle module layout (see plan.md Project
Structure). All paths below are relative to repo root.

---

## Phase 1: Setup

**Purpose**: none required — no new dependencies, build config, or scaffolding beyond what each
story's own tasks create. Skipped.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Extend the navigation state machine every story hangs off of.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete — every story adds a
branch to the same `AppScreen` sealed class and the same `when (screen)` dispatch in
`MainActivity.kt`.

- [X] T001 In `app/src/main/java/com/bullseyestracker/MainActivity.kt`, extend the private
  `AppScreen` sealed class: add `data object Splash`, `data object Home`,
  `data object GameModeList`, `data object CalibrationTest`; change `data object Setup` to
  `data class Setup(val gameMode: GameMode)` (import `com.bullseyestracker.match.model.GameMode`,
  already imported). Do not wire the `when` branches yet — each story wires its own.

**Checkpoint**: `AppScreen` compiles with all six states; `MainActivity` will not yet compile
end-to-end until Foundational's `Setup` change is consumed by US3 — that's expected, US3 fixes it.

---

## Phase 3: User Story 1 - Home screen replaces ad-hoc entry point (Priority: P1) 🎯 MVP

**Goal**: A home screen listing New Game / Match History / Player Stats / Test Calibrator becomes
the landing screen when no match is in progress, replacing the old Setup-screen-with-buttons.

**Independent Test**: Launch the app, confirm the four options appear, tap Match History and
Player Stats and confirm they open and their back action returns to Home.

### Implementation for User Story 1

- [X] T002 [US1] Create `app/src/main/java/com/bullseyestracker/ui/home/HomeScreen.kt`: a
  `@Composable fun HomeScreen(onNewGame: () -> Unit, onMatchHistory: () -> Unit,
  onPlayerStats: () -> Unit, onTestCalibrator: () -> Unit)` rendering a `Column` with four
  `Button`s labeled "New Game", "Match history", "Player stats", "Test calibrator", each invoking
  its callback.
- [X] T003 [US1] In `app/src/main/java/com/bullseyestracker/MainActivity.kt`: change the initial
  `screen` state from `AppScreen.Setup` to `AppScreen.Home`; add an `AppScreen.Home` branch to the
  `when (screen)` block rendering `HomeScreen` with `onMatchHistory = { screen = AppScreen.History }`,
  `onPlayerStats = { screen = AppScreen.Stats }`, and `onNewGame`/`onTestCalibrator` wired to
  `AppScreen.GameModeList` / `AppScreen.CalibrationTest` (these two branches are added by US3/US4;
  it is fine for this task to reference them since Phase 2 already declared those `AppScreen`
  states).
- [X] T004 [US1] In `app/src/main/java/com/bullseyestracker/MainActivity.kt`, change the
  `onBack` callbacks passed into `MatchHistoryScreen` and `PlayerStatsScreen` from
  `{ screen = AppScreen.Setup }` to `{ screen = AppScreen.Home }`.

**Checkpoint**: Home screen is the landing screen; Match History and Player Stats are reachable
and return to Home. (App will not fully compile until US3 resolves the `AppScreen.Setup`
constructor-arg change from Phase 2 — proceed directly to US3 next since both are P1.)

---

## Phase 4: User Story 3 - Choose a game mode before player setup (Priority: P1)

**Goal**: New Game leads to a 501/Cricket list screen; selecting a mode opens player-setup
pre-selected, with the mode picker removed from player-setup.

**Independent Test**: From Home, tap New Game, see 501 and Cricket listed, tap one, confirm
player-setup opens with that mode pre-selected and no mode-switching control.

### Implementation for User Story 3

- [X] T005 [P] [US3] Create `app/src/main/java/com/bullseyestracker/ui/home/GameModeListScreen.kt`:
  a `@Composable fun GameModeListScreen(onModeSelected: (GameMode) -> Unit, onBack: () -> Unit)`
  rendering a `Back` button plus one row/button per `GameMode.entries` (labeled "501" / "Cricket"
  matching `MatchSetupScreen`'s existing label mapping), invoking `onModeSelected(mode)` on tap.
- [X] T006 [US3] Modify `app/src/main/java/com/bullseyestracker/ui/match/MatchSetupScreen.kt`:
  change the signature to
  `fun MatchSetupScreen(gameMode: GameMode, onStartMatch: (GameMode, List<String>) -> Unit, onBack: () -> Unit)`;
  delete the internal `var gameMode by remember { ... }` and the entire
  `SingleChoiceSegmentedButtonRow` mode-picker block; use the incoming `gameMode` parameter for
  the title text and in the `onStartMatch(gameMode, trimmedNames)` call; add a `Button(onClick =
  onBack) { Text("Back") }` above the title.
- [X] T007 [US3] In `app/src/main/java/com/bullseyestracker/MainActivity.kt`: add an
  `AppScreen.GameModeList` branch to the `when (screen)` block rendering `GameModeListScreen`
  with `onModeSelected = { mode -> screen = AppScreen.Setup(mode) }` and
  `onBack = { screen = AppScreen.Home }`; update the `AppScreen.Setup` branch (now
  `is AppScreen.Setup`) to destructure `val setupScreen = screen as AppScreen.Setup` (or use a
  `when` `is` pattern) and call
  `MatchSetupScreen(gameMode = setupScreen.gameMode, onStartMatch = matchViewModel::startMatch, onBack = { screen = AppScreen.GameModeList })`;
  update `HomeScreen`'s `onNewGame` (from T003) to `{ screen = AppScreen.GameModeList }`.

**Checkpoint**: Home → New Game → mode list → player-setup (mode pre-selected, no picker) → Start
match all work; back hops (setup → mode list → Home) all work. App now compiles end-to-end for
US1+US3 combined (Foundational's `Setup` signature change is now fully consumed).

---

## Phase 5: User Story 2 - Splash screen at launch (Priority: P2)

**Goal**: A brief branding screen shows on cold start, then auto-advances to Home (or to the
in-progress-match/permission-prompt path, unchanged).

**Independent Test**: Cold-launch the app, confirm the splash screen appears immediately and
auto-dismisses to Home after a short fixed delay with no tap required.

### Implementation for User Story 2

- [X] T008 [P] [US2] Create `app/src/main/java/com/bullseyestracker/ui/home/SplashScreen.kt`: a
  `@Composable fun SplashScreen()` rendering a centered `Box`/`Column` with the app name
  ("BullseyesTracker") as `Text` using `MaterialTheme.typography.headlineMedium` — static
  content only, no callbacks (the caller owns the auto-advance timer per T009).
- [X] T009 [US2] In `app/src/main/java/com/bullseyestracker/MainActivity.kt`: change the initial
  `screen` state to `AppScreen.Splash`; add an `AppScreen.Splash` branch to the `when (screen)`
  block that renders `SplashScreen()` wrapped with a
  `LaunchedEffect(Unit) { delay(1200); screen = AppScreen.Home }` (import
  `androidx.compose.runtime.LaunchedEffect` and `kotlinx.coroutines.delay`); ensure this branch is
  reached before the `cameraPermissionGranted` check that currently gates the rest of the UI, so
  the splash timer runs regardless of permission state and lands on `AppScreen.Home` (the existing
  permission-required `Text` still shows afterward if permission is missing, per FR-010/edge
  cases — no change needed to that branch's condition, it already gates on
  `cameraPermissionGranted` independently of `screen`).

**Checkpoint**: Cold start shows Splash then Home automatically; backgrounding/foregrounding
(process still alive) does not re-show Splash, since `screen` state isn't reset on resume.

---

## Phase 6: User Story 4 - Test the calibrator without starting a match (Priority: P2)

**Goal**: A standalone live-camera screen shows the calibration overlay/status with no scoring or
match side effects.

**Independent Test**: From Home, tap Test Calibrator, point the camera at a dartboard, confirm
the overlay and "calibrated" status appear; confirm no match is created; back returns to Home.

### Implementation for User Story 4

- [X] T010 [P] [US4] Create
  `app/src/main/java/com/bullseyestracker/ui/calibration/CalibrationTestScreen.kt`: a
  `@Composable fun CalibrationTestScreen(cvEngine: CvEngine, onBack: () -> Unit)` modeled on
  `LiveScoringScreen` (`app/src/main/java/com/bullseyestracker/ui/detection/LiveScoringScreen.kt`)
  but stripped to calibration-only: use `CameraController` + `LiveDetectionAnalyzer` with
  `onCalibrated = { newCalibration, confidence -> ... }` and `onBoardNotFound = { ... }` updating
  local `calibration`/`boardStatus` state exactly as `LiveScoringScreen` does, but pass an empty
  `onDetections = { }` (discard results — no `DetectedThrow` accumulation, no
  `DetectionOverlay`/`CorrectionDialog`, no turn/score UI). Render the camera `AndroidView`
  (`PreviewView`), `BullseyeOverlay(calibration = calibration, ...)` from
  `com.bullseyestracker.ui.detection.BullseyeOverlay`, the status `Text`, and a `Back` button
  calling `onBack`.
- [X] T011 [US4] In `app/src/main/java/com/bullseyestracker/MainActivity.kt`: add an
  `AppScreen.CalibrationTest` branch to the `when (screen)` block rendering
  `CalibrationTestScreen(cvEngine = appContainer.cvEngine, onBack = { screen = AppScreen.Home })`;
  update `HomeScreen`'s `onTestCalibrator` (from T003) to `{ screen = AppScreen.CalibrationTest }`.

**Checkpoint**: All four user stories work end-to-end; app compiles and the full navigation graph
from spec.md is reachable.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T012 Run `make build` (compile + unit tests + lint) from repo root and fix any ktlint or
  compile issues introduced across the files touched above.
- [X] T013 Manually run all 5 scenarios in `specs/013-app-home-navigation/quickstart.md` on a
  device/emulator and confirm each passes.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 2)**: No dependencies — start immediately. BLOCKS all user stories (all
  share the `AppScreen` sealed class it modifies).
- **US1 (Phase 3)** and **US3 (Phase 4)**: Both P1. Implement sequentially in this order — T003
  (US1) references `AppScreen.GameModeList`/`AppScreen.CalibrationTest` which Phase 2 already
  declares, but the app only fully compiles again once T007 (US3) resolves the `AppScreen.Setup`
  constructor-arg change. Treat US1 → US3 as one continuous MVP slice for that reason, even though
  they're separate stories per spec.md.
- **US2 (Phase 5)** and **US4 (Phase 6)**: Both P2, both independent of each other and safely
  addable after US1+US3. Can be done in either order or in parallel (different files, and each
  only adds one new `when` branch in `MainActivity.kt`).
- **Polish (Phase 7)**: After all four stories.

### Parallel Opportunities

- T002 (HomeScreen.kt) and later T005/T008/T010 (GameModeListScreen.kt/SplashScreen.kt/
  CalibrationTestScreen.kt) are each new, independent files — safe to write in parallel with each
  other once Phase 2 is done, even though wiring them into `MainActivity.kt` (T003/T007/T009/T011)
  must happen one at a time (same file).

---

## Implementation Strategy

### MVP First (US1 + US3 together)

1. Phase 2 Foundational.
2. Phase 3 (US1) + Phase 4 (US3) — together they form the smallest state where the app compiles
   and the primary "start a game from a real home screen" flow works end-to-end.
3. **STOP and VALIDATE**: run quickstart.md Scenarios 1–3 minus the splash-specific assertions.
4. Add Phase 5 (US2, splash) and Phase 6 (US4, calibrator) — independent, either order.
5. Phase 7 Polish.
