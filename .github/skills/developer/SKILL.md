---
name: Developer Skills
description: Consolidated skill set for the Developer agent — Java 21, Spring Boot 3.4.x, REST API, MapStruct, JPA, and OAuth2 security
applies-to:
  - Developer
---

# Developer — Skill Definition

## 1. Java 21 Patterns

```java
// Records for DTOs (immutable, concise)
public record UserCreateRequest(
    @NotBlank String alias,
    @NotBlank String password,
    @Email    String email,
    @NotNull  UUID applicationId,
    @NotNull  UUID roleId
) {}

// var for obvious local types
var entity = userRepository.findById(userId).orElseThrow();

// Objects.isNull / Objects.nonNull (existing project pattern)
if (Objects.isNull(request)) {
    return ResponseEntity.badRequest().build();
}

// Optional from repository — use map/orElseGet
return userRepository.findById(userId)
    .map(e -> ResponseEntity.ok(userMapper.toTarget(e)))
    .orElseGet(() -> ResponseEntity.notFound().build());
```

---

## 2. API Interface Pattern (`*Api.java`)

```java
@Tag(name = "users", description = "The user API")
public interface UserApi {

    default UserService getService() { return new UserService() {}; }

    @Operation(description = "Find a user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found."),
        @ApiResponse(responseCode = "404", description = "User not found.")
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/{userId}")
    default ResponseEntity<UserTO> findUserById(
        @Parameter(description = "User UUID", required = true)
        @NotNull @PathVariable UUID userId
    ) {
        return getService().findUserById(userId);
    }
}
```

---

## 3. Controller Pattern (Thin)

```java
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController implements UserApi {
    private final UserService userService;

    /// Constructor for UserController.
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        return userService.findUserById(userId);
    }
}
```

---

## 4. Service Pattern (Business Logic + ResponseEntity)

```java
@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String MSG_NOT_FOUND = "User not found.";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        LOGGER.info("{} findUserById userId:{}", MessageUtil.LOG_START_MSG, userId);
        if (Objects.isNull(userId)) {
            return ResponseEntity.badRequest()
                .header(HttpHeaders.WARNING, "userId is null")
                .build();
        }
        var result = userRepository.findById(userId)
            .map(e -> ResponseEntity.ok(userMapper.toTarget(e)))
            .orElseGet(() -> ResponseEntity.notFound()
                .header(HttpHeaders.WARNING, MSG_NOT_FOUND).build());
        LOGGER.info("{} findUserById", MessageUtil.LOG_END_MSG);
        return result;
    }
}
```

---

## 5. MapStruct Mapping Pattern

```java
@Mapper(config = MapperAppConfig.class, uses = {RoleMapper.class, PersonMapper.class})
public interface UserMapper {
    @Mapping(target = "roles", source = "applicationRoleUser")
    UserTO toTarget(UserEntity entity);
    UserEntity toSource(UserTO user);
}
```

Always use `config = MapperAppConfig.class` (from `com.umdc.commons.services`).

---

## 6. Spring Security & OAuth2

**Two auth mechanisms — never conflate:**

| Mechanism | Scope | Token Location |
|-----------|-------|---------------|
| OAuth2 Resource Server (Keycloak) | All `/api/v1/**` except session | `Authorization: Bearer <token>` |
| App Session JWT (JJWT 0.12.3) | `/api/v1/session/**` | `session-token` header |

Roles extracted from Keycloak JWT `resource_access.<clientId>.roles` by `JwtConverter`.

```yaml
# bootstrap.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${AUTH_SERVER_URI}
          jwk-set-uri: ${AUTH_SERVER_URI}${AUTH_CERT_URI}
```

---

## 7. HTTP Status Code Conventions

| Status | Usage |
|--------|-------|
| 200 | Successful read/query |
| 201 | Resource created |
| 202 | Accepted (partial update) |
| 204 | Delete — no content |
| 400 | Null/blank input or validation failure (+ `Warning` header) |
| 401 | Unauthenticated |
| 404 | Resource not found |
| 406 | Not acceptable |
| 409 | Conflict (duplicate alias/email) |
| 422 | Unprocessable entity |
| 500 | Unexpected failure |

---

## 8. Package Map

| Package | Purpose |
|---------|---------|
| `com.prx.backoffice.v1.<domain>.api.controller` | `*Api` interface + `*Controller` |
| `com.prx.backoffice.v1.<domain>.service` | `*Service` interface + `*ServiceImpl` |
| `com.prx.backoffice.v1.<domain>.api.to` | DTOs (request/response) |
| `com.prx.backoffice.v1.<domain>.mapper` | MapStruct mappers |
| `com.prx.backoffice.security` | SecurityConfig, JwtConverter |
| `com.prx.backoffice.util` | MessageUtil, JwtUtil, KeystoreUtil |
| `com.prx.backoffice.constant.keys` | `*MessageKey` enums |

---

## 9. Build Commands

```bash
mvn -DskipTests compile        # fast compile check
mvn test                       # full test + PMD + JaCoCo
mvn -Dtest=UserServiceImplTest test  # single test class
mvn -DskipTests package        # package JAR
```

> No `mvnw` — always use `mvn` directly.  
> Set `REPSY_ACCOUNT_USER` + `REPSY_ACCOUNT_PASSWORD` before Maven runs.

---

## 10. Safety Rules

- Constructor injection only — no `@Autowired` on fields.
- Do NOT modify `keystore.jks` or `*.crt` files unless explicitly requested.
- Do NOT commit real secrets — `default.env` contains stubs only.
- Do NOT refactor unrelated packages — keep edits atomic.
- Do NOT break existing tests.
- Update `backbone_rest-openapi.yaml` when API contracts change.

