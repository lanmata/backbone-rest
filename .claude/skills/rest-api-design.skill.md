---
name: REST API Design
description: Shared — REST contract conventions, HTTP status codes, OpenAPI 3.1 patterns (java-developer, api-designer, product-owner)
applies-to:
  - java-developer
  - api-designer
  - product-owner
---

# Shared Skill — REST API Design

## HTTP Method + Status Conventions

| Method | Success Status | Body | Notes |
|--------|---------------|------|-------|
| GET | 200 | Resource TO | 404 + Warning if not found |
| POST | 201 | Created resource | 409 on duplicate |
| PUT | 200 / 202 | Updated resource | |
| PATCH | 200 / 202 | Partial update | |
| DELETE | 204 | Empty | |

## Path Conventions

- Base prefix: `/api/v1/<resource>` (plural noun)
- Sub-resources: `/api/v1/users/{userId}/roles`
- Actions: `/api/v1/sessions/token`, `/api/v1/sessions/validate`
- Path variables: camelCase UUID — `{userId}`, `{roleId}`

## `Warning` Header on Errors

Always include `HttpHeaders.WARNING` on 4xx responses:
```java
return ResponseEntity.notFound()
    .header(HttpHeaders.WARNING, "User not found").build();
```

## OpenAPI 3.1 Annotation Minimum

Every `*Api.java` method must have:
1. `@Operation(summary = "...", description = "...")`
2. `@ApiResponses` with at least 200/201, 400, 404 responses
3. `@Parameter(description, required)` for all path/query params
4. `@Content(schema = @Schema(implementation = XxxTO.class))` on success response

## Backward Compatibility

Breaking = requires stakeholder approval:
- Remove endpoint or field
- Change field type
- Change URL path

Non-breaking = can merge freely:
- Add optional field
- Add new endpoint
- Add new 4xx/5xx response code

## Auth Requirements

- All `/api/v1/**` except `/v1/sessions/token` + `/v1/sessions/validate` require `Authorization: Bearer <JWT>`.
- Session endpoints use `session-token` header instead.
