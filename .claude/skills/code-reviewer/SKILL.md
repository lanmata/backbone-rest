---
name: Code Reviewer Skills
description: Consolidated skill set for the Code Reviewer agent — PMD ruleset.xml, Spring Boot 3.4.x conventions, interface-first pattern, constructor injection
applies-to:
  - code-reviewer
---

# Code Reviewer — Skill Definition

## 1. Project-Specific Patterns

### PMD Ruleset (`ruleset.xml`)
PMD is enforced at the Maven test phase via `maven-pmd-plugin`. Zero violations are required. Common rules:
- No unused imports or variables
- No empty catch blocks
- `equals`/`hashCode` consistency
- Proper logging (no `System.out.println`)
- No `NullPointerException`-prone null dereferences

Run check: `mvn pmd:check pmd:cpd-check`

### Interface-First Convention
Every domain controller must:
1. Have a `*Api.java` interface with all `@RequestMapping`, `@Operation`, `@ApiResponses` annotations.
2. Have a `*Controller.java` that `implements *Api`, has only a constructor, `getService()`, and `@Override` methods.
3. Never have business logic in the controller.

### Service Convention
- `*ServiceImpl.java` returns `ResponseEntity<?>` directly.
- HTTP status decisions live here, not in controllers or APIs.
- Always use constructor injection.

---

## 2. Review Dimensions

| Dimension | What to Check |
|-----------|--------------|
| PMD compliance | Run `mvn pmd:check` — report all violations |
| Constructor injection | No `@Autowired` on fields |
| Interface-first | `*Api.java` carries annotations; controller is thin |
| ResponseEntity | Services return `ResponseEntity<?>` |
| MapStruct | `config = MapperAppConfig.class` present on all `@Mapper` |
| OpenAPI sync | `*Api.java` changes reflected in `api.yaml` |
| Logging | SLF4J via `LoggerFactory.getLogger`; `LOG_START_MSG`/`LOG_END_MSG` pattern |
| Security | No secrets in code; no field `@Autowired`; no plain text passwords |
| Test coverage | New code has corresponding unit tests |

---

## 3. Severity Levels

| Level | Meaning | Action |
|-------|---------|--------|
| CRITICAL | Breaks compilation, security hole, or data loss risk | Block PR |
| HIGH | PMD violation, missing test, broken convention | Request changes |
| MEDIUM | Style inconsistency, missing log, suboptimal pattern | Comment |
| LOW | Minor naming, formatting | Informational |

---

## 4. Key Files

| File | Purpose |
|------|---------|
| `ruleset.xml` | PMD rules — check this before reviewing |
| `src/main/resources/META-INF/api.yaml` | OpenAPI spec — must stay in sync |
| `pom.xml` — JaCoCo config | Coverage thresholds |

---

## 5. Constraints

- Do NOT auto-fix code — report findings only (unless invoked via `/fix-pmd`).
- Do NOT approve changes that introduce `@Autowired` field injection.
- Do NOT approve changes that break `/api/v1/*` contract backward compatibility.

---

## 6. Review Output Format

```markdown
## Code Review — <PR / changeset title>

| File | Line | Severity | Issue | Suggestion |
|------|------|----------|-------|-----------|
| UserServiceImpl.java | 45 | HIGH | Missing null check for userId | Add `Objects.isNull(userId)` guard |

### PMD Result
- Violations: N (run: `mvn pmd:check`)

### Overall: APPROVED / CHANGES_REQUESTED
```
