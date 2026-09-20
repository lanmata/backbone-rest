# ⚙️ MCAM Phase 2 — Core CRUD API

> **Prompt for:** `developer` agent  
> **Depends on:** Phase 1 complete (`docs/prompts/mcam/phase-1-foundation.md`)  
> **Estimated duration:** 3–4 days  
> **Complexity:** Medium  
> **Reference:** `docs/plans/mcam-implementation-plan.md` §3

---

## 🎯 Goal

Implement full admin CRUD for managed clients exposed at `/api/v1/managed-clients`.  
**Token issuance, revocation, and introspection are NOT part of this phase** — they are Phase 3.

---

## 📐 Architecture Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| AC-01 | Interface-first: `ManagedClientApi` (interface) + `ManagedClientController` (impl) |
| AC-02 | All service methods return `ResponseEntity<?>` — status decisions made in the service layer |
| AC-03 | Controller is a **thin delegate** — zero business logic in the controller |
| AC-04 | Constructor injection only — no `@Autowired` field injection |
| AC-05 | `mvn pmd:check` must pass with 0 violations after every change |
| AC-06 | `POST /api/v1/managed-clients` must return plaintext `clientSecret` exactly **once** (AC-REG-01) |
| AC-07 | `GET /api/v1/managed-clients/{clientId}` response body must **never** contain secret fields (AC-SEC-02) |
| AC-08 | Duplicate `(name, applicationId)` must return `409 Conflict` (AC-REG-02) |

---

## 📋 Tasks

### TASK 2.1 — ManagedClientAuditService

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientAuditService.java`

Service interface:

```java
/// Records an audit event for a managed client lifecycle action.
void record(UUID clientId, AuditEventType eventType, String ipAddress,
            String outcome, String details);
```

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientAuditServiceImpl.java`

- `@Service`
- Constructor: `ManagedClientAuditEventRepository`
- `record()` annotated with `@Async` — mirror the pattern used in the existing `AuditEventServiceImpl`
- Builds `ManagedClientAuditEventEntity` from parameters and saves via repository
- Log at `DEBUG` level on entry (SLF4J via `LoggerFactory.getLogger`)
- `outcome` values: `"SUCCESS"` or `"FAILURE"` — use constants, not literals

---

### TASK 2.2 — ManagedClientSecretHashService

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientSecretHashService.java`

```java
/// Hashes a raw secret asynchronously using BCrypt.
CompletableFuture<String> hashSecret(String rawSecret);

/// Constant-time comparison of a raw secret against a BCrypt hash.
boolean matchesWithConstantTime(String rawSecret, String storedHash);
```

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientSecretHashServiceImpl.java`

- `@Service`
- Constructor: `BCryptPasswordEncoder` (injected — must already be declared as `@Bean` in `SecurityBeansConfig` or equivalent; do **not** instantiate directly)
- `hashSecret`: `@Async("mcamHashExecutor")` — runs BCrypt hash on the dedicated thread pool; returns `CompletableFuture<String>`
- `matchesWithConstantTime`: delegates to `BCryptPasswordEncoder.matches()` — document explicitly that `BCryptPasswordEncoder.matches()` is inherently constant-time (satisfies AC-TOK-02)
- Add `///` Javadoc on both methods explaining the security contract

---

### TASK 2.3 — ManagedClientAsyncConfig

#### `src/main/java/com/umdc/backoffice/v1/managedclient/config/ManagedClientAsyncConfig.java`

```java
@Configuration
@EnableAsync
public class ManagedClientAsyncConfig {

    /// Thread pool for BCrypt hashing operations.
    /// Prevents BCrypt from blocking Tomcat I/O threads under load (NFR-P-03).
    @Bean("mcamHashExecutor")
    public ThreadPoolTaskExecutor mcamHashExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mcam-hash-");
        executor.initialize();
        return executor;
    }
}
```

---

### TASK 2.4 — ManagedClientService Interface

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientService.java`

```java
/// Registers a new managed client. Returns 201 with plaintext clientSecret on success.
ResponseEntity<?> registerClient(ManagedClientCreateRequest request);

/// Lists managed clients with optional filters. Returns 204 if no results.
ResponseEntity<?> listClients(UUID applicationId, Boolean active, int page, int size);

/// Returns a single managed client by ID. Never exposes secret material.
ResponseEntity<?> getClient(UUID clientId);

/// Partially updates a managed client. Returns 200 on success.
ResponseEntity<?> updateClient(UUID clientId, ManagedClientUpdateRequest request);

