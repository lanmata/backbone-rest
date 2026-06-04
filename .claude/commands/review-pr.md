# Review PR — backbone-rest

Review all changes on the current branch against `develop` — build, PMD, conventions,
OpenAPI sync, and security basics.

## Usage
```
/review-pr
```

---

## Steps Claude Will Execute

### 1. Identify Changed Files
```bash
git diff develop...HEAD --name-only
git log develop..HEAD --oneline
```

### 2. Compile Gate
```bash
mvn -DskipTests compile
```
Any error → CRITICAL, stop review.

### 3. PMD Gate
```bash
mvn pmd:check pmd:cpd-check 2>&1
```
Any violation → CRITICAL.

### 4. Test Gate
```bash
mvn test 2>&1 | tail -30
```
Report pass/fail count + JaCoCo summary.

### 5. Convention Audit (for changed Java files)
Check each changed file against:
- Controller is thin (`@Override` + delegate only).
- No `@Autowired` field injection.
- Service returns `ResponseEntity<?>`.
- `MessageUtil` used for messages.
- Constructor injection.
- SLF4J logger present.
- No hardcoded strings that should be constants.

### 6. OpenAPI Sync Check
If any `*Api.java` changed:
```bash
grep -l "changed *Api.java files" # compare with api.yaml
```
Flag as MEDIUM if spec not updated.

### 7. Security Quick Check
```bash
grep -rn "CrossOrigin\|@Autowired\|println\|password.*=" src/main/java/ --include="*.java" | grep -v "test"
```

---

## Output
```markdown
## PR Review — <branch>

### Commits
<list from git log>

### Gate Results
| Gate | Status | Details |
|------|--------|---------|
| Compile | PASS/FAIL | |
| PMD | PASS/FAIL | N violations |
| Tests | PASS/FAIL | N passed / N failed |
| Coverage | % | JaCoCo summary |

### Convention Findings
| # | Severity | File | Issue |

### OpenAPI Sync
| *Api.java | YAML Updated | Status |

### Security Flags
| File | Line | Issue |

### Verdict: READY TO MERGE / CHANGES REQUIRED
```
