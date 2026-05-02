---
name: Review Pull Request
description: Full code review — conventions, PMD, security, and quality for a PR or changed files
mode: agent
agent: code-reviewer
tools: [run_in_terminal, read_file, grep_search, file_search, get_errors]
---

Perform a code review for the **backbone-rest** project.

## Scope

- Changed files: ${changedFiles}
  _(list file paths, or leave blank to review all staged/uncommitted changes)_

## Review Checklist

Execute each check and report PASS / FAIL / WARN:

```
[ ] PMD: mvn pmd:check pmd:cpd-check → exits 0
[ ] Compile: mvn -DskipTests compile → exits 0
[ ] Constructor injection only — no @Autowired on fields
[ ] Services return ResponseEntity<?> — no raw domain objects returned
[ ] Null/blank input guards present in all service methods
[ ] Warning header on all 400/404 responses
[ ] SLF4J logging with MessageUtil.LOG_START_MSG / LOG_END_MSG
[ ] No hardcoded secrets or magic string literals (use constants or MessageUtil)
[ ] Records used for immutable DTOs
[ ] @Valid on @RequestBody parameters in Api interface methods
[ ] @Transactional on service methods that modify data
[ ] No commented-out production code
[ ] Test exists for new/changed behavior
[ ] Documentation style preserved (/// where present)
[ ] No stack traces in API responses
[ ] OpenAPI spec updated if API contract changed
```

## Output Format

```markdown
## Code Review Report

### Approval: APPROVED / CHANGES_REQUESTED / NEEDS_DISCUSSION

### Critical Issues (block merge)
| File:Line | Rule | Issue | Fix |
|-----------|------|-------|-----|

### Suggestions (non-blocking)
| File:Line | Suggestion |
|-----------|-----------|

### Positive Notes
- ...

### PMD: PASS / FAIL
### Compile: PASS / FAIL
```

