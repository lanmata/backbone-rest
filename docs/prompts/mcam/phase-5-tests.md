# 🧪 MCAM Phase 5 — Tests + PMD + JaCoCo Gates

> **Prompt for:** `test-writer` agent  
> **Depends on:** Phases 1–4 complete  
> **Estimated duration:** 3–4 days  
> **Complexity:** Medium  
> **Reference:** `docs/plans/mcam-implementation-plan.md` §6

---

## 🎯 Goal

Write a complete test suite covering all 15 MCAM acceptance criteria.  
All tests must pass (`mvn test`), PMD must report 0 violations on test files, and the MCAM service package must achieve **≥ 80% line coverage** in the JaCoCo report.

---

## 📐 Test Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| TC-01 | JUnit 5 + Mockito — match the existing test style in `src/test/java/com/umdc/backoffice/` |
| TC-02 | Constructor injection in test classes — no `@Autowired` or `@InjectMocks` on fields when a constructor is available |
| TC-03 | `@ExtendWith(MockitoExtension.class)` for unit tests |
| TC-04 | PMD zero violations on all test files — `mvn pmd:check` must pass |
| TC-05 | Do **not** modify production source files — test changes only |
| TC-06 | Use `///` doc comment style on test class and method Javadocs |
| TC-07 | No Lombok in test classes |
| TC-08 | Test class naming: `<ProductionClass>Test` for unit tests, `<Feature>IT` for integration tests |

---

## 📋 Tasks

### TASK 5.1 — ManagedClientServiceImplTest

#### `src/test/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientServiceImplTest.java`

**Framework:** JUnit 5 + Mockito  
**AC Coverage:** AC-REG-01, AC-REG-02, AC-REG-03, AC-SEC-02  
**Target:** ≥ 80% line coverage on `ManagedClientServiceImpl`

**Mocks required:**
- `ManagedClientRepository`
- `ManagedClientMapper`
- `ManagedClientAuditService`
- `ManagedClientSecretHashService`
- `ManagedClientTokenService` (injected in Phase 4)

**Test scenarios:**

| Test method | Scenario | Expected result |
|---|---|---|
| `registerClient_happyPath_returns201WithSecret` | Valid request, no duplicate | `201` response, body has `clientSecret` non-null |
| `registerClient_duplicate_returns409` | `existsByNameAndApplicationId` returns `true` | `409` with `error="client_conflict"` |
| `registerClient_missingName_returns400` | `@Valid` validation fires | `400` (test via `@WebMvcTest` slice or mock validator) |
| `listClients_withResults_returns200` | Repository returns non-empty page | `200` with list |
| `listClients_empty_returns204` | Repository returns empty page | `204 No Content` |
| `getClient_found_returns200WithNoSecretFields` | Entity found | `200`; assert `ManagedClientTO` has null `secretHash` and `prevSecretHash` |
| `getClient_notFound_returns404` | `findById` returns empty | `404` with `error="not_found"` |
| `updateClient_partialUpdate_returns200` | Valid request | `200` with updated TO |
| `updateClient_deactivate_triggersRevoke` | `request.active() == false`, entity was active | `revokeAllTokens` called once (verify mock interaction) |
| `deleteClient_found_triggersRevokeAndDelete` | Entity found | `revokeAllTokens` called; `repository.delete` called; `204` |
| `deleteClient_notFound_returns404` | `findById` returns empty | `404` |

**AC-SEC-02 assertion helper:**
```java
private void assertNoSecretFields(ManagedClientTO to) {
    // ManagedClientTO must not have secretHash or prevSecretHash fields at all
    // Use reflection or simply verify the mapper test separately
    assertNotNull(to.clientId());
    assertNotNull(to.name());
}
```

---

### TASK 5.2 — ManagedClientTokenServiceImplTest

#### `src/test/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientTokenServiceImplTest.java`

**AC Coverage:** AC-TOK-01, AC-TOK-02, AC-TOK-03, AC-TOK-04, AC-REV-01, AC-INT-01, AC-INT-02

**Mocks required:**
- `ManagedClientRepository`
- `ManagedClientSecretHashService`
- `ManagedClientAuditService`
- `ManagedClientRedisService`
- `ManagementAuthenticatorProperties`
- `KeystoreUtil`

