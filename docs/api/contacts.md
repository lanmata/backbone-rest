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

A **Contact** is a communication entry (phone number, email address, postal address) linked to a **Person**. One person can have many contacts of different types.

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
| `personId` | `UUID` | ✅ | Valid UUID | The person this contact belongs to |
| `contactTypeId` | `UUID` | ✅ | Valid UUID | Reference to a contact type |
| `value` | `string` | ✅ | Non-empty | The contact value (e.g. phone number, email) |
| `primary` | `boolean` | — | Default `false` | Whether this is the person's primary contact |

```json
{
  "personId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "contactTypeId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "value": "+1-555-0100",
  "primary": true
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

## 🟢 GET /api/v1/contacts/list-all

> List all contacts across all persons.

> [!CAUTION]
> This endpoint is **not implemented** and will always return `501 Not Implemented`. Do not call it in production.

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `501 Not Implemented` | Stub | Endpoint is planned but not yet active |

### 💡 Example

```bash
curl -k -s -X GET https://<host>:8084/api/v1/contacts/list-all \
  -H "Authorization: Bearer <oauth2-jwt>"
# Always returns 501
```

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
