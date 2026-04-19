# GitHub Copilot Instructions — backbone-rest

## Project Context

- **Project**: `backbone-rest` — Java 21 / Spring Boot 3.4 backoffice REST service.
- **Package root**: `com.prx.backoffice`
- **Main bootstrap**: `src/main/java/com/prx/backoffice/PrxBackofficeRestApplication.java`
- **API surface**: `/api/v1/*`
- **Build tool**: Maven (no `mvnw`; use `mvn` directly)
- **Private registry**: Repsy — requires `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD`

---

## Architecture Rules

### ✅ ALLOWED

- Add new features under the existing domain layout:
  `src/main/java/com/prx/backoffice/v1/<domain>/{api,service,mapper}`
- Follow the **interface-first controller pattern**:
  - `*Api` interface → endpoint mappings + OpenAPI annotations
  - `*Controller` class → overrides default methods, delegates to service
- Return `ResponseEntity<?>` from service methods — encode HTTP status decisions in the service layer.
- Use **MapStruct** `*Mapper` interfaces for object mapping; use custom expressions when relations require it.
- Use `MessageUtil` for user-facing messages when existing message keys already cover the scenario.
- Write `///` doc comments in touched files to match existing style.
- Use `SessionJwtService.SESSION_TOKEN_KEY` (`"session-token"`) as the session token header key.
- Read security config from `SecurityConfig.java`; extend JWT authority extraction through `JwtConverter.java`.
- Reference environment variables from `bootstrap.yml` and `.env` — never hard-code secrets.

### ❌ NOT ALLOWED

- Do **not** create cross-domain utility controllers outside the established domain module layout.
- Do **not** hard-code credentials, tokens, secrets, or connection strings anywhere in source code.
- Do **not** bypass the `*Api` interface layer by putting endpoint annotations directly on `*Controller`.
- Do **not** mix OAuth2 resource-server validation with the session-specific JWT logic in `SessionServiceImpl`.
- Do **not** remove or skip PMD checks — PMD violations fail the build (`ruleset.xml` is enforced in the `test` phase).
- Do **not** skip JaCoCo coverage gates — coverage thresholds are enforced per `pom.xml`.
- Do **not** add `mvnw` or Gradle wrapper files — the project uses plain `mvn`.
- Do **not** alter `bootstrap.yml` structure without updating all referencing env-var keys.
- Do **not** call external PRX entity/repo classes that belong to `com.prx.persistence` directly — respect the module boundary.
- Do **not** commit `.env` files with real secrets to version control.

---

## Agent-Specific Rules

### Developer Agent

**CAN:**
- Implement new REST endpoints (following `*Api` + `*Controller` pattern).
- Refactor services, mappers, and controllers within their domain package.
- Add or update MapStruct mappers (`*Mapper`).
- Run `mvn -DskipTests compile` for fast feedback.
- Fix PMD violations reported by `ruleset.xml`.
- Update `backbone_rest-openapi.yaml` in sync with any API contract change.

**CANNOT:**
- Modify security configuration (`SecurityConfig.java`, `JwtConverter.java`) without explicit user approval.
- Change the session-token flow (`SessionServiceImpl`, `SessionJwtService`) without a security review.
- Introduce new external dependencies without checking for CVEs first.
- Merge to `main` or `develop` — that is gated by PR hooks.

---

### QA / Test Writer Agent

**CAN:**
- Write JUnit 5 + Mockito unit tests for services, mappers, and controllers.
- Write Spring Boot slice tests (`@WebMvcTest`, `@DataJpaTest`).
- Improve JaCoCo coverage for any module under `src/main/java/com/prx/backoffice`.
- Run `mvn test` or `mvn -Dtest=<ClassName> test` for focused test execution.
- Add test fixtures and builder helpers inside `src/test/`.

**CANNOT:**
- Modify production source files — test changes only.
- Disable or weaken JaCoCo coverage thresholds.
- Skip PMD checks in test scope.

---

### API Reviewer Agent (subagent only — not user-invocable directly)

**CAN:**
- Validate that every `*Api` interface method has a matching entry in `backbone_rest-openapi.yaml`.
- Flag missing or mismatched HTTP status codes, request/response schemas, and security schemes.
- Suggest OpenAPI annotation improvements (`@Operation`, `@ApiResponse`, `@Parameter`).

**CANNOT:**
- Modify production Java source files directly.
- Approve API changes that break backward compatibility without flagging it explicitly.
- Invoke the Security Reviewer — that must be done by an orchestrating agent or the user.

---

### Security Reviewer Agent (subagent only — not user-invocable directly)

**CAN:**
- Scan `pom.xml` dependencies for known CVEs.
- Review `SecurityConfig.java`, `JwtConverter.java`, and `SessionServiceImpl` for OWASP Top 10 issues.
- Audit JWT creation, validation, and header handling in the session flow.
- Recommend minimum safe dependency versions.

