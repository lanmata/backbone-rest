---
name: Maven Build
description: Tool for executing Maven build lifecycle commands
type: terminal
command-prefix: mvn
---

# Maven Build Tool

## Purpose

Execute Maven build lifecycle commands for the **backbone-rest** project.

> **No `mvnw`** — always invoke `mvn` directly.  
> Set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` before any Maven command that resolves private PRX dependencies.

## Available Commands

### Build

```bash
# Fast compile check (no tests)
mvn -DskipTests compile

# Package JAR (skip tests)
mvn -DskipTests package

# Clean only
mvn clean
```

### Test

```bash
# Run all tests (also executes PMD + JaCoCo)
mvn test

# Run single test class
mvn -Dtest=UserServiceImplTest test

# Run single test method
mvn -Dtest=UserServiceImplTest#testFindUserById_Found test
```

### Coverage

```bash
# Generate JaCoCo report (included in mvn test, but explicit if needed)
mvn test jacoco:report

# View report
open target/site/jacoco/index.html
```

### Static Analysis

```bash
# PMD check only (also runs during mvn test)
mvn pmd:check pmd:cpd-check

# View PMD report
open target/pmd.xml
```

### Dependency Management

```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Check for plugin updates
mvn versions:display-plugin-updates

# Dependency tree
mvn dependency:tree
```

## Output Locations

- Build artifact: `target/backbone-rest.jar`
- JaCoCo HTML: `target/site/jacoco/index.html`
- PMD report: `target/pmd.xml`
