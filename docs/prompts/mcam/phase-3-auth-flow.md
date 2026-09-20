# 🔐 MCAM Phase 3 — Authentication Flow

> **Prompt for:** `developer` agent  
> **Depends on:** Phase 1 complete + Phase 2 complete  
> **Estimated duration:** 4–5 days  
> **Complexity:** High  
> **Reference:** `docs/plans/mcam-implementation-plan.md` §4

---

## 🎯 Goal

Implement the full M2M authentication flow:
- Token issuance (`POST /api/v1/managed-clients/token`)
- All-token revocation (`DELETE /api/v1/managed-clients/{clientId}/tokens`)
- Token introspection (`POST /api/v1/managed-clients/introspect`)
- `ManagedClientTokenFilter` — validates M2M JWTs in the Spring Security filter chain
- Guard in `SessionJwtAuthenticationFilter` — skips if M2M filter already authenticated

---

## 📐 Architecture Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| AC-01 | `ManagedClientTokenFilter` is registered **BEFORE** `SessionJwtAuthenticationFilter` in `SecurityConfig` |
| AC-02 | `SessionJwtAuthenticationFilter` must skip processing if `SecurityContextHolder.getContext().getAuthentication() != null` |
| AC-03 | JWT type claim `"M2M"` is the discriminator — tokens without this claim are passed to the session filter |
| AC-04 | RS256 signing via JJWT 0.12.6 (already in `pom.xml`) — use `KeystoreUtil` for key loading |
| AC-05 | No KEYS command in Redis — use per-client token index set: `mcam:client-tokens:{clientId}` |
| AC-06 | Introspection always returns `200` — `active=false` for bad/revoked/expired tokens (never `401` from introspect) |
| AC-07 | Constant-time secret comparison — `ManagedClientSecretHashService.matchesWithConstantTime()` for ALL secret checks |
| AC-08 | PMD zero violations — constructor injection, no field `@Autowired` |

---

## 📋 Tasks

### TASK 3.1 — ManagedClientRedisService Interface

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRedisService.java`

```java
/// Stores a new active M2M token entry in Redis.
void storeToken(String jti, UUID clientId, long ttlSeconds);

/// Marks a token JTI as revoked with a tombstone TTL.
void revokeToken(String jti, long tombstoneTtlSeconds);

/// Returns true if the given JTI has a revocation tombstone.
boolean isRevoked(String jti);

/// Returns true if the given JTI has an active (non-expired) token entry.
boolean isActive(String jti);

/// Returns all JTI values currently tracked for a client.
Set<String> getClientTokens(UUID clientId);

/// Stores the previous secret hash during the rotation grace period.
void storeGraceSecret(UUID clientId, String prevHash, long graceTtlSeconds);

/// Retrieves the grace-period secret hash for a client, if present.
Optional<String> getGraceSecret(UUID clientId);

/// Removes the grace-period secret hash for a client (used after grace expires or next rotation).
void removeGraceSecret(UUID clientId);

/// Increments the rate-limit counter for a client (sliding 60s window).
void incrementRateLimit(UUID clientId);

/// Returns the current rate-limit counter value for a client.
long getRateLimitCount(UUID clientId);
```

---

### TASK 3.2 — ManagedClientRedisServiceImpl

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientRedisServiceImpl.java`

- `@Service`
- Constructor: `StringRedisTemplate` (already configured in `RedisConfig` — inject, do not create)
- SLF4J logger

**Redis key schema (use private constants):**

```java
private static final String TOKEN_KEY        = "mcam:token:%s";           // %s = jti
private static final String REVOKED_KEY      = "mcam:revoked:%s";         // %s = jti
private static final String CLIENT_TOKENS    = "mcam:client-tokens:%s";   // %s = clientId
private static final String GRACE_KEY        = "mcam:grace:%s";           // %s = clientId
private static final String RATE_LIMIT_KEY   = "mcam:ratelimit:%s";       // %s = clientId
```

**Method implementations:**

