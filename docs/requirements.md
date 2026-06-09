# backbone-rest — Identity & Access Management Platform

> **Strategic Analysis** — Current state, security gaps, architecture recommendations, and phased roadmap.

---

## Table of Contents

1. [Current State Assessment](#1-current-state-assessment)
2. [Critical Security Gaps](#2-critical-security-gaps-fix-first)
3. [IAM Capability Gaps](#3-iam-capability-gaps)
4. [Architecture Recommendations](#4-architecture-recommendations)
5. [Phased Roadmap](#5-phased-roadmap)
6. [Quick Wins — This Week](#6-quick-wins--this-week)

---

## 1. Current State Assessment

### What Exists (IAM-relevant)

| Capability | Status | Location |
|---|---|---|
| User CRUD + alias/email uniqueness | ✅ Working | `v1/users` |
| Multi-tenant model (User → Application → Role) | ✅ Working | `ApplicationRoleUser` junction |
| Role CRUD + Role → Feature linking | ✅ Working | `v1/roles`, `v1/features` |
| Session JWT mint + validate + renew | ✅ Working | `v1/session`, JJWT 0.12.3 |
| OAuth2 Resource Server (Supabase JWT) | ⚠️ Commented out | `SecurityConfig.java` (all code is `//`) |
| JWT role extraction from Supabase tokens | ⚠️ Commented out | `JwtConverter.java` (all code is `//`) |
| Profile image per user-application | 🔧 In progress | `ds-196-include-supabase-storage` |
| Person + Contacts linked to users | ✅ Working | `v1/people`, `v1/contacts` |

### Existing IAM Data Model

```
Application (tenant)
└── ApplicationRoleUser (junction)
    ├── User
    └── Role
        └── RoleFeature
            └── Feature (capability/permission flag)
```

> This is a standard flat RBAC model. It is a valid foundation — but **incomplete for production IAM**.

---

## 2. Critical Security Gaps (Fix First)

> ⛔ These are **blockers**. The service cannot be called an IAM without resolving them.

### 2.1 Plain-Text Password Storage — 🔴 CRITICAL

**Location:** `SessionServiceImpl.java:116`

```java
userEntity.getPassword().equals(sessionRequest.getPassword())  // comparing raw strings
```

And in `UserServiceImpl.updateUserFields()`:

```java
target.setPassword(source.getPassword());  // storing whatever comes in
```

**Risk:** Full database compromise exposes every credential.

**Fix:** Introduce `PasswordEncoder` (BCrypt, Argon2id) in the persistence layer. Hash on write, `matches()` on verify.

---

### 2.2 Security Filter Chain is Disabled — 🔴 CRITICAL

`SecurityConfig.java` and `JwtConverter.java` are entirely commented out. The service is currently **unauthenticated — every endpoint is open**.

**Fix:** Uncomment, verify Supabase JWKS URI is correct, restore the filter chain.

---

### 2.3 No Account Lockout / Brute Force Protection — 🟠 HIGH

`SessionServiceImpl.loadSession()` accepts unlimited login attempts with no throttle.

**Fix:** Track failed attempts per `alias + application` in Redis or a DB table; lock after N failures for T minutes.

---

### 2.4 No Token Revocation — 🟠 HIGH

Issued JJWT session tokens cannot be invalidated before expiry. There is no deny-list.

**Fix:** Maintain a `revoked_tokens` table (or Redis set) keyed by `jti`. Check on every `getTokenClaims()` call.

---

### 2.5 Unimplemented Core Methods — 🟠 HIGH

```java
// UserServiceImpl.java
public ResponseEntity<UserTO> find(UUID id) { return null; }  // null return

public ResponseEntity<UserTO> unlink(UUID userId, UUID roleId) {
    throw new UnsupportedOperationException();               // crashes at runtime
}
```

**Fix:** Implement both before any IAM use.

---

### 2.6 Cross-Tenant Data Leak — 🟠 HIGH

`UserServiceImpl.findAll()` returns all users when `applicationId` is `null`. Any authenticated caller can enumerate the **entire user directory** across all tenants.

**Fix:** Make `applicationId` mandatory; remove the null-bypass branch.

---

### 2.7 No CSRF / CORS Policy — 🟡 MEDIUM

`@CrossOrigin(origins = "*")` is likely present on controllers. This is acceptable for pure API consumers but must be **locked to specific origins** in production.

---

## 3. IAM Capability Gaps

### 3.1 Authentication

| Gap | Priority | Notes |
|---|---|---|
| No password policy enforcement (complexity, length, history) | P1 | No `PasswordPolicy` service |
| No password reset flow (email link, OTP) | P1 | `GENERAL_DB_URI` + email integration needed |
| No email / phone verification on registration | P1 | `UserCreateRequest` sends no verification |
| No MFA / TOTP support | P2 | No `mfa` domain module |
| No magic-link / passwordless flow | P3 | Supabase GoTrue can delegate this |
| Session token lacks `iss`, `aud` claims | P1 | Needed for standard JWT validation |

### 3.2 Authorization

| Gap | Priority | Notes |
|---|---|---|
| `SecurityConfig` authorizes globally by role, not per-endpoint | P1 | All GETs get same role check |
| No permission-check API (`/permissions/check`) | P1 | Features exist but no evaluation endpoint |
| No attribute-based access control (ABAC) | P2 | Roles are flat; no resource-level conditions |
| No scope/claim → permission mapping | P2 | `app_metadata.roles` extracted but not used for endpoint guards |
| Role hierarchy not supported | P3 | Roles are independent, no `ADMIN > MANAGER > USER` chain |

### 3.3 Token Lifecycle

| Gap | Priority | Notes |
|---|---|---|
| No refresh token (separate from session token) | P1 | Single token, no separation of access/refresh |
| No token introspection endpoint (`/oauth2/introspect`) | P1 | Machine-to-machine consumers need this |
| No API key issuance for service-to-service | P2 | No `api_keys` domain |
| Token expiry in `AuthKey` enum but no rotation schedule | P2 | `APP_TOKEN_EXPIRATION` is static |

### 3.4 Audit & Observability

| Gap | Priority | Notes |
|---|---|---|
| No audit log (who logged in, when, from where) | P1 | No `audit_events` table |
| No failed-login event tracking | P1 | Required for SOC2/ISO27001 |
| No last-login / last-activity timestamp on `UserEntity` | P2 | Only `lastUpdate` (manual field) |
| `AOP LogDefault` exists but logs method names, not auth events | P2 | Could be extended |

### 3.5 Directory / Provisioning

| Gap | Priority | Notes |
|---|---|---|
| No SCIM 2.0 provisioning API | P3 | Enterprise SSO requires `/scim/v2/Users` |
| No user group concept (above Role) | P2 | Groups aggregate roles — useful for enterprise |
| No bulk user import | P2 | No batch endpoint |
| `UserServiceImpl` flagged `@SuppressWarnings("PMD.GodClass")` | P1 | Decompose before it grows further |

### 3.6 Multi-Tenancy

| Gap | Priority | Notes |
|---|---|---|
| Tenant isolation is per-call, not enforced at DB row level | P1 | No Postgres RLS on user tables |
| `Application` entity has no configuration (allowed domains, allowed redirects) | P2 | Needed for OIDC flows |
| No tenant-level password policy | P2 | Policy should be per-application |

---

## 4. Architecture Recommendations

### 4.1 Decompose `UserServiceImpl`

It currently owns: user CRUD, alias validation, email validation, role linking, person update, contact update, application linkage. Split into:

| New Service | Responsibility |
|---|---|
| `UserCoreService` | `find`, `create`, `deactivate`, `delete` |
| `UserCredentialService` | password hash, change-password, reset-password |
| `UserProfileService` | person + contacts |
| `UserRoleLinkService` | merge with existing `UserApplicationRoleService` |

---

### 4.2 Introduce `iam` Domain Module

Add `v1/iam/` alongside existing modules:

```
v1/iam/
├── permissions/   → PermissionCheckApi   (POST /iam/permissions/check)
├── tokens/        → TokenIntrospectApi   (POST /iam/tokens/introspect)
├── mfa/           → MfaApi              (POST /iam/mfa/enroll, /iam/mfa/verify)
├── apikeys/       → ApiKeyApi           (CRUD for machine credentials)
└── audit/         → AuditApi            (GET /iam/audit/events — admin only)
```

---

### 4.3 Separate the Two JWT Flows Cleanly

Currently the session JWT and the Supabase OAuth2 JWT share infrastructure inconsistently. Define clear contracts:

| Token Type | Issuer | Used For | Endpoint |
|---|---|---|---|
| Supabase access token | Supabase GoTrue | API gateway auth | Resource server filter |
| App session token | backbone-rest | Backoffice session | `/v1/session/token` |
| App refresh token | backbone-rest | Renew session | `/v1/session/refresh` *(new)* |
| API key | backbone-rest | M2M | `/v1/iam/apikeys` *(new)* |

---

### 4.4 Add `audit_event` to Persistence Layer

Request a new entity in `com.umdc.persistence`:

```java
AuditEventEntity {
    UUID          id
    UUID          userId
    UUID          applicationId
    String        eventType      // LOGIN_SUCCESS, LOGIN_FAILURE, PASSWORD_CHANGE, ROLE_ASSIGNED
    String        ipAddress
    String        userAgent
    LocalDateTime occurredAt
    String        details        // JSON blob
}
```

Publish via Spring Application Events from `SessionServiceImpl` and `UserServiceImpl`.

---

### 4.5 Password Credential Service (Immediate)

```java
// New component — inject into SessionServiceImpl and UserServiceImpl
@Component
public class PasswordCredentialService {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String encode(String raw)                  { return encoder.encode(raw); }
    public boolean matches(String raw, String hashed) { return encoder.matches(raw, hashed); }
}
```

Replace every `.equals(password)` and `setPassword(raw)` call.

---

## 5. Phased Roadmap

> **Effort key:** S = Small (< 1 day) · M = Medium (1–3 days) · L = Large (3–5 days) · XL = Extra Large (1+ week)

### Phase 1 — Security Hardening *(Immediate — blocks everything else)*

| Task | Effort | Risk if Skipped |
|---|---|---|
| Uncomment `SecurityConfig` + `JwtConverter`, verify Supabase JWT flow | S | All endpoints unauthenticated |
| Introduce `BCryptPasswordEncoder`, migrate `SessionServiceImpl` + `UserServiceImpl` | M | All passwords compromised on DB breach |
| Implement `unlink()` + `find()` in `UserServiceImpl` | S | Runtime crashes |
| Make `applicationId` mandatory in `findAll()` | S | Cross-tenant data leak |
| Add `jti` deny-list in Redis for token revocation | M | Stolen tokens cannot be invalidated |

---

### Phase 2 — Core IAM Features *(Sprint 1–2)*

| Task | Effort |
|---|---|
| Password policy service (min length, complexity, history) | M |
| Password reset flow (email OTP via Supabase or SMTP) | M |
| Email verification on `UserCreateRequest` | M |
| Failed login tracking + account lockout | M |
| `AuditEventEntity` + publish from session + user services | M |
| Permission check endpoint `POST /iam/permissions/check` | S |
| Token introspection `POST /iam/tokens/introspect` | S |
| Refresh token (separate from session token) | M |

---

### Phase 3 — IAM Platform Capabilities *(Sprint 3–5)*

| Task | Effort |
|---|---|
| TOTP / MFA enrollment + verify | L |
| API key issuance + management (`v1/iam/apikeys`) | M |
| Role hierarchy + inheritance | L |
| Tenant-level password policy per `Application` | M |
| User group concept above Role | M |
| Supabase RLS policy alignment (server-side row security) | M |
| Decompose `UserServiceImpl` god class | M |
| SCIM 2.0 `/scim/v2/Users` + `/scim/v2/Groups` | L |

---

### Phase 4 — Enterprise / Observability *(Sprint 6+)*

| Task | Effort |
|---|---|
| Audit log API with filters (`GET /iam/audit/events`) | M |
| Grafana dashboard for auth events (login rates, failure spikes) | M |
| OpenTelemetry traces on auth critical path | M |
| OIDC Authorization Server façade (Spring Authorization Server) | XL |
| SAML 2.0 SP / IdP bridge | XL |

---

## 6. Quick Wins — This Week

These can be done **without architectural changes**:

1. **Uncomment `SecurityConfig.java` and `JwtConverter.java`** — verify the Supabase JWKS URI is reachable, run `mvn test`. Unblocks all auth.
2. **Add `PasswordCredentialService` with BCrypt** — two classes, fixes the biggest vulnerability.
3. **Implement `UserServiceImpl.unlink()`** using `applicationRoleUserRepository.deleteByUserIdAndApplicationId()` — already exists in the repo, just not wired.
4. **Add `iss` + `aud` claims to `generateSessionToken()`** — one-line change to the `Jwts.builder()` chain.
5. **Run `/security-audit`** — use the `.claude/commands/security-audit.md` slash command to produce a full findings table before starting Phase 1.
