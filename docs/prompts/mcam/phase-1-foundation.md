# 🏗️ MCAM Phase 1 — Foundation & Domain Setup

> **Prompt for:** `developer` agent  
> **Depends on:** Nothing (first phase)  
> **Estimated duration:** 3–4 days  
> **Complexity:** Medium  
> **Reference:** `docs/plans/mcam-implementation-plan.md` §2

---

## 🎯 Goal

Establish all persistent structures, configuration, and shared infrastructure required by the Management Client Authentication Manager (MCAM) feature.  
**No new HTTP endpoints are created in this phase.**

---

## 📐 Architecture Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| AC-01 | Package root: `com.umdc.backoffice.v1.managedclient.*` |
| AC-02 | Constructor injection only — no `@Autowired` field injection |
| AC-03 | PMD zero violations — `mvn pmd:check` must pass |
| AC-04 | MapStruct `@Mapper` using `config = MapperAppConfig.class` from `com.umdc.commons.services` |
| AC-05 | No hardcoded secrets — all config values via `${ENV_VAR:default}` in `application.yml` |
| AC-06 | `AuditEventType` extension is additive — no removal or reordering of existing constants |
| AC-07 | No Lombok — use plain Java classes or records as in the existing codebase |

---

## 📋 Tasks

### TASK 1.1 — Flyway Migrations

Create the following three migration files **exactly as specified**:

#### `src/main/resources/db/migration/V2__create_managed_client.sql`

```sql
CREATE TABLE IF NOT EXISTS general.managed_client (
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
CREATE INDEX IF NOT EXISTS idx_managed_client_application_id ON public.managed_client (application_id);
CREATE INDEX IF NOT EXISTS idx_managed_client_active ON public.managed_client (active);
CREATE INDEX IF NOT EXISTS idx_managed_client_app_active ON public.managed_client (application_id, active);
```

#### `src/main/resources/db/migration/V3__create_managed_client_audit_event.sql`

```sql
CREATE TABLE IF NOT EXISTS general.managed_client_audit_event (
    id          UUID        NOT NULL,
    client_id   UUID        NOT NULL,
    event_type  VARCHAR(64) NOT NULL,
    ip_address  VARCHAR(45),
    outcome     VARCHAR(16) NOT NULL,
    details     JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT managed_client_audit_event_pk PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_mcam_audit_client_id    ON public.managed_client_audit_event (client_id);
CREATE INDEX IF NOT EXISTS idx_mcam_audit_event_type   ON public.managed_client_audit_event (event_type);
CREATE INDEX IF NOT EXISTS idx_mcam_audit_occurred_at  ON public.managed_client_audit_event (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_mcam_audit_client_event ON public.managed_client_audit_event (client_id, event_type);
```

#### `src/main/resources/db/migration/V4__extend_audit_event_check.sql`

Add M2M event type values to any existing `audit_event_type` CHECK constraint on the `audit_event` table so future M2M writes to that table do not cause constraint violations. Read `V1__create_audit_event.sql` first to see the current constraint definition and append the 9 new M2M event type values:
`CLIENT_REGISTERED`, `CLIENT_UPDATED`, `CLIENT_DEACTIVATED`, `CLIENT_DELETED`, `CLIENT_SECRET_ROTATED`, `CLIENT_TOKEN_ISSUED`, `CLIENT_TOKEN_ISSUE_FAILED`, `CLIENT_TOKEN_REVOKED`, `CLIENT_INTROSPECTION_CALLED`.

---

### TASK 1.2 — JPA Entities

#### `src/main/java/com/umdc/backoffice/v1/managedclient/domain/ManagedClientEntity.java`

- `@Entity @Table(name = "managed_client", schema = "public")`
- Fields: `id` (UUID, `@Id @GeneratedValue`), `name`, `description`, `applicationId` (UUID), `secretHash`, `prevSecretHash` (nullable), `scopes` (`List<String>` mapped as `TEXT[]`), `active` (boolean), `createdAt`, `lastUpdatedAt`, `secretLastRotatedAt`
- `@PrePersist`: set `createdAt = Instant.now()` if null
- `@PreUpdate`: set `lastUpdatedAt = Instant.now()`
- Use `///` doc comment style matching the existing codebase
- Map `scopes` via `@Column(columnDefinition = "TEXT[]")` with a custom Hibernate `@Type` or `@ElementCollection` — check how the existing entities in the `com.umdc.persistence` module handle array columns and match that style

