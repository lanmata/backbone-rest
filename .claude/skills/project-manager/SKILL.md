---
name: Project Manager Skills
description: Consolidated skill set for the Project Manager agent — delivery planning, quality gates, release coordination for backbone-rest
applies-to:
  - project-manager
---

# Project Manager — Skill Definition

## 1. Quality Gates

All must pass before a release or PR merge:

| Gate | Command | Threshold |
|------|---------|-----------|
| Compile | `mvn -DskipTests compile` | 0 errors |
| PMD | `mvn pmd:check pmd:cpd-check` | 0 violations |
| Tests | `mvn test` | 0 failures |
| JaCoCo | (included in `mvn test`) | Per `pom.xml` `<limit>` config |
| OpenAPI sync | Manual review | `*Api.java` ↔ `api.yaml` in sync |

---

## 2. Release Checklist Template

```markdown
## Release Checklist — v<version>

### Code Quality
- [ ] `mvn -DskipTests compile` passes
- [ ] `mvn pmd:check` — 0 violations
- [ ] `mvn test` — 0 failures
- [ ] JaCoCo coverage meets thresholds

### API & Docs
- [ ] `api.yaml` updated for all contract changes
- [ ] `CHANGELOG` entry written
- [ ] `CHANGELOG.new` reviewed and merged

### Security
- [ ] No secrets in committed code
- [ ] `default.env` contains stubs only
- [ ] Dependency CVE scan clean

### Build & Deploy
- [ ] `mvn -DskipTests package` produces `target/backbone-rest.jar`
- [ ] Docker image builds: `docker build -t backbone-rest:<version> .`
- [ ] Container starts on port 8082

### Sign-Off
- [ ] Product Owner verified acceptance criteria
- [ ] Security Auditor approved
- [ ] Code Reviewer approved
```

---

## 3. Risk Register

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| Private dep resolution fails | Low | High | Verify REPSY_ACCOUNT_USER/PASSWORD before build |
| PMD violation introduced | Medium | High | Run `mvn pmd:check` after every change |
| API breaking change | Low | Critical | api-designer review before merge |
| Supabase JWT config mismatch | Low | High | Verify AUTH_SERVER_URI matches env |

---

## 4. Delivery Plan Template

```markdown
## Delivery Plan — <feature title>

| Task | Agent | Dependencies | Status | Notes |
|------|-------|-------------|--------|-------|
| Define AC | product-owner | none | PENDING | |
| Analyze patterns | repo-requirements-analyst | none | PENDING | |
| Implement | java-developer | AC + patterns | PENDING | |
| Write tests | test-writer | Implementation | PENDING | |
| Review API | api-designer | Implementation | PENDING | |
| Review code | code-reviewer | Implementation | PENDING | |
| Security review | security-auditor | Implementation | PENDING | |
| Build & package | devops-engineer | All reviews | PENDING | |
```

---

## 5. Constraints

- Never approve a release with PMD violations or failing tests.
- Never approve breaking API changes without product-owner and api-designer sign-off.
- Always confirm `default.env` contains stubs before any release tagging.
