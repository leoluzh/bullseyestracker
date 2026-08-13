# Contributing to BullseyesTracker

Thanks for your interest in contributing. This project follows a spec-driven workflow
([spec-kit](https://github.com/github/spec-kit)) and a small set of non-negotiable engineering
principles defined in [`.specify/memory/constitution.md`](.specify/memory/constitution.md).
Read that file first — it governs scope decisions more than this document does.

## Code of Conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). By participating you
agree to abide by it.

## Getting set up

1. Install dependencies:
   - Linux/macOS: `./scripts/install-deps.sh`
   - Windows: `.\scripts\install-deps.ps1`
   - Or via [devbox](https://www.jetify.com/devbox) (`devbox shell`) for JDK/Gradle/adb —
     note devbox does **not** supply the full Android SDK; see `devbox.json`'s comments and
     use the install scripts (or Android Studio) for that part.
2. Verify: `make doctor` (checks java/gradle/adb/ANDROID_HOME are all visible).
3. Build: `make build` or `./gradlew build`.

Run `make help` for the full list of common commands (test, lint, install, run, wrapper).

## Project structure

Three Gradle modules, per [`plan.md`](specs/001-dart-scoring-match/plan.md):

- `app/` — Compose UI, CameraX capture, ViewModels. No OpenCV or Room types directly (see
  `CvNativeInit` and `MatchRepository.create()` for why).
- `cv/` — computer vision: `CvEngine` interface plus OpenCV-backed implementations.
- `match/` — game rules (501, Cricket), domain model, Room persistence.

## Working on a feature

This repo uses spec-kit's workflow, not ad-hoc feature branches:

1. `/speckit-specify` — write or update the feature spec under `specs/<NNN-feature-name>/`.
2. `/speckit-plan` — technical plan, checked against the constitution.
3. `/speckit-tasks` — dependency-ordered `tasks.md`.
4. `/speckit-analyze` (optional but recommended) — cross-check spec/plan/tasks before coding.
5. `/speckit-implement` — implement tasks in order, marking them `[X]` in `tasks.md` as you go.

Branch naming matches the spec directory: `<NNN>-<feature-name>` (e.g.
`001-dart-scoring-match`).

If you're fixing a bug or doing small cleanup unrelated to a spec'd feature, a normal
short-lived branch is fine — spec-kit isn't required for everything.

## Testing

Constitution Principle III (non-negotiable): any code that maps a detected position to a
score — `ScoreMapper`, `BoardDetector`/`DartDetector`, `X501Rules`, `CricketRules` — needs a
unit test written before the implementation, and that test must fail first (TDD). This is not
optional for that class of code; it is optional for everything else (UI wiring, camera
integration).

- `make test` — plain-JVM unit tests (fast, no device needed).
- `make connected-test` — instrumented tests (needs a connected device/emulator; this is where
  OpenCV-backed detector tests live, since OpenCV's native library needs a real Android
  runtime).
- `make lint` — ktlint check. `make format` auto-fixes what it can.

Before opening a PR, `make build` should pass clean (compile + test + lint).

## Commit messages

Imperative mood, explain *why* not just *what* when it's not obvious from the diff (see
`git log` for examples in this repo). No fixed prefix convention is enforced, but
`feat:`/`fix:`/`chore:`/`docs:` prefixes are used throughout the existing history and are
appreciated.

## Pull requests

- Target `master`.
- Describe what changed, why, and anything a reviewer should know (known gaps, unverified
  parts, follow-up work) — don't leave that only in commit messages.
- If your change touches a spec'd feature, keep `tasks.md` checkboxes in sync with what you
  actually implemented.
- Update [`CHANGELOG.md`](CHANGELOG.md) under `[Unreleased]` for user-facing or
  contributor-facing changes.

## Reporting issues

Use [GitHub Issues](https://github.com/leoluzh/bullseyestracker/issues). For security-relevant
reports, avoid filing a public issue — see `SECURITY.md` if present, or open a private security
advisory on GitHub instead.
