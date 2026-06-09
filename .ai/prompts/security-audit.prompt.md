---
name: Security Audit
description: Run a security review covering CVEs, OWASP Top 10, JWT, and Spring Security for backbone-rest
mode: agent
agent: security-reviewer
tools: [run_in_terminal, read_file, grep_search, file_search, validate_cves]
---

Perform a security audit of the **backbone-rest** project.

## Scope

- Focus: ${focus}
  _(e.g., `dependencies`, `authentication`, `endpoints`, `full` — default: `full`)_

## Steps

### 1. Dependency CVE Scan

```bash
mvn dependency:tree | grep -E "com\.|io\.|org\." | sort -u
mvn versions:display-dependency-updates
```

Use `validate_cves` tool with ecosystem `maven` for critical dependencies:
- `org.springframework.boot:spring-boot-starter-web`
- `io.jsonwebtoken:jjwt-api`
- `org.postgresql:postgresql`
- `org.yaml:snakeyaml`
- `com.thoughtworks.xstream:xstream`

### 2. OWASP Top 10 Checks

```
[ ] A01 — All /api/v1/** require OAuth2 JWT (check SecurityConfig)
[ ] A01 — Session endpoints /api/v1/session/** explicitly permitted
[ ] A02 — APP_TOKEN_SECRET ≥ 32 bytes decoded (no weak secret)
[ ] A03 — No native SQL with user input concatenation
[ ] A04 — Password comparison uses hashing (not plain-text .equals())
[ ] A05 — No stack traces in API responses
[ ] A07 — JWT expiration enforced in SessionJwtService.isTokenExpired
[ ] A07 — Session token in session-token header only (not URL)
[ ] A08 — Jackson default typing disabled
[ ] A09 — No passwords/tokens in any log statement
```

### 3. Spring Security Config Check

```bash
grep -n "permitAll\|csrf\|sessionManagement\|oauth2ResourceServer" \
  src/main/java/com/prx/backoffice/security/config/SecurityConfig.java
```

### 4. Secrets Check

```bash
grep -rn "password\s*=\s*['\"]" src/main/resources/
grep -rn "secret\s*=\s*['\"]" src/main/resources/
```

## Output Format

```markdown
## Security Audit Report

### Dependency CVEs
| CVE | Dependency | CVSS | Fixed In | Action |
|-----|-----------|------|----------|--------|

### OWASP Findings
| Check | Status | Detail |
|-------|--------|--------|

### Critical Findings (block release)
### High Findings (fix before release)
### Medium Findings (track)
### Overall: PASS / FAIL
```

