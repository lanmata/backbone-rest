---
name: JUnit 5 Testing
description: Skill for JUnit 5 and Mockito test patterns
applies-to:
  - QA / Test Writer
  - Developer
---

# JUnit 5 Testing Skill

## Scope

This skill covers JUnit 5 + Mockito test patterns and Spring Boot test utilities
for the **backbone-rest** project.

## Test Dependencies

- JUnit Jupiter 5.10.5
- Mockito 5.14.2
- Spring Boot Test 3.4.1 (managed)
- H2 2.2.224 (test scope)
- MockServer 5.15.0 (junit-jupiter)

## Test Structure — Service Unit Tests (Preferred Pattern)

> **Important**: Use `MockitoAnnotations.openMocks(this)` in `@BeforeEach`, NOT `@ExtendWith(MockitoExtension.class)` — matches the existing pattern in `UserServiceImplTest`.

```java
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock ApplicationRoleUserRepository applicationRoleUserRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserMapper userMapper;

    @InjectMocks UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserById_Found() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        UserTO userTO = new UserTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);

        ResponseEntity<UserTO> response = userService.findUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userTO, response.getBody());
    }

    @Test
    void testFindUserById_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<UserTO> response = userService.findUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
```

## Controller Tests

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean UserService userService;

    @Test
    void findUserById_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.findUserById(userId)).thenReturn(ResponseEntity.ok(new UserTO()));

        mockMvc.perform(get("/api/v1/users/user/{userId}", userId))
            .andExpect(status().isOk());
    }
}
```

## Assertions

Use JUnit 5 built-in assertions — no AssertJ or Hamcrest:

```java
assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
assertNotNull(result);
assertNull(result.getBody());
assertFalse(list.isEmpty());
verify(userRepository, times(1)).findById(userId);
verifyNoInteractions(userMapper);
```

## Naming Conventions

- Test class: `{ClassName}Test` — e.g., `UserServiceImplTest`
- Test method: `testMethodName_Scenario` — e.g., `testFindUserById_Found`
- Mirror production package path under `src/test/java`

## Test Profile

Tests use `src/test/resources/application-test.yml` (PostgreSQL + H2, Keycloak stubs).
Security is overridden via `SecurityKeycloakTestConfig`.