```
storeToken(jti, clientId, ttlSeconds):
  SET mcam:token:{jti} = clientId.toString()  with EXPIRE ttlSeconds
  SADD mcam:client-tokens:{clientId} = jti
  (Note: individual jti set members expire with the token; set itself has no TTL)

revokeToken(jti, tombstoneTtlSeconds):
  SET mcam:revoked:{jti} = "1"  with EXPIRE tombstoneTtlSeconds
  DEL mcam:token:{jti}

isRevoked(jti):
  EXIST mcam:revoked:{jti} → return true if exists

isActive(jti):
  EXIST mcam:token:{jti} → return true if exists (AND not revoked — callers check both)

getClientTokens(clientId):
  SMEMBERS mcam:client-tokens:{clientId}

storeGraceSecret(clientId, prevHash, graceTtlSeconds):
  SET mcam:grace:{clientId} = prevHash  with EXPIRE graceTtlSeconds

getGraceSecret(clientId):
  GET mcam:grace:{clientId} → Optional.ofNullable(result)

removeGraceSecret(clientId):
  DEL mcam:grace:{clientId}

incrementRateLimit(clientId):
  INCR mcam:ratelimit:{clientId}
  If result == 1: SET EXPIRE 60  (set TTL only on first increment of new window)

getRateLimitCount(clientId):
  GET mcam:ratelimit:{clientId} → parseLong(result) or 0L if null
```

---

### TASK 3.3 — ManagedClientTokenService Interface

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientTokenService.java`

```java
/// Issues an M2M access token after validating clientId + clientSecret + scopes.
ResponseEntity<?> issueToken(ManagedClientTokenRequest request, String clientIp);

/// Revokes all active tokens for the specified client.
ResponseEntity<?> revokeAllTokens(UUID clientId);

/// Introspects a raw M2M JWT. Always returns 200; active=false for invalid tokens.
ResponseEntity<?> introspectToken(String rawToken);

/// Used by ManagedClientTokenFilter to check if a JTI is currently active.
boolean isTokenActive(String jti);
```

---

### TASK 3.4 — ManagedClientTokenServiceImpl

#### `src/main/java/com/umdc/backoffice/v1/managedclient/service/ManagedClientTokenServiceImpl.java`

- `@Service`
- Constructor: `ManagedClientRepository`, `ManagedClientSecretHashService`, `ManagedClientAuditService`, `ManagedClientRedisService`, `ManagementAuthenticatorProperties`, `KeystoreUtil`

#### `issueToken` logic:

```
1. Rate-limit check:
   long count = redisService.getRateLimitCount(clientId)
   if count >= properties.getRateLimitRpm() → return 429 ManagedClientErrorResponse("rate_limit_exceeded", ...)

2. Load client: repository.findByIdAndActiveTrue(request.clientId())
   → 401 ManagedClientErrorResponse("invalid_client", "Client not found or inactive") if empty
   (constant-time path: do NOT reveal whether client exists vs. inactive)

3. Secret validation (constant-time):
   boolean currentMatch = secretHashService.matchesWithConstantTime(request.clientSecret(), entity.secretHash())
   boolean graceMatch   = false
   Optional<String> graceHash = redisService.getGraceSecret(entity.id())
   if (graceHash.isPresent()) {
       graceMatch = secretHashService.matchesWithConstantTime(request.clientSecret(), graceHash.get())
   }
   if (!currentMatch && !graceMatch) {
       auditService.record(entity.id(), CLIENT_TOKEN_ISSUE_FAILED, clientIp, "FAILURE", "bad_credentials")
       return 401 ManagedClientErrorResponse("invalid_client", "Invalid credentials")
   }

4. Scope validation:
   if (!entity.scopes().containsAll(request.scopes())) {
       auditService.record(entity.id(), CLIENT_TOKEN_ISSUE_FAILED, clientIp, "FAILURE", "invalid_scope")
       return 400 ManagedClientErrorResponse("invalid_scope",
               "Requested scopes exceed registered scopes. Allowed: " + entity.scopes())
   }

