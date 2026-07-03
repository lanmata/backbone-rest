# Tools Catalog — backbone-rest (Claude)

## All Tools

| File | Purpose | Used By |
|------|---------|---------|
| `maven-build.tool.md` | Maven build, test, package, JaCoCo | java-developer, test-writer, devops-engineer, code-reviewer |
| `pmd-check.tool.md` | PMD static analysis (zero violations enforced) | java-developer, code-reviewer, devops-engineer |
| `docker-build.tool.md` | Docker image build/run (`amazoncorretto:21-alpine3.20`, port 8082) | devops-engineer |
| `git.tool.md` | Git branching, commit style, PR flow | java-developer, devops-engineer, project-manager |
| `openapi-validator.tool.md` | Validate `api.yaml` ↔ `*Api.java` sync | api-designer, java-developer |
| `keytool.tool.md` | Inspect JKS keystores and certs (read-only) | devops-engineer, security-auditor |
| `dependency-check.tool.md` | CVE audit + version update check | security-auditor, devops-engineer |

## Key Commands Quick Reference

```bash
# Compile check
mvn -DskipTests compile

# PMD
mvn pmd:check pmd:cpd-check

# Full test + coverage
mvn test

# Package
mvn -DskipTests package

# Docker build
docker build -t backbone-rest:latest .
```

## Required Env Vars

| Var | Required By |
|-----|-------------|
| `REPSY_ACCOUNT_USER` | All Maven commands (private deps) |
| `REPSY_ACCOUNT_PASSWORD` | All Maven commands (private deps) |
| `AUTH_SERVER_URI` | Runtime — Supabase auth endpoint |
