# Security Audit — backbone-rest

Run a full OWASP-aligned security audit: JWT validation, plain-text password check,
CORS, CVE scan, and Supabase auth configuration.

## Usage
```
/security-audit
```

---

## Steps Claude Will Execute

### 1. Auth Architecture Verification
```bash
# Verify SecurityConfig excludes only session endpoints
grep -n "permitAll\|requestMatchers" src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java

# Verify JwtConverter extracts roles from Supabase token format
cat src/main/java/com/umdc/backoffice/security/jwt/JwtConverter.java
```

### 2. Plain-Text Password Check (High Risk)
```bash
grep -n "getPassword\|BCrypt\|PasswordEncoder\|encodePassword" \
  src/main/java/com/umdc/backoffice/v1/session/services/SessionServiceImpl.java
```
If `.equals(password)` found without hashing → **HIGH** finding.

### 3. CORS Audit
```bash
grep -rn "CrossOrigin" src/main/java/ --include="*.java"
```
`@CrossOrigin(origins = "*")` on any controller → **MEDIUM**.

### 4. Secrets in Config
```bash
# All values must be ${ENV_VAR} — no inline secrets
grep -E "password\s*=\s*[^$\{]|secret\s*=\s*[^$\{]|key\s*=\s*[^$\{]" \
  src/main/resources/bootstrap.yml
grep -v "^#\|^$\|=\s*$" src/main/resources/default.env | grep -E "SECRET|PASSWORD|KEY" | head -20
```

### 5. Logging PII Check
```bash
grep -rn "log\.\(info\|debug\|warn\|error\).*password\|log\.\(info\|debug\).*token" \
  src/main/java/ --include="*.java"
```

### 6. Dependency CVE Scan
```bash
mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7 2>&1 | tail -30
# Full report: target/dependency-check-report.html
```

### 7. Supabase Auth Config
```bash
# Verify issuer and JWK URI point to Supabase
grep -E "AUTH_SERVER_URI|AUTH_ISSUER_URI|AUTH_JWK_URI" src/main/resources/default.env
grep -E "issuer|jwk" src/main/resources/bootstrap.yml
```

### 8. JWT Token Expiry
```bash
grep -n "isTokenExpired\|expiration\|APP_TOKEN_EXPIRATION" \
  src/main/java/com/umdc/backoffice/v1/session/services/SessionServiceImpl.java
```

---

## Severity Reference
| CVSS | Level |
|------|-------|
| ≥ 9.0 | Critical |
| 7.0–8.9 | High |
| 4.0–6.9 | Medium |
| < 4.0 | Low |

## Output (use `.claude/agents/security-auditor.md` format)
Produce the full findings table ordered Critical → Low, then overall risk rating.
