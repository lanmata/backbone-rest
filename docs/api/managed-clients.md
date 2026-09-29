# 🤖 Managed Client Authentication Manager (MCAM)

> **Base path:** `/api/v1/managed-clients`  
> **Auth:** Mixed — see each endpoint  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/managed-clients` |
| Auth type | OAuth2 Bearer JWT (admin ops) · Public (token + introspect) |
| Content-Type | `application/json` |
| Token signing | RS256, keystore alias configured via `MCAM_KEY_ALIAS` (default `backbone-rest`) |

**MCAM** implements the OAuth2 **Client Credentials** flow for machine-to-machine (M2M) authentication. Register a named client, receive a one-time secret, then exchange credentials for a short-lived access token.

```
Register client → receive clientId + clientSecret (once)
   → POST /token with credentials → accessToken
   → use accessToken as Bearer on downstream APIs
   → rotate secret when needed
```

> [!WARNING]
> The `clientSecret` is shown **only once**: at registration or after rotation. Save it to a secrets manager immediately. There is no way to retrieve it again.

> [!NOTE]
> Default runtime values (overridable via env vars in `bootstrap.yml`): access token TTL `3600s` (`MCAM_TOKEN_TTL_SECONDS`), secret-rotation grace period `300s` (`MCAM_ROTATION_GRACE_SECONDS`), token-issuance rate limit `60 requests/min per client` (`MCAM_RATE_LIMIT_RPM`).

---

## 🔵 POST /api/v1/managed-clients

> Register a new M2M client application.

**Auth: OAuth2 Bearer JWT required**

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `name` | `string` | ✅ | max 128 chars, unique per `applicationId` | Human-readable client name |
| `description` | `string` | — | max 512 chars | Optional free-text purpose |
| `applicationId` | `UUID` | ✅ | Valid UUID of an existing `Application` | Application this client belongs to |
| `scopes` | `string[]` | ✅ | min 1 item, free-form strings (no fixed enum) | OAuth2 scopes this client may request |
| `active` | `boolean` | — | Default `true` | Whether the client can authenticate |

```json
{
  "name": "payment-service",
  "description": "Payments integration for order settlement",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scopes": ["read:payments", "write:payments"],
  "active": true
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Registered | Client created — secret included once |
| `400 Bad Request` | Invalid input | Missing/invalid required fields (bean validation) |
| `409 Conflict` | Duplicate | `error: "client_conflict"` — a client with this `name` already exists for the `applicationId` |

**Response `201 Created`:**
```json
{
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "clientSecret": "***",
  "name": "payment-service",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scopes": ["read:payments", "write:payments"],
  "active": true,
  "createdAt": "2026-07-12T12:00:00Z"
}
```

**Response `409 Conflict`:**
```json
{
  "error": "client_conflict",
  "errorDescription": "A client with this name already exists for the application.",
  "clientId": null
}
```

> [!CAUTION]
> The `clientSecret` above is the **only time** it will appear. Store it in a secrets manager (HashiCorp Vault, AWS Secrets Manager, Infisical) before closing this response.

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/managed-clients \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "payment-service",
    "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "scopes": ["read:payments", "write:payments"],
    "active": true
  }'
```

---

## 🟢 GET /api/v1/managed-clients

> List registered M2M clients with optional filters and pagination.

**Auth: OAuth2 Bearer JWT required**

> [!NOTE]
> `clientSecret` is **never** included in list or detail responses. Only the initial registration and secret rotation responses include the secret.

> [!IMPORTANT]
> The response body is a **plain JSON array** of client objects — there is no pagination envelope (no `content`/`totalElements`/`totalPages`). `page`/`size` still control which slice of results is fetched server-side; there's just no way to read the total count from this response today.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 🔍 Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|--------------|
| `applicationId` | `UUID` | — | Filter by application |
| `active` | `boolean` | — | `true` = active only, `false` = inactive only |
| `page` | `integer` | `0` | Zero-based page index |
| `size` | `integer` | `20` | Page size |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | JSON array of matching clients |
| `204 No Content` | Empty | No clients match the filters |

**Response `200 OK`:**
```json
[
  {
    "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "name": "payment-service",
    "description": "Payments integration for order settlement",
    "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "scopes": ["read:payments", "write:payments"],
    "active": true,
    "createdAt": "2026-07-12T12:00:00Z",
    "lastUpdatedAt": "2026-07-12T12:00:00Z",
    "secretLastRotatedAt": null
  }
]
```

### 💡 Example

```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/managed-clients?applicationId=a1b2c3d4-e5f6-7890-abcd-ef1234567890&active=true" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟢 GET /api/v1/managed-clients/{clientId}

