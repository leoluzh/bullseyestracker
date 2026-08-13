# Security Policy

## Scope

BullseyesTracker is a fully offline Android app (constitution Principle I): no backend, no
user accounts, no network calls for scoring or match data. The realistic attack surface is
local — the app's own permissions handling (camera), local data storage (Room database of
match history), and its dependencies (OpenCV, CameraX, Room, Compose, Kotlin/Gradle
toolchain).

## Supported Versions

Pre-release: there are no tagged versions yet. Only the latest `master` (and its active
feature branches) receive fixes.

| Version | Supported |
| ------- | --------- |
| `master` (latest) | ✅ |
| anything older | ❌ |

This table will be replaced with real version ranges once the first tagged release exists.

## Reporting a Vulnerability

Please do **not** open a public GitHub issue for security vulnerabilities.

Instead, use GitHub's private reporting:
[Report a vulnerability](https://github.com/leoluzh/bullseyestracker/security/advisories/new)
(Security tab → "Report a vulnerability" on the repo). This opens a private advisory visible
only to maintainers until it's resolved and disclosed.

If that's unavailable to you, contact the repo owner ([@leoluzh](https://github.com/leoluzh))
directly through GitHub.

Please include:

- A description of the issue and its potential impact
- Steps to reproduce (device/Android version, app version/commit, minimal repro if possible)
- Any relevant logs or stack traces (redact anything personal)

## What to expect

This is a small, early-stage project maintained on a best-effort basis — there's no formal SLA.
Reports will be acknowledged as soon as reasonably possible, and credited in the fix's
changelog entry unless you'd prefer otherwise.

## Dependency vulnerabilities

If you find a known-vulnerable version of a dependency (OpenCV, AndroidX libraries, Kotlin/
Gradle tooling, etc.) pinned in this repo's build files, a GitHub issue is fine for that —
version bumps aren't sensitive the way exploitable app behavior is.
