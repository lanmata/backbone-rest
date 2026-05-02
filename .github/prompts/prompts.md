# Prompts Catalog

Reusable prompt templates for repetitive agent tasks in **backbone-rest**.
Each file uses `${variable}` placeholders — replace before invoking.

## Prompts by Agent

| File | Name | Agent | Mode | Trigger |
|------|------|-------|------|---------|
| `add-endpoint.prompt.md` | Add REST Endpoint | developer | agent | New feature / story |
| `fix-bug.prompt.md` | Fix Bug | developer | agent | Bug report |
| `fix-pmd-violations.prompt.md` | Fix PMD Violations | developer / code-reviewer | agent | PMD failure |
| `write-service-tests.prompt.md` | Write Service Tests | test-writer | agent | New service or missing tests |
| `improve-coverage.prompt.md` | Improve Test Coverage | test-writer | agent | Low JaCoCo coverage |
| `review-api-contract.prompt.md` | Review API Contract | api-reviewer | agent | API change / PR |
| `review-pull-request.prompt.md` | Review Pull Request | code-reviewer | agent | PR / code change |
| `security-audit.prompt.md` | Security Audit | security-reviewer | agent | Pre-release / dependency change |
| `prepare-release.prompt.md` | Prepare Release | devops-engineer | agent | Release tag creation |
| `define-story.prompt.md` | Define User Story | product-owner | ask | New feature request |
| `full-feature-delivery.prompt.md` | Full Feature Delivery | orchestrator | agent | Complex feature requiring all agents |
| `bootstrap-agent-infrastructure.prompt.md` | Bootstrap Agent Infrastructure | orchestrator | agent | **Reusable across any project** — generate the entire agent infrastructure from scratch |

## Usage

### From Copilot Chat
```
@agent /prompt <prompt-file-name> domain=users method=GET path="{userId}" ...
```

### Variables Pattern
All prompts use `${variableName}` for required inputs.
If a variable is omitted, the agent will infer it from context or ask.

## Prompt → Hook Connections

| Hook | Prompts Used |
|------|-------------|
| `pre-pull-request.hook.md` | `review-pull-request`, `review-api-contract` |
| `post-merge-security.hook.md` | `security-audit` |
| `pre-release-gate.hook.md` | `security-audit`, `review-api-contract`, `prepare-release` |
| `post-implementation-review.hook.md` | `write-service-tests`, `improve-coverage`, `review-api-contract`, `review-pull-request` |

