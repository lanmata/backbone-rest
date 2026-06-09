---
name: Dependency Check
description: Tool for checking Maven dependency vulnerabilities and updates
type: terminal
command-prefix: mvn
---

# Dependency Check Tool

## Purpose

Audit Maven dependencies for security vulnerabilities and available updates
in the **backbone-rest** project.

> Set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` before running Maven commands.

## Available Commands

### Vulnerability Scanning

```bash
# OWASP dependency check (if plugin is configured)
mvn org.owasp:dependency-check-maven:check

# View report
open target/dependency-check-report.html
```

### Version Management

```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Check for plugin updates
mvn versions:display-plugin-updates

# Check for property updates
mvn versions:display-property-updates
```

### Dependency Analysis

```bash
# Full dependency tree
mvn dependency:tree

# Analyze unused/undeclared dependencies
mvn dependency:analyze

# Show effective POM
mvn help:effective-pom
```

### Key Dependencies to Monitor

| Dependency                                          | Version   | Risk Area                    |
|-----------------------------------------------------|-----------|------------------------------|
| `org.springframework.boot:spring-boot-starter-*`   | 3.4.1     | HTTP, servlet, security      |
| `org.springframework.cloud:spring-cloud-*`         | 4.2.0     | Config injection, Vault      |
| `org.postgresql:postgresql`                         | 42.7.4    | SQL injection, driver bugs   |
| `org.yaml:snakeyaml`                                | 2.3       | YAML deserialization         |
| `io.jsonwebtoken:jjwt-*`                            | 0.12.3    | JWT signing/verification     |
| `org.mapstruct:mapstruct`                           | 1.5.5.Final | Compile-time only          |
| `com.thoughtworks.xstream:xstream`                  | 1.4.21    | Deserialization              |
| `commons-fileupload:commons-fileupload`             | 1.5       | File upload security         |
| `com.prx:persistence`                               | 0.0.3     | External module (Repsy)      |
| `com.prx:prx-commons`                               | 0.0.4     | External module (Repsy)      |
