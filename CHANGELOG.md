# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
once a first tagged release exists.

## [Unreleased]

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
- Auto-update CHANGELOG.md and auto-publish releases on merge ([#57](https://github.com/leoluzh/bullseyestracker/pull/57)).

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

### Known gaps

- `OpenCvBoardDetectorTest`, `OpenCvDartDetectorTest`, and `PerformanceBenchmarkTest` need
  real dartboard fixture images under `cv/src/androidTest/assets/fixtures/`, not yet supplied
  (tracked as `tasks.md` T044).
- User Story 2 (photo scoring), User Story 3 (501 match), User Story 4 (Cricket match), and
  the Polish phase are not yet implemented.
