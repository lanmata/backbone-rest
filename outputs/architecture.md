# System Architecture — backbone-rest

---

## 1. High-Level Architecture

```mermaid
graph TB
    subgraph Clients
        SPA[Web SPA / Angular]
        MOB[Mobile App]
        SVC[Internal Microservices]
    end

    subgraph backbone-rest ["backbone-rest (Port 8082, TLS 1.3)"]
        SEC[SecurityFilterChain]
        API[REST Controllers]
        SRV[Service Layer]
        MAP[MapStruct Mappers]
        REPO[JPA Repositories]
    end

    subgraph External["External Infrastructure"]
        KC[Keycloak IAM]
        VLT[HashiCorp Vault]
        CNFS[Spring Cloud Config Server]
        EUREKA[Netflix Eureka Service Registry]
        PG[PostgreSQL Database]
    end

    SPA -->|HTTPS Bearer JWT| SEC
    MOB -->|HTTPS Bearer JWT| SEC
    SVC -->|HTTPS Bearer JWT| SEC
    SEC -->|Validate JWK| KC
    SEC --> API
    API --> SRV
    SRV --> MAP
    SRV --> REPO
    REPO --> PG
    backbone-rest -->|Secrets| VLT
    backbone-rest -->|Config| CNFS
    backbone-rest -->|Register| EUREKA
```

---

## 2. Layered Architecture

The service follows a strict layered design within each domain module:

```
┌───────────────────────────────────────────────────────────┐
│                   HTTP / REST Layer                        │
│  *Api (interface) + *Controller (implementation)          │
│  • Declares OpenAPI annotations (@Operation, @ApiResponse)│
│  • Validates inputs (@Valid, @NotNull, @NotBlank)          │
│  • Routes to Service layer                                 │
└──────────────────────────┬────────────────────────────────┘
                           │
┌──────────────────────────▼────────────────────────────────┐
│                   Service Layer                            │
│  *Service (interface) + *ServiceImpl (implementation)      │
│  • Owns business logic and validation                      │
│  • Returns ResponseEntity<?> directly                      │
│  • Uses MessageUtil for user-facing messages               │
│  • Annotated with @Service, @Transactional where needed    │
└──────────────────────────┬────────────────────────────────┘
                           │
┌──────────────────────────▼────────────────────────────────┐
│                   Mapper Layer                             │
│  *Mapper (MapStruct interface)                             │
│  • Converts between domain entities and Transfer Objects   │
│  • Uses expression mappings for complex relations          │
│  • Config via MapperAppConfig (from prx-commons-services)  │
└──────────────────────────┬────────────────────────────────┘
                           │
┌──────────────────────────▼────────────────────────────────┐
│                   Persistence Layer                        │
│  JPA Entities + Spring Data Repositories                   │
│  • Entities defined in external module: prx-persistence    │
│  • Repositories scanned via @EnableJpaRepositories         │
│  • Database: PostgreSQL (H2 for tests)                     │
└───────────────────────────────────────────────────────────┘
```

---

## 3. Domain Module Layout

Every business domain follows this canonical package structure:

```
v1/<domain>/
├── api/
│   ├── controller/
│   │   ├── *Api.java         ← Interface: @Tag, @Operation, @ApiResponse, default methods
│   │   └── *Controller.java  ← @RestController: implements *Api, delegates to service
│   └── to/
│       └── *.java            ← Transfer Objects (request/response records or POJOs)
├── mapper/
│   └── *Mapper.java          ← MapStruct @Mapper interface
└── service/
    ├── *Service.java         ← Interface
    └── *ServiceImpl.java     ← @Service implementation
```

### Domain modules present

