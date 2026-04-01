---
name: Docker Build
description: Tool for building and managing Docker images
type: terminal
command-prefix: docker
---

# Docker Build Tool

## Purpose

Build, run, and manage Docker images for the **backbone-rest** microservice.

## Image Details

- **Base image**: `amazoncorretto:21-alpine3.20`
- **Exposed port**: `8082`
- **Published image**: `lamata/backbone-rest`
- **Certificates**: `prx-qa.crt`, `prx-qa.manager.crt`, `srmn.crt`, `prx-qa.config-server.crt` are imported into JVM truststore at build time

## Available Commands

### Build

```bash
# Package JAR first
mvn -DskipTests package

# Build image (certs and keystore.jks must be present at project root)
docker build -t lamata/backbone-rest:latest .

# Build with specific version tag
docker build -t lamata/backbone-rest:0.0.2 .
```

### Push / Pull

```bash
# Push to registry
docker push lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]

# Pull from registry
docker pull lamata/backbone-rest -u [USER_REGISTRY] -p [TOKEN]
```

### Run

```bash
# Run container (port 8082)
docker run -p 8082:8082 --env-file default.env \
  -e JAR_FILE=backbone-rest.jar \
  lamata/backbone-rest:latest

# Run in background
docker run -d -p 8082:8082 --env-file default.env \
  -e JAR_FILE=backbone-rest.jar \
  --name backbone-rest lamata/backbone-rest:latest

# View logs
docker logs -f backbone-rest
```

### Inspect / Clean

```bash
# List images
docker images | grep backbone-rest

# Stop and remove container
docker stop backbone-rest && docker rm backbone-rest

# Remove image
docker rmi lamata/backbone-rest:latest
```

## Notes

- `default.env` contains only stub values — supply real env vars at runtime.
- The `Dockerfile` imports SSL certificates into the JVM cacerts truststore using `keytool` — ensure cert files are present at build root.
- Non-root user `jvapps:appmng` runs the application for security.
- Profile images directory (`/opt/images/LTHB`) is created inside the container.
