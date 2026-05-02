---
name: Security Reviewer Skills
description: Consolidated skill set for the Security Reviewer agent — CVE detection, dependency audit, OWASP Top 10, JWT security, and Spring Security review
applies-to:
  - Security Reviewer
---

# Security Reviewer — Skill Definition

## 1. CVE Detection & Dependency Audit

### Key Dependencies to Monitor

| Dependency | Version | Risk Area |
|-----------|---------|-----------|
| `spring-boot-starter-web` | 3.4.1 | HTTP/servlet layer |
| `spring-cloud-starter-config` | 4.2.0 | Config injection |
| `spring-cloud-starter-vault-config` | 4.2.0 | Secret management |
| `postgresql` (driver) | 42.7.4 | SQL injection, driver bugs |
| `jackson-databind` | managed | Deserialization attacks |
| `snakeyaml` | 2.3 | YAML deserialization |
| `jjwt-api` | 0.12.3 | JWT signing/verification |
| `commons-fileupload` | 1.5 | File upload security |
| `xstream` | 1.4.21 | XML deserialization |
| `woodstox-core` | 7.0.0 | XML parsing |

### CVE Severity Triage

| CVSS Score | Severity | Action |
|-----------|---------|--------|
| 9.0–10.0 | Critical | Block release; fix immediately |
| 7.0–8.9 | High | Fix before release |
| 4.0–6.9 | Medium | Fix in current sprint |
| 0.1–3.9 | Low | Track; fix in next sprint |

### Dependency Override Pattern

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.17.0</version>  <!-- patched version -->
    </dependency>
  </dependencies>
</dependencyManagement>
```

---

## 2. OWASP Top 10 Checks

### A01 — Broken Access Control
- All `/api/v1/**` require OAuth2 JWT with role enforcement.
- Session endpoints (`/api/v1/session/**`) explicitly excluded from OAuth2 filter.

```bash
grep -r "permitAll\|requestMatchers" src/main/java/com/prx/backoffice/security/
```

### A02 — Cryptographic Failures
- JWT signed with HMAC-SHA using `APP_TOKEN_SECRET` (env var, Base64).
- **Flag**: if `APP_TOKEN_SECRET` decodes to < 32 bytes → High finding.
- TLS 1.3 enforced via SSL bundle in `bootstrap.yml`.

### A03 — Injection
- Only Spring Data JPA repository methods — no native SQL.
- Jakarta annotations (`@NotBlank`, `@Email`, `@NotNull`, `@Valid`) on all request bodies.
- **Never** concatenate user input into `@Query`:

```java
// ❌ Dangerous
@Query("SELECT u FROM UserEntity u WHERE u.alias = '" + alias + "'")

// ✅ Safe — parameterized
@Query("SELECT u FROM UserEntity u WHERE u.alias = :alias")
Optional<UserEntity> findByAlias(@Param("alias") String alias);
```

### A04 — Insecure Design ⚠️ Known High Risk

```java
// ❌ Plain-text password comparison in SessionServiceImpl
userEntity.getPassword().equals(sessionRequest.getPassword())
```
**Flag as High** — recommend BCrypt / Argon2 password hashing.

### A05 — Security Misconfiguration
- `@CrossOrigin(origins = "*")` on all controllers — flag for production tightening.
- Verify actuator endpoints are restricted — only `health` should be public.
- No stack traces in API responses.

### A06 — Vulnerable Components
→ Covered by CVE Detection section above.

### A07 — Authentication Failures
- JWT expiration enforced in `SessionJwtService.isTokenExpired`.
- Keycloak tokens validated via `NimbusJwtDecoder` with JWK set URI.
- Session tokens in `session-token` header only — never in URL.

### A08 — Data Integrity Failures
- Jackson default typing must be disabled — no `enableDefaultTyping`.
- SnakeYAML 2.3 — safe loading only.

### A09 — Logging Failures

```java
// ❌ Never log passwords or tokens
LOGGER.info("Login: {} password: {}", alias, password);

// ✅ Log safe fields only
LOGGER.info("{} login attempt alias: {}", MessageUtil.LOG_START_MSG, alias);
```

### A10 — SSRF
- Feign client URLs must be config-controlled — not user-supplied.

---

## 3. JWT Security

### Two JWT Flows

```
Keycloak Token (RS256) → Authorization: Bearer header → NimbusJwtDecoder validates
App Session JWT (HS256) → POST /api/v1/session/token → session-token header
```

### App Session JWT — Key Requirements

| HMAC Algorithm | Minimum Key Length |
|---------------|-------------------|
| HS256 | 256 bits (32 bytes) |
| HS384 | 384 bits (48 bytes) |
| HS512 | 512 bits (64 bytes) |

```java
// Token generation (JJWT 0.12.3)
String generateToken(String subject) {
    return Jwts.builder()
        .subject(subject)
        .expiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(signingKey)
        .compact();
}
```

### Keycloak Role Extraction (`JwtConverter`)

```java
var resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
var clientAccess   = (Map<String, Object>) resourceAccess.get(clientId);
var roles          = (Collection<String>) clientAccess.get("roles");
roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
```

---

## 4. Spring Security Configuration Review

### Filter Chain

```java
http
    .csrf(csrf -> csrf.disable())           // Correct for stateless REST
    .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/session/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole(...)
        .anyRequest().authenticated()
    )
    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
```

### Sensitive Files Audit

| File | Check |
|------|-------|
| `src/main/resources/default.env` | Must contain only stub values |
| `src/main/resources/bootstrap.yml` | Must use `${ENV_VAR}` — no inline secrets |
| `src/main/resources/keystore.jks` | Do NOT modify |
| `src/test/resources/application-test.yml` | May contain test-only credentials (non-production) |

---

## 5. Output Format

```markdown
## Security Review Report

### Critical Findings (CVSS ≥ 9.0)
### High Findings (CVSS ≥ 7.0)
### Medium Findings (CVSS ≥ 4.0)
### Dependency CVE Summary (table with fix versions)
### Remediation Steps (ordered by priority)
```

---

## Security Review Checklist

```markdown
- [ ] All /api/v1/** endpoints require valid OAuth2 token
- [ ] Session endpoints explicitly excluded from OAuth2 filter
- [ ] No plain-text password comparison (flag A04)
- [ ] APP_TOKEN_SECRET ≥ 32 bytes decoded
- [ ] JWT expiration enforced in isTokenExpired
- [ ] Session token in header only (not URL)
- [ ] No JWT tokens or passwords logged
- [ ] TLS 1.3 enforced via SSL bundle
- [ ] @CrossOrigin(origins = "*") flagged for production review
- [ ] No hardcoded secrets in bootstrap.yml or any config
- [ ] Jackson default typing disabled
- [ ] No native SQL with user input concatenation
- [ ] No Critical/High CVEs in dependencies
```

