# Claude Agents — backbone-rest

AI agent roster for the **backbone-rest** Spring Boot 3.4.x / Java 21 project.

## Agents

| File | Name | Invocable | Purpose |
|------|------|-----------|---------|
| `orchestrator.md` | **Orchestrator** | ✅ User-facing | Decomposes requests, delegates to subagents, tracks progress, delivers consolidated report |
| `java-developer.md` | Java Developer | ✅ User-facing | Implement features/fixes per backbone-rest conventions (interface-first, constructor injection, PMD zero violations) |
| `test-writer.md` | Test Writer | ✅ User-facing | Write JUnit 5 + Mockito tests; ensure JaCoCo coverage and PMD compliance |
| `product-owner.md` | Product Owner | ✅ User-facing | Define acceptance criteria, refine stories, validate API contracts |
| `project-manager.md` | Project Manager | ✅ User-facing | Delivery planning, quality gate coordination, release management |
| `repo-requirements-analyst.md` | Repo Analyst | ✅ User-facing | Deep codebase analysis, pattern discovery, gap identification |
| `api-designer.md` | API Designer | ❌ Subagent | Validate `*Api.java` annotations against `api.yaml`, review REST contracts |
| `code-reviewer.md` | Code Reviewer | ❌ Subagent | Code quality, PMD convention compliance, Spring Boot patterns |
| `database-architect.md` | Database Architect | ❌ Subagent | JPA entity/query advice; PostgreSQL/Supabase pooler guidance |
| `devops-engineer.md` | DevOps Engineer | ❌ Subagent | Maven build, Docker (`amazoncorretto:21-alpine3.20`), Repsy publishing |
| `security-auditor.md` | Security Auditor | ❌ Subagent | CVE scanning, OWASP Top 10, JWT/Supabase auth review |
| `supabase-integrator.md` | Supabase Integrator | ❌ Subagent | Supabase Storage operations, signed URLs, profile image integration |

## Interaction Model

- The **Orchestrator** is the entry point for complex, multi-agent requests.
- Agents are invoked via the Agent tool; they do not call each other directly.
- Human developers approve and merge all changes; agents provide evidence (test results, PMD reports, review findings).

## Hard Constraints (all agents)

- No `mvnw` — always use `mvn` directly.
- Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` for private `com.umdc.*` dependencies.
- No `@Autowired` field injection — constructor injection only.
- No breaking changes to `/api/v1/*` contracts.
- No secrets or keystore files committed.
- Interface-first: `*Api.java` carries annotations; `*Controller.java` is thin.
- Services return `ResponseEntity<?>` — status decisions in `*ServiceImpl`.
