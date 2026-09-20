# 📄 MCAM Phase 6 — OpenAPI Spec + Documentation

> **Prompt for:** `api-reviewer` agent + `developer` agent  
> **Depends on:** Phase 5 complete (all tests green, PMD clean, JaCoCo ≥ 80%)  
> **Estimated duration:** 1–2 days  
> **Complexity:** Low  
> **Reference:** `docs/plans/mcam-implementation-plan.md` §7

---

## 🎯 Goal

Publish the MCAM API contract, update supporting documentation, and confirm the project is release-ready.

Tasks:
1. Append 9 new paths + 8 new schema components to `src/main/resources/api.yaml`
2. Extend `AuditEventType` enum in `api.yaml` with 9 M2M values
3. Create `docs/env/mcam-env-vars.md`
4. Update `CHANGELOG` at project root
5. Validate `api.yaml` is valid OpenAPI 3.0 (no broken `$ref`)

---

## 📐 Constraints (Non-Negotiable)

| # | Rule |
|---|---|
| OA-01 | **Append only** — do not remove or modify any existing paths, schemas, or tags in `api.yaml` |
| OA-02 | All `$ref` values must resolve to existing or newly added components |
| OA-03 | New tag name: `"managed-clients"` |
| OA-04 | Security scheme on protected endpoints: reference the existing `bearerAuth` scheme in `api.yaml` |
| OA-05 | `/token` and `/introspect` paths must have **no security requirement** (public endpoints) |
| OA-06 | `AuditEventType` enum extension is additive — existing values preserved, 9 new values appended |
| OA-07 | `CHANGELOG` update goes under `[Unreleased]` — do not create a version tag |
| OA-08 | `docs/env/mcam-env-vars.md` documents only the 6 MCAM env vars — no existing vars duplicated |

---

## 📋 Tasks

### TASK 6.1 — New Tag in api.yaml

**Read `src/main/resources/api.yaml` first** to find the `tags:` array. Append:

```yaml
  - name: "managed-clients"
    description: "Management Client Authentication Manager — M2M OAuth2 client credential lifecycle"
```

---

### TASK 6.2 — New Paths in api.yaml

Append all 9 paths under the `paths:` key. Do **not** replace any existing path.

#### `POST /api/v1/managed-clients`

```yaml
  /api/v1/managed-clients:
    post:
      tags: [managed-clients]
      summary: Register a new managed client
      description: Returns the clientSecret exactly once in the response body.
      operationId: registerManagedClient
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ManagedClientCreateRequest'
      responses:
        '201':
          description: Client registered successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientCreateResponse'
        '400':
          description: Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
        '409':
          description: Duplicate client name for application
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `GET /api/v1/managed-clients`

```yaml
    get:
      tags: [managed-clients]
      summary: List managed clients
      operationId: listManagedClients
      security:
        - bearerAuth: []
      parameters:
        - name: applicationId
          in: query
          required: false
          schema:
            type: string
            format: uuid
        - name: active
          in: query
          required: false
          schema:
            type: boolean
        - name: page
          in: query
          required: false
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          required: false
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Client list
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/ManagedClientTO'
        '204':
          description: No clients found
```

#### `GET /api/v1/managed-clients/{clientId}`

```yaml
  /api/v1/managed-clients/{clientId}:
    get:
      tags: [managed-clients]
      summary: Get a managed client by ID
      operationId: getManagedClient
      security:
        - bearerAuth: []
      parameters:
        - name: clientId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Client found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientTO'
        '404':
          description: Client not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `PUT /api/v1/managed-clients/{clientId}`

```yaml
    put:
      tags: [managed-clients]
      summary: Update a managed client
      operationId: updateManagedClient
      security:
        - bearerAuth: []
      parameters:
        - name: clientId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ManagedClientUpdateRequest'
      responses:
        '200':
          description: Client updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientTO'
        '404':
          description: Client not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `DELETE /api/v1/managed-clients/{clientId}`

```yaml
    delete:
      tags: [managed-clients]
      summary: Delete a managed client
      description: Revokes all active tokens before deletion.
      operationId: deleteManagedClient
      security:
        - bearerAuth: []
      parameters:
        - name: clientId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '204':
          description: Client deleted
        '404':
          description: Client not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `POST /api/v1/managed-clients/token` (public)