**Test scenarios:**

| Test method | Scenario | Expected result |
|---|---|---|
| `issueToken_validCredentials_returns200WithJwt` | Client active, secret matches, scope valid | `200`; JWT has `type=M2M`, `sub`, `jti`, `exp`, `scopes` claims (AC-TOK-01) |
| `issueToken_wrongSecret_returns401` | `matchesWithConstantTime` returns false | `401 {"error":"invalid_client"}` (AC-TOK-02) |
| `issueToken_inactiveClient_returns401` | `findByIdAndActiveTrue` returns empty | `401 {"error":"invalid_client"}` (AC-TOK-03) |
| `issueToken_scopeOverflow_returns400` | Requested scope not in entity scopes | `400 {"error":"invalid_scope"}` with allowed list (AC-TOK-04) |
| `issueToken_rateLimitExceeded_returns429` | `getRateLimitCount` ≥ `rateLimitRpm` | `429` |
| `issueToken_graceSecretMatches_returns200` | Current hash no match, grace hash matches | `200` — old secret valid during grace period (AC-ROT-01 partial) |
| `revokeAllTokens_withActiveTokens_returns204AndRevokes` | Redis has 3 JTIs for client | `204`; `revokeToken` called 3 times; `CLIENT_TOKEN_REVOKED` audit emitted |
| `revokeAllTokens_noActiveTokens_returns204` | Redis returns empty set | `204`; no Redis calls for revokeToken |
| `introspectToken_validToken_returnsActiveTrue` | Token signature valid, not revoked, not expired | `200 active=true` with all claims (AC-INT-01) |
| `introspectToken_revokedToken_returnsActiveFalse` | `isRevoked` returns true | `200 active=false` (AC-REV-01) |
| `introspectToken_expiredToken_returnsActiveFalse` | JWT is expired | `200 active=false` (AC-INT-02) |
| `introspectToken_malformedToken_returnsActiveFalse` | Invalid Base64 / bad format | `200 active=false` — never 4xx (AC-INT-02) |
| `isTokenActive_activeJti_returnsTrue` | `isActive=true`, `isRevoked=false` | `true` |
| `isTokenActive_revokedJti_returnsFalse` | `isRevoked=true` | `false` |

> **JWT generation in tests:** For tests that verify JWT parsing, generate a real RS256 test key pair using `KeyPairGenerator.getInstance("RSA")` with 2048 bits. Mock `KeystoreUtil` to return the test private/public key.

---

### TASK 5.3 — ManagedClientRotationServiceImplTest

#### `src/test/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRotationServiceImplTest.java`

**AC Coverage:** AC-ROT-01

**Mocks required:**
- `ManagedClientRepository`
- `ManagedClientSecretHashService`
- `ManagedClientRedisService`
- `ManagedClientAuditService`
- `ManagementAuthenticatorProperties`

**Test scenarios:**

| Test method | Scenario | Expected result |
|---|---|---|
| `rotateSecret_found_returns200WithNewSecret` | Client exists and active | `200`; `clientSecret` in response != old secret; `gracePeriodSeconds` matches config |
| `rotateSecret_found_storesGraceSecretInRedis` | Happy path | `storeGraceSecret` called with old hash + grace TTL (AC-ROT-01 grace) |
| `rotateSecret_found_updatesEntitySecretHash` | Happy path | `entity.secretHash` updated to new hash; `prevSecretHash` = old hash; `repository.save` called |
| `rotateSecret_found_emitsAuditEvent` | Happy path | `CLIENT_SECRET_ROTATED` audit record emitted |
| `rotateSecret_notFound_returns404` | `findByIdAndActiveTrue` returns empty | `404` |
| `rotateSecret_newSecretDiffersFromOld` | Happy path | `response.clientSecret()` != original entity `secretHash` (plaintext vs hash — assert they differ) |

---

### TASK 5.4 — ManagedClientSecretHashServiceImplTest

#### `src/test/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientSecretHashServiceImplTest.java`

**AC Coverage:** AC-SEC-01 (partial — secret not present in hash)

**No mocks required** (uses real `BCryptPasswordEncoder`)

