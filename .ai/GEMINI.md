# GEMINI.md — backbone-rest Agent Context for Gemma / Gemini

> This file is the **entry point** for Gemma / Gemini CLI agents working in
> this repository. It mirrors the role that `CLAUDE.md` plays for Claude Code
> and `.github/copilot-instructions.md` plays for GitHub Copilot.

---

## 1. Project Snapshot

- **Project**: `backbone-rest` — Spring Boot 3.4.1 backoffice REST service
- **Language / Runtime**: Java 21
- **Package root**: `com.umdc.backoffice` (modules under `com.umdc.backoffice.v1.<domain>`)
- **API surface**: `/api/v1/*`
- **Build tool**: Maven — `mvn` only, **no `mvnw`** in this repo
- **Private registry**: Repsy — requires `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD`
- **Current feature branch**: `ds-196-include-supabase-storage` (Supabase Storage profile-image integration)

### Domain modules under `v1/`
`application`, `contacts`, `contacttypes`, `features`, `people`, `profileimage`,
`report`, `roles`, `session`, `users`.

### Security
- OAuth2 Resource Server validates Supabase JWT for `/api/v1/**`.
- Session JWT (JJWT 0.12.3) flows through the `session-token` header.
- `/v1/sessions/token` and `/v1/sessions/validate` bypass the OAuth2 filter.

---

## 2. Hard Rules — Always Follow

1. **Interface-first controllers** — `*Api.java` declares mappings + OpenAPI
   annotations with a `default` method delegating to the service.
   `*Controller.java` is thin: `@RestController`, `@RequestMapping`,
   constructor injection, `@Override` for every API method, no business logic.
2. **Services return `ResponseEntity<?>`** — status decisions live in
   `*ServiceImpl`, never in controllers.
3. **Constructor injection only** — `@Autowired` field injection is banned.
4. **MapStruct** mappers use `config = MapperAppConfig.class` from
   `com.umdc.commons.services`.
5. **MessageUtil** is the only path for user-facing messages; keys live in
   `com.umdc.backoffice.constant.keys.*MessageKey` enums.
6. **SLF4J logging** via `LoggerFactory.getLogger(...)` — no `System.out`.
7. **PMD must report zero violations** — `ruleset.xml` is enforced at the
   Maven test phase.
8. **OpenAPI is the contract** — update
   `src/main/resources/META-INF/api.yaml` whenever an `*Api.java` interface
   changes. Preserve `/api/v1/*` backward compatibility unless the change
   request explicitly approves a break.
9. **No secrets in git** — never modify `keystore.jks` or `*.crt`, and never
   commit real credentials.
10. **No cross-domain utility controllers** — keep controllers inside their
    domain package.

---

## 3. Asset Index (.ai/)

When the user invokes an agent, skill, prompt, hook, tool, or workflow,
resolve the file from the corresponding directory below.

### Agents — `.ai/agents/`

| Agent | File | Invocable? | Purpose |
|-------|------|------------|---------|
| Orchestrator | `agents/orchestrator.agent.md` | ✅ | Decompose multi-agent requests, delegate, consolidate |
| Developer | `agents/developer.agent.md` | ✅ | Implement features / fixes following backbone-rest conventions |
| Test Writer | `agents/test-writer.agent.md` | ✅ | JUnit 5 + Mockito unit tests, PMD-compliant |
| Product Owner | `agents/product-owner.agent.md` | ✅ | Requirements, acceptance criteria, API contracts |
| Project Manager | `agents/project-manager.agent.md` | ✅ | Delivery planning, quality gates, release coordination |
| Repo Requirements Analyst | `agents/repo-requirements-analyst.agent.md` | ✅ | Deep codebase analysis & requirements discovery |
| API Reviewer | `agents/api-reviewer.agent.md` | ❌ (sub) | Validate `*Api.java` vs `api.yaml` |
| Code Reviewer | `agents/code-reviewer.agent.md` | ❌ (sub) | Code quality, conventions, PMD awareness |
| Database Architect | `agents/database-architect.agent.md` | ❌ (sub) | JPA entity / query advice |
| DevOps Engineer | `agents/devops-engineer.agent.md` | ❌ (sub) | Docker, Maven, Repsy, CI/CD |
| Security Reviewer | `agents/security-reviewer.agent.md` | ❌ (sub) | CVE scan, OWASP Top 10, auth / session audit |

Index: `.ai/agents/agents.md`.

### Skills — `.ai/skills/`

Standalone skills:
- `skills/api-contract-review.skill.md`
- `skills/release-management.skill.md`
- `skills/rest-api-design.skill.md`
- `skills/jpa-persistence.skill.md`

Per-agent skill manifests (one `SKILL.md` per directory):
- `skills/orchestrator/SKILL.md`
- `skills/developer/SKILL.md`
- `skills/test-writer/SKILL.md`
- `skills/product-owner/SKILL.md`
- `skills/project-manager/SKILL.md`
- `skills/repo-requirements-analyst/SKILL.md`
- `skills/api-reviewer/SKILL.md`
- `skills/code-reviewer/SKILL.md`
- `skills/database-architect/SKILL.md`
- `skills/devops-engineer/SKILL.md`
- `skills/security-reviewer/SKILL.md`

Index: `.ai/skills/skills.md`.

