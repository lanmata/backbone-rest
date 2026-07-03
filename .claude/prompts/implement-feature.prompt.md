---
name: Implement Feature
description: Implement a new feature end-to-end in backbone-rest following interface-first, constructor injection, and PMD zero-violation conventions
mode: agent
agent: java-developer
tools: [Bash, Read, Edit, Write]
---

Implement the following feature in **backbone-rest** (Spring Boot 3.4.x / Java 21).

## Feature Request

- Domain: ${domain}
- Feature: ${feature}
- Acceptance criteria: ${acceptanceCriteria}

## Implementation Steps

Follow the canonical pattern from `src/main/java/com/umdc/backoffice/v1/users/`:

### Step 1 — Read existing patterns
Read `*Api.java`, `*Controller.java`, `*ServiceImpl.java` in the target domain to understand current state before making changes.

### Step 2 — Create / update `*Api.java`
- Add `@Operation`, `@ApiResponses`, `@Parameter` annotations.
- Add `default` method delegating to `getService()`.
- Keep `@RequestMapping` on the interface.

### Step 3 — Create / update `*Controller.java`
- `@RestController`, `@RequestMapping`, constructor injection only.
- `@Override` every API method — zero logic.

### Step 4 — Create / update `*ServiceImpl.java`
- Return `ResponseEntity<?>`.
- Include null guard → 400, not-found → 404 + `Warning` header.
- Log with `MessageUtil.LOG_START_MSG` / `LOG_END_MSG`.

### Step 5 — Update `api.yaml`
Update `src/main/resources/META-INF/api.yaml` to reflect any new or changed endpoints.

### Step 6 — Create / update `*MessageKey` enum (if new messages needed)
Add keys to `src/main/java/com/umdc/backoffice/constant/keys/`.

### Step 7 — Verify
```bash
mvn -DskipTests compile
mvn pmd:check
mvn test
```

## Constraints

- No `@Autowired` field injection.
- No logic in controllers.
- No `mvnw` — use `mvn`.
- No breaking changes to existing `/api/v1/*` paths.
- MapStruct `@Mapper` must include `config = MapperAppConfig.class`.
- Use `///` triple-slash JavaDoc style.

## Output

Report: changed files, test result summary, PMD result, and OpenAPI changes made.
