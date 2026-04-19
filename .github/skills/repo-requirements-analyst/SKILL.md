---
name: Repo Requirements Analyst Skills
description: Consolidated skill set for the Repo Requirements Analyst agent — codebase reconnaissance, requirements discovery, documentation, and incremental implementation
applies-to:
  - Repo Requirements Analyst
---

# Repo Requirements Analyst — Skill Definition

## 1. Repository Reconnaissance (Phase 1)

Systematically map the codebase before drawing conclusions:

```
README.md → pom.xml → src/ structure → tests → config → CI/CD
```

### Output: Repository Understanding Summary

```markdown
### What is this project? (2-3 sentences)
### Tech Stack and Architecture
### Key Entry Points and Flow
### Deployment Model
### Key Files to Reference
```

### backbone-rest Quick Reference

| Attribute | Value |
|-----------|-------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1, Spring Cloud 2024.0.0 |
| Entry point | `PrxBackofficeRestApplication.java` |
| API base | `/api/v1/*` |
| Config | `bootstrap.yml` (not `application.yml`) |
| Build | `mvn test` (no `mvnw`) |
| Private deps | Repsy (`REPSY_ACCOUNT_USER`, `REPSY_ACCOUNT_PASSWORD`) |
| Domain modules | `application`, `contacts`, `contacttypes`, `features`, `people`, `profileimage`, `report`, `roles`, `session`, `users` |

---

## 2. Requirements Discovery (Phase 2)

### Signal Sources

| Source | What to Look For |
|--------|----------------|
| README / docs | Gaps — undocumented features |
| Code comments | TODO, FIXME, design notes |
| Tests | Assertions reveal intended behavior |
| Error handling | Missing validations, silent catches |
| `bootstrap.yml` | Undocumented env vars |
| Controllers | Missing validation, implicit contracts |
| Services | Business rules encoded in `if` statements |

### Requirements Backlog Table

| Column | Description |
|--------|------------|
| Requirement ID | REQ-001, REQ-002, ... |
| Title | Clear, user-facing |
| Type | Functional / Non-functional / Security / Observability / Tech Debt |
| Evidence | Exact file paths + what was found |
| Current State | implemented / partial / missing / broken |
| Acceptance Criteria | Given/When/Then format |
| Priority | P0 (blocking) / P1 (high value) / P2 (nice-to-have) |
| Estimated Effort | S (< 1 day) / M (1-3 days) / L (> 3 days) |
| Risks/Dependencies | What could go wrong; what must be done first |

### backbone-rest Evidence Sources

| Pattern | Location |
|---------|---------|
| API contracts | `src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java` |
| Business logic | `src/main/java/com/prx/backoffice/v1/*/service/*ServiceImpl.java` |
| DTOs | `src/main/java/com/prx/backoffice/v1/*/api/to/*.java` |
| OpenAPI spec | `src/main/resources/META-INF/backbone_rest-openapi.yaml` |
| Config | `src/main/resources/bootstrap.yml`, `default.env` |
| Tests | `src/test/java/com/prx/backoffice/v1/*/` |
| Quality rules | `ruleset.xml`, `pom.xml` (JaCoCo, PMD, SonarCloud) |

---

## 3. Documentation Output (Phase 3)

### Documentation Structure

```markdown
1. Repository Overview
2. Architecture Summary (components and data flow)
3. How to Build/Test/Run (exact commands)
4. Requirements Backlog (table, ranked by priority)
5. Next Steps (recommended Iteration 1)
```

### Placement

- If `docs/` exists: create `docs/requirements-backlog.md`
- Otherwise: add to `README.md` or create `REQUIREMENTS.md`

---

## 4. Implementation (Phase 4)

### Iteration 1 Selection Criteria

| Criterion | Rule |
|-----------|-----|
| Priority | P0 > P1 > P2 |
| Risk | Lowest risk first — no breaking changes |
| Scope | S or M effort (not L) |
| Clarity | High certainty — not question-marked items |

### Implementation Checklist

```markdown
- [ ] List all files to create/modify
- [ ] Follow existing patterns exactly (naming, error handling, logging)
- [ ] Write tests before code (test-driven) or alongside
- [ ] Run existing test suite: mvn test
- [ ] Verify implementation against acceptance criteria
- [ ] Update docs inline (/// comments) and in docs/
- [ ] No hardcoded secrets
- [ ] No commented-out code left behind
- [ ] No unrelated code refactored
```

---

## 5. Behavioral Rules

- **Do NOT invent requirements** — every requirement must have evidence.
- **Use EXACT file paths and line numbers** when citing evidence.
- **Cross-reference**: if a test exists, it's implemented; if documented but not tested, it may be missing.
- **Conflicting evidence**: note as a question, propose validation.
- **Respect conventions exactly**: naming, injection style, error handling, doc style.
- **Minimal changes**: don't refactor unrelated code.

---

## Quality Checks Before Submitting

```markdown
- [ ] Every requirement is evidence-backed (can point to a file or test)
- [ ] Documentation is executable (someone can follow steps and succeed)
- [ ] Code follows backbone-rest patterns (interface-first, constructor injection, PMD-safe)
- [ ] Tests pass: mvn test
- [ ] No breaking changes to existing /api/v1/* contracts
```

