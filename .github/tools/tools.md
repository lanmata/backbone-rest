# Tools Catalog

This folder describes tools that AI agents can invoke when assisting with **backbone-rest** tasks.
Each `.tool.md` file is referenced by one or more agents via `tool-docs` in their frontmatter.

## Tools Available

| File | Tool | Used By |
|------|------|---------|
| `maven-build.tool.md` | Maven Build | Developer, Test Writer, Code Reviewer, Database Architect, DevOps Engineer, Project Manager, Repo Requirements Analyst |
| `pmd-check.tool.md` | PMD Check | Code Reviewer, Developer, Test Writer |
| `docker-build.tool.md` | Docker Build | DevOps Engineer, Developer |
| `keytool.tool.md` | Keytool / OpenSSL | DevOps Engineer |
| `git.tool.md` | Git | DevOps Engineer, Project Manager |
| `github-cli.tool.md` | GitHub CLI | DevOps Engineer, Project Manager |
| `sonar-analysis.tool.md` | Sonar Analysis | DevOps Engineer, Project Manager |
| `dependency-check.tool.md` | Dependency Check | Security Reviewer |
| `openapi-validator.tool.md` | OpenAPI Validator | API Reviewer, Developer |

## Removed

| File | Reason |
|------|--------|
| `maven.md` | Duplicate of `maven-build.tool.md`; referenced `mvnw` (incorrect — this project has no wrapper) |

## Key Commands Quick Reference

```bash
# Build
mvn -DskipTests compile                   # fast compile check
mvn test                                   # full build: tests + PMD + JaCoCo
mvn -DskipTests package                   # produce target/backbone-rest.jar

# PMD
mvn pmd:check pmd:cpd-check               # zero-tolerance static analysis gate

# Docker
docker build -t lamata/backbone-rest .    # requires JAR built first

# Certificates
openssl x509 -in wildcard.tst.crt -text -noout   # view cert
keytool -list -v -keystore keystore.jks ...       # list keystore entries

# Git / Release
git tag -a vX.Y.Z -m "Release" && git push origin vX.Y.Z
gh release create vX.Y.Z --title "vX.Y.Z" --notes "..."
```

> **No `mvnw`** — always use `mvn` directly.  
> Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` before any Maven command.
