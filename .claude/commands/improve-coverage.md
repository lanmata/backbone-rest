# Improve Coverage — backbone-rest

Identify classes with low JaCoCo line coverage and write missing tests to raise it.

## Usage
```
/improve-coverage
```
Optionally target a specific domain: `/improve-coverage users`

---

## Steps Claude Will Execute

### 1. Run Tests + JaCoCo
```bash
mvn test
# JaCoCo report: target/site/jacoco/index.html
# XML report: target/site/jacoco/jacoco.xml
```

### 2. Parse Coverage Report
```bash
# Extract missed lines per class from XML
grep -A3 "<sourcefile" target/site/jacoco/jacoco.xml | grep "LINE" | sort -t '"' -k4 -rn | head -20
```

Identify top uncovered classes (< 70% line coverage).

### 3. For Each Uncovered Class — Read and Analyze
- What methods exist?
- Which branches (if/else, Optional) are not covered?
- What mock setup is needed?

### 4. Write Missing Tests
Add to existing `*Test.java` or create a new one — following
`.claude/agents/test-writer.md` conventions exactly.

Focus on:
- Uncovered `if/else` branches.
- Empty `Optional` return paths (→ 404).
- Exception paths.
- Parameterized inputs that exercise different branches.

### 5. Verify Improvement
```bash
mvn test
# Re-check target/site/jacoco/jacoco.xml for the target classes
```

---

## Output
```
## Coverage Improvement Report

### Before
| Class | Line% | Branch% |
|-------|-------|---------|

### Tests Added
| Class | Test Method | Branch Covered |

### After
| Class | Line% | Branch% | Delta |

### Remaining Gaps (if any)
| Class | Uncovered Method | Reason |
```
