---
name: JaCoCo Coverage
description: Skill for managing code coverage with JaCoCo
applies-to:
  - QA / Test Writer
  - Project Manager
---

# JaCoCo Coverage Skill

## Scope

This skill covers JaCoCo code coverage configuration, reporting, and analysis
for the **backbone-rest** project.

## Configuration

JaCoCo is configured in `pom.xml` with:

- **Plugin version**: 0.8.12
- **Report formats**: HTML + XML
- **Output**: `target/site/jacoco/`
- **Minimum threshold**: 0 (configured but not enforced — reports are generated for analysis, not as a build gate)
- **Bound to `test` phase** alongside PMD

## Exclusions

Configured via `sonar.exclusions` property in `pom.xml`:

```
**/*.xml, **/PrxBackofficeRestApplication.*,
**/config/*, **/config/jwt/*, **/config/jackson/*, **/config/security/*,
**/loggers/*, **/loggers/interceptor/*,
**/exceptions/*, **/mapper/*, **/mapper/decorator/*, **/converter/*,
**/*Test*, **/session/*
```

## Commands

```bash
# Run tests and generate coverage report
mvn test

# Generate report explicitly after tests
mvn test jacoco:report

# View HTML report
open target/site/jacoco/index.html
```

> **No `mvnw`** — always invoke `mvn` directly.

## Coverage Focus Areas (Highest Impact)

Prioritize test coverage for:
1. Service implementations: `*ServiceImpl.java` (e.g., `UserServiceImpl`, `SessionServiceImpl`)
2. Controller methods: `*Controller.java` (any logic beyond plain delegation)
3. Utility classes: `JwtUtil`, `KeystoreUtil`, `MessageUtil`

## PMD + JaCoCo Together

Both PMD (`pmd:check`) and JaCoCo (`jacoco:check`) are bound to the `test` phase.
A PMD violation will fail the build **before** JaCoCo completes — fix PMD issues first.

## Sonar Integration

JaCoCo XML report is used for SonarCloud analysis:
- Organization: `prx-open`
- `sonar.coverage.jacoco.xmlReportPaths` → `target/site/jacoco/jacoco.xml`