5. Rate-limit increment (after validation):
   redisService.incrementRateLimit(entity.id())

6. Build JWT (JJWT 0.12.6, RS256):
   String jti = UUID.randomUUID().toString()
   Instant now = Instant.now()
   Instant exp = now.plusSeconds(properties.getTokenTtlSeconds())
   PrivateKey privateKey = keystoreUtil.loadPrivateKey(properties.getKeyAlias(), ...)
   String token = Jwts.builder()
       .subject(entity.id().toString())
       .issuer("backbone-rest")
       .audience().add(request.scopes()).and()
       .issuedAt(Date.from(now))
       .expiration(Date.from(exp))
       .id(jti)
       .claim("client_name", entity.name())
       .claim("scopes", request.scopes())
       .claim("type", "M2M")
       .signWith(privateKey, Jwts.SIG.RS256)
       .compact()

7. Store in Redis:
   redisService.storeToken(jti, entity.id(), properties.getTokenTtlSeconds())

8. Emit audit:
   auditService.record(entity.id(), CLIENT_TOKEN_ISSUED, clientIp, "SUCCESS", null)

9. Return 200 ManagedClientTokenResponse(token, "Bearer", properties.getTokenTtlSeconds(),
                                         request.scopes(), now)
```

#### `revokeAllTokens` logic:

```
1. Set<String> jtis = redisService.getClientTokens(clientId)
2. For each jti:
   long tombstoneTtl = properties.getTokenTtlSeconds() + 60
   redisService.revokeToken(jti, tombstoneTtl)
3. auditService.record(clientId, CLIENT_TOKEN_REVOKED, null, "SUCCESS", "count=" + jtis.size())
4. return ResponseEntity.noContent().build()  // 204
```

#### `introspectToken` logic:

```
// RULE: This method NEVER throws — all exceptions → active=false

1. Parse JWT header to read "type" claim WITHOUT signature verification:
   try {
       // Use JJWT unsecured parser or parse header+payload as Base64
       // Check type claim == "M2M"
       // If type != "M2M" → return active=false (not an M2M token)
   } catch (Exception e) { return 200 active=false }

2. Try full signature verification:
   try {
       PublicKey publicKey = keystoreUtil.loadPublicKey(properties.getKeyAlias(), ...)
       Claims claims = Jwts.parser().verifyWith(publicKey).build()
                          .parseSignedClaims(rawToken).getPayload()
       String jti = claims.getId()
       
       // Revocation check
       if (redisService.isRevoked(jti)) return 200 active=false
       
       // Active check
       if (!redisService.isActive(jti)) return 200 active=false
       
       // Expiry check (JJWT throws ExpiredJwtException — but we already caught it below)
       
       auditService.record(UUID.fromString(claims.getSubject()),
                           CLIENT_INTROSPECTION_CALLED, null, "SUCCESS", null)
       
       return 200 ManagedClientTokenIntrospectResponse(
           true, claims.getSubject(),
           (String) claims.get("client_name"),
           (List<String>) claims.get("scopes"),
           claims.getIssuer(),
           claims.getExpiration().getTime() / 1000,
           claims.getIssuedAt().getTime() / 1000,
           jti
       )
   } catch (ExpiredJwtException e) {
       return 200 active=false (nulls for all other fields)
   } catch (Exception e) {
       return 200 active=false
   }
```

#### `isTokenActive` logic (used by filter):

```java
return redisService.isActive(jti) && !redisService.isRevoked(jti);
```

---

### TASK 3.5 — ManagedClientTokenFilter

#### `src/main/java/com/umdc/backoffice/security/filter/ManagedClientTokenFilter.java`

```
Extends     : OncePerRequestFilter
Annotations : @Component
Constructor : ManagedClientTokenService, ManagementAuthenticatorProperties, KeystoreUtil
```

#### `doFilterInternal` logic:

```
1. Extract Authorization header:
   String header = request.getHeader("Authorization")
   if (header == null || !header.startsWith("Bearer ")) {
       filterChain.doFilter(request, response); return
   }
   String rawToken = header.substring(7)

