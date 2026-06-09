# Supabase Check — backbone-rest

Validate the Supabase Storage integration on the `ds-196-include-supabase-storage` branch:
client wiring, bucket config, auth headers, RLS considerations, and test coverage.

## Usage
```
/supabase-check
```

---

## Steps Claude Will Execute

### 1. Verify Storage Client Exists
```bash
find src/main/java -name "*Supabase*" -o -name "*Storage*" | grep -v test
```
If missing → flag as gap, invoke `supabase-integrator` agent.

### 2. Check Configuration Properties
```bash
grep -rn "supabase\|SUPABASE" src/main/resources/ default.env
```
Required env vars:
- `SUPABASE_URL` or derivable from `AUTH_SERVER_URI`
- `SUPABASE_SERVICE_ROLE_KEY` (server-side ops — must NOT be in default.env with real value)
- `supabase.storage.bucket`

### 3. Verify Profile Image Domain
```bash
cat src/main/java/com/umdc/backoffice/v1/profileimage/service/ProfileImageServiceImpl.java
cat src/main/java/com/umdc/backoffice/v1/profileimage/api/controller/ProfileImageApi.java
```
Check: storage client injected via constructor? Returns `ResponseEntity<?>` with URL?

### 4. Auth Header Validation
Storage API requires `Authorization: Bearer <SERVICE_ROLE_KEY>` + `apikey: <SERVICE_ROLE_KEY>`.
Verify the client sends both headers.

### 5. RLS Safety
- Service role key bypasses RLS — verify it's not sent to client.
- Signed URLs used for client-facing access? Check expiry.
- Public bucket? If yes, flag for review — profile images may need access control.

### 6. Test Coverage
```bash
cat src/test/java/com/umdc/backoffice/v1/profileimage/service/ProfileImageServiceImplTest.java
```
Check: storage client is mocked? Upload + retrieve + error paths tested?

### 7. OpenAPI Sync
```bash
grep -A20 "profileimage\|profile-image\|upload" \
  src/main/resources/META-INF/api.yaml | head -40
```

### 8. Compile + PMD
```bash
mvn -DskipTests compile && mvn pmd:check
```

---

## Output
```markdown
## Supabase Storage Integration Check

### Client Wiring
| Component | Found | Status |
|-----------|-------|--------|
| SupabaseStorageClient | Yes/No | PASS/GAP |
| Config properties | Yes/No | PASS/GAP |
| Constructor injection | Yes/No | PASS/FAIL |

### Security
| Check | Status | Notes |
|-------|--------|-------|
| Service role key env-only | PASS/FAIL | |
| Auth headers set | PASS/FAIL | |
| RLS considered | PASS/GAP | |

### Test Coverage
| Class | Upload tested | Retrieve tested | Error path |

### Build
- Compile: PASS/FAIL
- PMD: PASS/FAIL

### Verdict: INTEGRATION COMPLETE / GAPS FOUND / BLOCKED
### Gaps: <list action items>
```