**Test scenarios:**

| Test method | Scenario | Expected result |
|---|---|---|
| `hashSecret_producesValidBcryptHash` | Hash a known secret | Result starts with `$2a$` or `$2b$`; not equal to raw input |
| `hashSecret_rawSecretNotInHashString` | Hash a known secret | `hash.contains(rawSecret)` is **false** (AC-SEC-01) |
| `matchesWithConstantTime_correctSecret_returnsTrue` | Hash then match same secret | `true` |
| `matchesWithConstantTime_wrongSecret_returnsFalse` | Wrong input | `false` |
| `hashSecret_isAsync` | Method returns `CompletableFuture` | `CompletableFuture.isDone()` eventually true; no exception |

---

### TASK 5.5 — ManagedClientRedisServiceImplTest

#### `src/test/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRedisServiceImplTest.java`

**Note:** Use `Mockito` to mock `StringRedisTemplate` and its `ValueOperations` / `SetOperations` — unless an embedded Redis (`it.ozimov:embedded-redis` or `com.github.kstyrc:embedded-redis`) is available in the test classpath. Match what is already used in the project's existing Redis tests.

**Test scenarios:**

| Test method | Scenario | Expected result |
|---|---|---|
| `storeToken_setsKeyWithTtl` | `storeToken(jti, clientId, 3600)` | `SET mcam:token:{jti}` called with TTL 3600 |
| `revokeToken_setsTombstoneAndDeletesActive` | `revokeToken(jti, 3660)` | Tombstone key set; active key deleted |
| `isRevoked_exists_returnsTrue` | Tombstone key present | `true` |
| `isRevoked_absent_returnsFalse` | No tombstone | `false` |
| `isActive_exists_returnsTrue` | Active key present | `true` |
| `isActive_absent_returnsFalse` | No active key | `false` |
| `storeGraceSecret_setsKeyWithTtl` | `storeGraceSecret(clientId, hash, 300)` | `SET mcam:grace:{clientId}` with TTL 300 |
| `getGraceSecret_present_returnsOptionalWithValue` | Key exists | `Optional.of(hash)` |
| `getGraceSecret_absent_returnsEmpty` | No key | `Optional.empty()` |
| `removeGraceSecret_deletesKey` | `removeGraceSecret(clientId)` | `DEL mcam:grace:{clientId}` called |
| `incrementRateLimit_firstCall_setsExpiry` | Counter goes from 0 → 1 | TTL set to 60s |
| `getRateLimitCount_noKey_returnsZero` | Missing key | `0L` |

---

### TASK 5.6 — ManagedClientMapperTest

#### `src/test/java/com/umdc/backoffice/v1/managedclient/mapper/ManagedClientMapperTest.java`

**AC Coverage:** AC-SEC-02 (GET response has no secret fields)

**Setup:** Use `@SpringBootTest` slice or instantiate the MapStruct-generated mapper implementation directly.

**Test scenarios:**

| Test method | Scenario | Expected result |
|---|---|---|
| `toTO_mapsAllFields` | Full entity → TO | All non-secret fields mapped correctly |
| `toTO_doesNotExposeSecretHash` | Entity has secretHash set | `ManagedClientTO` has no `secretHash` field accessible (AC-SEC-02) |
| `toTO_clientIdMappedFromEntityId` | Entity `id` = known UUID | `to.clientId()` equals that UUID |
| `toEntity_ignoresIdAndHashes` | Request → entity | `entity.id` is null; `entity.secretHash` is null |
| `updateEntityFromRequest_nullFieldsPreserved` | Request with null `name` → partial update | Existing `name` on entity unchanged |
| `toTOList_mapsAllElements` | List of 3 entities | List of 3 TOs, each without secret fields |

---

### TASK 5.7 — ManagedClientControllerIT (Integration)

#### `src/test/java/com/umdc/backoffice/v1/managedclient/ManagedClientControllerIT.java`

**Framework:** `@WebMvcTest(ManagedClientController.class)` + `MockMvc`  
**AC Coverage:** AC-REG-01, AC-REG-02, AC-REG-03, AC-SEC-02  
**Authentication:** Mock admin JWT in `Authorization` header

**Test scenarios:**

