# 📄 Report / Document Generation

> **Base path:** `/api/v1/report`
> **Auth:** OAuth2 Bearer JWT required on all endpoints

| | |
|---|---|
| **Auth type** | OAuth2 Bearer JWT |
| **Auth header** | `Authorization: Bearer <jwt>` |
| **Request content type** | `multipart/form-data` |
| **Response content type** | `application/octet-stream` (template fill) · `application/json` (placeholders) |

[← Back to API Index](./README.md)

---

## 🟢 GET /api/v1/report/template

> Fill a Word `.docx` template with provided values and return the resulting document as a binary download.

> [!NOTE]
> Templates are **not stored server-side**. The caller uploads the template on every request. Placeholder tokens inside the `.docx` (e.g. `{{firstName}}`) are replaced with values from the `values` map before the document is returned.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |
| `Content-Type` | Yes | `multipart/form-data` |

### 📦 Request Parts & Parameters

| Part / Param | Kind | Required | Type | Description |
|---|---|---|---|---|
| `documentTemplate` | Multipart file | Yes | `.docx` binary | The Word template containing placeholder tokens |
| `values.<key>` | Query params | No | `string` | Map of placeholder name → replacement value (repeat per placeholder) |

> [!TIP]
> Pass multiple values as repeated query params: `?values.firstName=John&values.lastName=Doe`. Key names must match the placeholder tokens inside the `.docx` exactly (case-sensitive).

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Document generated | Template processed and filled successfully |
| `400 Bad Request` | Missing or unreadable template | `documentTemplate` is absent or corrupt |
| `401 Unauthorized` | Auth failure | Missing or invalid Bearer token |
| `403 Forbidden` | Insufficient role | Token valid but caller lacks required role |

**200 OK — Response:** `Content-Type: application/octet-stream` — raw filled `.docx` binary.

> [!CAUTION]
> The response body is raw binary. Clients must save it to disk or stream it correctly — do not attempt to parse it as JSON.

### 💡 Example

```bash
curl -k -s -X GET "https://<host>:8084/api/v1/report/template" \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -F "documentTemplate=@./my-template.docx" \
  -G \
  --data-urlencode "values.firstName=John" \
  --data-urlencode "values.lastName=Doe" \
  --data-urlencode "values.reportDate=2026-07-12" \
  -o filled-document.docx
```

---

## 🟢 GET /api/v1/report/placeholdervalues

> Inspect a `.docx` template and return the list of placeholder token names it contains.

> [!TIP]
> Use this endpoint to discover what keys to supply when calling `/template`. Useful for building dynamic form UIs that auto-adapt to the uploaded template.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | Yes | `Bearer <oauth2-jwt>` |
| `Content-Type` | Yes | `multipart/form-data` |

### 📦 Request Parts & Parameters

| Part / Param | Kind | Required | Type | Description |
|---|---|---|---|---|
| `documentTemplate` | Multipart file | Yes | `.docx` binary | The Word template to inspect |
| `templateDocumentModel` | Query param | Yes | `string` | Model class name hint used by the extractor |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Placeholders extracted | Template parsed successfully |
| `400 Bad Request` | Missing model param | `templateDocumentModel` is null or blank |
| `401 Unauthorized` | Auth failure | Missing or invalid Bearer token |
| `403 Forbidden` | Insufficient role | Token valid but caller lacks required role |

**200 OK — Response body:**

```json
["{{firstName}}", "{{lastName}}", "{{date}}", "{{reportTitle}}", "{{companyName}}"]
```

### 💡 Example

```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/report/placeholdervalues?templateDocumentModel=UserReportModel" \
  -H "Authorization: Bearer <oauth2-jwt>" \
  -F "documentTemplate=@./my-template.docx"
```

---

## 🔄 Typical Workflow

```
1. Upload template to /placeholdervalues
   → receive ["{{firstName}}", "{{lastName}}", "{{date}}"]

2. Collect values for each placeholder from your data source

3. Upload template + values to /template
   → receive filled .docx binary
   → save as report-2026-07-12.docx
```

---

## 🚨 Error Reference

| Status | Applies to | Cause |
|--------|-----------|-------|
| `400 Bad Request` | Both | Missing `documentTemplate`, null `templateDocumentModel` |
| `401 Unauthorized` | Both | Missing or invalid Bearer token |
| `403 Forbidden` | Both | Valid token but insufficient role |

---

[← Back to API Index](./README.md)
