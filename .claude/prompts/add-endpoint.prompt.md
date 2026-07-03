---
name: Add Endpoint
description: Scaffold a complete new REST endpoint in backbone-rest end-to-end — *Api.java, *Controller.java, *ServiceImpl.java, DTO, mapper, message key, and api.yaml update
mode: agent
agent: java-developer
tools: [Bash, Read, Write, Edit]
---

Add a new REST endpoint to **backbone-rest**.

## Endpoint Specification

- Domain: ${domain}
- HTTP Method: ${method}
- Path: ${path}
- Description: ${description}
- Request body: ${requestBody}
- Response body: ${responseBody}
- Auth required: ${authRequired}

## Scaffolding Steps

### Step 1 — Read existing domain files
```bash
find src/main/java -path "*/${domain}*" -name "*.java" | sort
```

Read the existing `*Api.java` and `*Controller.java` to understand current patterns in this domain.

### Step 2 — Create / update DTO (if new request/response shape needed)
Location: `src/main/java/com/umdc/backoffice/v1/${domain}/api/to/`

```java
public record ${RequestDto}(
    @NotBlank String field1,
    @NotNull UUID field2
) {}
```

### Step 3 — Update `*Api.java`
Add the new method with full annotations:
```java
@Operation(summary = "${description}", description = "...")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", ...),
    @ApiResponse(responseCode = "400", ...),
    @ApiResponse(responseCode = "404", ...)
})
@${Method}Mapping(produces = APPLICATION_JSON_VALUE, path = "${subPath}")
default ResponseEntity<${ResponseDto}> ${methodName}(...) {
    return getService().${methodName}(...);
}
```

### Step 4 — Update `*Controller.java`
Add `@Override` method — zero logic, just delegate to service.

### Step 5 — Add to `*Service.java` interface
Add method signature.

### Step 6 — Implement in `*ServiceImpl.java`
Implement with null guard (400), not-found guard (404 + Warning), and success response.

### Step 7 — Update `api.yaml`
Add the new path and operation to `src/main/resources/META-INF/api.yaml`.

### Step 8 — Add message key (if new messages)
Add to `src/main/java/com/umdc/backoffice/constant/keys/${Domain}MessageKey.java`.

### Step 9 — Verify
```bash
mvn -DskipTests compile
mvn pmd:check
mvn test
```

## Output

List of files created/modified, `mvn test` result, new endpoint in `api.yaml`.
