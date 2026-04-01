---
name: Release Management
description: Skill for coordinating releases and quality gates
applies-to:
  - Project Manager
  - DevOps Engineer
---

# Release Management Skill

## Scope

This skill covers release coordination, quality gate validation, and deployment
processes for the **backbone-rest** project.

## Quality Gates

### Required Before Release

1. **Build** — `mvn test` passes (PMD violations fail the build at `test` phase)

2. **PMD** — No violations in `ruleset.xml`:
   - Violations at any priority → build fails
   - Run: `mvn pmd:check pmd:cpd-check`

3. **Coverage** — JaCoCo report generated (minimum is 0; ensure service classes have meaningful coverage):
   - Run: `mvn test jacoco:report`
   - Review: `target/site/jacoco/index.html`

4. **Security** — Dependency audit:
   - No critical/high CVEs unmitigated in `pom.xml` dependencies

5. **Documentation** — Updated:
   - `CHANGELOG` (project root)
   - OpenAPI spec (`backbone_rest-openapi.yaml`) for any API changes

## Release Process

1. Run full build: `mvn test` (PMD + JaCoCo + unit tests)
2. Security audit via **Security Reviewer** subagent
3. API contract review via **API Reviewer** subagent
4. Update `CHANGELOG` at project root
5. Tag release version: `git tag -a v[X.Y.Z] -m "Release [X.Y.Z]"`
6. Publish artifact: `mvn -DskipTests deploy` (requires `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD`)
7. Build and push Docker image:
   ```bash
   mvn -DskipTests package
   docker build -t lamata/backbone-rest:[X.Y.Z] .
   docker push lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]
   ```
8. Deploy and verify via actuator endpoints

## Versioning

Current artifact version in `pom.xml`: `0.0.2`
Follow semantic versioning: `MAJOR.MINOR.PATCH`

## CI/CD Note

There are **no GitHub Actions workflows** in this repository yet.
All builds and releases are triggered manually via Maven.
Contact the **DevOps Engineer** agent to set up CI/CD pipelines.
