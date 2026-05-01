---
name: Code Reviewer Skills
description: Consolidated skill set for the Code Reviewer agent — Java code quality, Spring Boot conventions, PMD, and clean code
applies-to:
  - Code Reviewer
---

# Code Reviewer — Skill Definition

## 1. Project Convention Compliance

- `*Api.java` holds Swagger annotations + default methods; controllers are thin.
- Services return `ResponseEntity<?>` directly — not raw domain objects.
- DTOs live in `com.prx.backoffice.v1.<domain>.api.to` (records or classes).
- User-facing messages use `MessageUtil` (keys from `*MessageKey` enums) — avoid new string literals.
- MapStruct mappers declare `config = MapperAppConfig.class` (from `com.umdc.commons.services`).
- Logging uses SLF4J `LoggerFactory.getLogger` with `MessageUtil.LOG_START_MSG` / `LOG_END_MSG`.
- Docs use `///` triple-slash JavaDoc style in many files — preserve existing style.

---

## 2. Java 21 Best Practices

```java
// ✅ Records for DTOs
public record UserTO(UUID id, String alias, String email) {}

// ✅ var for obvious types
var entity = userRepository.findById(userId).orElseThrow();

// ✅ Objects.isNull / Objects.nonNull (existing pattern)
if (Objects.isNull(request)) {
    return ResponseEntity.badRequest().build();
}

// ✅ Optional returned from repository — don't call .get() without check
return userRepository.findById(userId)
    .map(e -> ResponseEntity.ok(userMapper.toTarget(e)))
    .orElseGet(() -> ResponseEntity.notFound().build());

// ❌ Optional as method parameter
public ResponseEntity<?> find(Optional<UUID> id) { ... }
```

---

## 3. Spring Boot Best Practices

```java
// ✅ Constructor injection — never @Autowired on fields
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ✅ @Transactional on service methods that modify data
@Transactional
public ResponseEntity<Void> delete(UUID id) { ... }

// ✅ @Valid on request bodies
ResponseEntity<UserCreateResponse> create(@Valid @RequestBody UserCreateRequest request);

// ✅ bootstrap.yml is primary config — never application.yml
// ✅ All secrets use ${ENV_VAR} — never hardcoded
```

---

## 4. PMD Analysis

PMD 3.23.0 runs during `mvn test`. **Any violation fails the build.**

### Common Violations

```java
// ❌ AvoidDuplicateLiterals — fix with constant
private static final String MSG_NOT_FOUND = "User not found.";

// ❌ EmptyCatchBlock — always log or rethrow
try { ... } catch (Exception ex) {
    LOGGER.warn("Failed: {}", ex.getMessage());
}

// ❌ UnusedPrivateField / UnusedPrivateMethod — remove unused code
```

### PMD Commands

```bash
mvn pmd:check pmd:cpd-check   # check only
mvn pmd:pmd pmd:cpd           # generate report: target/site/pmd.html
```

---

## 5. Clean Code Principles

- Each class has one responsibility: `*Api` = mappings, `*Controller` = delegation, `*ServiceImpl` = logic.
- Methods under 20 lines; classes under 300 lines.
- No commented-out production code.
- No magic string literals — extract to constants.
- Only change what is requested — preserve surrounding code.

### Documentation Style

```java
/// Finds a user by their unique identifier.
///
/// @param userId the UUID of the user to find
/// @return 200 OK with the user DTO, or 404 Not Found
```

---

## 6. Security Checks

- No hardcoded secrets — all sensitive values must reference `${ENV_VAR}`.
- Input validation on all public endpoints via Jakarta annotations.
- No stack traces in API responses.
- Session token only in `session-token` header (`SessionJwtService.SESSION_TOKEN_KEY`).

---

## 7. Review Output Format

```
1. Approval Status: APPROVED / CHANGES_REQUESTED / NEEDS_DISCUSSION
2. Critical Issues — Must fix before merge
3. Suggestions — Recommended improvements
4. Positive Notes — Well-done aspects
5. Files Reviewed — List with per-file comments
```

---

## Code Review Checklist

```markdown
- [ ] No PMD violations (mvn pmd:check exits 0)
- [ ] No unused private fields or methods
- [ ] No duplicate string literals — constants used
- [ ] No empty catch blocks
- [ ] Constructor injection only — no @Autowired on fields
- [ ] Records used for immutable DTOs
- [ ] var used where type is obvious
- [ ] @Transactional on multi-step service writes
- [ ] @Valid on @RequestBody parameters
- [ ] No secrets hardcoded — ${ENV_VAR} used
- [ ] No stack traces in API responses
- [ ] Documentation style preserved (/// where existing)
```

