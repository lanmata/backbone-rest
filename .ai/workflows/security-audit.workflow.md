---
name: Security Audit
description: Workflow for comprehensive security assessment
trigger: manual
agents:
  - Security Reviewer
  - Developer
  - Project Manager
---

# Security Audit Workflow

## Purpose

Performs a comprehensive security assessment of the **backbone-rest** microservice
including dependency scanning, code review, and configuration audit.

## Workflow Steps

### Step 1: Dependency Scan
**Agent**: Security Reviewer
**Action**: Scan all dependencies for known CVEs

```
#runSubagent agentName="Security Reviewer"
"Perform a full dependency vulnerability scan on backbone-rest.
Check all Maven dependencies in pom.xml for CVEs using validate_cves.
Key dependencies: spring-boot 3.4.1, spring-cloud 4.2.0, postgresql 42.7.4,
snakeyaml 2.3, jjwt 0.12.3, xstream 1.4.21, commons-fileupload 1.5."
```

**Output**: CVE report with severity ratings and fix versions

---

### Step 2: Code Security Review
**Agent**: Security Reviewer
**Action**: Review code for OWASP Top 10 vulnerabilities

```
#runSubagent agentName="Security Reviewer"
"Review backbone-rest for OWASP Top 10 vulnerabilities. Focus on:
1. Plain-text password comparison in SessionServiceImpl (A04)
2. @CrossOrigin(origins='*') on all controllers (A05)
3. JWT secret strength and APP_TOKEN_SECRET usage (A02)
4. Keycloak JWT extraction in JwtConverter (A07)
5. No hardcoded secrets in bootstrap.yml or default.env (A02)
6. Input validation with Jakarta annotations (A03)"
```

**Output**: Security findings report with severity classification

---

### Step 3: Remediation
**Agent**: Developer
**Action**: Fix identified vulnerabilities

```
#runSubagent agentName="Developer"
"Remediate the following security findings in backbone-rest:
[paste security findings]
Prioritize critical and high severity items.
Keep changes minimal and atomic. Run mvn test to verify."
```

**Output**: Security fixes

---

### Step 4: Release Assessment
**Agent**: Project Manager
**Action**: Assess release readiness from security perspective

```
#runSubagent agentName="Project Manager"
"Assess backbone-rest release readiness based on the security audit results.
Verify all critical/high findings are resolved or have accepted mitigations."
```

**Output**: Security gate pass/fail determination

## Exit Criteria

- [ ] All dependencies scanned for CVEs
- [ ] No unmitigated critical/high vulnerabilities
- [ ] OWASP Top 10 review completed
- [ ] Plain-text password handling documented or remediated
- [ ] `bootstrap.yml` and `default.env` contain no real secrets
- [ ] `keystore.jks` not modified
- [ ] Security gate approved for release
