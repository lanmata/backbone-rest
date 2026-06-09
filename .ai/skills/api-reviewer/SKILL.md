---
name: API Reviewer Skills
description: Consolidated skill set for the API Reviewer agent — OpenAPI spec validation, REST contract review, and annotation compliance
applies-to:
  - API Reviewer
---

# API Reviewer — Skill Definition

## 1. OpenAPI Specification

### Artifact Location
```
src/main/resources/META-INF/backbone_rest-openapi.yaml
```
Every endpoint change **must** be reflected in this file.

### Required Swagger Annotations on `*Api.java`

```java
@Tag(name = "users", description = "User management API")
public interface UserApi {

    @Operation(description = "Find a user by their unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found."),
        @ApiResponse(responseCode = "400", description = "Bad request — userId is null."),
        @ApiResponse(responseCode = "404", description = "User not found.")
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

| Annotation | Requirement |
|-----------|-------------|
| `@Tag` | Once per interface — name + description |
| `@Operation(description)` | Every endpoint method |
| `@ApiResponses` | Every endpoint — all documented status codes |
| `@Parameter(description, required)` | Every path/query parameter |

### OpenAPI YAML Structure

```yaml
openapi: 3.1.0
paths:
  /api/v1/users/{userId}:
    get:
      tags: [users]
      operationId: findUserById
      parameters:
        - name: userId
          in: path
          required: true
          schema: { type: string, format: uuid }
      responses:
        '200':
          description: User found.
          content:
            application/json:
              schema: { $ref: '#/components/schemas/UserTO' }
        '404':
          description: User not found.
```

---

## 2. REST API Contract Review

### HTTP Status Code Conventions

| Status | Usage |
|--------|-------|
| 200 | Successful read/query |
| 201 | Resource created |
| 202 | Accepted (partial update) |
| 204 | Delete — no content |
| 400 | Validation failure / null inputs (+ `Warning` header) |
| 401 | Unauthorized session |
| 404 | Resource not found |
| 406 | Not acceptable (invalid payload) |
| 409 | Conflict (duplicate alias/email) |
| 422 | Unprocessable entity (exception during update) |
| 500 | Unexpected failure |

### Controller Annotations Checklist

```java
@RestController
@RequestMapping("/api/v1/<domain>")
@CrossOrigin(origins = "*")
public class XxxController implements XxxApi { ... }
```

### Controller-Specific Cast Pattern

When a `*Api` default method needs controller-specific logic:

```java
@PutMapping(path = "/{userId}")
default ResponseEntity<Void> putUserDetail(@PathVariable UUID userId,
                                           @RequestBody PutUserUpdateRequest request) {
    return ((UserController) this).putUserDetail(userId, request);
}
```

### Warning Header Convention

```java
return ResponseEntity.badRequest()
    .header(HttpHeaders.WARNING, "User ID empty or null")
    .build();
```

---

## 3. API Contract Review Checklist

```markdown
- [ ] All endpoints in *Api.java have matching paths in backbone_rest-openapi.yaml
- [ ] HTTP methods match between code and spec
- [ ] Request/response schemas match DTO records/classes
- [ ] All status codes documented in @ApiResponses
- [ ] No breaking changes to existing /api/v1/* endpoints
- [ ] Path parameter types consistent (UUID vs String)
- [ ] Content-Type headers correct (application/json)
- [ ] @Tag present on each *Api interface
- [ ] Controller annotated with @RequestMapping("/api/v1/<domain>")
- [ ] @CrossOrigin(origins = "*") present on controller
- [ ] Default method pattern used in *Api for delegation
```

---

## 4. Backward Compatibility Rules

- **Never remove** existing fields from response schemas.
- **Never change** HTTP method or path of existing endpoints.
- **Never rename** path parameters — breaks client codegen.
- Additive changes only: new optional fields, new endpoints.

---

## 5. Output Format

```markdown
## API Review Report

### ✅ Passed Checks
- [list of passing validations]

### ⚠️ Warnings
- [non-critical issues, suggestions]

### ❌ Failed Checks
- [critical issues that must be fixed before merge]

### 📋 Recommendations
- [improvement suggestions for future iterations]
```

---

## Key Files to Review

| File Pattern | Purpose |
|---|---|
| `src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java` | API interface definitions |
| `src/main/java/com/prx/backoffice/v1/*/api/controller/*Controller.java` | Controller implementations |
| `src/main/java/com/prx/backoffice/v1/*/api/to/*.java` | DTO classes/records |
| `../../../src/main/resources/META-INF/api.yaml` | OpenAPI specification |
| `src/main/java/com/prx/backoffice/constant/keys/*MessageKey.java` | Status/message key constants |

