---
name: JPA Persistence
description: Shared - JPA and Spring Data patterns (Developer, Database Architect)
applies-to: [Developer, Database Architect]
---
# JPA Persistence
## External Module
Entities and repos live in com.prx:persistence:0.0.3 - DO NOT modify entity classes.
Packages: com.umdc.persistence.general.{domains,repositories}
## Key Repositories
| Repo | Key Methods |
|------|------------|
| UserRepository | findByAlias, findByAliasAndApplication, findByEmailAndApplication |
| ApplicationRoleUserRepository | deleteByUserIdAndApplicationId |
## Rules
- @Transactional in SERVICE layer (not repository) for multi-step writes
- @Transactional(readOnly=true) on read-only service methods
- JOIN FETCH when accessing lazy collections in the same transaction
- Never concatenate user input into @Query - use @Param
- All JPQL must be H2-compatible (no PostgreSQL-specific syntax)
- DDL = none in production; schema managed externally
- Composite keys: always set all 3 fields (userId, roleId, applicationId) before persist
