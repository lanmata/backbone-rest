---
name: Post-Implementation Review
description: Non-blocking hook triggered after a push to a feature branch — provides code quality feedback and coverage report
trigger: post-push-feature-branch
agents:
  - code-reviewer
  - test-writer
auto-block: false
---

# Post-Implementation Review Hook

Triggered after a push to any `feature/*` or `fix/*` branch. Provides early feedback without blocking the push.

## Trigger Conditions

- Event: `git push`
- Branch pattern: `feature/*`, `fix/*`

## Steps (parallel — do not block)

### Step A — PMD Quick Scan
```bash
mvn pmd:check pmd:cpd-check 2>&1 | tail -20
```
Report violations found (non-blocking — for developer awareness).

### Step B — Coverage Report
```bash
mvn test jacoco:report
```
Parse `target/site/jacoco/index.html` and report coverage for changed classes.

### Step C — Code Review (agent: code-reviewer)
Invoke `code-reviewer` with:
- Prompt: `.claude/prompts/review-code.prompt.md`
- Input: `changedFiles` = files changed in last commit

### Step D — Test Gap Check (agent: test-writer)
Check if any new public methods in changed service/controller files lack test coverage.
- Prompt: `.claude/prompts/improve-coverage.prompt.md` (identify gaps only, do not write tests)

## Fail Behavior

- Non-blocking — push proceeds regardless.
- Post feedback as a comment on the branch's open PR (if exists), or write a summary to stdout.
- Log issues for developer review; do not create blockers.

## Output

```markdown
## Post-Implementation Review — <branch>

| Check | Result | Notes |
|-------|--------|-------|
| PMD | PASS/WARN | N violations |
| Tests | PASS/WARN | N failures |
| Coverage | <X>% | changed classes |
| Code Review | APPROVED/WARN | N issues |
| Test Gaps | <N methods> | uncovered |

### Early Feedback (non-blocking)
- ...
```
