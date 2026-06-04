---
name: Git
description: Tool for Git version control operations — branching, tagging, and release management in backbone-rest
type: terminal
command-prefix: git
used-by: [DevOps Engineer, Project Manager]
---

# Git Tool

## Purpose

Manage source control, release tags, and branch operations for **backbone-rest**.

## Commands

### Branches

```bash
# List branches
git branch -a

# Create and switch to a feature branch
git checkout -b feature/US-001-add-user-endpoint

# Switch branch
git checkout develop

# Delete a local branch
git branch -d feature/US-001-add-user-endpoint

# Push branch to remote
git push origin feature/US-001-add-user-endpoint
```

### Release Tags

```bash
# Create annotated release tag
git tag -a v1.2.0 -m "Release v1.2.0 — add feature X, fix Y"

# Push tag to remote
git push origin v1.2.0

# List all tags
git tag --sort=-version:refname

# Delete a tag locally and remotely (if created by mistake)
git tag -d v1.2.0
git push origin --delete v1.2.0
```

### Status and History

```bash
# Short status
git status -s

# Recent commits (one-line)
git log --oneline -10

# Commits since last tag
git log $(git describe --tags --abbrev=0)..HEAD --oneline

# Show changed files in last commit
git show --stat HEAD
```

### CHANGELOG Pre-release Check

```bash
# Show all commits since last release tag to populate CHANGELOG
git log $(git describe --tags --abbrev=0)..HEAD --pretty=format:"- %s" | sort
```

## Branching Convention

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code — tagged releases only |
| `develop` | Integration branch — merge features here first |
| `feature/US-XXX-*` | Individual feature/fix work |
| `hotfix/v*` | Emergency production fixes |

## Release Tag Convention

```
v{MAJOR}.{MINOR}.{PATCH}

Examples:
  v1.0.0  — initial release
  v1.1.0  — new backward-compatible feature
  v1.1.1  — bug fix / security patch
  v2.0.0  — breaking change (stakeholder approval required)
```

## Notes

- Tag only from `main` branch after full quality gate passes (`mvn test` green, PMD clean).
- Always update `CHANGELOG` before tagging.
- Coordinate tags with the **GitHub CLI** `gh release create` command for GitHub releases.

