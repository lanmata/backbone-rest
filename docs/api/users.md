# 👤 Users API

> **Base path:** `/api/v1/users`  
> **Auth:** OAuth2 Bearer JWT required on every request

| Auth type | Header | Source |
|-----------|--------|--------|
| OAuth2 Bearer JWT | `Authorization: Bearer <jwt>` | Supabase / Keycloak identity provider |

[← Back to API Index](./README.md)

---

## 🔵 POST /api/v1/users

> Create a new user account within an application.

> [!NOTE]
> Alias uniqueness is scoped per application. The same alias can exist in different applications.

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |
| `Content-Type` | ✅ | `application/json` |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `alias` | `string` | ✅ | Unique per application | Login alias / username |
| `email` | `string` | ✅ | Valid email format | User's email address |
| `password` | `string` | ✅ | — | Account password (hashed server-side) |
| `firstName` | `string` | — | — | First name |
| `lastName` | `string` | — | — | Last name |
| `applicationId` | `UUID` | ✅ | Must exist | Application this user belongs to |

```json
{
  "alias": "jdoe",
  "email": "jdoe@example.com",
  "password": "s3cr3t!Pass",
  "firstName": "John",
  "lastName": "Doe",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `201 Created` | User created | Success |
| `400 Bad Request` | Validation failure | Missing required field or constraint violation |
| `409 Conflict` | Duplicate alias | Alias already exists in the application |

**Response `201 Created`:**

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "alias": "jdoe",
  "email": "jdoe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "middleName": null,
  "displayName": "John Doe",
  "gender": null,
  "birthdate": null,
  "active": true,
  "roles": []
}
```

### 💡 Example

```bash
curl -k -s -X POST https://<host>:8084/api/v1/users \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "alias": "jdoe",
    "email": "jdoe@example.com",
    "password": "s3cr3t!Pass",
    "firstName": "John",
    "lastName": "Doe",
    "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

---

## 🟢 GET /api/v1/users/user/{userId}

> Retrieve a single user by their UUID.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `UUID` | ✅ | User's unique identifier |

### 📋 Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | User returned | Found |
| `404 Not Found` | No user | ID does not exist |

**Response `200 OK`:**

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "alias": "jdoe",
  "email": "jdoe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "middleName": null,
  "displayName": "John Doe",
  "gender": "M",
  "birthdate": "1990-01-15",
  "active": true,
  "roles": [
    { "id": "c3d4e5f6-a7b8-9012-cdef-123456789012", "name": "ADMIN" }
  ]
}
```

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/users/user/b2c3d4e5-f6a7-8901-bcde-f12345678901" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟢 GET /api/v1/users/application/{applicationId}

> List all users registered under a specific application.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `applicationId` | `UUID` | ✅ | Target application |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Array of users | At least one user found |
| `404 Not Found` | Empty | No users in the application |

**Response `200 OK`:**

```json
[
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "alias": "jdoe",
    "email": "jdoe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "active": true,
    "roles": []
  }
]
```

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/users/application/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟢 GET /api/v1/users/userByAlias/{alias}/application/{applicationId}

> Retrieve a user by their login alias within an application.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `alias` | `string` | ✅ | Login alias to look up |
| `applicationId` | `UUID` | ✅ | Application scope |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | User returned | Alias found in application |
| `404 Not Found` | No match | Alias not in application |

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/users/userByAlias/jdoe/application/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟢 GET /api/v1/users/alias/{alias}/application/{applicationId}

> Retrieve the alias entity record (not the full User object).

> [!NOTE]
> This returns the raw alias entity — useful when you need the alias metadata (creation time, active flag) without loading the full User graph.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `alias` | `string` | ✅ | Alias to look up |
| `applicationId` | `UUID` | ✅ | Application scope |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | UserAlias object | Found |
| `404 Not Found` | No match | Not in application |

**Response `200 OK`:**

```json
{
  "id": "d4e5f6a7-b8c9-0123-def0-234567890123",
  "alias": "jdoe",
  "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "active": true
}
```

### 💡 Example

```bash
curl -k -X GET \
  "https://<host>:8084/api/v1/users/alias/jdoe/application/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟢 GET /api/v1/users/check/alias/{alias}/application/{applicationId}

> Check whether an alias is available for registration.

> [!TIP]
> Call this before `POST /api/v1/users` during a registration flow to give immediate feedback to the user.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `alias` | `string` | ✅ | Alias to check |
| `applicationId` | `UUID` | ✅ | Application scope |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Available | Alias is free to use |
| `404 Not Found` | Taken | Alias already registered in the application |

> [!CAUTION]
> A `404` here means the alias is **taken** — this is an intentional design choice, not an error. Treat it as `"alias_unavailable"`.

### 💡 Example

```bash
curl -k -o /dev/null -w "%{http_code}" \
  "https://<host>:8084/api/v1/users/check/alias/jdoe/application/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>"
# 200 = available, 404 = taken
```

---

## 🟢 GET /api/v1/users/check/email/{email}/application/{applicationId}

> Check whether an email address is available for registration.

> [!WARNING]
> URL-encode the email before embedding it in the path. `@` must be encoded as `%40`.

### 🛤️ Path Parameters

| Parameter | Type | Required | Constraints | Description |
|-----------|------|----------|-------------|-------------|
| `email` | `string` | ✅ | URL-encoded, valid email | Email to check |
| `applicationId` | `UUID` | ✅ | — | Application scope |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Available | Email is free to use |
| `404 Not Found` | Taken | Email already registered |

### 💡 Example

```bash
curl -k -o /dev/null -w "%{http_code}" \
  "https://<host>:8084/api/v1/users/check/email/jdoe%40example.com/application/a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟡 PUT /api/v1/users/{userId}/full-detail

