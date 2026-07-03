---
name: Prepare Release
description: Run the full release checklist for backbone-rest — quality gates, CHANGELOG, Docker build, and version tagging
mode: agent
agent: devops-engineer
tools: [Bash, Read, Edit]
---

Prepare a release for **backbone-rest**.

## Release Version

- Version: ${version}
- Target branch: ${targetBranch}
_(Default: `develop`)_

## Step 1 — Quality Gates (sequential — stop on failure)

```bash
# 1. Compile
mvn -DskipTests compile

# 2. PMD
mvn pmd:check pmd:cpd-check

# 3. Tests + JaCoCo
mvn test

# 4. Package
mvn -DskipTests package
```

## Step 2 — OpenAPI Sync Check

```bash
# Verify api.yaml is in sync with *Api.java
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping" \
  src/main/java --include="*Api.java" | awk -F'"' '{print $2}' | sort
grep "^  /" src/main/resources/META-INF/api.yaml | sort
```

Report any paths in `*Api.java` not present in `api.yaml`.

## Step 3 — CHANGELOG

Read `CHANGELOG.new` (or `CHANGELOG_NEW`) and merge into `CHANGELOG`:
- Format: `## [${version}] — YYYY-MM-DD`
- Sections: Added, Changed, Fixed, Security

## Step 4 — Docker Build

```bash
docker build -t backbone-rest:${version} .
docker build -t backbone-rest:latest .
```

Verify container starts:
```bash
docker run --rm -p 8082:8082 backbone-rest:${version} &
sleep 5 && curl -f http://localhost:8082/actuator/health || echo "HEALTH CHECK FAILED"
```

## Step 5 — Pre-Release Checklist

- [ ] Compile: PASS
- [ ] PMD: 0 violations
- [ ] Tests: 0 failures
- [ ] JaCoCo: thresholds met
- [ ] `api.yaml` in sync
- [ ] CHANGELOG updated
- [ ] Docker image builds
- [ ] No secrets in committed files
- [ ] `default.env` contains stubs only

## Output

```markdown
## Release Report — v${version}

| Gate | Status | Notes |
|------|--------|-------|
| Compile | PASS/FAIL | |
| PMD | PASS/FAIL | N violations |
| Tests | PASS/FAIL | N failures |
| JaCoCo | PASS/FAIL | % coverage |
| OpenAPI sync | PASS/FAIL | mismatches |
| Docker build | PASS/FAIL | image size |

### Ready to release: YES / NO
### Blocking issues: ...
```
