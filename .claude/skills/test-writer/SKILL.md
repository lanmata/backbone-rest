---
name: Test Writer Skills
description: Consolidated skill set for the Test Writer agent — JUnit 5, Mockito, Spring Boot test slice, JaCoCo coverage for backbone-rest
applies-to:
  - test-writer
---

# Test Writer — Skill Definition

## 1. Project-Specific Patterns

### Base Test Class
All service tests extend or reference `MockLoaderBase`:
```java
// src/test/java/com/umdc/backoffice/MockLoaderBase.java
// Provides: pre-wired Mockito mocks for common dependencies
```

### Service Unit Test Pattern
```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findUserById_Found_Returns200() {
        var entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(entity));
        when(userMapper.toTarget(entity)).thenReturn(new UserTO());

        var response = userService.findUserById(entity.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findUserById_NullId_Returns400() {
        var response = userService.findUserById(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void findUserById_NotFound_Returns404() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        var response = userService.findUserById(UUID.randomUUID());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

### Controller Test Pattern (`@WebMvcTest`)
```java
@WebMvcTest(UserController.class)
@Import(SecurityKeycloakTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void findUserById_Returns200() throws Exception {
        when(userService.findUserById(any())).thenReturn(ResponseEntity.ok(new UserTO()));
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk());
    }
}
```

### Template Utilities
Use `TemplateUtil` and domain template classes (`UserTemplateTest`, `PersonTemplateTest`, etc.) to build test fixtures:
```java
// src/test/java/com/umdc/backoffice/v1/util/TemplateUtil.java
var user = TemplateUtil.buildUser();
```

---

## 2. Naming Conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| Service test | `*ServiceImplTest.java` | `UserServiceImplTest.java` |
| Controller test | `*ControllerTest.java` | `UserControllerTest.java` |
| Test package | mirrors main package under `src/test/java` | `com.umdc.backoffice.v1.users` |
| Template class | `*TemplateTest.java` | `UserTemplateTest.java` |

---

## 3. Test Scope and Framework

| Framework | Version | Usage |
|-----------|---------|-------|
| JUnit 5 (`junit-jupiter`) | 5.x | All tests |
| Mockito | 5.x | Mocking services, repos, mappers |
| Spring Boot Test | 3.4.x | `@WebMvcTest`, `@SpringBootTest` |
| AssertJ | (included) | Fluent assertions |
| H2 | In-memory | Integration tests datasource |

---

## 4. Key Files

| File | Purpose |
|------|---------|
| `src/test/java/com/umdc/backoffice/MockLoaderBase.java` | Base class with pre-wired mocks |
| `src/test/java/com/umdc/backoffice/config/SecurityKeycloakTestConfig.java` | Test security config — import in `@WebMvcTest` |
| `src/test/java/com/umdc/backoffice/v1/util/TemplateUtil.java` | Test fixture builders |
| `pom.xml` — `jacoco-maven-plugin` | Coverage thresholds (check `<limit>` elements) |

---

## 5. Constraints

- Cover all three cases for every service method: success (2xx), null input (400), not found (404).
- `@WebMvcTest` must import `SecurityKeycloakTestConfig` — otherwise all requests return 401.
- No real database in unit tests — use H2 or pure Mockito mocks.
- No `@Autowired` field injection in tests — use constructor or `@InjectMocks`.
- PMD applies to test code too — zero violations.

---

## 6. Checklist

- [ ] Every public service method has: success test, null-input test, not-found test
- [ ] `@ExtendWith(MockitoExtension.class)` on all unit tests
- [ ] `SecurityKeycloakTestConfig` imported in all `@WebMvcTest` tests
- [ ] Test names follow `methodName_Condition_ExpectedResult` pattern
- [ ] `mvn test` passes with new tests
- [ ] JaCoCo coverage meets threshold defined in `pom.xml`
- [ ] No real secrets or hardcoded passwords in test data
