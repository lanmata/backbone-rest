# 📚 backbone-rest — Technical Documentation

> **PRX Backbone REST** · Backoffice Management Service · v0.0.2
>
> *Java 21 · Spring Boot 3.5.8 · OAuth2 / Keycloak · PostgreSQL · AWS S3 / Cloudflare R2*

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture Overview](#3-architecture-overview)
4. [Domain Module Layout](#4-domain-module-layout)
5. [API Reference](#5-api-reference)
6. [Security Architecture](#6-security-architecture)
7. [Data Flow](#7-data-flow)
8. [Configuration & Environment Variables](#8-configuration--environment-variables)
9. [External Integrations](#9-external-integrations)
10. [Build & Quality Gates](#10-build--quality-gates)
11. [Containerization](#11-containerization)
12. [Testing Strategy](#12-testing-strategy)
13. [Key Source Files](#13-key-source-files)

---

## 1. Project Overview

`backbone-rest` is the central **backoffice REST service** of the PRX platform. It manages core system entities — users, roles, features, people, contacts, applications, and profile images — exposing a versioned API surface under `/api/v1/*`.

| Property | Value |
|---|---|
| **Name** | PRX Backbone REST |
| **Description** | Management service for system entities |
| **Group ID** | `com.prx.backbone` |
| **Artifact ID** | `backbone-rest` |
| **Version** | `0.0.2` |
| **Package Root** | `com.prx.backoffice` |
| **API Base Path** | `/api/v1/*` |
| **Bootstrap Class** | `PrxBackofficeRestApplication` |

---

## 2. Technology Stack

### Runtime

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.8 |
| Web | Spring MVC + Jersey | Spring Boot managed |
| Security | Spring Security + OAuth2 Resource Server | Spring Boot managed |
| Persistence | Spring Data JPA / Hibernate | Spring Boot managed |
| Object Mapping | MapStruct | 1.6.3 |
| JWT (Session) | JJWT (io.jsonwebtoken) | 0.12.3 |
| OpenAPI Docs | springdoc-openapi-starter-webmvc-ui | 2.6.0 |
| Cloud Config | Spring Cloud Config + Bootstrap | 2025.0.1 |
| Secret Management | Spring Cloud Vault | Spring Cloud managed |
| Service Discovery | Netflix Eureka Client | Spring Cloud managed |
| Object Storage | AWS SDK v2 (S3 API) | 2.21.0 |
| JSON | Jackson + Gson + org.json | Spring Boot managed |
| DB Driver | PostgreSQL JDBC | 42.7.7 |

### Build & Quality

| Tool | Version | Purpose |
|---|---|---|
| Maven | System | Build tool |
| maven-compiler-plugin | 3.14.1 | Java 21 compilation + MapStruct AP |
| maven-surefire-plugin | 3.5.2 | Test runner |
| maven-pmd-plugin | 3.28.0 | Static analysis (fails build on violations) |
| jacoco-maven-plugin | 0.8.14 | Code coverage gate |
| springdoc-openapi-maven-plugin | 1.4 | OpenAPI spec generation at integration-test phase |
| SonarCloud | — | External quality analysis |

---

## 3. Architecture Overview

### High-Level Architecture

```mermaid
graph TB
    subgraph Clients["Client Layer"]
        C1[Web App]
        C2[Mobile App]
        C3[Other Services]
    end

    subgraph Gateway["Infrastructure"]
        LB[Load Balancer / API Gateway]
        KC[Keycloak IAM]
        VAULT[HashiCorp Vault]
        CNFS[Spring Cloud Config Server]
    end

    subgraph App["backbone-rest (Spring Boot 3.5)"]
        SC[SecurityFilterChain<br/>OAuth2 + JWT]
        API[REST Controllers<br/>/api/v1/*]
        SVC[Service Layer]
        MAP[MapStruct Mappers]
        subgraph Domains["Domain Modules"]
            USR[users]
            SES[session]
            ROL[roles]
            FTR[features]
            PRS[people]
            APP[application]
            CON[contacts]
            CTY[contacttypes]
            PIM[profileimage]
            RPT[report]
        end
    end

    subgraph External["External Modules (com.prx)"]
        PERS[com.prx:persistence<br/>JPA Entities + Repositories]
        CMNS[com.prx:commons-services<br/>Shared Services]
        CMNO[com.prx:prx-commons<br/>POJOs + Utilities]
    end

    subgraph Storage["Storage"]
        PG[(PostgreSQL)]
        S3[Cloudflare R2 / AWS S3<br/>Profile Images]
    end

    Clients --> LB
    LB --> SC
    SC -- "validates Bearer JWT" --> KC
    KC -. "jwk-set-uri" .-> SC
    VAULT -. "secrets at startup" .-> App
    CNFS -. "app config" .-> App
    SC --> API
    API --> SVC
    SVC --> MAP
    MAP --> PERS
    PERS --> PG
    SVC --> S3
    Domains --- APP
```

### Layered Request Flow

```mermaid
sequenceDiagram
    participant Client
    participant SecurityFilter as SecurityFilterChain
    participant Controller as *Controller
    participant Service as *ServiceImpl
    participant Mapper as *Mapper (MapStruct)
    participant Persistence as com.prx:persistence

    Client->>SecurityFilter: HTTP Request + Bearer Token
    SecurityFilter->>SecurityFilter: validate JWT via Keycloak JWK
    SecurityFilter->>Controller: Authenticated request
    Controller->>Service: delegate (no business logic in controller)
    Service->>Mapper: map Request DTO → Entity
    Mapper->>Persistence: JPA Repository call
    Persistence-->>Mapper: Entity result
    Mapper-->>Service: map Entity → Response DTO
    Service-->>Controller: ResponseEntity<DTO>
    Controller-->>Client: HTTP Response
```

---

## 4. Domain Module Layout

Each business domain is organized in an identical three-layer structure:

```
src/main/java/com/prx/backoffice/
├── v1/
│   ├── <domain>/
│   │   ├── api/
│   │   │   ├── controller/
│   │   │   │   ├── <Domain>Api.java        ← Interface: endpoint mappings + OpenAPI annotations
│   │   │   │   └── <Domain>Controller.java ← Implementation: delegates to service
│   │   │   └── to/                         ← Request / Response Transfer Objects
│   │   ├── mapper/
│   │   │   └── <Domain>Mapper.java         ← MapStruct interface
│   │   └── service/
│   │       ├── <Domain>Service.java        ← Service interface
│   │       └── <Domain>ServiceImpl.java    ← Implementation (returns ResponseEntity<?>)
```

### Interface-First Controller Pattern

```mermaid
classDiagram
    class UserApi {
        <<interface>>
        +getService() UserService
        +create(UserCreateRequest) ResponseEntity~UserCreateResponse~
        +findUserById(UUID) ResponseEntity~UserTO~
        +findAll(UUID) ResponseEntity~List~UserTO~~
        +update(UUID, UserTO) ResponseEntity~UserTO~
        +link(UUID, UUID) ResponseEntity~UserTO~
        +unlink(UUID, UUID) ResponseEntity~UserTO~
        +putUserDetail(UUID, PutUserUpdateRequest) ResponseEntity~Void~
        +deleteUserByApplicationAndUserId(UUID, UUID) ResponseEntity~Void~
    }

    class UserController {
        -UserService userService
        +UserController(UserService)
        +getService() UserService
    }

    class UserService {
        <<interface>>
        +create(UserCreateRequest) ResponseEntity~UserCreateResponse~
        +findUserById(UUID) ResponseEntity~UserTO~
        +findAll(UUID) ResponseEntity~List~UserTO~~
    }

    class UserServiceImpl {
        -UserRepository userRepository
        -UserMapper userMapper
    }

    UserApi <|.. UserController
    UserController --> UserService
    UserService <|.. UserServiceImpl
```

> **Rule**: `*Api` carries `@RequestMapping` + OpenAPI annotations. `*Controller` only overrides and delegates.

---

## 5. API Reference

### Endpoint Map

```mermaid
graph LR
    subgraph "/api/v1/users"
        U1["GET /check/alias/{alias}/application/{applicationId}"]
        U2["GET /check/email/{email}/application/{applicationId}"]
        U3["POST /"]
        U4["GET /user/{userId}"]
        U5["GET /application/{applicationId}"]
        U6["GET /userByAlias/{alias}/application/{applicationId}"]
        U7["GET /alias/{alias}/application/{applicationId}"]
        U8["PUT /{userId}/full-detail"]
        U9["PUT /unlink/user/{userId}/role/{roleId}"]
        U10["PUT /link/user/{userId}/role/{roleId}"]
        U11["PUT /{userId}"]
        U12["DELETE /application/{applicationId}/user/{userId}"]
    end

    subgraph "/api/v1/sessions"
        S1["POST /"]
        S2["POST /token"]
        S3["GET /validate"]
        S4["GET /renew"]
    end

    subgraph "/api/v1/roles"
        R1["GET /find/{roleId}"]
        R2["GET /{includeInactive}/{roleIds}"]
        R3["GET /{includeInactive}"]
        R4["GET /"]
        R5["POST /"]
        R6["PUT /{roleId}"]
        R7["GET /user/{userId}"]
    end

    subgraph "/api/v1/features"
        F1["GET /find/{featureId}"]
        F2["GET /{includeInactive}"]
        F3["GET /{includeInactive}/{featuresIds}"]
        F4["POST /"]
        F5["PUT /{featureId}"]
    end

    subgraph "/api/v1/people"
        P1["POST /"]
        P2["GET /{personId}"]
        P3["PUT /{personId}"]
        P4["GET /"]
    end

    subgraph "/api/v1/application"
        A1["POST /"]
    end

    subgraph "/api/v1/contacts"
        C1["POST /"]
        C2["PUT /{contactId}"]
        C3["GET /{contactId}"]
        C4["GET /list/{contactIds}"]
        C5["GET /person/{personId}"]
        C6["GET /list-all"]
        C7["DELETE /{contactId}"]
    end

    subgraph "/api/v1/contacttypes"
        CT1["POST /"]
        CT2["GET /{contactTypeId}"]
        CT3["PUT /{contactTypeId}"]
        CT4["GET /list/{contactTypeIds}"]
        CT5["GET /list-all"]
        CT6["DELETE /{contactTypeId}"]
    end

    subgraph "/api/v1/profileimage"
        PI1["POST /application/{applicationId}"]
        PI2["GET /"]
        PI3["GET /application/{applicationId}/reference"]
    end

    subgraph "/api/v1/report"
        REP1["GET /template"]
        REP2["GET /placeholdervalues"]
    end
```

### Detailed Endpoint Table

#### Users (`/api/v1/users`)

| Method | Path | Description | Response |
|---|---|---|---|
| `GET` | `/check/alias/{alias}/application/{applicationId}` | Checks if user alias is available | `204` / `404` |
| `GET` | `/check/email/{email}/application/{applicationId}` | Checks if user email is available | `204` / `404` |
| `POST` | `/` | Creates a new user | `201 UserCreateResponse` |
| `GET` | `/user/{userId}` | Finds user by UUID | `200 UserTO` |
| `GET` | `/application/{applicationId}` | Lists all users for an application | `200 List<UserTO>` |
| `GET` | `/userByAlias/{alias}/application/{applicationId}` | Finds user by alias | `200 UserTO` |
| `GET` | `/alias/{alias}/application/{applicationId}` | Finds user alias by alias | `200 UserAliasTO` |
| `PUT` | `/{userId}/full-detail` | Full user update | `200 UserTO` |
| `PUT` | `/unlink/user/{userId}/role/{roleId}` | Unlinks role from user | `200 UserTO` |
| `PUT` | `/link/user/{userId}/role/{roleId}` | Links role to user | `200 UserTO` |
| `PUT` | `/{userId}` | Partial user detail update | `202` / `406` |
| `DELETE` | `/application/{applicationId}/user/{userId}` | Deletes user | `204` / `404` |

#### Sessions (`/api/v1/sessions`)

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/` | Generate session token by alias/password | `200 SessionResponse` |
| `POST` | `/token` | Generate session token by email/password | `200 SessionResponse` |
| `GET` | `/validate` | Validate session token (header: `session-token`) | `200 Boolean` |
| `GET` | `/renew` | Renew session token (header: `session-token`) | `200 SessionResponse` |

#### Roles (`/api/v1/roles`)

| Method | Path | Description | Response |
|---|---|---|---|
| `GET` | `/find/{roleId}` | Find role by ID | `200 Role` |
| `GET` | `/{includeInactive}/{roleIds}` | List roles by status + IDs | `200 List<Role>` |
| `GET` | `/{includeInactive}` | List roles by status | `200 List<Role>` |
| `GET` | `/` | List all roles | `200 List<Role>` |
| `POST` | `/` | Create role | `200 Role` |
| `PUT` | `/{roleId}` | Update role | `200 Role` |
| `GET` | `/user/{userId}` | List roles by user | `200 List<Role>` |

#### Features (`/api/v1/features`)

| Method | Path | Description | Response |
|---|---|---|---|
| `GET` | `/find/{featureId}` | Find feature by ID | `201 Feature` |
| `GET` | `/{includeInactive}` | List features by status | `200 List<Feature>` |
| `GET` | `/{includeInactive}/{featuresIds}` | List features by status + IDs | `200 List<Feature>` |
| `POST` | `/` | Create feature | `201 Feature` |
| `PUT` | `/{featureId}` | Update feature | `202 Feature` |

#### People (`/api/v1/people`)

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/` | Create person | `200 Person` |
| `GET` | `/{personId}` | Find person by ID | `200 Person` |
| `PUT` | `/{personId}` | Update person | `200 Person` |
| `GET` | `/` | List all people | `200 List<Person>` |

#### Application (`/api/v1/application`)

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/` | Create application | `201 Application` |

#### Contacts (`/api/v1/contacts`)

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/` | Create contact | `200 Contact` |
| `PUT` | `/{contactId}` | Update contact | `200 Contact` |
| `GET` | `/{contactId}` | Find contact by ID | `200 Contact` |
| `GET` | `/list/{contactIds}` | List contacts by IDs | `200 List<Contact>` |
| `GET` | `/person/{personId}` | List contacts by person | `200 List<Contact>` |
| `GET` | `/list-all` | List all contacts | `501` (not implemented) |
| `DELETE` | `/{contactId}` | Delete contact | `200 String` |

#### Contact Types (`/api/v1/contacttypes`)

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/` | Create contact type | `200 ContactType` |
| `GET` | `/{contactTypeId}` | Find contact type by ID | `200 ContactType` |
| `PUT` | `/{contactTypeId}` | Update contact type | `200` / `404` / `400` |
| `GET` | `/list/{contactTypeIds}` | List contact types by IDs | `200 List<ContactType>` |
| `GET` | `/list-all` | List all contact types | `200 List<ContactType>` |
| `DELETE` | `/{contactTypeId}` | Delete contact type | `200 ContactType` |

#### Profile Image (`/api/v1/profileimage`)

| Method | Path | Description | Response |
|---|---|---|---|
| `POST` | `/application/{applicationId}` | Upload profile image | `201 PostProfileImageResponse` |
| `GET` | `/` | Get profile image bytes | `200 byte[]` |
| `GET` | `/application/{applicationId}/reference` | Get profile image reference | `200 GetProfileImageReferenceResponse` |

#### Report (`/api/v1/report`)

| Method | Path | Description | Response |
|---|---|---|---|
| `GET` | `/template` | Generate Word document from template | `200 Resource (octet-stream)` |
| `GET` | `/placeholdervalues` | Extract placeholder values from template | `200 List<String>` |

---

## 6. Security Architecture

### Overview

```mermaid
flowchart TB
    subgraph Request["Inbound Request"]
        R1[HTTP Request]
        R2[Bearer JWT Token]
    end

    subgraph Security["Spring Security Layer"]
        SCF[SecurityFilterChain]
        JD[JwtDecoder\nNimbusJwtDecoder]
        JC[JwtConverter\nExtracts Keycloak Roles → GrantedAuthorities]
        AUTH{hasAnyRole\nclientRoles?}
    end

    subgraph Keycloak["Keycloak IAM"]
        JWK[JWK Set Endpoint\n/auth/realms/../certs]
    end

    subgraph SessionFlow["Session Token Flow (separate)"]
        SJW[SessionJwtService\nApp-specific JWT]
        HDR["Header: session-token"]
    end

    R1 --> SCF
    R2 --> SCF
    SCF --> JD
    JD -. "fetch public keys" .-> JWK
    JD --> JC
    JC --> AUTH
    AUTH -- "PERMIT" --> Controller
    AUTH -- "DENY 403" --> Client

    Controller -- "session endpoints" --> SJW
    SJW --> HDR
```

### Security Configuration (`SecurityConfig.java`)

| Rule | Detail |
|---|---|
| **Session policy** | `STATELESS` — no server-side sessions |
| **Swagger endpoints** | `PERMIT_ALL` (`/swagger-ui/**`, `/v3/api-docs/**`) |
| **GET /v1/\*\*** | Requires role membership in `app.clientRoles` (default: `backbone-rest-client`) |
| **All other requests** | `authenticated()` |
| **OAuth2 resource server** | `NimbusJwtDecoder` — validates against Keycloak JWK Set URI |
| **SSL** | TLS 1.3 enforced; JKS keystore + truststore loaded at startup |

### JWT Converter (`JwtConverter.java`)

Converts Keycloak JWTs → Spring Security `AbstractAuthenticationToken`. Extracts realm roles and client-specific roles from the `realm_access` and `resource_access` claims and maps them to `GrantedAuthority` objects with the `ROLE_` prefix.

### Session JWT Flow (`SessionJwtService` / `SessionServiceImpl`)

Separate from OAuth2 validation. Used by session endpoints to mint and validate short-lived application-specific JWTs.

| Constant | Value |
|---|---|
| `SESSION_TOKEN_KEY` | `"session-token"` |
| Token claim `"type"` | `"session-token"` |

```mermaid
sequenceDiagram
    participant Client
    participant SessionController
    participant SessionServiceImpl
    participant SessionJwtService
    participant KeycloakAuth

    Client->>SessionController: POST /api/v1/sessions {alias, password}
    SessionController->>SessionServiceImpl: loadSession(SessionRequest)
    SessionServiceImpl->>KeycloakAuth: authenticate credentials
    KeycloakAuth-->>SessionServiceImpl: Keycloak token
    SessionServiceImpl->>SessionJwtService: generateSessionToken(username, params)
    SessionJwtService-->>SessionServiceImpl: signed JWT
    SessionServiceImpl-->>SessionController: ResponseEntity<SessionResponse>
    SessionController-->>Client: 200 {token, ...}
```

---

## 7. Data Flow

### Typical CRUD Request

```mermaid
flowchart LR
    HTTP[HTTP Request] --> SC[SecurityFilter]
    SC --> CTRL[*Controller\nSpring @RestController]
    CTRL --> SVC[*ServiceImpl]
    SVC --> MAP[MapStruct *Mapper\nRequest DTO → Entity]
    MAP --> REPO[JPA Repository\ncom.prx:persistence]
    REPO --> DB[(PostgreSQL)]
    DB --> REPO
    REPO --> MAP2[MapStruct *Mapper\nEntity → Response DTO]
    MAP2 --> SVC2[ResponseEntity builder]
    SVC2 --> CTRL2[ResponseEntity<?>]
    CTRL2 --> HTTP2[HTTP Response]
```

### MapStruct Mappers

| Domain | Mapper | Key Mappings |
|---|---|---|
| `users` | `UserMapper` | `UserEntity ↔ UserTO`, `UserCreateRequest → UserEntity` |
| `users` | `ApplicationRoleUserMapper` | `ApplicationRoleUser ↔ DTO` |
| `session` | `UserAliasMapper` | `UserAlias ↔ UserAliasTO` |
| `roles` | `RoleMapper` | `RoleEntity ↔ Role (POJO)` |
| `features` | `FeatureMapper` | `FeatureEntity ↔ Feature`, custom expressions via `FeatureMapperUtil` |
| `people` | `PersonMapper` | `PersonEntity ↔ Person (POJO)` |
| `application` | `ApplicationMapper` | `ApplicationEntity ↔ Application (POJO)` |
| `contacts` | `ContactMapper` | `ContactEntity ↔ Contact (POJO)` |
| `contacttypes` | `ContactTypeMapper` | `ContactTypeEntity ↔ ContactType (POJO)` |

---

## 8. Configuration & Environment Variables

### `bootstrap.yml` Structure

```mermaid
mindmap
  root((bootstrap.yml))
    server
      port APP_PORT
      ssl TLS 1.3 JKS bundle
    spring
      security
        oauth2
          resourceserver JWT issuer-uri AUTH_SERVER_URI
          client Keycloak registration
      cloud
        config Spring Cloud Config Server CNFS_URI
        vault HashiCorp Vault VAULT_SERVER_URI
        bootstrap SPRING_BOOT_CLOUD_BOOTSTRAP_ENABLED
      jpa
        hibernate properties
      application name backbone-rest
    app
      clientRoles backbone-rest-client
      api endpoint /v1 excludes /sessions/token /sessions/validate
    prx
      jwt secret APP_TOKEN_SECRET expirationMs APP_TOKEN_EXPIRATION
```

### Required Environment Variables

| Variable | Purpose | Example / Default |
|---|---|---|
| `APP_PORT` | Server port | `8082` |
| `APP_TOKEN_SECRET` | Session JWT signing secret | *(secret)* |
| `APP_TOKEN_EXPIRATION` | Session JWT expiry (ms) | `3600000` |
| `AUTH_SERVER_URI` | Keycloak realm issuer URI | `https://<keycloak>/realms/<realm>` |
| `AUTH_CERT_URI` | Keycloak JWK cert path | `/protocol/openid-connect/certs` |
| `AUTH_CLIENT_ID` | Keycloak client ID | `backbone-rest` |
| `AUTH_CLIENT_SECRET` | Keycloak client secret | *(secret)* |
| `SSL_KEYSTORE_LOCATION` | JKS keystore classpath location | `keystore.jks` |
| `SSL_KEYSTORE_PASSWORD` | Keystore password | *(secret)* |
| `SSL_KEYSTORE_TYPE` | Keystore type | `JKS` |
| `SSL_TRUSTSTORE_LOCATION` | Truststore classpath location | `keystore.jks` |
| `SSL_TRUSTSTORE_PASSWORD` | Truststore password | *(secret)* |
| `SSL_TRUSTSTORE_TYPE` | Truststore type | `JKS` |
| `VAULT_ENABLED` | Enable HashiCorp Vault | `true` |
| `VAULT_TOKEN` | Vault access token | *(secret)* |
| `VAULT_SERVER_URI` | Vault server URI | `http://prx-qa.vault.tst:8200` |
| `CNFS_URI` | Config Server URI | `https://prx-qa.config-server.tst` |
| `CNFS_PORT` | Config Server port | `443` |
| `SPRING_BOOT_PROFILE_ACTIVE` | Active Spring profile | `remote-supabase` |
| `SPRING_CLOUD_CONFIG_LABEL` | Config label/branch | `Develop` |
| `SPRING_BOOT_CLOUD_BOOTSTRAP_ENABLED` | Enable cloud bootstrap | `true` |
| `LOGGING_TRACE_ENABLED` | Enable trace logging | `true` |
| `REPSY_ACCOUNT_USER` | Repsy registry username | *(secret)* |
| `REPSY_ACCOUNT_PASSWORD` | Repsy registry password | *(secret)* |

---

## 9. External Integrations

### PRX Private Modules (Repsy Registry)

```mermaid
graph TB
    backbone[backbone-rest] --> |uses| persistence[com.prx:persistence v0.0.3\nJPA Entities + Spring Data Repositories]
    backbone --> |uses| commons[com.prx:prx-commons v0.0.1\nShared POJOs + HttpStatusUtil]
    backbone --> |uses| cmsvc[com.prx:commons-services v0.0.1\nShared Services]
    backbone --> |uses| secoauth[com.prx:security-oauth v0.0.1\nOAuth2 Utilities]
    
    registry[Repsy Private Registry\nhttps://repo.repsy.io/mvn/lmata/prx] -.->|hosts| persistence
    registry -.->|hosts| commons
    registry -.->|hosts| cmsvc
    registry -.->|hosts| secoauth
```

> **Module boundary rule**: `backbone-rest` never creates or modifies JPA entity classes. All entities live in `com.prx:persistence`.

### Cloudflare R2 / AWS S3 (Profile Images)

```mermaid
sequenceDiagram
    participant Client
    participant ProfileImageController
    participant ProfileImageServiceImpl
    participant CloudflareR2StorageClient
    participant S3[Cloudflare R2 / AWS S3]

    Client->>ProfileImageController: POST /api/v1/profileimage/application/{id}\nbody: multipart image
    ProfileImageController->>ProfileImageServiceImpl: uploadProfileImage(token, appId, image)
    ProfileImageServiceImpl->>CloudflareR2StorageClient: upload(image)
    CloudflareR2StorageClient->>S3: PutObjectRequest
    S3-->>CloudflareR2StorageClient: ETag / URL
    CloudflareR2StorageClient-->>ProfileImageServiceImpl: reference URL
    ProfileImageServiceImpl-->>ProfileImageController: ResponseEntity<PostProfileImageResponse>
    ProfileImageController-->>Client: 201 {reference}
```

### HashiCorp Vault

Runtime secrets (database passwords, JWT secrets, OAuth credentials) are sourced from Vault at startup via Spring Cloud Vault. The KV backend is `PRX`, with a default context matching `app.name` (`backbone-rest`).

### Spring Cloud Config Server

Non-secret application configuration is served by the Spring Cloud Config Server at `${CNFS_URI}`. Profile and label filtering allows environment-specific configs (`SPRING_BOOT_PROFILE_ACTIVE`, `SPRING_CLOUD_CONFIG_LABEL`).

### Netflix Eureka

The application registers itself with Eureka for service discovery within the PRX platform.

---

## 10. Build & Quality Gates

### Build Lifecycle

```mermaid
flowchart LR
    A[initialize\nJaCoCo prepare-agent] --> B[compile\nmapstruct annotation processing]
    B --> C[process-classes\nfix spring-configuration-metadata]
    C --> D[test\nSurefire unit tests]
    D --> E[test\nPMD check + CPD check]
    E --> F[test\nJaCoCo report + check]
    F --> G[integration-test\nspringdoc OpenAPI spec generation]
    G --> H[package\nSpring Boot fat JAR]
```

### Quick Commands

```bash
# Fast compile check (MapStruct AP included)
mvn -DskipTests compile

# Full gate: unit tests + PMD + JaCoCo
mvn test

# Focused test class
mvn -Dtest=UserServiceImplTest test

# Package fat JAR → target/backbone-rest.jar
mvn -DskipTests package
```

### PMD Configuration (`ruleset.xml`)

- Bound to `test` phase; build **fails** on any violation (`failOnViolation=true`, `failurePriority=5`)
- CPD (copy-paste detection) also checked via `cpd-check` goal
- Excluded from checks: `*Application.*`, `exceptions`, `util`, `config`, `controller`, `mapper`, `security`, `producer`, `interceptor` packages

### JaCoCo Coverage Gates

| Element | Counter | Metric | Minimum |
|---|---|---|---|
| `PACKAGE` | `LINE` | `COVEREDRATIO` | `0` (currently permissive; threshold configurable) |
| `PACKAGE` | `BRANCH` | `COVEREDRATIO` | `0` (currently permissive) |

> Coverage reports are generated under `target/site/jacoco/`. SonarCloud integration reads from `site/jacoco/jacoco.xml`.

### Repsy Private Registry

```xml
<!-- settings.xml / ci_settings.xml -->
<server>
  <id>PRX-Repsy</id>
  <username>${REPSY_ACCOUNT_USER}</username>
  <password>${REPSY_ACCOUNT_PASSWORD}</password>
</server>
```

---

## 11. Containerization

### Dockerfile Summary

```mermaid
graph TB
    BASE[amazoncorretto:21-alpine3.20] --> COPY[Copy JAR + JKS + CRT files]
    COPY --> USER[Create non-root user: jvapps:appmng]
    USER --> CERTS[Import TLS certificates\ninto JVM cacerts via keytool]
    CERTS --> DIRS[Create /opt/images directories]
    DIRS --> EXPOSE[EXPOSE 8082]
    EXPOSE --> CMD[java -jar backbone-rest.jar]
```

| Property | Value |
|---|---|
| **Base image** | `amazoncorretto:21-alpine3.20` |
| **Exposed port** | `8082` |
| **Runtime user** | `jvapps` (group: `appmng`) — non-root |
| **Working directory** | `/usr/local/runme` |
| **JAR name** | `backbone-rest.jar` |
| **Certificates imported** | Auth server, Config server, Service monitor, Wildcard |
| **Image directory** | `/opt/images` (for profile images) |

### Running the Container

```bash
docker run -d \
  -p 8082:8082 \
  -e APP_PORT=8082 \
  -e AUTH_SERVER_URI=https://... \
  -e VAULT_ENABLED=true \
  -e VAULT_TOKEN=... \
  # ... (all required env vars)
  backbone-rest:latest
```

---

## 12. Testing Strategy

### Test Structure

```
src/test/java/com/prx/backoffice/
├── MockLoaderBase.java                   ← Shared mock loader base class
├── PrxBackofficeRestApplicationTest.java ← Spring Boot smoke test
├── config/
│   └── SecurityKeycloakTestConfig.java   ← Security test configuration
├── util/                                 ← Unit tests for utilities
├── v1/
│   ├── <domain>/
│   │   ├── api/to/                       ← DTO unit tests
│   │   ├── service/
│   │   │   ├── <Domain>ServiceTest.java        ← Interface contract tests
│   │   │   └── <Domain>ServiceImplTest.java    ← Implementation unit tests (Mockito)
│   │   └── api/controller/
│   │       └── <Domain>ApiTest.java            ← Controller slice tests
│   └── util/
│       ├── FeatureTemplateTest.java      ← Test data builders
│       ├── PersonTemplateTest.java
│       └── RoleTemplateTest.java
```

### Frameworks & Tools

| Tool | Purpose |
|---|---|
| **JUnit 5.14.1** | Test lifecycle, assertions |
| **Mockito 5.21.0** | Mocking service/repository dependencies |
| **Spring `@WebMvcTest`** | Controller slice tests (security filter included) |
| **`SecurityKeycloakTestConfig`** | Injects test-safe security beans |
| **JaCoCo** | Coverage instrumentation and reporting |

### Test Coverage Domains

| Domain | Service Test | Impl Test | Controller Test | DTO Test |
|---|---|---|---|---|
| users | ✅ | ✅ | ✅ | ✅ |
| session | ✅ | ✅ | — | ✅ |
| roles | ✅ | ✅ | — | ✅ |
| features | ✅ | ✅ | — | ✅ |
| people | ✅ | ✅ | — | ✅ |
| application | ✅ | ✅ | ✅ | — |
| contacts | ✅ | ✅ | — | ✅ |
| contacttypes | ✅ | ✅ | — | — |
| profileimage | — | ✅ | — | — |
| report | — | — | — | — |

---

## 13. Key Source Files

### Production Sources

| File | Path | Purpose |
|---|---|---|
| `PrxBackofficeRestApplication` | `com/prx/backoffice/` | Spring Boot entry point — component scan for `com.prx` |
| `SecurityConfig` | `security/config/` | Security filter chain, JWT decoder, RestTemplate with SSL |
| `JwtConverter` | `security/jwt/` | Keycloak JWT → Spring `GrantedAuthority` conversion |
| `SessionJwtService` | `v1/session/services/` | App-specific JWT interface; `SESSION_TOKEN_KEY` constant |
| `SessionServiceImpl` | `v1/session/services/` | Credential validation + session token minting |
| `UserApi` | `v1/users/api/controller/` | All user endpoint definitions + OpenAPI annotations |
| `UserController` | `v1/users/api/controller/` | User endpoint implementation; delegates to `UserService` |
| `UserServiceImpl` | `v1/users/service/` | User business logic; returns `ResponseEntity<?>` |
| `UserMapper` | `v1/users/mapper/` | MapStruct mapper: `UserEntity ↔ UserTO` |
| `CloudflareR2StorageClient` | `v1/profileimage/client/` | AWS SDK v2 S3 client for Cloudflare R2 |
| `DataSourceSslConfig` | `config/` | JPA datasource SSL configuration |
| `MessageUtil` | `util/` | User-facing message lookup by key |
| `KeystoreUtil` | `util/` | SSL bundle construction from keystore properties |
| `JwtUtil` | `util/` | JWT utility helpers |
| `LogDefault` | `aop/` | Custom annotation for AOP-based method logging |
| `CloudflareR2Properties` | `property/` | `@ConfigurationProperties` for R2 storage |
| `SecurityProperties` | `property/` | `@ConfigurationProperties` for SSL/security |

### Configuration Files

| File | Path | Purpose |
|---|---|---|
| `bootstrap.yml` | `src/main/resources/` | All runtime configuration (SSL, OAuth, Vault, Config Server, JPA) |
| `default.env` | Root | Default non-secret environment variable values |
| `ruleset.xml` | Root | PMD ruleset — enforced at `test` phase |
| `ci_settings.xml` | Root | Maven `settings.xml` for CI/CD (Repsy credentials via env vars) |
| `pom.xml` | Root | Maven build descriptor, dependency management, plugin config |
| `Dockerfile` | Root | Container image definition (Amazon Corretto 21 Alpine) |
| `backbone_rest-openapi.yaml` | `src/main/resources/META-INF/` | OpenAPI 3.x API contract artifact |

---

## Appendix: Cross-Cutting Concerns

### AOP — Logging

`@LogDefault` annotation can be applied to methods, fields, and parameters to trigger structured logging. Configured with a `LogActionKey` enum and a `MessageType` enum for consistent log message formatting.

### Message Internationalization

`MessageUtil` provides user-facing messages via message keys defined in:
- `UserMessageKey` — user-related messages
- `RoleMessageKey` — role-related messages
- `FeatureMessageKey` — feature-related messages
- `PersonMessageKey` — person-related messages
- `RoleFeatureMessageKey` — role-feature link messages
- `ApplicationMessageKey` — application-related messages
- `BackboneMessage` — generic backbone messages

### CORS

All controllers declare `@CrossOrigin(origins = "*")`. Production traffic should restrict origins at the API Gateway / Load Balancer level.

### OpenAPI / Swagger UI

Available at runtime (non-production) at:
- **Swagger UI**: `https://<host>:<port>/swagger-ui/index.html`
- **API Docs JSON**: `https://<host>:<port>/v3/api-docs`

Both paths are `PERMIT_ALL` in the security filter chain.

---

*© Luis Antonio Mata Mata — PRX Dev Innova. All rights reserved.*

