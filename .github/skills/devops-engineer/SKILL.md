---
name: DevOps Engineer Skills
description: Consolidated skill set for the DevOps Engineer agent — Maven build, Docker, GitHub Actions, CI/CD orchestration, and release management
applies-to:
  - DevOps Engineer
---

# DevOps Engineer — Skill Definition

## 1. Maven Build

> **No `mvnw`** — always invoke `mvn` directly.  
> Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` before any Maven command.

```bash
mvn -DskipTests compile          # fast compile check
mvn test                         # full build: tests + PMD + JaCoCo
mvn -DskipTests package          # produce target/backbone-rest.jar
mvn test jacoco:report           # generate coverage report
mvn pmd:check pmd:cpd-check      # static analysis only
mvn deploy -s ci_settings.xml    # publish to Repsy
```

### Repsy Private Repository

```xml
<!-- ci_settings.xml already configured; expose as env vars in CI -->
REPSY_ACCOUNT_USER=<user>
REPSY_ACCOUNT_PASSWORD=<password>
```

Maven annotation processing (MapStruct):
```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <source>21</source><target>21</target>
    <annotationProcessorPaths>
      <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

---

## 2. Docker Containerization

- **Base image**: `amazoncorretto:21-alpine3.20`
- **Port exposed**: `8082`
- **JAR**: `target/backbone-rest.jar` (must be built before Docker)

```bash
mvn -DskipTests package
docker build -t lamata/backbone-rest .
docker build -t lamata/backbone-rest:1.2.0 .
docker push lamata/backbone-rest
```

### Certificate Import at Build Time

```dockerfile
RUN keytool -importcert -alias wildcard-tst \
    -file /certs/wildcard.tst.crt \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt
```

Certs imported: `prx-qa.crt`, `prx-qa.manager.crt`, `srmn.crt`, `prx-qa.config-server.crt`

### Runtime Environment Variables

```bash
docker run -p 8082:8082 \
  -e APP_PORT=8082 \
  -e APP_TOKEN_SECRET=<base64-secret> \
  -e SSL_KEYSTORE_LOCATION=/certs/keystore.jks \
  -e SSL_KEYSTORE_PASSWORD=<password> \
  -e SSL_KEYSTORE_TYPE=JKS \
  -e VAULT_TOKEN=<token> \
  -e VAULT_SERVER_URL=https://vault.prx.tst \
  -e AUTH_SERVER_URI=https://keycloak.prx.tst/realms/prx \
  -e AUTH_CERT_URI=/protocol/openid-connect/certs \
  -e SPRING_BOOT_PROFILE_ACTIVE=qa \
  lamata/backbone-rest
```

### Image Hardening

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1
```

---

## 3. GitHub Actions

```yaml
# .github/workflows/ci.yml
name: CI Pipeline
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'corretto'
          cache: 'maven'

      - name: Build and Test
        run: mvn test -s ci_settings.xml
        env:
          REPSY_ACCOUNT_USER: ${{ secrets.REPSY_ACCOUNT_USER }}
          REPSY_ACCOUNT_PASSWORD: ${{ secrets.REPSY_ACCOUNT_PASSWORD }}

      - name: Package
        run: mvn -DskipTests package

      - name: Upload coverage report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/
```

### Required GitHub Secrets

| Secret | Purpose |
|--------|---------|
| `REPSY_ACCOUNT_USER` | Maven private dependency resolution |
| `REPSY_ACCOUNT_PASSWORD` | Maven private dependency resolution |
| `DOCKER_USERNAME` | Docker Hub push |
| `DOCKER_TOKEN` | Docker Hub authentication |

---

## 4. Quality Gates

| Gate | Tool | Failure Condition |
|------|------|------------------|
| Static Analysis | PMD 3.23.0 | Any violation → build fails |
| Copy-Paste Detection | PMD CPD | Any duplication → build fails |
| Unit Tests | JUnit 5 | Any test failure → build fails |
| Coverage Report | JaCoCo 0.8.12 | Report failure (minimum = 0%) |
| SonarCloud | sonarcloud.io | Manual review only |

---

## 5. Release Management

### Release Checklist

```markdown
- [ ] All tests pass (mvn test)
- [ ] No PMD violations
- [ ] JaCoCo report generated
- [ ] OpenAPI spec updated for any API changes
- [ ] CHANGELOG updated
- [ ] No critical/high CVEs in dependencies
- [ ] Docker image built and pushed
- [ ] Git tag created: v{MAJOR}.{MINOR}.{PATCH}
```

### Tagging

```bash
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0
```

---

## Required Environment Variables

| Variable | Purpose |
|----------|---------|
| `APP_PORT` | Server port (default 8082) |
| `APP_TOKEN_SECRET` | JWT signing secret (Base64) |
| `APP_TOKEN_EXPIRATION` | JWT expiration in ms |
| `SSL_KEYSTORE_LOCATION` | Keystore classpath location |
| `SSL_KEYSTORE_PASSWORD` | Keystore password |
| `SSL_KEYSTORE_TYPE` | `JKS` |
| `SSL_TRUSTSTORE_LOCATION` | Truststore path |
| `SSL_TRUSTSTORE_PASSWORD` | Truststore password |
| `VAULT_TOKEN` | HashiCorp Vault token |
| `VAULT_SERVER_URL` | Vault server URL |
| `CNFS_URI` / `CNFS_PORT` | Config Server |
| `AUTH_SERVER_URI` | Keycloak issuer URI |
| `AUTH_CERT_URI` | Keycloak JWK set URI suffix |
| `AUTH_CLIENT_ID` | OAuth2 client ID |
| `AUTH_CLIENT_SECRET` | OAuth2 client secret |
| `SPRING_BOOT_PROFILE_ACTIVE` | Active Spring profile |
| `REPSY_ACCOUNT_USER` | Repsy Maven repo user |
| `REPSY_ACCOUNT_PASSWORD` | Repsy Maven repo password |

