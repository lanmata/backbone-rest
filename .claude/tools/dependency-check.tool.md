---
name: Dependency Check
description: Tool for auditing dependencies for CVEs and version updates in backbone-rest
type: terminal
command-prefix: mvn
used-by:
  - security-auditor
  - devops-engineer
---

# Dependency Check Tool

## Purpose

Audit **backbone-rest** dependencies for known CVE vulnerabilities and available updates.

## Available Commands

### Dependency Tree
```bash
# Full dependency tree
mvn dependency:tree

# Filter by group
mvn dependency:tree -Dincludes=org.springframework.boot

# Resolve only (check for resolution errors)
mvn dependency:resolve
```

### Version Audits
```bash
# Check for available dependency updates
mvn versions:display-dependency-updates

# Check for available plugin updates
mvn versions:display-plugin-updates
```

### CVE Scanning (OWASP dependency-check)
```bash
# Run OWASP dependency-check plugin (if configured in pom.xml)
mvn org.owasp:dependency-check-maven:check

# Report location
open target/dependency-check-report.html
```

### Manual CVE Check
```bash
# Extract current versions for manual NVD search
mvn dependency:tree | grep -E "snakeyaml|jackson|spring-security|tomcat" | sort -u
```

## Key Dependencies to Monitor

| Dependency | CVE Risk | Notes |
|------------|----------|-------|
| `snakeyaml` | Medium | YAML parsing — version pinned in `pom.xml` |
| `woodstox-core` | Medium | XML processing — pinned to 7.0.0 |
| `commons-fileupload` | High historically | Pinned to 1.6.0 |
| `spring-boot-starter-*` | Low (maintained) | Stay on latest 3.4.x patch |
| `postgresql` | Low | JDBC driver |

## Notes

- Private deps (`com.umdc.*`) from Repsy — CVE scanning requires `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD`.
- `snakeyaml` version is explicitly pinned in `pom.xml` `${snakeyaml.version}` — check it matches latest safe version.
- OWASP dependency-check plugin may need to be added to `pom.xml` if not already present.
