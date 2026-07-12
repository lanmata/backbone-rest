# ⚡ Features API

> **Base path:** `/api/v1/features`  
> **[← Back to API Index](./README.md)**

| Auth type | Header | Required on |
|-----------|--------|-------------|
| OAuth2 Bearer JWT | `Authorization: Bearer <jwt>` | All endpoints |

Features are fine-grained capability flags grouped into Roles. A Feature has a unique name (e.g. `EXPORT_REPORTS`), an optional description, and an active flag. Features are linked to Roles — not directly to Users. A User inherits features through their assigned roles.

---

## 🟢 GET /api/v1/features/find/{featureId}

> Retrieve a single feature by its UUID.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `featureId` | `UUID` | Yes | The feature's unique identifier |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Feature found |
| `404 Not Found` | Missing | No feature with the given ID |

### 💡 Example

```bash
curl -k -X GET \
  https://<host>:8084/api/v1/features/find/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "EXPORT_REPORTS",
  "description": "Allows exporting reports to PDF and Excel",
  "active": true
}
```

---

## 🟢 GET /api/v1/features/{includeInactive}

> List features filtered by their active status.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `includeInactive` | `boolean` | Yes | `true` — return all features (active + inactive); `false` — active only |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | Matching features found |
| `404 Not Found` | Empty | No features match the filter |

### 💡 Example

```bash
# Active features only
curl -k -X GET https://<host>:8084/api/v1/features/false \
  -H "Authorization: Bearer <jwt>"

# All features including inactive
curl -k -X GET https://<host>:8084/api/v1/features/true \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "name": "EXPORT_REPORTS",
    "description": "Allows exporting reports",
    "active": true
  },
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "name": "LEGACY_DASHBOARD",
    "description": "Old dashboard — deprecated",
    "active": false
  }
]
```

---

## 🟢 GET /api/v1/features/{includeInactive}/{featuresIds}

> List a specific set of features by their IDs with optional status filter.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `includeInactive` | `boolean` | Yes | `true` includes inactive results; `false` active only |
| `featuresIds` | `string` | Yes | Comma-separated feature IDs or names |

> [!NOTE]
> `featuresIds` is a single path segment. Pass comma-separated UUIDs or feature name strings depending on the implementation. URL-encode commas as `%2C` if needed.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Success | At least one match found |
| `404 Not Found` | Empty | No features match the given IDs/names and status |

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/features/true/b2c3d4e5-f6a7-8901-bcde-f12345678901,c3d4e5f6-a7b8-9012-cdef-123456789012" \
  -H "Authorization: Bearer <jwt>"
```

**Response `200 OK`**

```json
[
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "name": "EXPORT_REPORTS",
    "description": "Allows exporting reports",
    "active": true
  }
]
```

---

## 🔵 POST /api/v1/features/

> Create a new feature flag.

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
| `feature.name` | `string` | Yes | Unique; recommended `SCREAMING_SNAKE_CASE` | Feature identifier, e.g. `EXPORT_REPORTS` |
| `feature.description` | `string` | No | — | Human-readable description of the capability |
| `feature.active` | `boolean` | No | Default: `true` | Whether the feature is currently active |

```json
{
  "feature": {
    "name": "EXPORT_REPORTS",
    "description": "Allows exporting reports to PDF and Excel",
    "active": true
  }
}
```

> [!TIP]
> Use `SCREAMING_SNAKE_CASE` for feature names (e.g. `VIEW_DASHBOARD`, `MANAGE_USERS`, `EXPORT_REPORTS`). This convention makes permission checks human-readable and easier to audit in logs.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Success | Feature created |
| `406 Not Acceptable` | Rejected | Name uniqueness constraint or other business rule violation |

### 💡 Example

```bash
curl -k -X POST https://<host>:8084/api/v1/features/ \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "feature": {
      "name": "EXPORT_REPORTS",
      "description": "Allows exporting reports to PDF and Excel",
      "active": true
    }
  }'
```

**Response `201 Created`**

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "EXPORT_REPORTS",
  "description": "Allows exporting reports to PDF and Excel",
  "active": true
}
```

---

## 🟡 PUT /api/v1/features/{featureId}

> Fully replace an existing feature's data.

### Auth

```
Authorization: Bearer <oauth2-jwt>
```

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `featureId` | `UUID` | Yes | The feature to update |

### 📋 Request Headers

| Header | Value |
|--------|-------|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <jwt>` |

### 📦 Request Body

Same structure as `POST /api/v1/features/`.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `feature.name` | `string` | Yes | Unique | Updated feature name |
| `feature.description` | `string` | No | — | Updated description |
| `feature.active` | `boolean` | No | Default: `true` | Updated active state |

```json
{
  "feature": {
    "name": "EXPORT_REPORTS",
    "description": "Updated: export to PDF, Excel, and CSV",
    "active": false
  }
}
```

> [!CAUTION]
> Setting `active: false` deactivates the feature. Users with roles that include this feature will lose the capability without notice. Coordinate deactivation with affected application teams.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `202 Accepted` | Success | Feature updated |
| `406 Not Acceptable` | Rejected | Feature not registered or business rule violation |

### 💡 Example

```bash
curl -k -X PUT \
  https://<host>:8084/api/v1/features/b2c3d4e5-f6a7-8901-bcde-f12345678901 \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "feature": {
      "name": "EXPORT_REPORTS",
      "description": "Export to PDF, Excel, and CSV",
      "active": true
    }
  }'
```

**Response `202 Accepted`**

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "EXPORT_REPORTS",
  "description": "Export to PDF, Excel, and CSV",
  "active": true
}
```

---

## 🚨 Error Reference

| Status | Endpoints | Description |
|--------|-----------|-------------|
| `404 Not Found` | `GET /find/{id}`, `GET /{includeInactive}`, `GET /{includeInactive}/{ids}` | No matching feature(s) |
| `406 Not Acceptable` | `POST /`, `PUT /{id}` | Name conflict or business rule rejection |

---

[← Back to API Index](./README.md)
