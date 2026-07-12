# 🚀 Backbone REST — API Technical Reference

> **Version:** v1 &nbsp;|&nbsp; **Protocol:** HTTPS/TLS 1.3 &nbsp;|&nbsp; **Port:** `8084`  
> **Base URL:** `https://<host>:8084/api/v1`  
> **License:** Proprietary — Copyright © 2024–2026 Luis Mata

---

## 🎨 Method Legend

| Badge | Method | Typical use |
|-------|--------|-------------|
| 🟢 | `GET` | Read / query — safe, idempotent |
| 🔵 | `POST` | Create resource or trigger action |
| 🟡 | `PUT` | Full or partial update |
| 🔴 | `DELETE` | Remove resource |

---

## ⚡ Quick Start

### 1 — Obtain a session token

```bash
curl -k -s -X POST https://<host>:8084/api/v1/session \
  -H "Content-Type: application/json" \
  -d '{"alias":"<alias>","password":"<password>","applicationId":"<app-uuid>"}' \
  | jq .sessionToken
```

### 2 — Call a protected endpoint

```bash
curl -k -X GET https://<host>:8084/api/v1/users/user/<user-uuid> \
  -H "Authorization: Bearer <oauth2-jwt>"
```

> [!NOTE]
> Most protected endpoints require an **OAuth2 Bearer JWT** issued by Supabase / Keycloak.  
> Profile image endpoints use the **session-token** header instead.  
> MCAM `/token` and `/introspect` endpoints are **public** (no auth required).

---

## 🔐 Authentication Matrix

| Auth type | Header | Issued by | Used on |
|-----------|--------|-----------|---------|
| OAuth2 Bearer JWT | `Authorization: Bearer <jwt>` | Supabase / Keycloak | All `/api/v1/**` except sessions & MCAM public |
| Session JWT | `session-token: <token>` | `POST /api/v1/session` | Profile image endpoints |
| None | — | — | `/session`, `/session/token`, `/session/refresh`, `/managed-clients/token`, `/managed-clients/introspect` |

---

## 📋 Standard Response Headers

Every response includes:

| Header | Description |
|--------|-------------|
| `X-Message` | Human-readable status description (e.g. `User found.`, `Not found.`) |
| `Content-Type` | `application/json` unless noted |

---

## 🗂️ Domain Index

| # | Domain | Base path | File | Endpoints |
|---|--------|-----------|------|-----------|
| 1 | Sessions & Auth | `/api/v1/session` | [session.md](./session.md) | 5 |
| 2 | Users | `/api/v1/users` | [users.md](./users.md) | 12 |
| 3 | Applications | `/api/v1/applications` | [applications.md](./applications.md) | 2 |
| 4 | Roles | `/api/v1/roles` | [roles.md](./roles.md) | 7 |
| 5 | Features | `/api/v1/features` | [features.md](./features.md) | 5 |
| 6 | Contacts | `/api/v1/contacts` | [contacts.md](./contacts.md) | 7 |
| 7 | Contact Types | `/api/v1/contact-types` | [contact-types.md](./contact-types.md) | 6 |
| 8 | People | `/api/v1/people` | [people.md](./people.md) | 4 |
| 9 | Profile Image | `/api/v1/profile/image` | [profile-image.md](./profile-image.md) | 3 |
| 10 | IAM — Permissions | `/api/v1/iam/permissions` | [iam-permissions.md](./iam-permissions.md) | 1 |
| 11 | IAM — Tokens | `/api/v1/iam/tokens` | [iam-tokens.md](./iam-tokens.md) | 1 |
| 12 | IAM — Audit | `/api/v1/iam/audit` | [iam-audit.md](./iam-audit.md) | 1 |
| 13 | Managed Clients (MCAM) | `/api/v1/managed-clients` | [managed-clients.md](./managed-clients.md) | 9 |
| 14 | Report / Document | `/api/v1/report` | [report.md](./report.md) | 2 |
| 15 | Service Types | `/api/v1/service-types` | [service-types.md](./service-types.md) | 5 |

**Total: 71 endpoints**

---

## 🚨 Global Error Codes

| HTTP Status | Code name | Typical cause |
|-------------|-----------|---------------|
| `400` | Bad Request | Null body, missing required field, validation failure |
| `401` | Unauthorized | Missing, expired, or invalid token |
| `403` | Forbidden | Valid token but insufficient role/permission |
| `404` | Not Found | Resource does not exist or list is empty |
| `406` | Not Acceptable | Business rule rejection (e.g. role/feature constraint) |
| `409` | Conflict | Duplicate unique field (name, alias, etc.) |
| `422` | Unprocessable Entity | MapStruct mapping returned null |
| `429` | Too Many Requests | Rate limit on MCAM token issuance |
| `500` | Internal Server Error | Unhandled exception |
| `501` | Not Implemented | Endpoint stub — not yet active |

---

## 🔢 Data Types

| Type | Format | Example |
|------|--------|---------|
| `UUID` | RFC 4122 v4 | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| `datetime` | ISO 8601 | `2026-07-12T14:30:00Z` |
| `boolean` | JSON | `true` / `false` |
| `string` | UTF-8 | `"ADMIN"` |

---

> **Developer Guide (user docs):** [../v1/README.md](../v1/README.md)
