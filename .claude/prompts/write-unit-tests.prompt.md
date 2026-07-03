---
name: Write Unit Tests
description: Write JUnit 5 + Mockito unit tests for a backbone-rest service or controller class, following MockLoaderBase patterns and JaCoCo coverage targets
mode: agent
agent: test-writer
tools: [Bash, Read, Write]
---

Write unit tests for the following class in **backbone-rest**.

## Target

- Class: ${className}
- File: ${filePath}
- Domain: ${domain}

## Step 1 — Read the target class

Read the full source of `${filePath}` to understand all public methods.

## Step 2 — Read existing tests for reference

```bash
find src/test/java -path "*/${domain}*" -name "*.java" | sort
```

Read the most relevant existing test to understand the test patterns used.

## Step 3 — Read `MockLoaderBase`

```
src/test/java/com/umdc/backoffice/MockLoaderBase.java
```

Understand what mocks are pre-wired.

## Step 4 — Write tests

For each public method in `${className}`, write at minimum:
1. **Success case** — 2xx response with valid input
2. **Null input case** — 400 response when required param is null
3. **Not found case** — 404 response when resource doesn't exist (if applicable)
4. **Conflict case** — 409 response for duplicates (if applicable)

### Test class template
```java
@ExtendWith(MockitoExtension.class)
class ${className}Test {

    @Mock
    private <Repository> repository;

    @Mock
    private <Mapper> mapper;

    @InjectMocks
    private ${className} service;

    @Test
    void methodName_Success_Returns200() { ... }

    @Test
    void methodName_NullInput_Returns400() { ... }

    @Test
    void methodName_NotFound_Returns404() { ... }
}
```

### For controller tests (`@WebMvcTest`)
```java
@WebMvcTest(${ControllerClass}.class)
@Import(SecurityKeycloakTestConfig.class)
class ${ControllerClass}Test {
    @Autowired MockMvc mockMvc;
    @MockBean <Service> service;
    ...
}
```

## Step 5 — Verify

```bash
mvn -Dtest=${className}Test test
mvn pmd:check
```

## Constraints

- Test method names: `methodName_Condition_ExpectedResult`
- No real DB in unit tests — use Mockito mocks.
- Always import `SecurityKeycloakTestConfig` in `@WebMvcTest` tests.
- No `@Autowired` field injection in tests.
- PMD must pass on test code.

## Output

List of test methods written + `mvn test` result for the test class.
