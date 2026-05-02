---
name: Full Feature Delivery
description: Orchestrate complete feature delivery from requirements to tested, reviewed, deployable code
mode: agent
agent: orchestrator
tools: [run_subagent, read_file, grep_search, file_search, create_file, run_in_terminal]
---

Orchestrate full delivery of a new feature for **backbone-rest**.

## Feature Request

- Title: ${title}
- Description: ${description}
- Domain: ${domain}
- Acceptance criteria: ${acceptanceCriteria}
  _(leave blank to have product-owner define them first)_

## Task Plan

Execute tasks in this order (respect dependencies):

```
TASK-01 [product-owner]              Define/validate acceptance criteria   depends-on: none
TASK-02 [repo-requirements-analyst]  Analyze existing patterns in domain   depends-on: none
TASK-03 [database-architect]         Validate JPA/query requirements       depends-on: TASK-02
TASK-04 [developer]                  Implement feature + OpenAPI update     depends-on: TASK-01, TASK-02, TASK-03
TASK-05 [test-writer]                Write unit tests                       depends-on: TASK-04
TASK-06 [api-reviewer]               Review API contract                    depends-on: TASK-04
TASK-07 [code-reviewer]              Review code quality + PMD              depends-on: TASK-04
TASK-08 [security-reviewer]          Security review of new code            depends-on: TASK-04
TASK-09 [devops-engineer]            Verify build + Docker image            depends-on: TASK-05, TASK-06, TASK-07
```

## Context to Pass Each Agent

Pass to developer (TASK-04):
- Patterns found by repo-requirements-analyst (TASK-02)
- JPA constraints from database-architect (TASK-03)
- Acceptance criteria from product-owner (TASK-01)

## Acceptance Criteria Validation

After all tasks complete, verify each criterion from TASK-01 with evidence.

## Final Report

```markdown
## Orchestration Report
### Request Summary
### Acceptance Criteria — | # | Criterion | Status | Evidence |
### Agent Execution — | Task | Agent | Status | Key Output |
### Issues & Blockers — | Task | Agent | Issue | Action |
### Next Steps
### Overall: SUCCESS / PARTIAL / BLOCKED
```