2. Peek at JWT "type" claim WITHOUT signature verification:
   try {
       // Decode payload (Base64URL, middle segment) → parse JSON → read "type"
       String typeClaim = parseTypeClaim(rawToken)  // helper method
       if (!"M2M".equals(typeClaim)) {
           filterChain.doFilter(request, response); return
       }
   } catch (Exception e) {
       filterChain.doFilter(request, response); return
   }

3. Validate M2M token:
   try {
       PublicKey publicKey = keystoreUtil.loadPublicKey(properties.getKeyAlias(), ...)
       Claims claims = Jwts.parser().verifyWith(publicKey).build()
                          .parseSignedClaims(rawToken).getPayload()
       String jti = claims.getId()
       
       if (!managedClientTokenService.isTokenActive(jti)) {
           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
           response.setContentType("application/json")
           response.getWriter().write("{\"error\":\"invalid_token\",\"error_description\":\"Token has been revoked\"}")
           return
       }
       
       // Build authentication
       List<String> scopes = (List<String>) claims.get("scopes")
       List<SimpleGrantedAuthority> authorities = scopes.stream()
           .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
           .toList()
       UsernamePasswordAuthenticationToken auth =
           new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities)
       SecurityContextHolder.getContext().setAuthentication(auth)
       
   } catch (ExpiredJwtException e) {
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
       response.setContentType("application/json")
       response.getWriter().write("{\"error\":\"invalid_token\",\"error_description\":\"Token expired\"}")
       return
   } catch (JwtException | IllegalArgumentException e) {
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
       response.setContentType("application/json")
       response.getWriter().write("{\"error\":\"invalid_token\",\"error_description\":\"Invalid token\"}")
       return
   }

4. filterChain.doFilter(request, response)
```

> **Helper method** `parseTypeClaim(String rawToken)`: Split on `.`, take index 1, Base64URL-decode, parse JSON, return `types` field value. Use Jackson `ObjectMapper` or `org.json` — whichever is already on the classpath.

---

### TASK 3.6 — SessionJwtAuthenticationFilter Guard

#### Modify: `src/main/java/com/umdc/backoffice/security/filter/SessionJwtAuthenticationFilter.java`

Add **at the very beginning** of `doFilterInternal`, before any existing logic:

```java
/// If ManagedClientTokenFilter has already authenticated this request,
/// skip session-token processing to avoid double authentication.
if (SecurityContextHolder.getContext().getAuthentication() != null) {
    filterChain.doFilter(request, response);
    return;
}
```

> ⚠️ Read the full `SessionJwtAuthenticationFilter` source before editing. Understand the existing flow. The guard goes at the start of `doFilterInternal` only — do **not** alter any other logic.

---

### TASK 3.7 — SecurityConfig Filter Chain Update

#### Modify: `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java`

1. Add `ManagedClientTokenFilter` as a constructor parameter (constructor injection):
   ```java
   private final ManagedClientTokenFilter managedClientTokenFilter;
   ```

2. In the `SecurityFilterChain` bean, register the M2M filter BEFORE the session filter:
   ```java
   .addFilterBefore(managedClientTokenFilter, SessionJwtAuthenticationFilter.class)
   ```

   The existing session filter registration line should remain unchanged.

> ⚠️ Read `SecurityConfig.java` fully before editing. Modify only the constructor parameter list and the filter chain `addFilterBefore` call. Do **not** touch `@Bean` signatures, OAuth2 resource-server config, or CORS config.

---

### TASK 3.8 — Token Endpoints in ManagedClientApi

#### Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientApi.java`

Append the following method declarations to the interface:

