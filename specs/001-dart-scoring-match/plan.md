# Implementation Plan: Dart Scoring & Match Tracking

**Branch**: `001-dart-scoring-match` | **Date**: 2026-08-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-dart-scoring-match/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Android app that auto-scores darts on a standard dartboard from either a live camera feed or a
single photo, using on-device computer vision (OpenCV) to locate the board and each dart, map
detections to sector/ring/score, let the user correct any detection, and feed confirmed turn
scores into 501 and Cricket match tracking with local persistence.

## Technical Context

**Language/Version**: Kotlin 1.9+ / JVM 17, Android Gradle Plugin (current stable)

**Primary Dependencies**: OpenCV for Android (board/dart detection per constitution Technology
Constraints — classical CV baseline, TFLite/ML Kit swappable later behind the same interface),
CameraX (camera capture for live-feed and photo modes), Jetpack Compose (UI), Room (local
persistence)

**Storage**: Room (SQLite) — match state, player/turn/throw history, completed-match history
(FR-011, FR-014)

**Testing**: JUnit + Kotlin for unit tests (scoring/rules/CV-interface contract tests per
constitution Principle III), Espresso/Compose UI testing for instrumented flows

**Target Platform**: Android, minSdk 26 (Android 8.0 — CameraX baseline support), target/compile
against current stable Android SDK

**Project Type**: Mobile app (single Android app with internal Gradle modules; no backend —
FR-013 requires full offline operation)

**Performance Goals**: Frame-to-result detection latency <200ms on a mid-tier device
(constitution Principle IV); live camera preview MUST NOT drop below a usable frame rate
(target 30fps preview, detection runs on a background pipeline, not per-preview-frame)

**Constraints**: Fully offline/on-device (constitution Principle I, spec FR-013); CV logic
isolated from UI behind a plain Kotlin interface (constitution Principle II); every automatic
score must be user-correctable before commit (constitution Principle V, spec FR-006)

**Scale/Scope**: Single-device, single-user-facing app (no multi-device sync); 2 game modes
(501, Cricket) with 2-4 local players per match; 4 user stories (spec.md)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|---|---|---|
| I. On-Device Processing | No network dependency for detection/scoring | PASS — OpenCV runs on-device; Room is local-only; no backend in scope (FR-013) |
| II. CV Logic Isolated From UI | CV exposed via plain Kotlin interface, separate module | PASS — dedicated `cv` module exposes `CvEngine` interface (see Project Structure, contracts/cv-engine-contract.md); `app` and `match` modules never touch OpenCV types directly |
| III. Test-First for Scoring Logic | Unit tests before scoring/rules implementation | PASS (process gate, enforced at task/implementation time) — `match` module's rule engines (501/Cricket) and `cv` module's sector/ring mapping are the concrete units bound by this gate |
| IV. Real-Time Performance Budget | <200ms frame-to-result, non-blocking UI thread | PASS (design-level) — detection pipeline runs off the UI/camera-preview thread via CameraX `ImageAnalysis` use case; budget re-validated against real device data in Phase 0 research |
| V. Explainable, Correctable Detection | Overlay + manual correction before commit | PASS — spec FR-005/FR-006 mandate this directly; UI design in quickstart.md must include the correction flow |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
app/                          # UI module: Compose screens, ViewModels, CameraX capture flows
├── src/main/java/.../ui/
├── src/main/java/.../camera/
├── src/test/
└── src/androidTest/

cv/                           # Vision module: OpenCV-backed detection, isolated per constitution II
├── src/main/java/.../cv/
│   ├── CvEngine.kt           # public interface: frame/photo in -> detected throws out
│   ├── BoardDetector.kt
│   ├── DartDetector.kt
│   └── ScoreMapper.kt        # position -> sector/ring/score
└── src/test/                 # unit tests against fixture images/coords (constitution III)

match/                        # Game/domain module: match state, 501 + Cricket rules, persistence
├── src/main/java/.../match/
│   ├── model/                # Match, Player, Turn, Throw
│   ├── rules/                # X501Rules, CricketRules
│   └── data/                 # Room entities/DAOs, MatchRepository
└── src/test/                 # unit tests for rules/bust/checkout/win-condition logic
```

**Structure Decision**: Single Android app, split into 3 Gradle modules (`app`, `cv`, `match`)
rather than one monolithic module. This directly implements constitution Principle II (CV logic
isolated from UI, testable without Android instrumentation) and keeps game-rule logic (`match`)
independently unit-testable from both UI and CV concerns. No backend/API module — FR-013 keeps
the app fully offline, so a mobile-only structure (no `api/` sibling) is sufficient.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
