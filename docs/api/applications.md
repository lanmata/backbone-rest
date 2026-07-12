# 📱 Application Management

> **Base path:** `/api/v1/applications`  
> **Auth:** OAuth2 Bearer JWT required on all endpoints

| | |
|---|---|
| **Auth type** | OAuth2 Bearer JWT |
| **Auth header** | `Authorization: Bearer <jwt>` |
| **JWT issuer** | Supabase / Keycloak |

[← Back to API Index](./README.md)

---

## 🟢 GET /api/v1/applications

> Retrieve a list of all registered application clients.

> [!NOTE]
> Applications represent logical tenants in the multi-tenant RBAC model. Each user is associated with one or more applications, and role assignments are scoped per application.

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | One or more applications exist |
| `404 Not Found` | No applications | No application records in the system |

**200 OK — Response body:**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "backoffice-web",
    "description": "Primary backoffice web client"
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f01234567891",
    "name": "mobile-app",
    "description": "Mobile client application"
  }
]
```

### 💡 Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/applications \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🔵 POST /api/v1/applications

> Register a new application client in the system.

> [!NOTE]
> The `id` field is auto-generated as a UUID v4. Callers must not set it in the request body — it will be ignored if supplied.

> [!CAUTION]
> Application names must be **unique** across the system. Attempting to register a duplicate name returns `400 Bad Request`.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |
| `Content-Type` | Yes | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `application` | `object` | Yes | Non-null | Wrapper object |
| `application.name` | `string` | Yes | Non-blank, unique | Unique identifier name for the application |
| `application.description` | `string` | No | Max 255 chars | Human-readable description |

```json
{
  "application": {
    "name": "mobile-app",
    "description": "Mobile client application"
  }
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Application created | Registration successful |
| `400 Bad Request` | Invalid input | Null body, null `application` object, or blank/duplicate name |
| `500 Internal Server Error` | Server error | Unexpected failure during persistence |

**200 OK — Response body:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "mobile-app",
  "description": "Mobile client application"
}
```

> [!TIP]
> Save the returned `id` — it is required as `applicationId` in login requests (`POST /api/v1/session`) and user operations.

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/applications \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "application": {
      "name": "mobile-app",
      "description": "Mobile client application"
    }
  }'
```

---

## 🚨 Error Reference

| Status | Applies to | Cause |
|--------|-----------|-------|
| `400 Bad Request` | `POST /` | Null body, null `application` field, or duplicate name |
| `401 Unauthorized` | Both | Missing or invalid Bearer token |
| `403 Forbidden` | Both | Token valid but caller lacks required role |
| `404 Not Found` | `GET /` | No applications registered in the system |
| `500 Internal Server Error` | `POST /` | Unexpected persistence failure |

---

[← Back to API Index](./README.md)
