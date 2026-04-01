# Technology Stack — backbone-rest

---

## 1. Runtime Platform

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 (LTS) |
| JVM | Amazon Corretto | 21-alpine3.20 (Docker) |
| Framework | Spring Boot | 3.4.1 |
| Build tool | Apache Maven | 3.x (no wrapper in repo) |
| Operating System | Alpine Linux | 3.20 (Docker) |

---

## 2. Spring Ecosystem

| Module | Purpose | Version |
|--------|---------|---------|
| `spring-boot-starter-web` | Embedded Tomcat, REST MVC | 3.4.1 |
| `spring-boot-starter-security` | Security filter chain | 3.4.1 |
| `spring-boot-starter-oauth2-resource-server` | JWT Bearer token validation | 3.4.1 |
| `spring-boot-starter-jersey` | JAX-RS support | 3.4.1 |
| `spring-boot-starter-actuator` | Health, metrics, info endpoints | 3.4.1 |
| `spring-boot-devtools` | Live reload in development | 3.4.1 |
| `spring-cloud-starter-config` | Centralised external configuration | 4.2.0 |
| `spring-cloud-starter-vault-config` | HashiCorp Vault secrets | 4.2.0 |
| `spring-cloud-starter-netflix-eureka-client` | Service discovery registration | 4.2.0 |
| `spring-cloud-starter-openfeign` | Declarative HTTP client | 4.2.0 |
| `spring-cloud-starter-bootstrap` | Bootstrap context for Config / Vault | 4.2.0 |
| `spring-cloud-starter-contract-verifier` | Consumer-driven contracts (test) | 4.2.0 |
| `spring-cloud-starter-contract-stub-runner` | WireMock stubs for tests | 4.2.0 |

---

## 3. Security & JWT

| Library | Purpose | Version |
|---------|---------|---------|
| Spring Security OAuth2 Resource Server | Keycloak JWT validation via JWK URI | 3.4.1 |
| Nimbus JOSE + JWT (`NimbusJwtDecoder`) | JWT decode and verification | (transitive) |
| JJWT API (`io.jsonwebtoken:jjwt-api`) | App-specific session JWT generation | 0.12.3 |
| JJWT Impl (`io.jsonwebtoken:jjwt-impl`) | JJWT runtime implementation | 0.12.3 |
| JJWT Jackson (`io.jsonwebtoken:jjwt-jackson`) | JJWT Jackson serialiser | 0.12.3 |
| SSL/TLS | TLSv1.3 enforced; keystore.jks bundled | — |

---

## 4. Persistence & Database

| Component | Technology | Version |
|-----------|-----------|---------|
| ORM | Spring Data JPA + Hibernate | (Spring Boot managed) |
| Production DB | PostgreSQL | 42.7.4 (JDBC driver) |
| Test DB | H2 (in-memory) | 2.2.224 |
| Entity definitions | `prx-persistence` (private Maven lib) | 0.0.3 |
| Repository definitions | `prx-persistence` (private Maven lib) | 0.0.3 |

> Entities and repositories are **not** in this repository. They are consumed from the private `prx-persistence` artifact resolved from Repsy.

---

## 5. Mapping & Serialisation

| Library | Purpose | Version |
|---------|---------|---------|
| MapStruct (`mapstruct`) | Compile-time bean mapping | 1.5.5.Final |
| MapStruct Processor (`mapstruct-processor`) | Annotation processor for mapper generation | 1.5.5.Final |
| Jackson Databind | JSON serialisation | (Spring Boot managed) |
| `jackson-datatype-jsr310` | Java 8 Date/Time serialisation | 2.18.1 |
| `org.json` | Lightweight JSON parsing | 20240303 |
| Google Gson | JSON conversion utility | 2.10.1 |
| SnakeYAML | YAML parsing | 2.3 |

---

## 6. Document / Report Processing

| Library | Purpose | Version |
|---------|---------|---------|
| Apache POI OOXML (`poi-ooxml`) | Word `.docx` template processing | 5.2.5 |
| `commons-fileupload` | Multipart file upload handling | 1.5 |

---

## 7. Utilities & Cross-Cutting

| Library | Purpose | Version |
|---------|---------|---------|
| `prx-commons` | Domain POJOs, shared utilities | 0.0.4 |
| `commons-services` | MapperAppConfig, shared service interfaces | 0.0.1 |
| `commons-lang` | Apache Commons Lang 2 utilities | 2.6 |
| `com.fasterxml.woodstox:woodstox-core` | XML/StAX processing | 7.0.0 |
| `com.thoughtworks.xstream:xstream` | XML serialisation | 1.4.21 |
| `plexus-utils` | Codehaus build utilities | 4.0.1 |

---

## 8. Testing Stack

| Library | Purpose | Version |
|---------|---------|---------|
| JUnit Jupiter (`junit-jupiter`) | Unit test framework | 5.10.5 |
| JUnit Jupiter API | JUnit 5 API | 5.10.5 |
| JUnit Jupiter Engine | JUnit 5 execution engine | 5.10.5 |
| JUnit Vintage Engine | JUnit 4 backward compat | 5.10.5 |
| Mockito Core | Mocking framework | 5.14.2 |
| Mockito JUnit Jupiter | Mockito-JUnit 5 integration | 5.14.2 |
| MockServer JUnit Jupiter | HTTP mock server for integration tests | 5.15.0 |
| Spring Boot Test | Spring context for tests | 3.4.1 |
| Spring Boot Test AutoConfigure | Test slice auto-configuration | 3.4.1 |
| H2 Database | In-memory DB for test isolation | 2.2.224 |

---

## 9. Build Plugins

| Plugin | Purpose | Version |
|--------|---------|---------|
| `spring-boot-maven-plugin` | Executable fat-JAR packaging | 3.4.1 |
| `maven-surefire-plugin` | Unit test execution | 3.2.5 |
| `maven-pmd-plugin` | Static code analysis (PMD + CPD); **fails build on violation** | 3.23.0 |
| `jacoco-maven-plugin` | Code coverage instrumentation & reporting | 0.8.12 |
| `maven-javadoc-plugin` | Javadoc generation | 3.6.3 |
| `springdoc-openapi-maven-plugin` | OpenAPI spec generation (integration-test phase) | 1.4 |

---

## 10. Code Quality

| Tool | Gate | Notes |
|------|------|-------|
| PMD | ❌ Build fails on any violation | Custom `ruleset.xml` at project root |
| JaCoCo | ✅ Report-only (minimum = 0) | Coverage report generated at `target/site/jacoco` |
| SonarCloud | 🔄 CI-triggered | Organisation: `prx-open`, project key: `prx-open_backbone-rest` |
| JetBrains Qodana | 🔄 CI-triggered | JVM Community edition in GitLab pipeline |

---

## 11. Artifact Repository

| Repository | URL | Usage |
|------------|-----|-------|
| Maven Central | `https://repo.maven.apache.org/maven2/` | Public dependencies |
| PRX Repsy | `https://repo.repsy.io/mvn/lmata/prx` | Private PRX dependencies (`prx-commons`, `prx-persistence`, `commons-services`) |

> Credentials: `REPSY_ACCOUNT_USER` and `REPSY_ACCOUNT_PASSWORD` environment variables must be set before any Maven operation.

---

## 12. OpenAPI Specification

The service ships a bundled OpenAPI 3 specification:

```
src/main/resources/META-INF/backbone_rest-openapi.yaml
```

It is also auto-generated via `springdoc-openapi-maven-plugin` during the `integration-test` phase and accessible at runtime at `/v3/api-docs` and `/swagger-ui/**` (these paths are **permit-all** in the security config).

