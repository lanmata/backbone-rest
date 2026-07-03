# backbone-rest — Agent Reference

Loaded into every agent's context. Do not repeat content from this file in agent prompts — reference the section number instead.

---

## 1. Runtime & Stack

| Item | Value |
|------|-------|
| Language | Java 21 (records, sealed types, text blocks in use) |
| Framework | Spring Boot 4.0.6 |
| Cloud | Spring Cloud 2025.1.1 (Config, Vault, Eureka, Bootstrap) |
| Security | Spring Security 6 — Session JWT (JJWT 0.12.6) + M2M Bearer |
| Persistence | Spring Data JPA / Hibernate + PostgreSQL 42.7.7 + Redis |
| Storage | AWS SDK S3 2.21.0 (Supabase Storage bucket) |
| Mapping | MapStruct 1.6.3 with `MapperAppConfig` from commons-services |
| API docs | SpringDoc OpenAPI 2.6.0 (springdoc-openapi-starter-webmvc-ui) |
| Build | Maven (no `mvnw`) — `pom.xml` at repo root |
| Static analysis | PMD 3.28.0 (`ruleset.xml`), SonarCloud |
| Coverage | JaCoCo 0.8.14 |
| Tests | JUnit 5.14.1, Mockito 5.21.0, MockServer 5.15.0, H2 2.2.224 |
| Port | 8084 (Dockerfile `EXPOSE 8084`) |
| Base image | `amazoncorretto:21-alpine3.20` |

Private artifacts from Repsy (`https://repo.repsy.io/mvn/lmata/prx`). Require `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD`.

---

## 2. Architecture

```
HTTP Request
    │
    ▼
SecurityFilterChain
    ├── ManagedClientTokenFilter   (validates Bearer M2M token)
    └── SessionJwtAuthenticationFilter  (validates session-token header)
    │
    ▼
*Api.java (interface)
    default method → delegates to *Service
    │
    ▼
*Controller.java (@RestController, thin)
    @Override every method → delegates to *Service
    │
    ▼
*ServiceImpl.java (business logic, returns ResponseEntity<?>)
    ├── Repositories (com.umdc.persistence.general.repositories)
    ├── Mappers (MapStruct, com.umdc.backoffice.v1.<domain>.mapper)
    ├── AuditEventService (records security events)
    └── External: AWS S3 / Redis / Eureka / Config Server / Vault
    │
    ▼
PostgreSQL (Supabase pooler aws-1-us-east-2.pooler.supabase.com:6543)
Redis (session JTI deny-list and managed-client cache)
```

Public endpoints (no auth required):
- `POST /api/v1/session`, `POST /api/v1/session/token`, `GET /api/v1/session/validate`, `GET /api/v1/session/renew`, `POST /api/v1/session/refresh`
- `POST /api/v1/managed-clients/token`, `POST /api/v1/managed-clients/introspect`
- Swagger UI paths: `/swagger-ui/**`, `/v3/api-docs/**`

---

## 3. Package / Module Map

| Package / Directory | Purpose |
|---------------------|---------|
| `com.umdc.backoffice` | Root; `UMDCBackofficeRestApplication` entry point |
| `com.umdc.backoffice.v1.<domain>` | Domain slice (see list below) |
| `com.umdc.backoffice.v1.<domain>.api.controller` | `*Api.java` interface + `*Controller.java` impl |
| `com.umdc.backoffice.v1.<domain>.service` | `*Service.java` interface + `*ServiceImpl.java` |
| `com.umdc.backoffice.v1.<domain>.api.to` | DTOs / transfer objects |
| `com.umdc.backoffice.v1.<domain>.mapper` | MapStruct mapper interfaces |
| `com.umdc.backoffice.security.config` | `SecurityConfig` — filter chain, CORS |
| `com.umdc.backoffice.security.filter` | `SessionJwtAuthenticationFilter`, `ManagedClientTokenFilter` |
| `com.umdc.backoffice.security.bruteforce` | `LoginAttemptService` — rate limiting |
| `com.umdc.backoffice.security.jwt` | `JwtConfigProperties`, `JwtConverterProperties` |
| `com.umdc.backoffice.security.util` | `RolesClaimParser` |
| `com.umdc.backoffice.jpa.domain` | Local JPA entities (audit, managed-client) |
| `com.umdc.backoffice.jpa.repository` | Local Spring Data repos |
| `com.umdc.backoffice.util` | `JwtUtil`, `KeystoreUtil`, `MessageUtil` |
| `com.umdc.backoffice.constant.keys` | `*MessageKey` enums (user-facing message keys) |
| `com.umdc.backoffice.constant.types` | `AuditEventType` enum |
| `com.umdc.backoffice.config` | `RedisConfig`, `DataSourceSslConfig`, `SecurityBeansConfig` |
| `com.umdc.backoffice.property` | `@ConfigurationProperties` classes |
| `com.umdc.backoffice.aop` | `LogDefault` AOP aspect |
| `com.umdc.persistence.general.domains` | **External** JPA entities (users, roles, contacts, etc.) |
| `com.umdc.persistence.general.repositories` | **External** Spring Data repos |
| `com.umdc.commons.*` | **External** shared utilities, POJOs, exceptions |
| `com.umdc.commons-services.*` | **External** MapStruct `MapperAppConfig`, `HttpStatusUtil` |

