---
name: Fix PMD Violations
description: Run PMD on backbone-rest and fix all violations to reach zero violations per ruleset.xml
mode: agent
agent: java-developer
tools: [Bash, Read, Edit]
---

Fix all PMD violations in **backbone-rest** to reach zero violations.

## Step 1 — Run PMD and capture violations

```bash
mvn pmd:check pmd:cpd-check 2>&1 | tee /tmp/pmd-output.txt
cat target/pmd.xml
```

## Step 2 — Parse violations

For each violation, note:
- File path
- Line number
- Rule name
- Description

## Step 3 — Fix violations

Common fix patterns:

| Rule | Fix |
|------|-----|
| `UnusedImports` | Remove unused import statement |
| `EmptyCatchBlock` | Add log statement or re-throw |
| `SystemPrintln` | Replace with `log.info()` / `log.error()` |
| `UnusedLocalVariable` | Remove unused variable |
| `AvoidDuplicateLiterals` | Extract to a constant |
| `NullAssignment` | Use `Optional` or guard clause |
| `UseUtilityClass` | Add `private` constructor |

## Step 4 — Verify zero violations

```bash
mvn pmd:check pmd:cpd-check
```

Output must be: `BUILD SUCCESS` with no PMD violations listed.

## Step 5 — Verify tests still pass

```bash
mvn test
```

## Constraints

- Fix only PMD violations — do not refactor non-violating code.
- Do not suppress violations with `@SuppressWarnings("PMD")` unless there is a documented reason.
- Do not change behavior — PMD fixes are structural only.
- Keep `///` triple-slash JavaDoc style where present.

## Output

Table: `| File | Line | Rule | Fix Applied |` for each violation fixed. Final PMD result.