| Domain | Package | Base Path |
|--------|---------|-----------|
| application | `v1.application` | `/api/v1/applications` |
| contacts | `v1.contacts` | `/api/v1/contacts` |
| contacttypes | `v1.contacttypes` | `/api/v1/contact-types` |
| features | `v1.features` | `/api/v1/features` |
| people | `v1.people` | `/api/v1/people` |
| profileimage | `v1.profileimage` | `/api/v1/profile-images` |
| report | `v1.report` | `/api/v1/report` |
| roles | `v1.roles` | `/api/v1/roles` |
| session | `v1.session` | `/api/v1/sessions` |
| users | `v1.users` | `/api/v1/users` |

---

## 4. External Module Dependencies

The app intentionally delegates entities and repositories to two private PRX libraries resolved from Repsy:

```
┌────────────────────────────────────────────────────────────┐
│  backbone-rest (this app)                                  │
│                                                            │
│  @EntityScan("com.umdc.persistence")        ──────────────► prx-persistence (0.0.3)
│  @EnableJpaRepositories("com.umdc.persistence")             │  UserEntity, PersonEntity,
│                                                            │  RoleEntity, FeatureEntity,
│  @SpringBootApplication(                                   │  ApplicationEntity,
│    scanBasePackages = {                                     │  ApplicationRoleUserEntity,
│      "com.prx.backoffice",                                  │  ContactEntity,
│      "com.umdc.commons.services"  ──────────────────────►   │  ContactTypeEntity, etc.
│    })                                                      │
│                                                prx-commons (0.0.4)
│                                                            │  Domain POJOs (User, Role,
│                                                            │  Person, Contact, Feature, etc.)
│                                                            │
│                                                commons-services (0.0.1)
│                                                            │  MapperAppConfig,
│                                                            │  shared service interfaces
└────────────────────────────────────────────────────────────┘
```

> **Important:** JPA entities and Spring Data repositories are **not** in this repository. They are injected at runtime via the external `prx-persistence` library.

---

## 5. Request Processing Flow

```mermaid
sequenceDiagram
    participant Client
    participant SecurityFilter
    participant Keycloak
    participant Controller
    participant Service
    participant Mapper
    participant Repository
    participant PostgreSQL

    Client->>SecurityFilter: HTTPS Request + Bearer JWT
    SecurityFilter->>Keycloak: Validate JWT (JWK URI)
    Keycloak-->>SecurityFilter: JWT valid (roles extracted)
    SecurityFilter->>Controller: Authenticated request
    Controller->>Controller: @Valid input validation
    Controller->>Service: Delegate business call
    Service->>Repository: Query / persist via JPA
    Repository->>PostgreSQL: SQL query
    PostgreSQL-->>Repository: Result set
    Repository-->>Service: Entity (or Optional)
    Service->>Mapper: toTarget(entity) → DTO
    Mapper-->>Service: DTO
    Service-->>Controller: ResponseEntity<DTO>
    Controller-->>Client: HTTP response (JSON)
```

---

## 6. Session Token Flow

Sessions expose a second, application-specific JWT layer (distinct from Keycloak):

```mermaid
sequenceDiagram
    participant Client
    participant SessionController
    participant SessionServiceImpl
    participant UserRepository
    participant PostgreSQL

    Note over SessionController: POST /api/v1/sessions (public endpoint)
    Client->>SessionController: {alias, password} or {email, password}
    SessionController->>SessionServiceImpl: loadSession(request)
    SessionServiceImpl->>UserRepository: findByAliasAndApplication / findByEmail
    UserRepository->>PostgreSQL: SELECT user WHERE alias=? AND app=?
    PostgreSQL-->>UserRepository: UserEntity
    UserRepository-->>SessionServiceImpl: UserEntity (or empty)
    SessionServiceImpl->>SessionServiceImpl: Validate password
    SessionServiceImpl->>SessionServiceImpl: generateSessionToken(username, params)
    Note over SessionServiceImpl: JJWT signs token with APP_TOKEN_SECRET
    SessionServiceImpl-->>SessionController: ResponseEntity<SessionResponse>
    SessionController-->>Client: {token, user, ...} + session-token header

    Note over Client: Subsequent calls include header: session-token: <jwt>
    Client->>ProfileImageController: GET /api/v1/profile-images/ + session-token header
    ProfileImageController->>SessionJwtService: isValid(token)
    SessionJwtService-->>ProfileImageController: true/false
```