> Retrieve details of a single M2M client.

**Auth: OAuth2 Bearer JWT required**

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the managed client |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Client details (no secret) |
| `404 Not Found` | Not found | `error: "not_found"` — no client with that ID |

**Response `200 OK`:**
```json
{
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "payment-service",
  "description": "Payments integration for order settlement",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scopes": ["read:payments", "write:payments"],
  "active": true,
  "createdAt": "2026-07-12T12:00:00Z",
  "lastUpdatedAt": "2026-07-12T12:00:00Z",
  "secretLastRotatedAt": null
}
```

### 💡 Example

```bash
curl -k -s -X GET \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟡 PUT /api/v1/managed-clients/{clientId}

> Partially update a managed client's name, description, scopes, or active flag.

**Auth: OAuth2 Bearer JWT required**

> [!NOTE]
> This is a **partial update** — only the fields present in the body are applied; omitted fields keep their current value. `applicationId` is immutable and is always ignored, even if included in the body. To change the client secret, use `POST /{clientId}/rotate-secret` instead.

> [!TIP]
> Setting `"active": false` on a currently-active client automatically revokes all of its outstanding tokens as part of the same request (equivalent to also calling `DELETE /{clientId}/tokens`).

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client to update |

### 📦 Request Body

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | `string` | — | max 128 chars |
| `description` | `string` | — | max 512 chars |
| `scopes` | `string[]` | — | replaces the full scopes list when provided |
| `active` | `boolean` | — | — |

```json
{
  "scopes": ["read:payments", "write:payments", "admin:refunds"],
  "active": true
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Full client resource returned |
| `404 Not Found` | Not found | `error: "not_found"` — no client with that ID |

### 💡 Example

```bash
curl -k -s -X PUT \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "scopes": ["read:payments", "write:payments", "admin:refunds"]
  }'
```

---

## 🔴 DELETE /api/v1/managed-clients/{clientId}

> Permanently delete a managed client. All of its active tokens are revoked first.

**Auth: OAuth2 Bearer JWT required**

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client to delete |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `204 No Content` | Deleted | Client and all its tokens removed |
| `404 Not Found` | Not found | `error: "not_found"` — no client with that ID |

### 💡 Example

```bash
curl -k -s -X DELETE \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🔵 POST /api/v1/managed-clients/token

> Issue an M2M access token using the Client Credentials grant.

**Auth: PUBLIC — no Authorization header required**

> [!TIP]
> This is the token issuance endpoint used by services and automation. The resulting `accessToken` is used as a Bearer JWT on downstream API calls.

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `clientId` | `UUID` | ✅ | Valid UUID | The registered client's ID |
| `clientSecret` | `string` | ✅ | Non-empty | The raw client secret |
| `scopes` | `string[]` | ✅ | min 1 item, must be a subset of the client's registered scopes | Requested scope(s) for this token |

```json
{
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "clientSecret": "***",
  "scopes": ["read:payments"]
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Issued | Access token returned |
| `400 Bad Request` | `error: "invalid_scope"` | Requested scopes are not a subset of the client's registered scopes |
| `401 Unauthorized` | `error: "invalid_client"` | `clientId` not found or inactive, or `clientSecret` incorrect (current secret and rotation grace secret both checked) |
| `429 Too Many Requests` | `error: "rate_limit_exceeded"` | Token requests for this client exceeded the configured rate limit |

**Response `200 OK`:**
```json
{
  "accessToken": "<signed-rs256-jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "scopes": ["read:payments"],
  "issuedAt": "2026-07-12T12:00:00Z"
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/managed-clients/token \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "clientSecret": "***",
    "scopes": ["read:payments"]
  }'
```

**Store and reuse the token:**
```bash
ACCESS_TOKEN=$(curl -k -s -X POST https://<host>:8084/api/v1/managed-clients/token \
  -H "Content-Type: application/json" \
  -d "{\"clientId\":\"$CLIENT_ID\",\"clientSecret\":\"$CLIENT_SECRET\",\"scopes\":[\"read:payments\"]}" \
  | jq -r '.accessToken')

# Use it on a downstream call
curl -k -s https://<host>:8084/api/v1/... \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

> [!IMPORTANT]
> `SecurityConfig` currently only requires `anyRequest().authenticated()` for these tokens — there is no per-endpoint `@PreAuthorize`/`hasAuthority` check on `scopes` yet anywhere in the codebase. A valid, active M2M token grants access to any `/api/v1/**` endpoint regardless of which scopes it was issued with; `scopes` are recorded, returned by introspection, and enforced only against the client's *registered* scopes at issuance time — not against individual endpoints at call time.

---

## 🔴 DELETE /api/v1/managed-clients/{clientId}/tokens

> Revoke all active access tokens issued for a client.

**Auth: OAuth2 Bearer JWT required**

> [!TIP]
> Use this before rotating the secret, or when you suspect the secret has been compromised. Existing tokens will stop working immediately.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client whose tokens to revoke |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `204 No Content` | Revoked | All tokens invalidated |
| `404 Not Found` | Not found | `error: "not_found"` — no client with that ID |

### 💡 Example

```bash
curl -k -s -X DELETE \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901/tokens \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🔵 POST /api/v1/managed-clients/introspect

> Validate and inspect an M2M access token.

**Auth: PUBLIC — no Authorization header required**

> [!NOTE]
> Per RFC 7662, this endpoint **always returns `200 OK`**, even for invalid tokens. The `active` field indicates validity. Never rely on the HTTP status alone.

### 📦 Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `token` | `string` | ✅ | The M2M access token to introspect |

```json
{
  "token": "<m2m-access-token>"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Evaluated | Always returned for well-formed requests |

**Response `200 OK` — valid token:**
```json
{
  "active": true,
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "clientName": "payment-service",
  "scopes": ["read:payments"],
  "issuer": "backbone-rest",
  "exp": 1752327600,
  "iat": 1752324000,
  "jti": "9f1c2e3a-....-uuid"
}
```

**Response `200 OK` — expired, revoked, or malformed token:**
```json
{
  "active": false
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/managed-clients/introspect \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<m2m-access-token>"
  }'
```

---

## 🔵 POST /api/v1/managed-clients/{clientId}/rotate-secret

> Generate a new client secret. The client must currently be active.

**Auth: OAuth2 Bearer JWT required**

> [!NOTE]
> The previous secret is **not** invalidated immediately — it remains valid for `gracePeriodSeconds` (default `300s`, `MCAM_ROTATION_GRACE_SECONDS`) so in-flight deployments can pick up the new secret without a hard cutover. After the grace period, only the new secret works.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client whose secret to rotate |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Rotated | New secret returned |
| `404 Not Found` | Not found | `error: "not_found"` — no client with that ID, or the client is **inactive** |

**Response `200 OK`:**
```json
{
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "clientSecret": "***",
  "gracePeriodSeconds": 300,
  "rotatedAt": "2026-07-12T15:30:00Z"
}
```

> [!CAUTION]
> This is the only time the new secret will be shown. Save it to your secrets manager before closing this response.

### 💡 Example

```bash
curl -k -s -X POST \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901/rotate-secret \
  -H "Authorization: Bearer <oauth2-jwt>"
```

### Rotation Checklist

```
1. Retrieve new secret:   POST /{clientId}/rotate-secret
2. Update secrets manager immediately
3. Deploy new secret to all services using this client
4. Verify services authenticate successfully with new secret
5. Old secret keeps working until gracePeriodSeconds elapses — no action needed to phase it out
```

---

## 🚨 Error Reference

| Status | Endpoint | `error` code | Cause |
|--------|----------|--------------|-------|
| `400 Bad Request` | `POST /` | — | Missing/invalid registration fields (bean validation) |
| `400 Bad Request` | `POST /token` | `invalid_scope` | Requested scopes not a subset of the client's registered scopes |
| `401 Unauthorized` | `POST /token` | `invalid_client` | `clientId` not found/inactive, or wrong `clientSecret` |
| `404 Not Found` | `GET/PUT/DELETE /{clientId}`, `DELETE /{clientId}/tokens` | `not_found` | Client ID not found |
| `404 Not Found` | `POST /{clientId}/rotate-secret` | `not_found` | Client ID not found **or inactive** |
| `409 Conflict` | `POST /` | `client_conflict` | Duplicate client `name` in the same application |
| `429 Too Many Requests` | `POST /token` | `rate_limit_exceeded` | Token issuance rate limit exceeded for this client |

---

[← Back to API Index](./README.md)
