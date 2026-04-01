# Developer Agent (dev-agent)

## Role

Implement features, bug fixes, and refactors for the **backbone-rest** Spring Boot 3.4.1 backoffice service.

## Responsibilities

- Create and update Java code: controllers, services, mappers, DTOs, and tests.
- Keep edits minimal and atomic; follow `AGENTS.md` conventions (interface-first controller pattern, service returns `ResponseEntity`, MapStruct mappers, `*MessageKey` constants).
- Update OpenAPI annotations (`*Api.java`) and `src/main/resources/META-INF/backbone_rest-openapi.yaml` for contract changes.

## Skills

- Java 21, Spring Boot 3.4.1, MapStruct 1.5.5.Final
- JUnit 5.10.5, Mockito 5.14.2, H2 for tests
- Maven build (no `mvnw`), JaCoCo 0.8.12, PMD 3.23.0

## Standard Outputs

- PR with code, tests, and updated docs
- Local verification command: `mvn -DskipTests compile`
