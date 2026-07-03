---
name: Improve Coverage
description: Identify JaCoCo coverage gaps in backbone-rest and write missing tests to meet thresholds
mode: agent
agent: test-writer
tools: [Bash, Read, Write]
---

Improve JaCoCo test coverage in **backbone-rest** to meet the thresholds defined in `pom.xml`.

## Step 1 — Run coverage and identify gaps

```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

Parse the JaCoCo report to identify classes/methods with coverage below threshold.

## Step 2 — Prioritize gaps

Focus on:
1. Service implementations (`*ServiceImpl.java`) — highest business value
2. Controllers (`*Controller.java`) — API contract validation
3. Utility classes (`MessageUtil`, `JwtUtil`, etc.)
4. Skip generated MapStruct implementations and Spring config classes

## Step 3 — Read target class and existing tests

For each uncovered class:
1. Read the source file.
2. Check for existing test:
```bash
find src/test/java -name "*<ClassName>*Test.java"
```

## Step 4 — Write missing tests

Following the patterns from `.claude/prompts/write-unit-tests.prompt.md`:
- Add missing cases to existing test classes where possible.
- Create new test class only if none exists.
- Cover: success, null input, not found, conflict cases per method.

## Step 5 — Verify coverage improvement

```bash
mvn test jacoco:report
```

Check that coverage metrics improved and thresholds are now met.

## Constraints

- Do not write trivial tests (getter/setter coverage) — focus on meaningful logic coverage.
- Do not modify production code to make it easier to test.
- PMD must still pass after adding tests.
- `SecurityKeycloakTestConfig` must be imported in all `@WebMvcTest` tests.

## Output

Table: `| Class | Before % | After % | Tests Added |` + final `mvn test` result.
