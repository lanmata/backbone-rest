---
name: Repo Requirements Analyst Skills
description: Consolidated skill set for the Repo Requirements Analyst agent — codebase pattern discovery, gap analysis, and requirements mapping for backbone-rest
applies-to:
  - repo-requirements-analyst
---

# Repo Requirements Analyst — Skill Definition

## 1. Canonical Reference Pattern

When analyzing a domain, always start with the `users` domain as the canonical reference:

| Layer | Canonical File |
|-------|---------------|
| API interface | `src/main/java/com/umdc/backoffice/v1/users/api/controller/UserApi.java` |
| Controller | `src/main/java/com/umdc/backoffice/v1/users/api/controller/UserController.java` |
| Service interface | `src/main/java/com/umdc/backoffice/v1/users/service/UserService.java` |
| Service impl | `src/main/java/com/umdc/backoffice/v1/users/service/UserServiceImpl.java` |
| DTO | `src/main/java/com/umdc/backoffice/v1/users/api/to/` |
| Mapper | `src/main/java/com/umdc/backoffice/v1/users/mapper/UserMapper.java` |
| Message key | `src/main/java/com/umdc/backoffice/constant/keys/UserMessageKey.java` |

---

## 2. Analysis Scope

For each domain being analyzed:

1. **Identify existing files** — list all Java files in the domain package.
2. **Identify patterns** — how is the `*Api.java` / `*Controller` / `*ServiceImpl` structured? Note deviations.
3. **Identify JPA usage** — what entities and repositories are imported?
4. **Identify test coverage** — what test classes exist? What methods are tested?
5. **Identify OpenAPI state** — is the domain covered in `api.yaml`? Is it in sync?
6. **Identify gaps** — missing tests, undocumented endpoints, pattern deviations.

---

## 3. Key Grep Patterns

```bash
# Find all *Api.java interfaces
find src/main/java -name "*Api.java" | sort

# Find all controllers
find src/main/java -name "*Controller.java" | sort

# Find all service impls
find src/main/java -name "*ServiceImpl.java" | sort

# Find usage of a specific class
grep -r "UserService" src/main/java --include="*.java" -l

# Find all @RequestMapping paths
grep -r "@RequestMapping" src/main/java --include="*.java" -n

# Check for field injection (@Autowired violations)
grep -rn "@Autowired" src/main/java --include="*.java"
```

---

## 4. Output Format

```markdown
### Codebase Analysis — <domain or feature>

#### Files Found
| File | Purpose | Pattern Compliance |
|------|---------|-------------------|
| UserApi.java | API interface | ✅ Compliant |
| UserController.java | Controller | ✅ Compliant |

#### Pattern Deviations
| File | Deviation | Severity |
|------|-----------|---------|

#### JPA / Persistence
- Entities used: <list>
- Repositories used: <list>

#### Test Coverage
| Test File | Coverage | Missing |
|-----------|---------|---------|

#### OpenAPI State
- Domain in api.yaml: YES / NO / PARTIAL
- Spec in sync with *Api.java: YES / NO

#### Gaps
| Gap | File | Priority |
|-----|------|---------|
```

---

## 5. Constraints

- Read only — do not modify any files.
- Report file paths relative to repo root.
- Flag all `@Autowired` field injections as CRITICAL deviations.
- Flag any controller with business logic as HIGH deviation.
