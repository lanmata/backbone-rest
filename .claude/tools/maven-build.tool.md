---
name: Maven Build
description: Tool for executing Maven build lifecycle commands in backbone-rest
type: terminal
command-prefix: mvn
used-by:
  - java-developer
  - test-writer
  - devops-engineer
  - code-reviewer
---

# Maven Build Tool

## Purpose

Execute Maven build, test, and analysis commands for the **backbone-rest** project.

> **No `mvnw`** — always use `mvn` directly.
> Set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` before any Maven command that resolves `com.umdc.*` / `com.prx.*` private dependencies.

## Available Commands

### Build
```bash
# Fast compile check — use after every code change
mvn -DskipTests compile

# Package JAR
mvn -DskipTests package

# Clean build
mvn clean -DskipTests package

# CI build with Repsy settings
mvn -s ci_settings.xml -DskipTests package
```

### Test
```bash
# Full suite: JUnit 5 + PMD + JaCoCo
mvn test

# Single test class
mvn -Dtest=UserServiceImplTest test

# Single test method
mvn -Dtest=UserServiceImplTest#findUserById_Found_Returns200 test
```

### Static Analysis
```bash
# PMD check (also runs during mvn test)
mvn pmd:check pmd:cpd-check

# View PMD report
open target/pmd.xml
```

### Coverage
```bash
# JaCoCo report (included in mvn test)
mvn test jacoco:report

# Open HTML report
open target/site/jacoco/index.html
```

### Dependency Management
```bash
# Dependency tree
mvn dependency:tree

# Check for updates
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

## Output Locations

| Artifact | Location |
|----------|----------|
| JAR | `target/backbone-rest.jar` |
| PMD report | `target/pmd.xml` |
| JaCoCo HTML | `target/site/jacoco/index.html` |
| Surefire results | `target/surefire-reports/` |

## Notes

- Maven coordinates: `com.umdc.backbone:backbone-rest:0.0.1`
- Private deps from Repsy — `ci_settings.xml` has credentials config.
- PMD ruleset: `ruleset.xml` at repo root.
- JaCoCo thresholds defined in `pom.xml` `<jacoco-maven-plugin>` config.