```yaml
  /api/v1/managed-clients/token:
    post:
      tags: [managed-clients]
      summary: Issue an M2M access token
      description: Public endpoint. Validates clientId, clientSecret, and requested scopes.
      operationId: issueManagedClientToken
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ManagedClientTokenRequest'
      responses:
        '200':
          description: Token issued
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientTokenResponse'
        '400':
          description: Invalid scope
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
        '401':
          description: Invalid credentials or inactive client
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
        '429':
          description: Rate limit exceeded
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `POST /api/v1/managed-clients/{clientId}/rotate-secret`

```yaml
  /api/v1/managed-clients/{clientId}/rotate-secret:
    post:
      tags: [managed-clients]
      summary: Rotate the secret for a managed client
      description: Returns the new plaintext clientSecret once. Grace period allows the old secret temporarily.
      operationId: rotateManagedClientSecret
      security:
        - bearerAuth: []
      parameters:
        - name: clientId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Secret rotated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientSecretRotateResponse'
        '404':
          description: Client not found or inactive
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `DELETE /api/v1/managed-clients/{clientId}/tokens`

```yaml
  /api/v1/managed-clients/{clientId}/tokens:
    delete:
      tags: [managed-clients]
      summary: Revoke all active tokens for a client
      operationId: revokeAllClientTokens
      security:
        - bearerAuth: []
      parameters:
        - name: clientId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '204':
          description: All tokens revoked
        '404':
          description: Client not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientErrorResponse'
```

#### `POST /api/v1/managed-clients/introspect` (public)

```yaml
  /api/v1/managed-clients/introspect:
    post:
      tags: [managed-clients]
      summary: Introspect an M2M access token
      description: Public endpoint. Always returns HTTP 200. active=false for invalid, expired, or revoked tokens.
      operationId: introspectManagedClientToken
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [token]
              properties:
                token:
                  type: string
                  description: The raw M2M access token to introspect
      responses:
        '200':
          description: Introspection result
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ManagedClientTokenIntrospectResponse'
```

---

### TASK 6.3 — New Schema Components in api.yaml

Append all 8 schemas under `components.schemas:`. Do **not** remove any existing schemas.

```yaml
    ManagedClientCreateRequest:
      type: object
      required: [name, applicationId, scopes]
      properties:
        name:
          type: string
          maxLength: 128
        description:
          type: string
          maxLength: 512
        applicationId:
          type: string
          format: uuid
        scopes:
          type: array
          items:
            type: string
          minItems: 1
        active:
          type: boolean
          default: true

    ManagedClientCreateResponse:
      type: object
      properties:
        clientId:
          type: string
          format: uuid
        clientSecret:
          type: string
          description: Plaintext secret — returned exactly once. Store immediately and securely.
        name:
          type: string
        applicationId:
          type: string
          format: uuid
        scopes:
          type: array
          items:
            type: string
        active:
          type: boolean
        createdAt:
          type: string
          format: date-time

    ManagedClientTO:
      type: object
      description: Read-only view of a managed client. Never contains secret material.
      properties:
        clientId:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
        applicationId:
          type: string
          format: uuid
        scopes:
          type: array
          items:
            type: string
        active:
          type: boolean
        createdAt:
          type: string
          format: date-time
        lastUpdatedAt:
          type: string
          format: date-time
        secretLastRotatedAt:
          type: string
          format: date-time

    ManagedClientUpdateRequest:
      type: object
      description: All fields optional — only provided fields are updated.
      properties:
        name:
          type: string
          maxLength: 128
        description:
          type: string
          maxLength: 512
        scopes:
          type: array
          items:
            type: string
        active:
          type: boolean

    ManagedClientTokenRequest:
      type: object
      required: [clientId, clientSecret, scopes]
      properties:
        clientId:
          type: string
          format: uuid
        clientSecret:
          type: string
        scopes:
          type: array
          items:
            type: string
          minItems: 1

    ManagedClientTokenResponse:
      type: object
      properties:
        accessToken:
          type: string
          description: Signed RS256 M2M JWT
        tokenType:
          type: string
          example: Bearer
        expiresIn:
          type: integer
          description: Token TTL in seconds
        scopes:
          type: array
          items:
            type: string
        issuedAt:
          type: string
          format: date-time

    ManagedClientSecretRotateResponse:
      type: object
      properties:
        clientId:
          type: string
          format: uuid
        clientSecret:
          type: string
          description: New plaintext secret — returned exactly once. Store immediately and securely.
        gracePeriodSeconds:
          type: integer
          description: Seconds the old secret remains valid
        rotatedAt:
          type: string
          format: date-time

    ManagedClientTokenIntrospectResponse:
      type: object
      required: [active]
      properties:
        active:
          type: boolean
        clientId:
          type: string
        clientName:
          type: string
        scopes:
          type: array
          items:
            type: string
        issuer:
          type: string
        exp:
          type: integer
          format: int64
        iat:
          type: integer
          format: int64
        jti:
          type: string

    ManagedClientErrorResponse:
      type: object
      required: [error]
      properties:
        error:
          type: string
          example: invalid_client
        errorDescription:
          type: string
        clientId:
          type: string
```

