# 🤖 Managed Client Authentication Manager (MCAM)

> **Base path:** `/api/v1/managed-clients`  
> **Auth:** Mixed — see each endpoint  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/managed-clients` |
| Auth type | OAuth2 Bearer JWT (admin ops) · Public (token + introspect) |
| Content-Type | `application/json` |
| Response header | `X-Message` — human-readable status |

**MCAM** implements the OAuth2 **Client Credentials** flow for machine-to-machine (M2M) authentication. Register a named client, receive a one-time secret, then exchange credentials for a short-lived access token.

```
Register client → receive clientId + clientSecret (once)
   → POST /token with credentials → access_token
   → use access_token as Bearer on downstream APIs
   → rotate secret when needed
```

> [!WARNING]
> The `clientSecret` is shown **only once**: at registration or after rotation. Save it to a secrets manager immediately. There is no way to retrieve it again.

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
| `name` | `string` | ✅ | Unique per application | Human-readable client name |
| `applicationId` | `UUID` | ✅ | Valid UUID | Application this client belongs to |
| `scopes` | `string[]` | ✅ | Non-empty array | OAuth2 scopes this client may request |
| `active` | `boolean` | — | Default `true` | Whether the client can authenticate |

```json
{
  "name": "payment-service",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scopes": ["read:payments", "write:payments"],
  "active": true
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Registered | Client created — secret included once |
| `400 Bad Request` | Invalid input | Missing required fields |
| `409 Conflict` | Duplicate | Client name already registered for this application |

**Response `201 Created`:**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "payment-service",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "clientSecret": "***",
  "scopes": ["read:payments", "write:payments"],
  "active": true,
  "createdAt": "2026-07-12T12:00:00Z"
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

> List all registered M2M clients with optional filters and pagination.

**Auth: OAuth2 Bearer JWT required**

> [!NOTE]
> `clientSecret` is **never** included in list or detail responses. Only the initial registration and secret rotation responses include the secret.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 🔍 Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `applicationId` | `UUID` | — | Filter by application |
| `active` | `boolean` | — | `true` = active only, `false` = inactive only |
| `page` | `integer` | `0` | Zero-based page index |
| `size` | `integer` | `20` | Page size |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Paginated client list |
| `204 No Content` | Empty | No clients match the filters |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "name": "payment-service",
      "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "scopes": ["read:payments", "write:payments"],
      "active": true,
      "createdAt": "2026-07-12T12:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
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
| `404 Not Found` | Not found | No client with that ID |

**Response `200 OK`:**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "payment-service",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scopes": ["read:payments", "write:payments"],
  "active": true,
  "createdAt": "2026-07-12T12:00:00Z"
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

> Update a managed client's name, scopes, or active flag.

**Auth: OAuth2 Bearer JWT required**

> [!NOTE]
> To change the client secret, use `POST /{clientId}/rotate-secret` instead.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client to update |

### 📦 Request Body

Same structure as the register body (full replacement).

```json
{
  "name": "payment-service-v2",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scopes": ["read:payments", "write:payments", "admin:refunds"],
  "active": true
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Client metadata updated |
| `404 Not Found` | Not found | No client with that ID |

### 💡 Example

```bash
curl -k -s -X PUT \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "payment-service-v2",
    "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "scopes": ["read:payments", "write:payments", "admin:refunds"],
    "active": true
  }'
```

---

## 🔴 DELETE /api/v1/managed-clients/{clientId}

> Permanently delete a managed client and all its tokens.

**Auth: OAuth2 Bearer JWT required**

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client to delete |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `204 No Content` | Deleted | Client and all tokens removed |
| `404 Not Found` | Not found | No client with that ID |

### 💡 Example

```bash
curl -k -s -X DELETE \
  https://<host>:8084/api/v1/managed-clients/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🔵 POST /api/v1/managed-clients/token

> Issue an M2M access token using Client Credentials grant.

**Auth: PUBLIC — no Authorization header required**

> [!TIP]
> This is the token issuance endpoint used by services and automation. The resulting `access_token` is used as a Bearer JWT on downstream API calls.

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `clientId` | `UUID` | ✅ | Valid UUID | The registered client's ID |
| `clientSecret` | `string` | ✅ | Non-empty | The raw client secret |
| `scope` | `string` | — | Space-separated | Requested scope(s); defaults to all registered scopes |

```json
{
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "clientSecret": "***",
  "scope": "read:payments"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Issued | Access token returned |
| `400 Bad Request` | Invalid input | Missing `clientId` or `clientSecret`, or requested scope not registered |
| `401 Unauthorized` | Bad credentials | `clientId` not found or `clientSecret` incorrect |
| `429 Too Many Requests` | Rate limited | Too many token requests in a short period |

**Response `200 OK`:**
```json
{
  "access_token": "<signed-jwt>",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read:payments"
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/managed-clients/token \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "clientSecret": "***",
    "scope": "read:payments"
  }'
```

**Store and reuse the token:**
```bash
ACCESS_TOKEN=$(curl -k -s -X POST https://<host>:8084/api/v1/managed-clients/token \
  -H "Content-Type: application/json" \
  -d "{\"clientId\":\"$CLIENT_ID\",\"clientSecret\":\"$CLIENT_SECRET\"}" \
  | jq -r '.access_token')

# Use it on a downstream call
curl -k -s https://<host>:8084/api/v1/... \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

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
| `404 Not Found` | Not found | No client with that ID |

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
  "client_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "scope": "read:payments",
  "exp": 1752327600,
  "iat": 1752324000
}
```

**Response `200 OK` — expired or invalid token:**
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

> Generate a new client secret, invalidating the previous one.

**Auth: OAuth2 Bearer JWT required**

> [!WARNING]
> The old secret is invalidated immediately after rotation (no grace period by default). Update your deployment environment with the new secret before calling this endpoint in production. Coordinate with all services using this client.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `clientId` | `UUID` | ✅ | ID of the client whose secret to rotate |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Rotated | New secret returned |
| `404 Not Found` | Not found | No client with that ID |

**Response `200 OK`:**
```json
{
  "clientId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "clientSecret": "***",
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
5. Old secret is already invalid — no rollback possible
```

---

## 🚨 Error Reference

| Status | Endpoint | Cause |
|--------|----------|-------|
| `400 Bad Request` | `POST /` | Missing required registration fields |
| `400 Bad Request` | `POST /token` | Missing `clientId` or `clientSecret`, or scope not registered |
| `401 Unauthorized` | `POST /token` | Invalid `clientId` or wrong `clientSecret` |
| `404 Not Found` | Any `/{clientId}` op | Client ID not found |
| `409 Conflict` | `POST /` | Duplicate client name in same application |
| `429 Too Many Requests` | `POST /token` | Rate limit exceeded on token issuance |

---

[← Back to API Index](./README.md)
