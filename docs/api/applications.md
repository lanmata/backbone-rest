# Application Management

> **Base path:** `/api/v1/applications`
> **Auth:** OAuth2 Bearer JWT required on all endpoints

| | |
|---|---|
| **Auth type** | OAuth2 Bearer JWT |
| **Auth header** | `Authorization: Bearer <jwt>` |
| **JWT issuer** | Supabase / Keycloak |

[← Back to API Index](./README.md)

---

## GET /api/v1/applications

> Retrieve a list of all registered application clients.

> [!NOTE]
> Applications represent logical tenants in the multi-tenant RBAC model. Each user is associated with one or more applications, and role assignments are scoped per application.

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |

### Responses

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
    "codeName": "backoffi",
    "description": "Primary backoffice web client",
    "active": true
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f01234567891",
    "name": "mobile-app",
    "codeName": "mobile_a",
    "description": "Mobile client application",
    "active": true
  }
]
```

### Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/applications \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## GET /api/v1/applications?ids={id1,id2,...}

> Retrieve a filtered list of applications by their UUIDs.

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `ids` | `UUID[]` | Yes | Comma-separated list of application UUIDs |

### Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | At least one ID matched |
| `404 Not Found` | No matches | None of the provided IDs exist |

**200 OK — Response body:**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "backoffice-web",
    "codeName": "backoffi",
    "description": "Primary backoffice web client",
    "active": true
  }
]
```

### Example

```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/applications?ids=a1b2c3d4-e5f6-7890-abcd-ef1234567890,b2c3d4e5-f6a7-8901-bcde-f01234567891" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## POST /api/v1/applications

> Register a new application client in the system.

> [!NOTE]
> The `id` field is auto-generated as a UUID v4. Callers must not set it in the request body — it will be ignored if supplied.
> The `codeName` is derived automatically from `name` (lowercased, non-alphanumeric characters replaced with `_`, truncated to **8 characters**).

> [!CAUTION]
> The `name` field is **required** and must be non-blank. A missing or blank name returns `400 Bad Request`.

### Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |
| `Content-Type` | Yes | `application/json` |

### Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `application` | `object` | Yes | Non-null | Wrapper object |
| `application.name` | `string` | Yes | Non-blank | Display name for the application |
| `application.description` | `string` | No | — | Human-readable description |
| `application.active` | `boolean` | No | — | Whether the application is active (defaults to DB default) |
| `application.serviceTypeId` | `UUID` | No | — | Link to a service type |

```json
{
  "application": {
    "name": "mobile-app",
    "description": "Mobile client application",
    "active": true
  }
}
```

### Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Application created | Registration successful |
| `400 Bad Request` | Invalid input | Null `application` object or blank `name` |
| `500 Internal Server Error` | Server error | Unexpected failure during persistence |

**201 Created — Response body:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "mobile-app",
  "codeName": "mobile_a",
  "description": "Mobile client application",
  "active": true,
  "createdDate": "2026-07-13T02:00:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated application identifier |
| `name` | `string` | Application display name |
| `codeName` | `string` | Auto-derived short code (max 8 chars) |
| `description` | `string` | Application description |
| `active` | `boolean` | Active status |
| `createdDate` | `datetime` | UTC timestamp of creation |

> [!TIP]
> Save the returned `id` — it is required as `applicationId` in login requests (`POST /api/v1/session`) and user operations.

### Response Headers

| Header | Example value |
|--------|--------------|
| `Message-header` | `Application created successfully.` |

### Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/applications \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "application": {
      "name": "mobile-app",
      "description": "Mobile client application",
      "active": true
    }
  }'
```

---

## GET /api/v1/applications/{id}

> Retrieve a single application by its UUID.

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | `UUID` | Yes | Application identifier |

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |

### Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Application exists |
| `404 Not Found` | Not found | No application matches the given ID |
| `401 Unauthorized` | Auth required | Missing or invalid Bearer token |

**200 OK — Response body:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "backoffice-web",
  "codeName": "backoffi",
  "description": "Primary backoffice web client",
  "active": true
}
```

### Response Headers

| Header | Example value |
|--------|--------------|
| `Message-header` | `Application found.` |

### Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/applications/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## PUT /api/v1/applications/{id}

> Update an existing application's metadata.

> [!NOTE]
> `codeName` is recalculated automatically from the new `name` — it cannot be set directly.

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | `UUID` | Yes | Application identifier |

### Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |
| `Content-Type` | Yes | `application/json` |

### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `application` | `object` | Yes | Wrapper object |
| `application.name` | `string` | Yes | Updated display name (non-blank) |
| `application.description` | `string` | No | Updated description |
| `application.active` | `boolean` | No | Updated active status |

```json
{
  "application": {
    "name": "Backoffice Portal v2",
    "description": "Internal management portal — v2",
    "active": true
  }
}
```

### Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Application updated successfully |
| `400 Bad Request` | Invalid input | Null `application` object or blank `name` |
| `404 Not Found` | Not found | No application matches the given ID |
| `401 Unauthorized` | Auth required | Missing or invalid Bearer token |

**200 OK — Response body:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Backoffice Portal v2",
  "codeName": "backoffi",
  "description": "Internal management portal — v2",
  "active": true
}
```

### Response Headers

| Header | Example value |
|--------|--------------|
| `Message-header` | `Application updated successfully.` |

### Example

```bash
curl -k -s -X PUT https://<host>:8084/api/v1/applications/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "application": {
      "name": "Backoffice Portal v2",
      "description": "Internal management portal — v2",
      "active": true
    }
  }'
```

---

## DELETE /api/v1/applications/{id}

> Permanently remove an application from the system.

> [!CAUTION]
> Deletion is irreversible. All `ApplicationRoleUser` associations for this application will also be removed by the database cascade rules.

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | `UUID` | Yes | Application identifier |

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |

### Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Deleted | Application removed |
| `404 Not Found` | Not found | No application matches the given ID |
| `401 Unauthorized` | Auth required | Missing or invalid Bearer token |

### Response Headers

| Header | Example value |
|--------|--------------|
| `Message-header` | `Application deleted successfully.` |

### Example

```bash
curl -k -s -X DELETE https://<host>:8084/api/v1/applications/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## Error Reference

| Status | Applies to | Cause |
|--------|-----------|-------|
| `400 Bad Request` | `POST /`, `PUT /{id}` | Null or missing `application` field, or blank `name` |
| `401 Unauthorized` | All | Missing or invalid Bearer token |
| `403 Forbidden` | All | Token valid but caller lacks required role |
| `404 Not Found` | `GET /`, `GET ?ids=`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` | Resource not found or list is empty |
| `500 Internal Server Error` | `POST /` | Unexpected persistence failure |

---

[← Back to API Index](./README.md)
