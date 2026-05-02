# AGENTS.md

## Project Snapshot
- This is a Java 21 Spring Boot 3.4 REST service (`backbone-rest`) for backoffice entities.
- Main app bootstrap is `src/main/java/com/prx/backoffice/PrxBackofficeRestApplication.java`.
- The app scans external PRX modules (`com.umdc.persistence`, `com.umdc.commons.services`), so domain entities/repos are mostly not in this repo.
- API surface is mostly under `/api/v1/*` (for example `UserController`, `SessionController`).

## Architecture That Matters
- Feature modules are organized by domain under `src/main/java/com/prx/backoffice/v1/<domain>/{api,service,mapper}`.
- API contracts are typically split into `*Api` interfaces + `*Controller` implementations.
- `*Api` carries endpoint mappings + OpenAPI annotations; controller overrides default methods and delegates to services.
- Services usually return `ResponseEntity<?>` directly and encode status decisions there (see `UserServiceImpl`, `SessionServiceImpl`).
- Mapping is MapStruct-based (`*Mapper` interfaces, e.g. `UserMapper`) with custom expressions for relation mapping.

## Security And Token Flow
- Resource-server security is configured in `src/main/java/com/prx/backoffice/security/config/SecurityConfig.java`.
- JWT authority extraction for Keycloak tokens is handled by `src/main/java/com/prx/backoffice/security/jwt/JwtConverter.java`.
- Session endpoints (`/api/v1/session`) also mint and validate app-specific JWTs in `SessionServiceImpl`; this is separate from OAuth2 resource-server validation.
- Session token header key is `session-token` (`SessionJwtService.SESSION_TOKEN_KEY`).

## Configuration And External Integrations
- Runtime config is centralized in `src/main/resources/bootstrap.yml` (SSL bundle, OAuth issuer/JWK URI, Vault, Config Server).
- `.env` starter values are in `src/main/resources/default.env`, but many required values come from env vars (`APP_*`, `SSL_*`, `VAULT_*`, `CNFS_*`, auth vars).
- Build pulls private dependencies from Repsy; set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` before Maven resolution.
- OpenAPI artifact lives at `src/main/resources/META-INF/backbone_rest-openapi.yaml`.

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
- Use `MessageUtil` for user-facing service messages when existing message keys already cover the scenario.
- Be careful with controller-specific casts in API default methods (example: `UserApi.putUserDetail` casts `this` to `UserController`).

