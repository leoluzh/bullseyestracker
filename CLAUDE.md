# CLAUDE.md

Guidance for Claude Code sessions working in this repo. Read
[`.specify/memory/constitution.md`](.specify/memory/constitution.md) first — it governs scope
decisions more than anything here.

## What this is

Android app that auto-scores darts via on-device computer vision (OpenCV). Fully offline, no
backend. Built with a spec-driven workflow ([spec-kit](https://github.com/github/spec-kit));
the active feature lives in `specs/001-dart-scoring-match/` (spec, plan, research, data-model,
contracts, tasks).

## Module boundaries (do not violate)

- `app` — Compose UI, CameraX. Must **never** import `org.opencv.*` or `androidx.room.*`
  directly. Use `com.bullseyestracker.cv.CvNativeInit` to load OpenCV's native lib, and
  `com.bullseyestracker.match.data.MatchRepository.create(context)` to get a repository. This
  was violated once (MainActivity/AppContainer calling OpenCV/Room directly) and caused a real
  compile failure when first built for real — see commit `0633ba1`.
- `cv` — `CvEngine` interface (`CvEngine.kt`) is the only thing `app`/`match` should depend on
  conceptually. `BoardDetector`/`DartDetector` are interfaces with OpenCV-backed
  implementations under `cv/opencv/`; this split exists so `CvEngineImpl` and `ScoreMapper` are
  plain-JVM unit-testable (`cv/src/test/`) without an Android runtime, while the OpenCV-touching
  classes are instrumented-tested (`cv/src/androidTest/`) since OpenCV's native lib only loads
  on a device/emulator.
- `match` — game rules (501, Cricket — not yet implemented) and Room persistence.

## Constitution-mandated testing (non-negotiable)

Anything that maps a detected position to a score — `ScoreMapper`, `BoardDetector`/
`DartDetector`, and (once built) `X501Rules`/`CricketRules` — needs a test written and failing
*before* the implementation. This is enforced by convention, not tooling; don't skip it.

## Build environment gotchas (hit and fixed this session)

- **JDK version vs. Kotlin/tooling**: very new JDKs (25/26) broke Kotlin 1.9.22's embedded
  compiler daemon and Mockito's ByteBuddy. Fixed by bumping Kotlin to 2.1.21 (+ KSP
  2.1.21-2.0.1, Compose via `org.jetbrains.kotlin.plugin.compose`) and adding
  `-Dnet.bytebuddy.experimental=true` to the `cv` module's test JVM args. If tooling starts
  failing again after a JDK update, check this first.
- **Room + KSP2**: Room <2.7.0 hits a known `unexpected jvm signature V` bug under KSP2/K2. Keep
  Room at 2.7.0+.
- **Android SDK**: not provided by `devbox.json` (nixpkgs' `androidenv` license-acceptance
  option doesn't propagate through devbox — see `jetify-com/devbox#2236`, still open). Use
  `scripts/install-deps.sh` / `.ps1`, or point `local.properties`' `sdk.dir` at an existing
  Android Studio SDK install. `local.properties` is gitignored — never commit it.
- **Gradle wrapper is committed** (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) and verified
  working (`./gradlew build` passes clean: compile + unit tests + lint). If you regenerate it,
  re-verify a full build before assuming it still works.

## Commands

```bash
make doctor          # sanity-check java/gradle/adb/ANDROID_HOME
make build            # compile + unit tests + lint (this is what CI runs)
make test             # unit tests only
make connected-test   # instrumented tests - needs a device/emulator
make lint / format    # ktlint check / autofix
```

## Known gaps (don't assume these exist)

- `cv/src/androidTest/assets/fixtures/*.png` (real dartboard photos) are not present — the
  OpenCV-backed instrumented tests and the perf benchmark will fail on a missing-asset error
  until someone adds them (`tasks.md` T044).
- User Stories 2-4 (photo scoring, 501 match, Cricket match) and the Polish phase are specified
  in `tasks.md` but not implemented.
- No LICENSE chosen yet.
