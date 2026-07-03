---
name: Post-Merge Security Scan
description: Non-blocking hook triggered after merge to develop when dependencies changed — runs CVE and secrets audit
trigger: post-merge-develop
agents:
  - security-auditor
auto-block: false
---

# Post-Merge Security Scan Hook

Triggered after a merge to `develop` when `pom.xml` or `default.env` was part of the merged changes.

## Trigger Conditions

- Event: merge to `develop`
- Condition: `pom.xml` OR `default.env` OR `src/main/resources/*.yml` changed in the merge

## Steps

### Step 1 — Dependency CVE Check
```bash
mvn dependency:tree | grep -E "snakeyaml|woodstox|commons-fileupload|spring-security|tomcat|jackson"
```
Compare versions against known CVE advisories.

### Step 2 — Secrets Hygiene Check
```bash
# Check for accidentally committed secrets
grep -rn "password\|secret\|apikey" src/main/resources/ --include="*.yml" --include="*.yaml"
grep -rn "password\|secret" default.env
git log -1 --diff-filter=A --name-only  # newly added files
```

### Step 3 — Full Security Audit (agent: security-auditor)
If Step 1 or Step 2 finds any issues:
- Invoke `security-auditor` agent
- Prompt: `.claude/prompts/security-audit.prompt.md`
- Scope: dependency changes + secrets hygiene

## Fail Behavior

- Non-blocking — merge is not reverted.
- If CRITICAL CVE found: create a follow-up issue and tag it `security:critical`.
- If secret committed: alert immediately and recommend `git filter-repo` cleanup.
- Post summary as a comment on the merge commit.

## Output

```markdown
## Post-Merge Security Scan — develop (<commit-sha>)

| Check | Status | Notes |
|-------|--------|-------|
| Dependency CVEs | PASS/WARN/FAIL | list CVEs |
| Secrets hygiene | PASS/FAIL | list issues |
| pom.xml changes | REVIEWED | N new deps |

### Actions Required
- [ ] ...

### Overall: CLEAN / REVIEW_REQUIRED / CRITICAL
```
