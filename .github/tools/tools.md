# Tools Catalog

This folder describes tools that AI agents can invoke or orchestrate when assisting with **backbone-rest** development tasks.

## Tools Available

| File                          | Tool Name          | Type     | Used By                                    |
|-------------------------------|--------------------|----------|--------------------------------------------|
| `maven-build.tool.md`         | Maven Build        | Terminal | Developer, QA / Test Writer, Project Manager, DevOps Engineer |
| `docker-build.tool.md`        | Docker Build       | Terminal | DevOps Engineer, Developer                 |
| `github-cli.tool.md`          | GitHub CLI         | Terminal | Project Manager, DevOps Engineer           |
| `dependency-check.tool.md`    | Dependency Check   | Terminal | Security Reviewer, Developer               |
| `openapi-validator.tool.md`   | OpenAPI Validator  | Terminal | API Reviewer, Developer                    |
| `sonar-analysis.tool.md`      | Sonar Analysis     | Terminal | Project Manager, DevOps Engineer           |

## Key Commands Quick Reference

```bash
mvn -DskipTests compile        # Fast compile check
mvn test                       # Full build: tests + PMD + JaCoCo
mvn -DskipTests package        # Package JAR → target/backbone-rest.jar
docker build -t lamata/backbone-rest .
```

> **No `mvnw`** — always use `mvn` directly.