| Test method | HTTP | Expected |
|---|---|---|
| `registerClient_valid_returns201` | `POST /api/v1/managed-clients` | `201`, body has `clientSecret` |
| `registerClient_duplicate_returns409` | `POST` (duplicate name+appId) | `409` |
| `registerClient_missingName_returns400` | `POST` (no `name`) | `400` |
| `registerClient_emptyScopes_returns400` | `POST` (empty `scopes`) | `400` |
| `listClients_returns200` | `GET /api/v1/managed-clients` | `200` |
| `getManagedClient_returns200WithNoSecretFields` | `GET /{clientId}` | `200`; JSON has no `secretHash` key |
| `getManagedClient_notFound_returns404` | `GET /unknown-id` | `404` |
| `updateManagedClient_returns200` | `PUT /{clientId}` | `200` |
| `deleteManagedClient_returns204` | `DELETE /{clientId}` | `204` |

**Secret field assertion helper:**
```java
private void assertNoSecretInJson(String json) throws Exception {
    assertFalse(json.contains("secretHash"),    "Response must not contain secretHash");
    assertFalse(json.contains("prevSecretHash"), "Response must not contain prevSecretHash");
    assertFalse(json.contains("clientSecret"),  "GET response must not contain clientSecret");
}
```

---

### TASK 5.8 — ManagedClientTokenControllerIT (Integration)

#### `src/test/java/com/umdc/backoffice/v1/managedclient/ManagedClientTokenControllerIT.java`

**Framework:** `@WebMvcTest(ManagedClientController.class)` + `MockMvc`  
**AC Coverage:** AC-TOK-01, AC-TOK-02, AC-TOK-03, AC-TOK-04, AC-REV-01, AC-INT-01, AC-INT-02

| Test method | HTTP | Expected |
|---|---|---|
| `issueToken_valid_returns200WithJwt` | `POST /token` | `200`; `access_token` present; `token_type="Bearer"` |
| `issueToken_wrongSecret_returns401` | `POST /token` | `401 {"error":"invalid_client"}` |
| `issueToken_inactiveClient_returns401` | `POST /token` | `401 {"error":"invalid_client"}` |
| `issueToken_scopeOverflow_returns400` | `POST /token` | `400 {"error":"invalid_scope"}` |
| `revokeAll_then_introspect_returnsActiveFalse` | `DELETE /{id}/tokens` then `POST /introspect` | `204` then `200 active=false` (AC-REV-01) |
| `introspect_validToken_returnsActiveTrue` | `POST /introspect` | `200 active=true` (AC-INT-01) |
| `introspect_expiredToken_returnsActiveFalse` | `POST /introspect` | `200 active=false` (AC-INT-02) |
| `introspect_malformedToken_returnsActiveFalse` | `POST /introspect` (garbage) | `200 active=false` — not 4xx (AC-INT-02) |

---

### TASK 5.9 — ManagedClientRotationIT (Integration)

#### `src/test/java/com/umdc/backoffice/v1/managedclient/ManagedClientRotationIT.java`

**AC Coverage:** AC-ROT-01

| Test method | HTTP | Expected |
|---|---|---|
| `rotateSecret_returns200WithNewSecret` | `POST /{id}/rotate-secret` | `200`; `clientSecret` present; `gracePeriodSeconds` > 0 |
| `rotateSecret_oldSecretStillValidDuringGrace` | `POST /token` with old secret after rotation | `200` (AC-ROT-01 grace) |
| `rotateSecret_notFound_returns404` | `POST /unknown-id/rotate-secret` | `404` |

---

### TASK 5.10 — ManagedClientTokenFilterTest

#### `src/test/java/com/umdc/backoffice/security/filter/ManagedClientTokenFilterTest.java`

**AC Coverage:** Filter chain behavior (indirect coverage of AC-TOK-01, AC-REV-01)

