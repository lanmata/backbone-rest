---
name: Git
description: Git workflow conventions for backbone-rest — branching, commit style, PR flow
type: terminal
command-prefix: git
used-by:
  - java-developer
  - devops-engineer
  - project-manager
---

# Git Tool

## Purpose

Source control operations for the **backbone-rest** project.

## Branch Conventions

| Branch | Pattern | Purpose |
|--------|---------|---------|
| Main | `develop` | Integration branch — PRs target here |
| Feature | `feature/<ticket-id>-<slug>` | New features |
| Bug fix | `fix/<ticket-id>-<slug>` | Bug fixes |
| Hotfix | `hotfix/<slug>` | Critical production fixes |

Example: `feature/ds-196-include-supabase-storage`

## Common Commands

```bash
# Create feature branch
git checkout -b feature/ds-XXX-my-feature

# Stage specific files (never git add -A blindly)
git add src/main/java/com/umdc/backoffice/v1/users/

# Commit
git commit -m "feat(users): add email uniqueness check endpoint"

# Check status
git status

# View recent commits
git log --oneline -10

# Diff staged changes
git diff --staged

# Push branch
git push -u origin feature/ds-XXX-my-feature
```

## Commit Message Format

```
<type>(<scope>): <short description>

<body — why, not what>
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`
Scope: domain module name (`users`, `roles`, `session`, `profileimage`, etc.)

## Safety Rules

- Never `git add -A` — stage specific files to avoid committing `default.env`, `.DS_Store`, or keystores.
- Never force-push to `develop` or `main`.
- Never commit: `keystore.jks`, `backbone.jks`, `umdc-truststore.jks`, `*.crt`, real `.env` values.
- Always run `mvn pmd:check` + `mvn test` before pushing.
