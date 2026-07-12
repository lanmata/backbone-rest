# 🏷️ Contact Type Management

> **Base path:** `/api/v1/contact-types`  
> **Auth:** OAuth2 Bearer JWT — `Authorization: Bearer <jwt>`  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/contact-types` |
| Auth type | OAuth2 Bearer JWT |
| Content-Type | `application/json` |
| Response header | `X-Message` — human-readable status |

**Contact Types** are reference data entries that categorize contacts. Examples: `PHONE`, `EMAIL`, `HOME_ADDRESS`, `WORK_ADDRESS`. Every `Contact` record must reference a Contact Type. Create your contact type catalog before creating contact records.

---

## 🔵 POST /api/v1/contact-types/

> Register a new contact type in the catalog.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `contactType.name` | `string` | ✅ | Unique | Identifier for the contact type (e.g. `PHONE`) |
| `contactType.description` | `string` | — | Max 512 chars | Human-readable description |
| `contactType.active` | `boolean` | — | Default `true` | Whether this type is available for new contacts |

```json
{
  "contactType": {
    "name": "PHONE",
    "description": "Mobile or landline phone number",
    "active": true
  }
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | Created | Contact type registered |
| `400 Bad Request` | Invalid input | Null body or missing name |
| `409 Conflict` | Duplicate | A contact type with that name already exists |

**Response `201 Created`:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "PHONE",
  "description": "Mobile or landline phone number",
  "active": true
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/contact-types/ \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "contactType": {
      "name": "PHONE",
      "description": "Mobile or landline phone number",
      "active": true
    }
  }'
```

---

## 🟢 GET /api/v1/contact-types/{contactTypeId}

> Retrieve a single contact type by its ID.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactTypeId` | `UUID` | ✅ | ID of the contact type to retrieve |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Contact type returned |
| `404 Not Found` | Not found | No contact type with that ID |

**Response `200 OK`:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "PHONE",
  "description": "Mobile or landline phone number",
  "active": true
}
```

### 💡 Example

```bash
curl -k -s -X GET \
  https://<host>:8084/api/v1/contact-types/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟡 PUT /api/v1/contact-types/{contactTypeId}

> Update an existing contact type (full replacement).

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactTypeId` | `UUID` | ✅ | ID of the contact type to update |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `name` | `string` | ✅ | Unique | Updated name |
| `description` | `string` | — | — | Updated description |
| `active` | `boolean` | — | — | Updated active flag |

```json
{
  "name": "MOBILE_PHONE",
  "description": "Mobile phone number only",
  "active": true
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Contact type updated |
| `400 Bad Request` | Invalid input | Null body or null ID |
| `404 Not Found` | Not found | No contact type with that ID |

**Response `200 OK`:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "MOBILE_PHONE",
  "description": "Mobile phone number only",
  "active": true
}
```

### 💡 Example

```bash
curl -k -s -X PUT \
  https://<host>:8084/api/v1/contact-types/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MOBILE_PHONE",
    "description": "Mobile phone number only",
    "active": true
  }'
```

---

## 🟢 GET /api/v1/contact-types/list/{contactTypeIds}

> Retrieve multiple contact types by their IDs in a single call.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactTypeIds` | `string` | ✅ | Comma-separated list of contact type UUIDs |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Array of matched contact types |
| `404 Not Found` | Not found | None of the IDs matched |

**Response `200 OK`:**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "PHONE",
    "description": "Phone number",
    "active": true
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "name": "EMAIL",
    "description": "Email address",
    "active": true
  }
]
```

### 💡 Example

```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/contact-types/list/a1b2c3d4-e5f6-7890-abcd-ef1234567890,b2c3d4e5-f6a7-8901-bcde-f12345678901" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟢 GET /api/v1/contact-types/list-all

> Retrieve the complete contact type catalog.

> [!TIP]
> Call this endpoint once at application startup to build a local cache of contact type IDs. Contact types are reference data that rarely change.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Full array of contact types |
| `404 Not Found` | Empty | No contact types registered |

**Response `200 OK`:**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "PHONE",
    "description": "Phone number",
    "active": true
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "name": "EMAIL",
    "description": "Email address",
    "active": true
  },
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "name": "HOME_ADDRESS",
    "description": "Residential address",
    "active": true
  }
]
```

### 💡 Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/contact-types/list-all \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🔴 DELETE /api/v1/contact-types/{contactTypeId}

> Permanently remove a contact type from the catalog.

> [!WARNING]
> Deleting a contact type that is still referenced by existing `Contact` records may cause referential integrity issues. Deactivate (`active: false`) instead of deleting when in doubt.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactTypeId` | `UUID` | ✅ | ID of the contact type to delete |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Deleted | Contact type removed |
| `404 Not Found` | Not found | No contact type with that ID |

**Response `200 OK`:**
```json
{
  "message": "Contact type deleted."
}
```

### 💡 Example

```bash
curl -k -s -X DELETE \
  https://<host>:8084/api/v1/contact-types/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🚨 Error Reference

| Status | Cause |
|--------|-------|
| `400 Bad Request` | Null body, null ID, or missing required name |
| `401 Unauthorized` | Missing or invalid OAuth2 Bearer JWT |
| `404 Not Found` | Contact type not found or catalog is empty |
| `409 Conflict` | Name uniqueness violation on create |

---

[← Back to API Index](./README.md)
