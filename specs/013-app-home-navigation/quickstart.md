# Quickstart: App Home Navigation

Manual validation guide (no automated app-module test suite exists in this repo today; see
plan.md Technical Context). Run against a device/emulator with camera access, ideally pointed at
a real dartboard for the calibration scenarios.

## Setup

```bash
make build   # compile + unit tests + lint
```

Install and launch the app (`make connected-test` needs a device; for manual validation, run from
Android Studio or `./gradlew installDebug` then launch `MainActivity` on a connected
device/emulator).

## Scenario 1 — Splash → Home (US1, US2)

1. Force-stop the app, then cold-launch it.
2. **Expect**: a splash screen appears immediately, then automatically (no tap) gives way to the
   home screen after ~1–1.5s — or to the camera-permission message if permission isn't granted
   yet.
3. **Expect**: the home screen lists exactly four options: New Game, Match History, Player
   Stats, Test Calibrator.

## Scenario 2 — Existing screens wired in (US1)

1. From Home, tap **Match History** → the existing match-history list opens; back returns to
   Home.
2. From Home, tap **Player Stats** → the existing player-stats screen opens; back returns to
   Home.

## Scenario 3 — New game via mode list (US3)

1. From Home, tap **New Game** → a screen listing **501** and **Cricket** appears.
2. Tap **501** → the player-setup screen opens with 501 already selected and no mode-picker
   control visible.
3. Back from player-setup → returns to the game-mode list (or Home, per implementation choice —
   confirm one consistent hop). Back from the game-mode list → returns to Home.
4. Repeat step 2 selecting **Cricket** instead, confirm Cricket is pre-selected.

## Scenario 4 — Test Calibrator (US4)

1. From Home, tap **Test Calibrator** → a live camera screen opens with calibration status text
   and no scoring/turn UI.
2. Point the camera away from any dartboard → status says no board found, no overlay drawn.
3. Point the camera at a real dartboard → the calibration boundary overlay (inner/outer bull
   circles) is drawn and status text says calibrated.
4. Back → returns to Home. Check Match History: confirm no new match/record was created by this
   screen.

## Scenario 5 — In-progress match bypasses Home (edge case)

1. Start a match (Scenario 3), confirm a couple of throws.
2. Background the app (Home button) and reopen it.
3. **Expect**: the app resumes directly into the in-progress match's scoreboard — Splash/Home are
   not shown.

## Done criteria

All five scenarios pass; `make build` is clean (compile + unit tests + lint).
