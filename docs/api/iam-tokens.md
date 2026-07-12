# 🎟️ IAM — Token Introspection

> **Base path:** `/api/v1/iam/tokens`  
> **Auth:** OAuth2 Bearer JWT — `Authorization: Bearer <jwt>`  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/iam/tokens` |
| Auth type | OAuth2 Bearer JWT |
| Content-Type | `application/json` |
| Response header | `X-Message` — human-readable status |

Decodes and validates a **session JWT** (issued by `POST /api/v1/session`) and returns its decoded claims. Use this for server-side validation without decoding the JWT yourself, or to extract user identity from a token.

> [!NOTE]
> This endpoint operates on **session tokens** (JJWT 0.12.3, signed with the internal keystore) — **not** OAuth2 Bearer JWTs from Supabase/Keycloak. To introspect OAuth2 tokens, call your identity provider's introspection endpoint directly.

---

## 🔵 POST /api/v1/iam/tokens/introspect

> Decode and validate a session JWT, returning its claims.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `token` | `string` | ✅ | Non-blank | The session JWT to introspect |

```json
{
  "token": "<session-jwt>"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Decoded | Token is valid — claims returned |
| `200 OK` (inactive) | Invalid | Token is expired or tampered — `active: false` |
| `400 Bad Request` | Invalid input | Token field is null or blank |

> [!TIP]
> The response always returns `200` when the request is well-formed. Check the `active` field to determine whether the token is valid. This follows the spirit of RFC 7662.

**Response `200 OK` — valid token:**
```json
{
  "active": true,
  "subject": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "issuer": "backbone-rest",
  "applicationId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "issuedAt": "2026-07-12T10:00:00Z",
  "expiresAt": "2026-07-12T11:00:00Z"
}
```

**Response `200 OK` — expired or invalid token:**
```json
{
  "active": false
}
```

**Response `400 Bad Request`:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Token must not be blank"
}
```

### Claims Reference

| Claim field | Type | Description |
|-------------|------|-------------|
| `active` | `boolean` | `true` if token signature is valid and not expired |
| `subject` | `UUID` | User ID extracted from the token |
| `issuer` | `string` | Always `backbone-rest` for session tokens |
| `applicationId` | `UUID` | Application context in which the session was created |
| `issuedAt` | `ISO-8601` | When the token was minted |
| `expiresAt` | `ISO-8601` | When the token expires (typically ~1 hour after issuance) |

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/iam/tokens/introspect \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<session-jwt>"
  }'
```

### Example — extract subject from token

```bash
SUBJECT=$(curl -k -s -X POST https://<host>:8084/api/v1/iam/tokens/introspect \
  -H "Authorization: Bearer $OAUTH2_JWT" \
  -H "Content-Type: application/json" \
  -d "{\"token\": \"$SESSION_TOKEN\"}" \
  | jq -r 'if .active then .subject else empty end')

echo "User ID: $SUBJECT"
```

---

## 🚨 Error Reference

| Status | Cause |
|--------|-------|
| `400 Bad Request` | Token field is null, blank, or missing from body |
| `401 Unauthorized` | OAuth2 Bearer JWT (the Authorization header) is invalid |

---

[← Back to API Index](./README.md)
