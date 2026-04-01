---
name: GitHub CLI
description: Tool for GitHub operations via the gh CLI
type: terminal
command-prefix: gh
---

# GitHub CLI Tool

## Purpose

Manage GitHub issues, pull requests, and releases for the **backbone-rest** repository.

## Available Commands

### Issues

```bash
# List open issues
gh issue list --state open

# Create issue
gh issue create --title "Title" --body "Description" --label "bug"

# View issue
gh issue view 42

# Close issue
gh issue close 42
```

### Pull Requests

```bash
# List PRs
gh pr list

# Create PR
gh pr create --title "Feature: ..." --body "Description" --base main

# View PR status
gh pr status

# Review PR
gh pr review 42 --approve

# Merge PR
gh pr merge 42 --merge
```

### Releases

```bash
# Create release
gh release create v0.0.2 --title "v0.0.2" --notes "Release notes"

# List releases
gh release list

# Upload artifact
gh release upload v0.0.2 target/backbone-rest.jar
```

## Notes

- There are **no GitHub Actions CI workflows** in this repository yet.
- The `gh run` commands are available but there are no workflow runs to inspect.
- Contact the **DevOps Engineer** agent to set up GitHub Actions pipelines.
