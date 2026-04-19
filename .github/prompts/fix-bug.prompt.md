---
name: Fix Bug
description: Diagnose and fix a bug in backbone-rest without breaking existing behavior
mode: agent
agent: developer
tools: [run_in_terminal, read_file, insert_edit_into_file, replace_string_in_file, grep_search, file_search, get_errors]
---

Diagnose and fix a bug in the **backbone-rest** project.

## Bug Report

- Affected component: ${component}
- Symptom: ${symptom}
- Reproduction steps: ${reproSteps}
- Expected behavior: ${expected}
- Actual behavior: ${actual}

## Steps

1. **Locate** the bug — read the affected service/controller/mapper.
2. **Identify root cause** — trace from the endpoint down through service → repository.
3. **Fix** — change only the minimum code needed:
   - Do NOT refactor unrelated code.
   - Preserve `///` doc comment style where present.
4. **Verify** the fix compiles: `mvn -DskipTests compile`
5. **Run existing tests**: `mvn -Dtest=${testClass} test` — all must pass.
6. **PMD check**: `mvn pmd:check` — zero violations.
7. **Report** — state the root cause, the fix applied, and which tests validate it.

## Output Format

```
Root cause: <1 sentence>
Fix: <file:line — what changed>
Tests validated: <test class + method names>
PMD: PASS / FAIL (with violation if any)
```