### Prompts — `.ai/prompts/`

Treat each file as an invocable prompt body (slash-command target):

| Prompt | File |
|--------|------|
| `/add-endpoint` | `prompts/add-endpoint.prompt.md` |
| `/fix-bug` | `prompts/fix-bug.prompt.md` |
| `/fix-pmd-violations` | `prompts/fix-pmd-violations.prompt.md` |
| `/improve-coverage` | `prompts/improve-coverage.prompt.md` |
| `/write-service-tests` | `prompts/write-service-tests.prompt.md` |
| `/review-pull-request` | `prompts/review-pull-request.prompt.md` |
| `/review-api-contract` | `prompts/review-api-contract.prompt.md` |
| `/security-audit` | `prompts/security-audit.prompt.md` |
| `/prepare-release` | `prompts/prepare-release.prompt.md` |
| `/full-feature-delivery` | `prompts/full-feature-delivery.prompt.md` |
| `/define-story` | `prompts/define-story.prompt.md` |
| `/bootstrap-agent-infrastructure` | `prompts/bootstrap-agent-infrastructure.prompt.md` |

Index: `.ai/prompts/prompts.md`.

### Hooks — `.ai/hooks/`

| Lifecycle moment | File |
|------------------|------|
| Before opening a PR | `hooks/pre-pull-request.hook.md` |
| After implementation | `hooks/post-implementation-review.hook.md` |
| After merge (security sweep) | `hooks/post-merge-security.hook.md` |
| Before cutting a release | `hooks/pre-release-gate.hook.md` |

Index: `.ai/hooks/hooks.md`.

### Tools — `.ai/tools/`

| Tool | File |
|------|------|
| Maven build | `tools/maven-build.tool.md` |
| PMD check | `tools/pmd-check.tool.md` |
| OpenAPI validator | `tools/openapi-validator.tool.md` |
| Dependency check (CVE) | `tools/dependency-check.tool.md` |
| SonarQube analysis | `tools/sonar-analysis.tool.md` |
| Docker build | `tools/docker-build.tool.md` |
| GitHub CLI (`gh`) | `tools/github-cli.tool.md` |
| Git | `tools/git.tool.md` |
| Java keytool | `tools/keytool.tool.md` |

Index: `.ai/tools/tools.md`.

### Workflows — `.ai/workflows/`

| Workflow | File |
|----------|------|
| Feature development | `workflows/feature-development.workflow.md` |
| Bug fix | `workflows/bug-fix.workflow.md` |
| Code review | `workflows/code-review.workflow.md` |
| Coverage improvement | `workflows/coverage-improvement.workflow.md` |
| Security audit | `workflows/security-audit.workflow.md` |
| Release | `workflows/release.workflow.md` |

Index: `.ai/workflows/workflows.md`.

---

## 4. Dispatch Cheat Sheet

| User intent | Load |
|-------------|------|
| "Add a new REST endpoint" | `prompts/add-endpoint.prompt.md` → `agents/developer.agent.md` + `agents/api-reviewer.agent.md` |
| "Fix this PMD violation" | `prompts/fix-pmd-violations.prompt.md` → `tools/pmd-check.tool.md` |
| "Write tests for class X" | `prompts/write-service-tests.prompt.md` → `agents/test-writer.agent.md` |
| "Raise coverage" | `prompts/improve-coverage.prompt.md` → `workflows/coverage-improvement.workflow.md` |
| "Review the current PR" | `prompts/review-pull-request.prompt.md` → `workflows/code-review.workflow.md` |
| "Audit security" | `prompts/security-audit.prompt.md` → `agents/security-reviewer.agent.md` + `workflows/security-audit.workflow.md` |
| "Prepare a release" | `prompts/prepare-release.prompt.md` → `workflows/release.workflow.md` → `hooks/pre-release-gate.hook.md` |
| "Validate API contract" | `prompts/review-api-contract.prompt.md` → `agents/api-reviewer.agent.md` + `tools/openapi-validator.tool.md` |
| Complex multi-step request | `agents/orchestrator.agent.md` |

---

## 5. What Not To Do

- Do **not** use `mvnw` (it does not exist here).
- Do **not** use `@Autowired` field injection.
- Do **not** break `/api/v1/*` endpoint contracts without an approved spec change.
- Do **not** commit secrets, certificates, or modify `keystore.jks` / `*.crt`.
- Do **not** put business logic in controllers — delegate to the service layer.
- Do **not** create cross-domain utility controllers.
- Do **not** edit files in `.github/` from a Gemma session unless explicitly
  asked — `.ai/` is the Gemma workspace; `.github/` is Copilot's.

---

## 6. Related Files Outside `.ai/`

| Path | Role |
|------|------|
| `AGENTS.md` | High-level agent charter (project-wide) |
| `CLAUDE.md` | Claude Code's project context (parallel to this file) |
| `.github/copilot-instructions.md` | GitHub Copilot's instructions (mirror source) |
| `src/main/resources/bootstrap.yml` | Spring Cloud bootstrap / Vault / OAuth config |
| `src/main/resources/META-INF/api.yaml` | OpenAPI 3.1 spec — the API contract |
| `ruleset.xml` | PMD ruleset enforced at test phase |
| `pom.xml` | Maven build, JaCoCo + PMD plugin config |
