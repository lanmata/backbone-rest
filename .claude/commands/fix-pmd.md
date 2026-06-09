# Fix PMD Violations — backbone-rest

Run PMD against the current codebase and fix every violation found in `ruleset.xml`.

## Usage
```
/fix-pmd
```
Optionally specify a file or package: `/fix-pmd src/main/java/com/umdc/backoffice/v1/users/`

---

## Steps Claude Will Execute

### 1. Run PMD
```bash
mvn pmd:check pmd:cpd-check 2>&1 | tee /tmp/pmd-output.txt
```

### 2. Parse Violations
Extract each violation: file path, line number, rule name, description.

### 3. Fix by Category

**UnusedImports** → remove the import line.

**MethodArgumentCouldBeFinal** → add `final` to method parameters.

**LocalVariableCouldBeFinal** → add `final` to local variable declarations.

**UnusedPrivateField** / **UnusedLocalVariable** → remove if genuinely unused;
if needed, rename with a comment explaining why it's retained.

**EmptyCatchBlock** → add `log.warn("...", e)` or re-throw as appropriate.

**SystemPrintln** → replace with `log.info(...)` or `log.debug(...)`.

**AvoidDuplicateLiterals** → extract to a constant.

**TooManyMethods** / **GodClass** → note as REFACTOR-REQUIRED; do not restructure
unless explicitly requested (risk of breaking changes).

**CyclomaticComplexity** → note as REFACTOR-REQUIRED.

**NullAssignment** → use `Optional` or guard clause instead.

### 4. Re-run to Confirm
```bash
mvn pmd:check pmd:cpd-check
```
Must return exit code 0 before reporting done.

### 5. Compile Check
```bash
mvn -DskipTests compile
```

---

## Output
```
## PMD Fix Report

### Before
| File | Line | Rule | Description |

### Changes Made
| File | Rule | Fix Applied |

### After
- PMD: PASS / FAIL (N remaining)
- Compile: PASS / FAIL

### Skipped (manual refactor required)
| File | Rule | Reason |
```
