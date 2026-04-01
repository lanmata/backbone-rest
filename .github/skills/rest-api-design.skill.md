---
name: REST API Design
description: Skill for designing and maintaining RESTful APIs with OpenAPI documentation
applies-to:
  - Developer
  - API Reviewer
  - Product Owner
---

# REST API Design Skill

## Scope

This skill covers REST API design principles, OpenAPI specification maintenance,
and API contract patterns used in the **backbone-rest** project.

## API Interface Pattern

API contracts are defined in `*Api.java` interfaces with Swagger annotations.
Controllers are thin implementations delegating to services.

```java
@Tag(name = "roles", description = "The Role API")
public interface RoleApi {

    default RoleService getService() { return new RoleService() {}; }

    @Operation(description = "Find a role by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role found."),
        @ApiResponse(responseCode = "404", description = "Role not found.")
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/{roleId}")
    default ResponseEntity<RoleTO> findById(@PathVariable UUID roleId) {
        return getService().findById(roleId);
    }
}
```

## Controller-Specific Cast Pattern

When a default method in `*Api` needs logic only the controller can provide,
cast `this` explicitly (see `UserApi.putUserDetail`):
```java
@PutMapping(path = "/{userId}")
default ResponseEntity<Void> putUserDetail(@PathVariable UUID userId,
                                           @RequestBody PutUserUpdateRequest request) {
    return ((UserController) this).putUserDetail(userId, request);
}
```

## HTTP Status Code Conventions

| Status | Usage in backbone-rest                                     |
|--------|------------------------------------------------------------|
| 200    | Successful read/query                                      |
| 201    | Resource created (`userService.create`)                    |
| 202    | Accepted (partial update via `putUserDetail`)              |
| 204    | No content (delete)                                        |
| 400    | Validation failure / null inputs (with `Warning` header)   |
| 401    | Unauthorized session                                       |
| 404    | Resource not found                                         |
| 406    | Not acceptable (invalid request payload)                   |
| 409    | Conflict (alias/email already exists — `validateAlias`)    |
| 422    | Unprocessable entity (exception during update)             |
| 500    | Unexpected failure                                         |

## Controller Annotations Checklist

```java
@RestController
@RequestMapping("/api/v1/<domain>")
@CrossOrigin(origins = "*")
public class XxxController implements XxxApi { ... }
```

## OpenAPI Spec Maintenance

When modifying API contracts, update **both**:
1. `*Api.java` interface annotations (`@Operation`, `@ApiResponses`, `@Parameter`)
2. `src/main/resources/META-INF/backbone_rest-openapi.yaml`

Verify consistency using the **API Reviewer** subagent.

## Response Header Convention

Services may add warning messages via `HttpHeaders.WARNING`:
```java
return ResponseEntity.badRequest()
    .header(HttpHeaders.WARNING, "User ID empty or null")
    .build();
```