> Replace all user profile fields in a single request (full update).

> [!CAUTION]
> This is a full replacement. Fields omitted from the body will be cleared on the record.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `UUID` | ✅ | User to update |

### 📦 Request Body

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `firstName` | `string` | ✅ | — | First name |
| `lastName` | `string` | ✅ | — | Last name |
| `middleName` | `string` | — | — | Middle name |
| `displayName` | `string` | — | — | Display / screen name |
| `gender` | `string` | — | `M`, `F`, or custom | Gender identifier |
| `birthdate` | `string` | — | `YYYY-MM-DD` | Date of birth |
| `email` | `string` | ✅ | Valid email | Email address |
| `contacts` | `array` | — | See Contacts API | Phone/contact entries |

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "middleName": "Paul",
  "displayName": "JP Doe",
  "gender": "M",
  "birthdate": "1990-01-15",
  "email": "jdoe@example.com",
  "contacts": []
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Updated | Full update applied |
| `400 Bad Request` | Validation failure | `@Valid` constraint not satisfied |

### 💡 Example

```bash
curl -k -X PUT \
  "https://<host>:8084/api/v1/users/b2c3d4e5-f6a7-8901-bcde-f12345678901/full-detail" \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"jdoe@example.com","birthdate":"1990-01-15","contacts":[]}'
```

---

## 🟡 PUT /api/v1/users/{userId}

> Partially update a user — only the fields present in the body are updated.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `UUID` | ✅ | User to update |

### 📦 Request Body

All fields are optional. Omitted fields are not modified.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `firstName` | `string` | — | — | First name |
| `lastName` | `string` | — | — | Last name |
| `middleName` | `string` | — | — | Middle name |
| `displayName` | `string` | — | — | Display name |
| `gender` | `string` | — | — | Gender |
| `birthdate` | `string` | — | `YYYY-MM-DD` | Date of birth |
| `contacts` | `array` | — | — | Contact entries to update |

```json
{
  "displayName": "JP Doe",
  "gender": "M"
}
```

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `202 Accepted` | Update accepted | Partial update applied |
| `406 Not Acceptable` | Business rule rejection | Update violates a constraint |

### 💡 Example

```bash
curl -k -X PUT \
  "https://<host>:8084/api/v1/users/b2c3d4e5-f6a7-8901-bcde-f12345678901" \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{"displayName":"JP Doe"}'
```

---

## 🟡 PUT /api/v1/users/link/user/{userId}/role/{roleId}

> Assign a role to a user.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `UUID` | ✅ | Target user |
| `roleId` | `UUID` | ✅ | Role to assign |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Role linked | Success |
| `404 Not Found` | Missing resource | User or role not found |

### 💡 Example

```bash
curl -k -X PUT \
  "https://<host>:8084/api/v1/users/link/user/b2c3d4e5-f6a7-8901-bcde-f12345678901/role/c3d4e5f6-a7b8-9012-cdef-123456789012" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🟡 PUT /api/v1/users/unlink/user/{userId}/role/{roleId}

> Remove a role from a user.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `UUID` | ✅ | Target user |
| `roleId` | `UUID` | ✅ | Role to remove |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Role removed | Success |
| `404 Not Found` | Missing resource | User or role not found |

### 💡 Example

```bash
curl -k -X PUT \
  "https://<host>:8084/api/v1/users/unlink/user/b2c3d4e5-f6a7-8901-bcde-f12345678901/role/c3d4e5f6-a7b8-9012-cdef-123456789012" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🔴 DELETE /api/v1/users/application/{applicationId}/user/{userId}

> Remove a user from an application. This deletes the user–application association.

> [!WARNING]
> This action is irreversible. The user's data within the application context will be removed.

### 🛤️ Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `applicationId` | `UUID` | ✅ | Application context |
| `userId` | `UUID` | ✅ | User to delete |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `204 No Content` | Deleted | User removed from application |
| `400 Bad Request` | Null IDs | One or both path params are missing/null |
| `404 Not Found` | Not found | User does not exist in the application |

### 💡 Example

```bash
curl -k -X DELETE \
  "https://<host>:8084/api/v1/users/application/a1b2c3d4-e5f6-7890-abcd-ef1234567890/user/b2c3d4e5-f6a7-8901-bcde-f12345678901" \
  -H "Authorization: Bearer <jwt>"
```

---

## 🚨 Error Reference

| Status | Code | Description |
|--------|------|-------------|
| `400` | Bad Request | Missing required field, `@Valid` constraint failure, or null path param |
| `401` | Unauthorized | Bearer JWT is missing, expired, or invalid |
| `403` | Forbidden | JWT is valid but caller lacks required role |
| `404` | Not Found | Resource not found — also used to signal "alias/email already taken" on check endpoints |
| `406` | Not Acceptable | Partial update rejected by a business rule |
| `409` | Conflict | Alias already registered in the application |
| `500` | Internal Server Error | Unexpected server-side failure |

---

[← Back to API Index](./README.md)
