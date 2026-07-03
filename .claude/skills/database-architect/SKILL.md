---
name: Database Architect Skills
description: Consolidated skill set for the Database Architect agent — JPA, Spring Data, PostgreSQL/Supabase pooler, SQL migrations for backbone-rest
applies-to:
  - database-architect
---

# Database Architect — Skill Definition

## 1. Persistence Architecture

| Layer | Location | Notes |
|-------|----------|-------|
| JPA Entities | External `com.umdc.persistence` module | Not in this repo |
| Local domain wrappers | `com.umdc.backoffice.jpa.domain` | If local extensions needed |
| Repositories | `com.umdc.backoffice.jpa.repository` | Spring Data `JpaRepository` |
| Database | PostgreSQL via Supabase pooler | Transaction mode only |
| Test DB | H2 in-memory | `src/test/resources/application.yml` |
| Migrations | `src/main/resources/db/` | SQL scripts |

---

## 2. Supabase Pooler Constraints

- Mode: **transaction mode** (PgBouncer) — no session-level features.
- Avoid: `SET LOCAL`, `LISTEN/NOTIFY`, advisory locks, server-side cursors.
- Connection string: `aws-1-us-east-2.pooler.supabase.com:6543`.
- SSL required: `prod-ca-2021.crt` trusted.

---

## 3. Spring Data Query Patterns

```java
// Derived query methods (prefer — no SQL needed)
Optional<UserEntity> findByEmail(String email);
List<UserEntity> findByActiveTrue();
boolean existsByAlias(String alias);

// JPQL for complex queries
@Query("SELECT u FROM UserEntity u WHERE u.active = true AND u.roleId = :roleId")
List<UserEntity> findActiveByRole(@Param("roleId") UUID roleId);

// Native SQL (use sparingly — not portable to H2)
@Query(value = "SELECT * FROM users WHERE ...", nativeQuery = true)
```

---

## 4. H2 Test Compatibility

When writing queries, verify H2 compatibility:
- No `RETURNING` clause.
- No PostgreSQL-specific functions (`uuid_generate_v4()` → use `UUID.randomUUID()` in Java).
- No `ILIKE` → use `LOWER(x) LIKE LOWER(:param)`.
- No `jsonb` columns in H2.

---

## 5. Migration Pattern

SQL migrations live in `src/main/resources/db/`. Follow naming:
```
V<version>__<description>.sql
```
Example: `V3__add_audit_event_table.sql`

Reference: `docs/sql/create_audit_event.sql`

---

## 6. Key Files

| File | Purpose |
|------|---------|
| `src/main/resources/db/` | SQL migration scripts |
| `src/main/resources/bootstrap.yml` | Datasource config (pooler URL, credentials from Vault) |
| `docs/sql/create_audit_event.sql` | Reference DDL |
| `src/test/resources/application.yml` | H2 test datasource config |

---

## 7. Checklist

- [ ] All Spring Data query methods tested with H2
- [ ] Native SQL queries flagged and reviewed for H2 incompatibility
- [ ] Supabase pooler constraints respected (no session-level features)
- [ ] Migration files follow naming convention
- [ ] No hardcoded DB credentials — use Vault references
- [ ] New entities/repos confirmed to exist in external `com.umdc.persistence` module
