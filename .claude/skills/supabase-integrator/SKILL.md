---
name: Supabase Integrator Skills
description: Consolidated skill set for the Supabase Integrator agent — Supabase Storage bucket ops, signed URLs, profile image upload/retrieve, RLS-safe patterns for backbone-rest
applies-to:
  - supabase-integrator
---

# Supabase Integrator — Skill Definition

## 1. Supabase Configuration

| Item | Value |
|------|-------|
| Auth issuer | `https://jygwixrpoxcrltmeshyl.supabase.co/auth/v1` |
| DB pooler | `aws-1-us-east-2.pooler.supabase.com:6543` (transaction mode) |
| Storage | Supabase Storage — `profileimage` domain |
| Spring profile | `remote-supabase` |
| Config source | `AUTH_SERVER_URI` env var (no hardcoding) |

---

## 2. Profile Image Domain

Domain package: `com.umdc.backoffice.v1.profileimage`

```
profileimage/
  api/controller/   → ProfileImageApi.java + ProfileImageController.java
  service/          → ProfileImageService.java + ProfileImageServiceImpl.java
  to/               → DTOs for upload request + image URL response
```

---

## 3. Storage Patterns

### Upload to Supabase Storage
```java
// Via AWS S3-compatible SDK (software.amazon.awssdk:s3)
PutObjectRequest request = PutObjectRequest.builder()
    .bucket(bucketName)
    .key(objectKey)
    .contentType(contentType)
    .build();
s3Client.putObject(request, RequestBody.fromBytes(fileBytes));
```

### Generate Signed URL (short TTL)
```java
GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
    .signatureDuration(Duration.ofMinutes(15))
    .getObjectRequest(r -> r.bucket(bucketName).key(objectKey))
    .build();
PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
return presigned.url().toString();
```

### RLS-Safe Pattern
- Always use the Supabase service role key for server-side uploads.
- Never expose the service role key to clients — use signed URLs for client access.
- Bucket RLS policies must be active; verify via Supabase MCP or dashboard.

---

## 4. AWS SDK Configuration

Dependency: `software.amazon.awssdk:s3` (version in `pom.xml` `${aws.sdk.version}`).

Configuration beans should use `@ConfigurationProperties` — never hardcode endpoint/region.

---

## 5. Key Files

| File | Purpose |
|------|---------|
| `src/main/java/com/umdc/backoffice/v1/profileimage/` | Profile image domain |
| `src/main/resources/bootstrap.yml` | Supabase endpoint config |
| `default.env` | Supabase env var stubs |
| `src/main/resources/prod-ca-2021.crt` | SSL cert for Supabase DB TLS |

---

## 6. Constraints

- Do NOT hardcode Supabase project URL or service role key.
- Always use signed URLs for client-side image retrieval (TTL ≤ 60 min).
- Do NOT use Supabase pooler session-mode features (stick to transaction mode).
- File size validation must happen before upload (use `commons-fileupload`).
- Always validate content type — only `image/*` MIME types allowed for profile images.

---

## 7. Checklist

- [ ] Supabase endpoint sourced from env var, not hardcoded
- [ ] Signed URL TTL ≤ 60 minutes
- [ ] Content type validated before upload
- [ ] File size limit enforced (use `commons-fileupload` `FileSizeException`)
- [ ] RLS policies verified on bucket
- [ ] Service role key never exposed in API responses or logs
- [ ] `remote-supabase` Spring profile active in Supabase environment
