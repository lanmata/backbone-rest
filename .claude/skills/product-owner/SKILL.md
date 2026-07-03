---
name: Product Owner Skills
description: Consolidated skill set for the Product Owner agent — acceptance criteria, API contracts, domain knowledge for backbone-rest
applies-to:
  - product-owner
---

# Product Owner — Skill Definition

## 1. Domain Knowledge

| Domain | Base Path | Key Operations |
|--------|-----------|---------------|
| users | `/api/v1/users` | CRUD, alias/email uniqueness check, role link/unlink |
| session | `/api/v1/sessions` | Token mint, validate, renew (bypass OAuth2 filter) |
| roles | `/api/v1/roles` | CRUD |
| contacts | `/api/v1/contacts` | CRUD |
| contacttypes | `/api/v1/contacttypes` | CRUD |
| features | `/api/v1/features` | CRUD |
| people | `/api/v1/people` | CRUD |
| profileimage | `/api/v1/profileimage` | Upload, retrieve (Supabase Storage) |
| managedclient | `/api/v1/managedclient` | Client credential CRUD + token rotation + audit |
| iam/audit | `/api/v1/iam/audit` | Audit event log read |
| iam/passwords | `/api/v1/iam/passwords` | Password change, reset |

---

## 2. Acceptance Criteria Format

Every criterion must be verifiable by a JUnit test or a `curl` command:

```markdown
- [ ] AC-1: `GET /api/v1/users/{id}` returns 200 with UserTO body when user exists
- [ ] AC-2: `GET /api/v1/users/{id}` returns 404 with Warning header when user not found
- [ ] AC-3: `POST /api/v1/users` with valid payload returns 201 with created user ID
- [ ] AC-4: `POST /api/v1/users` with duplicate alias returns 409
```

---

## 3. API Contract Principles

- No breaking changes to existing `/api/v1/*` endpoints without explicit approval.
- New fields must be optional (backward compatible).
- New endpoints must follow interface-first pattern and appear in `api.yaml`.
- Auth requirement: all `/api/v1/**` except `/v1/sessions/token` and `/v1/sessions/validate` require Bearer JWT.

---

## 4. Story Template

```markdown
### Story: <JIRA-ID> — <title>
**As a** <actor: admin / end-user / managed-client>
**I want** <action>
**So that** <business value>

#### Acceptance Criteria
- [ ] AC-1: ...
- [ ] AC-2: ...

#### API Contract (if new/changed endpoint)
- Method + Path: `POST /api/v1/users`
- Auth: Required (Bearer JWT)
- Request body: `{ "alias": "string", "email": "string", ... }`
- Response 201: `{ "id": "uuid", ... }`
- Response 409: `{ "message": "Alias already exists" }`

#### Out of Scope
- ...
```

---

## 5. Constraints

- Acceptance criteria must be specific enough for a developer to write a unit test without clarification.
- Do NOT define implementation details (controller name, class name) — only behavior.
- Auth model is fixed: OAuth2 (Keycloak/Supabase JWT) for all endpoints except session.
