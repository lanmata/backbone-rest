---
name: Product Owner Skills
description: Consolidated skill set for the Product Owner agent — requirements, acceptance criteria, backlog management, and API contract validation
applies-to:
  - Product Owner
---

# Product Owner — Skill Definition

## 1. Domain Knowledge

| Domain | Capabilities |
|--------|-------------|
| Users | CRUD, alias/email availability, role link/unlink, partial update |
| Session | JWT token generation (alias or email), validation, renewal |
| Roles | CRUD |
| Contacts | CRUD tied to people |
| Contact Types | CRUD enumeration management |
| Features | CRUD |
| People | CRUD (name, birthdate, gender, contacts) |
| Application | Application registration management |
| Profile Image | Upload and retrieval |
| Report | Report generation |

---

## 2. Acceptance Criteria

Use **Given/When/Then (Gherkin)** format. Every story must cover:
1. Happy path (with full JSON payload examples)
2. Validation failure (400)
3. Not-found case (404) where applicable
4. Conflict case (409) where applicable
5. Unauthorized (401 / 403)

### Template

```gherkin
Feature: <Feature Name>

  Background:
    Given the API is running at /api/v1/<domain>
    And the caller has a valid OAuth2 token with role ROLE_<role>

  Scenario: <Happy Path>
    Given <valid precondition>
    When <HTTP method> <path> is called with body:
      """json
      { ... }
      """
    Then the response status is <status>
    And the response body contains:
      """json
      { ... }
      """

  Scenario: <Error Case>
    Given <error precondition>
    When <HTTP method> <path> is called
    Then the response status is <error status>
    And the response Warning header contains "<message>"
```

### Example — Create User

```gherkin
Feature: Create User

  Scenario: Successful creation
    Given a valid UserCreateRequest with alias, password, email, applicationId, and roleId
    When POST /api/v1/users is called
    Then the response status is 201 Created
    And the response body contains id, alias, email, personId, applicationId, roleId

  Scenario: Missing alias
    Given a UserCreateRequest without alias
    When POST /api/v1/users is called
    Then the response status is 400 Bad Request
    And the Warning header contains "username is required"

  Scenario: Alias already exists
    Given alias "jdoe" already exists for the given applicationId
    When POST /api/v1/users is called
    Then the response status is 400 Bad Request
    And the Warning header contains "User previously exist."
```

### HTTP Status Code Reference

| Status | Scenario |
|--------|---------|
| 200 | Successful read |
| 201 | Resource created |
| 202 | Accepted (partial update) |
| 204 | Delete success |
| 400 | Validation failure / null input |
| 401 | No or invalid token |
| 403 | Valid token, missing role |
| 404 | Resource not found |
| 406 | Not acceptable |
| 409 | Conflict (duplicate) |
| 422 | Unprocessable entity |
| 500 | Unexpected error |

---

## 3. Backlog Management

### Story Format

```markdown
## US-NNN: <Story Title>

**As a** <actor>
**I want** <capability>
**So that** <business value>

### Acceptance Criteria
[Given/When/Then scenarios]

### Priority: P0 / P1 / P2 / P3
### Estimate: S (< 1 day) / M (1-3 days) / L (> 3 days)
### Dependencies: [list]
```

### Priority Scale

| Priority | Criteria |
|----------|---------|
| P0 | Blocking production, security vulnerability |
| P1 | Core business value, customer-facing |
| P2 | Quality improvement, tech debt with ROI |
| P3 | Nice-to-have, cosmetic |

---

## 4. API Contract Reference

- **OpenAPI spec**: `src/main/resources/META-INF/backbone_rest-openapi.yaml`
- **Base path**: `/api/v1/*`
- **Backward compatibility is mandatory** unless explicitly stated otherwise.
- Session endpoints (`/api/v1/session`) use `session-token` header — not OAuth2 Bearer.

### Deliverables for New Features

1. User story with Given/When/Then acceptance criteria and JSON examples.
2. OpenAPI contract fragment (YAML) for new/modified endpoints.
3. Test data for smoke testing.

---

## 5. Constraints

- Do NOT prescribe internal implementation — focus on WHAT, not HOW.
- All new endpoints require OpenAPI spec updates in `backbone_rest-openapi.yaml`.
- Session endpoints have special auth — clarify security requirements explicitly.
- Consult **API Reviewer** subagent after any contract change.

---

## Acceptance Criteria Checklist

```markdown
- [ ] Happy path with full request/response JSON example
- [ ] Null/empty input validation scenario
- [ ] Resource not found scenario
- [ ] Conflict/duplicate scenario (if applicable)
- [ ] Unauthorized/unauthenticated scenario
- [ ] HTTP status codes explicitly stated
- [ ] Warning header documented where used
- [ ] Session endpoints note session-token header (not OAuth2)
- [ ] Non-functional criteria included (performance, security, compatibility)
```