---

### TASK 6.4 — AuditEventType Enum Extension in api.yaml

Find the `AuditEventType` enum definition in `components.schemas` and **append** the 9 new values to the `enum` array (do not reorder or remove existing values):

```yaml
          - CLIENT_REGISTERED
          - CLIENT_UPDATED
          - CLIENT_DEACTIVATED
          - CLIENT_DELETED
          - CLIENT_SECRET_ROTATED
          - CLIENT_TOKEN_ISSUED
          - CLIENT_TOKEN_ISSUE_FAILED
          - CLIENT_TOKEN_REVOKED
          - CLIENT_INTROSPECTION_CALLED
```

---

### TASK 6.5 — Validate api.yaml

After all edits, validate the file:

```bash
# Option 1: Swagger Editor (paste file content at https://editor.swagger.io)
# Option 2: openapi-generator CLI
npx @openapitools/openapi-generator-cli validate -i src/main/resources/api.yaml

# Option 3: spectral (if available)
npx @stoplight/spectral-cli lint src/main/resources/api.yaml
```

Ensure:
- [ ] No broken `$ref` references
- [ ] No duplicate `operationId` values
- [ ] All `security: []` overrides are present on `/token` and `/introspect`
- [ ] All path parameters have matching `parameters` entries

---

### TASK 6.6 — Environment Variables Document

#### Create: `docs/env/mcam-env-vars.md`

```markdown
# MCAM Environment Variables

> All MCAM-specific environment variables for the Management Client Authentication Manager.

| Variable | Required | Default | Description |
|---|---|---|---|
| `MCAM_KEY_ALIAS` | **Yes** | — | Key alias in the PKCS12 keystore used to sign and verify M2M JWTs |
| `MCAM_KEYSTORE_LOCATION` | **Yes** | — | Path to PKCS12 keystore (`classpath:` or `file:` prefix) |
| `MCAM_KEYSTORE_PASSWORD` | **Yes** | — | Keystore password — inject from secrets manager, never hard-code |
| `MCAM_TOKEN_TTL_SECONDS` | No | `3600` | M2M access token TTL in seconds (max recommended: 86400) |
| `MCAM_ROTATION_GRACE_SECONDS` | No | `300` | Seconds the old secret remains valid after rotation (grace period) |
| `MCAM_RATE_LIMIT_RPM` | No | `60` | Maximum token issuance requests per minute per `clientId` |
| `MCAM_MAINTENANCE_INTERVAL_MS` | No | `600000` | Interval (ms) for the scheduled `prevSecretHash` cleanup task |

## Notes

- `MCAM_KEYSTORE_PASSWORD` must be injected at runtime (e.g., Vault, Kubernetes Secret, or AWS SSM).  
  **Never commit its value to version control.**
- The keystore referenced by `MCAM_KEYSTORE_LOCATION` must contain an RSA key pair for RS256 signing.
- Reduce `MCAM_TOKEN_TTL_SECONDS` for high-security environments; increase `MCAM_ROTATION_GRACE_SECONDS`  
  if your deployment pipeline requires more than 5 minutes to distribute the new secret.
```

