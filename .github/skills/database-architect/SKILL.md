---
name: Database Architect Skills
description: Consolidated skill set for the Database Architect agent — JPA entity design, schema management, Spring Data JPA, and query optimization
applies-to:
  - Database Architect
---

# Database Architect — Skill Definition

## 1. Project Database Context

- **Production DB**: PostgreSQL 42.7.4
- **Test DB**: H2 in-memory (`src/test/resources/application-test.yml`)
- **ORM**: Spring Data JPA / Hibernate
- **Entities and repositories are in the external module** `com.prx:persistence:0.0.3`
  — packages: `com.prx.persistence.general.domains` and `com.prx.persistence.general.repositories`
- **DDL**: `none` — schema managed externally; **never use** `create`, `update`, or `create-drop` in production.

---

## 2. Schema Design

### Entity Inventory

| Entity | Key Fields |
|--------|-----------|
| `UserEntity` | `id` (UUID), `alias`, `email`, `password`, `active` |
| `ApplicationEntity` | `id` (UUID), `name`, `active` |
| `RoleEntity` | `id` (UUID), `name`, `description`, `active` |
| `ApplicationRoleUserEntity` | composite PK: `userId`, `roleId`, `applicationId` |
| `PersonEntity` | `id` (UUID), `firstName`, `lastName`, `birthdate`, `gender` |
| `ContactEntity` | `id`, `value`, `contactType`, `person` |
| `ContactTypeEntity` | `id`, `name`, `description`, `active` |

### Composite Key Pattern

```java
@Embeddable
public class ApplicationRoleUserEntityId {
    private UUID userId;
    private UUID roleId;
    private UUID applicationId;
}

@Entity
public class ApplicationRoleUserEntity {
    @EmbeddedId
    private ApplicationRoleUserEntityId id;

    @ManyToOne @MapsId("userId")   private UserEntity user;
    @ManyToOne @MapsId("roleId")   private RoleEntity role;
    @ManyToOne @MapsId("applicationId") private ApplicationEntity application;
}
```

**Always populate all three ID fields** before persisting:

```java
var pk = new ApplicationRoleUserEntityId();
pk.setUserId(user.getId());
pk.setRoleId(role.getId());
pk.setApplicationId(application.getId());
entity.setId(pk);
```

### UUID Primary Keys

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

### Schema Change Rules

1. **Additive only** — never rename or drop columns.
2. New nullable columns require a default for existing rows.
3. Validate all changes against the H2 test setup.
4. DDL is `none` — changes managed via external migration scripts.

---

## 3. Spring Data JPA

### Repository Injection

Repositories auto-scanned via `PrxBackofficeRestApplication.scanBasePackages`. Inject via constructor:

```java
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public UserServiceImpl(UserRepository userRepository,
                           ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }
}
```

### Key Repositories

| Repository | Key Custom Methods |
|-----------|-------------------|
| `UserRepository` | `findByAlias`, `findByAliasAndApplication`, `findByEmailAndApplication`, `findByApplication`, `findUserInfo` |
| `ApplicationRoleUserRepository` | `deleteByUserIdAndApplicationId` |

### Query Method Naming

```java
Optional<UserEntity> findByAlias(String alias);
Optional<UserEntity> findByAliasAndApplication(String alias, ApplicationEntity app);
List<UserEntity> findByApplicationAndActiveTrue(ApplicationEntity app);
boolean existsByAlias(String alias);
```

### `@Query` for Complex Cases

```java
@Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.applicationRoleUser aru " +
       "LEFT JOIN FETCH aru.role WHERE u.alias = :alias")
Optional<UserEntity> findByAliasWithRoles(@Param("alias") String alias);
```

### `@Transactional` in Service Layer

```java
@Transactional
public ResponseEntity<Void> unlinkRole(UUID userId, UUID roleId, UUID applicationId) {
    applicationRoleUserRepository.deleteByUserIdAndApplicationId(userId, applicationId);
    return ResponseEntity.noContent().build();
}

@Transactional(readOnly = true)
public ResponseEntity<UserTO> findUserById(UUID userId) { ... }
```

---

## 4. SQL Optimization

### N+1 Detection and Fix

```java
// ❌ N+1 — triggers one query per item in applicationRoleUser collection
for (UserEntity user : users) {
    user.getApplicationRoleUser().forEach(aru -> process(aru.getRole()));
}

// ✅ Fix — JOIN FETCH
@Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.applicationRoleUser aru " +
       "LEFT JOIN FETCH aru.role WHERE u.id = :userId")
Optional<UserEntity> findUserWithRoles(@Param("userId") UUID userId);
```

### Key Associations to Watch

| Association | Risk | Strategy |
|-------------|------|----------|
| `UserEntity.applicationRoleUser` | N+1 on role access | JOIN FETCH when roles needed |
| `ApplicationRoleUserEntity.role` | N+1 on `getRole().getId()` | JOIN FETCH or batch fetch |
| `PersonEntity.contacts` | N+1 in person listing | FETCH when contacts displayed |

### H2 Compatibility

Tests run on H2. Avoid PostgreSQL-specific syntax:
- ❌ `ILIKE`, `::text`, `RETURNING`
- ✅ `LOWER()`, `UPPER()`, `CONCAT()`, standard `FETCH JOIN`

---

## 5. Constraints

- **Do NOT modify entity classes** — they are in `com.prx:persistence` (external).
- All schema changes must be backward compatible.
- Use `@Transactional` in service layer (not repository layer).
- Test JPQL against H2 in `src/test/resources/application-test.yml`.

---

## Checklist

```markdown
- [ ] UUID PK with @GeneratedValue(strategy = GenerationType.UUID)
- [ ] Composite keys use @EmbeddedId with @MapsId on FK fields
- [ ] All three composite key fields set before persist
- [ ] @Transactional on service methods with multiple writes
- [ ] @Transactional(readOnly = true) on read-only service methods
- [ ] JOIN FETCH used when accessing lazy collections
- [ ] No native SQL unless justified
- [ ] JPQL is H2-compatible (no PostgreSQL-specific syntax)
- [ ] Schema changes are additive only
```

