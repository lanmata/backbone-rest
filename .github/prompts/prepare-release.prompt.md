---
name: Prepare Release
description: Execute the full release checklist — quality gates, versioning, tagging, and CHANGELOG update
mode: agent
agent: devops-engineer
tools: [run_in_terminal, read_file, replace_string_in_file, grep_search, file_search]
---

Prepare and execute a release for **backbone-rest**.

## Release Parameters

- Version: ${version}
  _(e.g., `1.2.0`)_
- Release notes summary: ${notes}

## Release Checklist

Execute in order — stop and report immediately on any failure:

### 1. Quality Gates

```bash
mvn test                      # tests + PMD + JaCoCo — must exit 0
mvn pmd:check pmd:cpd-check   # explicit PMD gate
```

### 2. Verify No Critical CVEs

```bash
mvn dependency:tree
mvn versions:display-dependency-updates
```
If critical/high CVEs found → block release, report to security-reviewer.

### 3. Verify OpenAPI Spec is Updated

```bash
grep -c "operationId:" src/main/resources/META-INF/backbone_rest-openapi.yaml
# Compare against number of @*Mapping methods in *Api.java files
grep -rn '@\(Get\|Post\|Put\|Patch\|Delete\)Mapping' \
  src/main/java/com/prx/backoffice/v1/*/api/controller/*Api.java | wc -l
```

### 4. Update CHANGELOG

Add entry at the top of `CHANGELOG`:
```markdown
## [${version}] - $(date +%Y-%m-%d)
### Added / Fixed / Changed / Security
${notes}
```

### 5. Package and Build Docker Image

```bash
mvn -DskipTests package
docker build -t lamata/backbone-rest:${version} .
docker build -t lamata/backbone-rest:latest .
```

### 6. Tag and Release

```bash
git tag -a v${version} -m "Release v${version}"
git push origin v${version}
gh release create v${version} \
  --title "v${version}" \
  --notes "${notes}"
gh release upload v${version} target/backbone-rest.jar
```

### 7. Push Docker Image

```bash
docker push lamata/backbone-rest:${version}
docker push lamata/backbone-rest:latest
```

## Output Format

```
Release: v${version}
Quality gate: PASS / FAIL
CVEs: NONE / list
CHANGELOG: UPDATED
Docker: lamata/backbone-rest:${version} PUSHED
Git tag: v${version} PUSHED
GitHub release: CREATED
```

