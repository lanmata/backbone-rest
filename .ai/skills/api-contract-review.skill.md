---
name: API Contract Review
description: Shared - API contract validation checklist (API Reviewer, Product Owner, Developer)
applies-to: [API Reviewer, Product Owner, Developer]
---
# API Contract Review
## Checklist
- [ ] All *Api.java endpoints have matching paths in backbone_rest-openapi.yaml
- [ ] HTTP methods match between code and YAML spec
- [ ] Request/response schemas match DTO records/classes
- [ ] All status codes covered in @ApiResponses
- [ ] No breaking changes to /api/v1/* endpoints
- [ ] Path parameter types consistent (UUID vs String)
- [ ] Content-Type: application/json on all endpoints
- [ ] @Tag on each *Api interface; @CrossOrigin(origins="*") on controller
- [ ] operationId values unique across entire spec
## Backward Compatibility Rules
- Never remove existing response fields or rename path parameters
- Never change HTTP method or path of existing endpoints
- Additive changes only (new optional fields, new endpoints)
## Output Format
```
### Passed Checks | Warnings | Failed Checks | Recommendations
```
