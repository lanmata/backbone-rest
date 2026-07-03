---
name: PMD Check
description: Tool for running PMD static analysis in backbone-rest
type: terminal
command-prefix: mvn pmd:check
used-by:
  - java-developer
  - code-reviewer
  - devops-engineer
---

# PMD Check Tool

## Purpose

Run PMD static analysis and CPD (copy-paste detection) against backbone-rest source code. Zero violations are enforced — the build fails if any are found.

## Available Commands

```bash
# PMD + CPD check (required before every commit)
mvn pmd:check pmd:cpd-check

# PMD only (no CPD)
mvn pmd:check

# Generate PMD report without failing build
mvn pmd:pmd

# View XML report
cat target/pmd.xml

# View CPD report
cat target/cpd.xml
```

## Ruleset

File: `ruleset.xml` at repository root.

Key rules enforced:
- No unused imports
- No empty catch blocks
- No `System.out.println`
- Proper null handling
- Consistent `equals`/`hashCode`
- No unused variables or fields

## Output Locations

| Report | Location |
|--------|----------|
| PMD XML | `target/pmd.xml` |
| CPD XML | `target/cpd.xml` |
| PMD site | `target/site/pmd.html` |

## Notes

- PMD runs automatically during `mvn test` — separate `pmd:check` is for quick checks.
- The build exits non-zero on any violation — fix all before committing.
- Use `/fix-pmd` slash command to auto-fix violations.
