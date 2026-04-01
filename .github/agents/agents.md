# AI Agents - Overview

This directory documents the AI agents and subagents that act as developer coworkers for the **backbone-rest** project. Each agent is a specialized collaborator with prescribed responsibilities, skills, and allowed tools. Agents are advisory and automation-focused; they do not make unilateral changes to production systems without developer review.

## Agents in This Repository

| File                                  | Name                        | Invocable | Purpose                                                       |
|---------------------------------------|-----------------------------|-----------|---------------------------------------------------------------|
| `developer.agent.md`                  | Developer                   | ✅         | Implement features/fixes following backbone-rest conventions   |
| `test-writer.agent.md`                | QA / Test Writer            | ✅         | Write JUnit 5 + Mockito unit tests, ensure PMD compliance     |
| `product-owner.agent.md`              | Product Owner               | ✅         | Define requirements, acceptance criteria, API contracts        |
| `project-manager.agent.md`            | Project Manager             | ✅         | Delivery planning, quality gates, release coordination         |
| `repo-requirements-analyst.agent.md`  | Repo Requirements Analyst   | ✅         | Deep codebase analysis, requirements discovery, documentation  |
| `api-reviewer.agent.md`               | API Reviewer                | ❌         | Validate *Api.java against `backbone_rest-openapi.yaml`        |
| `code-reviewer.agent.md`              | Code Reviewer               | ❌         | Code quality, convention compliance, PMD awareness             |
| `database-architect.agent.md`         | Database Architect          | ❌         | JPA entity/query advice (entities live in external `com.prx:persistence`) |
| `devops-engineer.agent.md`            | DevOps Engineer             | ❌         | Docker, Maven build, Repsy publishing, CI/CD setup             |
| `security-reviewer.agent.md`          | Security Reviewer           | ❌         | CVE scanning, OWASP Top 10, auth/session security review       |

## Agent Interaction Model

- Agents collaborate through GitHub: issues, PRs, and code review.
- Agents produce clear artifacts: PRs with code, tests, docs, and checklists.
- Human reviewers approve and merge changes; agents provide CI evidence (tests, PMD/JaCoCo reports).

## Important Constraints

- Agents must follow the project conventions in `AGENTS.md` (interface-first controllers, service returns `ResponseEntity`, MapStruct with `MapperAppConfig`, `*MessageKey` constants).
- Agents must never commit secrets, certificates, or keystore material.
- Agents preserve `/api/v1/*` contract backwards compatibility unless a change request explicitly states otherwise.
- There is no `mvnw` — always use `mvn` directly.
- Private PRX dependencies (`com.prx.*`) are resolved from Repsy; set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD`.
