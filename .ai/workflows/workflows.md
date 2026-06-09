# AI Agent Workflows

This folder describes workflows that orchestrate AI agents for common development tasks in **backbone-rest**. Workflows are documented as guidance for automation and human-in-the-loop steps.

## Workflows Available

| File                                    | Workflow Name        | Trigger       | Agents Involved                                        |
|-----------------------------------------|----------------------|---------------|--------------------------------------------------------|
| `feature-development.workflow.md`       | Feature Development  | Manual        | Product Owner, Developer, QA / Test Writer, API Reviewer, Code Reviewer |
| `bug-fix.workflow.md`                   | Bug Fix              | Manual        | Developer, QA / Test Writer, Code Reviewer             |
| `release.workflow.md`                   | Release              | Manual        | Project Manager, Security Reviewer, API Reviewer, Developer, DevOps Engineer |
| `security-audit.workflow.md`            | Security Audit       | Manual        | Security Reviewer, Developer, Project Manager          |
| `code-review.workflow.md`               | Code Review          | Pull Request  | Code Reviewer, API Reviewer, Security Reviewer         |
| `coverage-improvement.workflow.md`      | Coverage Improvement | Manual        | QA / Test Writer, Developer, Project Manager           |

## Key Gates Per Workflow

- **Feature Development**: `mvn test` green + backbone_rest-openapi.yaml updated
- **Bug Fix**: regression test added + `mvn test` green
- **Release**: `mvn test` + CVE scan + API contract review + CHANGELOG updated
- **Security Audit**: CVE scan + OWASP review + plain-text password check
- **Code Review**: PMD clean + convention compliance + `mvn test` green
- **Coverage Improvement**: JaCoCo report reviewed + new tests PMD-compliant
