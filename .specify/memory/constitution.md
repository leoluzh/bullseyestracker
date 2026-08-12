<!--
Sync Impact Report
Version change: TEMPLATE → 1.0.0
Modified principles: n/a (initial ratification)
Added sections: Core Principles (5), Technology Constraints, Development Workflow, Governance
Removed sections: none
Templates requiring updates:
  ✅ .specify/templates/plan-template.md (generic Constitution Check gate, no changes needed)
  ✅ .specify/templates/spec-template.md (generic, no changes needed)
  ✅ .specify/templates/tasks-template.md (generic, no changes needed)
  ✅ .claude/skills/speckit-*/SKILL.md (agent-agnostic, no changes needed)
Follow-up TODOs: none
-->

# BullseyesTracker Constitution

## Core Principles

### I. On-Device Processing (NON-NEGOTIABLE)
All dart-detection and counting logic MUST run on-device via computer vision; the app MUST
NOT require a network round-trip or external server to score a throw. Rationale: usage happens
at a dartboard (garage, bar, club) where connectivity is unreliable; latency and privacy both
demand local inference.

### II. CV Logic Isolated From UI
Computer vision and scoring logic (target detection, dart-tip detection, coordinate-to-score
mapping) MUST live in a separate module/package from Activities/Composables/ViewModels, exposed
through a plain Kotlin interface that takes an image/frame and returns structured results
(dart positions, score). Rationale: enables unit testing of detection accuracy without
Android instrumentation, and allows swapping the CV backend (classical OpenCV vs. TFLite model)
without touching UI code.

### III. Test-First for Scoring Logic (NON-NEGOTIABLE)
Any code that maps a detected dart position to a score (ring, sector, multiplier) MUST have
unit tests written and failing before implementation, using fixed calibration data (known board
geometry) and fixture images/coordinates. Rationale: scoring correctness is the core value
proposition of the app; regressions here are silent and high-cost (wrong game scores).

### IV. Real-Time Performance Budget
Frame-to-result latency for live detection MUST stay under 200ms on a mid-tier device (as
defined in the plan's target-device list), and the camera pipeline MUST NOT block the UI
thread. Rationale: the app is used interactively during a live game; a laggy camera view breaks
the product's core interaction.

### V. Explainable, Correctable Detection
Every automatic score MUST be visually overlaid on the camera/photo (detected dart position,
board sectors) and MUST be manually correctable by the user before being committed to the
game score. Rationale: computer vision detection is probabilistic and will misfire (occlusion,
lighting, dart bounce-outs); the user must always be able to fix a wrong call without losing
trust in the app.

## Technology Constraints

Platform: Android (Kotlin), minimum SDK to be fixed in the implementation plan.
Computer vision: OpenCV for Android (classical CV — Hough circle/contour detection for the
board, color/contrast-based detection for dart tips) is the default baseline; a TensorFlow
Lite/ML Kit object-detection model MAY be introduced later behind the same CV interface
(Principle II) if classical CV proves insufficient in real-world lighting/occlusion
conditions. Any new CV dependency MUST support fully offline/on-device inference (Principle I).
Persistence: local storage only for game history/score data; no mandatory account or backend
in the initial scope.

## Development Workflow

Features are specified via `/speckit-specify`, planned via `/speckit-plan`, and broken into
tasks via `/speckit-tasks` before implementation. Each plan MUST pass the Constitution Check
gate against this document before task breakdown begins. Scoring-logic changes require unit
tests per Principle III before merge. UI-only changes are exempt from the CV-isolation and
real-time-budget gates but MUST NOT reach into CV internals directly (Principle II).

## Governance

This constitution supersedes ad-hoc practices for this project. Amendments require: (1) a
documented rationale for the change, (2) a version bump per semantic versioning (MAJOR for
principle removal/redefinition, MINOR for new/expanded principles, PATCH for wording/
clarification), and (3) a pass over dependent templates (plan/spec/tasks) to check for
outdated references. All feature plans MUST verify compliance with this constitution at the
Constitution Check gate; unresolved violations MUST be justified in the plan's Complexity
Tracking section or the plan MUST be simplified.

**Version**: 1.0.0 | **Ratified**: 2026-08-12 | **Last Amended**: 2026-08-12
