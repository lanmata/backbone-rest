# Implement Feature

## When to use
When adding a new REST endpoint and its backing service logic to backbone-rest.

## Steps

1. **Define the API interface** — create `src/main/java/com/umdc/backoffice/v1/<domain>/api/controller/<Domain>Api.java`
   - Add `@Tag(name = "<domain>", description = "...")` on the interface
   - Add `default <Domain>Service getService() { return new <Domain>Service() {}; }`
   - Add one method per endpoint with `@Operation`, `@ApiResponses`, and `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`
   - Use `HttpStatusUtil.OK_STR`, `HttpStatusUtil.CREATED_STR`, etc. for response codes
   - Declare string constants (e.g., `STR_ID_WIDGET`) to avoid PMD `AvoidDuplicateLiterals`

2. **Create the controller** — create `<Domain>Controller.java` in the same package
   - `@RestController` + `@RequestMapping("/api/v1/<domain>")` + `@CrossOrigin(origins = "*")`
   - Constructor injection of `<Domain>Service` only
   - `@Override` every method from the API interface — one line each, delegates to service
   - One `LOGGER.info("{} /methodName", MessageUtil.LOG_START_MSG)` call per method

3. **Create the service interface** — `src/.../v1/<domain>/service/<Domain>Service.java`
   - Methods return `ResponseEntity<?>`
   - No implementation

4. **Create the service impl** — `<Domain>ServiceImpl.java` in the same `service/` package
   - `@Service`
   - Constructor injection of all dependencies (repositories, mappers, other services)
   - `@Transactional` on write methods
   - Null guards with `Objects.isNull()` returning `ResponseEntity.badRequest().build()`
   - `LOGGER.error("{}| ...", MessageKey.STATUS.getStatus(), ..., ex)` for exceptions

5. **Create the DTO(s)** — in `v1/<domain>/api/to/`
   - Request: Java record with `@NotBlank` / `@NotNull` / `@Valid` constraints
   - Response: plain class with getters + setters (no Lombok — MapStruct needs setters)

6. **Create the mapper** — `v1/<domain>/mapper/<Domain>Mapper.java`
   - `@Mapper(config = MapperAppConfig.class)`
   - `toSource(DTO)` and `toTarget(Entity)` methods

7. **Add message keys** — `src/.../constant/keys/<Domain>MessageKey.java`
   - Implement `BackboneMessage`
   - One enum constant per user-facing error state

8. **Update OpenAPI YAML** — `src/main/resources/META-INF/api.yaml`
   - Add path entry, operation, parameters, and response schemas

9. **Write tests** — see `/write-tests` skill or invoke `@tester`

10. **Run end-of-task checklist**:
    ```bash
    mvn -DskipTests compile
    mvn pmd:check
    mvn test
    ```

## Checklist
- [ ] `*Api.java` has `@Tag` and all `@Operation` + `@ApiResponses`
- [ ] Controller has zero logic — only `delegate to service`
- [ ] Constructor injection only — no `@Autowired`
- [ ] Service returns `ResponseEntity<?>` — status decisions in `*ServiceImpl`
- [ ] Null checks use `Objects.isNull()` / `Objects.nonNull()`
- [ ] `@Valid` on all `@RequestBody` params
- [ ] New constants in `*MessageKey` enum
- [ ] `api.yaml` updated
- [ ] PMD zero violations: `mvn pmd:check`
- [ ] Tests written and passing: `mvn test`
