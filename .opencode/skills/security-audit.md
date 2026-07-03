# Security Audit

## When to use
Before any release, after adding new endpoints or auth logic, or when reviewing a branch that touches `security/`, `session/`, `managedclient/`, or `bootstrap.yml`.

## Steps

1. **Run grep patterns to detect common vulnerabilities**

```bash
# A — Hardcoded secrets / credentials
grep -rEn "(password|secret|token|apikey|api_key)\s*=\s*['\"][^$\{]" src/main/ --include="*.java" --include="*.yml" --include="*.yaml"

# B — SQL injection risk: string concatenation in queries
grep -rEn "createNativeQuery\s*\(" src/main/java/
grep -rEn "createQuery\s*\(.*\+" src/main/java/

# C — Sensitive data in logs
grep -rEn "LOGGER\.(info|debug|trace)\s*\(.*[Tt]oken" src/main/java/
grep -rEn "LOGGER\.(info|debug|trace)\s*\(.*[Pp]assword" src/main/java/
grep -rEn "LOGGER\.(info|debug|trace)\s*\(.*[Ss]ecret" src/main/java/

# D — Unsafe CORS
grep -rEn "allowCredentials\s*=\s*['\"]true['\"]" src/main/java/

# E — Missing @Valid on @RequestBody
grep -rn "@RequestBody" src/main/java/ | grep -v "@Valid"

# F — Hardcoded IPs
grep -rEn "\b(\d{1,3}\.){3}\d{1,3}\b" src/main/ --include="*.java"

# G — Committed keystores or certs
git ls-files | grep -E "\.(jks|p12|pem|crt|key)$"

# H — SNAPSHOT in production pom.xml
grep -E "SNAPSHOT" pom.xml

# I — cross-tenant leak risk (findAll without applicationId scope)
grep -rEn "findAll\(\)" src/main/java/com/umdc/backoffice/v1/ --include="*.java"

# J — BCrypt usage (confirm passwords are hashed)
grep -rEn "\.encode\(" src/main/java/ --include="*.java"
grep -rEn "setPassword\(" src/main/java/ --include="*.java" | grep -v "passwordEncoder"
```

2. **Review authentication bypass list** — read `SecurityConfig.java` and confirm every `permitAll()` path is intentional:
   - `/api/v1/session` — login endpoints
   - `/api/v1/managed-clients/token` and `/introspect` — M2M public
   - Swagger paths — acceptable in non-production only

3. **Check JWT configuration**:
   - `SessionJwtAuthenticationFilter`: token validated with keystore, not just decoded
   - `JtiDenyListService`: deny-list checked on every authenticated request
   - `ManagedClientTokenFilter`: BCrypt comparison, not plain-text compare
   - `LoginAttemptService`: rate limiting active on failed auth attempts

4. **Review sensitive response fields** — GET endpoints for `ManagedClientTO` must not include `secretHash` or `prevSecretHash`

5. **Check dependency CVEs** — review these libraries manually or via OWASP Dependency-Check:
   - Spring Boot 4.0.6
   - JJWT 0.12.6
   - SnakeYAML 2.5
   - commons-fileupload 1.6.0
   - PostgreSQL driver 42.7.7

6. **Audit event coverage** — confirm `AuditEventService.record(...)` is called for:
   - `PASSWORD_CHANGE`
   - `ROLE_ASSIGNED`
   - `ROLE_REVOKED`
   - Any new sensitive operations

## Output report format

```
## Security Audit — <scope or branch>
## Date: <date>

### Crítico — CVSS 7.0+ (block release)
- **file:line** — finding, CWE-XXX, recommended fix

### Importante — CVSS 4.0–6.9
- **file:line** — finding, recommended fix

### Informativo
- **file:line** — observation

### Clean ✓
- Grep patterns A–J: <pass/findings>
- Auth bypass list: <confirmed / anomalies>
- JWT validation: <pass/findings>
- Sensitive fields: <pass/findings>
- Audit event coverage: <pass/gaps>
```

## Checklist
- [ ] All grep patterns run with zero critical findings
- [ ] Every `permitAll()` path justified in SecurityConfig comments
- [ ] JWT tokens not logged at INFO/DEBUG
- [ ] BCrypt used for all passwords and M2M secrets
- [ ] No keystores or certs in git
- [ ] Audit events fire for all sensitive state changes
- [ ] No SNAPSHOT dependencies in pom.xml
