---
name: Docker Build
description: Tool for building and running the backbone-rest Docker image
type: terminal
command-prefix: docker
used-by:
  - devops-engineer
---

# Docker Build Tool

## Purpose

Build and run the **backbone-rest** container image. The image uses `amazoncorretto:21-alpine3.20` and exposes port 8082.

## Available Commands

### Build
```bash
# Build image (tagged with version)
docker build -t backbone-rest:<version> .

# Build with latest tag
docker build -t backbone-rest:latest .

# Build with no cache (clean rebuild)
docker build --no-cache -t backbone-rest:latest .
```

### Run
```bash
# Run with Supabase remote profile
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=remote-supabase \
  -e AUTH_SERVER_URI=<supabase-auth-url> \
  -e REPSY_ACCOUNT_USER=<user> \
  -e REPSY_ACCOUNT_PASSWORD=<password> \
  backbone-rest:latest

# Run with environment file
docker run -p 8082:8082 --env-file default.env backbone-rest:latest
```

### Inspect
```bash
# List images
docker images | grep backbone-rest

# Check running containers
docker ps

# View logs
docker logs <container-id>

# Container shell
docker exec -it <container-id> /bin/sh
```

## Output Locations

| Artifact | Description |
|----------|-------------|
| Image | `backbone-rest:<version>` in local Docker daemon |
| JAR inside image | `/app/backbone-rest.jar` (see Dockerfile) |

## Notes

- Base image: `amazoncorretto:21-alpine3.20`
- Exposed port: `8082`
- Dockerfile at: `Dockerfile` (repo root)
- JAR source: `target/backbone-rest.jar` (must be built first via `mvn -DskipTests package`)
- Never put real secrets in `default.env` — it is committed. Use env vars at runtime.
