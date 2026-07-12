# 📊 IAM — Security Audit Events

> **Base path:** `/api/v1/iam/audit`  
> **Auth:** OAuth2 Bearer JWT — `Authorization: Bearer <jwt>`  
> [← Back to API Index](./README.md)

| Property | Value |
|----------|-------|
| Base path | `/api/v1/iam/audit` |
| Auth type | OAuth2 Bearer JWT |
| Content-Type | `application/json` |
| Response header | `X-Message` — human-readable status |

Provides a **paginated, filterable** interface for querying security audit events. Events are recorded automatically by the system for significant security actions: logins, logouts, token operations, and permission checks.

---

## 🟢 GET /api/v1/iam/audit/events

> Query the security audit log with optional filters and pagination.

### 📋 Request Headers

| Header | Required | Value |
|--------|----------|-------|
| `Authorization` | ✅ | `Bearer <oauth2-jwt>` |

### 🔍 Query Parameters

All parameters are optional. Combine them to narrow results.

| Parameter | Type | Default | Constraints | Description |
|-----------|------|---------|-------------|-------------|
| `userId` | `UUID` | — | Valid UUID | Filter events triggered by this user |
| `applicationId` | `UUID` | — | Valid UUID | Filter events in this application context |
| `eventType` | `string` | — | See table below | Filter by event type (case-insensitive) |
| `from` | `ISO-8601 datetime` | — | e.g. `2026-07-01T00:00:00` | Earliest event timestamp (inclusive) |
| `to` | `ISO-8601 datetime` | — | e.g. `2026-07-12T23:59:59` | Latest event timestamp (inclusive) |
| `page` | `integer` | `0` | ≥ 0 | Zero-based page index |
| `size` | `integer` | `20` | 1–100 | Number of results per page |

### Event Types

| `eventType` value | Description |
|-------------------|-------------|
| `LOGIN` | Successful user login |
| `LOGOUT` | User session terminated |
| `TOKEN_ISSUED` | Session or M2M token issued |
| `TOKEN_REVOKED` | Token explicitly revoked |
| `PERMISSION_DENIED` | Permission check returned `granted: false` |

### 📡 Responses

| Status | Meaning | When |
|--------|---------|------|
| `200 OK` | Results found | Paginated event list returned |
| `204 No Content` | No results | Query matched zero events |
| `400 Bad Request` | Invalid param | Malformed UUID, datetime, or page/size value |
| `401 Unauthorized` | Invalid token | Missing or expired Bearer JWT |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "eventType": "LOGIN",
      "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "applicationId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "timestamp": "2026-07-12T10:00:00Z",
      "ipAddress": "203.0.113.42",
      "details": "Login successful via alias"
    },
    {
      "id": "d4e5f6a7-b8c9-0123-defa-234567890123",
      "eventType": "TOKEN_ISSUED",
      "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "applicationId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "timestamp": "2026-07-12T10:00:01Z",
      "ipAddress": "203.0.113.42",
      "details": "Session token issued"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "page": 0,
  "size": 20
}
```

### 💡 Examples

**Filter by event type with date range:**
```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/iam/audit/events?eventType=LOGIN&from=2026-07-01T00:00:00&to=2026-07-12T23:59:59&page=0&size=50" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

**Filter by user:**
```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/iam/audit/events?userId=b2c3d4e5-f6a7-8901-bcde-f12345678901&page=0&size=20" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

**Filter by application + event type:**
```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/iam/audit/events?applicationId=c3d4e5f6-a7b8-9012-cdef-123456789012&eventType=PERMISSION_DENIED" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

**Get all events, paginated (default page size = 20):**
```bash
curl -k -s -X GET \
  "https://<host>:8084/api/v1/iam/audit/events?page=2&size=20" \
  -H "Authorization: Bearer <oauth2-jwt>"
```

### Pagination Notes

> [!TIP]
> Pagination is zero-based. Page `0` is the first page. Use `totalPages` from the response to iterate all pages.

```bash
# Iterate all pages
PAGE=0
TOTAL_PAGES=1
while [ $PAGE -lt $TOTAL_PAGES ]; do
  RESPONSE=$(curl -k -s -X GET \
    "https://<host>:8084/api/v1/iam/audit/events?page=$PAGE&size=100" \
    -H "Authorization: Bearer $OAUTH2_JWT")
  TOTAL_PAGES=$(echo "$RESPONSE" | jq '.totalPages')
  echo "$RESPONSE" | jq '.content[]'
  PAGE=$((PAGE + 1))
done
```

---

## 🚨 Error Reference

| Status | Cause |
|--------|-------|
| `204 No Content` | Query returned zero results (not an error) |
| `400 Bad Request` | Malformed UUID, invalid datetime format, or page/size out of range |
| `401 Unauthorized` | Missing or expired OAuth2 Bearer JWT |

---

[← Back to API Index](./README.md)
