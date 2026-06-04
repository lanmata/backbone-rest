---
name: Define User Story
description: Define a well-structured user story with acceptance criteria and API contract for backbone-rest
mode: ask
agent: product-owner
tools: [read_file, grep_search, file_search, create_file]
---

Define a user story for **backbone-rest** with full acceptance criteria.

## Input

- Feature request: ${featureRequest}
- Actor: ${actor}
  _(e.g., `backoffice admin`, `authenticated user`, `system`)_
- Domain: ${domain}
  _(e.g., `users`, `roles`, `contacts`, `features`)_

## Story Format

```markdown
## US-${storyId}: ${title}

**As a** ${actor}
**I want** <capability>
**So that** <business value>

### Priority: P0 / P1 / P2 / P3
### Estimate: S (<1 day) / M (1-3 days) / L (>3 days)
### Dependencies: [list or "none"]
```

## Acceptance Criteria (Gherkin)

Write Given/When/Then scenarios covering:

1. **Happy path** — with full JSON request/response example
2. **Null/blank input** — 400 with Warning header
3. **Not found** — 404 (if applicable)
4. **Conflict/duplicate** — 409 or 400 (if applicable)
5. **Unauthorized** — 401 (no token)
6. **Forbidden** — 403 (valid token, wrong role)

## API Contract Fragment

```yaml
# New/modified endpoint
/api/v1/${domain}/${path}:
  ${method}:
    tags: [${domain}]
    operationId: ${operationId}
    summary: ${summary}
    requestBody: ...
    responses:
      '200': ...
      '400': ...
      '404': ...
```

## Constraints

- No breaking changes to existing `/api/v1/*` endpoints.
- Session endpoints (`/api/v1/session/**`) use `session-token` header — not OAuth2 Bearer.
- Document required role in the story (e.g., `ROLE_ADMIN`).

