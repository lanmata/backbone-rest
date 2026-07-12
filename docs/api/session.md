# 🔑 Sessions & Authentication

> **Base path:** `/api/v1/session`  
> **Auth:** All endpoints are public — no Bearer token required

| | |
|---|---|
| **Auth type** | None (public) |
| **Token issued** | Session JWT + Refresh JWT (JJWT 0.12.3) |
| **Session TTL** | ~1 hour |
| **Refresh TTL** | ~7 days |

[← Back to API Index](./README.md)

---

## 🔵 POST /api/v1/session

> Login using an application alias and password. Returns a session token and refresh token.

> [!NOTE]
> Use this endpoint when the client identifies the user by their **alias** (username). For email-based login, use `POST /api/v1/session/token`.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Content-Type` | Yes | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `alias` | `string` | Yes | Non-blank | User's login alias (username) |
| `password` | `string` | Yes | Non-blank | User's plaintext password |
| `applicationId` | `UUID` | Yes | Valid UUID v4 | Application context for multi-tenant auth |

```json
{
  "alias": "jdoe",
  "password": "s3cr3t",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Login successful | Credentials valid |
| `401 Unauthorized` | Bad credentials | Wrong alias or password |
| `404 Not Found` | Resource not found | Application ID not found or null payload |

**200 OK — Response body:**

```json
{
  "sessionToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

> [!TIP]
> Store both tokens securely. Pass `sessionToken` as the `session-token` header on subsequent calls. Use `refreshToken` with `POST /api/v1/session/refresh` when the session expires.

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/session \
  -H "Content-Type: application/json" \
  -d '{
    "alias": "jdoe",
    "password": "s3cr3t",
    "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

---

## 🔵 POST /api/v1/session/token

> Login using an email address and password. Returns a session token and refresh token.

> [!NOTE]
> Functionally equivalent to `POST /api/v1/session` but accepts an **email** instead of an alias.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Content-Type` | Yes | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `email` | `string` | Yes | Valid email format | User's registered email address |
| `password` | `string` | Yes | Non-blank | User's plaintext password |
| `applicationId` | `UUID` | Yes | Valid UUID v4 | Application context for multi-tenant auth |

```json
{
  "email": "jdoe@example.com",
  "password": "s3cr3t",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Login successful | Credentials valid |
| `401 Unauthorized` | Bad credentials | Wrong email or password |
| `404 Not Found` | Resource not found | Application ID not found or null payload |

**200 OK — Response body:**

```json
{
  "sessionToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/session/token \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jdoe@example.com",
    "password": "s3cr3t",
    "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

---

## 🟢 GET /api/v1/session/validate

> Validate whether a session token is still active and not expired.

> [!NOTE]
> This endpoint does not renew the token. Use `GET /api/v1/session/renew` to extend the session.

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `session-token` | Yes | The session JWT to validate |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Token is valid | Session is active and not expired |
| `401 Unauthorized` | Token invalid | Expired, tampered, or missing token |

**200 OK — Response body:**

```json
true
```

> [!CAUTION]
> The response is a raw JSON boolean (`true`), not an object wrapper. Parse accordingly.

### 💡 Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/session/validate \
  -H "session-token: eyJhbGciOiJIUzUxMiJ9..."
```

---

## 🟢 GET /api/v1/session/renew

> Renew a session token before it expires. Returns a new session JWT with a refreshed expiry timestamp.

> [!TIP]
> Call this endpoint proactively (e.g., when less than 10 minutes remain) to keep the user's session alive without requiring re-authentication.

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `session-token` | Yes | The current (still-valid) session JWT |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Token renewed | New session JWT issued |
| `400 Bad Request` | Missing token | `session-token` header not present |
| `401 Unauthorized` | Token invalid | Expired or tampered token |
| `404 Not Found` | User not found | Subject in token no longer exists |
| `500 Internal Server Error` | Server error | Unexpected failure during renewal |

**200 OK — Response body:**

```json
{
  "sessionToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

> [!WARNING]
> The old `sessionToken` is not explicitly revoked server-side after renewal — it remains valid until its original expiry. Discard it client-side immediately after receiving the new one.

### 💡 Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/session/renew \
  -H "session-token: eyJhbGciOiJIUzUxMiJ9..."
```

---

## 🔵 POST /api/v1/session/refresh

> Exchange a refresh token for a fresh session token and a new refresh token.

> [!NOTE]
> Use this endpoint after the session token has expired. The refresh token has a longer TTL (~7 days). Once the refresh token expires, the user must log in again.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Content-Type` | Yes | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `refreshToken` | `string` | Yes | Non-blank, valid JWT | The refresh token received at login or previous refresh |

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Tokens refreshed | New session + refresh tokens issued |
| `400 Bad Request` | Null or malformed body | Missing `refreshToken` field |
| `401 Unauthorized` | Refresh token invalid | Expired, tampered, or revoked |
| `500 Internal Server Error` | Server error | Unexpected failure |

**200 OK — Response body:**

```json
{
  "sessionToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

> [!WARNING]
> Treat the refresh token as a secret credential. Do not log it or expose it in URLs.

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/session/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzUxMiJ9..."}'
```

---

## 🚨 Error Reference

| Status | Code | Applies to |
|--------|------|-----------|
| `400 Bad Request` | Missing/malformed body | `POST /refresh`, `GET /renew` |
| `401 Unauthorized` | Invalid credentials or token | All endpoints |
| `404 Not Found` | User or application not found | `POST /`, `POST /token`, `GET /renew` |
| `500 Internal Server Error` | Unexpected server failure | `GET /renew`, `POST /refresh` |

---

[← Back to API Index](./README.md)
