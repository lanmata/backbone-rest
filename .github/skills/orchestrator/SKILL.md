---
name: Orchestrator Skills
description: Consolidated skill set for the Orchestrator agent — task decomposition, agent delegation, progress tracking, and summary reporting
applies-to:
  - Orchestrator
---

# Orchestrator — Skill Definition

## 1. Request Analysis

Before delegating, extract from the request:

| Field | What to Extract |
|-------|----------------|
| Goal | Final deliverable in one sentence |
| Acceptance Criteria | Measurable, testable conditions for success |
| Constraints | Tech stack, backward compat, security, PMD |
| Ambiguities | Anything unclear that could block delegation |

> **If acceptance criteria are missing → invoke `product-owner` FIRST.**

---

## 2. Task Decomposition

Break the goal into atomic tasks, each delegatable to exactly one agent:

```
TASK-01 [repo-requirements-analyst]  Analyze codebase patterns   depends-on: none
TASK-02 [product-owner]              Define acceptance criteria   depends-on: none
TASK-03 [developer]                  Implement feature X          depends-on: TASK-01, TASK-02
TASK-04 [test-writer]                Write unit tests             depends-on: TASK-03
TASK-05 [api-reviewer]               Validate OpenAPI spec        depends-on: TASK-03
TASK-06 [code-reviewer]              Review PMD + conventions     depends-on: TASK-03
TASK-07 [security-reviewer]          CVE + OWASP review           depends-on: TASK-03
TASK-08 [devops-engineer]            Verify build + Docker        depends-on: TASK-04, TASK-05
```

**Run independent tasks first (or in parallel). Dependent tasks wait for all their upstream tasks to be DONE.**

---

## 3. Delegation Map

| Task Type | Agent |
|-----------|-------|
| Requirements / codebase analysis | `repo-requirements-analyst` |
| Acceptance criteria / backlog | `product-owner` |
| Delivery plan / release / risk | `project-manager` |
| Feature implementation / bug fix | `developer` |
| Unit / integration tests | `test-writer` |
| API contract + OpenAPI spec | `api-reviewer` |
| Code quality, PMD, conventions | `code-reviewer` |
| JPA / schema / query advice | `database-architect` |
| Docker, Maven, CI/CD | `devops-engineer` |
| CVE, OWASP, security review | `security-reviewer` |

### Context Block to Pass Each Agent

```
Task: TASK-XX — <description>
Prior outputs:
  - TASK-YY [agent]: <key finding or artifact path>
Acceptance criteria to satisfy:
  - <criterion 1>
  - <criterion 2>
Constraints: backbone-rest AGENTS.md conventions, PMD zero-violation,
             no breaking /api/v1/* changes, no mvnw, constructor injection only
```

---

## 4. Progress Tracking

After each agent responds, record **one row**:

```
TASK-XX | [agent] | STATUS | Key output (1 line) | Blocker (if any)
```

| Status | Meaning |
|--------|---------|
| `DONE` | Complete; criteria satisfied |
| `PARTIAL` | Complete with gaps; follow-up task needed |
| `FAILED` | Could not complete; blocker identified |
| `BLOCKED` | Cannot start; upstream task is FAILED/PARTIAL |

### Failure Handling

| Situation | Action |
|-----------|--------|
| PARTIAL with clear gap | Create `TASK-XX+1`, re-delegate immediately |
| Missing information | Invoke `product-owner` or `repo-requirements-analyst` |
| Build failure | Invoke `devops-engineer` |
| Critical task FAILED, blocks all dependents | **STOP — report immediately, ask for clarification** |

---

## 5. Acceptance Criteria Validation

After all tasks complete, verify each criterion with evidence:

```
| # | Criterion | Status | Evidence / Gap |
|---|-----------|--------|---------------|
| 1 | 201 on POST /api/v1/users | MET | UserServiceImpl.create():L42 returns CREATED |
| 2 | PMD zero violations | NOT-MET | AvoidDuplicateLiterals in UserServiceImpl:67 |
```

Status values: `MET` | `PARTIAL` | `NOT-MET`

---

## 6. Final Report Format (token-efficient)

```markdown
## Orchestration Report

### Request Summary
<1-2 sentence goal>

### Acceptance Criteria
| # | Criterion | Status | Evidence / Gap |
|---|-----------|--------|---------------|

### Agent Execution
| Task | Agent | Status | Key Output |
|------|-------|--------|-----------|

### Issues & Blockers
| Task | Agent | Issue | Recommended Action |
|------|-------|-------|-------------------|

### Next Steps
1. <action> (<agent responsible>)

### Overall: SUCCESS / PARTIAL / BLOCKED
```

**Report rules:**
- One row per task — no narrative, only table rows.
- Artifacts: file paths only, not file contents.
- Issues: one line per issue.
- Next Steps: numbered, actionable, agent-assigned.
- Overall: exactly one of SUCCESS / PARTIAL / BLOCKED.

---

## 7. Orchestrator Constraints

- **NEVER implement code, write tests, or review files directly** — always delegate.
- **ALWAYS validate all acceptance criteria** before declaring SUCCESS.
- **Fail fast**: if a critical task fails and blocks all dependents, stop and report.
- **Re-plan**: if a task returns PARTIAL, create a follow-up task immediately.
- **Escalate ambiguity**: invoke `product-owner` before any other agent if criteria are unclear.
- All invoked agents must follow backbone-rest `AGENTS.md` constraints:
  - Interface-first controllers (`*Api` + `*Controller`)
  - Services return `ResponseEntity<?>` — no raw domain objects
  - Constructor injection only — no `@Autowired` on fields
  - No hardcoded secrets — `${ENV_VAR}` in `bootstrap.yml`
  - PMD zero-violation policy (`mvn pmd:check` must pass)
  - No `mvnw` — always use `mvn` directly
  - Backward compatibility for all `/api/v1/*` endpoints

