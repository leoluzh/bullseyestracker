# Feature Specification: Release Tooling Cleanup

**Feature Branch**: `007-release-tooling-cleanup`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Remove orphaned release-please configuration and keep
release-drafter as the project's single release-notes tool. Both release-drafter and
release-please are currently configured (CLAUDE.md known gap), but investigation found
release-please has never actually run: no GitHub Actions workflow file wires it up (only
release-drafter.yml exists under .github/workflows/), and CHANGELOG.md is a stale hand-written
file frozen at the initial MVP milestone, not release-please-generated output. release-drafter is
confirmed active (passes in CI on every recent merged PR, drafting release notes from PR labels).
Delete release-please-config.json and .release-please-manifest.json (dead config, nothing
consumes them), and update CLAUDE.md's 'Known gaps' section to remove the now-resolved overlap
note. Decision to keep release-drafter over release-please was made explicitly by the user before
this spec was written."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - A maintainer trusts the release-notes config they see (Priority: P1)

A developer opens the repository and looks at what's configured to manage releases. Today they'd
find two competing tools (`release-please-config.json`/`.release-please-manifest.json` alongside
`release-drafter.yml`) and have to investigate which one is actually in effect before trusting
either — exactly what this session had to do. After this change, only one release tool's
configuration exists in the repository, so there's nothing to investigate: what's configured is
what runs.

**Why this priority**: This is the entire scope of the change — a single, self-contained
correction to remove confusion and dead configuration. There is no smaller independently-valuable
slice.

**Independent Test**: Clone the repository fresh and search for release-tooling config files;
exactly one tool's configuration is present, and CLAUDE.md's "Known gaps" no longer lists a
tooling overlap.

**Acceptance Scenarios**:

1. **Given** the repository after this change, **When** a developer searches for release-related
   configuration files, **Then** they find only `release-drafter`'s configuration — no
   `release-please` config or manifest remains.
2. **Given** the repository after this change, **When** a developer reads CLAUDE.md's "Known
   gaps" section, **Then** it no longer mentions a release-drafter/release-please overlap (since
   the overlap no longer exists).
3. **Given** a pull request is opened and merged after this change, **When** CI runs, **Then**
   release-drafter continues drafting release notes exactly as it did before this change — this
   change does not alter its behavior.

---

### Edge Cases

- What happens to the existing (stale, hand-written) `CHANGELOG.md`? It was never produced by
  release-please (Assumptions) and release-drafter doesn't consume or write to it either — it is
  left as-is, since removing or rewriting it is outside this change's scope (it's a separate,
  pre-existing staleness issue, not part of the tooling-overlap gap this change resolves).
- What happens if someone later wants release-please back? Re-adding it (config plus, this time,
  an actual workflow file) is a normal follow-up feature request, not something this change needs
  to guard against or make harder.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The repository MUST NOT contain `release-please` configuration or state files
  (`release-please-config.json`, `.release-please-manifest.json`) after this change.
- **FR-002**: The repository's `release-drafter` configuration and its GitHub Actions workflow
  MUST remain unchanged and continue to function exactly as before this change.
- **FR-003**: CLAUDE.md's "Known gaps" section MUST no longer state that both release-drafter and
  release-please are configured and overlapping, since that will no longer be true.
- **FR-004**: This change MUST NOT modify `CHANGELOG.md` (Edge Cases — out of scope).

### Key Entities

Not applicable — this change touches only configuration files and project documentation, no
application data or runtime entities.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Exactly one release-notes tool's configuration exists in the repository after this
  change (down from two).
- **SC-002**: The next pull request merged after this change still gets a release-drafter-authored
  draft release update in CI, with zero behavior change versus before this change.
- **SC-003**: CLAUDE.md's "Known gaps" list is one item shorter than before this change.

## Assumptions

- `release-please` has never run in this repository: no `.github/workflows/*.yml` file references
  it, and `CHANGELOG.md`'s content (frozen at the initial MVP milestone, not reflecting any of the
  four merged feature PRs since) confirms it was never invoked, manually or otherwise.
- `release-drafter` is the tool actually in effect: its workflow
  (`.github/workflows/release-drafter.yml`) has passed in CI on every recent merged pull request
  in this repository.
- The choice to standardize on `release-drafter` rather than `release-please` was made by the
  project's maintainer directly, before this specification was written — this is not a
  [NEEDS CLARIFICATION] item.
