---
name: Pre-PR Quality Gate
description: Hook triggered before opening a pull request — runs compile, PMD, tests, code review, and API contract review
trigger: before-pull-request
agents:
  - code-reviewer
  - api-designer
auto-block: true
---

# Pre-PR Quality Gate Hook

Triggered before opening a pull request targeting `develop` or `main` from any `feature/*`, `fix/*`, or `hotfix/*` branch.

## Trigger Conditions

- Branch: `feature/*`, `fix/*`, `hotfix/*`
- Target base: `develop` or `main`

## Steps (sequential — stop on first failure)

### Step 1 — Compile Gate
```bash
mvn -DskipTests compile
```
**Fail condition**: any compile error → block PR immediately.

### Step 2 — PMD Gate
```bash
mvn pmd:check pmd:cpd-check
```
**Fail condition**: any PMD/CPD violation → block PR; list violations.

### Step 3 — Test Gate
```bash
mvn test
```
**Fail condition**: any test failure → block PR; attach JaCoCo summary.

### Step 4 — Code Review
Invoke `code-reviewer` agent with:
- Prompt: `.claude/prompts/review-code.prompt.md`
- Input: `changedFiles` = `git diff develop...HEAD --name-only | grep "\.java$"`

**Fail condition**: any CRITICAL issue → request changes.

### Step 5 — API Contract Review (conditional)
If `*Api.java` or `api.yaml` changed:
- Invoke `api-designer` agent
- Prompt: `.claude/prompts/review-api-contract.prompt.md`

**Fail condition**: breaking change or spec out of sync → block merge.

## Fail Behavior

- Any gate failure: add `status: BLOCKED` label to PR.
- Post summary comment on PR with gate results table.
- Do NOT proceed to next gate after a failure.

## Output

```markdown
## Pre-PR Quality Gate

| Gate | Status | Details |
|------|--------|---------|
| Compile | PASS/FAIL | |
| PMD | PASS/FAIL | N violations |
| Tests | PASS/FAIL | N failures, JaCoCo: X% |
| Code Review | APPROVED/CHANGES_REQUESTED | N issues |
| API Contract | PASS/FAIL/SKIPPED | mismatches |

### Overall: READY TO MERGE / CHANGES REQUIRED
```
