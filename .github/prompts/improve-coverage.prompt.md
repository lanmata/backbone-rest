---
name: Improve Test Coverage
description: Identify and fill JaCoCo coverage gaps for a given class or module
mode: agent
agent: test-writer
tools: [run_in_terminal, read_file, insert_edit_into_file, replace_string_in_file, create_file, grep_search, file_search, get_errors]
---

Improve JaCoCo test coverage for the **backbone-rest** project.

## Target

- Class or package: ${target}
  _(leave blank to analyze all modules)_

## Steps

1. **Generate coverage report**: `mvn test jacoco:report`
2. **Identify uncovered branches**: open `target/site/jacoco/index.html` or read XML:
   ```bash
   grep -A3 "${target}" target/site/jacoco/jacoco.xml | grep -E "missed|covered"
   ```
3. **Read the target class** — identify every branch (`if`, `null check`, `Optional`, exception catch) not yet tested.
4. **Add tests** for each uncovered branch using the standard AAA pattern.
5. **Re-run**: `mvn -Dtest=${TestClass} test jacoco:report` — verify coverage increased.
6. **PMD check**: `mvn pmd:check` — zero violations.

## Coverage Targets (advisory)

| Layer | Target |
|-------|--------|
| `*ServiceImpl` | ≥ 80% line coverage |
| `*Controller` | ≥ 60% line coverage |
| `*Mapper` | ≥ 70% line coverage |
| Utility classes | ≥ 70% line coverage |

## Constraints

- Do NOT write trivial getter/setter tests just to inflate numbers.
- Each new test must assert a real behavioral branch.
- No duplicate string literals — extract to constants.
- Do NOT modify production code to make it more testable (extract only if genuinely needed).

## Output Format

```
Class: <fully qualified name>
Before: X% line / Y% branch
After:  X% line / Y% branch
New tests added: N (list method names)
PMD: PASS
```

