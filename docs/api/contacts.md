# 📞 Contact Management

> **Base path:** `/api/v1/contacts`  
> **Auth:** OAuth2 Bearer JWT — `Authorization: Bearer <jwt>`  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/contacts` |
| Auth type | OAuth2 Bearer JWT |
| Content-Type | `application/json` |
| Response header | `X-Message` — human-readable status |

A **Contact** is a communication entry (phone number, email address, messaging account) that belongs to exactly **one subject**, inside an **application**:

| Subject | Identified by | Typical use |
|---------|---------------|-------------|
| A person | `personId` | A user's contacts; the same person can hold different contacts in different applications |
| An external owner (neither a person nor an application) | `ownerType` + `ownerId` + `applicationId` | A merchant of a commerce-index application, a branch, a supplier… |

> **Scalability rule.** Contacts grow with every application using backbone, so they are **never listed across applications**. Every listing is scoped: by person (`/person/{id}`), by external owner (`/owner/{type}/{id}`) or by application (`GET /?applicationId=`, server-side paginated). The contact limit (`app.environments.contact.limit`) and the duplicate check apply **per subject and per application**.

---

## 🔵 POST /api/v1/contacts/

> Create a new contact and associate it with a person.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `contentTypeId` | `UUID` | ✅ | Existing contact type | Reference to a contact type |
| `content` | `string` | ✅ | Non-empty | The contact value (e.g. phone number, email) |
| `personId` | `UUID` | one of | Valid UUID | Subject = a person. Mutually exclusive with `ownerType`/`ownerId` |
| `ownerType` | `string` | one of | `^[A-Za-z][A-Za-z0-9_-]{1,49}$`, stored upper-case | Subject = an external owner (e.g. `MERCHANT`) |
| `ownerId` | `UUID` | with `ownerType` | Valid UUID | Id of the external owner in the namespace of `applicationId` |
| `applicationId` | `UUID` | for owners | Existing application | Application the contact belongs to (optional for persons) |
| `active` | `boolean` | — | | Whether the contact is active |

```json
{
  "content": "+1-555-0100",
  "contentTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "ownerType": "MERCHANT",
  "ownerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "applicationId": "d4e5f6a7-b8c9-0123-def0-123456789abc",
  "active": true
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Created | Contact saved successfully |
| `400 Bad Request` | Invalid input | Null body or missing required fields |

**Response `200 OK`:**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "contactTypeName": "PHONE",
  "value": "+1-555-0100",
  "primary": true
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/contacts/ \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "value": "+1-555-0100",
    "primary": true
  }'
```

---

## 🟡 PUT /api/v1/contacts/{contactId}

> Update an existing contact's value, type, or primary flag.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactId` | `UUID` | ✅ | ID of the contact to update |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

Same structure as `POST /api/v1/contacts/`.

```json
{
  "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "value": "+1-555-0199",
  "primary": false
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Contact updated successfully |
| `404 Not Found` | Not found | No contact with that ID |

**Response `200 OK`:**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "contactTypeName": "PHONE",
  "value": "+1-555-0199",
  "primary": false
}
```

### 💡 Example

```bash
curl -k -s -X PUT https://<host>:8084/api/v1/contacts/c3d4e5f6-a7b8-9012-cdef-123456789012 \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "value": "+1-555-0199",
    "primary": false
  }'
```

---

## 🟢 GET /api/v1/contacts/{contactId}

> Retrieve a single contact by its ID.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactId` | `UUID` | ✅ | ID of the contact to retrieve |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Contact returned |
| `404 Not Found` | Not found | No contact with that ID |

**Response `200 OK`:**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "contactTypeName": "PHONE",
  "value": "+1-555-0100",
  "primary": true
}
```

### 💡 Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/contacts/c3d4e5f6-a7b8-9012-cdef-123456789012 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟢 GET /api/v1/contacts/list/{contactIds}

> Retrieve multiple contacts in a single request by providing their IDs.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactIds` | `string` | ✅ | Comma-separated list of contact UUIDs |

> [!TIP]
> URL-encode commas if your HTTP client requires it: `%2C`. Most clients handle bare commas in path segments.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Array of matched contacts |
| `404 Not Found` | Not found | None of the IDs matched |

**Response `200 OK`:**
```json
[
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "contactTypeName": "PHONE",
    "value": "+1-555-0100",
    "primary": true
  },
  {
    "id": "d4e5f6a7-b8c9-0123-defa-234567890123",
    "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "contactTypeId": "e5f6a7b8-c9d0-1234-efab-345678901234",
    "contactTypeName": "EMAIL",
    "value": "john.doe@example.com",
    "primary": false
  }
]
```

### 💡 Example

```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/contacts/list/c3d4e5f6-a7b8-9012-cdef-123456789012,d4e5f6a7-b8c9-0123-defa-234567890123" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟢 GET /api/v1/contacts/person/{personId}

> Optional `?applicationId=` limits the result to one application.

> Retrieve all contacts belonging to a specific person.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `personId` | `UUID` | ✅ | ID of the person whose contacts to list |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Found | Array of contacts for that person |
| `404 Not Found` | Not found | Person has no contacts or person ID is invalid |

**Response `200 OK`:**
```json
[
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "contactTypeName": "PHONE",
    "value": "+1-555-0100",
    "primary": true
  }
]
```

### 💡 Example

```bash
curl -k -s -X GET \
  https://<host>:8084/api/v1/contacts/person/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🟢 GET /api/v1/contacts/?applicationId=

> Paginated contact directory of **one application** (persons' and external owners' contacts). Replaces the removed `GET /list-all`.

| Query | Required | Description |
|-------|----------|-------------|
| `applicationId` | ✅ | The application. `400` when missing |
| `contactTypeId` | — | Filter by contact type |
| `active` | — | Filter by status |
| `q` | — | Case-insensitive "contains" filter on the content (`%` and `_` are literal) |
| `page` | — | Zero-based page, default `0` |
| `size` | — | Page size, default `20`, maximum `100` |

```json
{ "content": [ { "id": "…", "content": "shop@a.com", "ownerType": "MERCHANT", "ownerId": "…", "applicationId": "…", "active": true } ],
  "page": 0, "size": 20, "totalElements": 134, "totalPages": 7 }
```

---

## 🟢 GET /api/v1/contacts/owner/{ownerType}/{ownerId}

> Contacts of an external owner, optionally limited with `?applicationId=`. Bounded by the per-application contact limit. `400` when `ownerType`/`ownerId` is blank.

---

## 🔴 DELETE /api/v1/contacts/{contactId}

> Permanently delete a contact record.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contactId` | `UUID` | ✅ | ID of the contact to delete |

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Deleted | Contact removed successfully |
| `404 Not Found` | Not found | No contact with that ID |

**Response `200 OK`:**
```json
{
  "message": "Contact deleted."
}
```

### 💡 Example

```bash
curl -k -s -X DELETE \
  https://<host>:8084/api/v1/contacts/c3d4e5f6-a7b8-9012-cdef-123456789012 \
  -H "Authorization: Bearer <oauth2-jwt>"
```

---

## 🚨 Error Reference

| Status | Code | Cause |
|--------|------|-------|
| `200` | OK | Success (create, update, delete all return 200) |
| `400` | Bad Request | Null body or missing required field on create |
| `401` | Unauthorized | Missing or invalid Bearer JWT |
| `404` | Not Found | Contact ID not found, or person has no contacts |
| `501` | Not Implemented | `GET /list-all` is not implemented |

---

[← Back to API Index](./README.md)
