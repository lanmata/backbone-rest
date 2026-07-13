---
name: docs-conventions
description: Documentation conventions for backbone-rest — file locations, diagram format, style rules, and which files to update when adding or changing endpoints
metadata:
  type: feedback
---

All diagrams must use **Mermaid** format (flowchart, sequenceDiagram, graph, classDiagram). Never use ASCII art tables for diagrams.

**Why:** User explicitly required Mermaid for all diagrams when updating application docs.

**How to apply:** When adding any new endpoint or domain, produce Mermaid sequence diagrams for each operation's happy path and error path.

## Files to update when adding/changing endpoints

For any domain endpoint change, update ALL of the following:

| File | What to change |
|------|---------------|
| `docs/api/<domain>.md` | Full endpoint reference (request/response/examples/error table) |
| `docs/api/README.md` | Endpoint count in domain index table + total count |
| `docs/v1/01-getting-started.md` | Quick Reference — All Endpoints table |
| `docs/v1/README.md` | Quick Reference — All Endpoints table (duplicate of above) |
| `docs/v1/04-application-client.md` | (application domain only) endpoint sections + mermaid flows |
| `docs/v1/10-api-reference.md` | Domain section endpoint table + request/response bodies |

## Style rules

- No emoji in headings (the existing files already had them before the convention was set; don't add new ones).
- Every endpoint section in `docs/api/<domain>.md` includes: request headers table, request body table, responses table, response headers table (`Message-header`), and a `curl` example.
- `Message-header` response header must be documented on every endpoint — it carries the human-readable status (e.g. `Application created successfully.`, `Application not found.`).
- POST create returns `201 Created`, not `200 OK`.
- Error responses table in `docs/api/<domain>.md` must list the `Message-header` value alongside the HTTP status.
- Mermaid sequence diagrams must cover at least: happy path + validation failure (400) + not-found (404).

## codeName rule (application domain)

`codeName` is auto-derived from `name`: lowercase, non-alphanumeric → `_`, truncated to **8 characters**. Callers cannot set it directly. Document this on every create/update endpoint in the application domain.

## Mermaid color scheme

All diagrams must use this palette consistently:

**Component colors (graph / flowchart `classDef`):**

| Component | fill | stroke | text |
|-----------|------|--------|------|
| Application node | `#10B981` | `#059669` | `#fff` |
| User node | `#3B82F6` | `#1D4ED8` | `#fff` |
| Role / amber accent | `#F59E0B` | `#D97706` | `#fff` |
| Shared / neutral | `#6B7280` | `#4B5563` | `#fff` |
| Isolated / warning | `#F59E0B` | `#D97706` | `#fff` |

**classDiagram single-class style:**
`style ClassName fill:#10B981,stroke:#059669,color:#fff`

**Sequence diagram `rect` backgrounds:**

| Path | `rect` color |
|------|-------------|
| Request (neutral) | `rgb(219,234,254)` — light blue |
| Success (2xx) | `rgb(220,252,231)` — light green |
| Not found (404) | `rgb(254,243,199)` — light yellow |
| Validation error (400/401) | `rgb(254,226,226)` — light red |

Use `rect` blocks with `note over` labels to distinguish paths — do NOT nest `rect` inside `alt/else/end`.

**Why:** User explicitly required colored Mermaid diagrams to identify components visually.

## Related memories

[[project-crud-application]]