/// Deletes a managed client. Revokes all active tokens before deletion.
ResponseEntity<?> deleteClient(UUID clientId);
```

---

### TASK 2.5 — ManagedClientServiceImpl

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientServiceImpl.java`

- `@Service`
- Constructor: `ManagedClientRepository`, `ManagedClientMapper`, `ManagedClientAuditService`, `ManagedClientSecretHashService`  
  *(Note: `ManagedClientTokenService` injection is added in Phase 4 when revoke-on-deactivate/delete is wired)*
- `@LogDefault` AOP annotation on service methods (if pattern exists in codebase — check `AuditService` usage)
- SLF4J logger via `LoggerFactory.getLogger(ManagedClientServiceImpl.class)`

#### `registerClient` logic:

1. Check `existsByNameAndApplicationId(request.name(), request.applicationId())` → return `ResponseEntity.status(409)` with `ManagedClientErrorResponse("client_conflict", "A client with this name already exists for the application.")` if true
2. Generate `UUID clientId = UUID.randomUUID()`
3. Generate 32-byte random secret: `Base64.getUrlEncoder().withoutPadding().encodeToString(secureRandom.generateSeed(32))`
4. `String hash = managedClientSecretHashService.hashSecret(rawSecret).get()` (block for now — async optimization can be a follow-up)
5. Map `request` → `ManagedClientEntity` via mapper; set `id`, `secretHash`, `active`
6. `repository.save(entity)`
7. `auditService.record(clientId, CLIENT_REGISTERED, null, "SUCCESS", null)`
8. Build and return `ResponseEntity.status(201).body(new ManagedClientCreateResponse(clientId, rawSecret, ...))`

#### `listClients` logic:

- Build `PageRequest.of(page, size)` 
- Branch on `applicationId` / `active` filter combinations using the appropriate repository method
- If page is empty → `ResponseEntity.noContent().build()` (204)
- Otherwise → `ResponseEntity.ok(mapper.toTOList(page.getContent()))`

#### `getClient` logic:

- `repository.findById(clientId).orElse(null)` → 404 `ManagedClientErrorResponse("not_found", ...)` if null
- Map to `ManagedClientTO` (mapper ensures no secret fields) → `ResponseEntity.ok(to)`

#### `updateClient` logic:

1. Load entity → 404 if not found
2. Apply partial update: `mapper.updateEntityFromRequest(request, entity)`
3. If `request.active() == Boolean.FALSE` AND `entity.active == true` → *(placeholder)* leave a `// TODO Phase 4: call revokeAllTokens` comment — wired in Phase 4
4. `repository.save(entity)`
5. `auditService.record(clientId, CLIENT_UPDATED, null, "SUCCESS", null)`
6. Return `ResponseEntity.ok(mapper.toTO(entity))`

#### `deleteClient` logic:

1. Load entity → 404 if not found
2. *(placeholder)* `// TODO Phase 4: call revokeAllTokens(clientId)` — wired in Phase 4
3. `repository.delete(entity)`
4. `auditService.record(clientId, CLIENT_DELETED, null, "SUCCESS", null)`
5. Return `ResponseEntity.noContent().build()` (204)

---

### TASK 2.6 — ManagedClientApi Interface

#### `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientApi.java`

```java
@RequestMapping("/api/v1/managed-clients")
public interface ManagedClientApi {

    @Operation(summary = "Register a new managed client",
               description = "Returns the clientSecret exactly once in the response body.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Client registered"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Duplicate name for application")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> registerManagedClient(
            @Valid @RequestBody ManagedClientCreateRequest request) {
        return ((ManagedClientController) this).getManagedClientService().registerClient(request);
    }

    @Operation(summary = "List managed clients")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client list"),
        @ApiResponse(responseCode = "204", description = "No clients found")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> listManagedClients(
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ((ManagedClientController) this).getManagedClientService()
                .listClients(applicationId, active, page, size);
    }

    @Operation(summary = "Get a managed client by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client found"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping(value = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> getManagedClient(@PathVariable UUID clientId) {
        return ((ManagedClientController) this).getManagedClientService().getClient(clientId);
    }

    @Operation(summary = "Update a managed client")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Client updated"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PutMapping(value = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<?> updateManagedClient(
            @PathVariable UUID clientId,
            @Valid @RequestBody ManagedClientUpdateRequest request) {
        return ((ManagedClientController) this).getManagedClientService()
                .updateClient(clientId, request);
    }

    @Operation(summary = "Delete a managed client")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Client deleted"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    @DeleteMapping(value = "/{clientId}")
    default ResponseEntity<?> deleteManagedClient(@PathVariable UUID clientId) {
        return ((ManagedClientController) this).getManagedClientService().deleteClient(clientId);
    }
}
```

