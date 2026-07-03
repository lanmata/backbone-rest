# Skills Catalog — backbone-rest (Claude)

## Shared Skills (used by 2+ agents)

| File | Used By |
|------|---------|
| `rest-api-design.skill.md` | java-developer, api-designer, product-owner |
| `jpa-persistence.skill.md` | java-developer, database-architect |
| `release-management.skill.md` | project-manager, devops-engineer |

## Agent-Specific Skills (`<agent>/SKILL.md`)

| Folder | Agent | Key Coverage |
|--------|-------|-------------|
| `orchestrator/` | Orchestrator | Task decomposition, delegation map, progress tracking, final report |
| `java-developer/` | Java Developer | Java 21 patterns, interface-first, MapStruct, OAuth2, PMD, build |
| `test-writer/` | Test Writer | JUnit 5, Mockito, `@WebMvcTest`, `MockLoaderBase`, JaCoCo |
| `code-reviewer/` | Code Reviewer | PMD ruleset, convention compliance, severity classification |
| `security-auditor/` | Security Auditor | OWASP Top 10, JWT/OAuth2 review, Supabase auth, CVE, secrets |
| `api-designer/` | API Designer | OpenAPI 3.1, `*Api.java` annotations, REST contract, backward compat |
| `supabase-integrator/` | Supabase Integrator | Storage bucket ops, signed URLs, profile image, RLS |
| `devops-engineer/` | DevOps Engineer | Maven build, Docker, Repsy, CI/CD, artifact packaging |
| `product-owner/` | Product Owner | Domain knowledge, acceptance criteria, story template |
| `project-manager/` | Project Manager | Quality gates, release checklist, risk register, delivery plan |
| `repo-requirements-analyst/` | Repo Analyst | Codebase pattern discovery, gap analysis, canonical reference |
| `database-architect/` | Database Architect | JPA, Spring Data, pooler constraints, H2 compat, migrations |
