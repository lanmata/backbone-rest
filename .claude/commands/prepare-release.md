# Prepare Release — backbone-rest

Run the full release checklist: version bump, full test suite, PMD gate, Docker build
verification, CHANGELOG entry, and a go/no-go decision.

## Usage
```
/prepare-release
```
Provide the release version, e.g.: `/prepare-release 1.2.0`

---

## Steps Claude Will Execute

### 1. Confirm Current State
```bash
git status
git log develop..HEAD --oneline  # commits since last release
mvn help:evaluate -Dexpression=project.version -q -DforceStdout  # current version
```

### 2. Version Bump (if not already bumped)
In `pom.xml`, update `<version>` to the release version:
```bash
mvn versions:set -DnewVersion=<version> -DgenerateBackupPoms=false
```

### 3. Full Test Suite
```bash
mvn test
```
**Stop if any test fails** — do not proceed with failing tests.

### 4. PMD Gate
```bash
mvn pmd:check pmd:cpd-check
```
**Stop if violations found.**

### 5. JaCoCo Report
```bash
# Already run in step 3; check overall coverage
grep "TOTAL" target/site/jacoco/index.html | head -3
```

### 6. Package Artifact
```bash
mvn -DskipTests package
ls -lh target/*.jar
```

### 7. Docker Build Check
```bash
docker build -t backbone-rest:<version> . 2>&1 | tail -10
```

### 8. Security Quick Check
```bash
grep -rn "CrossOrigin\|println\|TODO\|FIXME\|HACK" src/main/java/ --include="*.java" | grep -v test | head -20
```

### 9. CHANGELOG Entry
Append to `CHANGELOG` file:
```
## [<version>] - <date>

### Added
- <feature from commit log>

### Fixed
- <bug fix from commit log>

### Changed
- <changed behavior>

### Security
- <security improvements>
```

### 10. OpenAPI Version Sync
Update `info.version` in `../../src/main/resources/META-INF/api.yaml`
to match the release version.

---

## Go / No-Go Criteria
| Gate | Required |
|------|----------|
| Tests: 0 failures | ✅ mandatory |
| PMD: 0 violations | ✅ mandatory |
| Docker build succeeds | ✅ mandatory |
| JaCoCo ≥ 60% line coverage | ⚠️ warn if below |
| No FIXME/TODO in changed files | ⚠️ warn |
| CHANGELOG updated | ✅ mandatory |

## Output
```markdown
## Release Checklist — v<version>

| Gate | Status | Detail |
|------|--------|--------|
| Tests | PASS/FAIL | N passed / N failed |
| PMD | PASS/FAIL | N violations |
| Coverage | %% | line / branch |
| Package | PASS/FAIL | target/<name>.jar |
| Docker | PASS/FAIL | |
| CHANGELOG | UPDATED | |

### Decision: GO / NO-GO
### Blockers (if NO-GO): <list>
```
