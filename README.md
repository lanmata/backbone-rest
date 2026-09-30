<div align="center">

<img src="docs/images/um-dev-creatives-logo.png" alt="UM Dev Creatives" width="120" />

# 🧩 Backbone REST

**Spring Boot backoffice REST service for PRX** — users, roles, contacts, features, people,
sessions, and profile images, backed by Supabase (auth + Postgres pooler + Storage).

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=lanmata_backbone-rest)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)

[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=coverage)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Maintainability issues](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=software_quality_maintainability_issues)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
[![Reliability issues](https://sonarcloud.io/api/project_badges/measure?project=lanmata_backbone-rest&metric=software_quality_reliability_issues)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)

<br/>

[![Java](https://img.shields.io/badge/Java-25%20LTS-blue?logo=java&style=flat-square)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=spring&style=flat-square)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-brightgreen?logo=spring&style=flat-square)](https://spring.io/projects/spring-cloud)
[![Maven](https://img.shields.io/badge/Maven->=3.8-red?logo=apachemaven&style=flat-square)](https://maven.apache.org/)

[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.13-blue?logo=postgresql&style=flat-square)](https://www.postgresql.org/)
[![Supabase](https://img.shields.io/badge/Supabase-Auth%20%2B%20Postgres-3FCF8E?logo=supabase&style=flat-square)](https://supabase.com/)
[![Redis](https://img.shields.io/badge/Redis-Lettuce%20client-red?logo=redis&style=flat-square)](https://redis.io/)
[![Cloudflare R2](https://img.shields.io/badge/Cloudflare%20R2-object%20storage-orange?logo=cloudflare&style=flat-square)](https://developers.cloudflare.com/r2/)

[![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-blue?logo=mapstruct&style=flat-square)](https://mapstruct.org/)
[![JJWT](https://img.shields.io/badge/JJWT-0.13.0-blue?style=flat-square)](https://github.com/jwtk/jjwt)
[![JUnit](https://img.shields.io/badge/JUnit-6.1.3-red?logo=junit&style=flat-square)](https://junit.org/)
[![Mockito](https://img.shields.io/badge/Mockito-5.23.0-red?logo=mockito&style=flat-square)](https://site.mockito.org/)

[![Docker base image](https://img.shields.io/badge/amazoncorretto-21--alpine3.20-blue?logo=docker&style=flat-square)](https://hub.docker.com/_/amazoncorretto)
[![GraalVM Native Image](https://img.shields.io/badge/GraalVM-Native%20Image%20available-orange?logo=graalvm&style=flat-square)](https://www.graalvm.org/)
[![SonarCloud](https://img.shields.io/badge/SonarCloud-detected-4E9BCF?logo=sonarcloud&style=flat-square)](https://sonarcloud.io/)

</div>

Overview
--------
Backbone REST is the backoffice API for PRX. It manages users, roles, contacts, features, people, sessions, and profile images, and integrates with Supabase for authentication (JWT), pooled PostgreSQL access, and Storage. Authentication is dual-mode: an OAuth2 resource server validates Supabase/Keycloak JWTs on `/api/v1/**` GETs, while a separate application-minted session JWT (JJWT) is used for the session-token login flow, transmitted via a `session-token` header. A managed-client (M2M) client-credentials flow (MCAM) issues its own short-lived Bearer tokens for machine clients, backed by Redis for token/JTI/rate-limit state.

Requirements
------------
Minimum requirements to build and run Backbone REST locally:
- Java 25 (JDK, LTS) or a compatible runtime (Amazon Corretto 25 recommended)
- Maven 3.8+ — **there is no `mvnw` wrapper in this repo**; use your own `mvn`
- `REPSY_ACCOUNT_USER` / `REPSY_ACCOUNT_PASSWORD` environment variables, for the private PRX artifacts (`com.umdc:commons`, `commons-services`, `security-oauth`, `persistence`) hosted on Repsy
- Docker (optional; required to run the provided `Dockerfile`/`Dockerfile.native` locally)
- A Supabase project (Postgres pooler + Auth + Storage) or a local PostgreSQL instance, and Redis, if you need to run the app rather than just `mvn test`

Quick build
-----------
This project uses Maven — no wrapper script. From the repository root:

```bash
# Fast syntax check (compile only, no tests)
mvn -DskipTests compile

# Full suite: PMD + JaCoCo + JUnit — the correctness/quality gate
mvn test

# Run a single test class
mvn -Dtest=UserServiceImplTest test

# Produce the executable JAR (target/backbone-rest.jar)
mvn -DskipTests package
```

Running locally requires the runtime environment variables normally supplied by Vault/Config
Server. `default.env` (git-ignored, local secrets allowed) and `config/local.env` supply these
for the `remote-supabase` / `local` profiles:

```bash
set -a && source default.env && set +a
mvn spring-boot:run
```

> Forgetting to source the env file fails fast with `Profile
> '${SPRING_BOOT_PROFILE_ACTIVE}' must contain a letter, digit or allowed char` — that
> placeholder is only ever resolved from the environment, never defaulted in `application.yml`.

Docker setup
------------
* Build the image:
  ```bash
  docker build --secret id=prx_internal_ca,src=certs/backbone/prx-internal-ca.crt \
    -t lamata/backbone-rest .
  ```
  `certs/backbone/` is entirely git-ignored, so the internal CA cert is passed in as a BuildKit
  secret rather than `COPY`-ed — the build never depends on that file being present in a fresh
  checkout.

* Push the image to Docker Hub:
  ```bash
  docker push lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]
  ```

* Pull the image from the registry:
  ```bash
  docker pull lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]
  ```

* Run the container (exposes port 8084):
  ```bash
  docker run --rm -p 8084:8084 lamata/backbone-rest
  ```

Build and run as a GraalVM Native Image
----------------------------------------
Backbone REST also builds as a GraalVM native executable — near-instant startup and a much
smaller memory footprint than the JIT mode above, at the cost of a much longer build. Same
pattern as mercury's own native build (MER-5) — see that repo's
`docs/architecture/graalvm-native-image.md` for the underlying story.

**JIT mode (default) vs Native mode — when to use which:**
- JIT mode (`mvn spring-boot:run`, or the regular `Dockerfile`): day-to-day development. Fast
  rebuilds, full debugger support, `mvn test`/Mockito work exactly as normal.
- Native mode (`Dockerfile.native`): validating a release candidate's startup/memory profile, or
  the actual production image. Rebuilds take minutes, not seconds — don't use it for iterative
  development.

**Building and running the native executable locally** (not in Docker): a native build requires
a full Spring context boot at build time (`spring-boot:process-aot`), which normally means live
Vault/Config Server/Postgres — `config/native.env` supplies safe placeholder values for all of
that instead, so the build works without live infrastructure:
```bash
set -a; source config/native.env; set +a
mvn -Pnative clean package -DskipTests
mvn -Pnative native:compile   # produces target/backbone-rest (the native executable)

./target/backbone-rest
```

**Building the native Docker image:** two stages — GraalVM 25.0.4 + Maven 3.9.14 (pinned tarball
installs) on a `debian:12-slim` builder — **not** a GraalVM community builder image, which is
built on Oracle Linux 10 and requires an x86-64-v3 CPU baseline (AVX2/BMI2) that fails outright
("Fatal glibc error") on older deployment hardware:
```bash
DOCKER_BUILDKIT=1 docker build -f Dockerfile.native \
  --secret id=maven_settings,src=$HOME/.m2/settings.xml \
  --secret id=prx_internal_ca,src=certs/backbone/prx-internal-ca.crt \
  -t prx/backbone-rest:native .
docker run --rm -p 8084:8084 prx/backbone-rest:native
```

**What does NOT run the same way in native mode:**
- `mvn test` (the JUnit 5 + Mockito suite) is **not** run against the native image — Mockito's
  default inline mock maker does not support native-image test execution
  ([mockito/mockito#2435](https://github.com/mockito/mockito/issues/2435)). The JVM test suite
  (`mvn test`) remains the correctness gate; the native build should be verified with a runtime
  smoke test instead.

Known issues and workarounds
-----------------------------
1. **`Profile '${SPRING_BOOT_PROFILE_ACTIVE}' must contain a letter, digit or allowed char`**
   - Symptom: `mvn spring-boot:run` fails immediately with a property-binding error.
   - Cause: `SPRING_BOOT_PROFILE_ACTIVE` is only ever resolved from the environment — there is no
     default in `application.yml`.
   - Workaround: `set -a && source default.env && set +a` (or `config/local.env`) before running.

2. **Missing internal CA cert / keystore when building the Docker image**
   - Symptom: `docker build` fails because `certs/backbone/prx-internal-ca.crt` isn't found.
   - Cause: `certs/backbone/` is entirely git-ignored.
   - Workaround: pass it as a BuildKit secret (`--secret id=prx_internal_ca,src=...`), as shown
     above — never add a plain `COPY certs/backbone/` step, it silently copies nothing on a fresh
     checkout.

3. **Transitive dependency vulnerability warnings from IDE/Dependabot**
   - Workaround: pin the vulnerable transitive artifact via a `<properties>` entry named
     `<lib>.version` plus a matching override in `<dependencyManagement>` — see `tomcat.version`,
     `bouncycastle.version`, `jackson.version`, `netty.version` in `pom.xml` for worked examples.
     Always verify with `mvn dependency:tree -Dincludes=<groupId>:<artifactId>` that the override
     actually took effect.

4. **Sonar coverage thresholds fail locally but not in CI, or vice versa**
   - Workaround: run `mvn test` first so JaCoCo's XML report exists at
     `target/site/jacoco/jacoco.xml` before invoking `sonar:sonar` (see below).

Continuous Integration
-----------------------
This repository's CI lives in `.gitlab-ci.yml` (GitLab CI, not GitHub Actions), with two jobs
running on merge requests and on `master`/`Develop`:
- **`sonarcloud-check`** — `mvn verify sonar:sonar -Dspring.cloud.vault.enabled=false -s ci_settings.xml`
- **`qodana`** — JetBrains Qodana static analysis, results published as a GitLab Code Quality
  report

## How to verify Sonar coverage locally

1) Generate the JaCoCo XML report (runs tests and produces XML/HTML reports):
   ```bash
   mvn -DskipITs clean verify
   ```

2) Confirm the JaCoCo XML report exists at the path `pom.xml` configures:
   ```bash
   test -f target/site/jacoco/jacoco.xml && echo "report present"
   ```

3) Run Sonar analysis locally (requires a Sonar token):
   ```bash
   mvn sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=<SONAR_TOKEN>
   ```

Notes:
- `pom.xml` sets `sonar.coverage.jacoco.xmlReportPaths` to `target/site/jacoco/jacoco.xml` and
  scopes `sonar.inclusions`/`sonar.coverage.inclusions` to `src/main/java/**/*.java`, excluding
  generated/config/mapper/test code from coverage accounting.

## Documentation

- `CHANGELOG` — project changelog and migration notes (Keep a Changelog / SemVer)
- `CLAUDE.md` — architecture, package structure, mandatory conventions, security model
- `src/main/resources/static/api.yaml` — OpenAPI 3.1 spec, served at `/api.yaml`
- `LICENSE` — proprietary, all rights reserved (see below)

Domain modules (`v1/`)
-----------------------
| Module | Purpose |
|---|---|
| `addresses` | Postal address records |
| `application` | Application (tenant) registry |
| `contacts` / `contacttypes` | Contact records and their types |
| `features` | Feature flags/entitlements linked to roles |
| `iam` | Audit trail, permission checks, password policy, token/JTI deny-list |
| `identificationdocuments` | Identification document records |
| `managedclient` | MCAM — M2M client-credentials auth (registration, token issuance, rotation, audit) |
| `notices` / `noticetypes` | Notices and their types |
| `people` | Person records |
| `profileimage` | Profile image upload/retrieve via Cloudflare R2 |
| `report` | Document generation (template-based) |
| `rolefeatures` | Shared Role↔Feature link service (no REST surface) |
| `roles` | Role registry |
| `servicetype` | Service type registry |
| `session` | Login, token renewal/refresh, session JWT minting |
| `users` | User CRUD, role linking, password management |

Tech stack and versions
-----------------------
| Technology | Version | Source |
|---|--------------:|---|
| Amazon Corretto (Docker base image) | 21-alpine3.20 | Dockerfile |
| GraalVM (native build) | 25.0.4 | Dockerfile.native |
| Java (language / runtime) | 25 | pom.xml |
| Spring Boot | 4.1.1 | pom.xml |
| Spring Cloud | 2025.1.3 | pom.xml |
| Maven (build tool) | >=3.8 | pom.xml / Dockerfile.native (3.9.14 pinned) |
| PostgreSQL JDBC driver | 42.7.13 | pom.xml |
| AWS SDK (S3-compatible client, used for Cloudflare R2) | 2.54.5 | pom.xml |
| MapStruct | 1.6.3 | pom.xml |
| JJWT (session JWT) | 0.13.0 | pom.xml |
| Springdoc OpenAPI | 3.1.0 | pom.xml |
| JUnit Jupiter | 6.1.3 | pom.xml |
| Mockito | 5.23.0 | pom.xml |
| Redis (Spring Data + Lettuce) | detected | pom.xml |
| Supabase (Auth + Postgres pooler + Storage) | detected | CLAUDE.md / application.yml |
| SonarCloud (project properties present) | detected | pom.xml, .gitlab-ci.yml |

Note: "detected" means the technology is present but there is no single pinned version string to
extract (managed by a BOM, or an external managed service).

Files scanned
-------------
- `pom.xml` — project metadata, properties, dependencies, plugin versions
- `Dockerfile` / `Dockerfile.native` — base image tags and runtime container instructions
- `.gitlab-ci.yml` — CI pipeline (SonarCloud, Qodana)
- `CLAUDE.md` — project architecture and conventions

License
-------
Proprietary and confidential — Copyright (c) 2024-2026 UM Dev Creative. All Rights Reserved. See
`LICENSE` for the full terms; this is not open-source software.

More
----
For architecture, security model, and convention details, see `CLAUDE.md`. For questions, reach
out to:

<luis.antonio.mata@gmail.com>

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=lanmata_backbone-rest)
