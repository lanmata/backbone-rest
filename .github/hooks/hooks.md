# Hooks Catalog

Event-driven hooks that trigger agent tasks automatically for **backbone-rest**.

## Hooks Available

| File | Name | Trigger | Agents | Blocking |
|------|------|---------|--------|---------|
| `pre-pull-request.hook.md` | Pre-PR Quality Gate | Before opening PR to `main`/`develop` | code-reviewer, api-reviewer | Yes — blocks PR if failures |
| `post-merge-security.hook.md` | Post-Merge Security Scan | After merge to `develop` (if pom.xml changed) | security-reviewer | No — creates issues |
| `pre-release-gate.hook.md` | Pre-Release Gate | Before `git tag v*` on `main` | security-reviewer, api-reviewer, code-reviewer, devops-engineer | Yes — blocks tag |
| `post-implementation-review.hook.md` | Post-Implementation Review | Push to `feature/*` or `fix/*` (Java changed) | test-writer, api-reviewer, code-reviewer | No — advisory comment |

## Hook Lifecycle

```
Developer pushes code
       │
       ▼
[post-implementation-review]  ← advisory: coverage + review + API check (parallel)
       │
       ▼
Developer opens PR
       │
       ▼
[pre-pull-request]  ← blocking: compile + PMD + tests + code review + API review
       │
       ▼
PR merged to develop
       │
       ▼
[post-merge-security]  ← non-blocking: CVE + secrets scan (if pom.xml changed)
       │
       ▼
Release tag created
       │
       ▼
[pre-release-gate]  ← blocking: full quality + security + API + CHANGELOG + Docker
```

## Implementing Hooks as GitHub Actions

Each hook maps to a GitHub Actions workflow trigger:

| Hook | GitHub Actions Event |
|------|---------------------|
| `pre-pull-request` | `pull_request` → `opened`, `synchronize` |
| `post-merge-security` | `push` to `develop` with path filter `pom.xml` |
| `pre-release-gate` | `create` event with tag `v*` |
| `post-implementation-review` | `push` to `feature/**` or `fix/**` with path `src/main/java/**` |

See `.github/tools/maven-build.tool.md` and `.github/tools/github-cli.tool.md`
for the commands used in these hooks.

