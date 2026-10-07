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
`addresses`, `application`, `contacts`, `contacttypes`, `features`, `iam`
(audit / permissions / tokens / passwords), `identificationdocuments`,
`managedclient` (MCAM — M2M client-credential auth), `notices`, `noticetypes`,
`people`, `profileimage`, `report`, `rolefeatures` (shared Role↔Feature link
service, no REST surface), `roles`, `servicetype`, `session`, `users`

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
9. **OpenAPI YAML** at `src/main/resources/static/api.yaml` (served statically at
   `/api.yaml`, per `springdoc.swagger-ui.url` in `application.yml`) — update
   it when any `*Api.java` contract changes.

## Security Architecture
- **OAuth2 Resource Server** (Supabase / Keycloak JWT): all `/api/v1/**` GETs require
  a valid JWT. Roles extracted from `resource_access.<clientId>.roles` by `JwtConverter`.
- **Session JWT** (JJWT 0.12.3): minted in `SessionServiceImpl`, transmitted via
  `session-token` header. Separate from OAuth2 validation.
- Endpoints `/api/v1/session/token` and `/api/v1/session/validate` bypass OAuth2 filter.
- **JTI deny-list**: `JtiDenyListServiceImpl` stores revoked JTIs in Redis under
  `jti:<value>` with the token's remaining TTL — Redis expires them natively,
  no scheduled cleanup job needed.
- **Shared with Mercury:** Mercury verifies this service's session JWT locally with the *same* `APP_TOKEN_SECRET`, requires
  `iss`/`aud` (`JWT_ISSUER`/`JWT_AUDIENCE` must be non-empty and listed in Mercury's `APP_TOKEN_TRUSTED_ISSUERS`/
  `_AUDIENCES`), requires `type=session-token` (refresh tokens share the key and carry `uid` — never accept one as a
  session), and calls `GET /api/v1/session/validate` (public; token in the `Authorization` header, raw) to honour the
  JTI deny-list. Treat those as a cross-service contract. See `docs/v1/00-prerequisites.md`.
- **`APP_TOKEN_SECRET`**: sourced from `${APP_TOKEN_SECRET}` (no default/fallback),
  injected via Vault + Spring Cloud Config (`spring.config.import` in `application.yml`). Never hardcoded.
  Rotation is an operational Vault procedure, not application code — the
  rotation cadence/runbook is not documented here; owner to fill in.

## Known Issues (fixed 2026-09-30 — see below for the current, working config)
Two bugs made Swagger UI return a bare `403 Forbidden` instead of loading, and masked real
error status codes app-wide. Both are now fixed in `SecurityConfig.java` / `application.yml`;
documenting them here since the symptom is non-obvious and the fix touches security config.

1. **`/error` was not `permitAll`.** Any request that internally forwards to `/error` (a 404
   from a missing static resource, a 400 from `HttpMessageNotReadableException`, a 500,
   anything) gets re-evaluated by the security filter chain on that forward. Since `/error`
   fell under `anyRequest().authenticated()` and the request is anonymous, `AuthorizationFilter`
   denied it and the client saw a bare `403 Forbidden` instead of the real status code — e.g. a
   genuine 404 or 400 both came back as indistinguishable 403s. Verified with
   `-Dlogging.level.org.springframework.security=TRACE`: the original request was correctly
   authorized (`SingleResultAuthorizationManager` granted it), the handler threw/resolved to
   `/error`, and *that* forwarded dispatch is what got `AuthorizationDeniedException`'d.
   **Fix**: `"/error"` added to the permitAll matcher list, with a comment explaining why.
