# GraalVM Native Image for backbone-rest

## Objective and result

Java 21 → 25 + GraalVM Native Image, following the same migration mercury
already went through (MER-5) and using the exact same troubleshooting
method: run the real build, read GraalVM's/Spring's own error, fix exactly
that, repeat — no guessing, no copy-pasting mercury's own reflection hints
without re-deriving whether they even apply here.

**Result: the full application — Tomcat HTTPS, Hibernate/JPA, Redis, MCAM
RS256 keys, the real Spring Security filter chain — compiles to a native
executable with `--no-fallback` and boots successfully**, serving a real
HTTPS `403` from an authenticated endpoint end to end.

## What was added

- **`pom.xml`**: `java.version` 21→25; `com.umdc.commons/commons-services/
  security-oauth/persistence` bumped to `0.0.2`; `springdoc.version` bumped
  `2.6.0`→`3.1.0` (see "Real bugs found" below — not native-specific, a
  latent Spring Boot 4.1.1 incompatibility this migration exposed); a
  `native`/`nativeTest` Maven profile, identical in shape to mercury's own
  (activates the plumbing `spring-boot-starter-parent` already
  `pluginManagement`s, `--no-fallback` so a build that can't produce a true
  native binary fails loudly).
- **Migrated off the legacy Spring Cloud Bootstrap Context** —
  `spring-cloud-starter-bootstrap` removed; `bootstrap.yml` deleted, its
  entire content merged into `application.yml` with
  `spring.config.import: "optional:vault://,optional:configserver:"` added.
  Same root cause and fix as mercury: the legacy mechanism runs its own
  throwaway nested `SpringApplication` before the real one, and
  `SpringApplicationAotProcessor` captures whichever context is created
  first — silently AOT-processing the empty bootstrap context instead of
  the real application, with no error (a context genuinely did refresh
  successfully, just not the one that matters). Verified with the full
  `mvn test` gate (PMD + JaCoCo + JUnit) passing unchanged before and after.
- **`config/native.env`**: safe placeholder values for every undefaulted
  `${VAR}` in `application.yml`, plus several `@Value`-bound properties
  (`app.environments.contact.limit`, `messages.user.*`) that have no
  default anywhere in this repo's own resources — normally Vault/Config
  Server-supplied. Pattern and header comment copied from mercury's own
  file; values are backbone-rest-specific.
- **`src/main/java/com/umdc/backoffice/config/ThirdPartyNativeRuntimeHints.java`**:
  reflection/resource gaps found empirically (§ below).
- **`Dockerfile.native`**: same two-stage Debian-builder /
  distroless-runtime shape as mercury's. The PRX CA is supplied as a
  BuildKit secret; the `.jks` files use optional BuildKit secrets for clean
  checkouts and fall back to `certs/backbone/` when building locally with
  those files in the context.
