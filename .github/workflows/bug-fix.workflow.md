---
name: Bug Fix
description: Workflow for diagnosing and fixing bugs
trigger: manual
agents:
  - Developer
  - QA / Test Writer
  - Code Reviewer
---

# Bug Fix Workflow

## Purpose

Orchestrates the process of diagnosing, fixing, and verifying bugs in the
**backbone-rest** microservice.

## Workflow Steps

### Step 1: Diagnosis
**Agent**: Developer
**Action**: Analyze the bug and identify root cause

```
#runSubagent agentName="Developer"
"Diagnose the following bug in backbone-rest:
[bug description]
Identify the root cause, affected files under src/main/java/com/prx/backoffice/,
and propose a minimal fix. Preserve existing behavior for all other endpoints."
```

**Output**: Root cause analysis, proposed fix

---

### Step 2: Write Regression Test
**Agent**: QA / Test Writer
**Action**: Write a test that reproduces the bug

```
#runSubagent agentName="QA / Test Writer"
"Write a regression test that reproduces this bug:
[bug description]
Use MockitoAnnotations.openMocks(this) in @BeforeEach.
The test should FAIL before the fix and PASS after."
```

**Output**: Regression test (initially failing)

---

### Step 3: Implement Fix
**Agent**: Developer
**Action**: Apply the minimal fix

```
#runSubagent agentName="Developer"
"Apply the fix for the diagnosed bug. Keep the change minimal and atomic.
Ensure the regression test now passes and no existing tests are broken."
```

**Output**: Bug fix code

---

### Step 4: Code Review
**Agent**: Code Reviewer
**Action**: Review the fix

```
#runSubagent agentName="Code Reviewer"
"Review the bug fix for correctness, side effects, and convention compliance.
Verify PMD ruleset is not violated."
```

**Output**: Review approval

---

### Step 5: Verification

```bash
# Must pass before merging
mvn test
```

## Exit Criteria

- [ ] Root cause identified and documented
- [ ] Regression test added
- [ ] Fix applied and verified
- [ ] No side effects (all existing tests pass)
- [ ] PMD clean
- [ ] Code review approved
