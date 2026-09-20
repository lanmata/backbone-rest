# 📚 MCAM Prompt Index — Management Client Authentication Manager

> **Project:** backbone-rest  
> **Feature:** Management Client Authentication Manager (MCAM)  
> **Plan reference:** `docs/plans/mcam-implementation-plan.md`  
> **Requirements reference:** `docs/requirements/managed-client-auth.md`

---

## 🗂️ Phase Prompt Files

Each prompt file is a self-contained, actionable instruction set for the `developer` or `test-writer` agent for that phase. Run them **in order** — each phase depends on the previous one being complete.

| Phase | File | Agent | Duration | Complexity | Depends On |
|---|---|---|---|---|---|
| **Phase 1** — Foundation & Domain Setup | [`phase-1-foundation.md`](./phase-1-foundation.md) | `developer` | 3–4 days | Medium | None |
| **Phase 2** — Core CRUD API | [`phase-2-crud-api.md`](./phase-2-crud-api.md) | `developer` | 3–4 days | Medium | Phase 1 |
| **Phase 3** — Authentication Flow | [`phase-3-auth-flow.md`](./phase-3-auth-flow.md) | `developer` | 4–5 days | High | Phase 1 + 2 |
| **Phase 4** — Secret Rotation & Audit | [`phase-4-rotation-audit.md`](./phase-4-rotation-audit.md) | `developer` | 2–3 days | Medium | Phase 1 + 2 + 3 |
| **Phase 5** — Tests + PMD + JaCoCo | [`phase-5-tests.md`](./phase-5-tests.md) | `test-writer` | 3–4 days | Medium | Phase 1 + 2 + 3 + 4 |
| **Phase 6** — OpenAPI Spec + Docs | [`phase-6-openapi-docs.md`](./phase-6-openapi-docs.md) | `api-reviewer` + `developer` | 1–2 days | Low | Phase 5 |

**Total estimated duration:** 16–22 working days (3 × 2-week sprints for a solo developer)

---

## 🏃 Sprint Assignment

| Sprint | Phases | Days | Exit Criteria |
|---|---|---|---|
| **Sprint 1** (Days 1–10) | Phase 1 + Phase 2 | 10 | CRUD endpoints functional, PMD clean, secret returned once, no secret in GET |
| **Sprint 2** (Days 11–20) | Phase 3 + Phase 4 | 10 | All 9 endpoints working, token flow verified, rotation + audit confirmed |
| **Sprint 3** (Days 21–30) | Phase 5 + Phase 6 | 10 | All 15 ACs tested, JaCoCo ≥ 80%, OpenAPI updated, CHANGELOG written |

---

## ✅ Acceptance Criteria Coverage Map

| AC ID | Description | Prompt |
|---|---|---|
| AC-REG-01 | `POST /managed-clients` → `201` with one-time `clientSecret` | Phase 2 + Phase 5 |
| AC-REG-02 | Duplicate name+appId → `409` | Phase 2 + Phase 5 |
| AC-REG-03 | Missing name / empty scopes → `400` | Phase 2 + Phase 5 |
| AC-TOK-01 | Valid credentials → `200` signed M2M JWT with all required claims | Phase 3 + Phase 5 |
| AC-TOK-02 | Wrong `clientSecret` → `401 invalid_client` (constant-time) | Phase 3 + Phase 5 |
| AC-TOK-03 | Inactive client → `401 invalid_client` | Phase 3 + Phase 5 |
| AC-TOK-04 | Out-of-range scope → `400 invalid_scope` with allowed list | Phase 3 + Phase 5 |
| AC-ROT-01 | Rotate → new secret + grace period + `CLIENT_SECRET_ROTATED` audit | Phase 4 + Phase 5 |
| AC-REV-01 | Revoke → introspect returns `active=false` | Phase 3 + Phase 5 |
| AC-INT-01 | Valid token introspection → `200 active=true` with full claims | Phase 3 + Phase 5 |
| AC-INT-02 | Expired/revoked token introspection → `200 active=false` (never 4xx) | Phase 3 + Phase 5 |
| AC-AUD-01 | All 9 lifecycle events queryable via audit endpoint | Phase 4 + Phase 5 |
| AC-SEC-01 | `secret_hash` is BCrypt — never plaintext in DB | Phase 2 + Phase 5 |
| AC-SEC-02 | `GET /{clientId}` never exposes secret material | Phase 2 + Phase 5 |
| AC-PMD-01 | `mvn pmd:check` passes with 0 violations | All phases |

