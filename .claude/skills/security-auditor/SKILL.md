---
name: Security Auditor Skills
description: Consolidated skill set for the Security Auditor agent — OWASP Top 10, Supabase JWT, Spring Security OAuth2, session JWT (JJWT), CVE scanning for backbone-rest
applies-to:
  - security-auditor
---

# Security Auditor — Skill Definition

## 1. Auth Architecture

### Two Separate Auth Mechanisms
| Mechanism | Scope | Token Location | Implementation |
|-----------|-------|---------------|----------------|
| OAuth2 Resource Server (Keycloak / Supabase JWT) | All `/api/v1/**` GETs except session endpoints | `Authorization: Bearer <token>` | `SecurityConfig.java` + `JwtConverter.java` |
| App Session JWT (JJWT 0.12.3) | `/api/v1/sessions/token`, `/api/v1/sessions/validate` | `session-token` header | `SessionServiceImpl.java` |

### Bypassed Endpoints (whitelist in SecurityConfig)
- `/v1/sessions/token`
- `/v1/sessions/validate`

### JWT Role Extraction
Roles extracted from Keycloak JWT claim: `resource_access.<clientId>.roles` by `JwtConverter.java`.

---

## 2. Audit Checklist

### OWASP Top 10 Focus Areas
- [ ] **Injection**: No concatenated SQL — use Spring Data query methods.
- [ ] **Broken Auth**: JWT validation in place; session token not in URL.
- [ ] **Sensitive Data Exposure**: No plain-text passwords in logs or responses; no secrets in `application.yml` / `bootstrap.yml` (use Vault references `${vault:...}`).
- [ ] **Security Misconfig**: CORS limited to known origins in prod; actuator endpoints secured.
- [ ] **XSS**: REST JSON API — minimal risk; validate that no HTML is reflected.
- [ ] **Broken Access Control**: Role-based checks in `SecurityConfig` via `.hasRole()` or `@PreAuthorize`.
- [ ] **Vulnerable Deps**: Check CVEs via `mvn dependency:tree` + OWASP dep check.

### Supabase-Specific
- [ ] Supabase JWT issuer URI matches `AUTH_SERVER_URI` env var (not hardcoded).
- [ ] Supabase Storage bucket access uses signed URLs with short TTL.
- [ ] RLS policies on Supabase tables are active (verify via Supabase dashboard).

### Secrets Hygiene
- [ ] No API keys, passwords, or JWTs in git history.
- [ ] `default.env` contains only stubs (no real values).
- [ ] Vault references used in `bootstrap.yml` for all secrets.
- [ ] `keystore.jks`, `backbone.jks`, `umdc-truststore.jks`, `*.crt` not modified or re-committed.

---

## 3. Key Files

| File | What to Check |
|------|--------------|
| `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java` | OAuth2 setup, whitelisted endpoints, CORS |
| `src/main/java/com/umdc/backoffice/security/jwt/JwtConverter.java` | Role extraction from JWT claims |
| `src/main/java/com/umdc/backoffice/v1/session/services/SessionServiceImpl.java` | Session JWT minting/validation |
| `src/main/resources/bootstrap.yml` | Auth server URI, Vault config — no hardcoded secrets |
| `pom.xml` | Dependency versions — check for known CVEs |
| `default.env` | Must contain stubs only |

---

## 4. Output Format

```markdown
## Security Audit — <scope>

### Findings (ordered by CVSS severity)
| # | Severity | Category | File:Line | Issue | Remediation |
|---|----------|----------|-----------|-------|------------|
| 1 | CRITICAL | Broken Auth | SecurityConfig.java:42 | ... | ... |

### Auth Architecture Review
- OAuth2 gate: PASS/FAIL
- Session JWT: PASS/FAIL
- Supabase JWT issuer: PASS/FAIL

### Secrets Hygiene
- Vault references: PASS/FAIL
- default.env stubs only: PASS/FAIL
- No secrets in git: PASS/FAIL

### Overall Risk: LOW / MEDIUM / HIGH / CRITICAL
```
