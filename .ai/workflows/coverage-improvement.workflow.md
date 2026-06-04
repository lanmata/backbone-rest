---
name: Coverage Improvement
description: Workflow for improving test coverage
trigger: manual
agents:
  - QA / Test Writer
  - Developer
  - Project Manager
---

# Coverage Improvement Workflow

## Purpose

Systematically improves test coverage in the **backbone-rest** project.
JaCoCo has a minimum threshold of 0 (no hard gate), but meaningful coverage
on service and utility classes is essential for long-term maintainability.

## Workflow Steps

### Step 1: Coverage Analysis
**Agent**: QA / Test Writer
**Action**: Identify coverage gaps

```bash
# Generate current coverage report
mvn test jacoco:report
open target/site/jacoco/index.html
```

```
#runSubagent agentName="QA / Test Writer"
"Analyze the JaCoCo coverage report for backbone-rest and identify the top 5
classes/packages with the lowest coverage. Prioritize:
1. Service implementations (*ServiceImpl.java)
2. Controller-specific logic (*Controller.java non-delegation methods)
3. Utility classes (JwtUtil, KeystoreUtil, MessageUtil)
Report: class name, current line coverage %, uncovered scenarios."
```

---

### Step 2: Write Missing Tests
**Agent**: QA / Test Writer
**Action**: Create tests for uncovered code

```
#runSubagent agentName="QA / Test Writer"
"Write JUnit 5 unit tests for the following uncovered classes in backbone-rest:
[list from Step 1]
Use MockitoAnnotations.openMocks(this) in @BeforeEach.
Use JUnit 5 assertions (assertEquals, assertNotNull) — no AssertJ.
Follow test naming: testMethodName_Scenario (e.g., testCreate_BadRequest_NullRequest)."
```

---

### Step 3: Implementation Support (if needed)
**Agent**: Developer
**Condition**: If code is genuinely untestable and requires refactoring

```
#runSubagent agentName="Developer"
"The following backbone-rest code is difficult to test. Suggest minimal refactoring
to improve testability without changing public behavior or API contracts."
```

---

### Step 4: Verify Coverage Improvement
**Agent**: Project Manager

```bash
# Run tests and regenerate report
mvn test jacoco:report
open target/site/jacoco/index.html
```

## Coverage Focus Areas (Highest Impact)

| Class Type                     | Priority | Example                          |
|--------------------------------|----------|----------------------------------|
| `*ServiceImpl.java`            | High     | `UserServiceImpl`, `SessionServiceImpl` |
| `*Controller.java` (logic)     | Medium   | `UserController.putUserDetail`   |
| Utility classes                | Medium   | `JwtUtil`, `MessageUtil`         |
| `*Mapper.java`                 | Low      | Excluded from Sonar but testable |

## Exit Criteria

- [ ] Top coverage gaps identified from JaCoCo report
- [ ] New tests added following backbone-rest test conventions
- [ ] All new tests pass (`mvn test` green, PMD clean)
- [ ] Coverage improvement verified in `target/site/jacoco/index.html`