> **Note on cast pattern:** The `((ManagedClientController) this)` cast mirrors the existing pattern used in `UserApi` and other `*Api` interfaces in this codebase. Check `UserApi.putUserDetail` for the exact pattern and replicate it.

---

### TASK 2.7 — ManagedClientController

#### `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientController.java`

```java
@RestController
@RequestMapping("/api/v1/managed-clients")
public class ManagedClientController implements ManagedClientApi {

    private final ManagedClientService managedClientService;

    public ManagedClientController(final ManagedClientService managedClientService) {
        this.managedClientService = managedClientService;
    }

    /// Package-visible accessor used by ManagedClientApi default methods.
    ManagedClientService getManagedClientService() {
        return managedClientService;
    }

    @Override
    public ResponseEntity<?> registerManagedClient(
            @Valid @RequestBody ManagedClientCreateRequest request) {
        return managedClientService.registerClient(request);
    }

    @Override
    public ResponseEntity<?> listManagedClients(UUID applicationId, Boolean active,
                                                  int page, int size) {
        return managedClientService.listClients(applicationId, active, page, size);
    }

    @Override
    public ResponseEntity<?> getManagedClient(UUID clientId) {
        return managedClientService.getClient(clientId);
    }

    @Override
    public ResponseEntity<?> updateManagedClient(UUID clientId,
                                                   @Valid @RequestBody ManagedClientUpdateRequest request) {
        return managedClientService.updateClient(clientId, request);
    }

    @Override
    public ResponseEntity<?> deleteManagedClient(UUID clientId) {
        return managedClientService.deleteClient(clientId);
    }
}
```

---

### TASK 2.8 — SecurityConfig Update

#### Modify: `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java`

In the `authorizeHttpRequests` configuration block, add permit rules for the two public MCAM endpoints **before** the existing authenticated rules:

```java
auth.requestMatchers(HttpMethod.POST, "/api/v1/managed-clients/token").permitAll();
auth.requestMatchers(HttpMethod.POST, "/api/v1/managed-clients/introspect").permitAll();
```

> ⚠️ These two paths are opened now (Phase 2) even though the endpoints are implemented in Phase 3. This prevents later SecurityConfig changes from interfering with Phase 3 work.

> ⚠️ **Read `SecurityConfig.java` in full before editing.** Never modify the OAuth2 resource-server filter chain order without understanding the full bean wiring. Modify only the `authorizeHttpRequests` lambda — do not touch `@Bean` method signatures or filter chain ordering (that's done in Phase 3).

---

## ✅ Phase 2 Quality Gate Checklist

```bash
mvn -DskipTests compile
mvn pmd:check pmd:cpd-check
mvn test
```

- [ ] `mvn -DskipTests compile` — exits 0
- [ ] `mvn pmd:check` — 0 violations on all new and modified files
- [ ] `POST /api/v1/managed-clients` → `201` with `clientSecret` field populated (AC-REG-01)
- [ ] `GET /api/v1/managed-clients/{clientId}` → `200` response body has **no** `secretHash`, `prevSecretHash`, or `clientSecret` fields (AC-SEC-02)
- [ ] Duplicate `(name, applicationId)` → `409` (AC-REG-02)
- [ ] Missing required `name` or empty `scopes` → `400` (AC-REG-03)
- [ ] `DELETE /api/v1/managed-clients/{clientId}` for unknown ID → `404`

---

## 🔗 What Phase 3 Needs From This Phase

| Artifact | Used By |
|---|---|
| `ManagedClientService` (interface + impl) | Phase 3: `ManagedClientServiceImpl` update (deactivate→revoke wiring via TODO stub) |
| `ManagedClientApi` + `ManagedClientController` | Phase 3: Add `/token`, `/introspect`, `/{clientId}/tokens` endpoint declarations |
| `SecurityConfig` permit rules | Phase 3: Token filter registered without re-editing permitted paths |
| `ManagedClientAuditService` | Phase 3: `ManagedClientTokenServiceImpl` |
| `ManagedClientSecretHashService` | Phase 3: `ManagedClientTokenServiceImpl` |

