---
name: REST API Design
description: Shared - REST API design patterns (Developer, API Reviewer, Product Owner)
applies-to: [Developer, API Reviewer, Product Owner]
---
# REST API Design
## HTTP Status Codes
| Status | Usage |
|--------|-------|
| 200 | Read success | 201 | Created | 202 | Accepted | 204 | Delete |
| 400 | Validation + Warning header | 401 | Unauthorized | 404 | Not found |
| 406 | Not acceptable | 409 | Conflict | 422 | Unprocessable | 500 | Error |
## Rules
- API contracts defined in *Api.java (annotations + default delegation), controllers are thin.
- All controllers: @RestController @RequestMapping("/api/v1/<domain>") @CrossOrigin(origins="*")
- Warning header: ResponseEntity.badRequest().header(HttpHeaders.WARNING, "msg").build()
- On contract change: update BOTH *Api.java AND backbone_rest-openapi.yaml
