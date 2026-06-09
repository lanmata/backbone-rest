---
name: Post-Implementation Review
description: Hook triggered after a developer completes an implementation — triggers tests and reviews in parallel
trigger: after-implementation
agents: [test-writer, api-reviewer, code-reviewer]
auto-block: false
---

# Post-Implementation Review Hook

Triggered when a developer marks an implementation as complete (e.g., pushes to a feature branch).

## Trigger Conditions

- Event: push to `feature/*` or `fix/*` branch
- Condition: Java source files changed under `src/main/java`

## Parallel Steps (all run simultaneously)

### A — Test Coverage Check (agent: test-writer)

If no test file exists for changed service/controller:
- Prompt: `.github/prompts/write-service-tests.prompt.md`
- Input: `serviceClass` = changed `*ServiceImpl.java`

If test file exists but coverage is below target:
- Prompt: `.github/prompts/improve-coverage.prompt.md`
- Input: `target` = changed class

### B — API Contract Review (agent: api-reviewer)

If any `*Api.java` was changed:
- Prompt: `.github/prompts/review-api-contract.prompt.md`
- Input: `domain` = affected domain

### C — Code Review (agent: code-reviewer)

Always:
- Prompt: `.github/prompts/review-pull-request.prompt.md`
- Input: `changedFiles` = all Java files changed in push

## Consolidation

After all parallel steps complete, produce a summary comment on the branch:

```
## Post-Implementation Review

| Agent | Task | Status | Key Output |
|-------|------|--------|-----------|
| test-writer | Coverage | PASS/PARTIAL | X% coverage |
| api-reviewer | Contract | PASS/FAIL | issues list |
| code-reviewer | Quality | APPROVED/CHANGES | issues list |

### Recommended Action: READY FOR PR / FIX REQUIRED
```

