# 🔒 IAM — Permission Check

> **Base path:** `/api/v1/iam/permissions`  
> **Auth:** OAuth2 Bearer JWT — `Authorization: Bearer <jwt>`  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/iam/permissions` |
| Auth type | OAuth2 Bearer JWT |
| Content-Type | `application/json` |
| Response header | `X-Message` — human-readable status |

This endpoint evaluates whether a **session token** holder has a specific named permission (Feature). It decodes the session JWT to extract user identity, then walks the user's Role → Feature assignments.

> [!NOTE]
> There are two token types in this system:
> - **OAuth2 Bearer JWT** (from Supabase/Keycloak) — used in the `Authorization` header to authenticate the API call itself.
> - **Session JWT** (from `POST /api/v1/session`) — passed in the request **body** as `sessionToken` to identify the user being checked.
>
> Both must be present on this endpoint.

---

## 🔵 POST /api/v1/iam/permissions/check

> Check whether a session has a named feature permission.

### How It Works

When `applicationId` is provided **and** the session token carries a resolvable `uid` claim, the check is
**application-scoped** — the authoritative path, since a user holds at most one role per application
(`general.application_role_user`, unique on `(user_id, application_id)`):

```
sessionToken → decode uid
→ general.application_role_user.findByUserAndApplication(uid, applicationId)
→ role must be active
→ match permission == role.name, or permission == name of one of role's active granted features
→ return { granted, permission, reason }
```

Otherwise (no `applicationId`, or the token has no `uid` claim — e.g. a token minted before this claim was
added), it falls back to the **global** check against the token's flattened `roles` claim, which is not
scoped to any single application:

```
sessionToken → decode roles claim (cross-application, flattened)
→ match permission ∈ roles
→ return { granted, permission, reason }
```

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `sessionToken` | `string` | ✅ | Valid session JWT | The session JWT to evaluate — this service's own, or any signed with the same shared `APP_TOKEN_SECRET` (e.g. Mercury's) |
| `permission` | `string` | ✅ | Non-empty | Role name or feature name to check (e.g. `EXPORT_REPORTS`, `TEMPLATE_MANAGE`) |
| `applicationId` | `string` (uuid) | – | Valid UUID | Application scope. When set and the token carries a resolvable `uid`, enforces the check against `general.application_role_user` for that specific `(user, application)` pair instead of the token's cross-application `roles` claim |

```json
{
  "sessionToken": "<session-jwt>",
  "permission": "TEMPLATE_MANAGE",
  "applicationId": "33333333-3333-3333-3333-333333333333"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Evaluated | Check completed — inspect `granted` field |
| `400 Bad Request` | Invalid input | Null or malformed request body |
| `401 Unauthorized` | Invalid token | Session token is expired, malformed, or tampered |

> [!TIP]
> A `200` response does **not** mean the permission was granted. Always read the `granted` boolean field to determine the result.

**Response `200 OK` — permission granted:**
```json
{
  "granted": true,
  "permission": "EXPORT_REPORTS",
  "reason": "Permission granted"
}
```

**Response `200 OK` — permission denied (no active role in the requested application):**
```json
{
  "granted": false,
  "permission": "EXPORT_REPORTS",
  "reason": "User has no active role in this application"
}
```

**Response `400 Bad Request`:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Request body must not be null"
}
```

**Response `401 Unauthorized`:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Session token is invalid or expired"
}
```

### Example — check granted permission

```bash
curl -k -s -X POST https://<host>:8084/api/v1/iam/permissions/check \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionToken": "<session-jwt>",
    "permission": "EXPORT_REPORTS"
  }'
```

### Example — programmatic usage pattern

```bash
RESULT=$(curl -k -s -X POST https://<host>:8084/api/v1/iam/permissions/check \
  -H "Authorization: Bearer $OAUTH2_JWT" \
  -H "Content-Type: application/json" \
  -d "{\"sessionToken\": \"$SESSION_TOKEN\", \"permission\": \"MANAGE_USERS\"}")

GRANTED=$(echo "$RESULT" | jq -r '.granted')
if [ "$GRANTED" = "true" ]; then
  echo "Access allowed"
else
  echo "Access denied"
fi
```

---

## 🚨 Error Reference

| Status | Cause |
|--------|-------|
| `400 Bad Request` | Null body, missing `sessionToken`, or missing `permission` |
| `401 Unauthorized` | Session JWT is expired, invalid signature, or malformed |

---

[← Back to API Index](./README.md)