**Domain slices under `v1/`:**
`application`, `contacts`, `contacttypes`, `features`, `iam/audit`, `iam/passwords`, `iam/permissions`, `iam/tokens`, `managedclient`, `people`, `profileimage`, `report`, `roles`, `session`, `users`

---

## 4. Messaging

Not applicable — no message broker in this project.

---

## 5. Persistence

| Store | Tech | Purpose |
|-------|------|---------|
| PostgreSQL | Spring Data JPA / Hibernate | Primary data store (Supabase pooler) |
| Redis | Spring Data Redis | Session JTI deny-list, managed-client token cache |
| H2 | In-memory (test only) | Unit/integration tests |

**SQL Migrations** — Flyway-convention naming, stored in `src/main/resources/db/migration/`:
- `V{n}__{description}.sql` — sequential, never edited after merge
- Existing: `V1__create_audit_event.sql` through `V4__extend_audit_event_check.sql`
- Schema: `general` (e.g., `@Table(schema = "general", name = "audit_event")`)
- Add indexes for all FK columns and UUID primary keys
- Run migrations against PostgreSQL only (not H2)

**Entity conventions:**
- `@Id` column is a caller-generated `UUID`, never `@GeneratedValue`
- `@PrePersist` sets `createdAt` / `occurredAt` if not already set
- Append-only entities: `updatable = false` on all columns
- JSON columns: `@JdbcTypeCode(SqlTypes.JSON)` with `columnDefinition = "jsonb"`

**External persistence (com.umdc.persistence):**
- `UserEntity`, `PersonEntity`, `ContactEntity`, `RoleEntity`, `ApplicationEntity`, `ApplicationRoleUserEntity` — do NOT modify these classes; they live in the external artifact

---

## 6. Key Conventions

- **Interface-first**: `*Api.java` carries `@RequestMapping`, `@Operation`, `@ApiResponses`, and `default` methods that delegate to service. Never put `@RequestMapping` on the controller.
- **Thin controllers**: `@RestController` + constructor injection + `@Override` every API method. No business logic.
- **Services return `ResponseEntity<?>`**: Status code decisions live in `*ServiceImpl`, not in controllers.
- **Constructor injection only**: Never `@Autowired` field injection. No `@Inject`.
- **Logger naming**: `private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);`
- **Log pattern**: `LOGGER.info("{} /endpoint", MessageUtil.LOG_START_MSG);` for entry. `LOGGER.error("{}| ...", MessageKey.getStatus(), ...)` for errors.
- **MessageUtil**: Use for all user-facing messages. Keys live in `*MessageKey` enums that implement `BackboneMessage`.
- **MapStruct**: `@Mapper(config = MapperAppConfig.class)` — do NOT use `componentModel = "spring"` directly.
- **PMD zero violations**: Check `ruleset.xml` before committing. Common violations: `GodClass`, `UnusedPrivateMethod`, `AvoidDuplicateLiterals`, `ConstantsInInterface`.
- **Transactions**: `@Transactional` on `*ServiceImpl` methods that write to the DB.
- **Null checks**: Use `Objects.isNull()` / `Objects.nonNull()` — not `== null`.
- **UUID PKs**: Caller-generated, never auto-generated by DB.
- **No cross-domain utility controllers**: Each controller belongs to exactly one domain.
- **`@Valid`** on every `@RequestBody` and `@PathVariable` that carries constraints.

---

## 7. Build & Test Commands

```bash
# Fast syntax check (skip tests)
mvn -DskipTests compile

# Full suite: PMD + JaCoCo + JUnit
mvn test

# Single test class
mvn -Dtest=UserServiceImplTest test

# Single test method
mvn -Dtest=UserServiceImplTest#create_valid_user test

# Produce JAR (skip tests)
mvn -DskipTests package

# PMD check only
mvn pmd:check

# JaCoCo report (after mvn test)
open target/site/jacoco/index.html
```

Requires env-vars: `REPSY_ACCOUNT_USER`, `REPSY_ACCOUNT_PASSWORD` (for private artifact resolution).

---

## 8. External Dependencies

| Dependency | Registry | Purpose |
|------------|----------|---------|
| `com.umdc:commons:0.0.1` | Repsy (lmata/prx) | Shared utilities, exception types, common POJOs |
| `com.umdc:commons-services:0.0.1` | Repsy (lmata/prx) | MapStruct `MapperAppConfig`, `HttpStatusUtil`, `ValidatorCommonsUtil` |
| `com.umdc:persistence:0.0.1` | Repsy (lmata/prx) | JPA entities and Spring Data repos for all domains |
| `com.umdc:security-oauth:0.0.1` | Repsy (lmata/prx) | OAuth2 security helpers |