- **`docker-compose.yml`**: unchanged — it already excludes Vault/Config
  Server from this container (`extra_hosts` mapping to their own containers
  on `nginx_umdc-net`, same as mercury's pattern), so no fix was needed there.

## Real bugs found (not native/AOT-specific — this migration just surfaced them)

1. **`springdoc.version` 2.6.0 incompatible with Spring Data 4.1.1.** Spring
   Data 4.1.1 relocated `TypeInformation` from `org.springframework.data.util`
   to `org.springframework.data.core`. springdoc 2.6.0's
   `QuerydslPredicateOperationCustomizer` (targets Spring Boot 3.x) still
   references the old package, and its class fails to even load
   (`NoClassDefFoundError`/`ClassNotFoundException`) the first time
   something reflectively introspects it. `mvn test` never caught this
   because no test in this suite loads a full `ApplicationContext` —
   `process-aot`'s real context refresh was the first thing that ever
   exercised this bean. Fixed by bumping to `3.1.0`, the version mercury's
   own pom.xml already carries for the same Boot/Data line. **This would
   also break a real `spring-boot:run`**, independent of native image.
2. **`spring-cloud-starter-netflix-eureka-client`'s `@RefreshScope`
   `eurekaClient` bean can't be AOT-code-generated** — Spring's
   `ValueCodeGenerator` has no support for `RefreshScope` as a bean
   definition value. A known Spring Cloud + AOT incompatibility, not
   backbone-specific; mercury hit and disabled the same thing.
   `EUREKA_CLIENT_ENABLED=false` in `config/native.env` for this
   build-time-only profile.
3. **Confirmed upstream regression** (spring-projects/spring-data-commons#3499):
   `process-aot`'s own generated repository sources fail to *compile* with
   `jakarta.validation.Constraint is not a repeatable annotation interface`
   for every JPA repository method with a Bean Validation annotation on a
   parameter. Same exact failure mercury documented. Spring Data's own
   documented escape hatch: `SPRING_AOT_REPOSITORIES_ENABLED=false` (falls
   back to normal runtime repository proxies — still fully native-image
   compatible, just without that one AOT optimization).

## Build-time-only config needed for a full `ApplicationContext` refresh

`process-aot` and the native binary both need Hibernate to actually build a
`SessionFactory` — this repo's `application.yml` sets
`hibernate.boot.allow_jdbc_metadata_access: true`, which makes Hibernate try
a real JDBC connection to auto-detect the dialect. Against an unreachable
`localhost:5432` (no live Postgres in this environment, deliberately — see
mercury's own file for why), that would either block for the connection
timeout or fail context refresh outright. `config/native.env` overrides
this build-time profile only:
- `SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT=-1` — per
  `DataSourceSslConfig`'s own documented escape hatch ("useful for tests
  where the DB may be intentionally unreachable during startup").
- `SPRING_JPA_PROPERTIES_HIBERNATE_BOOT_ALLOW_JDBC_METADATA_ACCESS=false`
  and an explicit `SPRING_JPA_DATABASE_PLATFORM` so Hibernate doesn't need
  metadata access to pick a dialect either.

Known non-fatal noise with this config: the smoke test below took ~30s to
start (vs. mercury's sub-2s) — HikariCP still makes exactly one blocking
connection attempt somewhere in Hibernate's bootstrap path despite the
above, exhausting the pool's 30s `connection-timeout` once before
proceeding. Real deployments have a reachable Supabase Postgres pooler, so
this delay is specific to this build-time smoke test, not a native-image
characteristic — not chased further here to avoid changing runtime
behavior as a side effect of a build-tooling migration.

## Reflection/resource gaps found and fixed

Each is the exact failure GraalVM/Spring named, found by running the
binary and reading the crash — none guessed. All registered in
`ThirdPartyNativeRuntimeHints.java`:

1. **`org.springframework.cloud.vault.config.VaultProperties`** —
   `NoSuchMethodException: VaultProperties.<init>()` despite a public
   no-arg constructor existing. `spring-cloud-vault-config` ships no
   GraalVM reachability metadata of its own. Identical gap mercury found
   for the same dependency.
2. **`backbone.jks` / `umdc-truststore.jks` as native-image resources** —
   these back `spring.ssl.bundle.jks.backbone-rest-security`, which
   Tomcat's HTTPS listener needs at runtime. Native-image doesn't embed
   arbitrary classpath resources by default; the classpath lookup failed
   with a plain `FileNotFoundException` (the files are real, on the
   classpath at build time — just never copied into the image). Fixed with
   `hints.resources().registerPattern("*.jks")`. Not something mercury
   needed (its keystore loading goes through a different, custom path), so
   this one's backbone-rest-specific.
3. **`com.umdc.backoffice.property.SecurityProperties` and its nested tree**
   (`ManagementAuthenticatorProperties`, `StoreProperties`) plus
   `KeystoreUtil` — the subtlest of the four, same shape as mercury's own
   third gap. Did **not** throw GraalVM's usual loud
   `MissingReflectionRegistrationError`; surfaced as a plain
   `NullPointerException` inside `ManagedClientTokenServiceImpl.init()`
   trying to load the MCAM RS256 keypair. A `@ConfigurationProperties`
   object several levels deep in a nested tree
   (`umdc.security.managementAuthenticator.keystore.*`) came back null
   under native-image's closed-world reflection, even though the outer
   levels of the same tree bound fine. Registering full reflective access
   (constructors, methods, fields) for the whole tree fixed it.
4. **JDK dynamic proxy for `jakarta.servlet.http.HttpServletRequest`** —
   `com.umdc.commons-services`' `requestBodyInterceptor` bean autowires a
   request-scoped `HttpServletRequest` into a singleton, resolved via a JDK
   dynamic proxy that needs explicit native-image registration. Identical
   gap mercury found for the same shared dependency.

## Testing & verification

**Not done, and why:** `mvn -Pnative test` (the `nativeTest` profile).
Mockito's default inline mock maker doesn't support native-image test
execution ([mockito/mockito#2435](https://github.com/mockito/mockito/issues/2435));
this suite uses it throughout. Wired for completeness per Spring Boot
convention; the JVM-mode `mvn test` remains the correctness gate
(unchanged pass, before and after every change in this migration).

**What was done instead:** a runtime smoke test of the actual compiled
native binary (Apple M4, macOS, `config/native.env`):
- `mvn -Pnative clean package native:compile` — `BUILD SUCCESS`, native
  executable produced (330MB, `--no-fallback`, so a true native binary,
  not a JVM-mode fallback).
- Started the binary directly: `Started UMDCBackofficeRestApplication in
  30.354 seconds` (see the Hikari note above for why this isn't sub-second
  like mercury's) — no crash, MCAM RS256 keys loaded, `SecurityFilterChain
  built successfully`, Tomcat HTTPS listener up on 8084.
- `curl -k https://localhost:8084/actuator/health` → real `403 Forbidden`
  over real TLS (the checked-in dev `backbone.jks`) — confirms Tomcat, the
  full Spring Security filter chain, and the entire real bean graph are
  live in the native binary, not a JVM-mode process.

**Not done:** an actual `docker build -f Dockerfile.native`. The Dockerfile
mirrors mercury's own (same builder-base rationale, same distroless
runtime, same libz staging step) and downloads its GraalVM tarball from a
URL confirmed to resolve (`download.oracle.com/graalvm/25/archive/...`),
but building the image itself needs a real BuildKit secret for Repsy Maven
credentials this environment doesn't have configured, and takes
significantly longer than the ~5 minute local build measured above.
Recommend one real CI/deployment build as the actual end-to-end
confirmation, same caveat mercury's own doc states for the equivalent step.