2. **`springdoc.api-docs.enabled: false` is springdoc's master switch, not just a toggle for
   `/v3/api-docs`.** It had been set to `false` to avoid exposing the auto-generated OpenAPI doc
   (incomplete because every `*Api.java` method returns `ResponseEntity<?>` — convention #3 —
   and that wildcard erasure means springdoc can't resolve a response schema). But disabling it
   took down the *entire* springdoc-openapi-starter-webmvc-ui autoconfiguration, including the
   resource handlers for `/swagger-ui.html`, `/swagger-ui/**`, and `/swagger-resources` — there
   was no controller registered for them at all (confirmed via
   `NoResourceFoundException: No static resource swagger-resources ...` in the TRACE log).
   Combined with bug #1, this presented as `403 Forbidden` rather than `404`, which looked like
   a permissions problem instead of "the UI isn't wired up."
   **Fix**: `springdoc.api-docs.enabled` set back to `true` (required for the UI backend to
   register at all), while `/v3/api-docs` — the actual generated spec document — is deliberately
   **excluded** from `SecurityConfig.SWAGGER_PATHS`, so it still requires auth and a client can't
   stumble onto the broken generated doc by mistake. Swagger UI itself renders the real,
   hand-maintained `api.yaml` instead, via `springdoc.swagger-ui.url: /api.yaml` (already set).
3. **Follow-on gotcha from fix #2**: once the page loaded, it showed "Failed to load remote
   configuration." Swagger UI's own bootstrap JS unconditionally fetches
   `/v3/api-docs/swagger-config` (a small JSON describing which spec URL to render — no schema/
   path data in it) regardless of `swagger-ui.url`, and that path was still blocked by the same
   exclusion as `/v3/api-docs`. **Fix**: `/v3/api-docs/swagger-config` added as its own explicit
   `permitAll` entry in `SWAGGER_PATHS`, separate from `/v3/api-docs` itself.
   `/swagger-ui.html` also needed its own explicit entry — the `/swagger-ui/**` pattern doesn't
   match the exact literal `/swagger-ui.html` (no `/` after `swagger-ui`), which is the actual
   page entry point (it 302-redirects to `/swagger-ui/index.html`).

**Current working state**: `GET https://localhost:8084/swagger-ui.html` → 302 →
`/swagger-ui/index.html` → 200, rendering `api.yaml`. `GET /v3/api-docs` stays `403` (by
design). Verify after any future springdoc/security change with the curl checks above, not just
by eyeballing the page — the "page loads" and "page's own API calls succeed" states are
different failure surfaces, as bug #3 showed.

Also noted while debugging: `server.port` is `8084` over **HTTPS** with a custom cert
(`backbone.jks` / PRX Internal CA, TLS 1.3 only) — hitting `http://` or the wrong port looks
like "won't load" too, independent of the bugs above. And `default.env` with real runtime
values (Vault token, Config Server URI, etc. for the `remote-supabase` profile) lives at the
**project root** (`default.env`), gitignored — not at `src/main/resources/default.env` as the
Key Files table below says. The IntelliJ run configuration (`.idea/workspace.xml`) loads it via
the EnvFile plugin. To run the same way from a shell:
`set -a; source default.env; set +a; mvn -Dspring-boot.run.profiles=remote-supabase spring-boot:run`.
Don't run both an IntelliJ instance and a shell-launched instance at once — whichever binds
port 8084 first wins and the other's `mvn` process just sits there failing to bind, which looks
like your code change had no effect when it's actually a stale second instance still answering.

## Dependency Security (Dependabot)
- **Review cadence**: check open alerts at the start of any work session that
  touches `pom.xml`, and at least weekly otherwise —
  `gh api repos/lanmata/backbone-rest/dependabot/alerts --paginate -q '.[] | select(.state=="open")'`.
  Don't rely on discovering them only via the warning GitHub prints on `git push`.
- **Remediation pattern**: when a CVE is in a *transitive* dependency (no direct
  `<dependency>` entry to bump), add a `<properties>` entry named `<lib>.version`
  next to the other centralized versions, then an explicit override in
  `<dependencyManagement>` pinning that artifact to it — see `tomcat.version`,
  `bouncycastle.version`, `guava.version`, `nimbus-jose-jwt.version`,
  `handlebars.version`, `rhino.version`, `httpclient.version` in `pom.xml` for
  worked examples (added to close 39 alerts in one pass — see PR #59).
  Always verify with `mvn dependency:tree -Dincludes=<groupId>:<artifactId>`
  that the override actually took effect before assuming a CVE is closed.
- **Test-scope-only CVEs** (pulled in by `mockserver-junit-jupiter` /
  `spring-cloud-contract-verifier`) still get the same override treatment for
  hygiene, even though they never ship in the runtime JAR — see the same PR.
- After merging a remediation, GitHub's dependency graph re-scans on the next
  `submit-maven` Action run and typically auto-closes the alerts within minutes;
  no manual "resolve" step is needed.

## Supabase Integration (Current Branch: ds-196-include-supabase-storage)
- Auth: `AUTH_SERVER_URI=https://jygwixrpoxcrltmeshyl.supabase.co`
- DB: PostgreSQL pooler at `aws-1-us-east-2.pooler.supabase.com:6543`
- Profile image storage: Supabase Storage (`profileimage` domain)
- Spring profile for Supabase: `remote-supabase`

## Key Files
| File | Purpose |
|------|---------|
| `src/main/resources/application.yml` | Central config — Vault, Config Server, OAuth (migrated off the legacy `bootstrap.yml`/`spring-cloud-starter-bootstrap` mechanism for GraalVM AOT compatibility — see `spring.config.import`) |
| `src/main/resources/static/api.yaml` | OpenAPI 3.1 spec (served at `/api.yaml`) |
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
