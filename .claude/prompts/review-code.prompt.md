---
name: Review Code
description: Review changed files in backbone-rest for PMD compliance, Spring Boot conventions, interface-first pattern, and security issues
mode: agent
agent: code-reviewer
tools: [Bash, Read]
---

Review the following changes in **backbone-rest**.

## Changed Files

${changedFiles}

_(If blank, review all files changed since branch diverged from `develop`:)_
```bash
git diff develop...HEAD --name-only | grep "\.java$"
```

## Review Dimensions

### 1. PMD Compliance
```bash
mvn pmd:check pmd:cpd-check
```
Report any violations found.

### 2. Interface-First Pattern
For each changed `*Controller.java`:
- [ ] Does it implement `*Api.java`?
- [ ] Is it thin (no business logic)?
- [ ] Constructor injection only (no `@Autowired`)?
- [ ] `@Override` on every API method?

### 3. Service Pattern
For each changed `*ServiceImpl.java`:
- [ ] Returns `ResponseEntity<?>`?
- [ ] Null checks with 400 + `Warning` header?
- [ ] Not-found with 404 + `Warning` header?
- [ ] SLF4J logging with `LOG_START_MSG` / `LOG_END_MSG`?

### 4. OpenAPI Sync
If `*Api.java` changed:
- [ ] Is `src/main/resources/META-INF/api.yaml` updated?

### 5. Security Basics
- [ ] No hardcoded secrets or passwords?
- [ ] No `System.out.println`?
- [ ] No plain-text credential logging?

### 6. Test Coverage
- [ ] New public methods have corresponding unit tests?

## Output Format

```markdown
## Code Review

| File | Line | Severity | Issue | Suggestion |
|------|------|----------|-------|-----------|

### PMD Result
- Violations: N

### OpenAPI Sync
- Status: IN_SYNC / OUT_OF_SYNC (list mismatches)

### Overall: APPROVED / CHANGES_REQUESTED
```
