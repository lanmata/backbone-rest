# 🛡️ Roles API

> **Base path:** `/api/v1/roles`  
> **[← Back to API Index](./README.md)**

| Auth type | Header | Required on |
|-----------|--------|-------------|
| OAuth2 Bearer JWT | `Authorization: Bearer <jwt>` | All endpoints |

Roles are the RBAC building block. Each Role groups one or more Features (capabilities) and is assigned to a User within a specific Application, enabling multi-tenant permission management. Roles can be active or inactive.

---

## 🟢 GET /api/v1/roles

> List every registered role regardless of active status.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | One or more roles exist |
| `404 Not Found` | Empty | No roles registered |

### 💡 Example

```bash
curl -k -X GET https://<host>:8084/api/v1/roles \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "ADMIN",
    "description": "Full access administrator",
    "active": true,
    "features": [
      {
        "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
        "name": "EXPORT_REPORTS",
        "active": true
      }
    ]
  },
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "name": "VIEWER",
    "description": "Read-only access",
    "active": false,
    "features": []
  }
]
```

---

## 🟢 GET /api/v1/roles/{includeInactive}

> List roles filtered by active status.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `includeInactive` | `boolean` | Yes | `true` — return all roles (active + inactive); `false` — active only |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Matching roles found |
| `404 Not Found` | Empty | No roles match the filter |

### 💡 Example

```bash
# Active roles only
curl -k -X GET https://<host>:8084/api/v1/roles/false \
  -H "Authorization: Bearer <jwt>"

# All roles including inactive
curl -k -X GET https://<host>:8084/api/v1/roles/true \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "ADMIN",
    "description": "Full access administrator",
    "active": true,
    "features": []
  }
]
```

---

## 🟢 GET /api/v1/roles/{includeInactive}/{roleIds}

> List a specific set of roles by their IDs, with optional status filter.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `includeInactive` | `boolean` | Yes | `true` — include inactive roles in results; `false` — active only |
| `roleIds` | `string` | Yes | Comma-separated UUID list, e.g. `uuid1,uuid2,uuid3` |

> [!NOTE]
> `roleIds` is a single path segment containing comma-separated values. URL-encode commas if required by your HTTP client: `%2C`.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | At least one matching role found |
| `404 Not Found` | Empty | No roles match the given IDs and status |

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/roles/true/a1b2c3d4-e5f6-7890-abcd-ef1234567890,c3d4e5f6-a7b8-9012-cdef-123456789012" \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "ADMIN",
    "description": "Full access administrator",
    "active": true,
    "features": []
  }
]
```

---

## 🟢 GET /api/v1/roles/find/{roleId}

> Retrieve a single role by its UUID, including its full feature list.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | `UUID` | Yes | The role's unique identifier |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Role found |
| `404 Not Found` | Missing | No role with the given ID |

### 💡 Example

```bash
curl -k -X GET \
  https://<host>:8084/api/v1/roles/find/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "ADMIN",
  "description": "Full access administrator",
  "active": true,
  "features": [
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "name": "EXPORT_REPORTS",
      "description": "Allows exporting reports",
      "active": true
    },
    {
      "id": "d4e5f6a7-b8c9-0123-defa-234567890123",
      "name": "MANAGE_USERS",
      "description": "Create and delete users",
      "active": true
    }
  ]
}
```

---

## 🟢 GET /api/v1/roles/user/{userId}

> List all roles currently assigned to a specific user.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `UUID` | Yes | The user's unique identifier |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | User has one or more roles |
| `404 Not Found` | Missing | User not found or has no roles assigned |

### 💡 Example

```bash
curl -k -X GET \
  https://<host>:8084/api/v1/roles/user/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "name": "EDITOR",
    "description": "Content editor",
    "active": true,
    "features": []
  }
]
```

---

## 🔵 POST /api/v1/roles/

> Create a new role, optionally linking it to existing features.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 📋 Request Headers

| Header | Value |
|--------|-------|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <jwt>` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `role.name` | `string` | Yes | Unique across all roles | Role identifier, e.g. `ADMIN` |
| `role.description` | `string` | No | — | Human-readable description |
| `role.active` | `boolean` | No | Default: `true` | Whether the role is active |
| `role.features` | `array` | No | Each item must have a valid `id` | Feature references to link to this role |
| `role.features[].id` | `UUID` | Yes (if features provided) | Must exist in the database | Feature UUID — only `id` is needed; the service resolves the rest |

```json
{
  "role": {
    "name": "ADMIN",
    "description": "Full access administrator",
    "active": true,
    "features": [
      { "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901" },
      { "id": "d4e5f6a7-b8c9-0123-defa-234567890123" }
    ]
  }
}
```

> [!NOTE]
> Feature references only need the `id` field. The service resolves the full Feature object from the database. Passing non-existent feature IDs may result in a `422 Unprocessable Entity`.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Success | Role created successfully |
| `400 Bad Request` | Invalid input | Null or missing request body |
| `422 Unprocessable Entity` | Mapping failure | MapStruct returned null (e.g. invalid feature reference) |

### 💡 Example

```bash
curl -k -X POST https://<host>:8084/api/v1/roles/ \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "role": {
      "name": "EDITOR",
      "description": "Content editor role",
      "active": true,
      "features": [
        { "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901" }
      ]
    }
  }'
```

**Response `201 Created`**

```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "name": "EDITOR",
  "description": "Content editor role",
  "active": true,
  "features": [
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "name": "EXPORT_REPORTS",
      "active": true
    }
  ]
}
```

---

## 🟡 PUT /api/v1/roles/{roleId}

> Fully replace an existing role's data, including its feature links.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | `UUID` | Yes | The role to update |

### 📋 Request Headers

| Header | Value |
|--------|-------|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <jwt>` |

### 📦 Request Body

Same structure as `POST /api/v1/roles/`. All fields are replaced on the existing record.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `role.name` | `string` | Yes | Unique across all roles | Updated role name |
| `role.description` | `string` | No | — | Updated description |
| `role.active` | `boolean` | No | Default: `true` | Updated active flag |
| `role.features` | `array` | No | Each item needs a valid `id` | Replaces the current feature list |

```json
{
  "role": {
    "name": "SUPER_ADMIN",
    "description": "Full system access",
    "active": true,
    "features": [
      { "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901" },
      { "id": "d4e5f6a7-b8c9-0123-defa-234567890123" }
    ]
  }
}
```

> [!WARNING]
> This is a **full replacement**. Features not included in the request body will be unlinked from the role.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Role updated |
| `404 Not Found` | Missing | No role with the given ID |

### 💡 Example

```bash
curl -k -X PUT \
  https://<host>:8084/api/v1/roles/e5f6a7b8-c9d0-1234-efab-345678901234 \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "role": {
      "name": "SUPER_ADMIN",
      "description": "Full system access",
      "active": true,
      "features": [
        { "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901" }
      ]
    }
  }'
```

**Response `200 OK`**

```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "name": "SUPER_ADMIN",
  "description": "Full system access",
  "active": true,
  "features": [
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "name": "EXPORT_REPORTS",
      "active": true
    }
  ]
}
```

---

## 🚨 Error Reference

| Status | Code | Endpoints | Description |
|--------|------|-----------|-------------|
| `400 Bad Request` | — | `POST /` | Null or missing request body |
| `404 Not Found` | — | All GET, `PUT /{id}` | No matching role(s) found |
| `422 Unprocessable Entity` | — | `POST /` | MapStruct mapper returned null; typically a bad feature reference |

---

[← Back to API Index](./README.md)