#### `src/main/java/com/umdc/backoffice/v1/managedclient/domain/ManagedClientAuditEventEntity.java`

- `@Entity @Table(name = "managed_client_audit_event", schema = "public")`
- Fields: `id` (UUID, PK), `clientId` (UUID), `eventType` (`AuditEventType` enum — stored as `@Enumerated(EnumType.STRING)`), `ipAddress`, `outcome` (String), `details` (String / JSONB), `occurredAt`
- `@PrePersist`: set `occurredAt = Instant.now()` if null

---

### TASK 1.3 — Repositories

#### `src/main/java/com/umdc/backoffice/v1/managedclient/repository/ManagedClientRepository.java`

```java
public interface ManagedClientRepository extends JpaRepository<ManagedClientEntity, UUID> {
    Optional<ManagedClientEntity> findByIdAndActiveTrue(UUID id);
    boolean existsByNameAndApplicationId(String name, UUID applicationId);
    Page<ManagedClientEntity> findByApplicationId(UUID applicationId, Pageable pageable);
    Page<ManagedClientEntity> findByActive(boolean active, Pageable pageable);
    Page<ManagedClientEntity> findByApplicationIdAndActive(UUID applicationId, boolean active, Pageable pageable);
}
```

#### `src/main/java/com/umdc/backoffice/v1/managedclient/repository/ManagedClientAuditEventRepository.java`

```java
public interface ManagedClientAuditEventRepository extends JpaRepository<ManagedClientAuditEventEntity, UUID> {
    Page<ManagedClientAuditEventEntity> findByClientId(UUID clientId, Pageable pageable);
    Page<ManagedClientAuditEventEntity> findByEventType(AuditEventType type, Pageable pageable);
    Page<ManagedClientAuditEventEntity> findByClientIdAndEventType(UUID clientId, AuditEventType type, Pageable pageable);
}
```

---

### TASK 1.4 — Configuration Properties Extension

#### Modify: `src/main/java/com/umdc/backoffice/property/ManagementAuthenticatorProperties.java`

Add the following fields following the **existing getter/setter style** in that class:

```java
/// M2M access token TTL in seconds. Defaults to 3600 (1 hour).
private long tokenTtlSeconds;

/// Grace period in seconds during which the old secret remains valid after rotation.
private long rotationGracePeriodSeconds;

/// Maximum token issuance requests per minute per clientId (sliding window).
private int rateLimitRpm;
```

#### Modify: `src/main/resources/application.yml`

Append to the existing `umdc.security.managementAuthenticator` block (do **not** create a duplicate key):

```yaml
umdc:
  security:
    managementAuthenticator:
      tokenTtlSeconds: ${MCAM_TOKEN_TTL_SECONDS:3600}
      rotationGracePeriodSeconds: ${MCAM_ROTATION_GRACE_SECONDS:300}
      rateLimitRpm: ${MCAM_RATE_LIMIT_RPM:60}
      keyAlias: ${MCAM_KEY_ALIAS}
      keystore:
        location: ${MCAM_KEYSTORE_LOCATION}
        password: ${MCAM_KEYSTORE_PASSWORD}
        type: PKCS12
```

---

### TASK 1.5 — AuditEventType Extension

#### Modify: `src/main/java/com/umdc/backoffice/v1/iam/audit/domain/AuditEventType.java`

**Append** the following constants at the end of the enum (additive — do not reorder or remove existing values):

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

/// M2M token issuance failed (bad credentials or inactive client).
CLIENT_TOKEN_ISSUE_FAILED,

/// All active tokens revoked for a client.
CLIENT_TOKEN_REVOKED,

/// Token introspection endpoint called.
CLIENT_INTROSPECTION_CALLED
```

---

### TASK 1.6 — DTOs / Transfer Objects

Create all 9 classes under `src/main/java/com/umdc/backoffice/v1/managedclient/api/to/`.  
Use **Java records or plain classes** — match the style of existing TOs in the codebase (e.g., `UserTO`, `RoleTO`). No Lombok. All fields must have `///` doc comments.

