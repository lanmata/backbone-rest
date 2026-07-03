---
name: Review API Contract
description: Review *Api.java annotations and api.yaml for backbone-rest — check sync, completeness, backward compatibility, and REST design
mode: agent
agent: api-designer
tools: [Bash, Read]
---

Review the API contract for **backbone-rest**.

## Scope

${scope}
_(If blank, review all `*Api.java` files and `api.yaml`)_

## Step 1 — Identify changed API files

```bash
git diff develop...HEAD --name-only | grep -E "*Api\.java|api\.yaml"
```

## Step 2 — Read the spec

```bash
cat src/main/resources/META-INF/api.yaml
```

## Step 3 — Read changed `*Api.java` files

For each changed `*Api.java`:
- Read the full interface.
- Extract all HTTP methods, paths, and response codes.

## Step 4 — Cross-check spec vs. annotations

```bash
# Extract paths from *Api.java
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping" \
  src/main/java --include="*Api.java" -n

# Extract paths from api.yaml
grep -E "^  /" src/main/resources/META-INF/api.yaml
```

## Review Checklist

### Per endpoint in changed files
- [ ] `@Operation(summary, description)` present
- [ ] `@ApiResponses` covers: success, 400, 404 (minimum)
- [ ] `@Parameter(description, required)` on path/query vars
- [ ] `@Content(schema = @Schema(implementation = ...))` on success response
- [ ] Path in `api.yaml` matches `*Api.java` mapping

### Backward Compatibility
- [ ] No existing endpoints removed
- [ ] No existing required request fields removed
- [ ] No existing response field types changed
- [ ] No URL path segments renamed

### REST Design
- [ ] Resource paths use plural nouns (`/users`, `/roles`)
- [ ] HTTP methods match semantics (GET=read, POST=create, PUT=update, DELETE=delete)
- [ ] Status codes follow conventions (201 for create, 204 for delete, 409 for conflict)

## Output Format

```markdown
## API Contract Review

### Sync Status
| Endpoint | In *Api.java | In api.yaml | Status |
|----------|-------------|-------------|--------|

### Annotation Completeness
| File | Method | Missing Annotations |
|------|--------|-------------------|

### Breaking Changes
| Change | File | Severity |
|--------|------|---------|

### Overall: PASS / CHANGES_REQUIRED
```
