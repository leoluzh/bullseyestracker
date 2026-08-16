# Feature Specification: Release Drafter Autolabeling

**Feature Branch**: `011-release-drafter-autolabel`

**Created**: 2026-08-16

**Status**: Draft

**Input**: User description: "Fix release-drafter's PR categorization going forward. Investigation
found two separate label vocabularies that don't overlap: .github/labeler.yml (path-based:
android, cv, match, testing, build, dependencies, documentation) and .github/release-drafter.yml's
category triggers (feature, enhancement, bug, fix, documentation, chore, maintenance, major,
minor, patch) — only 'documentation' is shared. So a real feature PR that happens to touch a
spec .md file gets labeled only 'documentation' by the path-based labeler and gets miscategorized
under release-drafter's Documentation section instead of Features, purely by accident, on every
PR going forward unless someone manually labels it. Fix: add an autolabeler section to
release-drafter.yml matching conventional-commit-style PR title prefixes (feat:, fix:, docs:,
chore:, test:) to release-drafter's own category labels (feature, fix, documentation, chore), so
the release draft categorizes new PRs correctly without requiring manual labeling or a second
labeling mechanism. Does not touch .github/labeler.yml (different purpose, keeps working
independently) and does not retroactively relabel already-merged PRs (accepted limitation)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - A maintainer trusts the draft release's categorization (Priority: P1)

A maintainer opens the in-progress draft release to see what's changed since the last published
release. Today, a PR that's clearly a new feature (e.g., it touches `app/` and adds a screen)
shows up filed under "📝 Documentation" instead of "🚀 Features" whenever it happens to also touch
a `specs/**/*.md` file — which nearly every PR from this project's spec-driven workflow does,
since every feature ships its own `specs/NNN-*/` documentation alongside the code. After this
change, a PR whose title follows this project's existing conventional-commit-style prefix
(`feat:`, `fix:`, `docs:`, `chore:`, `test:` — already used in this repo's own commit messages)
is filed under the correct release-drafter category automatically, with no manual labeling step.

**Why this priority**: This is the entire scope of the change — a configuration fix so the
release draft's categorization reflects reality instead of an accidental default. There is no
smaller independently-valuable slice.

**Independent Test**: Open a pull request whose title starts with a conventional-commit prefix
(e.g. `fix: ...`); after release-drafter's workflow runs on it, the PR carries the matching
release-drafter category label (e.g. `fix`), without anyone manually adding it.

**Acceptance Scenarios**:

1. **Given** a pull request titled starting with `feat:`, **When** release-drafter's workflow runs
   on it (PR opened or synchronized), **Then** the PR is automatically labeled `enhancement`
   (spec FR-001).
2. **Given** a pull request titled starting with `fix:`, **When** release-drafter's workflow runs
   on it, **Then** the PR is automatically labeled `bug` (spec FR-002).
3. **Given** a pull request titled starting with `docs:` or `chore:` or `test:`, **When**
   release-drafter's workflow runs on it, **Then** the PR is automatically labeled `documentation`
   or `chore` respectively (spec Assumptions: `test:` maps to `chore`, since this project has no
   dedicated "Testing" release-note category).
4. **Given** the next draft release is viewed after such a PR merges, **When** a maintainer reads
   it, **Then** the PR appears under the category matching its actual title prefix, not under
   "Documentation" by accident.
5. **Given** `.github/labeler.yml`'s existing path-based labels (`android`, `cv`, `match`,
   `testing`, `build`, `dependencies`, `documentation`), **When** this change is in place,
   **Then** those labels continue to be applied exactly as before — this change adds a label
   source, it does not remove or alter the existing one.

---

### Edge Cases

- What happens to a PR whose title doesn't start with any recognized prefix? It gets no
  autolabel from this change and falls into release-drafter's existing uncategorized/default
  bucket — the same behavior as today for such PRs; this change only adds coverage for the
  conventional-commit-prefixed case, it doesn't change what happens otherwise.
- What happens to PRs merged before this change (e.g. the ones that motivated this investigation)?
  Not retroactively relabeled — an accepted limitation (this feature's Input), since GitHub
  Actions triggers don't run against already-closed PRs.
- What happens if a PR's title has a prefix but its actual changes don't match (e.g. titled
  `fix:` but it's really a feature)? Out of scope — this change automates the same signal a human
  would use (the PR title the author chose), it doesn't verify title accuracy against the diff.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The release-drafter configuration MUST automatically apply the `enhancement` label
  (already wired to the "🚀 Features" category) to any pull request whose title starts with
  `feat:` (case-insensitive). Correction during implementation: this repo has no `feature` label
  at all (verified via `gh label list`); `enhancement` already exists and already triggers the
  same category, so it's used instead of creating a redundant new label.
- **FR-002**: The release-drafter configuration MUST automatically apply the `bug` label (already
  wired to the "🐛 Bug Fixes" category) to any pull request whose title starts with `fix:`
  (case-insensitive). Same correction as FR-001 — no `fix` label exists; `bug` already does and
  already triggers the same category.
- **FR-003**: The release-drafter configuration MUST automatically apply the `documentation`
  label to any pull request whose title starts with `docs:` (case-insensitive).
- **FR-004**: The release-drafter configuration MUST automatically apply the `chore` label to any
  pull request whose title starts with `chore:` or `test:` (case-insensitive). Unlike FR-001/
  FR-002, no existing repo label already triggers the "🔧 Maintenance" category (`chore` and
  `maintenance` — the category's two listed triggers — neither exists as a repo label today), so
  this change creates the `chore` label.
- **FR-005**: This change MUST NOT modify `.github/labeler.yml` or its workflow — the existing
  path-based labels must continue to be applied unchanged (Acceptance Scenario 4).
- **FR-006**: This change MUST NOT attempt to relabel any pull request merged before this change
  takes effect (Edge Cases).

### Key Entities

Not applicable — this change edits only GitHub Actions/release-drafter configuration; no
application data or runtime entities are involved.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A pull request titled with a conventional-commit prefix (`feat:`/`fix:`/`docs:`/
  `chore:`/`test:`) is automatically labeled with the correct release-drafter category label,
  verified on this change's own pull request (which will itself carry such a title).
- **SC-002**: The next draft release generated after this change categorizes such PRs under the
  matching section (Features/Bug Fixes/Documentation/Maintenance), not defaulting to
  Documentation.
- **SC-003**: `.github/labeler.yml`'s path-based labels remain applied exactly as before — zero
  regression to existing labeling behavior.

## Assumptions

- This project's PR titles will follow the same conventional-commit-style prefix already used in
  its own commit messages (`feat:`, `fix:`, `docs:`, `chore:`, `test:`) — this is an existing,
  established convention in this repository's git history, not a new one introduced by this
  change. Autolabeling only helps for PRs that follow it going forward.
- `test:`-prefixed PRs map to release-drafter's `chore`/"🔧 Maintenance" category rather than a
  new dedicated category, since `release-drafter.yml` currently defines only four categories
  (Features, Bug Fixes, Documentation, Maintenance) and adding a fifth is a separate, larger
  change not requested here.
- `.github/labeler.yml`'s path-based labels (`android`, `cv`, `match`, etc.) and release-drafter's
  own category labels (`feature`, `fix`, `documentation`, `chore`) are independent label sets
  that both get applied to the same PR without conflict — a PR can carry both `android` (from
  path-based labeling) and `feature` (from this change's title-based autolabeling)
  simultaneously.
