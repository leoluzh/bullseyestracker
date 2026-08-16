# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Starting with the next release, entries below this line are generated automatically by
[release-please](https://github.com/googleapis/release-please) from conventional-commit PR
titles on every merge to `master` — do not hand-edit past this point.

## [Unreleased]

### Added

- Auto-update CHANGELOG.md and auto-publish releases on merge ([#57](https://github.com/leoluzh/bullseyestracker/pull/57)).
- Swap release-drafter and the custom changelog script for release-please ([#58](https://github.com/leoluzh/bullseyestracker/pull/58)).

## [0.1.1] - 2026-08-16

### Added

- Draw bullseye calibration boundary overlay ([#38](https://github.com/leoluzh/bullseyestracker/pull/38)).
- App home navigation: splash, home menu, game-mode list, calibrator test ([#39](https://github.com/leoluzh/bullseyestracker/pull/39)).
- Add a way back to Home from the active match screen ([#55](https://github.com/leoluzh/bullseyestracker/pull/55)).
- Add a real app launcher icon ([#56](https://github.com/leoluzh/bullseyestracker/pull/56)).

### Fixed

- Autolabel PRs by title prefix for release-drafter categorization ([#37](https://github.com/leoluzh/bullseyestracker/pull/37)).

### Changed

- Give home screen a dartboard-themed look ([#40](https://github.com/leoluzh/bullseyestracker/pull/40)).
- Give splash screen a dartboard-themed look ([#41](https://github.com/leoluzh/bullseyestracker/pull/41)).
- Give game mode screen a dartboard-themed look ([#42](https://github.com/leoluzh/bullseyestracker/pull/42)).
- Give match history screen a dartboard-themed look ([#43](https://github.com/leoluzh/bullseyestracker/pull/43)).
- Give match history detail screen a dartboard-themed look ([#44](https://github.com/leoluzh/bullseyestracker/pull/44)).
- Give player stats screen a dartboard-themed look ([#45](https://github.com/leoluzh/bullseyestracker/pull/45)).
- Give test calibrator screen a more legible overlay UI ([#46](https://github.com/leoluzh/bullseyestracker/pull/46)).
- Give the match-scoring screen a dartboard-themed look ([#47](https://github.com/leoluzh/bullseyestracker/pull/47)).
- Give the capture mode toggle a dartboard-themed look ([#48](https://github.com/leoluzh/bullseyestracker/pull/48)).
- Give the throw correction dialog a real form UI ([#49](https://github.com/leoluzh/bullseyestracker/pull/49)).
- Give the detection markers a dartboard-themed look ([#50](https://github.com/leoluzh/bullseyestracker/pull/50)).
- Give the bullseye boundary rings a dartboard-themed look ([#51](https://github.com/leoluzh/bullseyestracker/pull/51)).
- Highlight the selected item in correction dialog dropdowns ([#52](https://github.com/leoluzh/bullseyestracker/pull/52)).
- Give the correction dialog title a real header ([#53](https://github.com/leoluzh/bullseyestracker/pull/53)).
- Give the correction dialog Save button proper CTA weight ([#54](https://github.com/leoluzh/bullseyestracker/pull/54)).

## [0.1.0] - 2026-08-16

### Added

- Spec-kit project setup: constitution, feature spec, plan, research, data model, and
  contracts for dart auto-scoring and match tracking (`specs/001-dart-scoring-match/`).
- MVP implementation of Setup + Foundational + User Story 1 (live-camera auto-scoring):
  3-module Gradle project (`app`/`cv`/`match`), `CvEngine` contract with OpenCV-backed board
  and dart detection, `ScoreMapper` real dartboard scoring geometry, Room persistence for
  match/player/turn/throw state, live-camera scoring screen with detection overlay and manual
  correction.
- `devbox.json` and `Makefile` for local dev commands (build/test/lint/install/run).
- `scripts/install-deps.sh` / `scripts/install-deps.ps1`: idempotent JDK 17 + Android SDK +
  Gradle-wrapper installers for Linux and Windows.
- Committed, verified-working Gradle wrapper (`gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.jar`).
- `CvNativeInit` (`cv` module) and `MatchRepository.create()` (`match` module) so the `app`
  module never touches OpenCV or Room types directly, per constitution Principle II.
  (All of the above landed in [#1](https://github.com/leoluzh/bullseyestracker/pull/1).)
- Photo-based dart scoring, User Story 2 ([#27](https://github.com/leoluzh/bullseyestracker/pull/27)).
- 501 match scoring, User Story 3 ([#29](https://github.com/leoluzh/bullseyestracker/pull/29)).
- Cricket match scoring, User Story 4 ([#30](https://github.com/leoluzh/bullseyestracker/pull/30)).
- Match history with confirm resume-on-launch ([#31](https://github.com/leoluzh/bullseyestracker/pull/31)).
- Fixture ground-truth harness and detection-accuracy benchmark ([#32](https://github.com/leoluzh/bullseyestracker/pull/32)).
- Player statistics screen with win rate ([#36](https://github.com/leoluzh/bullseyestracker/pull/36)).

### Fixed

- `CameraController` ran `ImageAnalysis` on the main executor instead of a background one,
  violating the real-time performance budget (constitution Principle IV).
- Spec-analysis gaps: missing SC-002 (detection accuracy) task coverage, and an unpinned
  performance-benchmark reference device.
- Toolchain incompatibilities surfaced by the first real build against an installed Android
  SDK: Kotlin bumped 1.9.22 → 2.1.21 (old compiler couldn't parse newer JDK version strings),
  KSP bumped to 2.1.21-2.0.1, Room bumped 2.6.1 → 2.7.1 (KSP2 processing bug), Compose
  compiler switched to the `org.jetbrains.kotlin.plugin.compose` plugin, Mockito/ByteBuddy
  given `-Dnet.bytebuddy.experimental=true` for JDK 26 support.
- ktlint style violations (trailing commas, blank lines); added `.editorconfig` so ktlint's
  function-naming rule doesn't flag `@Composable` functions.
  (All of the above landed in [#1](https://github.com/leoluzh/bullseyestracker/pull/1).)
- Repair master build after Dependabot AGP 9 / OpenCV 5 bumps ([#19](https://github.com/leoluzh/bullseyestracker/pull/19)).
- Bump CameraX to 1.4.2 for 16 KB page size support ([#28](https://github.com/leoluzh/bullseyestracker/pull/28)).

### Documentation

- Add Apache 2.0 LICENSE ([#34](https://github.com/leoluzh/bullseyestracker/pull/34)).
- Refresh stale project-state claims in CLAUDE.md ([#35](https://github.com/leoluzh/bullseyestracker/pull/35)).

### Maintenance

- Remove orphaned release-please config, keep release-drafter ([#33](https://github.com/leoluzh/bullseyestracker/pull/33)) —
  later reversed by [#58](https://github.com/leoluzh/bullseyestracker/pull/58) above, once release-drafter turned out to
  never publish what it drafted.
- Dependency bumps: gradle-wrapper, AGP, Kotlin, Kotlin Compose plugin, KSP, OpenCV, Room,
  activity-compose, core-ktx, Mockito, ktlint, JUnit, coroutines-test, and CI Actions
  (`checkout`, `setup-java`, `setup-android`, `upload-artifact`, `gradle/actions`,
  `release-drafter`, `ghaction-github-labeler`)
  ([#2](https://github.com/leoluzh/bullseyestracker/pull/2)–[#18](https://github.com/leoluzh/bullseyestracker/pull/18),
  [#20](https://github.com/leoluzh/bullseyestracker/pull/20)–[#26](https://github.com/leoluzh/bullseyestracker/pull/26)).

### Known gaps

- `OpenCvBoardDetectorTest`, `OpenCvDartDetectorTest`, and `PerformanceBenchmarkTest` need
  real dartboard fixture images under `cv/src/androidTest/assets/fixtures/`, not yet supplied
  (tracked as `tasks.md` T044).