Add to `~/.m2/settings.xml` (or `ci_settings.xml` for CI):
```xml
<server>
  <id>PRX-Repsy</id>
  <username>${env.REPSY_ACCOUNT_USER}</username>
  <password>${env.REPSY_ACCOUNT_PASSWORD}</password>
</server>
```

**Spring Cloud Config Server**: `${CNFS_URI}:${CNFS_PORT}` — serves application properties per profile.
**Vault**: `${VAULT_URI}` (HashiCorp Vault) — secrets injected at startup when `VAULT_ENABLED=true`.

---

## 9. Agents

| Agent | Purpose | Mode | Model | Key MCPs |
|-------|---------|------|-------|----------|
| developer | Implement features, fix bugs, follow all conventions | primary | claude-opus-4-8 | codegraph, git, postgres |
| reviewer | Review diffs: correctness, PMD, OWASP, conventions | subagent | claude-sonnet-4-6 | codegraph, git |
| tester | Write JUnit 5 + Mockito tests, hit JaCoCo thresholds | subagent | claude-sonnet-4-6 | codegraph |
| database | JPA entities, repositories, SQL migrations | subagent | claude-sonnet-4-6 | codegraph, postgres |
| devops | Docker build, Maven packaging, env-vars | subagent | claude-sonnet-4-6 | git, docker |
| security | OWASP audit, JWT analysis, secret scanning | subagent | claude-opus-4-8 | codegraph, git |
| api | REST contract design, OpenAPI annotations | subagent | claude-sonnet-4-6 | codegraph, fetch |

**Keybindings:**

| Shortcut | Agent |
|----------|-------|
| Ctrl+Shift+D | developer |
| Ctrl+Shift+R | reviewer |
| Ctrl+Shift+T | tester |
| Ctrl+Shift+B | database |
| Ctrl+Shift+K | devops |
| Ctrl+Shift+S | security |
| Ctrl+Shift+A | api |

---

## 10. MCPs

### Tier 1

| Name | Purpose | Required Env-vars |
|------|---------|-------------------|
| codegraph | Symbol index, call graph, file map | none |
| git | Repo history, blame, diff | none |
| github | PRs, issues, Actions | `GITHUB_PERSONAL_ACCESS_TOKEN` |
| postgres | Run SQL against PostgreSQL | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSLMODE` |

### Tier 2

| Name | Purpose | Required Env-vars |
|------|---------|-------------------|
| fetch | Inline external docs (Spring, Swagger) | none |
| docker | Build/run Docker images locally | `DOCKER_HOST` (optional) |
| memory | Cross-session context persistence | none |

**Usage patterns by role:**
- `developer`: codegraph (find symbols before editing) → git (check blame) → postgres (verify queries)
- `reviewer`: codegraph (impact analysis) → git (diff context)
- `tester`: codegraph (find existing test patterns) — never uses postgres directly
- `database`: codegraph (entity/repo discovery) → postgres (run and verify migrations)
- `devops`: git (branch state) → docker (build/run)
- `security`: codegraph (grep for patterns) → git (history of security files)
- `api`: codegraph (existing API interfaces) → fetch (OpenAPI spec docs)

---

## 11. Skills

| Skill | Trigger phrase | Purpose |
|-------|---------------|---------|
| implement-feature | `/implement-feature` | End-to-end guide: add a new endpoint across all layers |
| add-sql-migration | `/add-sql-migration` | Create a Flyway-convention SQL migration |
| security-audit | `/security-audit` | Structured OWASP + secrets + JWT audit |

Full prompt templates are in `.opencode/skills/*.md`.

---

## 12. Hooks

| Hook | Trigger | What it does |
|------|---------|--------------|
| before-edit | Before any file is edited | Warns if the file is a build output, binary, secret, or vendor directory |
| after-session | End of every session | Prints pre-commit checklist: build, PMD, tests, coverage, secrets, env-vars, migrations |

`before-edit.sh` detects: `target/`, `*.class`, `*.jar`, `bootstrap.yml`, `application-prod.yml`, `*.jks`, `*.p12`, `*.pem`, `*.crt`, `*.key`, `node_modules/`, `.gradle/`, `.mvn/`.

`after-session.sh` checklist covers: compile, test, PMD, JaCoCo, secret scan, env-var docs, migration check.

---

## 13. Tools

| Script | MCP it wraps | Required env-vars |
|--------|-------------|-------------------|
| `.opencode/tools/postgres-mcp.sh` | postgres | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSLMODE` |

**Template for adding a new wrapper:**
```sh
#!/usr/bin/env sh
# Required env-vars: VARNAME — description
: "${VARNAME:?VARNAME is not set}"
exec npx -y <mcp-server-package> --option "${VARNAME}"
```
