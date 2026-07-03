---
name: Pre-Release Gate
description: Blocking hook triggered before a release tag — all quality gates must pass before tagging
trigger: before-release-tag
agents:
  - devops-engineer
  - security-auditor
  - api-designer
auto-block: true
---

# Pre-Release Gate Hook

Triggered before any `v*` release tag is created on `develop` or `main`. All gates must pass — the tag is blocked if any fail.

## Trigger Conditions

- Event: `git tag v*`
- Branch: `develop` or `main`

## Steps (sequential — stop on first failure)

### Step 1 — Compile Gate
```bash
mvn -DskipTests compile
```

### Step 2 — PMD Gate (zero violations)
```bash
mvn pmd:check pmd:cpd-check
```

### Step 3 — Test Gate (zero failures)
```bash
mvn test
```
Attach JaCoCo summary to release notes.

### Step 4 — Package Gate
```bash
mvn -DskipTests package
ls -lh target/backbone-rest.jar
```

### Step 5 — Docker Build Gate
```bash
docker build -t backbone-rest:${VERSION} .
```

### Step 6 — OpenAPI Sync Check (agent: api-designer)
Invoke `api-designer` agent:
- Prompt: `.claude/prompts/review-api-contract.prompt.md`
- Scope: all `*Api.java` files vs. `api.yaml`

**Fail condition**: any endpoint in `*Api.java` not documented in `api.yaml`.

### Step 7 — Security Pre-Release Audit (agent: security-auditor)
Invoke `security-auditor` agent:
- Prompt: `.claude/prompts/security-audit.prompt.md`
- Scope: full codebase

**Fail condition**: any HIGH or CRITICAL finding.

### Step 8 — Secrets Hygiene Check
```bash
grep -rn "password\|secret\|apikey" src/main/resources/ --include="*.yml"
cat default.env | grep -v "^#\|^$\|=<\|=$"
```
**Fail condition**: any real secret found.

## Fail Behavior

- Block tag creation immediately.
- Report all failing gates in summary.
- Do not partially pass — all gates must be GREEN.

## Output

```markdown
## Pre-Release Gate — v<version>

| Gate | Status | Details |
|------|--------|---------|
| Compile | PASS/FAIL | |
| PMD | PASS/FAIL | N violations |
| Tests | PASS/FAIL | N failures |
| Package | PASS/FAIL | JAR size |
| Docker build | PASS/FAIL | image built |
| OpenAPI sync | PASS/FAIL | N mismatches |
| Security audit | PASS/FAIL | N findings |
| Secrets hygiene | PASS/FAIL | |

### Release Decision: APPROVED / BLOCKED
### Blocking issues:
- ...
```
