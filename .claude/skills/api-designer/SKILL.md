---
name: API Designer Skills
description: Consolidated skill set for the API Designer agent — OpenAPI 3.1 spec, *Api.java annotation compliance, REST contract design for backbone-rest
applies-to:
  - api-designer
---

# API Designer — Skill Definition

## 1. OpenAPI Spec Location

Single source of truth: `src/main/resources/META-INF/api.yaml` (OpenAPI 3.1).

Every change to `*Api.java` annotations **must** be reflected in `api.yaml`.

---

## 2. `*Api.java` Annotation Pattern

```java
@Tag(name = "users", description = "The user API")
@RequestMapping(path = "/api/v1/users")
public interface UserApi {

    @Operation(summary = "Find user by ID", description = "Returns a single user by UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/{userId}")
    default ResponseEntity<UserTO> findUserById(
        @Parameter(description = "User UUID", required = true)
        @NotNull @PathVariable UUID userId
    ) {
        return getService().findUserById(userId);
    }
}
```

---

## 3. REST Contract Conventions

| Method | Status Success | Body | Notes |
|--------|---------------|------|-------|
| GET | 200 | Resource TO | 404 if not found |
| POST | 201 | Created resource | Location header optional |
| PUT | 200 or 202 | Updated resource | |
| PATCH | 200 or 202 | Partial update | |
| DELETE | 204 | Empty | |

### Path Naming
- Resource paths: plural nouns — `/api/v1/users`, `/api/v1/roles`
- Sub-resources: `/api/v1/users/{userId}/roles`
- Actions (non-CRUD): `/api/v1/sessions/token`, `/api/v1/sessions/validate`

---

## 4. Backward Compatibility Rules

Breaking changes (require stakeholder approval):
- Removing an endpoint
- Removing or renaming a required request field
- Changing a response field type
- Changing a URL path segment

Non-breaking:
- Adding optional request fields
- Adding new response fields
- Adding new endpoints

---

## 5. Key Files

| File | Purpose |
|------|---------|
| `src/main/resources/META-INF/api.yaml` | OpenAPI 3.1 spec |
| `src/main/java/com/umdc/backoffice/v1/*/api/controller/*Api.java` | Contract annotations |
| `src/main/java/com/umdc/backoffice/v1/*/api/to/*.java` | DTOs referenced in spec |

---

## 6. Review Checklist

- [ ] Every `*Api.java` method has `@Operation` with `summary` and `description`
- [ ] Every method has `@ApiResponses` covering success + 400 + 404 (minimum)
- [ ] Every `@PathVariable` and `@RequestParam` has `@Parameter(description, required)`
- [ ] `api.yaml` paths match `*Api.java` `@RequestMapping` + `@*Mapping` paths
- [ ] `api.yaml` schemas match DTO fields (name, type, required)
- [ ] No breaking changes without explicit approval
- [ ] Response content type is `application/json` for all JSON endpoints
