---
name: Security Audit
description: Run a full security audit of backbone-rest covering OWASP Top 10, JWT/OAuth2 config, Supabase auth, secrets hygiene, and CVE scan
mode: agent
agent: security-auditor
tools: [Bash, Read]
---

Run a full security audit of **backbone-rest**.

## Scope

${scope}
_(If blank: audit the entire codebase and all dependencies)_

## Step 1 — Auth Architecture Review

Read:
- `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java`
- `src/main/java/com/umdc/backoffice/security/jwt/JwtConverter.java`
- `src/main/resources/bootstrap.yml`

Check:
- [ ] All `/api/v1/**` except `/v1/sessions/token` + `/v1/sessions/validate` require Bearer JWT
- [ ] Session JWT uses JJWT 0.12.3 with strong signing algorithm
- [ ] `AUTH_SERVER_URI` sourced from env var, not hardcoded
- [ ] Roles extracted from `resource_access.<clientId>.roles` correctly

## Step 2 — Secrets Hygiene

```bash
# Check for hardcoded secrets
grep -rn "password\|secret\|apikey\|api_key\|token" src/main/resources/ --include="*.yml" --include="*.yaml"

# Check default.env is stubs only
cat default.env

# Check for secrets in Java files
grep -rn "\"password\"\|\"secret\"\|\"Bearer " src/main/java --include="*.java" | grep -v "test\|Test"
```

## Step 3 — CVE Dependency Scan

```bash
mvn dependency:tree | grep -E "snakeyaml|woodstox|commons-fileupload|spring-security|tomcat"
```

Check versions against known CVEs.

## Step 4 — OWASP Top 10 Spot Checks

- [ ] SQL Injection: no concatenated SQL — Spring Data query methods used
- [ ] XSS: REST API — no HTML reflection
- [ ] Broken Access Control: role checks via `SecurityConfig` `.hasRole()`
- [ ] Security Misconfiguration: actuator endpoints not publicly exposed
- [ ] Brute Force: `LoginAttemptServiceImpl` in `security/bruteforce/` functioning

## Step 5 — Supabase-Specific

- [ ] Signed URLs have TTL ≤ 60 minutes
- [ ] Service role key not in any committed file
- [ ] `profileimage` bucket RLS policies active

## Output Format

```markdown
## Security Audit — backbone-rest

### Findings
| # | Severity | Category | File:Line | Issue | Remediation |
|---|----------|----------|-----------|-------|------------|

### Auth Review
| Check | Status |
|-------|--------|
| OAuth2 gate | PASS/FAIL |
| Session JWT | PASS/FAIL |
| Supabase JWT | PASS/FAIL |

### Secrets
| Check | Status |
|-------|--------|
| No hardcoded secrets | PASS/FAIL |
| default.env stubs only | PASS/FAIL |
| Vault references in bootstrap.yml | PASS/FAIL |

### Dependency CVEs
| Dependency | Version | CVE | Severity |
|------------|---------|-----|---------|

### Overall Risk: LOW / MEDIUM / HIGH / CRITICAL
```
