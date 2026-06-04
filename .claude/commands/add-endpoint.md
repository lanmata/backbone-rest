# Add Endpoint — backbone-rest

Scaffold a complete new REST endpoint following the backbone-rest interface-first pattern.

## Usage
```
/add-endpoint
```
Then provide:
- **Domain** (existing module name, e.g. `users`, `roles`, `contacts`)
- **HTTP method** (`GET` / `POST` / `PUT` / `PATCH` / `DELETE`)
- **Path** (e.g. `/api/v1/users/{id}/contacts`)
- **Brief description** of what it does

---

## Steps Claude Will Execute

### 1. Analyze Existing Pattern
Read the existing `*Api.java` and `*Controller.java` for the target domain to match the exact style.

### 2. Add to `*Api.java`
Append a new `default` method:
```java
@Operation(summary = "<summary>", description = "<description>")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal error")
})
@<METHOD>Mapping("<path>")
default ResponseEntity<?> <methodName>(<params>) {
    return ((DomainController) this).<serviceMethod>(<args>);
}
```

### 3. Add `@Override` to `*Controller.java`
```java
@Override
public ResponseEntity<?> <methodName>(<params>) {
    return <domainService>.<serviceMethod>(<args>);
}
```

### 4. Add Method to `*Service.java` Interface
```java
ResponseEntity<?> <serviceMethod>(<params>);
```

### 5. Implement in `*ServiceImpl.java`
```java
@Override
public ResponseEntity<?> <serviceMethod>(<params>) {
    // implementation
    return ResponseEntity.ok(<result>);
}
```

### 6. Add DTOs if needed
Create `src/main/java/com/umdc/backoffice/v1/<domain>/api/to/` request/response records.

### 7. Update OpenAPI YAML
Add the new path + operation to:
`../../src/main/resources/META-INF/api.yaml`

### 8. Verify
```bash
mvn -DskipTests compile
mvn pmd:check
```

---

## Rules
- Constructor injection only — no `@Autowired`.
- Controller has one-liner override — zero logic.
- Service encodes all HTTP status decisions.
- PMD zero violations before reporting done.
