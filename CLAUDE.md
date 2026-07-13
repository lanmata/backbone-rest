# backbone-rest — Claude Code Project Context

## What This Project Is
Spring Boot 3.4.1 backoffice REST service on Java 21. Manages users, roles, contacts,
features, people, sessions, and profile images. Integrates with Supabase (auth JWT +
PostgreSQL pooler + Storage for profile images).

## Build — No `mvnw`
```bash
mvn -DskipTests compile          # fast syntax check
mvn test                         # full suite: PMD + JaCoCo + JUnit
mvn -Dtest=UserServiceImplTest test  # single class
mvn -DskipTests package          # produce JAR
```
Requires `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` for private PRX artifacts.

## Package Structure
```
com.umdc.backoffice.v1.<domain>/
  api/controller/   → *Api.java (interface) + *Controller.java (impl)
  service/          → *Service.java (interface) + *ServiceImpl.java
  api/to/           → DTOs
  mapper/           → MapStruct mappers
com.umdc.backoffice.security/    → SecurityConfig, JwtConverter
com.umdc.backoffice.util/        → MessageUtil, JwtUtil, KeystoreUtil
com.umdc.backoffice.constant.keys/ → *MessageKey enums
```

## Domain Modules (`v1/`)
`application`, `contacts`, `contacttypes`, `features`, `people`, `profileimage`,
`report`, `roles`, `session`, `users`

## Mandatory Conventions — Always Follow
1. **Interface-first**: `*Api.java` carries `@RequestMapping`, `@Operation`, `@ApiResponses` +
   a `default` method that delegates to the service.
2. **Controller is thin**: `@RestController`, `@RequestMapping`, constructor injection,
   `@Override` every API method — no logic.
3. **Services return `ResponseEntity<?>`** — status decisions live in `*ServiceImpl`.
4. **Constructor injection only** — never `@Autowired` field injection.
5. **MessageUtil** for user-facing messages; keys in `*MessageKey` enums.
6. **MapStruct** with `config = MapperAppConfig.class` from `com.umdc.commons.services`.
7. **SLF4J logging** via `LoggerFactory.getLogger`.
8. **PMD zero violations** — `ruleset.xml` is enforced at test phase.
9. **OpenAPI YAML** at `src/main/resources/META-INF/api.yaml` — update
   it when any `*Api.java` contract changes.

## Security Architecture
- **OAuth2 Resource Server** (Supabase / Keycloak JWT): all `/api/v1/**` GETs require
  a valid JWT. Roles extracted from `resource_access.<clientId>.roles` by `JwtConverter`.
- **Session JWT** (JJWT 0.12.3): minted in `SessionServiceImpl`, transmitted via
  `session-token` header. Separate from OAuth2 validation.
- Endpoints `/v1/sessions/token` and `/v1/sessions/validate` bypass OAuth2 filter.

## Supabase Integration (Current Branch: ds-196-include-supabase-storage)
- Auth: `AUTH_SERVER_URI=https://jygwixrpoxcrltmeshyl.supabase.co`
- DB: PostgreSQL pooler at `aws-1-us-east-2.pooler.supabase.com:6543`
- Profile image storage: Supabase Storage (`profileimage` domain)
- Spring profile for Supabase: `remote-supabase`

## Key Files
| File | Purpose |
|------|---------|
| `src/main/resources/bootstrap.yml` | Central config — Vault, Config Server, OAuth |
| `src/main/resources/META-INF/api.yaml` | OpenAPI 3.1 spec |
| `src/main/resources/default.env` | Runtime env stubs (no real secrets in git) |
| `ruleset.xml` | PMD rules — check before committing |
| `pom.xml` | Dependencies + JaCoCo + PMD plugin config |
| `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java` | OAuth2 setup |

## What Not To Do
- Do NOT use `mvnw` — it does not exist in this repo.
- Do NOT use `@Autowired` field injection.
- Do NOT break `/api/v1/*` endpoint contracts.
- Do NOT commit real secrets or modify `keystore.jks` / `*.crt`.
- Do NOT add logic to controllers — delegate to service.
- Do NOT create cross-domain utility controllers.

## Subagents Available (`.claude/agents/`)
Spawn these via the Agent tool for specialized tasks:
- `java-developer` — implement features + fixes
- `test-writer` — JUnit 5 + Mockito tests
- `code-reviewer` — PMD + convention audit
- `security-auditor` — OWASP + JWT + Supabase auth review
- `supabase-integrator` — Supabase Storage operations
- `api-designer` — OpenAPI spec + REST contract design

## Slash Commands (`.claude/commands/`)
| Command | Purpose |
|---------|---------|
| `/add-endpoint` | Scaffold a new REST endpoint end-to-end |
| `/fix-pmd` | Run PMD and fix all violations |
| `/write-tests` | Write JUnit 5 tests for a class |
| `/improve-coverage` | Identify gaps and raise JaCoCo coverage |
| `/review-pr` | Review current branch changes |
| `/security-audit` | OWASP + CVE + auth pattern review |
| `/supabase-check` | Validate Supabase Storage integration |
| `/prepare-release` | Run release checklist and produce CHANGELOG entry |
