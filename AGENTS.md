# AGENTS.md

## Project Snapshot
- This is a Java 21 Spring Boot 3.4 REST service (`backbone-rest`) for backoffice entities.
- Main app bootstrap is `src/main/java/com/umdc/backoffice/UMDCBackofficeRestApplication.java`.
- The app scans `com.umdc.backoffice` and `com.umdc.commons.services`; domain entities/repos live in the external `com.umdc.persistence` module, not in this repo.
- API surface is mostly under `/api/v1/*` (for example `UserController`, `SessionController`).

## Architecture That Matters
- Feature modules are organized by domain under `src/main/java/com/umdc/backoffice/v1/<domain>/{api,service,mapper}`.
- The `iam` domain is a **nested module** under `v1/iam/` with four sub-modules, each owning its own `api/` and `service/`:
  - `audit/` — `AuditApi`, audit event repository (`audit/repository/`), domain types (`audit/domain/`), mapper
  - `passwords/` — password management API (no mapper; `api/to/` only)
  - `permissions/` — `PermissionCheckApi`
  - `tokens/` — `TokenIntrospectApi`
- `profileimage/` does **not** have a `mapper/` sub-directory (only `api/`, `service/`, `to/`).
- `session/` uses `services/` (plural) for its service package, unlike other domains that use `service/`.
- API contracts are typically split into `*Api` interfaces + `*Controller` implementations.
- `*Api` carries endpoint mappings + OpenAPI annotations; controller overrides default methods and delegates to services.
- Services usually return `ResponseEntity<?>` directly and encode status decisions there (see `UserServiceImpl`, `SessionServiceImpl`).
- Mapping is MapStruct-based (`*Mapper` interfaces, e.g. `UserMapper`) with custom expressions for relation mapping.

## Security And Token Flow
- Resource-server security is configured in `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java`.
- JWT authority extraction is handled by `src/main/java/com/umdc/backoffice/security/jwt/JwtConverter.java`.
- Session endpoints (`/api/v1/session`) also mint and validate app-specific JWTs in `SessionServiceImpl`; this is separate from OAuth2 resource-server validation.
- Session token header key is `session-token` (`SessionJwtService.SESSION_TOKEN_KEY`).
- Public endpoints excluded from OAuth2 filter are listed in `umdc.api.excludes` in `bootstrap.yml` (currently `/v1/sessions/token, /v1/sessions/validate`).

## Configuration And External Integrations
- Runtime config is centralized in `src/main/resources/bootstrap.yml` (SSL bundle, OAuth issuer/JWK URI, Spring Cloud Config, Redis).
- `.env` starter values are in `src/main/resources/default.env`, but many required values come from env vars (`APP_*`, `SSL_*`, `VAULT_*`, `CNFS_*`, auth vars).
- Build pulls private dependencies from Repsy; set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` before Maven resolution.
- OpenAPI artifact lives at `src/main/resources/api.yaml`.
- **Redis**: configured via `config/RedisConfig.java`; connection URL supplied by `REDIS_URL` env var (`spring.data.redis.url`).
- **Profile image storage**: uses Cloudflare R2 via `CloudflareR2StorageClient` from `com.umdc.commons.services.cloudflare.r2`; see `ProfileImageServiceImpl`.
- **Database migrations**: Flyway scripts live in `src/main/resources/db/migration/` (e.g. `V1__create_audit_event.sql`).

## Build/Test Workflows
- Use Maven (no `mvnw` in repo).
- Run a fast compile check:
  ```bash
  mvn -DskipTests compile
  ```
- Run full unit tests:
  ```bash
  mvn test
  ```
- Run a focused test class while iterating:
  ```bash
  mvn -Dtest=UserServiceImplTest test
  ```
- Package artifact:
  ```bash
  mvn -DskipTests package
  ```
- PMD (`ruleset.xml`) and JaCoCo are bound to test phase in `pom.xml`; PMD violations fail the build.

## Codebase-Specific Conventions
- Keep new endpoints in existing domain module layout (`api`, `service`, `mapper`) instead of creating cross-domain utility controllers.
- Follow existing interface-first controller pattern (`UserApi` + `UserController`, `SessionApi` + `SessionController`).
- Preserve current comment/documentation style in touched files (many classes use `///` doc comments).
- Use `MessageUtil` for user-facing service messages when existing message keys already cover the scenario; keys live in `constant/keys/*MessageKey` enums and `BackboneAppConstants`.
- Be careful with controller-specific casts in API default methods (example: `UserApi.putUserDetail` casts `this` to `UserController`).
- The `@LogDefault` annotation (`aop/LogDefault.java`) provides AOP-based method logging; apply it on service methods where entry/exit tracing is needed.
- The bootstrap class is annotated `@EnableAsync`; background tasks may use `@Async`.
