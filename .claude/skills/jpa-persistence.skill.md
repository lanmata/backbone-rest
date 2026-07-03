---
name: JPA Persistence
description: Shared — Spring Data JPA patterns, PostgreSQL/Supabase pooler constraints, H2 test compatibility (java-developer, database-architect)
applies-to:
  - java-developer
  - database-architect
---

# Shared Skill — JPA Persistence

## Entity Location

JPA entities live in the **external** `com.umdc.persistence` Maven module — they are not in this repo. Repositories are Spring Data interfaces that extend `JpaRepository` or `CrudRepository`.

## Repository Naming

```java
// In com.umdc.backoffice.jpa.repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByAlias(String alias);
}
```

## Transaction Mode Constraints

Supabase pooler runs in **transaction mode** — avoid:
- `SET LOCAL` / session-level settings
- `LISTEN/NOTIFY`
- Advisory locks
- Server-side cursors

## H2 Test Compatibility

Tests use H2 in-memory DB. Avoid in queries:
- `RETURNING` clause
- `uuid_generate_v4()` → use `UUID.randomUUID()` in Java
- `ILIKE` → `LOWER(x) LIKE LOWER(:p)`
- `jsonb` type columns

## Migration Naming

```
src/main/resources/db/V<N>__<description>.sql
```
Example: `V3__add_audit_event_table.sql`

## Datasource Config

Datasource configured in `bootstrap.yml` via Vault references:
```yaml
spring:
  datasource:
    url: ${vault:database/url}
    username: ${vault:database/username}
    password: ${vault:database/password}
```
Never hardcode credentials.
