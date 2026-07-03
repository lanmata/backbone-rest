---
name: Release Management
description: Shared — release checklist, quality gate sequence, CHANGELOG conventions, Docker packaging (project-manager, devops-engineer)
applies-to:
  - project-manager
  - devops-engineer
---

# Shared Skill — Release Management

## Release Quality Gate Order

Run in sequence — stop on first failure:

```bash
# 1. Compile
mvn -DskipTests compile

# 2. PMD
mvn pmd:check pmd:cpd-check

# 3. Tests + JaCoCo
mvn test

# 4. Package
mvn -DskipTests package

# 5. Docker build
docker build -t backbone-rest:<version> .
```

## CHANGELOG Convention

File: `CHANGELOG` (no extension) at repo root.

Entry format:
```
## [<version>] — <YYYY-MM-DD>

### Added
- ...

### Changed
- ...

### Fixed
- ...

### Security
- ...
```

Staging area: `CHANGELOG.new` — merge into `CHANGELOG` at release.

## Docker Release

```bash
docker build -t backbone-rest:<version> .
docker tag backbone-rest:<version> backbone-rest:latest
```

Base: `amazoncorretto:21-alpine3.20`, port 8082.

## Pre-Release Checklist

- [ ] All acceptance criteria verified
- [ ] 0 PMD violations
- [ ] 0 test failures
- [ ] JaCoCo thresholds met
- [ ] `api.yaml` in sync with `*Api.java`
- [ ] `CHANGELOG` updated
- [ ] No secrets in git
- [ ] `default.env` contains stubs only
- [ ] Docker image builds and runs on port 8082
- [ ] Private deps resolved (`REPSY_ACCOUNT_USER`, `REPSY_ACCOUNT_PASSWORD` set)