| Class | Fields |
|---|---|
| `ManagedClientCreateRequest` | `name` (required), `description`, `applicationId` (UUID, required), `scopes` (List\<String\>, required), `active` (boolean, default true) |
| `ManagedClientCreateResponse` | `clientId` (UUID), `clientSecret` (String, plaintext — returned **once only**), `name`, `applicationId`, `scopes`, `active`, `createdAt` |
| `ManagedClientTO` | `clientId` (UUID), `name`, `description`, `applicationId`, `scopes`, `active`, `createdAt`, `lastUpdatedAt`, `secretLastRotatedAt` — **no secret fields** |
| `ManagedClientUpdateRequest` | `name` (nullable), `description` (nullable), `scopes` (nullable), `active` (nullable) — all optional for partial update |
| `ManagedClientTokenRequest` | `clientId` (UUID, required), `clientSecret` (String, required), `scopes` (List\<String\>, required) |
| `ManagedClientTokenResponse` | `accessToken` (String), `tokenType` ("Bearer"), `expiresIn` (long, seconds), `scopes` (List\<String\>), `issuedAt` (Instant) |
| `ManagedClientSecretRotateResponse` | `clientId` (UUID), `clientSecret` (String, new plaintext — once only), `gracePeriodSeconds` (long), `rotatedAt` (Instant) |
| `ManagedClientTokenIntrospectResponse` | `active` (boolean), `clientId` (String), `clientName` (String), `scopes` (List\<String\>), `issuer` (String), `exp` (Long), `iat` (Long), `jti` (String) |
| `ManagedClientErrorResponse` | `error` (String), `errorDescription` (String), `clientId` (String, optional) |

Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`) where appropriate for `@Valid` to work.

---

### TASK 1.7 — MapStruct Mapper

#### `src/main/java/com/umdc/backoffice/v1/managedclient/mapper/ManagedClientMapper.java`

```java
@Mapper(componentModel = "spring")   // or config = MapperAppConfig.class if that's the project standard
public interface ManagedClientMapper {

    /// Entity → read-only DTO. Secret fields MUST be ignored.
    @Mapping(target = "clientId", source = "id")
    @Mapping(target = "secretHash", ignore = true)      // field not on TO — ignore prevents PMD warning
    @Mapping(target = "prevSecretHash", ignore = true)
    ManagedClientTO toTO(ManagedClientEntity entity);

    /// CreateRequest → Entity. Service sets id, secretHash, timestamps.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretHash", ignore = true)
    @Mapping(target = "prevSecretHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "secretLastRotatedAt", ignore = true)
    ManagedClientEntity toEntity(ManagedClientCreateRequest request);

    /// Partial update: merge non-null fields from request into existing entity.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretHash", ignore = true)
    @Mapping(target = "prevSecretHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "secretLastRotatedAt", ignore = true)
    void updateEntityFromRequest(ManagedClientUpdateRequest request, @MappingTarget ManagedClientEntity entity);

    List<ManagedClientTO> toTOList(List<ManagedClientEntity> entities);
}
```

Check what `config` value is used on existing mappers (e.g., `UserMapper`) and apply the same annotation.

---

## ✅ Phase 1 Quality Gate Checklist

Run these commands before marking Phase 1 complete:

```bash
# 1. Fast compile — must be green (no errors)
mvn -DskipTests compile

# 2. PMD check — 0 violations on all new and modified files
mvn pmd:check pmd:cpd-check

# 3. Full test run — Flyway migrations validated, no SQL errors
mvn test
```

- [ ] `mvn -DskipTests compile` — exits 0
- [ ] `mvn pmd:check` — 0 violations
- [ ] Flyway V2, V3, V4 migrations execute without errors
- [ ] `ManagedClientMapper` MapStruct source generated in `target/generated-sources/annotations/`
- [ ] No hardcoded secrets in any file (grep for passwords, secrets, keys)
- [ ] `AuditEventType` still has all original constants (no removals)

---

## 🔗 What Phase 2 Needs From This Phase

| Artifact | Used By |
|---|---|
| `ManagedClientEntity` + `ManagedClientAuditEventEntity` | Phase 2 services |
| Both repositories | Phase 2 `ManagedClientServiceImpl` |
| All 9 DTOs | Phase 2 `ManagedClientApi` + controller |
| `ManagedClientMapper` | Phase 2 `ManagedClientServiceImpl` |
| `ManagementAuthenticatorProperties` (extended) | Phase 2 + Phase 3 services |
| `AuditEventType` (extended) | Phase 2 `ManagedClientAuditServiceImpl` |

