# 🔄 MCAM Phase 4 — Secret Rotation & Audit

> **Prompt for:** `developer` agent  
> **Depends on:** Phase 1 + Phase 2 + Phase 3 complete  
> **Estimated duration:** 2–3 days  
> **Complexity:** Medium  
> **Reference:** `docs/plans/mcam-implementation-plan.md` §5

---

## 🎯 Goal

Implement secret rotation with a grace period, wire token revocation into deactivation and deletion flows, and confirm full audit trail coverage for all 9 M2M lifecycle event types.

---

## 📐 Architecture Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| AC-01 | Secret rotation generates a new 32-byte `SecureRandom` secret encoded as Base64URL |
| AC-02 | Old secret hash stored in Redis (`mcam:grace:{clientId}`) for exactly `rotationGracePeriodSeconds` — NOT only in DB |
| AC-03 | `prevSecretHash` is also persisted in DB (`managed_client.prev_secret_hash`) — for persistence across Redis restarts |
| AC-04 | Plaintext secret is returned **once only** in the rotate response — never logged, never stored |
| AC-05 | Deactivating a client (`active = false`) triggers `revokeAllTokens` before persisting |
| AC-06 | Deleting a client triggers `revokeAllTokens` before deletion |
| AC-07 | All 9 `AuditEventType` M2M constants must have audit calls wired — see coverage table below |
| AC-08 | `AuditEventServiceImpl.findEvents` must route M2M event types to `ManagedClientAuditEventRepository` without breaking the existing `GET /api/v1/iam/audit/events` contract |
| AC-09 | PMD zero violations — constructor injection, no `@Autowired` field injection |

---

## 📋 Tasks

### TASK 4.1 — ManagedClientRotationService Interface

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRotationService.java`

```java
/// Rotates the secret for the specified managed client.
/// Returns the new plaintext secret in the response — delivered once only.
ResponseEntity<?> rotateSecret(UUID clientId, String requestorIp);
```

---

### TASK 4.2 — ManagedClientRotationServiceImpl

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRotationServiceImpl.java`

- `@Service`
- Constructor: `ManagedClientRepository`, `ManagedClientSecretHashService`, `ManagedClientRedisService`, `ManagedClientAuditService`, `ManagementAuthenticatorProperties`
- SLF4J logger

#### `rotateSecret` logic:

```
1. Load client: repository.findByIdAndActiveTrue(clientId)
   → 404 ManagedClientErrorResponse("not_found", ...) if empty

2. Capture current hash:
   String currentSecretHash = entity.secretHash()   // will become prev

3. Generate new secret:
   SecureRandom secureRandom = new SecureRandom()
   byte[] rawBytes = new byte[32]
   secureRandom.nextBytes(rawBytes)
   String newRawSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes)

4. Hash new secret asynchronously:
   String newHash = managedClientSecretHashService.hashSecret(newRawSecret).get()
   // .get() blocks — acceptable since rotation is infrequent admin operation

5. Store old secret in Redis for grace period:
   redisService.storeGraceSecret(clientId,
                                  currentSecretHash,
                                  properties.getRotationGracePeriodSeconds())
   // Key: mcam:grace:{clientId}  TTL: rotationGracePeriodSeconds

6. Update entity:
   entity.setSecretHash(newHash)
   entity.setPrevSecretHash(currentSecretHash)   // DB persistence across Redis restart
   entity.setSecretLastRotatedAt(Instant.now())
   repository.save(entity)

7. Emit audit event (NEVER include hash values in details):
   String details = "{\"gracePeriodSeconds\":" + properties.getRotationGracePeriodSeconds() + "}"
   auditService.record(clientId, CLIENT_SECRET_ROTATED, requestorIp, "SUCCESS", details)

8. Return 200 ManagedClientSecretRotateResponse(
       clientId,
       newRawSecret,                          // plaintext — returned once only
       properties.getRotationGracePeriodSeconds(),
       Instant.now()
   )
```

> ⚠️ The `ManagedClientTokenServiceImpl.issueToken` method (Phase 3) already checks both `entity.secretHash` and `redisService.getGraceSecret(clientId)`. This is how the grace period works. No further changes to `issueToken` are needed for rotation support.

---

### TASK 4.3 — Rotation Endpoint in ManagedClientApi

#### Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientApi.java`

Append:

