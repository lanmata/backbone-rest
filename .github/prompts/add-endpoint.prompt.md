---
name: Add REST Endpoint
description: Implement a new REST endpoint following backbone-rest interface-first conventions
mode: agent
agent: developer
tools: [run_in_terminal, read_file, insert_edit_into_file, replace_string_in_file, create_file, grep_search, file_search, get_errors]
---

Implement a new REST endpoint for the **backbone-rest** project.

## Context

- Domain: ${domain}
- HTTP Method: ${method}
- Path: /api/v1/${domain}/${path}
- Request DTO: ${requestType}
- Response DTO: ${responseType}
- Description: ${description}

## Required Deliverables

1. **`*Api.java`** — add the endpoint with:
   - `@Operation(description = "...")` and `@ApiResponses` for all status codes
   - `@Parameter(description, required)` on every path/query param
   - Default method delegating to `getService()`

2. **`*Controller.java`** — override the default method, delegate to service only.

3. **`*Service.java`** — add the method signature to the interface.

4. **`*ServiceImpl.java`** — implement the business logic:
   - Null/blank input check → 400 with `Warning` header
   - Not-found → 404 with `Warning` header
   - Success → correct 2xx status
   - SLF4J logging with `MessageUtil.LOG_START_MSG` / `LOG_END_MSG`

5. **DTO** — add request/response records if not already defined.

6. **`backbone_rest-openapi.yaml`** — add the new path with all response codes.

## Constraints

- Constructor injection only — no `@Autowired` on fields.
- Services return `ResponseEntity<?>` — no raw domain objects.
- No hardcoded secrets or message strings — use `MessageUtil` / `*MessageKey`.
- All JPQL must be H2-compatible.
- Run `mvn -DskipTests compile` to confirm zero compile errors.
- Run `mvn pmd:check` to confirm zero PMD violations.