---

### TASK 6.7 — CHANGELOG Update

#### Modify: `CHANGELOG` (project root)

Prepend the following entry at the top of the `[Unreleased]` section (or add a new `[Unreleased]` section if absent):

```
## [Unreleased] — MCAM v1

### Added
- Management Client Authentication Manager (MCAM) — M2M OAuth2 client credential lifecycle
  - POST   /api/v1/managed-clients              — Register managed client (returns secret once)
  - GET    /api/v1/managed-clients              — List clients (paginated, filterable)
  - GET    /api/v1/managed-clients/{clientId}   — Get client detail (no secret fields)
  - PUT    /api/v1/managed-clients/{clientId}   — Update client metadata (partial update)
  - DELETE /api/v1/managed-clients/{clientId}   — Delete client + revoke all tokens
  - POST   /api/v1/managed-clients/token        — Issue M2M access token (public endpoint)
  - POST   /api/v1/managed-clients/{clientId}/rotate-secret — Rotate client secret
  - DELETE /api/v1/managed-clients/{clientId}/tokens        — Revoke all active tokens
  - POST   /api/v1/managed-clients/introspect   — Introspect M2M token (public endpoint)
- Flyway migrations V2 (managed_client), V3 (managed_client_audit_event), V4 (audit_event CHECK extension)
- ManagedClientTokenFilter — RS256 M2M JWT validation registered before SessionJwtAuthenticationFilter
- AuditEventType extended with 9 additive M2M event type constants
- ManagementAuthenticatorProperties extended: tokenTtlSeconds, rotationGracePeriodSeconds, rateLimitRpm
- ManagedClientMaintenanceService — scheduled cleanup of stale prevSecretHash values

### Security
- client_secret BCrypt-hashed at strength ≥ 12; plaintext never logged or persisted
- Constant-time secret comparison via BCryptPasswordEncoder.matches() (AC-TOK-02)
- All key material injected via environment variables — no hardcoded secrets
- Secret rotation grace period enforced via Redis TTL (mcam:grace:{clientId})
- Per-client rate limiting on token issuance (mcam:ratelimit:{clientId})
- Token revocation via Redis tombstone pattern (mcam:revoked:{jti})
```

---

## ✅ Phase 6 Quality Gate Checklist

```bash
# Validate OpenAPI spec
npx @openapitools/openapi-generator-cli validate -i src/main/resources/api.yaml

# Full build + tests
export REPSY_ACCOUNT_USER=<user>
export REPSY_ACCOUNT_PASSWORD=<pass>
mvn test

# Package
mvn -DskipTests package
```

- [ ] `api.yaml` passes OpenAPI 3.0 validation — no errors, no broken `$ref`
- [ ] No existing path schemas removed or modified
- [ ] All 9 new paths documented with correct security annotations (`security: []` for public endpoints)
- [ ] All 8 new schemas defined in `components.schemas`
- [ ] `AuditEventType` enum in `api.yaml` includes all 9 new M2M values (total not less than before + 9)
- [ ] `docs/env/mcam-env-vars.md` created with all 7 variables documented
- [ ] `CHANGELOG` updated under `[Unreleased]`
- [ ] `mvn test` — all tests pass
- [ ] `mvn -DskipTests package` — produces `target/backbone-rest.jar` without errors
- [ ] Release notes sent to integration teams (manual step — PM sign-off)

---

## 📌 Release Readiness Summary

Before marking MCAM as ready for merge to `develop`:

| Gate | Command / Check | Status |
|---|---|---|
| Compilation | `mvn -DskipTests compile` | Must exit 0 |
| PMD | `mvn pmd:check pmd:cpd-check` | 0 violations |
| Unit + Integration tests | `mvn test` | All green |
| JaCoCo coverage | `target/site/jacoco/index.html` | MCAM service ≥ 80% |
| OpenAPI spec | Swagger validation | No errors |
| CHANGELOG | `CHANGELOG` root file | Updated |
| Env vars documented | `docs/env/mcam-env-vars.md` | Created |
| Security review | `security-reviewer` agent | Passed |
| No breaking changes | Diff of `api.yaml` — existing paths only | Confirmed |

