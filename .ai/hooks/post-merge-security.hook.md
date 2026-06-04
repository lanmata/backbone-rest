---
name: Post-Merge Security Scan
description: Hook triggered after merge to develop — runs security and dependency audit
trigger: after-merge
branch: develop
agents: [security-reviewer]
auto-block: false
---

# Post-Merge Security Scan Hook

Triggered automatically after a PR is merged into `develop`.

## Trigger Conditions

- Event: merge to `develop`
- Condition: `pom.xml` or `bootstrap.yml` was changed (dependency/config update)

## Steps

### Step 1 — Dependency Audit

```bash
mvn dependency:tree
mvn versions:display-dependency-updates
```

### Step 2 — CVE Scan (agent: security-reviewer)

Invoke `security-reviewer`:
- Prompt: `.github/prompts/security-audit.prompt.md`
- Input: `focus = dependencies`

### Step 3 — Secrets Check

```bash
grep -rn "password\s*=\s*['\"]" src/main/resources/
grep -rn "secret\s*=\s*['\"]" src/main/resources/
```

## Fail Behavior

- **Critical/High CVE found** → create a GitHub issue labeled `security` with CVE details; notify Project Manager.
- **Secret exposed** → block further merges to `main`; notify immediately.
- **Medium CVE** → create issue labeled `security` + `tech-debt`; non-blocking.

## Output

```
## Security Scan — post-merge

| Check | Status | Details |
|-------|--------|---------|
| Dependency audit | PASS/FAIL | ... |
| CVEs | NONE / list | severity + fixed-in |
| Secrets | CLEAN / EXPOSED | file:line |

### Action Required: YES / NO
```

