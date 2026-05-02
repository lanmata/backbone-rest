---
name: Feature Development
description: End-to-end workflow for developing a new feature
trigger: manual
agents:
  - Product Owner
  - Developer
  - QA / Test Writer
  - API Reviewer
  - Code Reviewer
---

# Feature Development Workflow

## Purpose

Orchestrates the end-to-end process of developing a new feature for the
**backbone-rest** microservice, from requirements to merged code.

## Workflow Steps

### Step 1: Requirements Definition
**Agent**: Product Owner
**Action**: Define user story with acceptance criteria

```
#runSubagent agentName="Product Owner"
"Define the user story, acceptance criteria, and OpenAPI contract fragment for [feature description].
Include example JSON payloads and expected HTTP status codes.
Reference the affected domain under /api/v1/<domain>."
```

**Output**: User story with acceptance criteria, OpenAPI YAML fragment

---

### Step 2: Implementation
**Agent**: Developer
**Action**: Implement the feature following backbone-rest conventions

```
#runSubagent agentName="Developer"
"Implement the feature based on the acceptance criteria:
[paste acceptance criteria]
Follow the existing pattern:
  *Api.java (interface: mappings + OpenAPI) → *Controller.java (thin, delegates) → *ServiceImpl.java (ResponseEntity logic) → repositories (com.umdc.persistence)
Use MessageUtil for user-facing messages. Use MapperAppConfig for MapStruct config."
```

**Output**: Production code changes in `src/main/java/com/prx/backoffice/v1/<domain>/`

---

### Step 3: Test Writing
**Agent**: QA / Test Writer
**Action**: Write unit tests for the implementation

```
#runSubagent agentName="QA / Test Writer"
"Write JUnit 5 unit tests for the implemented feature.
Use MockitoAnnotations.openMocks(this) in @BeforeEach (not @ExtendWith).
Cover happy path, null inputs, not-found, conflict, and bad-request cases.
Mirror the package structure under src/test/java."
```

**Output**: Test files under `src/test/java/com/prx/backoffice/v1/<domain>/`

---

### Step 4: API Contract Review
**Agent**: API Reviewer
**Action**: Validate API contract consistency

```
#runSubagent agentName="API Reviewer"
"Review the API changes for consistency between *Api.java annotations
and backbone_rest-openapi.yaml. Check backward compatibility and
verify @RestController/@RequestMapping/@CrossOrigin annotations on the controller."
```

**Output**: API review report

---

### Step 5: Code Review
**Agent**: Code Reviewer
**Action**: Review code quality and conventions

```
#runSubagent agentName="Code Reviewer"
"Review the code changes for quality, convention compliance, and potential issues.
Check: thin controllers, service ResponseEntity pattern, MessageUtil usage,
constructor injection, PMD ruleset compliance, and /// doc style."
```

**Output**: Code review report with approval status

---

### Step 6: Verification

```bash
# Must pass before merging
mvn test
```

## Exit Criteria

- [ ] User story with acceptance criteria defined
- [ ] Code implemented following backbone-rest conventions
- [ ] JUnit 5 unit tests added and passing
- [ ] API review passed
- [ ] Code review approved
- [ ] `mvn test` green (no PMD violations, tests pass)
- [ ] `backbone_rest-openapi.yaml` updated if API changed
