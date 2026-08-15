# BullseyesTracker

Android app that auto-scores darts on a standard dartboard using on-device computer vision —
point your camera at the board (or take a photo) and it detects each dart, overlays the
sector/ring/score, lets you correct anything it got wrong, and tracks a full 501 or Cricket
match across players.

Fully offline. No backend, no account, no network dependency for scoring.

## Status

Early, in progress. Live-camera auto-scoring (the core MVP) builds, runs its unit tests, and
passes them. Photo scoring, 501/Cricket match tracking, and match history are specified and
planned but not yet implemented. See [CHANGELOG.md](CHANGELOG.md) for what's landed and
[specs/001-dart-scoring-match/tasks.md](specs/001-dart-scoring-match/tasks.md) for what's left.

## How it works

- **`cv` module** — `CvEngine`: locates the dartboard (Hough-circle detection on its outer
  edge, then derives ring/sector geometry from regulation board ratios) and detects darts
  (frame-diff against a calibrated baseline), mapping each to a sector/ring/score.
- **`match` module** — game rules (501, Cricket) and Room-backed persistence for
  matches/players/turns/throws.
- **`app` module** — Jetpack Compose UI and CameraX capture; never touches OpenCV or Room
  types directly (see `CvNativeInit` / `MatchRepository.create()`), only the `cv`/`match`
  interfaces.

Full rationale for these decisions is in
[specs/001-dart-scoring-match/research.md](specs/001-dart-scoring-match/research.md) and the
project [constitution](.specify/memory/constitution.md).

## Getting started

**Prerequisites**: JDK 17, Android SDK (`platform-tools`, `platforms;android-34`,
`build-tools;34.0.0`), a physical Android device or emulator for camera-dependent testing.

Install dependencies:

```bash
# Linux/macOS
./scripts/install-deps.sh

# Windows
.\scripts\install-deps.ps1
```

Or use [devbox](https://www.jetify.com/devbox) (`devbox shell`) for JDK/Gradle/adb — it does
**not** cover the full Android SDK; use the install scripts or Android Studio for that part.

Then:

```bash
make doctor   # sanity-check java/gradle/adb/ANDROID_HOME
make build    # compile + unit tests + lint
make run      # install debug build + launch on a connected device
```

Run `make help` for the full command list.

## Documentation

- [CONTRIBUTING.md](CONTRIBUTING.md) — workflow, module layout, testing requirements
- [CHANGELOG.md](CHANGELOG.md) — what's changed
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- [specs/001-dart-scoring-match/](specs/001-dart-scoring-match/) — spec, plan, research, data
  model, contracts, and tasks for the current feature (built with
  [spec-kit](https://github.com/github/spec-kit))

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
