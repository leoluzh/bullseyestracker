# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Starting with the next release, entries below this line are generated automatically by
[release-please](https://github.com/googleapis/release-please) from conventional-commit PR
titles on every merge to `master` — do not hand-edit past this point.

## [0.3.0](https://github.com/leoluzh/bullseyestracker/compare/bullseyestracker-v0.2.0...bullseyestracker-v0.3.0) (2026-08-18)


### Features

* add DNN-based dart/board detection backend (OpenCV dnn + YOLOv8) ([#62](https://github.com/leoluzh/bullseyestracker/issues/62)) ([c855c79](https://github.com/leoluzh/bullseyestracker/commit/c855c792a424f5a558fb31865d143b866225a6a4))

## [0.2.0](https://github.com/leoluzh/bullseyestracker/compare/bullseyestracker-v0.1.1...bullseyestracker-v0.2.0) (2026-08-17)


### Features

* add a real app launcher icon ([#56](https://github.com/leoluzh/bullseyestracker/issues/56)) ([3ef56af](https://github.com/leoluzh/bullseyestracker/commit/3ef56afebfeee670cb53bb29c899b7c6022ba745))
* add a way back to Home from the active match screen ([#55](https://github.com/leoluzh/bullseyestracker/issues/55)) ([1b0bdbc](https://github.com/leoluzh/bullseyestracker/commit/1b0bdbc1209cbe558c36c9e3b5a580850c93012f))
* add dependency-install scripts for Linux and Windows ([9c95001](https://github.com/leoluzh/bullseyestracker/commit/9c95001a35657e648f193ed31ee948241d1fc9e1))
* add home screen, splash, game-mode list, and calibrator test screen (spec 013-app-home-navigation) ([#39](https://github.com/leoluzh/bullseyestracker/issues/39)) ([67e2f74](https://github.com/leoluzh/bullseyestracker/commit/67e2f74d357f64e0532831bb59885e8c8a8c2db5))
* add match history and confirm resume-on-launch (spec 005-match-history) ([e46fff3](https://github.com/leoluzh/bullseyestracker/commit/e46fff349267536d13881b2356eed07f0a6eef33))
* add player statistics screen with win rate (spec 010-player-stats) ([#36](https://github.com/leoluzh/bullseyestracker/issues/36)) ([f7e21be](https://github.com/leoluzh/bullseyestracker/commit/f7e21be4709a4aa57c87c92f819d1f4d0c9c9462))
* auto-update CHANGELOG.md and auto-publish releases on merge ([#57](https://github.com/leoluzh/bullseyestracker/issues/57)) ([5bea51f](https://github.com/leoluzh/bullseyestracker/commit/5bea51f2cd11496de7d6d5c15df8b3cfa1a26ae3))
* draw bullseye calibration boundary overlay (spec 012-bullseye-overlay) ([#38](https://github.com/leoluzh/bullseyestracker/issues/38)) ([f463aac](https://github.com/leoluzh/bullseyestracker/commit/f463aacc9e9c44449964aa6b0c231a7e44f30ed4))
* implement 501 match scoring (spec 003-501-match) ([da39135](https://github.com/leoluzh/bullseyestracker/commit/da3913554514f289541f4f51448d931980b0ee72))
* implement 501 match scoring (US3) ([6ba5c8f](https://github.com/leoluzh/bullseyestracker/commit/6ba5c8fb4c52667257146adf115e90d19e5e34b0))
* implement Cricket match scoring (US4) ([acdb48b](https://github.com/leoluzh/bullseyestracker/commit/acdb48b868e044c2d0d9674382c682af49c0883a))
* implement Cricket match scoring (US4) ([70e603d](https://github.com/leoluzh/bullseyestracker/commit/70e603d2f5bda0209daacfe21669607267f59ae2))
* implement MVP (Setup + Foundational + US1 live-camera scoring) ([17c7ac8](https://github.com/leoluzh/bullseyestracker/commit/17c7ac8b47eb47fc8eedfaf65461e2e438a2ffdc))
* implement US2 photo-based dart scoring ([60c96f4](https://github.com/leoluzh/bullseyestracker/commit/60c96f4be4492ad919e2f8f38aad10d7b46e7fb0))
* initialize spec-kit project and dart-scoring feature spec ([222fe63](https://github.com/leoluzh/bullseyestracker/commit/222fe63400d3f3c09c3ae707ba44d0bfed95853a))
* swap release-drafter + custom changelog script for release-please ([#58](https://github.com/leoluzh/bullseyestracker/issues/58)) ([2753437](https://github.com/leoluzh/bullseyestracker/commit/2753437480e186b2e4afa8f5bdb83a9c90a2c81a))
* US2 photo-based dart scoring ([a2f06cb](https://github.com/leoluzh/bullseyestracker/commit/a2f06cb561aa0d695ff5edf21724b15d10e3c631))


### Bug Fixes

* autolabel PRs by title prefix for release-drafter categorization (spec 011-release-drafter-autolabel) ([#37](https://github.com/leoluzh/bullseyestracker/issues/37)) ([2ddf87b](https://github.com/leoluzh/bullseyestracker/commit/2ddf87b8ba3126941457c7e4e2535d1865bd1bd5))
* bump CameraX to 1.4.2 for 16 KB page size support ([315cb0d](https://github.com/leoluzh/bullseyestracker/commit/315cb0d7785a53f657cf1ef1e23640b359d0c94b))
* bump CameraX to 1.4.2 for 16 KB page size support ([e672527](https://github.com/leoluzh/bullseyestracker/commit/e672527f85d14bdf0df75ea50299208e2b422e2e))
* bump compileSdk/targetSdk to 37, reformat for ktlint 14.2.0 ([d4c7129](https://github.com/leoluzh/bullseyestracker/commit/d4c7129dc622ac87742026171c2d7db3c91815b6))
* close spec-analysis gaps in dart-scoring plan ([2345886](https://github.com/leoluzh/bullseyestracker/commit/2345886ecf9a7b42158cc10f593ed66d00d6e285))
* make gradlew executable in CI ([79e5c58](https://github.com/leoluzh/bullseyestracker/commit/79e5c582501571b7c7af39c5517bad60b0978194))
* make the project actually build and pass its unit tests ([0633ba1](https://github.com/leoluzh/bullseyestracker/commit/0633ba1f835466077349aff1edcf9a98d9fd0d80))
* repair master build after Dependabot AGP 9 / OpenCV 5 bumps ([11e6982](https://github.com/leoluzh/bullseyestracker/commit/11e69829e5372b3b74914afa028cb4417b58a417))
* repair master build after Dependabot AGP 9 / OpenCV 5 bumps ([c940fc9](https://github.com/leoluzh/bullseyestracker/commit/c940fc9e3b91d52929a605b7ca89e23e73c25e4c))

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
