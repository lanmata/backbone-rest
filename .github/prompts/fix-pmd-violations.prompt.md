---
name: Fix PMD Violations
description: Resolve all PMD and CPD violations in a given class or module
mode: agent
agent: developer
tools: [run_in_terminal, read_file, replace_string_in_file, grep_search, file_search, get_errors]
---

Fix all PMD static analysis violations in the **backbone-rest** project.

## Scope

- Target: ${targetFile}
  _(leave blank to fix all violations in the current module)_

## Steps

1. **Generate the PMD report**: `mvn pmd:pmd pmd:cpd`
2. **Read violations**: `cat target/pmd.xml | grep -A5 "<violation"`
3. **Fix each violation** in priority order:

   | Rule | Fix |
   |------|-----|
   | `AvoidDuplicateLiterals` | Extract to `private static final String MSG_XXX = "..."` |
   | `UnusedPrivateField` | Remove the unused field (check all usages first) |
   | `EmptyCatchBlock` | Add `LOGGER.warn(...)` or rethrow |
   | `GodClass` | Extract responsibility to a helper or sub-service |
   | CPD block | Extract duplicated block to a private method |

4. **Re-run check**: `mvn pmd:check pmd:cpd-check` — must exit 0.
5. **Confirm tests still pass**: `mvn test`

## Constraints

- Use `@SuppressWarnings("PMD.RuleName")` only as a last resort with a comment explaining why.
- Never suppress `AvoidDuplicateLiterals` for user-facing message strings — extract them.
- Do not change test logic when fixing test-file violations — only fix the PMD issue.

## Output Format

```
Violations fixed: N
Files changed: [list]
Remaining violations: 0 (or list if suppressed with reason)
mvn pmd:check: PASS
```

