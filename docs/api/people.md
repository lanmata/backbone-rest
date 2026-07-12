# 👥 People API

> **Base path:** `/api/v1/people`  
> **Auth:** OAuth2 Bearer JWT required on every request

| Auth type | Header | Source |
|-----------|--------|--------|
| OAuth2 Bearer JWT | `Authorization: Bearer <jwt>` | Supabase / Keycloak identity provider |

> [!NOTE]
> A **Person** is a physical individual record (biographic data). It is distinct from a **User**, which is a system account with credentials. A User may optionally be linked to a Person record.

[← Back to API Index](./README.md)

---

## 🔵 POST /api/v1/people

> Create a new person record.

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `person.firstName` | `string` | ✅ | — | First (given) name |
| `person.lastName` | `string` | ✅ | — | Last (family) name |
| `person.middleName` | `string` | — | — | Middle name |
| `person.birthdate` | `string` | — | `YYYY-MM-DD` | Date of birth |

```json
{
  "person": {
    "firstName": "John",
    "lastName": "Doe",
    "middleName": "Paul",
    "birthdate": "1990-01-15"
  }
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Person created | Success — returns the persisted record with generated UUID |
| `400 Bad Request` | Invalid input | Null body or missing required field |

**Response `200 OK`:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "firstName": "John",
  "lastName": "Doe",
  "middleName": "Paul",
  "birthdate": "1990-01-15"
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/people \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "person": {
      "firstName": "John",
      "lastName": "Doe",
      "middleName": "Paul",
      "birthdate": "1990-01-15"
    }
  }'
```

---

## 🟢 GET /api/v1/people/{personId}

> Retrieve a single person record by UUID.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `personId` | `UUID` | ✅ | Person's unique identifier |

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Person returned | Found |
| `404 Not Found` | No record | ID does not exist |

**Response `200 OK`:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "firstName": "John",
  "lastName": "Doe",
  "middleName": "Paul",
  "birthdate": "1990-01-15"
}
```

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/people/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟡 PUT /api/v1/people/{personId}

> Update an existing person record. All provided fields are applied; omitted fields retain their current values.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `personId` | `UUID` | ✅ | Person to update |

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `person.firstName` | `string` | ✅ | — | First name |
| `person.lastName` | `string` | ✅ | — | Last name |
| `person.middleName` | `string` | — | — | Middle name |
| `person.birthdate` | `string` | — | `YYYY-MM-DD` | Date of birth |

```json
{
  "person": {
    "firstName": "John",
    "lastName": "Doe",
    "middleName": null,
    "birthdate": "1990-01-15"
  }
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Record saved successfully |
| `400 Bad Request` | Invalid input | Null body, null ID, or missing required fields |

**Response `200 OK`:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "firstName": "John",
  "lastName": "Doe",
  "middleName": null,
  "birthdate": "1990-01-15"
}
```

### 💡 Example

```bash
curl -k -X PUT \
  "https://<host>:8084/api/v1/people/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "person": {
      "firstName": "John",
      "lastName": "Doe",
      "middleName": null,
      "birthdate": "1990-01-15"
    }
  }'
```

---

## 🟢 GET /api/v1/people

> List all person records in the system.

> [!TIP]
> This endpoint returns all records without pagination. For large datasets, consider filtering in the application layer or request that pagination be added.

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Array of people | At least one record found |
| `404 Not Found` | Empty | No person records exist |

**Response `200 OK`:**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "firstName": "John",
    "lastName": "Doe",
    "middleName": null,
    "birthdate": "1990-01-15"
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "firstName": "Jane",
    "lastName": "Smith",
    "middleName": "Marie",
    "birthdate": "1985-06-30"
  }
]
```

### 💡 Example

```bash
curl -k -X GET https://<host>:8084/api/v1/people \
  -H "Authorization: Bearer <jwt>"
```

---

## 🚨 Error Reference

| Status | Code | Description |
|--------|------|-------------|
| `400` | Bad Request | Null request body, null ID, or missing required field |
| `401` | Unauthorized | Bearer JWT is missing, expired, or invalid |
| `403` | Forbidden | JWT is valid but caller lacks required role |
| `404` | Not Found | Person record does not exist, or no records in the system |
| `500` | Internal Server Error | Unexpected server-side failure |

---

[← Back to API Index](./README.md)
