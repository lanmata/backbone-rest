# AI Agent Coworkers — backbone-rest

This document indexes all AI agents, skills, tools, and workflows configured
for the **backbone-rest** Spring Boot 3.4.1 backoffice REST service.

## Agents

### Primary Agents (User-Invocable)

| Agent | File | Description |
|-------|------|-------------|
| Developer | `agents/developer.agent.md` | Senior backend developer (Java 21 / Spring Boot 3.4.1) |
| QA / Test Writer | `agents/test-writer.agent.md` | JUnit 5 + Mockito test authoring |
| Product Owner | `agents/product-owner.agent.md` | Business stakeholder / requirements |
| Project Manager | `agents/project-manager.agent.md` | Delivery lead / release coordination |
| Repo Requirements Analyst | `agents/repo-requirements-analyst.agent.md` | Codebase analysis / requirement discovery |

### Subagents (Agent-Invocable Only)

| Agent | File | Description |
|-------|------|-------------|
| API Reviewer | `agents/api-reviewer.agent.md` | `*Api.java` ↔ `backbone_rest-openapi.yaml` validation |
| Security Reviewer | `agents/security-reviewer.agent.md` | CVE scanning / OWASP review / JWT audit |
| Code Reviewer | `agents/code-reviewer.agent.md` | Code quality / PMD / backbone-rest convention compliance |
| Database Architect | `agents/database-architect.agent.md` | JPA advice (entities in external `com.prx:persistence`) |
| DevOps Engineer | `agents/devops-engineer.agent.md` | Docker / Maven build / Repsy publishing / CI setup |

## Skills

| Skill | File | Used By |
|-------|------|---------|
| Java Spring Development | `skills/java-spring-development.skill.md` | Developer, Code Reviewer |
| REST API Design | `skills/rest-api-design.skill.md` | Developer, API Reviewer, Product Owner |
| JPA Persistence | `skills/jpa-persistence.skill.md` | Developer, Database Architect |
| MapStruct Mapping | `skills/mapstruct-mapping.skill.md` | Developer |
| Feign Integration | `skills/feign-integration.skill.md` | Developer |
| Spring Security OAuth2 | `skills/kafka-messaging.skill.md` | Developer, Security Reviewer |
| JUnit 5 Testing | `skills/junit5-testing.skill.md` | QA / Test Writer, Developer |
| JaCoCo Coverage | `skills/jacoco-coverage.skill.md` | QA / Test Writer, Project Manager |
| API Contract Review | `skills/api-contract-review.skill.md` | API Reviewer, Product Owner, Developer |
| Backlog Management | `skills/backlog-management.skill.md` | Product Owner, Project Manager |
| Release Management | `skills/release-management.skill.md` | Project Manager, DevOps Engineer |

## Tools

| Tool | File | Type |
|------|------|------|
| Maven Build | `tools/maven-build.tool.md` | Terminal |
| Docker Build | `tools/docker-build.tool.md` | Terminal |
| GitHub CLI | `tools/github-cli.tool.md` | Terminal |
| Dependency Check | `tools/dependency-check.tool.md` | Terminal |
| OpenAPI Validator | `tools/openapi-validator.tool.md` | Terminal |
| Sonar Analysis | `tools/sonar-analysis.tool.md` | Terminal |

## Workflows

| Workflow | File | Trigger | Agents Involved |
|----------|------|---------|-----------------|
| Feature Development | `workflows/feature-development.workflow.md` | Manual | PO, Dev, QA, API Rev, Code Rev |
| Bug Fix | `workflows/bug-fix.workflow.md` | Manual | Dev, QA, Code Rev |
| Security Audit | `workflows/security-audit.workflow.md` | Manual | Sec Rev, Dev, PM |
| Code Review | `workflows/code-review.workflow.md` | Pull Request | Code Rev, API Rev, Sec Rev |
| Release | `workflows/release.workflow.md` | Manual | PM, Sec Rev, API Rev, Dev, DevOps |
| Coverage Improvement | `workflows/coverage-improvement.workflow.md` | Manual | QA, Dev, PM |

## Agent Collaboration Map

```
                    ┌──────────────────┐
                    │  Project Manager │
                    └────────┬─────────┘
                             │ coordinates
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
     ┌─────────────┐ ┌────────────┐ ┌──────────────┐
     │Product Owner│ │  Developer │ │QA/Test Writer│
     └──────┬──────┘ └─────┬──────┘ └──────┬───────┘
            │               │               │
            │  requirements │ implementation│ tests
            │               │               │
            ▼               ▼               ▼
     ┌──────────────────────────────────────────┐
     │              Subagents                    │
     │                                           │
     │  ┌─────────────┐  ┌──────────────────┐   │
     │  │API Reviewer  │  │Security Reviewer │   │
     │  └─────────────┘  └──────────────────┘   │
     │  ┌─────────────┐  ┌──────────────────┐   │
     │  │Code Reviewer │  │Database Architect│   │
     │  └─────────────┘  └──────────────────┘   │
     │  ┌──────────────────┐                     │
     │  │ DevOps Engineer  │                     │
     │  └──────────────────┘                     │
     └──────────────────────────────────────────┘
```

## Quick Start

1. **New feature**: Use the [Feature Development Workflow](workflows/feature-development.workflow.md)
2. **Bug fix**: Use the [Bug Fix Workflow](workflows/bug-fix.workflow.md)
3. **Pre-release**: Use the [Release Workflow](workflows/release.workflow.md)
4. **Coverage gap**: Use the [Coverage Improvement Workflow](workflows/coverage-improvement.workflow.md)
5. **Security check**: Use the [Security Audit Workflow](workflows/security-audit.workflow.md)

## Project Quick Reference

```bash
mvn -DskipTests compile   # Fast compile check
mvn test                  # Tests + PMD + JaCoCo (single gate command)
mvn -DskipTests package   # Package → target/backbone-rest.jar
docker build -t lamata/backbone-rest .
```

> **No `mvnw`** — always use `mvn` directly.  
> Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` for private PRX dependency resolution.
