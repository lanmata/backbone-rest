# 🗂️ MCAM Implementation Plan — Management Client Authentication Manager
### Implementation Plan — `backbone-rest` · TASK-03

---

![Status](https://img.shields.io/badge/status-planning-blue)
![Phase](https://img.shields.io/badge/phases-6-brightgreen)
![Sprints](https://img.shields.io/badge/sprints-3-orange)
![Owner](https://img.shields.io/badge/owner-backbone--rest%20dev-lightgrey)
![Ref](https://img.shields.io/badge/requirements-TASK--02-yellow)

---

## 📋 Table of Contents

1. [Overview & Codebase Anchors](#1-overview--codebase-anchors)
2. [Phase 1 — Foundation & Domain Setup](#2-phase-1--foundation--domain-setup)
3. [Phase 2 — Core CRUD API](#3-phase-2--core-crud-api)
4. [Phase 3 — Authentication Flow](#4-phase-3--authentication-flow)
5. [Phase 4 — Secret Rotation & Audit](#5-phase-4--secret-rotation--audit)
6. [Phase 5 — Tests + PMD + JaCoCo Gates](#6-phase-5--tests--pmd--jacoco-gates)
7. [Phase 6 — OpenAPI Spec + Documentation](#7-phase-6--openapi-spec--documentation)
8. [Risk Register](#8-risk-register)
9. [Sprint Breakdown](#9-sprint-breakdown)
10. [Dependency Graph](#10-dependency-graph)

---

## 1. Overview & Codebase Anchors

### 1.1 New Module Root

All new production classes live under:

```
src/main/java/com/umdc/backoffice/v1/managedclient/
```

Mirroring the established `v1.<domain>.*` convention seen in `application`, `iam`, `users`, etc.

### 1.2 Key Existing Files That Will Be Modified

| File | Reason |
|---|---|
| `security/config/SecurityConfig.java` | Register `ManagedClientTokenFilter` in the filter chain |
| `security/filter/SessionJwtAuthenticationFilter.java` | Guard: skip if `SecurityContext` already populated by M2M filter |
| `property/ManagementAuthenticatorProperties.java` | Add `tokenTtlSeconds` and `rotationGracePeriodSeconds` fields |
| `v1/iam/audit/domain/AuditEventType.java` | Add 9 new M2M event type constants |
| `src/main/resources/db/migration/V1__create_audit_event.sql` | **NOT modified** — new migrations added instead |
| `src/main/resources/application.yml` | Add `umdc.security.managementAuthenticator.*` MCAM property block |

### 1.3 Architecture Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| AC-01 | Interface-first: `ManagedClientApi` interface + `ManagedClientController` impl |
| AC-02 | All controller methods return `ResponseEntity<?>` |
| AC-03 | Constructor injection — no `@Autowired` field injection anywhere |
| AC-04 | `mvn pmd:check` passes with 0 violations |
| AC-05 | MapStruct for all entity↔DTO conversions |
| AC-06 | `ManagedClientTokenFilter` registered BEFORE `SessionJwtAuthenticationFilter` |
| AC-07 | No new Maven dependencies (JJWT 0.12.6, Spring Security, Redis, JPA, MapStruct already present) |
| AC-08 | Package `com.umdc.backoffice.v1.managedclient.*` |
| AC-09 | All secrets/keys via `@ConfigurationProperties` + env vars — no hardcoding |
| AC-10 | No breaking changes to any existing `/api/v1/*` endpoint |

---

## 2. Phase 1 — Foundation & Domain Setup

**Goal:** All persistent structures, configuration, and shared infrastructure in place. No new HTTP endpoints yet.

**Complexity:** Medium  
**Dependencies:** None (first phase)  
**Estimated Duration:** 3–4 days

---

### 2.1 Flyway Migrations

#### File to Create: `src/main/resources/db/migration/V2__create_managed_client.sql`

```
Purpose : managed_client table + indexes
Tables  : public.managed_client
Notes   : scopes stored as TEXT[] (native Postgres array)
          prev_secret_hash nullable — populated only during grace-period rotation
          Unique constraint on (name, application_id) enforces FR-02 uniqueness
```

Schema outline:
```sql
CREATE TABLE IF NOT EXISTS public.managed_client (
    id                      UUID         NOT NULL,
    name                    VARCHAR(128) NOT NULL,
    description             VARCHAR(512),
    application_id          UUID         NOT NULL,
    secret_hash             VARCHAR(255) NOT NULL,
    prev_secret_hash        VARCHAR(255),
    scopes                  TEXT[]       NOT NULL,
    active                  BOOLEAN      NOT NULL DEFAULT true,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_updated_at         TIMESTAMPTZ,
    secret_last_rotated_at  TIMESTAMPTZ,
    CONSTRAINT managed_client_pk PRIMARY KEY (id),
    CONSTRAINT managed_client_name_app_uq UNIQUE (name, application_id)
);
-- Indexes on: id (PK implicit), application_id, active, (application_id, active)
```

#### File to Create: `src/main/resources/db/migration/V3__create_managed_client_audit_event.sql`

```
Purpose : Dedicated audit table for all M2M lifecycle events.
          Kept separate from audit_event to avoid making user_id nullable
          on the existing table (which would break the NOT NULL constraint
          on V1__create_audit_event.sql without a costly migration).
Tables  : public.managed_client_audit_event
```

Schema outline:
```sql
CREATE TABLE IF NOT EXISTS public.managed_client_audit_event (
    id          UUID        NOT NULL,
    client_id   UUID        NOT NULL,    -- logical FK → managed_client.id
    event_type  VARCHAR(64) NOT NULL,
    ip_address  VARCHAR(45),
    outcome     VARCHAR(16) NOT NULL,    -- 'SUCCESS' | 'FAILURE'
    details     JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT managed_client_audit_event_pk PRIMARY KEY (id)
);
-- Indexes on: client_id, event_type, occurred_at DESC, (client_id, event_type)
```

#### File to Create: `src/main/resources/db/migration/V4__extend_audit_event_check.sql`

```
Purpose : Add M2M event type values to the audit_event_type_ck CHECK constraint
          on the existing audit_event table so the AuditEventType enum extension
          does not cause constraint violations if future code writes M2M events
          to the original table.
          The V1 migration comment explicitly designed this as ALTER-able.
```

---

### 2.2 JPA Entities

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/domain/ManagedClientEntity.java`

```
Annotation  : @Entity @Table(name = "managed_client")
Key fields  : id (UUID, PK), name, description, applicationId (UUID),
              secretHash, prevSecretHash (nullable), scopes (List<String>),
              active (boolean), createdAt, lastUpdatedAt, secretLastRotatedAt
JPA mapping : scopes mapped via @ElementCollection or @Column(columnDefinition = "TEXT[]")
              with a Hibernate ArrayType descriptor
@PrePersist : set createdAt if null
@PreUpdate  : set lastUpdatedAt
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/domain/ManagedClientAuditEventEntity.java`

```
Annotation  : @Entity @Table(name = "managed_client_audit_event")
Key fields  : id (UUID, PK), clientId (UUID), eventType (AuditEventType enum),
              ipAddress, outcome (String), details (JSONB String), occurredAt
@PrePersist : set occurredAt if null
Note        : reuses existing AuditEventType enum — avoids duplication
```

---

### 2.3 Repositories

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/repository/ManagedClientRepository.java`

```java
// JpaRepository<ManagedClientEntity, UUID>
// Custom methods:
//   Optional<ManagedClientEntity> findByIdAndActiveTrue(UUID id)
//   boolean existsByNameAndApplicationId(String name, UUID applicationId)
//   Page<ManagedClientEntity> findByApplicationId(UUID applicationId, Pageable pageable)
//   Page<ManagedClientEntity> findByActive(boolean active, Pageable pageable)
//   Page<ManagedClientEntity> findByApplicationIdAndActive(UUID appId, boolean active, Pageable p)
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/repository/ManagedClientAuditEventRepository.java`

```java
// JpaRepository<ManagedClientAuditEventEntity, UUID>
// Custom methods:
//   Page<ManagedClientAuditEventEntity> findByClientId(UUID clientId, Pageable pageable)
//   Page<ManagedClientAuditEventEntity> findByEventType(AuditEventType type, Pageable pageable)
//   Page<ManagedClientAuditEventEntity> findByClientIdAndEventType(UUID clientId, AuditEventType type, Pageable p)
```

---

### 2.4 Configuration Properties

#### File to Modify: `src/main/java/com/umdc/backoffice/property/ManagementAuthenticatorProperties.java`

Add fields:
```java
private long tokenTtlSeconds;        // bound to ${MCAM_TOKEN_TTL_SECONDS:3600}
private long rotationGracePeriodSeconds; // bound to ${MCAM_ROTATION_GRACE_SECONDS:300}
private int  rateLimitRpm;           // bound to ${MCAM_RATE_LIMIT_RPM:60}
// + getters/setters following existing style
```

#### File to Modify: `src/main/resources/application.yml`

Add block (env-var driven, no hardcoded values):
```yaml
umdc:
  security:
    managementAuthenticator:
      keyAlias: ${MCAM_KEY_ALIAS}
      tokenTtlSeconds: ${MCAM_TOKEN_TTL_SECONDS:3600}
      rotationGracePeriodSeconds: ${MCAM_ROTATION_GRACE_SECONDS:300}
      rateLimitRpm: ${MCAM_RATE_LIMIT_RPM:60}
      keystore:
        location: ${MCAM_KEYSTORE_LOCATION}
        password: ${MCAM_KEYSTORE_PASSWORD}
        type: PKCS12
```

---

### 2.5 AuditEventType Extension

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/iam/audit/domain/AuditEventType.java`

Append (additive — backward compatible):
```java
/// Managed client registered by an admin.
CLIENT_REGISTERED,

/// Managed client metadata updated.
CLIENT_UPDATED,

/// Managed client deactivated (active = false).
CLIENT_DEACTIVATED,

/// Managed client record deleted.
CLIENT_DELETED,

/// Client secret rotated; grace period started.
CLIENT_SECRET_ROTATED,

/// M2M access token issued successfully.
CLIENT_TOKEN_ISSUED,

/// M2M token issuance failed (bad credentials / inactive client).
CLIENT_TOKEN_ISSUE_FAILED,

/// All active tokens revoked for a client.
CLIENT_TOKEN_REVOKED,

/// Token introspection endpoint called.
CLIENT_INTROSPECTION_CALLED
```

---

### 2.6 DTOs / Transfer Objects

#### Files to Create under `src/main/java/com/umdc/backoffice/v1/managedclient/api/to/`:

| Class | Purpose |
|---|---|
| `ManagedClientCreateRequest.java` | POST body — name, description, applicationId, scopes, active |
| `ManagedClientCreateResponse.java` | 201 body — includes plaintext `clientSecret` (once only) |
| `ManagedClientTO.java` | Read-only view — never includes secret material |
| `ManagedClientUpdateRequest.java` | PUT body — name, description, scopes, active (all nullable for partial update) |
| `ManagedClientTokenRequest.java` | POST /token body — clientId, clientSecret, scopes |
| `ManagedClientTokenResponse.java` | 200 body — accessToken, tokenType, expiresIn, scopes |
| `ManagedClientSecretRotateResponse.java` | rotate-secret response — clientId, clientSecret, gracePeriodSeconds, rotatedAt |
| `ManagedClientTokenIntrospectResponse.java` | introspect response — active, clientId, clientName, scopes, issuer, exp, iat, jti |
| `ManagedClientErrorResponse.java` | Structured error — error, error_description, optional fields |

All DTOs: Java records or plain classes with constructor injection. No Lombok (not in existing code style).

---

### 2.7 MapStruct Mapper

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/mapper/ManagedClientMapper.java`

```java
// @Mapper(componentModel = "spring")
// Mappings:
//   ManagedClientEntity → ManagedClientTO
//     @Mapping(target = "clientId", source = "id")
//     @Mapping(target = "secretHash", ignore = true)  ← never exposed
//     @Mapping(target = "prevSecretHash", ignore = true)
//   ManagedClientCreateRequest → ManagedClientEntity
//     @Mapping(target = "id", ignore = true)         ← generated by service
//     @Mapping(target = "secretHash", ignore = true) ← set by service post-hash
//     @Mapping(target = "createdAt", ignore = true)
//     @Mapping(target = "lastUpdatedAt", ignore = true)
//     @Mapping(target = "secretLastRotatedAt", ignore = true)
//   ManagedClientUpdateRequest → ManagedClientEntity (partial / @MappingTarget)
//   List<ManagedClientEntity> → List<ManagedClientTO>
```

---

### Phase 1 Quality Gate Checklist

- [ ] `mvn -DskipTests compile` — green (no compilation errors)
- [ ] `mvn pmd:check` — 0 violations on new files
- [ ] Flyway migrations have no SQL syntax errors (validated by `mvn test` with embedded or test DB)
- [ ] No hardcoded secrets in any file

---

## 3. Phase 2 — Core CRUD API

**Goal:** Full admin CRUD for managed clients accessible at `/api/v1/managed-clients`. Token issuance is NOT included yet.

**Complexity:** Medium  
**Dependencies:** Phase 1 complete  
**Estimated Duration:** 3–4 days

---

### 3.1 Service Interface & Implementation

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientService.java`

```java
// Interface methods — all return ResponseEntity<?>:
ResponseEntity<?> registerClient(ManagedClientCreateRequest request);
ResponseEntity<?> listClients(UUID applicationId, Boolean active, int page, int size);
ResponseEntity<?> getClient(UUID clientId);
ResponseEntity<?> updateClient(UUID clientId, ManagedClientUpdateRequest request);
ResponseEntity<?> deleteClient(UUID clientId);
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientServiceImpl.java`

```
Annotations  : @Service
Constructor  : ManagedClientRepository, ManagedClientMapper,
               ManagedClientAuditService, BCryptPasswordEncoder (from SecurityBeansConfig)
Key logic:
  registerClient  — check name/appId uniqueness (409), generate UUID clientId,
                    generate 32-byte SecureRandom secret (Base64URL), BCrypt-hash it,
                    persist entity, emit CLIENT_REGISTERED audit, return 201 with plaintext secret
  listClients     — delegate to repository with Pageable; return 204 if empty
  getClient       — findById or 404; map to ManagedClientTO (no secret fields)
  updateClient    — partial update with null-safe field merge; emit CLIENT_UPDATED audit
  deleteClient    — call revokeAllClientTokens first; delete entity; emit CLIENT_DELETED audit

BCrypt hashing MUST use @Async thread pool (NFR-P-03) — move hash operation to
ManagedClientSecretHashService to isolate async boundary.
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientSecretHashService.java`

```java
// Interface:
//   CompletableFuture<String> hashSecret(String rawSecret);
//   boolean matches(String rawSecret, String storedHash);
//   boolean matchesWithConstantTime(String rawSecret, String storedHash); // timing-safe
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientSecretHashServiceImpl.java`

```
Annotations : @Service
Constructor : BCryptPasswordEncoder
@Async on   : hashSecret(rawSecret) — runs on "mcamHashExecutor" thread pool
Note        : BCryptPasswordEncoder.matches() is already constant-time;
              document this explicitly in Javadoc for AC-TOK-02
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/config/ManagedClientAsyncConfig.java`

```java
// @Configuration @EnableAsync (scoped to MCAM)
// @Bean("mcamHashExecutor") ThreadPoolTaskExecutor
//   corePoolSize=2, maxPoolSize=4, queueCapacity=50, threadNamePrefix="mcam-hash-"
// Prevents BCrypt blocking Tomcat threads (NFR-P-03)
```

---

### 3.2 Audit Service

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientAuditService.java`

```java
// Interface:
//   void record(UUID clientId, AuditEventType eventType, String ipAddress,
//               String outcome, String details);
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientAuditServiceImpl.java`

```
Annotations : @Service
Constructor : ManagedClientAuditEventRepository
@Async      : record() — mirror pattern from AuditEventServiceImpl
```

---

### 3.3 API Interface & Controller

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientApi.java`

```java
// @RequestMapping("/api/v1/managed-clients")
// Methods (all return ResponseEntity<?>):
//   @PostMapping               registerManagedClient(@Valid @RequestBody ManagedClientCreateRequest)
//   @GetMapping                listManagedClients(@RequestParam optional filters + page/size)
//   @GetMapping("/{clientId}") getManagedClient(@PathVariable UUID clientId)
//   @PutMapping("/{clientId}") updateManagedClient(@PathVariable UUID clientId, @Valid @RequestBody ManagedClientUpdateRequest)
//   @DeleteMapping("/{clientId}") deleteManagedClient(@PathVariable UUID clientId)
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientController.java`

```
Annotations : @RestController (implements ManagedClientApi)
Constructor : ManagedClientService
Note        : Controller is a thin delegate — no business logic here
```

---

### 3.4 SecurityConfig Update (Phase 2 addition)

#### File to Modify: `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java`

Add permit rule for POST /token (public endpoint) in `authorizeHttpRequests`:
```java
auth.requestMatchers(HttpMethod.POST, "/api/v1/managed-clients/token").permitAll();
auth.requestMatchers(HttpMethod.POST, "/api/v1/managed-clients/introspect").permitAll();
```

> ⚠️ These two paths must be added in Phase 2 even though the endpoints are implemented in Phase 3, so the CRUD endpoints can be tested without interfering with security config changes later.

---

### Phase 2 Quality Gate Checklist

- [ ] `mvn -DskipTests compile` — green
- [ ] `mvn pmd:check` — 0 violations
- [ ] All 5 CRUD endpoints reachable (manual smoke test via Swagger UI)
- [ ] `POST /api/v1/managed-clients` returns `clientSecret` exactly once (AC-REG-01)
- [ ] `GET /api/v1/managed-clients/{clientId}` response body contains no secret fields (AC-SEC-02)
- [ ] Duplicate name+applicationId returns `409` (AC-REG-02)

---

## 4. Phase 3 — Authentication Flow

**Goal:** Token issuance, validation via a new security filter, token revocation, and introspection.

**Complexity:** High  
**Dependencies:** Phase 1 and Phase 2 complete  
**Estimated Duration:** 4–5 days

---

### 4.1 M2M Token Service

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientTokenService.java`

```java
// Interface:
//   ResponseEntity<?> issueToken(ManagedClientTokenRequest request, String clientIp);
//   ResponseEntity<?> revokeAllTokens(UUID clientId);
//   ResponseEntity<?> introspectToken(String rawToken);
//   boolean isTokenActive(String jti);  // used by ManagedClientTokenFilter
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientTokenServiceImpl.java`

```
Annotations : @Service
Constructor : ManagedClientRepository, ManagedClientSecretHashService,
              ManagedClientAuditService, ManagedClientRedisService,
              ManagementAuthenticatorProperties (for TTL, key alias),
              KeystoreUtil (existing utility for key loading)

issueToken logic:
  1. Load client by clientId — 401 if not found or inactive (constant-time path)
  2. ManagedClientSecretHashService.matchesWithConstantTime(request.clientSecret, entity.secretHash)
     also check prevSecretHash (grace period) — 401 if neither matches
  3. Validate requested scopes ⊆ registered scopes — 400 invalid_scope if violated
  4. Build JWT: sub=clientId, iss=backbone-rest issuer, aud=scopes, exp, iat, jti (UUID),
     client_name, scopes, type="M2M" — signed with RS256 via JJWT 0.12.6
  5. Store jti → expiry in Redis via ManagedClientRedisService (key: mcam:token:{jti})
  6. Rate-limit check via Redis sliding counter (key: mcam:ratelimit:{clientId})
  7. Emit CLIENT_TOKEN_ISSUED (success) or CLIENT_TOKEN_ISSUE_FAILED (failure) audit event
  8. Return ManagedClientTokenResponse

revokeAllTokens logic:
  1. Find all active jti keys for client in Redis (mcam:token:{jti} where sub=clientId)
  2. Write revocation tombstones (mcam:revoked:{jti}) with TTL = token expiry + 60s
  3. Delete original mcam:token:{jti} entries
  4. Emit CLIENT_TOKEN_REVOKED audit event
  5. Return 204

introspectToken logic:
  1. Parse JWT without throwing — catch all exceptions → return active=false
  2. Check Redis: if mcam:revoked:{jti} exists → active=false
  3. Check Redis: if mcam:token:{jti} absent → active=false
  4. Verify signature against public key from keystore
  5. Check expiry → active=false if expired
  6. Return active=true with full claims, or active=false with nulls

NOTE on Redis scan: avoid KEYS command in production.
  Strategy: store per-client token index as Redis SET (mcam:client-tokens:{clientId})
  containing all live jti values. Revocation iterates this set. TTL on set members
  managed via EXPIREAT on individual entries.
```

---

### 4.2 Redis Service

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRedisService.java`

```java
// Interface:
//   void storeToken(String jti, UUID clientId, long ttlSeconds);
//   void revokeToken(String jti, long tombstoneTtlSeconds);
//   boolean isRevoked(String jti);
//   boolean isActive(String jti);
//   Set<String> getClientTokens(UUID clientId);
//   void storeGraceSecret(UUID clientId, String prevHash, long graceTtlSeconds);
//   Optional<String> getGraceSecret(UUID clientId);
//   void removeGraceSecret(UUID clientId);
//   void incrementRateLimit(UUID clientId);
//   long getRateLimitCount(UUID clientId);
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRedisServiceImpl.java`

```
Annotations : @Service
Constructor : StringRedisTemplate (already configured via RedisConfig)
Redis keys  :
  mcam:token:{jti}              → String "clientId"        TTL = tokenTtlSeconds
  mcam:revoked:{jti}            → String "1"               TTL = tokenTtl + 60s
  mcam:client-tokens:{clientId} → Set of jti strings       no TTL (members expire via token TTL)
  mcam:grace:{clientId}         → String prevSecretHash    TTL = rotationGracePeriodSeconds
  mcam:ratelimit:{clientId}     → String count             TTL = 60s (sliding window)
```

---

### 4.3 Security Filter

#### File to Create: `src/main/java/com/umdc/backoffice/security/filter/ManagedClientTokenFilter.java`

```
Extends     : OncePerRequestFilter
Annotations : @Component
Constructor : ManagedClientTokenService, ManagementAuthenticatorProperties, KeystoreUtil

Logic:
  1. Read Authorization: Bearer <token>
  2. If absent → filterChain.doFilter() (pass through — SessionJwtAuthenticationFilter handles next)
  3. Attempt to parse JWT header/payload (no signature verify yet) to read "type" claim
  4. If type != "M2M" → filterChain.doFilter() (let SessionJwtAuthenticationFilter handle)
  5. If type == "M2M":
       a. Verify signature (RS256, public key from keystore)
       b. Check revocation via ManagedClientTokenService.isTokenActive(jti)
       c. If invalid/revoked → 401 {"error":"invalid_token"}
       d. If valid → build Authentication with clientId as principal, scopes as authorities
                     (e.g. "SCOPE_backbone:users:read" → SimpleGrantedAuthority)
          SecurityContextHolder.getContext().setAuthentication(authentication)
       e. filterChain.doFilter()
```

#### File to Modify: `src/main/java/com/umdc/backoffice/security/filter/SessionJwtAuthenticationFilter.java`

Add early-exit guard at start of `doFilterInternal`:
```java
// If a previous filter (ManagedClientTokenFilter) already authenticated
// this request, skip session-token processing entirely.
if (SecurityContextHolder.getContext().getAuthentication() != null) {
    filterChain.doFilter(request, response);
    return;
}
```

#### File to Modify: `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java`

1. Inject `ManagedClientTokenFilter` via constructor parameter
2. Register filter BEFORE `SessionJwtAuthenticationFilter`:
```java
.addFilterBefore(managedClientTokenFilter, SessionJwtAuthenticationFilter.class)
.addFilterBefore(sessionJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

---

### 4.4 Token Endpoint Addition to Controller Interface

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientApi.java`

Add token-related endpoint declarations:
```java
@PostMapping("/token")
ResponseEntity<?> issueManagedClientToken(@Valid @RequestBody ManagedClientTokenRequest request,
                                           HttpServletRequest httpRequest);

@DeleteMapping("/{clientId}/tokens")
ResponseEntity<?> revokeAllClientTokens(@PathVariable UUID clientId);

@PostMapping("/introspect")
ResponseEntity<?> introspectManagedClientToken(@Valid @RequestBody TokenIntrospectRequest request);
```

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientController.java`

Add constructor injection of `ManagedClientTokenService`; implement new methods.

---

### Phase 3 Quality Gate Checklist

- [ ] `mvn -DskipTests compile` — green
- [ ] `mvn pmd:check` — 0 violations
- [ ] `POST /token` with valid credentials returns signed JWT (AC-TOK-01)
- [ ] JWT `types` claim = `"M2M"` and contains all required claims (AC-TOK-01)
- [ ] `POST /token` with bad secret returns `401 {"error":"invalid_client"}` (AC-TOK-02)
- [ ] Inactive client returns `401` (AC-TOK-03)
- [ ] Out-of-scope request returns `400 {"error":"invalid_scope"}` (AC-TOK-04)
- [ ] `DELETE /{clientId}/tokens` + introspect → `active=false` (AC-REV-01)
- [ ] `POST /introspect` on valid token → `active=true` (AC-INT-01)
- [ ] `POST /introspect` on revoked/expired → `active=false` (AC-INT-02)
- [ ] `ManagedClientTokenFilter` does not interfere with existing session-token auth

---

## 5. Phase 4 — Secret Rotation & Audit

**Goal:** Implement secret rotation with grace period and confirm full audit trail coverage for all lifecycle events.

**Complexity:** Medium  
**Dependencies:** Phase 3 complete  
**Estimated Duration:** 2–3 days

---

### 5.1 Secret Rotation Service

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRotationService.java`

```java
// Interface:
//   ResponseEntity<?> rotateSecret(UUID clientId, String requestorIp);
```

#### File to Create: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRotationServiceImpl.java`

```
Annotations : @Service
Constructor : ManagedClientRepository, ManagedClientSecretHashService,
              ManagedClientRedisService, ManagedClientAuditService,
              ManagementAuthenticatorProperties

rotateSecret logic:
  1. Find client by clientId — 404 if absent
  2. Generate new 32-byte SecureRandom secret (Base64URL)
  3. Async: hash new secret → newHash (via ManagedClientSecretHashService.hashSecret)
  4. Store CURRENT secretHash in Redis:
       ManagedClientRedisService.storeGraceSecret(clientId, currentHash, gracePeriodSeconds)
       Key: mcam:grace:{clientId}, TTL = rotationGracePeriodSeconds
  5. Update entity: secretHash = newHash, prevSecretHash = currentHash (in DB for persistence)
                    secretLastRotatedAt = now()
  6. Persist entity
  7. Emit CLIENT_SECRET_ROTATED audit event (include gracePeriodSeconds in details, NEVER hashes)
  8. Return ManagedClientSecretRotateResponse with plaintext newSecret (returned once only)

Authentication validation in ManagedClientTokenServiceImpl.issueToken checks BOTH:
  - entity.secretHash (new secret)
  - ManagedClientRedisService.getGraceSecret(clientId) (old secret, if still in Redis TTL)
  After grace TTL expires, Redis key is gone → old secret no longer valid (FR-12, FR-13).
  entity.prevSecretHash is cleared on next rotation or deactivation (cleanup migration or service).
```

---

### 5.2 Rotation Endpoint Addition to API

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientApi.java`

```java
@PostMapping("/{clientId}/rotate-secret")
ResponseEntity<?> rotateManagedClientSecret(@PathVariable UUID clientId,
                                             HttpServletRequest httpRequest);
```

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientController.java`

Add constructor injection of `ManagedClientRotationService`; implement `rotateManagedClientSecret`.

---

### 5.3 Deactivation → Implicit Token Revocation

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientServiceImpl.java`

In `updateClient`: if `request.active == false` AND `entity.active == true`, call  
`ManagedClientTokenService.revokeAllTokens(clientId)` before persisting (FR-17).

In `deleteClient`: call `ManagedClientTokenService.revokeAllTokens(clientId)` before deletion (FR-30).

---

### 5.4 Audit Coverage Verification

| Event | Triggered In | Audit Call |
|---|---|---|
| `CLIENT_REGISTERED` | `ManagedClientServiceImpl.registerClient` | `auditService.record(clientId, CLIENT_REGISTERED, ...)` |
| `CLIENT_UPDATED` | `ManagedClientServiceImpl.updateClient` | `auditService.record(...)` |
| `CLIENT_DEACTIVATED` | `ManagedClientServiceImpl.updateClient` (active→false) | `auditService.record(...)` |
| `CLIENT_DELETED` | `ManagedClientServiceImpl.deleteClient` | `auditService.record(...)` |
| `CLIENT_SECRET_ROTATED` | `ManagedClientRotationServiceImpl.rotateSecret` | `auditService.record(...)` |
| `CLIENT_TOKEN_ISSUED` | `ManagedClientTokenServiceImpl.issueToken` (success) | `auditService.record(...)` |
| `CLIENT_TOKEN_ISSUE_FAILED` | `ManagedClientTokenServiceImpl.issueToken` (failure) | `auditService.record(...)` |
| `CLIENT_TOKEN_REVOKED` | `ManagedClientTokenServiceImpl.revokeAllTokens` | `auditService.record(...)` |
| `CLIENT_INTROSPECTION_CALLED` | `ManagedClientTokenServiceImpl.introspectToken` | `auditService.record(...)` |

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/iam/audit/service/AuditEventServiceImpl.java`

In `findEvents`: when `eventType` is one of the 9 M2M types (checked via a helper set), delegate to `ManagedClientAuditEventRepository` instead of `AuditEventRepository`. Return unified `List<AuditEventTO>` by adapting M2M events to the existing `AuditEventTO` shape (clientId maps to userId field; details remain).

This preserves the existing `GET /api/v1/iam/audit/events` API contract (AC-10).

#### File to Modify: `src/main/java/com/umdc/backoffice/v1/iam/audit/service/AuditEventService.java`

No signature change needed — behavior extension is internal to `AuditEventServiceImpl`.

---

### Phase 4 Quality Gate Checklist

- [ ] `mvn pmd:check` — 0 violations
- [ ] `POST /{clientId}/rotate-secret` returns new secret + `gracePeriodSeconds` (AC-ROT-01)
- [ ] Old secret valid during grace period (AC-ROT-01 grace behavior)
- [ ] Old secret rejected after Redis TTL expires (FR-13)
- [ ] `CLIENT_SECRET_ROTATED` audit event appears in `GET /api/v1/iam/audit/events?eventType=CLIENT_SECRET_ROTATED` (AC-AUD-01)
- [ ] Deactivating client also revokes all tokens (FR-17)
- [ ] All 9 audit event types verified against acceptance criteria (AC-AUD-01)

---

## 6. Phase 5 — Tests + PMD + JaCoCo Gates

**Goal:** Full test suite covering all 15 acceptance criteria. CI-ready quality gates.

**Complexity:** Medium  
**Dependencies:** Phases 1–4 complete  
**Estimated Duration:** 3–4 days

---

### 6.1 Unit Tests

All unit tests in `src/test/java/com/umdc/backoffice/v1/managedclient/`.

#### File to Create: `...service/ManagedClientServiceImplTest.java`

```
Framework   : JUnit 5 + Mockito
Covers      : registerClient (happy, duplicate, validation), listClients, getClient (found/404),
              updateClient (partial, deactivate triggers revoke), deleteClient (triggers revoke)
AC coverage : AC-REG-01, AC-REG-02, AC-REG-03, AC-SEC-02
Target      : ≥ 80% line coverage (NFR-Q-02)
```

#### File to Create: `...service/ManagedClientTokenServiceImplTest.java`

```
Covers      : issueToken (valid credentials, bad secret, inactive client, scope overflow),
              revokeAllTokens, introspectToken (active, expired, revoked)
AC coverage : AC-TOK-01, AC-TOK-02, AC-TOK-03, AC-TOK-04, AC-REV-01, AC-INT-01, AC-INT-02
Mocks       : ManagedClientRepository, ManagedClientSecretHashService,
              ManagedClientRedisService, ManagedClientAuditService,
              ManagementAuthenticatorProperties
```

#### File to Create: `...service/ManagedClientRotationServiceImplTest.java`

```
Covers      : rotateSecret (found, not found), grace period stored in Redis,
              new secret different from old, audit event emitted
AC coverage : AC-ROT-01
```

#### File to Create: `...service/ManagedClientSecretHashServiceImplTest.java`

```
Covers      : hashSecret produces valid BCrypt hash, matches returns true for correct secret,
              matchesWithConstantTime returns false for wrong secret,
              raw secret NOT present in hash string
AC coverage : AC-SEC-01 (partial — secret not in hash)
```

#### File to Create: `...service/ManagedClientRedisServiceImplTest.java`

```
Covers      : storeToken, revokeToken, isRevoked, isActive, rate limit increment/read,
              grace secret store/retrieve/remove
Uses        : EmbeddedRedis or Mockito for StringRedisTemplate
```

#### File to Create: `...mapper/ManagedClientMapperTest.java`

```
Covers      : Entity→TO (no secret fields in output), CreateRequest→Entity (no id/hash),
              partial update mapping
AC coverage : AC-SEC-02
```

---

### 6.2 Integration Tests

#### File to Create: `src/test/java/com/umdc/backoffice/v1/managedclient/ManagedClientControllerIT.java`

```
Framework       : @SpringBootTest + MockMvc (or @WebMvcTest)
Test slices     : CRUD endpoints — register, list, get, update, delete
Authentication  : Mock admin JWT in Authorization header
AC coverage     : AC-REG-01, AC-REG-02, AC-REG-03, AC-SEC-02
DB              : H2 or TestContainers (PostgreSQL)
```

#### File to Create: `src/test/java/com/umdc/backoffice/v1/managedclient/ManagedClientTokenControllerIT.java`

```
Covers          : POST /token, DELETE /{clientId}/tokens, POST /introspect
Scenarios       : happy path, invalid credentials, inactive client, scope overflow,
                  revoke then introspect
AC coverage     : AC-TOK-01–04, AC-REV-01, AC-INT-01, AC-INT-02
```

#### File to Create: `src/test/java/com/umdc/backoffice/v1/managedclient/ManagedClientRotationIT.java`

```
Covers          : rotate-secret endpoint, grace period validation
AC coverage     : AC-ROT-01
```

#### File to Create: `src/test/java/com/umdc/backoffice/security/filter/ManagedClientTokenFilterTest.java`

```
Covers          : M2M token → sets SecurityContext, invalid M2M token → 401,
                  non-M2M token → passes through to SessionJwtAuthenticationFilter,
                  no Authorization header → passes through
```

---

### 6.3 PMD — Zero Violations

PMD ruleset (`ruleset.xml`) already enforced on the build. All new classes must:
- Use constructor injection (no field `@Autowired`)
- Not use system output or `System.exit`
- Keep method complexity ≤ threshold
- Not suppress PMD rules with `@SuppressWarnings("PMD.*")`

**Command to validate:**
```bash
export REPSY_ACCOUNT_USER=<user>
export REPSY_ACCOUNT_PASSWORD=<pass>
mvn pmd:check pmd:cpd-check
```

---

### 6.4 JaCoCo Report

**Command to generate:**
```bash
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

The project minimum is currently 0% (report-only). For MCAM specifically, the target is **≥ 80% line coverage** on service-layer classes (NFR-Q-02). This is a team commitment, not a build gate enforcement — but the JaCoCo HTML report must show the MCAM packages reaching this threshold before release sign-off.

---

### Phase 5 Quality Gate Checklist

- [ ] `mvn test` — all tests green
- [ ] `mvn pmd:check` — 0 violations (including new test classes)
- [ ] `mvn test jacoco:report` — MCAM service package ≥ 80% line coverage
- [ ] AC-PMD-01 verified: no PMD violations in any file under `v1/managedclient/`
- [ ] All 15 acceptance criteria have at least one automated test asserting the expected behavior

---

## 7. Phase 6 — OpenAPI Spec + Documentation

**Goal:** API contract published, changelog updated, release notes prepared.

**Complexity:** Low  
**Dependencies:** Phase 5 complete  
**Estimated Duration:** 1–2 days

---

### 7.1 OpenAPI Spec

#### File to Modify: `src/main/resources/api.yaml`

Append (do NOT replace existing content):

1. New tag:
   ```yaml
   tags:
     - name: "managed-clients"
       description: "Management Client Authentication Manager — M2M credential lifecycle"
   ```

2. All paths from Section 6 of `docs/requirements/managed-client-auth.md` — 9 path entries.

3. All schemas from Section 6 — 8 schema components:
   - `ManagedClientCreateRequest`
   - `ManagedClientCreateResponse`
   - `ManagedClientTO`
   - `ManagedClientUpdateRequest`
   - `ManagedClientTokenRequest`
   - `ManagedClientTokenResponse`
   - `ManagedClientSecretRotateResponse`
   - `ManagedClientTokenIntrospectResponse`

4. `TokenIntrospectRequest` — reuse existing if present in `api.yaml`; define new schema if absent.

5. `AuditEventType` enum values in `api.yaml` extended with the 9 new M2M values (additive — backward compatible with existing API Reviewer / code-gen consumers).

> ⚠️ The existing `backbone_rest-openapi.yaml.bak` at `META-INF/backbone_rest-openapi.yaml.bak` should be reviewed and reconciled with the canonical `api.yaml` before final publication.

---

### 7.2 Environment Variable Manifest

#### File to Create: `docs/env/mcam-env-vars.md`

| Variable | Required | Default | Description |
|---|---|---|---|
| `MCAM_KEY_ALIAS` | Yes | — | Key alias in PKCS12 keystore for M2M token signing |
| `MCAM_KEYSTORE_LOCATION` | Yes | — | Path to PKCS12 keystore (classpath or file:) |
| `MCAM_KEYSTORE_PASSWORD` | Yes | — | Keystore password — inject from secrets manager |
| `MCAM_TOKEN_TTL_SECONDS` | No | `3600` | M2M token TTL (max 86400) |
| `MCAM_ROTATION_GRACE_SECONDS` | No | `300` | Grace period after secret rotation |
| `MCAM_RATE_LIMIT_RPM` | No | `60` | Max token requests per minute per clientId |

---

### 7.3 CHANGELOG Update

#### File to Modify: `CHANGELOG` (project root)

Add entry:
```
## [Unreleased] — MCAM v1

### Added
- Management Client Authentication Manager (MCAM) — M2M OAuth2 client credential lifecycle
  - POST   /api/v1/managed-clients              — Register managed client
  - GET    /api/v1/managed-clients              — List clients (paginated)
  - GET    /api/v1/managed-clients/{clientId}   — Get client detail
  - PUT    /api/v1/managed-clients/{clientId}   — Update client metadata
  - DELETE /api/v1/managed-clients/{clientId}   — Delete client + revoke tokens
  - POST   /api/v1/managed-clients/token        — Issue M2M access token (public)
  - POST   /api/v1/managed-clients/{clientId}/rotate-secret — Rotate secret
  - DELETE /api/v1/managed-clients/{clientId}/tokens        — Revoke all tokens
  - POST   /api/v1/managed-clients/introspect   — Introspect M2M token
- Flyway migrations V2, V3, V4 (managed_client, managed_client_audit_event tables)
- ManagedClientTokenFilter — RS256 M2M JWT validation in Spring Security filter chain
- AuditEventType extended with 9 new M2M event types
- ManagementAuthenticatorProperties extended with tokenTtlSeconds, rotationGracePeriodSeconds

### Security
- client_secret BCrypt-hashed (strength ≥ 12); plaintext never logged or stored
- All key material injected via environment variables
- Secret rotation grace period via Redis TTL
```

---

### 7.4 Docker Image

> Delegated to **DevOps Engineer** agent.

Confirm that the PKCS12 keystore (`MCAM_KEYSTORE_LOCATION`) is available to the container either:
- Mounted as a Kubernetes Secret volume, OR
- Embedded in the image at a known path (not recommended for production)

The existing `Dockerfile` (`amazoncorretto:21-alpine3.20`, port 8082) requires no structural changes for MCAM. New env vars listed above must be documented in the Docker run / Helm values template.

---

### Phase 6 Quality Gate Checklist

- [ ] `api.yaml` validates against OpenAPI 3.0 specification (no broken `$ref`)
- [ ] No existing endpoint schemas removed or modified
- [ ] `CHANGELOG` updated
- [ ] `docs/env/mcam-env-vars.md` created and reviewed
- [ ] Release notes distributed to integration teams
- [ ] Docker image built and smoke-tested with all new env vars set

---

## 8. Risk Register

| # | Risk | Likelihood | Impact | Phase | Mitigation |
|---|---|---|---|---|---|
| **R-01** | **BCrypt hashing blocks Tomcat threads under load, breaching p99 > 250 ms (NFR-P-01)** | Medium | High | Phase 3 | Isolate BCrypt in `ManagedClientSecretHashServiceImpl` annotated `@Async("mcamHashExecutor")`. Provision a dedicated `ThreadPoolTaskExecutor` (`ManagedClientAsyncConfig`) with min 2, max 4 threads. Load-test before release. |
| **R-02** | **Redis unavailability blocks token issuance and introspection entirely** | Low | High | Phase 3 | Implement circuit-breaker pattern in `ManagedClientRedisServiceImpl`: if Redis call fails, fall back to DB-based revocation check via `ManagedClientRepository`. Log Redis failures at ERROR level; emit a health indicator. Do NOT make token issuance fail-closed on Redis outage — fall back gracefully. |
| **R-03** | **`SessionJwtAuthenticationFilter` rejects M2M tokens before `ManagedClientTokenFilter` can process them (type mismatch causes premature 401)** | High | High | Phase 3 | Register `ManagedClientTokenFilter` BEFORE `SessionJwtAuthenticationFilter` in `SecurityConfig`. Add early-return guard to `SessionJwtAuthenticationFilter`: skip if `SecurityContextHolder.getContext().getAuthentication() != null`. Validate with integration test that covers both session-token and M2M-token requests in the same filter chain. |
| **R-04** | **`AuditEventType` enum extension causes serialisation or OpenAPI discriminator breaks for existing audit consumers** | Low | Medium | Phase 1 | Enum extension is additive — Java enums support new constants without breaking existing code. Validate that `api.yaml` enum extension does not break API Reviewer tooling or generated clients by checking `api.yaml` before merging. The V1 migration comment explicitly anticipated this pattern. |
| **R-05** | **Grace-period old secret not cleaned from `managed_client.prev_secret_hash` DB column after Redis TTL expires** | Medium | Medium | Phase 4 | Redis TTL handles the authentication grace window (FR-12, FR-13). For DB hygiene, implement a `@Scheduled` cleanup task in `ManagedClientRotationServiceImpl` (or a separate `ManagedClientMaintenanceService`) that clears `prevSecretHash` on entities where `secretLastRotatedAt + gracePeriodSeconds < now()`. Run on a configurable cron (e.g. every 10 min). |

---

## 9. Sprint Breakdown

Assumes a solo developer working 5-day weeks with standard review/testing overhead.

---

### Sprint 1 (Days 1–10): Foundation + CRUD

| Day | Work Item | Phase |
|---|---|---|
| 1 | Flyway migrations V2, V3, V4. Validate SQL locally. | P1 |
| 2 | `ManagedClientEntity`, `ManagedClientAuditEventEntity`, both repositories. | P1 |
| 2–3 | `ManagementAuthenticatorProperties` extension, `application.yml` MCAM block. | P1 |
| 3 | `AuditEventType` enum extension (9 new values). | P1 |
| 4 | All 9 DTO/TO classes under `api/to/`. | P1 |
| 4–5 | `ManagedClientMapper` with all mappings. Compile and verify MapStruct generates correctly. | P1 |
| 5 | `ManagedClientAuditService` + `ManagedClientAuditServiceImpl`. | P2 |
| 6 | `ManagedClientSecretHashService` + `ManagedClientSecretHashServiceImpl` + `ManagedClientAsyncConfig`. | P2 |
| 6–7 | `ManagedClientService` + `ManagedClientServiceImpl` (CRUD only, no token ops). | P2 |
| 7–8 | `ManagedClientApi` interface + `ManagedClientController` (CRUD endpoints). | P2 |
| 8 | `SecurityConfig` — add permit rules for `/token` and `/introspect`. | P2 |
| 9 | Smoke test all 5 CRUD endpoints. Fix any issues. | P2 |
| 10 | `mvn pmd:check` pass. Unit tests for `ManagedClientServiceImpl`. | P5 (partial) |

**Sprint 1 Exit Criteria:** All CRUD endpoints functional, PMD clean, `registerClient` returns secret once, `getClient` has no secret fields.

---

### Sprint 2 (Days 11–20): Auth Flow + Rotation

| Day | Work Item | Phase |
|---|---|---|
| 11 | `ManagedClientRedisService` + `ManagedClientRedisServiceImpl`. | P3 |
| 12–13 | `ManagedClientTokenServiceImpl` — `issueToken` (happy path + error cases). | P3 |
| 13–14 | `ManagedClientTokenFilter` — parse, verify RS256, set SecurityContext. | P3 |
| 14 | `SessionJwtAuthenticationFilter` — add early-return guard. | P3 |
| 14 | `SecurityConfig` — register `ManagedClientTokenFilter` before session filter. | P3 |
| 15 | `ManagedClientTokenServiceImpl` — `revokeAllTokens` + `introspectToken`. | P3 |
| 15 | Controller: add `/token`, `/{clientId}/tokens`, `/introspect` endpoints. | P3 |
| 16 | `ManagedClientRotationServiceImpl` — `rotateSecret` with grace period. | P4 |
| 16 | Controller: add `/{clientId}/rotate-secret` endpoint. | P4 |
| 17 | Deactivation→revoke wiring in `ManagedClientServiceImpl`. Delete→revoke wiring. | P4 |
| 17 | Audit event coverage: verify all 9 events are emitted. | P4 |
| 17–18 | `AuditEventServiceImpl` — extend `findEvents` for M2M event types. | P4 |
| 18–19 | `ManagedClientMaintenanceService` — scheduled `prevSecretHash` cleanup. | P4 |
| 19–20 | `mvn pmd:check` pass. Fix violations. Smoke test all 9 endpoints. | P5 (partial) |

**Sprint 2 Exit Criteria:** All 9 endpoints functional, M2M token issuance/revocation/introspection verified, secret rotation with grace period working, all 9 audit events queryable.

---

### Sprint 3 (Days 21–30): Tests + OpenAPI + Release

| Day | Work Item | Phase |
|---|---|---|
| 21–22 | Unit tests: `ManagedClientTokenServiceImplTest`, `ManagedClientRotationServiceImplTest`. | P5 |
| 22–23 | Unit tests: `ManagedClientRedisServiceImplTest`, `ManagedClientSecretHashServiceImplTest`, `ManagedClientMapperTest`. | P5 |
| 23–24 | Integration tests: `ManagedClientControllerIT` — CRUD scenarios. | P5 |
| 24–25 | Integration tests: `ManagedClientTokenControllerIT`, `ManagedClientRotationIT`. | P5 |
| 25 | `ManagedClientTokenFilterTest` — filter chain behavior. | P5 |
| 26 | `mvn test jacoco:report` — verify MCAM service coverage ≥ 80%. Add tests for any gaps. | P5 |
| 26 | `mvn pmd:check` on all new test files — 0 violations. | P5 |
| 27 | `api.yaml` — append all 9 paths + 8 schema components + AuditEventType extension. | P6 |
| 27 | Validate `api.yaml` against OpenAPI 3.0 spec (Swagger Editor or `openapi-generator`). | P6 |
| 28 | `docs/env/mcam-env-vars.md` created. `CHANGELOG` updated. | P6 |
| 28 | Full `mvn test` run — all tests green. | P5/P6 |
| 29 | Docker image build + smoke test with all MCAM env vars. | P6 |
| 30 | PM sign-off: release checklist complete. Release notes sent to integration teams. | P6 |

**Sprint 3 Exit Criteria:** All 15 acceptance criteria have automated tests, `mvn test` green, PMD 0 violations, JaCoCo ≥ 80% on MCAM packages, `api.yaml` valid, CHANGELOG updated.

---

## 10. Dependency Graph

```
Phase 1 (Foundation)
    │
    ├── Flyway V2, V3, V4              ← no code deps
    ├── ManagedClientEntity            ← Flyway V2
    ├── ManagedClientAuditEventEntity  ← Flyway V3
    ├── Both Repositories              ← Entities
    ├── ManagementAuthenticatorProperties update ← no deps
    ├── AuditEventType extension       ← no deps (additive)
    ├── All DTOs                       ← no deps
    └── ManagedClientMapper            ← Entities + DTOs
                │
Phase 2 (CRUD API)
    │
    ├── ManagedClientAuditService      ← Repository, AuditEventType
    ├── ManagedClientSecretHashService ← BCryptPasswordEncoder (SecurityBeansConfig)
    ├── ManagedClientAsyncConfig       ← no deps
    ├── ManagedClientServiceImpl       ← Repository, Mapper, AuditService, HashService
    └── ManagedClientController        ← ManagedClientService
                │
Phase 3 (Auth Flow)
    │
    ├── ManagedClientRedisService      ← StringRedisTemplate (RedisConfig)
    ├── ManagedClientTokenServiceImpl  ← Repository, HashService, RedisService,
    │                                     AuditService, ManagementAuthenticatorProperties,
    │                                     KeystoreUtil
    ├── ManagedClientTokenFilter       ← TokenService, ManagementAuthenticatorProperties,
    │                                     KeystoreUtil
    ├── SecurityConfig update          ← ManagedClientTokenFilter (inject)
    ├── SessionJwtAuthenticationFilter update ← guard only (no new deps)
    └── ManagedClientController update ← ManagedClientTokenService
                │
Phase 4 (Rotation & Audit)
    │
    ├── ManagedClientRotationServiceImpl ← Repository, HashService, RedisService, AuditService,
    │                                       ManagementAuthenticatorProperties
    ├── ManagedClientController update  ← ManagedClientRotationService
    ├── ManagedClientServiceImpl update ← ManagedClientTokenService (revoke on deactivate/delete)
    ├── ManagedClientMaintenanceService ← Repository, ManagementAuthenticatorProperties
    └── AuditEventServiceImpl update    ← ManagedClientAuditEventRepository
                │
Phase 5 (Tests)
    │
    └── All prior phases complete
                │
Phase 6 (OpenAPI + Docs)
    │
    └── Phase 5 complete
```

---

## 📌 Document Control

| Field | Value |
|---|---|
| **Document ID** | TASK-03 |
| **Author** | Project Manager Agent |
| **Created** | 2025-07-14 |
| **Requires** | TASK-02 (managed-client-auth.md) |
| **Status** | Ready for Developer Review |
| **Target Sprints** | Sprint 1–3 (30 working days, solo developer) |
| **Review By** | Developer Agent · QA Agent · Security Reviewer · DevOps Engineer |

---

*End of Document*

