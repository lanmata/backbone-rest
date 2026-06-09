---
name: Release Management
description: Shared - release process and versioning (Project Manager, DevOps Engineer)
applies-to: [Project Manager, DevOps Engineer]
---
# Release Management
## Release Checklist
- [ ] mvn test passes (0 failures, 0 PMD violations)
- [ ] backbone_rest-openapi.yaml updated for any API changes
- [ ] CHANGELOG updated at project root
- [ ] No Critical/High CVEs (Security Reviewer sign-off)
- [ ] Docker image built and pushed
- [ ] Git tag created: v{MAJOR}.{MINOR}.{PATCH}
## Versioning: PATCH=bugfix, MINOR=feature, MAJOR=breaking (stakeholder approval)
## Commands
```bash
mvn test && mvn -DskipTests package
docker build -t lamata/backbone-rest:X.Y.Z .
git tag -a vX.Y.Z -m "Release vX.Y.Z" && git push origin vX.Y.Z
```
