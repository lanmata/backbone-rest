---
name: OpenAPI Validator
description: Tool for validating OpenAPI specifications
type: terminal
---

# OpenAPI Validator Tool

## Purpose

Validate the OpenAPI specification and ensure consistency between Java API
interfaces and the YAML spec in the **backbone-rest** project.

## Spec Location

- `src/main/resources/META-INF/backbone_rest-openapi.yaml`

## Validation Commands

### Using springdoc-openapi Maven Plugin

```bash
# Generate OpenAPI spec from running application (integration-test phase)
mvn springdoc-openapi:generate

# Output: target/openapi.json
```

### Manual Validation

```bash
# Install spectral (OpenAPI linter)
npm install -g @stoplight/spectral-cli

# Lint the spec
spectral lint src/main/resources/META-INF/backbone_rest-openapi.yaml

# Validate with swagger-cli
npm install -g @apidevtools/swagger-cli
swagger-cli validate src/main/resources/META-INF/backbone_rest-openapi.yaml
```

### Consistency Check

To verify Java annotations match the YAML spec:

```bash
# Find all API endpoint mappings in Java interfaces
grep -rn '@\(Get\|Post\|Put\|Patch\|Delete\)Mapping' \
  src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java

# Find all paths in OpenAPI spec
grep -E '^  /api/' src/main/resources/META-INF/backbone_rest-openapi.yaml
```

## Key Files

- API interfaces: `src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java`
- OpenAPI spec: `src/main/resources/META-INF/backbone_rest-openapi.yaml`
- DTO classes: `src/main/java/com/prx/backoffice/v1/*/api/to/*.java`