---

## 7. Security Architecture

```mermaid
graph LR
    subgraph "Security Layer"
        JWK["NimbusJwtDecoder\n(JWK URI from Keycloak)"]
        CONV["JwtConverter\n(extract resource_access roles)"]
        CHAIN["SecurityFilterChain\n• Stateless sessions\n• Swagger = permitAll\n• GET /v1/** = hasAnyRole\n• Anything else = authenticated"]
    end

    subgraph "SSL/TLS"
        KS["keystore.jks\n(SSL_KEYSTORE_*)"]
        TS["truststore\n(SSL_TRUSTSTORE_*)"]
        BUNDLE["SSL Bundle\nbackbone-rest-security"]
    end

    JWK --> CONV --> CHAIN
    KS --> BUNDLE
    TS --> BUNDLE
    BUNDLE --> CHAIN
```

---

## 8. Cloud Infrastructure Integration

```mermaid
graph TB
    APP[backbone-rest]

    APP -->|"bootstrap.yml: spring.cloud.config.uri"| CNFS[Spring Cloud Config Server\nExternal YAML config]
    APP -->|"spring.cloud.vault.*"| VAULT[HashiCorp Vault\nSecrets: DB creds, tokens]
    APP -->|"eureka.client.*"| EUREKA[Netflix Eureka\nService discovery]
    APP -->|"oauth2.resourceserver.jwt.jwk-set-uri"| KC[Keycloak\nJWT validation]
    APP -->|"spring.datasource.*"| PG[(PostgreSQL)]

    CNFS -.->|"Provides: datasource, messaging configs"| APP
    VAULT -.->|"Provides: passwords, secrets"| APP
```

---

## 9. AOP & Cross-Cutting Concerns

| Concern | Implementation |
|---------|---------------|
| **Logging** | `@LogDefault` annotation (`LogDefault.java`) + SLF4J/Log4j2 in controllers and services |
| **Input validation** | Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Email`) on controller parameters |
| **Error messages** | `MessageUtil` bean reads message keys from Spring `@Value` properties |
| **Transaction management** | `@Transactional` on service methods that write to the database |
| **CORS** | `@CrossOrigin(origins = "*")` on individual controllers |

---

## 10. Component Dependency Map

```
PrxBackofficeRestApplication
    ├── SecurityConfig
    │   ├── JwtConverter ← JwtConverterProperties
    │   ├── NimbusJwtDecoder ← JWK URI
    │   └── RestTemplate ← KeystoreUtil ← SecurityProperties
    ├── UserController → UserServiceImpl
    │   ├── UserRepository (prx-persistence)
    │   ├── RoleRepository (prx-persistence)
    │   ├── ApplicationRepository (prx-persistence)
    │   ├── ApplicationRoleUserRepository (prx-persistence)
    │   └── UserMapper → PersonMapper, RoleMapper, ApplicationRoleUserMapper
    ├── SessionController → SessionServiceImpl
    │   ├── UserRepository (prx-persistence)
    │   ├── JwtConfigProperties
    │   ├── UserMapper
    │   └── UserAliasMapper
    ├── RoleController → RoleServiceImpl
    │   └── RoleRepository (prx-persistence)
    ├── FeatureController → FeatureServiceImpl
    │   └── FeatureRepository (prx-persistence)
    ├── PersonController → PersonServiceImpl
    │   └── PersonRepository (prx-persistence)
    ├── ContactController → ContactServiceImpl
    │   └── ContactRepository (prx-persistence)
    ├── ContactTypeController → ContactTypeServiceImpl
    │   └── ContactTypeRepository (prx-persistence)
    ├── ApplicationController → ApplicationServiceImpl
    │   └── ApplicationRepository (prx-persistence)
    ├── ProfileImageController → ProfileImageServiceImpl
    └── DocumentController → DocumentServiceImpl
```

