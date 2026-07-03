# Hooks Catalog — backbone-rest (Claude)

## All Hooks

| File | Trigger | Blocking | Agents |
|------|---------|---------|--------|
| `pre-pull-request.hook.md` | Before PR opened to `develop`/`main` | ✅ Yes | code-reviewer, api-designer |
| `post-implementation-review.hook.md` | After push to `feature/*` or `fix/*` | ❌ No | code-reviewer, test-writer |
| `post-merge-security.hook.md` | After merge to `develop` (when `pom.xml`/config changed) | ❌ No | security-auditor |
| `pre-release-gate.hook.md` | Before `v*` release tag created | ✅ Yes | devops-engineer, security-auditor, api-designer |

## Lifecycle Diagram

```
push feature/fix branch
    └─► [post-implementation-review.hook] (non-blocking)
            ├─ PMD quick scan
            ├─ Coverage report
            ├─ Code review (code-reviewer)
            └─ Test gap check (test-writer)

open PR → develop / main
    └─► [pre-pull-request.hook] (BLOCKING)
            ├─ Compile gate
            ├─ PMD gate
            ├─ Test gate
            ├─ Code review (code-reviewer)
            └─ API contract review (api-designer) [if *Api.java changed]

merge to develop
    └─► [post-merge-security.hook] (non-blocking) [if pom.xml/config changed]
            ├─ CVE dependency check
            ├─ Secrets hygiene check
            └─ Security audit (security-auditor) [if issues found]

git tag v*
    └─► [pre-release-gate.hook] (BLOCKING)
            ├─ Compile gate
            ├─ PMD gate
            ├─ Test gate
            ├─ Package gate
            ├─ Docker build gate
            ├─ OpenAPI sync (api-designer)
            ├─ Security audit (security-auditor)
            └─ Secrets hygiene check
```

## Prompts Referenced by Hooks

| Hook | Prompt |
|------|--------|
| `pre-pull-request.hook.md` | `.claude/prompts/review-code.prompt.md`, `.claude/prompts/review-api-contract.prompt.md` |
| `post-implementation-review.hook.md` | `.claude/prompts/review-code.prompt.md`, `.claude/prompts/improve-coverage.prompt.md` |
| `post-merge-security.hook.md` | `.claude/prompts/security-audit.prompt.md` |
| `pre-release-gate.hook.md` | `.claude/prompts/review-api-contract.prompt.md`, `.claude/prompts/security-audit.prompt.md` |