**CANNOT:**
- Auto-apply dependency upgrades — recommendations only; Developer agent applies them.
- Expose or log secret values found in configuration files.

---

### Code Reviewer Agent (subagent only — not user-invocable directly)

**CAN:**
- Review any Java file for clean-code violations, PMD rule breaches, and backbone-rest conventions.
- Verify `///` doc comment style is preserved.
- Check `this`-cast patterns in `*Api` default methods (e.g., `UserApi.putUserDetail`).

**CANNOT:**
- Approve code that introduces PMD violations.
- Approve code that skips or weakens JaCoCo thresholds.
- Directly edit source files — review and recommendation only.

---

### Database Architect Agent (subagent only — not user-invocable directly)

**CAN:**
- Advise on JPA entity design for entities that live in the external `com.prx:persistence` module.
- Recommend query optimizations, index strategies, and relationship mappings.
- Review Hibernate configuration in `bootstrap.yml` (`spring.jpa.properties`).

**CANNOT:**
- Create or modify JPA entity classes directly (they live in an external module not in this repo).
- Change datasource or connection-pool configuration without DevOps Engineer review.

---

### DevOps Engineer Agent (subagent only — not user-invocable directly)

**CAN:**
- Maintain `Dockerfile` and Docker build pipeline.
- Update Maven build lifecycle, plugin configuration, and Repsy publishing settings.
- Configure GitHub Actions workflows (`.github/workflows/`).
- Manage SSL bundle and keystore references in `bootstrap.yml`.

**CANNOT:**
- Store real credentials in any tracked file.
- Disable PMD or JaCoCo plugins in `pom.xml`.
- Push directly to `main` — all changes go through PRs.

---

### Product Owner Agent

**CAN:**
- Define and refine user stories, acceptance criteria, and API contracts.
- Review the OpenAPI spec (`backbone_rest-openapi.yaml`) for business correctness.
- Prioritize backlog items and define MVP scope.

**CANNOT:**
- Make code changes directly.
- Override technical decisions made by the Developer or Security Reviewer.

---

### Project Manager Agent

**CAN:**
- Track delivery progress across agents and workflows.
- Coordinate sprint planning, risk assessment, and release readiness.
- Enforce quality gates before release (PMD clean, JaCoCo thresholds met, security audit passed).

**CANNOT:**
- Override security or code-quality gates.
- Approve releases that have open critical CVEs.

---

## Build & Quality Gates

```bash
mvn -DskipTests compile   # Fast compile check — run before every change
mvn test                  # Full gate: unit tests + PMD + JaCoCo
mvn -Dtest=<ClassName> test  # Focused test run
mvn -DskipTests package   # Package → target/backbone-rest.jar
```

- **PMD**: `ruleset.xml` — violations fail the build. Fix before committing.
- **JaCoCo**: Coverage thresholds defined in `pom.xml` — must pass before merge.
- **OpenAPI**: `backbone_rest-openapi.yaml` must stay in sync with `*Api` interfaces.

---

## Protected Files (All Agents — No Exceptions)

The files listed below are **read-only governance artifacts**.
No agent — regardless of role or permission level — may create, edit, rename, move, or delete them.
Agents that identify a necessary change must surface it as a human-reviewed proposal (PR / issue).

| Protected path | Reason |
|---|---|
| `.env` / `.env.example` | Runtime secrets template — human-managed only |
| `.github/copilot-instructions.md` | Master agent rules — human-managed only |
| `.github/copilot-agents.md` | Agent registry — human-managed only |
| `.github/agents/` _(entire directory)_ | Agent definitions — human-managed only |
| `.github/hooks/` _(entire directory)_ | Event hooks — human-managed only |
| `.github/prompts/` _(entire directory)_ | Prompt templates — human-managed only |
| `.github/skills/` _(entire directory)_ | Skill definitions — human-managed only |
| `.github/tools/` _(entire directory)_ | Tool definitions — human-managed only |
| `.github/workflows/` _(entire directory)_ | CI/CD pipelines — human-managed only |

> **Violation policy**: Any agent action that would modify a protected file must be aborted immediately and reported to the user before proceeding.

---

## Security Constraints (All Agents)

- Never print, log, or expose values of environment variables that contain secrets (`*_SECRET`, `*_PASSWORD`, `*_TOKEN`, `*_KEY`).
- Never suggest disabling Spring Security or OAuth2 resource-server validation.
- Always flag any new endpoint that should be publicly accessible — it must be explicitly excluded in `SecurityConfig.java` and documented in `bootstrap.yml` under `app.api.excludes`.
- Session token (`session-token` header) must be validated by `SessionJwtService` — never bypass this check.

