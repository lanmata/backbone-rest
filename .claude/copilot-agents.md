# backbone-rest — Claude Agent Infrastructure

Master index for the Claude Code agent infrastructure in this project.

## Project Snapshot

| Field | Value |
|-------|-------|
| Name | backbone-rest |
| Language | Java 21 |
| Framework | Spring Boot 3.4.x, Spring Cloud 2024.x |
| Build | Maven 3.x (no `mvnw`) |
| Test | JUnit 5 + Mockito |
| Static analysis | PMD (`ruleset.xml`) + JaCoCo |
| Config | Spring Cloud Config + HashiCorp Vault (`bootstrap.yml`) |
| Auth | OAuth2 Resource Server (Supabase JWT) + Session JWT (JJWT 0.12.3) |
| Storage | Supabase Storage (AWS S3 SDK) |
| Container | Docker (`amazoncorretto:21-alpine3.20`, port 8082) |

---

## Indexes

| Index | Location | Purpose |
|-------|----------|---------|
| Agents | [`.claude/agents/agents.md`](.claude/agents/agents.md) | All agents, invocability, purpose |
| Skills | [`.claude/skills/skills.md`](.claude/skills/skills.md) | Shared + per-agent skill catalog |
| Tools | [`.claude/tools/tools.md`](.claude/tools/tools.md) | All tools, who uses them, key commands |
| Prompts | [`.claude/prompts/prompts.md`](.claude/prompts/prompts.md) | All prompts, agent, mode, trigger |
| Hooks | [`.claude/hooks/hooks.md`](.claude/hooks/hooks.md) | All hooks, trigger, blocking, lifecycle |
| Commands | `.claude/commands/` | Claude Code slash commands (`/add-endpoint`, `/fix-pmd`, etc.) |

---

## Agent Quick Reference

| Agent | File | Invocable | Primary Use |
|-------|------|-----------|-------------|
| Orchestrator | `agents/orchestrator.md` | ✅ | Multi-agent feature delivery |
| Java Developer | `agents/java-developer.md` | ✅ | Feature impl, bug fixes |
| Test Writer | `agents/test-writer.md` | ✅ | JUnit 5 test authoring |
| Product Owner | `agents/product-owner.md` | ✅ | Story definition, AC |
| Project Manager | `agents/project-manager.md` | ✅ | Delivery plans, release checklists |
| Repo Analyst | `agents/repo-requirements-analyst.md` | ✅ | Codebase pattern discovery |
| API Designer | `agents/api-designer.md` | ❌ | OpenAPI + contract review |
| Code Reviewer | `agents/code-reviewer.md` | ❌ | PMD + convention audit |
| Database Architect | `agents/database-architect.md` | ❌ | JPA + schema advice |
| DevOps Engineer | `agents/devops-engineer.md` | ❌ | Maven, Docker, CI/CD |
| Security Auditor | `agents/security-auditor.md` | ❌ | OWASP, JWT, CVE |
| Supabase Integrator | `agents/supabase-integrator.md` | ❌ | Storage, signed URLs |

---

## Quick Start

```bash
# Full feature delivery (multi-agent)
# → Use orchestrator with full-feature-delivery.prompt.md

# Add a single endpoint
# → /add-endpoint (or implement-feature.prompt.md)

# Fix PMD violations
# → /fix-pmd (or fix-pmd-violations.prompt.md)

# Write tests for a class
# → /write-tests (or write-unit-tests.prompt.md)

# Pre-PR quality gate (manual)
mvn -DskipTests compile && mvn pmd:check && mvn test

# Prepare a release
# → /prepare-release (or prepare-release.prompt.md)
```

---

## Hard Rules (all agents and humans)

1. No `mvnw` — use `mvn`.
2. No `@Autowired` field injection — constructor injection only.
3. No breaking changes to `/api/v1/*` contracts.
4. No secrets committed — `default.env` is stubs only.
5. Zero PMD violations before any PR.
6. `api.yaml` must stay in sync with `*Api.java`.
7. Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` before Maven runs.
