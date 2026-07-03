---
name: Define Story
description: Define a user story with acceptance criteria, API contract, and out-of-scope boundaries for backbone-rest
mode: agent
agent: product-owner
tools: [Read, Bash]
---

Define a user story for **backbone-rest**.

## Feature Request

- Title: ${title}
- Domain: ${domain}
- Description: ${description}

## Step 1 — Understand existing domain

Read the existing domain package to understand current state:
```bash
find src/main/java -path "*/${domain}*" -name "*Api.java" | sort
find src/main/java -path "*/${domain}*" -name "*.java" | sort
```

Read `src/main/resources/META-INF/api.yaml` to understand existing API contracts.

## Step 2 — Produce the story

```markdown
### Story: <JIRA-ID> — ${title}
**As a** <actor: admin / end-user / managed-client>
**I want** <action>
**So that** <business value>

#### Acceptance Criteria
- [ ] AC-1: `<HTTP method> /api/v1/${domain}/...` returns <status> with <body> when <condition>
- [ ] AC-2: ...
- [ ] AC-3: ...

#### API Contract (if new or changed endpoint)
- Method + Path: `POST /api/v1/${domain}/...`
- Auth: Required (Bearer JWT)
- Request body: `{ ... }`
- Response <status>: `{ ... }`
- Error cases: 400 (null input), 404 (not found), 409 (conflict — if applicable)

#### Out of Scope
- ...

#### Dependencies
- [ ] JPA entity must exist in `com.umdc.persistence` (confirm with database-architect)
- [ ] External integration: <none / Supabase Storage / etc.>
```

## Constraints

- Each AC must be verifiable by a JUnit test or `curl` command without additional clarification.
- Do not define implementation details (class names, method signatures).
- Auth model is fixed: Bearer JWT for all `/api/v1/**` except session endpoints.
- If Supabase Storage is involved, flag the `supabase-integrator` agent.
