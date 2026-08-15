# Feature Specification: Project License

**Feature Branch**: `008-project-license`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "Add a LICENSE. CLAUDE.md known gap: no LICENSE chosen yet. License
chosen by the repository owner: Apache 2.0. README.md currently has a 'License' section reading
'Not yet decided.'"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - A visitor can see the project's license terms (Priority: P1)

Someone browsing the repository — a potential contributor, a company evaluating whether they can
use the code, or the maintainer themselves months later — wants to know what they're legally
allowed to do with this code. Today, the README explicitly says the license is "Not yet decided,"
which is worse than having no license section at all: it signals the question was raised and
deliberately left open, discouraging anyone from touching the code until it's resolved.

**Why this priority**: This is the entire scope of the change — publishing a clear, standard
license text and pointing to it from the places people would look. There is no smaller
independently-valuable slice.

**Independent Test**: Open the repository (e.g., on GitHub) and confirm a license file is present
and its terms are unambiguous; open README.md and confirm it states the actual license, not "not
yet decided."

**Acceptance Scenarios**:

1. **Given** the repository after this change, **When** a visitor looks for licensing terms (root
   directory listing, or a hosting platform's automatic license detection), **Then** they find a
   complete, standard, unmodified license text identifying Apache License 2.0.
2. **Given** the repository after this change, **When** a visitor reads README.md's "License"
   section, **Then** it states the project is licensed under Apache License 2.0 and points to the
   license file, instead of "Not yet decided."
3. **Given** the repository after this change, **When** a developer reads CLAUDE.md's "Known
   gaps" section, **Then** it no longer lists "no LICENSE chosen yet" (since it's now resolved).

---

### Edge Cases

- What happens to code/assets contributed under the old "no license decided" state? Not a concern
  for this change — all existing code was authored by the repository owner (per project history),
  who is also the one choosing this license, so there's no third-party contribution to reconcile.
- Does this affect the bundled/dependency licenses (e.g., OpenCV)? No — this change licenses this
  project's own source code; it does not alter the terms of any third-party dependency, which
  remain governed by their own respective licenses regardless of what this project chooses.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The repository MUST contain a complete, unmodified, standard Apache License 2.0
  text, placed where a visitor or a hosting platform's automatic license detection would find it.
- **FR-002**: The license text MUST correctly identify the copyright holder and the year(s) the
  copyright applies to.
- **FR-003**: README.md's "License" section MUST state that the project is licensed under Apache
  License 2.0 (no longer "Not yet decided") and reference the license file.
- **FR-004**: CLAUDE.md's "Known gaps" section MUST no longer state that no license has been
  chosen, since that will no longer be true.

### Key Entities

Not applicable — this change adds a static legal document and updates two references to it; no
application data or runtime entities are involved.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A visitor can determine the project's license terms within seconds of opening the
  repository, with zero ambiguity (down from an explicit "not yet decided" today).
- **SC-002**: README.md's "License" section and CLAUDE.md's "Known gaps" section are both
  internally consistent with the actual license now present in the repository.

## Assumptions

- The copyright holder is the repository owner, Leonardo Fernandes (per this session's git
  configuration), and the copyright year is 2026 (per this session's current date) — this is not
  a [NEEDS CLARIFICATION] item; it follows directly from who is doing the choosing and when.
- Apache License 2.0 was explicitly chosen by the repository owner before this specification was
  written (this session's prior turn) — not a decision this feature needs to make or revisit.
- This change does not add a `NOTICE` file or a third-party-attribution audit. The project's one
  major third-party dependency, OpenCV (`org.opencv:opencv` via Maven Central), is itself licensed
  under Apache License 2.0 (as of OpenCV 4.5+, which this project's pinned 5.x version postdates)
  — the same license this project is choosing, so there is no license-compatibility conflict to
  resolve. Whether bundling OpenCV's compiled Android library into the app's APK obligates
  reproducing its own `NOTICE` file is a separate, narrower compliance question independent of
  choosing *this* project's own license (the scope of this feature) and is left for a dedicated
  follow-up if ever needed.
