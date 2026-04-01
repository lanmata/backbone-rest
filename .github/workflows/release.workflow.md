---
name: Release
description: Workflow for preparing and executing a release
trigger: manual
agents:
  - Project Manager
  - Security Reviewer
  - API Reviewer
  - Developer
  - DevOps Engineer
---

# Release Workflow

## Purpose

Coordinates the full release process for the **backbone-rest** microservice,
from preparation through deployment and verification.

> **Note**: There are no automated CI/CD pipelines yet — all steps are manual.

## Workflow Steps

### Step 1: Release Planning
**Agent**: Project Manager
**Action**: Define release scope and checklist

```
#runSubagent agentName="Project Manager"
"Prepare release plan for backbone-rest version [X.Y.Z].
Define scope, update CHANGELOG at project root, and verify quality gate requirements
(PMD clean, tests passing)."
```

---

### Step 2: Security Audit
**Agent**: Security Reviewer
**Action**: Pre-release security scan

```
#runSubagent agentName="Security Reviewer"
"Perform pre-release security audit for backbone-rest.
Scan all Maven dependencies for CVEs (use validate_cves tool).
Verify no sensitive data in bootstrap.yml or default.env.
Flag any plain-text password handling or overly permissive @CrossOrigin usage."
```

---

### Step 3: API Contract Validation
**Agent**: API Reviewer
**Action**: Validate all API contracts

```
#runSubagent agentName="API Reviewer"
"Validate all API contracts for the backbone-rest release.
Ensure backbone_rest-openapi.yaml is consistent with all *Api.java interfaces.
Check backward compatibility for all /api/v1/* endpoints."
```

---

### Step 4: Full Build Verification
**Agent**: Developer

```bash
# Full build: tests + PMD + JaCoCo (must all pass)
mvn test

# Generate and review coverage report
mvn test jacoco:report
open target/site/jacoco/index.html
```

---

### Step 5: Release Execution
**Agent**: DevOps Engineer

```bash
# Update version in pom.xml if needed, then:
git tag -a v[X.Y.Z] -m "Release [X.Y.Z]"
git push origin v[X.Y.Z]

# Publish to Repsy (requires REPSY_ACCOUNT_USER + REPSY_ACCOUNT_PASSWORD)
mvn -DskipTests deploy

# Build and push Docker image
mvn -DskipTests package
docker build -t lamata/backbone-rest:[X.Y.Z] .
docker push lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]
```

---

### Step 6: Post-Release Verification
**Agent**: Project Manager

- Verify deployment health via Spring Boot Actuator (`/actuator/health`)
- Confirm `CHANGELOG` is updated
- Communicate release notes to integrators

## Release Checklist

- [ ] `mvn test` passes (PMD clean, all tests green)
- [ ] JaCoCo report generated and reviewed
- [ ] No unmitigated critical/high CVEs
- [ ] `backbone_rest-openapi.yaml` updated for any API changes
- [ ] `CHANGELOG` updated at project root
- [ ] Release tagged and artifact pushed to Repsy
- [ ] Docker image built and pushed
