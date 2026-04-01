# Development Guide — backbone-rest

---

## 1. Project Setup

### Prerequisites

- Java 21 (Amazon Corretto or OpenJDK)
- Maven 3.x on `PATH` (no `mvnw` in this repo)
- IntelliJ IDEA (recommended — `.idea/` and `.run/` configurations are present)
- Access to Repsy PRX Maven repository

### Clone and Configure

```bash
git clone <repo-url>
cd backbone-rest

# Required before any mvn command
export REPSY_ACCOUNT_USER=<your-repsy-user>
export REPSY_ACCOUNT_PASSWORD=<your-repsy-password>
```

### Fast Compile Check

```bash
mvn -DskipTests compile
```

---

## 2. Package Structure

```
com.prx.backoffice
├── PrxBackofficeRestApplication.java   ← Main entry point
├── aop/
│   └── LogDefault.java                ← Custom @LogDefault annotation
├── constant/
│   └── keys/                          ← Enum message keys (UserMessageKey, RoleMessageKey, etc.)
├── converter/
│   └── TemplateDocumentConverter.java ← Document template converter
├── property/
│   ├── ManagementAuthenticatorProperties.java
│   ├── SecurityProperties.java        ← @ConfigurationProperties(prefix="prx.security")
│   └── StoreProperties.java
├── security/
│   ├── config/
│   │   └── SecurityConfig.java        ← SecurityFilterChain, JwtDecoder, RestTemplate beans
│   ├── exception/
│   │   ├── CertificateSecurityException.java
│   │   └── JwtConverterException.java
│   └── jwt/
│       ├── JwtConfigProperties.java   ← @ConfigurationProperties(prefix="prx.jwt")
│       ├── JwtConverter.java          ← Keycloak JWT → Spring authorities
│       └── JwtConverterProperties.java
├── util/
│   ├── JwtUtil.java
│   ├── KeystoreUtil.java
│   └── MessageUtil.java               ← Spring @Component for user-facing messages
└── v1/
    ├── application/
    ├── contacts/
    ├── contacttypes/
    ├── features/
    ├── people/
    ├── profileimage/
    ├── report/
    ├── roles/
    ├── session/
    └── users/
```

---

## 3. Domain Module Layout (Canonical Pattern)

Every domain follows this exact structure. **Do not deviate.**

```
v1/<domain>/
├── api/
│   ├── controller/
│   │   ├── <Domain>Api.java        ← Interface with @Tag, @Operation, @ApiResponse, default methods
│   │   └── <Domain>Controller.java ← @RestController, implements <Domain>Api, delegates to service
│   └── to/
│       └── *.java                  ← Transfer Objects (records or simple POJOs)
├── mapper/
│   └── <Domain>Mapper.java         ← MapStruct @Mapper interface
└── service/
    ├── <Domain>Service.java        ← Interface
    └── <Domain>ServiceImpl.java    ← @Service implementation
```

---

## 4. API Interface Pattern

The `*Api` interface declares **all** endpoint mappings and OpenAPI annotations. The `*Controller` **only** overrides methods to delegate to the service.

### Example — UserApi (interface)

```java
@Tag(name = "users", description = "The user API")
public interface UserApi {

    default UserService getService() { return new UserService() {}; }

    @Operation(description = "Create a new user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created.")
    })
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<UserCreateResponse> create(
            @RequestBody @Valid UserCreateRequest userCreateRequest) {
        return getService().create(userCreateRequest);
    }
}
```

### Example — UserController (implementation)

```java
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserCreateResponse> create(UserCreateRequest request) {
        LOGGER.info("{} /create", MessageUtil.LOG_START_MSG);
        if (ValidatorCommonsUtil.esNulo(request)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return userService.create(request);
    }
}
```

> **Controller-cast pattern:** When a default method in `*Api` needs to call a method that exists only on the concrete controller, it casts `this`:
> ```java
> return ((UserController) this).putUserDetail(userId, request);
> ```

---

## 5. Service Layer Pattern

Services return `ResponseEntity<?>` directly — status decisions are made inside the service.

```java
@Service
public class UserServiceImpl implements UserService {

    @Override
    public ResponseEntity<Void> validateAlias(String alias, UUID applicationId) {
        var result = userRepository.findByAliasAndApplication(alias, applicationId);
        AtomicReference<ResponseEntity<Void>> response = new AtomicReference<>();
        result.ifPresentOrElse(
            entity -> response.set(new ResponseEntity<>(HttpStatus.CONFLICT)),
            ()     -> response.set(ResponseEntity.status(HttpStatus.OK).build())
        );
        return response.get();
    }

    @Transactional
    @Override
    public ResponseEntity<UserTO> update(UUID userId, UserTO user) {
        // ... update logic
    }
}
```

