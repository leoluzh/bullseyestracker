#!/usr/bin/env python3
"""Append a bullet to CHANGELOG.md's [Unreleased] section for a merged PR.

Invoked by .github/workflows/changelog.yml right after a PR merges to master.
Categorizes by the PR title's conventional-commit prefix (same prefixes
.github/release-drafter.yml's autolabeler matches) and inserts the new bullet
into the matching "### <Section>" heading, creating it if absent. "### Known
gaps" is hand-maintained and always stays last.
"""
import os
import re
import sys

CHANGELOG_PATH = "CHANGELOG.md"

# Ordered so newly-created sections land in a consistent, sensible position.
SECTION_ORDER = ["Added", "Changed", "Fixed", "Documentation", "Maintenance"]

PREFIX_TO_SECTION = {
    "feat": "Added",
    "fix": "Fixed",
    "docs": "Documentation",
    "refactor": "Changed",
    "perf": "Changed",
    "style": "Changed",
    "chore": "Maintenance",
    "test": "Maintenance",
    "build": "Maintenance",
    "ci": "Maintenance",
}


def categorize(title: str) -> str:
    match = re.match(r"^(\w+)(\(.+\))?!?:\s*", title)
    if match:
        prefix = match.group(1).lower()
        if prefix in PREFIX_TO_SECTION:
            return PREFIX_TO_SECTION[prefix]
    return "Maintenance"


def clean_title(title: str) -> str:
    title = re.sub(r"^(\w+)(\(.+\))?!?:\s*", "", title).strip()
    if title:
        title = title[0].upper() + title[1:]
    title = title.rstrip(".")
    return title


def find_section_bounds(lines: list[str], start: int, end: int, heading: str):
    """Return (heading_idx, section_end_idx) for '### {heading}' within lines[start:end], or (None, None)."""
    target = f"### {heading}"
    for i in range(start, end):
        if lines[i].strip() == target:
            j = i + 1
            while j < end and not lines[j].startswith("## ") and not lines[j].startswith("### "):
                j += 1
            return i, j
    return None, None


def main() -> None:
    pr_number = os.environ["PR_NUMBER"]
    pr_title = os.environ["PR_TITLE"]
    pr_url = os.environ["PR_URL"]

    section = categorize(pr_title)
    bullet_text = clean_title(pr_title)
    bullet = f"- {bullet_text} ([#{pr_number}]({pr_url})).\n"

    with open(CHANGELOG_PATH, encoding="utf-8") as f:
        lines = f.readlines()

    unreleased_idx = None
    for i, line in enumerate(lines):
        if line.strip() == "## [Unreleased]":
            unreleased_idx = i
            break
    if unreleased_idx is None:
        print("No '## [Unreleased]' heading found in CHANGELOG.md", file=sys.stderr)
        sys.exit(1)

    unreleased_end = len(lines)
    for i in range(unreleased_idx + 1, len(lines)):
        if lines[i].startswith("## "):
            unreleased_end = i
            break

    heading_idx, section_end = find_section_bounds(lines, unreleased_idx + 1, unreleased_end, section)

    if heading_idx is not None:
        # Insert after the last non-blank line in the existing section (i.e. after the last bullet).
        insert_at = section_end
        while insert_at > heading_idx + 1 and lines[insert_at - 1].strip() == "":
            insert_at -= 1
        lines[insert_at:insert_at] = [bullet]
    else:
        # Create the section. Place it in SECTION_ORDER position, before "### Known gaps"
        # if present, else at the end of the Unreleased block.
        known_gaps_idx = None
        for i in range(unreleased_idx + 1, unreleased_end):
            if lines[i].strip() == "### Known gaps":
                known_gaps_idx = i
                break

        boundary = known_gaps_idx if known_gaps_idx is not None else unreleased_end
        # Walk backwards past any trailing blank lines so the block's own blank-line
        # separators are the only ones left, instead of stacking with pre-existing ones.
        run_start = boundary
        while run_start > unreleased_idx + 1 and lines[run_start - 1].strip() == "":
            run_start -= 1

        block = ["\n", f"### {section}\n", "\n", bullet, "\n"]
        lines[run_start:boundary] = block

    with open(CHANGELOG_PATH, "w", encoding="utf-8") as f:
        f.writelines(lines)

    print(f"Added to ### {section}: {bullet.strip()}")


if __name__ == "__main__":
    main()
