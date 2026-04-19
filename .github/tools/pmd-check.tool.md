---
name: PMD Check
description: Tool for running PMD static analysis and CPD copy-paste detection in backbone-rest
type: terminal
command-prefix: mvn
used-by: [Code Reviewer, Developer]
---

# PMD Check Tool

## Purpose

Run PMD 3.23.0 static analysis and CPD copy-paste detection on **backbone-rest**.
PMD violations **fail the build** — zero-tolerance policy enforced during `mvn test`.

> Set `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` before running.

## Commands

### Check (fail on violation)

```bash
# Run PMD + CPD check — same as what runs during mvn test
mvn pmd:check pmd:cpd-check

# PMD only
mvn pmd:check

# CPD only
mvn pmd:cpd-check
```

### Report Only (no failure)

```bash
# Generate PMD HTML report without failing
mvn pmd:pmd pmd:cpd

# View reports
open target/site/pmd.html
open target/site/cpd.html

# View raw XML
cat target/pmd.xml
```

### Combined with Full Build

```bash
# Full build — PMD runs automatically
mvn test
```

## Common Violations and Fixes

| Rule | Example Fix |
|------|------------|
| `AvoidDuplicateLiterals` | Extract repeated string to `private static final String` |
| `UnusedPrivateField` | Remove unused `@Mock` fields or unused private members |
| `EmptyCatchBlock` | Log or rethrow — never silently swallow exceptions |
| `GodClass` | Extract sub-services or utility helpers |
| CPD block | Extract to shared private method |

## Suppression (last resort)

```java
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
// Reason: JSON field name that must not be extracted to constant
```

## Key Files

- Ruleset: `ruleset.xml` (project root)
- PMD report: `target/site/pmd.html`
- CPD report: `target/site/cpd.html`
- Raw XML: `target/pmd.xml`