---

## 6. MapStruct Mapper Pattern

```java
@Mapper(
    config = MapperAppConfig.class,
    uses = {RoleMapper.class, PersonMapper.class, ApplicationRoleUserMapper.class}
)
public interface UserMapper {

    @Mapping(target = "roles",        source = "applicationRoleUser")
    @Mapping(target = "applications", source = "applicationRoleUser")
    UserTO toTarget(UserEntity entity);

    @Mapping(target = "applicationRoleUser",
             expression = "java(ApplicationRoleUserMapper.getApplicationRoleUser(user))")
    UserEntity toSource(UserTO user);
}
```

- All mappers use `MapperAppConfig` from `commons-services` for shared configuration.
- Use `expression = "java(...)"` for complex field transformations.
- Avoid modifying `prx-persistence` entities directly — use mappers.

---

## 7. Message Keys Convention

User-facing messages are externalised using `MessageUtil` (a Spring bean) and backed by message keys defined in `constant/keys/`:

```java
// In a service:
messageUtil.getUserAliasNuloVacio()       // "User alias null or empty"
messageUtil.getUserClaveNulaVacia()       // "Password null or empty"
messageUtil.getUserSolicitudNulaVacia()   // "Request null or empty"
```

Available enum key classes:

| Class | Domain |
|-------|--------|
| `UserMessageKey` | User operations |
| `RoleMessageKey` | Role operations |
| `FeatureMessageKey` | Feature operations |
| `PersonMessageKey` | Person operations |
| `RoleFeatureMessageKey` | Role-feature association |
| `ApplicationMessageKey` | Application operations |
| `AuthKey` | Authentication |
| `LogActionKey` | Logging actions |

---

## 8. Documentation Style

This project uses Java's `///` triple-slash doc comment style (JEP 467 / Java 23 preview syntax used proactively):

```java
/// Creates a new user.
///
/// @param userCreateRequest the user create request
/// @return the response entity with the user create response
@PostMapping(...)
default ResponseEntity<UserCreateResponse> create(...) { ... }
```

Use `///` for all new Javadoc. Standard `/** */` is acceptable in older files but do not mix within the same class.

---

## 9. Testing Conventions

### Test Class Setup

```java
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
```

> **Always use `MockitoAnnotations.openMocks(this)`** — not `@ExtendWith(MockitoExtension.class)` or the deprecated `initMocks`.

### Test Resource Configuration

`src/test/resources/application-test.yml` overrides configuration for the test profile.
`src/test/java/com/prx/backoffice/config/SecurityKeycloakTestConfig.java` provides a test security configuration.
`MockLoaderBase.java` provides shared mock loading utilities.

### PMD Compliance in Tests

PMD runs on the `test` phase and **will fail the build**. Ensure test classes comply with `ruleset.xml`.

---

## 10. Common Maven Commands

| Task | Command |
|------|---------|
| Fast compile | `mvn -DskipTests compile` |
| Full tests + PMD + JaCoCo | `mvn test` |
| Single test class | `mvn -Dtest=UserServiceImplTest test` |
| Package fat JAR | `mvn -DskipTests package` |
| Generate Javadoc | `mvn javadoc:javadoc` |
| SonarCloud (CI) | `mvn verify sonar:sonar -Dsonar.projectKey=prx-open_backbone-rest -s ci_settings.xml` |

---

## 11. Adding a New Domain Module

1. Create the package structure under `v1/<newdomain>/`:
   ```
   v1/newdomain/api/controller/NewDomainApi.java
   v1/newdomain/api/controller/NewDomainController.java
   v1/newdomain/api/to/<TOs>.java
   v1/newdomain/mapper/NewDomainMapper.java
   v1/newdomain/service/NewDomainService.java
   v1/newdomain/service/NewDomainServiceImpl.java
   ```
2. Annotate `NewDomainController` with `@RestController`, `@RequestMapping("/api/v1/<newdomain>")`, `@CrossOrigin(origins = "*")`.
3. Add `@Tag(name=..., description=...)` to `NewDomainApi`.
4. Annotate each endpoint method on the interface with `@Operation` and `@ApiResponses`.
5. Use `MessageUtil` for error/success messages (add new keys to an appropriate enum in `constant/keys/` if needed).
6. Write unit tests in `src/test/java/com/prx/backoffice/v1/<newdomain>/`.
7. Update `backbone_rest-openapi.yaml` in `src/main/resources/META-INF/`.

---

## 12. AOP Logging

The `@LogDefault` annotation can be applied to controller methods, fields, or parameters:

```java
@LogDefault(action = LogActionKey.CREATE, detail = UserMessageKey.class)
public ResponseEntity<UserCreateResponse> create(...) { ... }
```

This integrates with the logging infrastructure to record action type and message category.

