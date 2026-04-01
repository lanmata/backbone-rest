---
name: JPA Persistence
description: Skill for Spring Data JPA, entity access, and repository patterns
applies-to:
  - Developer
  - Database Architect
---

# JPA Persistence Skill

## Scope

This skill covers Spring Data JPA patterns and repository usage in the **backbone-rest** project.

> **Critical constraint**: JPA entities and repositories live in the **external** `com.prx:persistence:0.0.3` module (`com.prx.persistence.general.domains` / `com.prx.persistence.general.repositories`). Do NOT add entity or repository classes to this repo.

## Key Entities (`com.prx.persistence.general.domains`)

| Entity                          | Purpose                                                |
|---------------------------------|--------------------------------------------------------|
| `UserEntity`                    | User profiles (alias, password, email, active)         |
| `ApplicationEntity`             | Application registrations                              |
| `RoleEntity`                    | Role definitions                                       |
| `ApplicationRoleUserEntity`     | Three-way join (composite key `ApplicationRoleUserEntityId`) |
| `PersonEntity`                  | Person details (firstName, lastName, birthdate, gender) |
| `ContactEntity`                 | Contact data                                           |
| `ContactTypeEntity`             | Contact type enumeration                               |

## Key Repositories (`com.prx.persistence.general.repositories`)

| Repository                        | Notable Methods                                                     |
|-----------------------------------|---------------------------------------------------------------------|
| `UserRepository`                  | `findByAlias`, `findByAliasAndApplication`, `findByEmailAndApplication`, `findByApplication`, `findUserInfo` |
| `ApplicationRepository`           | Standard `JpaRepository<ApplicationEntity, UUID>`                   |
| `RoleRepository`                  | Standard `JpaRepository<RoleEntity, UUID>`                          |
| `ApplicationRoleUserRepository`   | `deleteByUserIdAndApplicationId(UUID, UUID)`                        |

## Composite Key Pattern

`ApplicationRoleUserEntity` uses `@EmbeddedId` (`ApplicationRoleUserEntityId`) with `userId`, `roleId`, `applicationId` fields. Always set all three before persisting:

```java
var id = new ApplicationRoleUserEntityId();
id.setUserId(user.getId());
id.setRoleId(role.getId());
id.setApplicationId(application.getId());

var aru = new ApplicationRoleUserEntity();
aru.setId(id);
aru.setUser(userEntity);
aru.setRole(roleEntity);
aru.setApplication(applicationEntity);
aru.setActive(true);
```

## Repository Usage in Services

Inject repositories via constructor:
```java
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    // ...
    public UserServiceImpl(UserRepository userRepository, ...) {
        this.userRepository = userRepository;
        // ...
    }
}
```

## Transaction Management

Use `@Transactional` on service methods that perform multiple write operations:
```java
@Transactional
@Override
public ResponseEntity<UserTO> update(UUID userId, UserTO user) {
    // multi-step update including applicationRoleUserRepository changes
}
```

## Test Profile

Tests use H2 in-memory with `spring.jpa.hibernate.ddl-auto=none` (schema is pre-loaded). See `src/test/resources/application-test.yml`.
