---
name: Java Spring Development
description: Core skill for developing Java 21 Spring Boot 3.4.x microservices
applies-to:
  - Developer
  - Code Reviewer
---

# Java Spring Development Skill

## Scope

This skill covers Java 21 and Spring Boot 3.4.1 development patterns used in the
**backbone-rest** microservice.

## Java 21 Patterns

### Records for DTOs
```java
public record UserCreateRequest(
    String alias,
    String password,
    UUID applicationId,
    UUID roleId,
    String email
) {}
```

### `var` for Locals
```java
var userEntity = userRepository.findById(userId).orElseThrow(...);
var result = userMapper.toTarget(userEntity);
```

### `Objects.isNull` / `Objects.nonNull` (existing pattern)
```java
if (Objects.isNull(userId)) {
    return ResponseEntity.badRequest().build();
}
```

## Spring Boot 3.4.1 Patterns

### Api Interface Pattern
```java
@Tag(name = "users", description = "The user API")
public interface UserApi {
    default UserService getService() { return new UserService() {}; }

    @Operation(description = "Find a user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found.")
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, path = "/user/{userId}")
    default ResponseEntity<UserTO> findUserById(@NotNull @PathVariable UUID userId) {
        return getService().findUserById(userId);
    }
}
```

### Controller Pattern (Thin)
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

### Service Pattern (Business Logic + ResponseEntity)
```java
@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        LOGGER.info("{} findUserById userId:{}", MessageUtil.LOG_START_MSG, userId);
        return userRepository.findById(userId)
            .map(entity -> new ResponseEntity<>(userMapper.toTarget(entity), HttpStatus.OK))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
```

### MapStruct Mapper Pattern
```java
@Mapper(config = MapperAppConfig.class, uses = {RoleMapper.class, PersonMapper.class})
public interface UserMapper {
    @Mapping(target = "roles", source = "applicationRoleUser")
    UserTO toTarget(UserEntity entity);
    UserEntity toSource(UserTO user);
}
```

## Documentation Style

Many files use `///` triple-slash JavaDoc — preserve this in touched files:
```java
/// Finds a user by ID.
///
/// @param userId the user ID
/// @return the response entity containing the user
```

## Dependency Injection

- **Always use constructor injection** — no `@Autowired` on fields.
- External module beans (`com.prx.persistence`, `com.prx.commons.services`) are auto-scanned via `PrxBackofficeRestApplication`.
