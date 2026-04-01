---
name: API Contract Review
description: Skill for reviewing and validating OpenAPI contracts
applies-to:
  - API Reviewer
  - Product Owner
  - Developer
---

# API Contract Review Skill

## Scope

This skill covers the process of reviewing and validating OpenAPI contracts
for the **backbone-rest** project.

## Contract Locations

1. **Java API Interfaces**: `src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java`
   - Swagger/OpenAPI annotations: `@Operation`, `@ApiResponses`, `@Tag`, `@Parameter`
   - Default method implementations delegating to service layer

2. **OpenAPI Spec**: `src/main/resources/META-INF/backbone_rest-openapi.yaml`
   - Full API specification in OpenAPI 3.1 format
   - Must be kept in sync with Java annotations

## Review Checklist

### Consistency
- Every `*Api.java` endpoint has a matching path in `backbone_rest-openapi.yaml`
- HTTP methods match between code and spec
- Request/response schemas match DTO class/record definitions
- Status codes in `@ApiResponses` match spec responses

### Backward Compatibility
- No removal of existing fields from response objects
- No changes to existing endpoint paths or HTTP methods
- Path parameter types consistent (`UUID` vs `String`)

### Annotation Quality
- `@Operation(description = "...")` present on all endpoints
- `@Parameter(description = "...")` on all path/query params
- `@Tag(name = "...", description = "...")` at interface level
- Error responses documented in `@ApiResponses`

### Controller Conventions
- `@RestController` + `@RequestMapping("/api/v1/<domain>")` + `@CrossOrigin(origins = "*")` on every controller
- Constructor injection only; `@Override` on every API method
- Cast-to-controller pattern (`(UserController)this`) only when controller-specific logic is needed

### Security
- Session endpoints (`/api/v1/session`) documented as not requiring Keycloak auth
- No sensitive data exposed in example payloads