```java
@Operation(summary = "Rotate the secret for a managed client",
           description = "Returns the new plaintext clientSecret once. Grace period allows old secret temporarily.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Secret rotated"),
    @ApiResponse(responseCode = "404", description = "Client not found or inactive")
})
@PostMapping(value = "/{clientId}/rotate-secret",
             produces = MediaType.APPLICATION_JSON_VALUE)
default ResponseEntity<?> rotateManagedClientSecret(
        @PathVariable UUID clientId,
        HttpServletRequest httpRequest) {
    String requestorIp = httpRequest.getRemoteAddr();
    return ((ManagedClientController) this).getManagedClientRotationService()
            .rotateSecret(clientId, requestorIp);
}
```

---

### TASK 4.4 — ManagedClientController Rotation Method

#### Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientController.java`

1. Add `ManagedClientRotationService` constructor parameter
2. Expose package-visible accessor `getManagedClientRotationService()`
3. Override `rotateManagedClientSecret` — delegate to `managedClientRotationService.rotateSecret(clientId, ip)`

---

### TASK 4.5 — Deactivation → Implicit Token Revocation

#### Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientServiceImpl.java`

**Constructor:** Add `ManagedClientTokenService` parameter (replacing the TODO placeholder from Phase 2).

**In `updateClient`:** Replace the Phase 2 TODO comment with actual wiring:

```java
// If client is being deactivated, revoke all active tokens first (FR-17)
if (Boolean.FALSE.equals(request.active()) && entity.isActive()) {
    managedClientTokenService.revokeAllTokens(entity.getId());
    auditService.record(entity.getId(), AuditEventType.CLIENT_DEACTIVATED,
                        null, "SUCCESS", null);
}
```

**In `deleteClient`:** Replace the Phase 2 TODO comment with:

```java
// Revoke all active tokens before deletion (FR-30)
managedClientTokenService.revokeAllTokens(entity.getId());
```

---

### TASK 4.6 — Scheduled Cleanup of Stale prevSecretHash

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientMaintenanceService.java`

Interface:

```java
/// Clears stale prevSecretHash values from managed_client rows where the grace period has expired.
void clearExpiredPrevSecretHashes();
```

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientMaintenanceServiceImpl.java`

- `@Service`
- Constructor: `ManagedClientRepository`, `ManagementAuthenticatorProperties`
- `@Scheduled(fixedDelayString = "${MCAM_MAINTENANCE_INTERVAL_MS:600000}")` on `clearExpiredPrevSecretHashes`

```java
/// Runs every 10 minutes by default (MCAM_MAINTENANCE_INTERVAL_MS).
/// Clears prevSecretHash on clients where secretLastRotatedAt + gracePeriodSeconds < now.
@Scheduled(fixedDelayString = "${MCAM_MAINTENANCE_INTERVAL_MS:600000}")
@Override
public void clearExpiredPrevSecretHashes() {
    Instant graceCutoff = Instant.now()
        .minusSeconds(properties.getRotationGracePeriodSeconds());
    // Query: find all entities where prevSecretHash != null AND secretLastRotatedAt < graceCutoff
    // For each: entity.setPrevSecretHash(null); repository.save(entity)
    // Log count at INFO level
}
```

Add the necessary `@Query` to `ManagedClientRepository`:

```java
@Query("SELECT e FROM ManagedClientEntity e WHERE e.prevSecretHash IS NOT NULL " +
       "AND e.secretLastRotatedAt < :cutoff")
List<ManagedClientEntity> findWithExpiredPrevSecretHash(
    @Param("cutoff") Instant cutoff);
```

Ensure `@EnableScheduling` is active — check if the main bootstrap class or an existing config already has it; if not, add `@EnableScheduling` to `ManagedClientMaintenanceServiceImpl`'s configuration class.

---

### TASK 4.7 — Audit Coverage Verification

Verify that **every one of the 9 M2M audit event types** has a wired `auditService.record()` call:

