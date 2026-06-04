---
name: Test Writer Skills
description: Consolidated skill set for the QA / Test Writer agent — JUnit 5, Mockito, Spring Boot testing, JaCoCo, and test design patterns
applies-to:
  - QA / Test Writer
---

# QA / Test Writer — Skill Definition

## 1. Test Setup Pattern

**Always use `MockitoAnnotations.openMocks(this)` in `@BeforeEach`** — do NOT use `@ExtendWith(MockitoExtension.class)`. Follow the pattern from `UserServiceImplTest`:

```java
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock UserMapper userMapper;

    @InjectMocks UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
```

---

## 2. JUnit 5 Patterns

### Test Method Naming Convention

```
test<MethodName>_<Condition>

Examples:
  testFindUserById_Found
  testFindUserById_NotFound
  testCreate_BadRequest_NullRequest
  testCreate_Conflict_AliasExists
```

### Arrange / Act / Assert (AAA)

```java
@Test
void testFindUserById_Found() {
    // Arrange
    var userId = UUID.randomUUID();
    var entity = buildUserEntity(userId, "jdoe");
    var dto    = new UserTO(userId, "jdoe", "jdoe@example.com");
    when(userRepository.findById(userId)).thenReturn(Optional.of(entity));
    when(userMapper.toTarget(entity)).thenReturn(dto);

    // Act
    var response = userService.findUserById(userId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("jdoe", response.getBody().alias());
}
```

### Standard Coverage Scenarios per Service Method

| Scenario | Expected Status | Test Name Pattern |
|---------|----------------|------------------|
| Null input | 400 Bad Request | `test<Method>_BadRequest_NullInput` |
| Valid, found | 200 OK | `test<Method>_Found` |
| Valid, not found | 404 Not Found | `test<Method>_NotFound` |
| Conflict/duplicate | 409 / 400 | `test<Method>_Conflict` |
| Exception thrown | 500 / 422 | `test<Method>_Exception` |
| Successful create | 201 Created | `test<Method>_Created` |
| Successful delete | 204 No Content | `test<Method>_Deleted` |

### Parameterized Tests

```java
@ParameterizedTest
@NullSource
@ValueSource(strings = {"", " ", "\t"})
void testCreate_BadRequest_InvalidAlias(String alias) {
    var response = userService.create(buildCreateRequest(alias));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
}
```

---

## 3. Mockito Mocking Patterns

```java
// Stub return
when(userRepository.findById(userId)).thenReturn(Optional.of(entity));

// Stub with any matcher
when(userRepository.findByAlias(anyString())).thenReturn(Optional.empty());

// Stub void method
doNothing().when(userRepository).deleteById(userId);

// Stub exception
when(userRepository.findById(any())).thenThrow(new RuntimeException("DB error"));

// Verify called once
verify(userRepository).findById(userId);

// Verify never called
verify(userRepository, never()).deleteById(any());

// Argument capture
var captor = ArgumentCaptor.forClass(UserEntity.class);
verify(userRepository).save(captor.capture());
assertEquals("jdoe", captor.getValue().getAlias());
```

> ⚠️ **Never mix raw values and matchers** — use `eq()` when combining with other matchers.

---

## 4. Spring Boot Web Layer Tests (`@WebMvcTest`)

```java
@WebMvcTest(UserController.class)
@Import(SecurityKeycloakTestConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  UserService userService;

    @Test
    void testFindUserById_Returns200() throws Exception {
        var userId = UUID.randomUUID();
        when(userService.findUserById(userId)).thenReturn(ResponseEntity.ok(buildUserTO(userId)));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alias").value("jdoe"));
    }

    @Test
    void testFindUserById_NotFound() throws Exception {
        when(userService.findUserById(any())).thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
```

### MockMvc Helpers

```java
// GET / POST / PUT / DELETE
mockMvc.perform(post("/api/v1/users")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.alias").value("jdoe"))
    .andExpect(header().string("Warning", containsString("...")));
```

---

## 5. JaCoCo Coverage

```bash
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

**Advisory coverage targets** (minimum enforced = 0%):

| Layer | Advisory Target |
|-------|----------------|
| `*ServiceImpl` | ≥ 80% |
| `*Controller` | ≥ 60% |
| `*Mapper` | ≥ 70% |
| Utility classes | ≥ 70% |

---

## 6. Test Design Principles

- Each test must be **independent** — no shared mutable state between tests.
- `MockitoAnnotations.openMocks(this)` in `@BeforeEach` resets mocks.
- No `Thread.sleep()` — tests must be deterministic.
- Unit tests preferred over `@SpringBootTest` (faster; use `@WebMvcTest` for controllers).
- Extract test data builders to avoid duplicate literals (PMD compliance).

---

## 7. PMD Compliance in Tests

| PMD Rule | Fix |
|----------|-----|
| `AvoidDuplicateLiterals` | Extract repeated string to `private static final String` |
| `UnusedPrivateField` | Remove any `@Mock` field that is never referenced |
| `JUnitTestsShouldIncludeAssert` | Add `verify(...)` or `assertEquals(...)` |

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `src/test/java/com/prx/backoffice/v1/users/service/UserServiceImplTest.java` | Canonical service test example |
| `src/test/resources/application-test.yml` | Test profile config |
| `src/test/java/com/prx/backoffice/config/SecurityKeycloakTestConfig.java` | Security override for `@WebMvcTest` |

---

## Deliverables When Writing Tests

1. Test file under `src/test/java` mirroring the production package.
2. Summary of coverage paths (happy path, error branches).
3. Runnable command: `mvn -Dtest=<ClassName> test`

