---
name: Orchestrator Skills
description: Consolidated skill set for the Orchestrator agent — task decomposition, delegation, progress tracking, and structured reporting for backbone-rest
applies-to:
  - orchestrator
---

# Orchestrator — Skill Definition

## 1. Task Decomposition Template

For any feature request, produce this task plan before delegating:

```
TASK-01 [product-owner]              Define/validate acceptance criteria      depends-on: none
TASK-02 [repo-requirements-analyst]  Analyze existing patterns in domain      depends-on: none
TASK-03 [database-architect]         Validate JPA/query requirements          depends-on: TASK-02
TASK-04 [java-developer]             Implement feature + OpenAPI update        depends-on: TASK-01, TASK-02, TASK-03
TASK-05 [test-writer]                Write unit tests                          depends-on: TASK-04
TASK-06 [api-designer]               Review API contract                       depends-on: TASK-04
TASK-07 [code-reviewer]              Review code quality + PMD                 depends-on: TASK-04
TASK-08 [security-auditor]           Security review of new code               depends-on: TASK-04
TASK-09 [devops-engineer]            Verify build + Docker image               depends-on: TASK-05, TASK-06, TASK-07
```

---

## 2. Delegation Rules

- Run TASK-01 and TASK-02 in parallel (no dependencies between them).
- TASK-05, TASK-06, TASK-07, TASK-08 can run in parallel after TASK-04 completes.
- Stop on CRITICAL failure — do not continue blocked dependents.
- If a task is PARTIAL, re-delegate with the gap explicitly described.

---

## 3. Context Passing

Always pass these to `java-developer` (TASK-04):
1. Patterns found by `repo-requirements-analyst` (exact file paths + patterns).
2. JPA constraints from `database-architect` (entity names, query methods).
3. Acceptance criteria from `product-owner` (verbatim list).

---

## 4. Progress Tracking Table

| Task | Agent | Status | Key Output |
|------|-------|--------|-----------|
| TASK-01 | product-owner | PENDING / IN_PROGRESS / DONE / FAILED | AC list |
| TASK-02 | repo-requirements-analyst | ... | Patterns found |
| ... | ... | ... | ... |

---

## 5. Final Report Template

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
1. <action> (<agent>)

### Overall: SUCCESS / PARTIAL / BLOCKED
```

---

## 6. Constraints

- Never implement code, write tests, or run reviews yourself.
- Never invoke an agent without providing the full context it needs.
- Always verify acceptance criteria against agent outputs before reporting SUCCESS.
- PARTIAL is not SUCCESS — always surface gaps in the report.
