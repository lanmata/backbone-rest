# Write Tests — backbone-rest

Generate JUnit 5 + Mockito unit tests for a service or controller class.

## Usage
```
/write-tests
```
Provide the fully qualified class name or file path, e.g.:
`com.umdc.backoffice.v1.roles.service.RoleServiceImpl`

---

## Steps Claude Will Execute

### 1. Read the Target Class
Identify all public methods, dependencies (constructor params), and return types.

### 2. Read Existing Test Pattern
Find the nearest existing `*ServiceImplTest.java` in the same domain and match its style exactly.

### 3. Generate Test Class

**Location**: `src/test/java/com/umdc/backoffice/v1/<domain>/service/<ClassNameTest>.java`

**Template**:
```java
@ExtendWith(MockitoExtension.class)
class FooServiceImplTest {

    @Mock
    FooRepository fooRepository;  // one @Mock per constructor dependency

    @InjectMocks
    FooServiceImpl fooService;

    @Test
    void methodName_returnsOk_whenCondition() {
        // given
        // when
        // then — assert getStatusCode() first, then body
    }

    @Test
    void methodName_returnsNotFound_whenAbsent() { ... }

    @Test
    void methodName_returnsBadRequest_whenInvalidInput() { ... }
}
```

### 4. Coverage Targets
For each public method:
- Happy path (expected input → 200/201).
- Not found (repository returns empty → 404).
- Bad/null input (validation failure → 400).
- Repository exception → 500 or appropriate status.

### 5. PMD Check
```bash
mvn pmd:check
mvn -Dtest=<ClassName>Test test
```

---

## Rules
- `@ExtendWith(MockitoExtension.class)` — no `@SpringBootTest`.
- Method names: `method_expectedResult_condition`.
- AssertJ assertions: `assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK)`.
- No `@Autowired` in test classes.
- Services return `ResponseEntity<?>` — always assert status code first.

## Output
Produce the complete test file and a coverage summary table.
