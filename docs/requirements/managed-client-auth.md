# 🔐 Management Client Authentication Manager
### Requirements Document — `backbone-rest` · TASK-02

---

![Status](https://img.shields.io/badge/status-draft-yellow)
![Priority](https://img.shields.io/badge/priority-high-red)
![Service](https://img.shields.io/badge/service-backbone--rest-blue)
![Java](https://img.shields.io/badge/java-21-blue)
![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.x-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0--DRAFT-lightgrey)

---

## 📋 Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [Stakeholders](#3-stakeholders)
4. [Functional Requirements](#4-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [API Contract Overview](#6-api-contract-overview)
7. [Data Model Overview](#7-data-model-overview)
8. [Security Considerations](#8-security-considerations)
9. [Architecture Constraints](#9-architecture-constraints)
10. [Out of Scope](#10-out-of-scope)
11. [Acceptance Criteria](#11-acceptance-criteria)
12. [Example Payloads](#12-example-payloads)
13. [Dependencies & Risks](#13-dependencies--risks)

---

## 1. Executive Summary

The **Management Client Authentication Manager** (MCAM) feature introduces first-class **machine-to-machine (M2M) / service-to-service authentication** for the `backbone-rest` backoffice platform. Today, every inter-service call either reuses a human session JWT or relies on implicit trust inside the private network — neither of which provides the auditability, rotation strategy, or scope enforcement that a production-grade platform requires.

MCAM closes this gap by exposing a dedicated API under `/api/v1/managed-clients` that allows operators to:

- **Register** external or internal client applications and issue them OAuth2-style `client_id` / `client_secret` credentials.
- **Rotate and revoke** secrets without downtime.
- **Define scopes** that bound which `backbone-rest` resources each client is authorised to call.
- **Introspect** inbound M2M tokens to verify their authenticity and scope before granting access.
- **Query a tamper-evident audit log** of every credential lifecycle event and authentication attempt.

The feature builds on the existing `SecurityProperties` / `ManagementAuthenticatorProperties` configuration hierarchy, the `SessionJwtAuthenticationFilter`, Redis for token caching, and PostgreSQL for durable credential storage — no new runtime dependencies are required.

---

## 2. Problem Statement

### 2.1 Current State

| Concern | Current Behaviour | Impact |
|---|---|---|
| **M2M authentication** | No dedicated credential pair; services share session JWTs | Human user sessions are incorrectly used for automated calls |
| **Secret management** | No lifecycle management API | Secrets cannot be rotated without redeployment or manual DB intervention |
| **Scope enforcement** | Not enforced per client | Any authenticated entity can reach any endpoint |
| **Audit trail** | `AuditEventType` enum exists but M2M events are not modelled | Compliance / forensic investigations are not possible for M2M flows |
| **Token introspection for M2M** | Existing `/api/v1/iam/tokens/introspect` only covers session JWTs | Other services cannot validate a client-credentials token issued by this service |

### 2.2 Desired State

A registered client application obtains a short-lived **M2M access token** using `client_id` + `client_secret` (OAuth2 Client Credentials Grant pattern). Every call to a protected `backbone-rest` resource carries this token. Secrets can be rotated or revoked at any time by an authorised admin without service interruption.

---

## 3. Stakeholders

| Role | Name / Team | Interest |
|---|---|---|
| Product Owner | TASK-02 requestor | Business value, delivery sign-off |
| Platform Engineers | backbone-rest dev team | Implementation |
| Security Team | Ops / InfoSec | Secret governance, audit compliance |
| Client Service Teams | Any service calling backbone-rest | Stable, well-documented credential API |
| QA / Test Writer | Test agent | Test coverage of acceptance criteria |

---

## 4. Functional Requirements

### 4.1 🗂️ Client Registration

| ID | Requirement |
|---|---|
| **FR-01** | The system **MUST** allow an admin user to register a new managed client with a `name`, `description`, `scopes`, `applicationId` link, and an optional `redirectUri`. |
| **FR-02** | On registration the system **MUST** generate a cryptographically random `client_id` (UUID v4) and a `client_secret` (minimum 32-byte, Base64URL-encoded). |
| **FR-03** | The `client_secret` **MUST** be returned to the caller exactly once (at creation time) and **MUST NOT** be retrievable via any subsequent read operation. Only a hashed form is stored. |
| **FR-04** | The system **MUST** store a `bcrypt`-hashed version of the `client_secret` in the database. The raw secret **MUST NOT** appear in logs, error messages, or audit records. |
| **FR-05** | The system **MUST** support activation (`active=true`) and deactivation (`active=false`) of a registered client without deleting the record. |

### 4.2 🔑 Token Issuance (Client Credentials Grant)

| ID | Requirement |
|---|---|
| **FR-06** | The system **MUST** expose a public token endpoint `POST /api/v1/managed-clients/token` that accepts `client_id` + `client_secret` and issues a signed **M2M JWT** (access token). |
| **FR-07** | The issued M2M token **MUST** include claims: `sub` (client_id), `iss` (backbone-rest issuer URI), `aud` (requested scope list), `exp` (configurable TTL, default 3600 s), `iat`, `jti` (unique token ID), `client_name`, `scopes`. |
| **FR-08** | The token endpoint **MUST** be excluded from `bearerAuth` and `sessionToken` security schemes (public endpoint, analogous to `/api/v1/session`). |
| **FR-09** | The system **MUST** cache the issued token reference (jti → expiry) in Redis to support fast revocation checks. |
| **FR-10** | If a client is inactive or the secret is incorrect, the endpoint **MUST** return `401 Unauthorized` with an `{"error":"invalid_client"}` body. The response time for invalid credentials **MUST NOT** leak timing information (constant-time comparison). |

### 4.3 🔄 Secret Rotation

| ID | Requirement |
|---|---|
| **FR-11** | The system **MUST** allow an admin to rotate the `client_secret` for a given client via `POST /api/v1/managed-clients/{clientId}/rotate-secret`. |
| **FR-12** | During rotation the **old secret MUST remain valid** for a configurable grace period (`umdc.security.managementAuthenticator.rotationGracePeriodSeconds`, default 300 s) to allow zero-downtime secret rollout. |
| **FR-13** | After the grace period expires, the old secret **MUST** be invalidated and removed from storage. |
| **FR-14** | Secret rotation **MUST** emit an `AUDIT_CLIENT_SECRET_ROTATED` event to the audit log. |

### 4.4 🚫 Revocation

| ID | Requirement |
|---|---|
| **FR-15** | The system **MUST** allow an admin to immediately revoke all active tokens for a client via `DELETE /api/v1/managed-clients/{clientId}/tokens`. |
| **FR-16** | On revocation the system **MUST** invalidate the corresponding Redis cache entries so that in-flight tokens are rejected on the next introspection. |
| **FR-17** | Deactivating a client (`active=false`) **MUST** implicitly revoke all its active tokens. |

### 4.5 🔍 Token Introspection (M2M)

| ID | Requirement |
|---|---|
| **FR-18** | The system **MUST** extend `POST /api/v1/iam/tokens/introspect` (or introduce `POST /api/v1/managed-clients/introspect`) to handle M2M tokens in addition to session JWTs. The token type is differentiated by the `types` claim. |
| **FR-19** | Introspection **MUST** return `active=false` (never an error) for expired, revoked, or malformed M2M tokens. |
| **FR-20** | Introspection for a valid M2M token **MUST** return: `active`, `client_id`, `client_name`, `scopes`, `exp`, `iat`, `jti`. |

### 4.6 📜 Scope Management

| ID | Requirement |
|---|---|
| **FR-21** | Each registered client **MUST** be associated with one or more named **scopes** (e.g., `backbone:users:read`, `backbone:roles:write`). |
| **FR-22** | The system **MUST** validate that a requested scope set at token-issuance time is a subset of the client's registered scopes. Requesting an out-of-range scope **MUST** return `400 Bad Request` with `{"error":"invalid_scope"}`. |
| **FR-23** | Protected endpoints **MUST** declare their required scope(s). Requests carrying a token without the required scope **MUST** receive `403 Forbidden`. |

### 4.7 📊 Audit Trail

| ID | Requirement |
|---|---|
| **FR-24** | The system **MUST** record audit events for: client registration, client update, client deactivation, secret rotation, token issuance (success/failure), token revocation, and introspection calls. |
| **FR-25** | Audit events **MUST** be queryable via the existing `GET /api/v1/iam/audit/events` endpoint, filtered by the new `eventType` values defined for M2M flows. |
| **FR-26** | Each audit record **MUST** capture: `clientId`, `eventType`, `ipAddress`, `occurredAt`, `outcome` (`SUCCESS` / `FAILURE`), `details` (non-sensitive). |

### 4.8 📖 Client CRUD (Admin)

| ID | Requirement |
|---|---|
| **FR-27** | Admin **MUST** be able to list all registered clients (paginated) via `GET /api/v1/managed-clients`. |
| **FR-28** | Admin **MUST** be able to retrieve a single client record (excluding secret hash) via `GET /api/v1/managed-clients/{clientId}`. |
| **FR-29** | Admin **MUST** be able to update a client's `name`, `description`, `scopes`, and `active` flag via `PUT /api/v1/managed-clients/{clientId}`. |
| **FR-30** | Admin **MUST** be able to delete a client record via `DELETE /api/v1/managed-clients/{clientId}`. Deletion **MUST** also revoke all active tokens. |

---

## 5. Non-Functional Requirements

### 5.1 🔒 Security

| ID | Requirement |
|---|---|
| **NFR-S-01** | All `client_secret` values in transit **MUST** be transported over TLS only. |
| **NFR-S-02** | Secrets **MUST NOT** be stored in plaintext; only a `BCryptPasswordEncoder` (strength ≥ 12) hash is persisted. |
| **NFR-S-03** | No secret, hash, or key material **MUST** appear in application logs at any log level. |
| **NFR-S-04** | All configuration values for keystore/truststore passwords, secret signing keys, and Redis passwords **MUST** be injected via environment variables or Spring Cloud Config — **no hardcoded values**. |
| **NFR-S-05** | M2M tokens **MUST** be signed using the existing keystore referenced by `umdc.security.managementAuthenticator.keystore` and `keyAlias`. |
| **NFR-S-06** | Token TTL **MUST** be configurable (`umdc.security.managementAuthenticator.tokenTtlSeconds`) and **MUST NOT** exceed 86400 s (24 h). Default: 3600 s. |
| **NFR-S-07** | Admin endpoints under `/api/v1/managed-clients/**` (except `POST /token`) **MUST** require a valid `bearerAuth` JWT with scope `backbone:admin` or role `ROLE_ADMIN`. |

### 5.2 ⚡ Performance

| ID | Requirement |
|---|---|
| **NFR-P-01** | Token issuance (`POST /token`) **MUST** respond in ≤ 250 ms at p99 under 50 concurrent requests. |
| **NFR-P-02** | Token introspection **MUST** respond in ≤ 100 ms at p99 for cached (Redis) lookups. |
| **NFR-P-03** | Secret hashing (BCrypt) **MUST** be performed asynchronously or on a dedicated thread pool to avoid blocking the Tomcat request threads. |

### 5.3 📈 Scalability

| ID | Requirement |
|---|---|
| **NFR-SC-01** | The feature **MUST** operate correctly in a horizontally scaled deployment (multiple backbone-rest instances) by relying on the shared Redis cache for token state. |
| **NFR-SC-02** | The PostgreSQL schema for managed clients **MUST** include appropriate indexes on `client_id`, `application_id`, and `active` columns. |

### 5.4 🧪 Testability & Quality

| ID | Requirement |
|---|---|
| **NFR-Q-01** | All new service classes **MUST** pass PMD analysis with **zero violations** (priority ≤ 5). |
| **NFR-Q-02** | Minimum unit test coverage for service layer: **80% line coverage**. |
| **NFR-Q-03** | All new endpoints **MUST** have integration tests using `@SpringBootTest` + MockMvc (or `@WebMvcTest`). |

### 5.5 🔄 Backward Compatibility

| ID | Requirement |
|---|---|
| **NFR-BC-01** | No existing endpoint under `/api/v1/*` **MUST** change its request/response contract. |
| **NFR-BC-02** | The existing `AuditEventType` enum **MUST** be extended (new values added), not replaced. |

---

## 6. API Contract Overview

> All admin endpoints require `Authorization: Bearer <admin-jwt>` unless marked 🔓 Public.

### Base Path: `/api/v1/managed-clients`

| Method | Path | Auth | Summary | Success | Error Codes |
|---|---|---|---|---|---|
| `POST` | `/api/v1/managed-clients` | 🔐 Bearer | Register a new managed client | `201 Created` | `400`, `409` |
| `GET` | `/api/v1/managed-clients` | 🔐 Bearer | List all managed clients (paginated) | `200 OK` | `401`, `403` |
| `GET` | `/api/v1/managed-clients/{clientId}` | 🔐 Bearer | Get managed client detail | `200 OK` | `401`, `403`, `404` |
| `PUT` | `/api/v1/managed-clients/{clientId}` | 🔐 Bearer | Update managed client metadata | `200 OK` | `400`, `401`, `403`, `404` |
| `DELETE` | `/api/v1/managed-clients/{clientId}` | 🔐 Bearer | Delete client + revoke all tokens | `204 No Content` | `401`, `403`, `404` |
| `POST` | `/api/v1/managed-clients/token` | 🔓 Public | Issue M2M access token | `200 OK` | `400`, `401` |
| `POST` | `/api/v1/managed-clients/{clientId}/rotate-secret` | 🔐 Bearer | Rotate client secret | `200 OK` | `401`, `403`, `404` |
| `DELETE` | `/api/v1/managed-clients/{clientId}/tokens` | 🔐 Bearer | Revoke all active tokens for client | `204 No Content` | `401`, `403`, `404` |
| `POST` | `/api/v1/managed-clients/introspect` | 🔐 Bearer | Introspect an M2M token | `200 OK` | `400`, `401` |

### OpenAPI Contract Fragment

```yaml
tags:
  - name: "managed-clients"
    description: "Management Client Authentication Manager — M2M credential lifecycle"

paths:

  /api/v1/managed-clients:
    post:
      tags: ["managed-clients"]
      operationId: "registerManagedClient"
      summary: "Register a new managed client"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ManagedClientCreateRequest"
      responses:
        "201":
          description: "Client registered — client_secret returned once only"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ManagedClientCreateResponse"
        "400":
          description: "Invalid request payload"
        "409":
          description: "Client name already registered for this application"
    get:
      tags: ["managed-clients"]
      operationId: "listManagedClients"
      summary: "List all managed clients (paginated)"
      parameters:
        - name: "applicationId"
          in: "query"
          required: false
          schema:
            type: "string"
            format: "uuid"
        - name: "active"
          in: "query"
          required: false
          schema:
            type: "boolean"
        - name: "page"
          in: "query"
          required: false
          schema:
            type: "integer"
            default: 0
        - name: "size"
          in: "query"
          required: false
          schema:
            type: "integer"
            default: 20
      responses:
        "200":
          description: "Paginated list of managed clients"
          content:
            application/json:
              schema:
                type: "array"
                items:
                  $ref: "#/components/schemas/ManagedClientTO"
        "204":
          description: "No clients found matching filters"
        "401":
          description: "Missing or invalid bearer token"

  /api/v1/managed-clients/{clientId}:
    get:
      tags: ["managed-clients"]
      operationId: "getManagedClient"
      summary: "Get managed client detail"
      parameters:
        - name: "clientId"
          in: "path"
          required: true
          schema:
            type: "string"
            format: "uuid"
      responses:
        "200":
          description: "Managed client found"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ManagedClientTO"
        "404":
          description: "Client not found"
    put:
      tags: ["managed-clients"]
      operationId: "updateManagedClient"
      summary: "Update managed client metadata"
      parameters:
        - name: "clientId"
          in: "path"
          required: true
          schema:
            type: "string"
            format: "uuid"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ManagedClientUpdateRequest"
      responses:
        "200":
          description: "Client updated"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ManagedClientTO"
        "400":
          description: "Invalid payload"
        "404":
          description: "Client not found"
    delete:
      tags: ["managed-clients"]
      operationId: "deleteManagedClient"
      summary: "Delete client and revoke all tokens"
      parameters:
        - name: "clientId"
          in: "path"
          required: true
          schema:
            type: "string"
            format: "uuid"
      responses:
        "204":
          description: "Client deleted"
        "404":
          description: "Client not found"

  /api/v1/managed-clients/token:
    post:
      tags: ["managed-clients"]
      operationId: "issueManagedClientToken"
      summary: "Issue an M2M access token (Client Credentials Grant)"
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ManagedClientTokenRequest"
      responses:
        "200":
          description: "M2M token issued"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ManagedClientTokenResponse"
        "400":
          description: "Missing or invalid scope requested"
        "401":
          description: "Invalid client credentials"

  /api/v1/managed-clients/{clientId}/rotate-secret:
    post:
      tags: ["managed-clients"]
      operationId: "rotateManagedClientSecret"
      summary: "Rotate client secret"
      parameters:
        - name: "clientId"
          in: "path"
          required: true
          schema:
            type: "string"
            format: "uuid"
      responses:
        "200":
          description: "New secret issued — returned once only"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ManagedClientSecretRotateResponse"
        "404":
          description: "Client not found"

  /api/v1/managed-clients/{clientId}/tokens:
    delete:
      tags: ["managed-clients"]
      operationId: "revokeAllClientTokens"
      summary: "Revoke all active tokens for a managed client"
      parameters:
        - name: "clientId"
          in: "path"
          required: true
          schema:
            type: "string"
            format: "uuid"
      responses:
        "204":
          description: "All tokens revoked"
        "404":
          description: "Client not found"

  /api/v1/managed-clients/introspect:
    post:
      tags: ["managed-clients"]
      operationId: "introspectManagedClientToken"
      summary: "Introspect an M2M access token"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/TokenIntrospectRequest"
      responses:
        "200":
          description: "Introspection result"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ManagedClientTokenIntrospectResponse"
        "400":
          description: "Token is blank or missing"

components:
  schemas:

    ManagedClientCreateRequest:
      type: "object"
      required: ["name", "applicationId", "scopes"]
      properties:
        name:
          type: "string"
          maxLength: 128
        description:
          type: ["string", "null"]
          maxLength: 512
        applicationId:
          type: "string"
          format: "uuid"
        scopes:
          type: "array"
          minItems: 1
          items:
            type: "string"
        active:
          type: "boolean"
          default: true

    ManagedClientCreateResponse:
      type: "object"
      properties:
        clientId:
          type: "string"
          format: "uuid"
        clientSecret:
          type: "string"
          description: "Plain-text secret — returned ONCE only. Store securely."
        name:
          type: "string"
        applicationId:
          type: "string"
          format: "uuid"
        scopes:
          type: "array"
          items:
            type: "string"
        active:
          type: "boolean"
        createdAt:
          type: "string"
          format: "date-time"

    ManagedClientTO:
      type: "object"
      description: "Managed client record — never includes secret material"
      properties:
        clientId:
          type: "string"
          format: "uuid"
        name:
          type: "string"
        description:
          type: ["string", "null"]
        applicationId:
          type: "string"
          format: "uuid"
        scopes:
          type: "array"
          items:
            type: "string"
        active:
          type: "boolean"
        createdAt:
          type: "string"
          format: "date-time"
        lastUpdatedAt:
          type: ["string", "null"]
          format: "date-time"
        secretLastRotatedAt:
          type: ["string", "null"]
          format: "date-time"

    ManagedClientUpdateRequest:
      type: "object"
      properties:
        name:
          type: ["string", "null"]
          maxLength: 128
        description:
          type: ["string", "null"]
          maxLength: 512
        scopes:
          type: ["array", "null"]
          items:
            type: "string"
        active:
          type: ["boolean", "null"]

    ManagedClientTokenRequest:
      type: "object"
      required: ["clientId", "clientSecret"]
      properties:
        clientId:
          type: "string"
          format: "uuid"
        clientSecret:
          type: "string"
        scopes:
          type: ["array", "null"]
          description: "Requested scope subset. Defaults to all registered scopes if omitted."
          items:
            type: "string"

    ManagedClientTokenResponse:
      type: "object"
      properties:
        accessToken:
          type: "string"
          description: "Signed M2M JWT"
        tokenType:
          type: "string"
          enum: ["Bearer"]
        expiresIn:
          type: "integer"
          format: "int64"
          description: "TTL in seconds"
        scopes:
          type: "array"
          items:
            type: "string"

    ManagedClientSecretRotateResponse:
      type: "object"
      properties:
        clientId:
          type: "string"
          format: "uuid"
        clientSecret:
          type: "string"
          description: "New plain-text secret — returned ONCE only."
        gracePeriodSeconds:
          type: "integer"
          description: "Seconds for which the old secret remains valid"
        rotatedAt:
          type: "string"
          format: "date-time"

    ManagedClientTokenIntrospectResponse:
      type: "object"
      properties:
        active:
          type: "boolean"
        clientId:
          type: ["string", "null"]
          format: "uuid"
        clientName:
          type: ["string", "null"]
        scopes:
          type: "array"
          items:
            type: "string"
        issuer:
          type: ["string", "null"]
        expiresAt:
          type: "integer"
          format: "int64"
        issuedAt:
          type: "integer"
          format: "int64"
        jti:
          type: ["string", "null"]
```

---

## 7. Data Model Overview

### 7.1 Entity: `ManagedClient`

```
managed_client
├── id                UUID          PK
├── name              VARCHAR(128)  NOT NULL  UNIQUE(name, application_id)
├── description       VARCHAR(512)
├── application_id    UUID          FK → application.id
├── secret_hash       VARCHAR(255)  NOT NULL  (BCrypt)
├── prev_secret_hash  VARCHAR(255)            (grace period rotation)
├── scopes            TEXT[]        NOT NULL
├── active            BOOLEAN       NOT NULL  DEFAULT true
├── created_at        TIMESTAMPTZ   NOT NULL
├── last_updated_at   TIMESTAMPTZ
└── secret_last_rotated_at TIMESTAMPTZ
```

### 7.2 Entity: `ManagedClientAuditEvent` (extends existing audit structure)

```
managed_client_audit_event
├── id              UUID          PK
├── client_id       UUID          FK → managed_client.id
├── event_type      VARCHAR(64)   NOT NULL  (enum value)
├── ip_address      VARCHAR(64)
├── outcome         VARCHAR(16)   NOT NULL  ('SUCCESS' | 'FAILURE')
├── details         TEXT                    (non-sensitive context)
└── occurred_at     TIMESTAMPTZ   NOT NULL
```

### 7.3 Redis Key Schema

| Key Pattern | Type | TTL | Purpose |
|---|---|---|---|
| `mcam:token:{jti}` | String | token expiry | Active token presence check |
| `mcam:revoked:{jti}` | String | token expiry + 60 s | Revocation tombstone |
| `mcam:grace:{clientId}` | String | `rotationGracePeriodSeconds` | Previous secret hash during rotation |

### 7.4 DTOs / MapStruct Mappings

| Source Entity | Target DTO | Mapper Interface |
|---|---|---|
| `ManagedClient` | `ManagedClientTO` | `ManagedClientMapper` |
| `ManagedClientCreateRequest` | `ManagedClient` | `ManagedClientMapper` |
| `ManagedClientUpdateRequest` | `ManagedClient` (partial) | `ManagedClientMapper` |

> **Note:** `ManagedClientMapper` **MUST** use `@Mapper(componentModel = "spring")` and be injected via constructor injection in all service classes.

### 7.5 New `AuditEventType` Values

The existing `AuditEventType` enum in `api.yaml` **MUST** be extended with:

```yaml
- "CLIENT_REGISTERED"
- "CLIENT_UPDATED"
- "CLIENT_DEACTIVATED"
- "CLIENT_DELETED"
- "CLIENT_SECRET_ROTATED"
- "CLIENT_TOKEN_ISSUED"
- "CLIENT_TOKEN_ISSUE_FAILED"
- "CLIENT_TOKEN_REVOKED"
- "CLIENT_INTROSPECTION_CALLED"
```

---

## 8. Security Considerations

### 8.1 Secret Storage & Transmission

```
Registration Flow
─────────────────
Client (Admin) ──POST /managed-clients──► Controller
                                           │
                                           ▼
                                    Generate random secret
                                    (SecureRandom, 32 bytes, Base64URL)
                                           │
                               ┌───────────┴──────────────┐
                               ▼                          ▼
                        Return plaintext           BCrypt hash (strength=12)
                        ONCE in response           stored in DB only
                               │
                       (never stored, never logged)
```

### 8.2 Token Signing

M2M tokens **MUST** be signed using the **asymmetric key** referenced by:
```yaml
umdc.security.managementAuthenticator:
  keyAlias: ${MCAM_KEY_ALIAS}
  keystore:
    location: ${MCAM_KEYSTORE_LOCATION}
    password: ${MCAM_KEYSTORE_PASSWORD}
    type: PKCS12
```

No symmetric HS256 signing is permitted for M2M tokens.

### 8.3 Scope-Based Access Control

```
Incoming M2M Request
─────────────────────
Token in Authorization header
        │
        ▼
SessionJwtAuthenticationFilter (skip — no session-token header)
        │
        ▼
ManagedClientTokenFilter (NEW — validates M2M JWT from Authorization: Bearer)
        │
        ├── valid + has required scope? ──► proceed
        ├── valid + missing scope?       ──► 403 Forbidden
        └── invalid / revoked?           ──► 401 Unauthorized
```

### 8.4 Rate Limiting

The token endpoint (`POST /api/v1/managed-clients/token`) **MUST** be rate-limited to prevent brute-force secret enumeration. Implementation detail is left to the developer; a simple Redis-backed sliding window counter per `client_id` is recommended.

### 8.5 Environment Variable Manifest

| Variable | Purpose | Example |
|---|---|---|
| `MCAM_KEY_ALIAS` | Key alias in keystore for M2M token signing | `mcam-signing-key` |
| `MCAM_KEYSTORE_LOCATION` | Path to PKCS12 keystore | `classpath:keystore/mcam.p12` |
| `MCAM_KEYSTORE_PASSWORD` | Keystore password | _(from secrets manager)_ |
| `MCAM_TOKEN_TTL_SECONDS` | M2M token TTL | `3600` |
| `MCAM_ROTATION_GRACE_SECONDS` | Grace period for old secret after rotation | `300` |
| `MCAM_RATE_LIMIT_RPM` | Max token requests per minute per client_id | `60` |

---

## 9. Architecture Constraints

> These constraints **MUST** be honoured. They are non-negotiable.

| # | Constraint | Detail |
|---|---|---|
| **AC-01** | Interface-first pattern | New controller **MUST** implement a `ManagedClientApi` interface; concrete class is `ManagedClientController`. |
| **AC-02** | `ResponseEntity<?>` return type | All controller methods **MUST** return `ResponseEntity<?>`. |
| **AC-03** | Constructor injection only | No `@Autowired` field injection. All dependencies via constructor. |
| **AC-04** | PMD zero violations | `mvn pmd:check` **MUST** pass with 0 violations at priority ≤ 5. |
| **AC-05** | MapStruct for all mappings | No manual `new DTO()` construction from entities in service or controller layers. |
| **AC-06** | Integration with `SecurityConfig` | The new `ManagedClientTokenFilter` **MUST** be registered in the existing `SecurityFilterChain` after `SessionJwtAuthenticationFilter`. |
| **AC-07** | No new runtime dependencies | Feature **MUST** be implemented using the existing dependency set (JJWT 0.12.6, Spring Security, Redis, PostgreSQL, MapStruct). |
| **AC-08** | Package convention | New classes under `com.umdc.backoffice.v1.managedclient.*` following the existing `v1.<domain>.*` pattern. |
| **AC-09** | No hardcoded secrets | All key material, passwords, and TTL values from environment variables via `@ConfigurationProperties`. |
| **AC-10** | Backward compatibility | No existing API contract under `/api/v1/*` changes. |

---

## 10. Out of Scope

The following items are explicitly **excluded** from this feature:

| Item | Reason |
|---|---|
| OAuth2 Authorization Code / PKCE flows | User-facing auth remains the responsibility of the session endpoints |
| Dynamic scope definition UI | Scopes are string values registered at client creation; no separate scope registry API |
| Multi-tenancy / namespace isolation | All clients are application-scoped; full tenant isolation is a future initiative |
| External OAuth2 Authorization Server integration | backbone-rest acts as its own issuer for M2M tokens; delegation to Keycloak/Okta is out of scope |
| Refresh tokens for M2M | Client Credentials Grant does not use refresh tokens (re-authenticate when expired) |
| PKCS#11 / HSM key storage | Software keystore (PKCS12) is sufficient for v1 |
| Self-service client registration | Registration is admin-only; self-service portal is a future product decision |

---

## 11. Acceptance Criteria

### Summary Table

| # | ID | Category | Given | When | Then | Priority |
|---|---|---|---|---|---|---|
| 1 | **AC-REG-01** | Registration | A valid `ManagedClientCreateRequest` with name, applicationId, and scopes | `POST /api/v1/managed-clients` | `201 Created` with `clientId`, `clientSecret` (plaintext, once only), `scopes`, `createdAt` | 🔴 Must |
| 2 | **AC-REG-02** | Registration — Conflict | A `ManagedClientCreateRequest` where the name already exists for that `applicationId` | `POST /api/v1/managed-clients` | `409 Conflict` with no secret leaked in response | 🔴 Must |
| 3 | **AC-REG-03** | Registration — Validation | A `ManagedClientCreateRequest` with missing `name` or empty `scopes` | `POST /api/v1/managed-clients` | `400 Bad Request` with field-level validation errors | 🔴 Must |
| 4 | **AC-TOK-01** | Token Issuance — Happy Path | A valid `clientId` and `clientSecret` for an active client | `POST /api/v1/managed-clients/token` | `200 OK` with signed JWT containing `sub`, `iss`, `aud`, `exp`, `iat`, `jti`, `scopes`, `type=M2M` | 🔴 Must |
| 5 | **AC-TOK-02** | Token Issuance — Invalid Secret | A valid `clientId` with an incorrect `clientSecret` | `POST /api/v1/managed-clients/token` | `401 Unauthorized` with `{"error":"invalid_client"}` and no timing leak | 🔴 Must |
| 6 | **AC-TOK-03** | Token Issuance — Inactive Client | A valid `clientId` for a client where `active=false` | `POST /api/v1/managed-clients/token` | `401 Unauthorized` with `{"error":"invalid_client"}` | 🔴 Must |
| 7 | **AC-TOK-04** | Token Issuance — Scope Overflow | A `ManagedClientTokenRequest` requesting a scope not in the client's registered scope list | `POST /api/v1/managed-clients/token` | `400 Bad Request` with `{"error":"invalid_scope","requested":"X","allowed":["Y","Z"]}` | 🔴 Must |
| 8 | **AC-ROT-01** | Secret Rotation | An active managed client | `POST /api/v1/managed-clients/{clientId}/rotate-secret` | `200 OK` with new `clientSecret`, old secret still valid for `gracePeriodSeconds`; `CLIENT_SECRET_ROTATED` audit event recorded | 🔴 Must |
| 9 | **AC-REV-01** | Token Revocation | Active tokens exist for a client | `DELETE /api/v1/managed-clients/{clientId}/tokens` | `204 No Content`; subsequent introspection of previously valid token returns `active=false` | 🔴 Must |
| 10 | **AC-INT-01** | Introspection — Active Token | A valid, non-expired, non-revoked M2M JWT | `POST /api/v1/managed-clients/introspect` | `200 OK` with `active=true`, `clientId`, `clientName`, `scopes`, `exp`, `iat`, `jti` | 🔴 Must |
| 11 | **AC-INT-02** | Introspection — Expired / Revoked Token | An expired or revoked M2M JWT | `POST /api/v1/managed-clients/introspect` | `200 OK` with `active=false` (never a 4xx) | 🔴 Must |
| 12 | **AC-AUD-01** | Audit Trail | Any MCAM lifecycle operation (register, rotate, issue, revoke) | Corresponding admin action completes | A corresponding audit record is persisted, queryable via `GET /api/v1/iam/audit/events?eventType=CLIENT_TOKEN_ISSUED` | 🔴 Must |
| 13 | **AC-SEC-01** | Secret Not Persisted in Plaintext | A new client is registered | DB record is inspected | `secret_hash` column contains a BCrypt hash; plaintext never present | 🔴 Must |
| 14 | **AC-SEC-02** | No Secret in GET Response | An existing managed client is fetched | `GET /api/v1/managed-clients/{clientId}` | Response body contains no `clientSecret`, `secretHash`, or password-like field | 🔴 Must |
| 15 | **AC-PMD-01** | Code Quality | All new Java source files are committed | `mvn pmd:check` executes | Build passes with **0 PMD violations** | 🔴 Must |

---

## 12. Example Payloads

### 12.1 Register Managed Client

**Request**
```http
POST /api/v1/managed-clients
Authorization: Bearer <admin-jwt>
Content-Type: application/json

{
  "name": "inventory-service",
  "description": "Inventory microservice — read access to users and roles",
  "applicationId": "a1b2c3d4-0000-0000-0000-000000000001",
  "scopes": ["backbone:users:read", "backbone:roles:read"],
  "active": true
}
```

**Response `201 Created`**
```json
{
  "clientId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "clientSecret": "X9kL2mNpQr7sVwYzAbCdEfGhIjKlMnOpQrStUvWxYz0=",
  "name": "inventory-service",
  "applicationId": "a1b2c3d4-0000-0000-0000-000000000001",
  "scopes": ["backbone:users:read", "backbone:roles:read"],
  "active": true,
  "createdAt": "2025-07-14T10:30:00Z"
}
```

> ⚠️ `clientSecret` is returned **exactly once**. The caller must store it securely.

---

### 12.2 Issue M2M Access Token

**Request**
```http
POST /api/v1/managed-clients/token
Content-Type: application/json

{
  "clientId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "clientSecret": "X9kL2mNpQr7sVwYzAbCdEfGhIjKlMnOpQrStUvWxYz0=",
  "scopes": ["backbone:users:read"]
}
```

**Response `200 OK`**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "scopes": ["backbone:users:read"]
}
```

**JWT Payload (decoded)**
```json
{
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "iss": "https://backbone-rest.umdc.internal",
  "aud": ["backbone:users:read"],
  "exp": 1752583800,
  "iat": 1752580200,
  "jti": "7e3f8c91-a1b2-4d3e-8f5a-6b7c8d9e0f1a",
  "client_name": "inventory-service",
  "scopes": ["backbone:users:read"],
  "type": "M2M"
}
```

---

### 12.3 Rotate Secret

**Request**
```http
POST /api/v1/managed-clients/f47ac10b-58cc-4372-a567-0e02b2c3d479/rotate-secret
Authorization: Bearer <admin-jwt>
```

**Response `200 OK`**
```json
{
  "clientId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "clientSecret": "NewR4nd0mS3cr3tV4lu3Base64URL==",
  "gracePeriodSeconds": 300,
  "rotatedAt": "2025-07-14T11:00:00Z"
}
```

---

### 12.4 Introspect M2M Token

**Request**
```http
POST /api/v1/managed-clients/introspect
Authorization: Bearer <admin-or-service-jwt>
Content-Type: application/json

{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response `200 OK` — Active Token**
```json
{
  "active": true,
  "clientId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "clientName": "inventory-service",
  "scopes": ["backbone:users:read"],
  "issuer": "https://backbone-rest.umdc.internal",
  "expiresAt": 1752583800,
  "issuedAt": 1752580200,
  "jti": "7e3f8c91-a1b2-4d3e-8f5a-6b7c8d9e0f1a"
}
```

**Response `200 OK` — Revoked / Expired Token**
```json
{
  "active": false,
  "clientId": null,
  "clientName": null,
  "scopes": [],
  "issuer": null,
  "expiresAt": 0,
  "issuedAt": 0,
  "jti": null
}
```

---

### 12.5 Error Responses

**`401` Invalid Client**
```json
{
  "error": "invalid_client",
  "error_description": "The client credentials are invalid or the client is inactive."
}
```

**`400` Invalid Scope**
```json
{
  "error": "invalid_scope",
  "error_description": "Requested scope exceeds client registration.",
  "requested": "backbone:users:write",
  "allowed": ["backbone:users:read", "backbone:roles:read"]
}
```

**`409` Name Conflict**
```json
{
  "error": "conflict",
  "error_description": "A managed client named 'inventory-service' already exists for this application."
}
```

---

## 13. Dependencies & Risks

### 13.1 Dependencies

| Dependency | Type | Notes |
|---|---|---|
| `umdc.security.managementAuthenticator` config | Internal | Keystore/truststore must be provisioned before deployment |
| Redis | Infrastructure | Already in use; must support key TTL and atomic operations |
| PostgreSQL | Infrastructure | New tables `managed_client`, `managed_client_audit_event` require migration scripts |
| Existing `SecurityConfig` | Code | `ManagedClientTokenFilter` must be registered in the correct filter chain order |
| `AuditEventType` enum | Code | Enum extension must not break existing serialisation (OpenAPI discriminator) |

### 13.2 Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| BCrypt hashing time causing p99 > 250 ms | Medium | High | Run BCrypt on a dedicated `@Async` thread pool; cache result in Redis |
| Key rotation breaks existing M2M tokens | Medium | High | Implement JWKS endpoint for public key discovery (future scope flag) |
| Scope list grows unbounded | Low | Medium | Enforce max 20 scopes per client at registration validation layer |
| Redis unavailability blocking token validation | Low | High | Fall back to DB-based revocation check with circuit breaker pattern |
| Old secret not cleaned after grace period | Medium | Medium | Use Redis TTL-based expiry (Redis handles cleanup automatically) |

---

## 📌 Document Control

| Field | Value |
|---|---|
| **Document ID** | TASK-02 |
| **Author** | Product Owner Agent |
| **Created** | 2025-07-14 |
| **Status** | Draft — Pending Developer & QA Review |
| **Target Sprint** | TBD |
| **Review By** | Developer Agent · QA Agent · Security Team |
| **Approval** | Product Owner |

---

*End of Document*