| Event Type | Must be called in | Outcome |
|---|---|---|
| `CLIENT_REGISTERED` | `ManagedClientServiceImpl.registerClient` | `"SUCCESS"` |
| `CLIENT_UPDATED` | `ManagedClientServiceImpl.updateClient` | `"SUCCESS"` |
| `CLIENT_DEACTIVATED` | `ManagedClientServiceImpl.updateClient` (active → false) | `"SUCCESS"` |
| `CLIENT_DELETED` | `ManagedClientServiceImpl.deleteClient` | `"SUCCESS"` |
| `CLIENT_SECRET_ROTATED` | `ManagedClientRotationServiceImpl.rotateSecret` | `"SUCCESS"` |
| `CLIENT_TOKEN_ISSUED` | `ManagedClientTokenServiceImpl.issueToken` (success path) | `"SUCCESS"` |
| `CLIENT_TOKEN_ISSUE_FAILED` | `ManagedClientTokenServiceImpl.issueToken` (any failure path) | `"FAILURE"` |
| `CLIENT_TOKEN_REVOKED` | `ManagedClientTokenServiceImpl.revokeAllTokens` | `"SUCCESS"` |
| `CLIENT_INTROSPECTION_CALLED` | `ManagedClientTokenServiceImpl.introspectToken` (valid token only) | `"SUCCESS"` |

---

### TASK 4.8 — AuditEventServiceImpl Extension

#### Modify: `src/main/java/com/umdc/backoffice/v1/iam/audit/service/AuditEventServiceImpl.java`

**Goal:** When `findEvents` is called with an M2M `eventType`, delegate to `ManagedClientAuditEventRepository` instead of `AuditEventRepository`, then adapt the results to the existing `AuditEventTO` shape.

**Implementation steps:**

1. Inject `ManagedClientAuditEventRepository` via constructor parameter
2. Define a private constant set of M2M event types:
   ```java
   private static final Set<AuditEventType> M2M_EVENT_TYPES = Set.of(
       AuditEventType.CLIENT_REGISTERED, AuditEventType.CLIENT_UPDATED,
       AuditEventType.CLIENT_DEACTIVATED, AuditEventType.CLIENT_DELETED,
       AuditEventType.CLIENT_SECRET_ROTATED, AuditEventType.CLIENT_TOKEN_ISSUED,
       AuditEventType.CLIENT_TOKEN_ISSUE_FAILED, AuditEventType.CLIENT_TOKEN_REVOKED,
       AuditEventType.CLIENT_INTROSPECTION_CALLED
   );
   ```
3. In `findEvents`: if the requested `eventType` is in `M2M_EVENT_TYPES`:
   - Query `ManagedClientAuditEventRepository.findByEventType(eventType, pageable)`
   - Map `ManagedClientAuditEventEntity` → `AuditEventTO`:
     - `userId` ← `clientId` (field reuse — document this mapping)
     - `eventType` ← `eventType`
     - `ipAddress`, `outcome`, `details`, `occurredAt` ← direct mapping
   - Return as `ResponseEntity.ok(page)`
4. Otherwise: existing logic unchanged

> ⚠️ Read `AuditEventServiceImpl` fully before modifying. The existing `findEvents` contract (`GET /api/v1/iam/audit/events`) must not change its HTTP response shape. Only the data source branches internally.

---

## ✅ Phase 4 Quality Gate Checklist

```bash
mvn -DskipTests compile
mvn pmd:check pmd:cpd-check
mvn test
```

- [ ] `mvn -DskipTests compile` — exits 0
- [ ] `mvn pmd:check` — 0 violations
- [ ] `POST /{clientId}/rotate-secret` → `200` with new `clientSecret` + `gracePeriodSeconds` (AC-ROT-01)
- [ ] Old secret accepted during grace period (`POST /token` with old secret within TTL) (AC-ROT-01 grace behavior)
- [ ] Old secret rejected after Redis TTL expires (FR-13) — verify by manually expiring Redis key in test
- [ ] `GET /api/v1/iam/audit/events?eventType=CLIENT_SECRET_ROTATED` returns rotation events (AC-AUD-01)
- [ ] All 9 M2M event types queryable via audit endpoint (AC-AUD-01)
- [ ] `PATCH`/`PUT` client with `active=false` → `DELETE /{clientId}/tokens` revocation confirmed (FR-17)
- [ ] `DELETE /{clientId}` → tokens revoked before record deleted (FR-30)
- [ ] `prevSecretHash` cleared from DB after grace period + cleanup task runs (R-05 mitigation)

---

## 🔗 What Phase 5 Needs From This Phase

| Artifact | Used By |
|---|---|
| `ManagedClientRotationServiceImpl` | Phase 5: `ManagedClientRotationServiceImplTest` |
| All 9 audit event wiring confirmed | Phase 5: AC-AUD-01 test assertions |
| `ManagedClientMaintenanceServiceImpl` | Phase 5: scheduled cleanup test (or manual verification) |
| `AuditEventServiceImpl` M2M routing | Phase 5: Integration test via `GET /api/v1/iam/audit/events` |

