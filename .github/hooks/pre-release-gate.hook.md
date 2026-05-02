---
name: Pre-Release Gate
description: Hook triggered before tagging a release — full quality + security + OpenAPI validation
trigger: before-release-tag
agents: [security-reviewer, api-reviewer, code-reviewer, devops-engineer]
auto-block: true
---

# Pre-Release Gate Hook

Triggered before creating a release tag (`v*`) on `main`.

## Trigger Conditions

- Event: `git tag v*` on `main` branch

## Sequential Steps (stop and block on any failure)

### Step 1 — Full Quality Gate

```bash
mvn test                      # tests + PMD + JaCoCo
mvn pmd:check pmd:cpd-check   # explicit double-check
```

### Step 2 — Security Review (agent: security-reviewer)

- Prompt: `.github/prompts/security-audit.prompt.md`
- Input: `focus = full`
- **Block condition**: any Critical or High CVE → tag blocked.

### Step 3 — API Contract Validation (agent: api-reviewer)

- Prompt: `.github/prompts/review-api-contract.prompt.md`
- Input: `domain =` all domains
- **Block condition**: spec out of sync with code → tag blocked.

### Step 4 — OpenAPI Endpoint Count Sync Check

```bash
# Java endpoints
grep -rn '@\(Get\|Post\|Put\|Patch\|Delete\)Mapping' \
  src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java | wc -l

# YAML operationIds
grep -c "operationId:" src/main/resources/META-INF/backbone_rest-openapi.yaml
```
**Block condition**: counts differ.

### Step 5 — CHANGELOG Entry Exists

```bash
grep "\[${version}\]" CHANGELOG
```
**Block condition**: no CHANGELOG entry for this version.

### Step 6 — Docker Build Verification (agent: devops-engineer)

```bash
mvn -DskipTests package
docker build -t lamata/backbone-rest:${version} .
```
**Block condition**: Docker build failure.

## Output

```
## Pre-Release Gate — v${version}

| Gate | Status | Detail |
|------|--------|--------|
| Tests + PMD | PASS/FAIL | ... |
| Security | PASS/FAIL | CVEs found |
| API Contract | PASS/FAIL | sync issues |
| OpenAPI count | PASS/FAIL | expected/actual |
| CHANGELOG | PASS/FAIL | entry present |
| Docker build | PASS/FAIL | ... |

### RELEASE: APPROVED / BLOCKED
```