---

## 🔑 Key Files Created / Modified by MCAM

### New Files

```
src/main/resources/db/migration/
  V2__create_managed_client.sql
  V3__create_managed_client_audit_event.sql
  V4__extend_audit_event_check.sql

src/main/java/com/umdc/backoffice/v1/managedclient/
  domain/
    ManagedClientEntity.java
    ManagedClientAuditEventEntity.java
  repository/
    ManagedClientRepository.java
    ManagedClientAuditEventRepository.java
  api/to/
    ManagedClientCreateRequest.java
    ManagedClientCreateResponse.java
    ManagedClientTO.java
    ManagedClientUpdateRequest.java
    ManagedClientTokenRequest.java
    ManagedClientTokenResponse.java
    ManagedClientSecretRotateResponse.java
    ManagedClientTokenIntrospectResponse.java
    ManagedClientErrorResponse.java
  mapper/
    ManagedClientMapper.java
  config/
    ManagedClientAsyncConfig.java
  service/
    ManagedClientService.java
    ManagedClientServiceImpl.java
    ManagedClientSecretHashService.java
    ManagedClientSecretHashServiceImpl.java
    ManagedClientAuditService.java
    ManagedClientAuditServiceImpl.java
    ManagedClientTokenService.java
    ManagedClientTokenServiceImpl.java
    ManagedClientRedisService.java
    ManagedClientRedisServiceImpl.java
    ManagedClientRotationService.java
    ManagedClientRotationServiceImpl.java
    ManagedClientMaintenanceService.java
    ManagedClientMaintenanceServiceImpl.java
  api/controller/
    ManagedClientApi.java
    ManagedClientController.java

src/main/java/com/umdc/backoffice/security/filter/
  ManagedClientTokenFilter.java

src/test/java/com/umdc/backoffice/v1/managedclient/
  service/
    ManagedClientServiceImplTest.java
    ManagedClientTokenServiceImplTest.java
    ManagedClientRotationServiceImplTest.java
    ManagedClientSecretHashServiceImplTest.java
    ManagedClientRedisServiceImplTest.java
  mapper/
    ManagedClientMapperTest.java
  ManagedClientControllerIT.java
  ManagedClientTokenControllerIT.java
  ManagedClientRotationIT.java

src/test/java/com/umdc/backoffice/security/filter/
  ManagedClientTokenFilterTest.java

docs/env/
  mcam-env-vars.md
```

### Modified Files

```
src/main/java/com/umdc/backoffice/
  property/ManagementAuthenticatorProperties.java       (Phase 1)
  v1/iam/audit/domain/AuditEventType.java               (Phase 1)
  security/config/SecurityConfig.java                   (Phase 2 + Phase 3)
  security/filter/SessionJwtAuthenticationFilter.java   (Phase 3)
  v1/iam/audit/service/AuditEventServiceImpl.java       (Phase 4)
  v1/managedclient/service/ManagedClientServiceImpl.java (Phase 4)
  v1/managedclient/api/controller/ManagedClientApi.java  (Phase 3 + Phase 4)
  v1/managedclient/api/controller/ManagedClientController.java (Phase 3 + Phase 4)

src/main/resources/
  application.yml                                        (Phase 1)
  api.yaml                                              (Phase 6)

CHANGELOG                                               (Phase 6)
```

---

## ⚡ Quick Build Commands

```bash
# Fast compile check (run after every file change)
mvn -DskipTests compile

# PMD violation check
mvn pmd:check pmd:cpd-check

# Full test suite
mvn test

# JaCoCo coverage report
mvn test jacoco:report
open target/site/jacoco/index.html

# Package JAR
mvn -DskipTests package
```

---

*Generated by Orchestrator — backbone-rest MCAM feature*

