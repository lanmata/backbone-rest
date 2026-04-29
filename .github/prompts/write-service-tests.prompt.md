---
name: Write Service Tests
description: Generate comprehensive JUnit 5 + Mockito unit tests for a service implementation
mode: agent
agent: test-writer
tools: [run_in_terminal, read_file, insert_edit_into_file, replace_string_in_file, create_file, grep_search, file_search, get_errors]
---

Write complete unit tests for a **backbone-rest** service implementation.

## Target

- Service class: ${serviceClass}
  _(e.g., `com.umdc.backoffice.service.users.v1.backoffice.UserServiceImpl`)_

## Steps

1. Read `${serviceClass}` — identify all public methods.
2. Read existing tests in `src/test/java` to match naming and setup patterns.
3. Read `src/test/resources/application-test.yml` for the test profile config.

## Test Requirements Per Method

For each public service method, write tests covering:

| Scenario | Expected Status | Test Name Pattern |
|---------|----------------|------------------|
| Null / blank input | 400 Bad Request | `test<Method>_BadRequest_NullInput` |
| Entity found | 200 OK | `test<Method>_Found` |
| Entity not found | 404 Not Found | `test<Method>_NotFound` |
| Conflict / duplicate | 400 or 409 | `test<Method>_Conflict` |
| Exception thrown | 500 or 422 | `test<Method>_Exception` |
| Successful create | 201 Created | `test<Method>_Created` |
| Successful delete | 204 No Content | `test<Method>_Deleted` |

## Setup Pattern (mandatory — do NOT use @ExtendWith)

```java
class ${ServiceClass}Test {
    @Mock ${Repo}  repository;
    @Mock ${Mapper} mapper;
    @InjectMocks ${ServiceClass} service;

    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }
}
```

## Constraints

- One assertion per logical branch — use `assertEquals`, `assertNotNull`, `verify`.
- No duplicate string literals — extract to `private static final String`.
- No unused `@Mock` fields.
- Run `mvn -Dtest=${ServiceClass}Test test` — all tests must pass.
- Run `mvn pmd:check` — zero violations.

## Output

Report: test class file path, number of tests written, all scenario branches covered.

