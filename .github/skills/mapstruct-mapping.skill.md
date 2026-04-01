---
name: MapStruct Mapping
description: Skill for MapStruct-based DTO-entity mapping
applies-to:
  - Developer
---

# MapStruct Mapping Skill

## Scope

This skill covers MapStruct mapper patterns used to convert between JPA entities
and DTOs in the **backbone-rest** project.

## Mapper Configuration

All mappers use the shared config from `com.prx.commons.services.config.mapper.MapperAppConfig`:

```java
@Mapper(config = MapperAppConfig.class, uses = {RoleMapper.class, PersonMapper.class, ApplicationRoleUserMapper.class})
public interface UserMapper {
    @Mapping(target = "roles", source = "applicationRoleUser")
    @Mapping(target = "applications", source = "applicationRoleUser")
    UserTO toTarget(UserEntity userEntity);

    @Mapping(target = "applicationRoleUser", expression = "java(ApplicationRoleUserMapper.getApplicationRoleUser(user))")
    UserEntity toSource(UserTO user);

    UserEntity toSource(UserCreateRequest userCreateRequest);

    @Mapping(target = "applicationId", expression = "java(getApplicationId(userEntity))")
    @Mapping(target = "roleId", expression = "java(getRoleId(userEntity))")
    UserCreateResponse toUserCreateResponse(UserEntity userEntity);
}
```

## Current Mappers (`com.prx.backoffice.v1.<domain>.mapper`)

| Domain        | Mapper Interface                 | Notes                                                   |
|---------------|----------------------------------|---------------------------------------------------------|
| `users`       | `UserMapper`                     | Handles `applicationRoleUser` → `roles`/`applications`  |
| `users`       | `ApplicationRoleUserMapper`      | Includes static `getApplicationRoleUser` helper         |
| `roles`       | `RoleMapper`                     |                                                         |
| `people`      | `PersonMapper`                   |                                                         |
| `session`     | `UserAliasMapper`                |                                                         |

## Expression Mappings

Use `expression = "java(...)"` for complex transformations that can't be expressed as field paths:

```java
@Mapping(target = "applicationId", expression = "java(getApplicationId(userEntity))")
UserCreateResponse toUserCreateResponse(UserEntity userEntity);

default UUID getApplicationId(UserEntity userEntity) {
    return userEntity.getApplicationRoleUser().stream()
        .map(aru -> aru.getApplication().getId())
        .findFirst().orElse(null);
}
```

## Conventions

- Mapper interfaces live in `com.prx.backoffice.v1.<domain>.mapper`.
- MapStruct generates implementations at compile time via `maven-compiler-plugin` annotation processor (version in `pom.xml`).
- Use `@Mapping` annotations for field name mismatches.
- Use `default` methods for multi-step transformations (e.g., `getRoleId`, `getApplicationId`).
- Never use MapStruct for entity-to-entity — only entity↔DTO.
- Annotation processor processes both `mapstruct` and `mapstruct-processor` from `pom.xml`.
