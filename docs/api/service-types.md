# ⚙️ Service Types API

> **Base path:** `/api/v1/service-types`  
> **[← Back to API Index](./README.md)**

| Auth type | Header | Required on |
|-----------|--------|-------------|
| OAuth2 Bearer JWT | `Authorization: Bearer <jwt>` | All endpoints |

Service Types are catalog entries that classify services managed within the backoffice (e.g. `STANDARD`, `PREMIUM`, `ENTERPRISE`). Each entry has a unique name, an optional description, and an active lifecycle flag.

**Database:** `general.service_type` — created by Flyway migration `V7__create_service_type.sql`

---

## 🟢 GET /api/v1/service-types

> List every registered service type regardless of active status.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | One or more service types exist |
| `404 Not Found` | Empty | No service types registered |

### 💡 Example

```bash
curl -k -X GET https://<host>:8084/api/v1/service-types \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "STANDARD",
    "description": "Standard service offering",
    "active": true
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "name": "LEGACY",
    "description": "Legacy tier — deprecated",
    "active": false
  }
]
```

---

## 🟢 GET /api/v1/service-types/{active}

> List service types filtered by their active status.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `active` | `boolean` | Yes | `true` — active service types only; `false` — inactive only |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Matching service types found |
| `404 Not Found` | Empty | No service types match the given status |

### 💡 Example

```bash
# Active service types only
curl -k -X GET https://<host>:8084/api/v1/service-types/true \
  -H "Authorization: Bearer <jwt>"

# Inactive service types only
curl -k -X GET https://<host>:8084/api/v1/service-types/false \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "STANDARD",
    "description": "Standard service offering",
    "active": true
  },
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "name": "PREMIUM",
    "description": "Premium service tier",
    "active": true
  }
]
```

---

## 🟢 GET /api/v1/service-types/find/{serviceTypeId}

> Retrieve a single service type by its UUID.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `serviceTypeId` | `UUID` | Yes | The service type's unique identifier |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Service type found |
| `400 Bad Request` | Invalid input | `serviceTypeId` is null or missing |
| `404 Not Found` | Missing | No service type with the given ID |

### 💡 Example

```bash
curl -k -X GET \
  https://<host>:8084/api/v1/service-types/find/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "PREMIUM",
  "description": "Premium service tier",
  "active": true
}
```

---

## 🔵 POST /api/v1/service-types/

> Create a new service type catalog entry.

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
| `serviceType.name` | `string` | Yes | Unique; max 128 chars | Service type identifier, e.g. `PREMIUM` |
| `serviceType.description` | `string` | No | Max 512 chars | Human-readable description |
| `serviceType.active` | `boolean` | No | Default: `true` | Whether the service type is active |

```json
{
  "serviceType": {
    "name": "PREMIUM",
    "description": "Premium service tier with SLA guarantees",
    "active": true
  }
}
```

> [!NOTE]
> The `id` field is assigned by the server. If you include an `id` in the request body and it is null, the server generates a new UUID automatically.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Success | Service type created |
| `400 Bad Request` | Invalid input | Null request body or null `serviceType` object |
| `409 Conflict` | Duplicate | A service type with the same `name` already exists |

### 💡 Example

```bash
curl -k -X POST https://<host>:8084/api/v1/service-types/ \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceType": {
      "name": "ENTERPRISE",
      "description": "Enterprise tier with dedicated support",
      "active": true
    }
  }'
```

**Response `201 Created`**

```json
{
  "id": "d4e5f6a7-b8c9-0123-defa-234567890123",
  "name": "ENTERPRISE",
  "description": "Enterprise tier with dedicated support",
  "active": true
}
```

**Response `409 Conflict`**

```json
{
  "X-Message": "Service type name already in use."
}
```

---

## 🟡 PUT /api/v1/service-types/{serviceTypeId}

> Fully replace an existing service type's data.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `serviceTypeId` | `UUID` | Yes | The service type to update |

### 📋 Request Headers

| Header | Value |
|--------|-------|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <jwt>` |

### 📦 Request Body

Same structure as `POST /api/v1/service-types/`. All provided fields replace the current values.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `serviceType.name` | `string` | Yes | Unique; max 128 chars | Updated name |
| `serviceType.description` | `string` | No | Max 512 chars | Updated description |
| `serviceType.active` | `boolean` | No | Default: `true` | Updated active flag |

```json
{
  "serviceType": {
    "name": "PREMIUM",
    "description": "Updated description — now includes 24/7 SLA",
    "active": true
  }
}
```

> [!CAUTION]
> Setting `active: false` deactivates the service type. Existing records referencing this type are not automatically affected, but new assignments should no longer use it.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `202 Accepted` | Success | Service type updated |
| `400 Bad Request` | Invalid input | Null `serviceTypeId` path param or null request body |
| `404 Not Found` | Missing | No service type with the given ID |

### 💡 Example

```bash
curl -k -X PUT \
  https://<host>:8084/api/v1/service-types/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceType": {
      "name": "STANDARD",
      "description": "Standard tier — updated SLA terms",
      "active": true
    }
  }'
```

**Response `202 Accepted`**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "STANDARD",
  "description": "Standard tier — updated SLA terms",
  "active": true
}
```

---

## Database Schema Reference

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `UUID` | Primary Key | Auto-generated unique identifier |
| `name` | `VARCHAR(128)` | NOT NULL, UNIQUE | Service type name |
| `description` | `VARCHAR(512)` | NULLABLE | Optional description |
| `active` | `BOOLEAN` | NOT NULL, DEFAULT true | Active lifecycle flag |

Table: `general.service_type` · Migration: `V7__create_service_type.sql`

---

## 🚨 Error Reference

| Status | Endpoints | Description |
|--------|-----------|-------------|
| `400 Bad Request` | `GET /find/{id}`, `POST /`, `PUT /{id}` | Null ID, null body, or null serviceType object |
| `404 Not Found` | `GET /`, `GET /{active}`, `GET /find/{id}`, `PUT /{id}` | No matching service type(s) |
| `409 Conflict` | `POST /` | Service type name already exists in the database |

---

[← Back to API Index](./README.md)
