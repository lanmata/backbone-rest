---
name: Project Manager Skills
description: Consolidated skill set for the Project Manager agent — release management, quality gates, sprint planning, and risk assessment
applies-to:
  - Project Manager
---

# Project Manager — Skill Definition

## 1. Release Management

### Release Checklist

```markdown
- [ ] All tests pass (mvn test — green)
- [ ] No PMD violations (mvn pmd:check exits 0)
- [ ] JaCoCo report generated (target/site/jacoco/index.html)
- [ ] OpenAPI spec updated (backbone_rest-openapi.yaml)
- [ ] CHANGELOG updated at project root
- [ ] No critical/high CVEs (Security Reviewer sign-off)
- [ ] Docker image built and pushed
- [ ] Git tag created: v{MAJOR}.{MINOR}.{PATCH}
- [ ] Release notes communicated to integrators
```

### CHANGELOG Format

```markdown
## [1.2.0] - YYYY-MM-DD

### Added
- New endpoint: GET /api/v1/features/{id}

### Fixed
- NPE in UserServiceImpl when alias is null

### Changed
- UserCreateResponse now includes personId field

### Security
- Upgraded jjwt-api to 0.12.6 (CVE-XXXX-YYYY)
```

### Versioning

```
v{MAJOR}.{MINOR}.{PATCH}
```

- **PATCH**: bug fixes, security patches
- **MINOR**: new backward-compatible features
- **MAJOR**: breaking changes (require stakeholder approval)

---

## 2. Quality Gates

| Gate | Tool | Threshold | Failure Action |
|------|------|-----------|---------------|
| Static Analysis | PMD 3.23.0 | Zero violations | Block release |
| Copy-Paste | PMD CPD | Zero duplications | Block release |
| Unit Tests | JUnit 5 | Zero failures | Block release |
| Coverage Report | JaCoCo 0.8.12 | Report generated (min = 0%) | Block release |
| SonarCloud | sonarcloud.io | Manual review | Advise, don't block |

### Quality Commands

```bash
mvn test                      # all gates: tests + PMD + JaCoCo
mvn pmd:check pmd:cpd-check   # PMD only
mvn test jacoco:report        # coverage report: target/site/jacoco/index.html
```

### Quality Gate Report Format

```markdown
## Quality Gate Report — vX.Y.Z

| Gate            | Status   | Notes                     |
|-----------------|----------|---------------------------|
| PMD Violations  | ✅ PASS   | 0 violations               |
| CPD Detection   | ✅ PASS   | 0 duplications             |
| Unit Tests      | ✅ PASS   | 142 tests, 0 failures      |
| JaCoCo Coverage | ✅ PASS   | 74% line coverage          |
| SonarCloud      | ⚠️ REVIEW | 2 code smells (not blocking) |
```

---

## 3. Sprint Planning

### Sprint Structure

- **Sprint length**: 2 weeks
- **Ceremonies**: Planning → Daily standups → Review → Retrospective

### Definition of Done (DoD)

```markdown
- [ ] Code follows backbone-rest conventions (interface-first, constructor injection)
- [ ] Unit tests pass (mvn test)
- [ ] No PMD violations
- [ ] JaCoCo coverage report generated
- [ ] OpenAPI spec updated (if API changed)
- [ ] CHANGELOG entry added
- [ ] PR reviewed and approved by Code Reviewer
- [ ] No breaking changes to /api/v1/* endpoints
- [ ] Smoke-tested in QA environment
```

### Story Point Scale

| Points | Complexity | Examples |
|--------|-----------|---------|
| 1 | Trivial | Add a constant, fix a log message |
| 2 | Small | Fix a bug in existing service method |
| 3 | Medium | New endpoint following existing pattern |
| 5 | Large | New domain module (api + service + mapper + tests) |
| 8 | X-Large | Cross-domain feature, security change, major refactor |
| 13 | Epic | Break into sub-stories |

### Sprint Backlog Template

```markdown
## Sprint N — <Sprint Goal>
Dates: YYYY-MM-DD to YYYY-MM-DD | Capacity: X story points

| ID | Story | Points | Assignee | Status |
|----|-------|--------|----------|--------|
| US-001 | ... | 3 | Developer | In Progress |
```

---

## 4. Risk Assessment

### Risk Matrix

| Probability \ Impact | Low | Medium | High |
|----------------------|-----|--------|------|
| **High** | Medium | High | Critical |
| **Medium** | Low | Medium | High |
| **Low** | Low | Low | Medium |

### Known Project Risks

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|-----------|
| `com.prx:persistence` breaking change | Medium | High | Pin version; test upgrade in branch |
| Repsy outage blocks CI | Low | High | Cache `~/.m2`; maintain fallback |
| PMD rule addition breaks existing code | Medium | Medium | Run `mvn pmd:check` locally before merge |
| SSL certificate expiry | Medium | Critical | Monitor cert expiry; automate renewal alerts |
| API breaking change without notice | Low | Critical | API Reviewer gate + backward compat rule |

### Escalation Path

| Risk Level | Action |
|-----------|--------|
| Low / Medium | Developer resolves in current sprint |
| High | PM + PO discuss in sprint review |
| Critical | Immediate escalation; block release |

---

## 5. Subagent Delegation

| Task | Delegate To |
|------|------------|
| Clarify acceptance criteria | Product Owner |
| Implement feature/fix | Developer |
| Write/improve tests | QA / Test Writer |
| Analyze requirements | Repo Requirements Analyst |
| Review API contracts | API Reviewer |
| Check security vulnerabilities | Security Reviewer |
| Review code quality | Code Reviewer |
| Docker / pipeline setup | DevOps Engineer |
| Data model questions | Database Architect |

---

## Constraints

- Encourage incremental, small releases to reduce risk.
- Ensure changes are documented and communicated to integrators.
- Do NOT make implementation decisions — focus on delivery, quality, and coordination.

