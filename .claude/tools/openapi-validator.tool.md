---
name: OpenAPI Validator
description: Tool for validating the backbone-rest OpenAPI 3.1 spec and checking *Api.java sync
type: terminal
command-prefix: mvn
used-by:
  - api-designer
  - java-developer
---

# OpenAPI Validator Tool

## Purpose

Validate that `src/main/resources/META-INF/api.yaml` is consistent with `*Api.java` annotations and that the spec is well-formed OpenAPI 3.1.

## Available Commands

### Generate spec from code (springdoc plugin)
```bash
# Generate OpenAPI JSON/YAML from running app annotations
mvn springdoc-openapi:generate

# Generated spec location
cat target/openapi.json
```

### Manual validation approach
```bash
# Check api.yaml is valid YAML
python3 -c "import yaml, sys; yaml.safe_load(sys.stdin)" < src/main/resources/META-INF/api.yaml && echo "YAML valid"

# List all paths in spec
grep "^  /" src/main/resources/META-INF/api.yaml

# Check for specific endpoint
grep -n "/api/v1/users" src/main/resources/META-INF/api.yaml
```

### Cross-check with *Api.java annotations
```bash
# List all @RequestMapping paths in Api interfaces
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping" \
  src/main/java --include="*Api.java"

# Compare with paths in api.yaml
grep "^  /" src/main/resources/META-INF/api.yaml | sort
```

## Output Locations

| File | Purpose |
|------|---------|
| `src/main/resources/META-INF/api.yaml` | OpenAPI 3.1 spec (canonical) |
| `target/openapi.json` | Generated from annotations (springdoc plugin) |

## Notes

- `api.yaml` is the **source of truth** — always update it when `*Api.java` changes.
- The `openapitools.json` at repo root configures the OpenAPI Generator CLI (for client generation).
- PMD does not cover OpenAPI sync — manual or springdoc plugin check required.
