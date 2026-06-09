---
name: Code Review
description: Workflow for comprehensive code review process
trigger: pull-request
agents:
  - Code Reviewer
  - API Reviewer
  - Security Reviewer
---

# Code Review Workflow

## Purpose

Orchestrates a comprehensive code review process for pull requests in the
**backbone-rest** repository.

## Workflow Steps

### Step 1: Automated Quality Check
**Prerequisite**: Build passes locally before requesting review

```bash
# Verify locally — must pass before opening a PR
mvn test
```

---

### Step 2: Code Quality Review
**Agent**: Code Reviewer
**Action**: Review for code quality and conventions

```
#runSubagent agentName="Code Reviewer"
"Review the PR changes in backbone-rest for:
- Interface-first controller pattern (*Api.java annotations + *Controller thin delegation)
- Service returns ResponseEntity directly (no raw domain objects)
- MessageUtil used for user-facing messages (no new string literals for messages)
- Constructor injection only (no @Autowired fields)
- @Transactional on multi-step service methods
- PMD ruleset.xml compliance (no unused vars, no empty catch blocks, no duplicate literals)
- /// triple-slash JavaDoc style preserved in touched files
- @CrossOrigin(origins='*') present on controllers"
```

---

### Step 3: API Contract Review (if API changes)
**Agent**: API Reviewer
**Condition**: Changes include `*Api.java` or `backbone_rest-openapi.yaml`

```
#runSubagent agentName="API Reviewer"
"Review API contract changes in backbone-rest for consistency between
*Api.java annotations and backbone_rest-openapi.yaml.
Check backward compatibility for all /api/v1/* endpoints."
```

---

### Step 4: Security Review (if security-sensitive changes)
**Agent**: Security Reviewer
**Condition**: Changes include `security/`, auth logic, JWT handling, new dependencies, or `bootstrap.yml`

```
#runSubagent agentName="Security Reviewer"
"Review security-sensitive changes in backbone-rest for vulnerabilities.
Check: JWT handling (both Keycloak and session JWT), no hardcoded secrets,
input validation, and new dependency CVEs."
```

---

### Step 5: Merge Decision

**Approval criteria**:
- Code review: APPROVED
- API review (if applicable): PASSED
- Security review (if applicable): PASSED
- `mvn test` green (PMD clean, all tests pass)

## Exit Criteria

- [ ] All applicable reviews completed
- [ ] No unresolved critical/blocking comments
- [ ] `mvn test` passes
- [ ] No PMD violations introduced
