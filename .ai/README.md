# .ai — Gemma / Gemini Agent Workspace for backbone-rest

This directory mirrors the AI collaboration assets defined in `.github/` so they
can be consumed by **Gemma / Gemini CLI** (or any Google-AI-based agent runner)
without disturbing the GitHub Copilot configuration.

> **Source of truth:** `.github/` is authoritative. Files in `.ai/` are
> structural copies kept in sync manually. Do **not** edit only one side —
> propagate changes to both, or re-mirror from `.github/`.

## Directory Layout

```
.ai/
├── GEMINI.md                 # Gemma entry-point (project context + index)
├── README.md                 # this file
├── agents/                   # specialized AI coworkers (developer, test-writer, ...)
│   ├── agents.md             # index of available agents
│   └── *.agent.md            # one file per agent
├── skills/                   # reusable capability definitions
│   ├── skills.md             # index of available skills
│   ├── *.skill.md            # standalone skills
│   └── <agent-name>/SKILL.md # per-agent skill manifests
├── prompts/                  # invocable prompt templates / slash-command bodies
│   ├── prompts.md            # index of available prompts
│   └── *.prompt.md           # one file per prompt
├── hooks/                    # lifecycle gates (pre-PR, post-merge, release)
│   ├── hooks.md              # index of available hooks
│   └── *.hook.md             # one file per hook
├── tools/                    # tool integration contracts (mvn, gh, pmd, docker, ...)
│   ├── tools.md              # index of available tools
│   └── *.tool.md             # one file per tool
└── workflows/                # multi-step orchestrations (feature, release, audit)
    ├── workflows.md          # index of available workflows
    └── *.workflow.md         # one file per workflow
```

## How Gemma Should Use This

1. **Bootstrap** — Read `GEMINI.md` first; it carries project context and the
   index of every asset in this directory.
2. **Agent dispatch** — When a user asks for help that maps to a role
   (developer, test-writer, security-reviewer, ...), load the matching
   `agents/<role>.agent.md` and follow its system prompt.
3. **Skills** — Each agent's persona may reference skills under
   `skills/<agent-name>/SKILL.md` or the standalone `skills/*.skill.md` files.
4. **Prompts** — Treat `prompts/*.prompt.md` as invocable templates (e.g. the
   user typing `/add-endpoint` should load `prompts/add-endpoint.prompt.md`).
5. **Workflows** — Multi-agent processes (feature development, release prep,
   security audit) are scripted under `workflows/*.workflow.md`.
6. **Hooks** — Apply `hooks/*.hook.md` at the lifecycle moments they name
   (e.g. `pre-pull-request.hook.md` runs before opening a PR).
7. **Tools** — `tools/*.tool.md` documents the external commands an agent may
   invoke (Maven, PMD, GitHub CLI, Docker, etc.) and their guard-rails.

## Project Conventions (Recap)

All assets here assume the **backbone-rest** rules already enforced in
`CLAUDE.md` / `AGENTS.md` / `.github/copilot-instructions.md`:

- Java 21 / Spring Boot 3.4 / Maven (no `mvnw`)
- Interface-first controllers (`*Api` + `*Controller`)
- Services return `ResponseEntity<?>`
- Constructor injection only — never `@Autowired` fields
- MapStruct with `MapperAppConfig`
- PMD ruleset (`ruleset.xml`) zero-violation policy
- OpenAPI 3.1 (`src/main/resources/META-INF/api.yaml`) is the API contract
- Supabase JWT auth on all `/api/v1/**` reads

## Sync With `.github/`

To re-sync `.ai/` from `.github/` (overwrites local changes):

```bash
rm -rf .ai/agents .ai/skills .ai/prompts .ai/hooks .ai/tools .ai/workflows
cp -R .github/agents   .ai/agents
cp -R .github/skills   .ai/skills
cp -R .github/prompts  .ai/prompts
cp -R .github/hooks    .ai/hooks
cp -R .github/tools    .ai/tools
cp -R .github/workflows .ai/workflows
find .ai -name ".DS_Store" -delete
```