```java
@Operation(summary = "Issue an M2M access token",
           description = "Public endpoint. Validates clientId + clientSecret + requested scopes.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Token issued"),
    @ApiResponse(responseCode = "400", description = "Invalid scope"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive client"),
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
})
@PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE,
             consumes = MediaType.APPLICATION_JSON_VALUE)
default ResponseEntity<?> issueManagedClientToken(
        @Valid @RequestBody ManagedClientTokenRequest request,
        HttpServletRequest httpRequest) {
    String clientIp = httpRequest.getRemoteAddr();
    return ((ManagedClientController) this).getManagedClientTokenService()
            .issueToken(request, clientIp);
}

@Operation(summary = "Revoke all active tokens for a client")
@ApiResponses({
    @ApiResponse(responseCode = "204", description = "All tokens revoked"),
    @ApiResponse(responseCode = "404", description = "Client not found")
})
@DeleteMapping(value = "/{clientId}/tokens")
default ResponseEntity<?> revokeAllClientTokens(@PathVariable UUID clientId) {
    return ((ManagedClientController) this).getManagedClientTokenService()
            .revokeAllTokens(clientId);
}

@Operation(summary = "Introspect an M2M token",
           description = "Public endpoint. Always returns 200. active=false for invalid tokens.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Introspection result")
})
@PostMapping(value = "/introspect", produces = MediaType.APPLICATION_JSON_VALUE,
             consumes = MediaType.APPLICATION_JSON_VALUE)
default ResponseEntity<?> introspectManagedClientToken(
        @RequestBody ManagedClientTokenIntrospectResponse request) {
    // Note: introspect takes raw token as plain body string or a simple wrapper DTO
    // Adjust based on actual TokenIntrospectRequest DTO design
    return ((ManagedClientController) this).getManagedClientTokenService()
            .introspectToken(/* extract raw token from request */);
}
```

> Adjust the `introspectToken` call to accept the raw token string. Create a simple `TokenIntrospectRequest` record with a single `token` (String) field if one doesn't already exist in the `iam/tokens` domain.

---

### TASK 3.9 — ManagedClientController Token Methods

#### Modify: `src/main/java/com/umdc/backoffice/v1/managedclient/api/controller/ManagedClientController.java`

1. Add `ManagedClientTokenService` constructor parameter
2. Expose package-visible accessor `getManagedClientTokenService()`
3. Override all three new methods: `issueManagedClientToken`, `revokeAllClientTokens`, `introspectManagedClientToken`

---

## ✅ Phase 3 Quality Gate Checklist

```bash
mvn -DskipTests compile
mvn pmd:check pmd:cpd-check
mvn test
```

- [ ] `mvn -DskipTests compile` — exits 0
- [ ] `mvn pmd:check` — 0 violations
- [ ] `POST /token` with valid credentials → `200` JWT with `type=M2M`, `sub`, `iss`, `aud`, `exp`, `jti`, `scopes`, `client_name` claims (AC-TOK-01)
- [ ] `POST /token` with wrong `clientSecret` → `401 {"error":"invalid_client"}` (AC-TOK-02)
- [ ] `POST /token` for inactive client → `401 {"error":"invalid_client"}` (AC-TOK-03)
- [ ] `POST /token` with out-of-range scope → `400 {"error":"invalid_scope"}` with allowed scopes listed (AC-TOK-04)
- [ ] `DELETE /{clientId}/tokens` → `204`; subsequent `POST /introspect` → `active=false` (AC-REV-01)
- [ ] `POST /introspect` on valid token → `200 active=true` with full claims (AC-INT-01)
- [ ] `POST /introspect` on expired or revoked token → `200 active=false` (AC-INT-02)
- [ ] Existing session-token authentication still works for non-M2M requests (filter chain guard)
- [ ] `ManagedClientTokenFilter` does not interfere with `/api/v1/session` endpoints

---

## 🔗 What Phase 4 Needs From This Phase

| Artifact | Used By |
|---|---|
| `ManagedClientTokenService.revokeAllTokens()` | Phase 4: wired into `updateClient` (deactivate) + `deleteClient` |
| `ManagedClientRedisService.storeGraceSecret()` / `getGraceSecret()` | Phase 4: `ManagedClientRotationServiceImpl` |
| `ManagedClientTokenFilter` | Phase 4: confirmed working before rotation/audit testing |

