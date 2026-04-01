---
name: Backlog Management
description: Skill for managing product backlog and user stories
applies-to:
  - Product Owner
  - Project Manager
---

# Backlog Management Skill

## Scope

This skill covers product backlog management, user story creation, and prioritization
for the **backbone-rest** project.

## User Story Template

```markdown
### [STORY-XXX] Title

**As a** [role],
**I want to** [capability],
**So that** [business value].

#### Acceptance Criteria

```gherkin
Given [precondition]
When [action]
Then [expected result]
```

#### Technical Notes
- Affected endpoints: [list `/api/v1/*` paths]
- DTOs affected: [list TO classes under `v1/<domain>/api/to`]
- Repositories used: [list from `com.prx.persistence.general.repositories`]

#### Priority: P0/P1/P2
#### Effort: S/M/L
#### Dependencies: [list]
```

## Priority Framework

- **P0 (Critical)**: Security vulnerabilities, auth/session failures, data corruption, production outages
- **P1 (High)**: Core entity CRUD delivery, backward API compatibility, PMD violations
- **P2 (Medium)**: New domain features, improved test coverage, performance improvements
- **P3 (Low)**: Nice-to-have, cosmetic changes, documentation improvements

## Domain Areas

- **User Management**: CRUD, alias/email check, role link/unlink (`/api/v1/users`)
- **Session / Auth**: Token generation, validation, renewal (`/api/v1/session`)
- **Roles**: CRUD (`/api/v1/roles`)
- **Contacts**: CRUD tied to people (`/api/v1/contacts`)
- **Contact Types**: Enumeration CRUD (`/api/v1/contacttypes`)
- **Features**: CRUD (`/api/v1/features`)
- **People**: CRUD with contacts (`/api/v1/people`)
- **Application**: Registration management (`/api/v1/application`)
- **Profile Image**: Upload/retrieval
- **Report**: Report generation
