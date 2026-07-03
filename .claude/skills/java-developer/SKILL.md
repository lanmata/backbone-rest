---
name: Java Developer Skills
description: Consolidated skill set for the Java Developer agent — Java 21, Spring Boot 3.4.x, MapStruct, OAuth2, Supabase, PMD
applies-to:
  - java-developer
---

# Java Developer — Skill Definition

## 1. Project-Specific Patterns

### API Interface (`*Api.java`)
```java
@Tag(name = "users", description = "The user API")
public interface UserApi {

    UserService getService();

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

### Controller (`*Controller.java` — thin, no logic)
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
    public UserService getService() { return userService; }

    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        return userService.findUserById(userId);
    }
}
```

### Service (`*ServiceImpl.java` — ResponseEntity decisions here)
```java
@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        log.info("{} findUserById userId:{}", MessageUtil.LOG_START_MSG, userId);
        if (Objects.isNull(userId)) {
            return ResponseEntity.badRequest()
                .header(HttpHeaders.WARNING, "userId is null").build();
        }
        var result = userRepository.findById(userId)
            .map(e -> ResponseEntity.ok(userMapper.toTarget(e)))
            .orElseGet(() -> ResponseEntity.notFound()
                .header(HttpHeaders.WARNING, "User not found").build());
        log.info("{} findUserById", MessageUtil.LOG_END_MSG);
        return result;
    }
}
```

### MapStruct Mapper
```java
@Mapper(config = MapperAppConfig.class, uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "roles", source = "applicationRoleUser")
    UserTO toTarget(UserEntity entity);
    UserEntity toSource(UserTO user);
}
```
`MapperAppConfig` is from `com.umdc.commons.services` — always include it.

---

## 2. Naming Conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| API interface | `*Api.java` | `UserApi.java` |
| Controller | `*Controller.java` | `UserController.java` |
| Service interface | `*Service.java` | `UserService.java` |
| Service impl | `*ServiceImpl.java` | `UserServiceImpl.java` |
| DTO | `*TO.java` | `UserTO.java` |
| Mapper | `*Mapper.java` | `UserMapper.java` |
| Message key enum | `*MessageKey.java` | `UserMessageKey.java` |
| Package prefix | `com.umdc.backoffice.v1.<domain>` | `com.umdc.backoffice.v1.users` |

---

## 3. Error Handling

| Status | When to Use |
|--------|-------------|
| 200 | Successful read/query |
| 201 | Resource created |
| 202 | Accepted (async/partial update) |
| 204 | Delete — no content |
| 400 | Null/blank input or validation failure (+ `Warning` header) |
| 401 | Unauthenticated |
| 404 | Resource not found (+ `Warning` header) |
| 409 | Conflict (duplicate alias/email) |
| 500 | Unexpected failure |

Always include a `HttpHeaders.WARNING` header on 4xx responses to surface reason to the client.

---

## 4. Key Files

| File | Purpose |
|------|---------|
| `src/main/resources/META-INF/api.yaml` | OpenAPI 3.1 spec — update when `*Api.java` changes |
| `src/main/resources/bootstrap.yml` | Spring Cloud Config + Vault + OAuth2 config |
| `src/main/resources/application.yml` | App-level config (ports, JPA, Redis) |
| `ruleset.xml` | PMD ruleset — zero violations enforced at test phase |
| `pom.xml` | Dependencies, plugins, JaCoCo/PMD config |
| `src/main/java/com/umdc/backoffice/util/MessageUtil.java` | LOG_START_MSG, LOG_END_MSG, user-facing messages |
| `src/main/java/com/umdc/backoffice/security/config/SecurityConfig.java` | OAuth2 + session JWT config |

---

## 5. Constraints

- No `@Autowired` field injection — constructor injection only.
- No logic in controllers — all business logic in `*ServiceImpl`.
- No `mvnw` — use `mvn` directly.
- No breaking changes to `/api/v1/*` endpoint contracts.
- No secrets committed — `default.env` contains stubs only.
- No modifications to `keystore.jks`, `backbone.jks`, `*.crt`, `umdc-truststore.jks`.
- MapStruct `@Mapper` always includes `config = MapperAppConfig.class`.
- `*Api.java` default methods delegate to service via `getService()` — not via direct cast.
- Use `///` triple-slash JavaDoc style (existing project pattern).

---

## 6. Checklist

- [ ] Compile passes: `mvn -DskipTests compile`
- [ ] PMD passes: `mvn pmd:check`
- [ ] All tests pass: `mvn test`
- [ ] `api.yaml` updated if any `*Api.java` changed
- [ ] No `@Autowired` field injection introduced
- [ ] Constructor injection used throughout
- [ ] `MessageUtil` used for user-facing strings
- [ ] SLF4J logger added: `LoggerFactory.getLogger(Foo.class)`
- [ ] HTTP status codes match conventions table above
- [ ] `Warning` header included on 4xx responses