| Test method | Scenario | Expected |
|---|---|---|
| `m2mToken_valid_setsSecurityContext` | Valid M2M JWT in `Authorization` header | `SecurityContextHolder` has non-null authentication; `filterChain.doFilter` called |
| `m2mToken_revoked_returns401` | M2M JWT but `isTokenActive` returns false | `401 {"error":"invalid_token"}` |
| `m2mToken_expired_returns401` | Expired M2M JWT | `401 {"error":"invalid_token"}` |
| `nonM2mToken_passesToNextFilter` | JWT without `type=M2M` claim | `filterChain.doFilter` called; SecurityContext untouched |
| `noAuthHeader_passesToNextFilter` | No `Authorization` header | `filterChain.doFilter` called |
| `sessionTokenRequest_notIntercepted` | `session-token` header only (no Bearer) | `filterChain.doFilter` called |

---

### TASK 5.11 — PMD & JaCoCo Validation

After all tests are written, run the full quality gate:

```bash
# Full test + PMD + JaCoCo
export REPSY_ACCOUNT_USER=<user>
export REPSY_ACCOUNT_PASSWORD=<pass>
mvn test jacoco:report

# PMD check specifically
mvn pmd:check pmd:cpd-check

# JaCoCo report location
open target/site/jacoco/index.html
```

Navigate to `com.umdc.backoffice.v1.managedclient.service` in the JaCoCo report.  
The **line coverage** for all service classes must be **≥ 80%**.

---

## ✅ Phase 5 Quality Gate Checklist

- [ ] `mvn test` — all tests green (0 failures, 0 errors)
- [ ] `mvn pmd:check` — 0 violations on ALL new test files
- [ ] `mvn test jacoco:report` — `v1/managedclient/service` package shows **≥ 80%** line coverage
- [ ] All 15 acceptance criteria have **at least one automated test** asserting the expected behavior:

| AC | Covered By |
|---|---|
| AC-REG-01 | `ManagedClientServiceImplTest.registerClient_happyPath_returns201WithSecret` + `ManagedClientControllerIT.registerClient_valid_returns201` |
| AC-REG-02 | `ManagedClientServiceImplTest.registerClient_duplicate_returns409` + `ManagedClientControllerIT.registerClient_duplicate_returns409` |
| AC-REG-03 | `ManagedClientControllerIT.registerClient_missingName_returns400` |
| AC-TOK-01 | `ManagedClientTokenServiceImplTest.issueToken_validCredentials_returns200WithJwt` + `ManagedClientTokenControllerIT.issueToken_valid_returns200WithJwt` |
| AC-TOK-02 | `ManagedClientTokenServiceImplTest.issueToken_wrongSecret_returns401` |
| AC-TOK-03 | `ManagedClientTokenServiceImplTest.issueToken_inactiveClient_returns401` |
| AC-TOK-04 | `ManagedClientTokenServiceImplTest.issueToken_scopeOverflow_returns400` |
| AC-ROT-01 | `ManagedClientRotationServiceImplTest.*` + `ManagedClientRotationIT.*` |
| AC-REV-01 | `ManagedClientTokenServiceImplTest.revokeAllTokens_*` + `ManagedClientTokenControllerIT.revokeAll_then_introspect_returnsActiveFalse` |
| AC-INT-01 | `ManagedClientTokenServiceImplTest.introspectToken_validToken_returnsActiveTrue` + `ManagedClientTokenControllerIT.introspect_validToken_returnsActiveTrue` |
| AC-INT-02 | `ManagedClientTokenServiceImplTest.introspectToken_expiredToken_returnsActiveFalse` + `ManagedClientTokenControllerIT.introspect_expiredToken_returnsActiveFalse` |
| AC-AUD-01 | `ManagedClientServiceImplTest.registerClient_happyPath_returns201WithSecret` (verify audit mock called) |
| AC-SEC-01 | `ManagedClientSecretHashServiceImplTest.hashSecret_rawSecretNotInHashString` |
| AC-SEC-02 | `ManagedClientControllerIT.getManagedClient_returns200WithNoSecretFields` + `ManagedClientMapperTest.toTO_doesNotExposeSecretHash` |
| AC-PMD-01 | `mvn pmd:check` passes with 0 violations |

---

## 🔗 What Phase 6 Needs From This Phase

| Artifact | Used By |
|---|---|
| All tests green | Phase 6: release sign-off prerequisite |
| JaCoCo report ≥ 80% | Phase 6: PM release checklist |
| PMD 0 violations confirmed | Phase 6: release notes |

