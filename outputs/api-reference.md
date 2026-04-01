# REST API Reference — backbone-rest

> **Base URL:** `https://<host>:8082/api/v1`  
> **Auth:** Bearer JWT (Keycloak) required on all endpoints unless noted  
> **Content-Type:** `application/json` (unless noted)  
> **OpenAPI Spec:** `src/main/resources/META-INF/backbone_rest-openapi.yaml`  
> **Swagger UI (runtime):** `/swagger-ui/index.html`

---

## 1. Session (`/api/v1/sessions`)

> Session endpoints mint an **application-specific JWT** (different from the Keycloak Bearer token).  
> The `session-token` header key is used for subsequent calls to session-protected endpoints (e.g. profile image).

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/api/v1/sessions` | Authenticate with alias + password; returns session token | ❌ Public |
| POST | `/api/v1/sessions/token` | Authenticate with email + password; returns session token | ❌ Public |
| GET | `/api/v1/sessions/validate` | Validate a session token (header: `session-token`) | ❌ Public |
| GET | `/api/v1/sessions/renew` | Renew a session token (header: `session-token`) | ❌ Public |

### POST `/api/v1/sessions` — Authenticate by alias

**Request body:**
```json
{
  "alias": "johndoe",
  "password": "s3cr3t",
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

**Response `200 OK`:**
```json
{
  "token": "<jwt>",
  "user": { ... },
  "sessionId": "uuid"
}
```

**Response `401 Unauthorized`:** Invalid credentials  
**Response `404 Not Found`:** Invalid request payload

---

### POST `/api/v1/sessions/token` — Authenticate by email

**Request body:**
```json
{
  "email": "john@example.com",
  "password": "s3cr3t",
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

Same response as above.

---

### GET `/api/v1/sessions/validate`

**Header:** `session-token: <jwt>`  
**Response `200 OK`:** `true`  
**Response `401 Unauthorized`:** invalid token

---

### GET `/api/v1/sessions/renew`

**Header:** `session-token: <jwt>`  
**Response `200 OK`:** `SessionResponse` with new token  
**Response `400 Bad Request`:** token is missing  
**Response `401 Unauthorized`:** expired/invalid token

---

## 2. Users (`/api/v1/users`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/users/check/alias/{alias}/application/{applicationId}` | Check alias availability |
| GET | `/api/v1/users/check/email/{email}/application/{applicationId}` | Check email availability |
| POST | `/api/v1/users` | Create a new user |
| GET | `/api/v1/users/user/{userId}` | Find user by ID |
| GET | `/api/v1/users/application/{applicationId}` | List all users for an application |
| GET | `/api/v1/users/userByAlias/{alias}/application/{applicationId}` | Find user by alias |
| GET | `/api/v1/users/alias/{alias}/application/{applicationId}` | Find user alias record by alias |
| PUT | `/api/v1/users/{userId}/full-detail` | Full update of a user |
| PUT | `/api/v1/users/{userId}` | Partial update of a user |
| PUT | `/api/v1/users/unlink/user/{userId}/role/{roleId}` | Unlink a role from a user |
| PUT | `/api/v1/users/link/user/{userId}/role/{roleId}` | Link a role to a user |
| DELETE | `/api/v1/users/application/{applicationId}/user/{userId}` | Delete a user |

### GET `/check/alias/{alias}/application/{applicationId}`

**Path params:** `alias` (string), `applicationId` (UUID)  
**Response `200 OK`:** Alias is available  
**Response `409 Conflict`:** Alias already in use

---

### POST `/api/v1/users` — Create User

**Request body:**
```json
{
  "alias": "johndoe",
  "password": "s3cr3t",
  "email": "john@example.com",
  "displayName": "John Doe",
  "active": true,
  "applicationId": "uuid",
  "roleId": "uuid",
  "personId": "uuid"
}
```

**Response `201 Created`:**
```json
{
  "id": "uuid",
  "alias": "johndoe",
  "email": "john@example.com",
  "active": true,
  "personId": "uuid",
  "applicationId": "uuid",
  "roleId": "uuid"
}
```

**Response `406 Not Acceptable`:** Request body is null  
**Response `417 Expectation Failed`:** alias or password is null

---

### PUT `/api/v1/users/{userId}` — Partial Update

**Request body:**
```json
{
  "active": true,
  "displayName": "John Updated"
}
```

**Response `202 Accepted`:** User updated  
**Response `406 Not Acceptable`:** Update rejected

---

### DELETE `/api/v1/users/application/{applicationId}/user/{userId}`

**Response `204 No Content`:** Deleted  
**Response `404 Not Found`:** User not found  
**Response `400 Bad Request`:** Invalid params

---

## 3. Roles (`/api/v1/roles`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/roles/find/{roleId}` | Find role by ID |
| GET | `/api/v1/roles/{includeInactive}` | List roles by active status |
| GET | `/api/v1/roles/{includeInactive}/{roleIds}` | List roles by status and IDs |
| GET | `/api/v1/roles` | List all roles |
| GET | `/api/v1/roles/user/{userId}` | List roles assigned to a user |
| POST | `/api/v1/roles/` | Create a new role |
| PUT | `/api/v1/roles/{roleId}` | Update a role |

### POST `/api/v1/roles/` — Create Role

**Request body:**
```json
{
  "role": {
    "name": "ADMIN",
    "description": "Administrator role",
    "active": true
  }
}
```

**Response `200 OK`:** Created role  
**Response `400 Bad Request`:** Role is null

---

## 4. Features (`/api/v1/features`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/features/find/{featureId}` | Find feature by ID |
| GET | `/api/v1/features/{includeInactive}` | List features by active status |
| GET | `/api/v1/features/{includeInactive}/{featuresIds}` | List features by status and IDs |
| POST | `/api/v1/features` | Create a feature |
| PUT | `/api/v1/features/{featureId}` | Update a feature |

---

## 5. People (`/api/v1/people`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/people/` | Create a person |
| GET | `/api/v1/people/{personId}` | Find person by ID |
| PUT | `/api/v1/people/{personId}` | Update person |
| GET | `/api/v1/people` | List all people |

### POST `/api/v1/people/` — Create Person

**Request body:**
```json
{
  "person": {
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1990-01-15",
    "gender": "M"
  }
}
```

**Response `200 OK`:** Created person

---

## 6. Contacts (`/api/v1/contacts`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/contacts/` | Create contact |
| PUT | `/api/v1/contacts/{contactId}` | Update contact |
| GET | `/api/v1/contacts/{contactId}` | Find contact by ID |
| GET | `/api/v1/contacts/list/{contactIds}` | List contacts by IDs |
| GET | `/api/v1/contacts/person/{personId}` | List contacts for a person |

---

## 7. Contact Types (`/api/v1/contact-types`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/contact-types/` | Create contact type |
| GET | `/api/v1/contact-types/{contactTypeId}` | Find contact type by ID |
| PUT | `/api/v1/contact-types/{contactTypeId}` | Update contact type |
| GET | `/api/v1/contact-types/list/{contactTypeIds}` | List contact types by IDs |
| GET | `/api/v1/contact-types` | List all contact types |

---

## 8. Applications (`/api/v1/applications`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/applications` | Register a new application |

### POST `/api/v1/applications` — Create Application

**Request body:**
```json
{
  "application": {
    "name": "my-app",
    "description": "My Application",
    "active": true
  }
}
```

**Response `201 Created`:** Created application  
**Response `400 Bad Request`:** Invalid request body  
**Response `500 Internal Server Error`:** Server error

---

## 9. Profile Images (`/api/v1/profile-images`)

> These endpoints require the `session-token` request header.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/profile-images/application/{applicationId}` | Upload a profile image (multipart) |
| GET | `/api/v1/profile-images/` | Retrieve the profile image |
| GET | `/api/v1/profile-images/{userId}/reference` | Get profile image reference by user ID |

### POST Upload — Multipart

**Headers:** `session-token: <jwt>`, `Content-Type: multipart/form-data`  
**Path param:** `applicationId` (UUID)  
**Form part:** `image` (byte array)  
**Response `201 Created`:** `{ "reference": "..." }`

---

## 10. Documents / Reports (`/api/v1/report`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/report/template` | Fill a Word template with values; returns `.docx` |
| GET | `/api/v1/report/placeholdervalues` | List all placeholder keys in a Word template |

### GET `/api/v1/report/template`

**Consumes:** `application/json`, `multipart/form-data`  
**Produces:** `application/octet-stream`  
**Query params:** `values` (key=value map), `documentTemplate` (MultipartFile)  
**Response `200 OK`:** Populated Word document as download stream

---

## 11. HTTP Status Code Guide

| Code | Meaning in this service |
|------|------------------------|
| `200 OK` | Successful read / update |
| `201 Created` | Successful resource creation |
| `202 Accepted` | Partial update accepted |
| `204 No Content` | Successful delete |
| `400 Bad Request` | Malformed request or validation failure |
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Authenticated but lacks required role |
| `404 Not Found` | Resource does not exist |
| `406 Not Acceptable` | Business rule rejection |
| `409 Conflict` | Alias or email already exists |
| `417 Expectation Failed` | Required field is null |
| `422 Unprocessable Entity` | Semantically invalid data |
| `500 Internal Server Error` | Unexpected server-side error |
| `501 Not Implemented` | Endpoint stub not yet implemented |

---

## 12. Swagger / OpenAPI Access

When the service is running:

| URL | Description |
|-----|-------------|
| `https://<host>:8082/swagger-ui/index.html` | Interactive Swagger UI |
| `https://<host>:8082/v3/api-docs` | OpenAPI 3 JSON descriptor |
| `src/main/resources/META-INF/backbone_rest-openapi.yaml` | Bundled YAML spec |

