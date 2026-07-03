---
name: DevOps Engineer Skills
description: Consolidated skill set for the DevOps Engineer agent — Maven build, Docker, Repsy artifact publishing, CI/CD for backbone-rest
applies-to:
  - devops-engineer
---

# DevOps Engineer — Skill Definition

## 1. Maven Build Commands

```bash
# Fast compile check (no tests)
mvn -DskipTests compile

# Full test + PMD + JaCoCo
mvn test

# Single test class
mvn -Dtest=UserServiceImplTest test

# Package JAR (skip tests)
mvn -DskipTests package

# PMD only
mvn pmd:check pmd:cpd-check

# JaCoCo report
mvn test jacoco:report && open target/site/jacoco/index.html

# Dependency tree
mvn dependency:tree

# CI with Repsy settings
mvn -s ci_settings.xml -DskipTests package
```

> **No `mvnw`** — always use `mvn` directly.
> Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` before any Maven command.

---

## 2. Docker Build

```bash
# Build image
docker build -t backbone-rest:latest .

# Run container
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=remote-supabase \
  -e AUTH_SERVER_URI=<supabase-auth-url> \
  backbone-rest:latest
```

Base image: `amazoncorretto:21-alpine3.20`
Exposed port: `8082`
JAR: `target/backbone-rest.jar`

---

## 3. Artifact Output

| Artifact | Location |
|----------|----------|
| JAR | `target/backbone-rest.jar` |
| PMD report | `target/pmd.xml` |
| JaCoCo HTML | `target/site/jacoco/index.html` |
| Docker image | `backbone-rest:latest` (local) |

---

## 4. Private Dependency Resolution

Private dependencies (`com.umdc.*`, `com.prx.*`) are hosted on Repsy.

Required env vars:
- `REPSY_ACCOUNT_USER`
- `REPSY_ACCOUNT_PASSWORD`

CI settings file: `ci_settings.xml` — pass as `mvn -s ci_settings.xml`.

---

## 5. Constraints

- Never run `mvnw` — it does not exist in this repo.
- Never modify `keystore.jks`, `backbone.jks`, `umdc-truststore.jks`, or `*.crt`.
- Never commit real secrets — `default.env` is stubs only.
- Docker image must expose only port 8082.
- Always verify `mvn test` passes before declaring build SUCCESS.

---

## 6. Checklist

- [ ] `mvn -DskipTests compile` passes
- [ ] `mvn test` passes (0 failures, 0 PMD violations)
- [ ] JAR produced at `target/backbone-rest.jar`
- [ ] Docker image builds without errors
- [ ] Container starts and responds on port 8082
- [ ] No secrets in Dockerfile or CI config
