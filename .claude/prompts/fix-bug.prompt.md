---
name: Fix Bug
description: Diagnose and fix a bug in backbone-rest while maintaining PMD compliance, test coverage, and API backward compatibility
mode: agent
agent: java-developer
tools: [Bash, Read, Edit]
---

Fix the following bug in **backbone-rest**.

## Bug Report

- Domain: ${domain}
- Symptom: ${symptom}
- Steps to reproduce: ${stepsToReproduce}
- Expected behavior: ${expectedBehavior}

## Diagnosis Steps

### Step 1 — Locate the failing code
```bash
# Find related files
find src/main/java -path "*/${domain}*" -name "*.java" | sort

# Search for symptom-related method
grep -rn "${searchTerm}" src/main/java --include="*.java" -l
```

### Step 2 — Read the relevant service impl
Service impls contain the business logic and `ResponseEntity` decisions. Start there.

### Step 3 — Check the test for this behavior
```bash
find src/test/java -path "*/${domain}*" -name "*.java" | sort
```

## Fix Steps

1. Identify the root cause (null check missing, wrong status code, incorrect mapping, etc.).
2. Apply the minimal fix — do not refactor unrelated code.
3. Add or update a test that would have caught this bug.
4. Verify:
```bash
mvn -DskipTests compile
mvn pmd:check
mvn test
```

## Constraints

- Fix only the reported bug — no surrounding cleanup.
- Do not break existing tests.
- Do not change `/api/v1/*` response contract unless the bug IS the wrong contract.
- No `@Autowired` field injection introduced.
- Update `api.yaml` only if the fix changes a documented response code or field.

## Output

Report: root cause, files changed, test added/updated, `mvn test` result.
