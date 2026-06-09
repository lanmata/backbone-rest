---
name: Pre-PR Quality Gate
description: Hook triggered before opening a pull request — runs quality gates and reviews
trigger: before-pull-request
agents: [code-reviewer, api-reviewer]
auto-block: true
---

# Pre-PR Quality Gate Hook

Triggered automatically before a pull request is opened against `main` or `develop`.

## Trigger Conditions

- Branch: any `feature/*`, `fix/*`, `hotfix/*`
- Target base: `main` or `develop`

## Automated Steps (sequential — stop on failure)

### Step 1 — Build Gate

```bash
mvn -DskipTests compile
```
**Fail condition**: any compile error → block PR creation immediately.

### Step 2 — PMD Gate

```bash
mvn pmd:check pmd:cpd-check
```
**Fail condition**: any PMD/CPD violation → block PR; list violations in PR comment.

### Step 3 — Test Gate

```bash
mvn test
```
**Fail condition**: any test failure → block PR; attach JaCoCo summary.

### Step 4 — Code Review (agent: code-reviewer)

Invoke `code-reviewer` with changed files list:
- Prompt: `.github/prompts/review-pull-request.prompt.md`
- Input: `changedFiles` = files changed since branch diverged from base

**Fail condition**: any CRITICAL issue → request changes on PR.

### Step 5 — API Contract Review (agent: api-reviewer)

If any `*Api.java` or `backbone_rest-openapi.yaml` was changed:
- Prompt: `.github/prompts/review-api-contract.prompt.md`

**Fail condition**: breaking change detected or spec out of sync → block merge.

## Output

Post a PR comment with:
```
## Pre-PR Quality Gate

| Gate | Status | Details |
|------|--------|---------|
| Compile | PASS/FAIL | ... |
| PMD | PASS/FAIL | N violations |
| Tests | PASS/FAIL | N failures |
| Code Review | APPROVED/CHANGES_REQUESTED | ... |
| API Contract | PASS/FAIL (if applicable) | ... |

### Overall: READY TO MERGE / CHANGES REQUIRED
```

