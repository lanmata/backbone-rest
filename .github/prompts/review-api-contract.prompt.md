---
name: Review API Contract
description: Validate *Api.java annotations against backbone_rest-openapi.yaml and HTTP conventions
mode: agent
agent: api-reviewer
tools: [read_file, grep_search, file_search, get_errors]
---

Review the API contract for the **backbone-rest** project.

## Scope

- Domain: ${domain}
  _(e.g., `users` — leave blank to review all domains)_

## Checklist

Execute each check and report PASS / FAIL / WARN:

```
[ ] Every endpoint in *Api.java has a matching path + method in backbone_rest-openapi.yaml
[ ] @Tag present on the *Api interface (name + description)
[ ] @Operation(description) present on every method
[ ] @ApiResponses covers all expected status codes (200, 400, 404 minimum)
[ ] @Parameter(description, required) on every @PathVariable and @RequestParam
[ ] HTTP status codes match backbone-rest conventions (see rest-api-design.skill.md)
[ ] Request/response DTO types match schema definitions in the YAML
[ ] @CrossOrigin(origins = "*") on the controller class
[ ] @RequestMapping("/api/v1/${domain}") on the controller class
[ ] operationId values are unique across the entire spec
[ ] No breaking changes — existing paths/methods/params unchanged
[ ] Content-Type: application/json on all endpoints
```

## Key Files

```bash
grep -rn '@\(Get\|Post\|Put\|Patch\|Delete\)Mapping' \
  src/main/java/com/prx/backoffice/v1/${domain}/api/controller/*Api.java

grep -E '^  /api/' src/main/resources/META-INF/backbone_rest-openapi.yaml
```

## Output Format

```markdown
## API Contract Review — ${domain}

| Check | Status | Detail |
|-------|--------|--------|
| @Tag | PASS/FAIL | ... |
| @Operation | PASS/FAIL | ... |
...

### Failed Checks (must fix before merge)
### Warnings (should fix)
### Passed Checks
```

