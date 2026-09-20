# 🚀 Deploying Backbone REST Container

This guide provides a comprehensive breakdown of the command used to spin up the `backbone-rest` containerized service. Follow these instructions to ensure your local or staging environment is correctly configured.

## 📜 The Deployment Command

Execute the following command to launch the container in detached mode:

```bash
docker run -d -ti \
  --network nginx_umdc-net \
  --link umdc-nginx:umdc-qa.tst \
  --link umdc-nginx:config-server.umdc-qa.tst \
  --link umdc-nginx:monitor.umdc-qa.tst \
  -p 8084:8084 \
  --name backbone-rest \
  -e CNFS_PORT=443 \
  -e CNFS_URI=https://config-server.umdc-qa.tst \
  -e SPRING_BOOT_CLOUD_BOOTSTRAP_ENABLED=true \
  -e SPRING_BOOT_PROFILE_AUTHENTICATED=remote-supabase \
  -e SPRING_CLOUD_CONFIG_LABEL=Develop \
  -e INFISICAL_ENABLED=true \
  -e INFISICAL_SITE_URL=https://app.infisical.com \
  -e INFISICAL_CLIENT_ID=afd66433-f83c-4ab4-be7b-fe0616db368a \
  -e INFISICAL_CLIENT_SECRET=8b65f5b53c2b9dce3abe8d01e7a07a82b5784c1cff3a7dcfcaaa98abfbace189 \
  -e INFISICAL_PROJECT_ID=953e3afa-2cd8-4698-b62b-46cc372e0bc0 \
  -e INFISICAL_ENVIRONMENT=dev \
  -e INFISICAL_SECRET_PATH=/ \
  -e LOGGING_TRACE_ENABLED=true \
  -e APP_PORT=8084 \
  -e SSL_KEYSTORE_LOCATION=backbone.jks \
  -e SSL_KEYSHEET_PASSWORD=changeit \
  -e SSL_KEYSTORE_TYPE=JKS \
  -e SSL_TRUSTSTORE_LOCATION=umdc-truststore.jks \
  -e SSL_TRUSTSTORE_PASSWORD=changeit \
  lamata/backbone-rest:0.0.4
```

---

## 🔍 Deep Dive Breakdown

### 🏗 Infrastructure & Networking
These parameters define how the container interacts with your existing Docker ecosystem.

| Flag | Value | Description |
| :--- | :--- | :--- |
| `--network` | `nginx_umdc-net` | Connects this container to the pre-existing `nginx_umdc-net` network, allowing communication with Nginx and other services. |
| `--link` | `umdc-nginx:umdc-qa.tst` | Creates a network alias so that inside the container, code can resolve `umdc-qa.tst` to the `umdc-nginx` container. |
| `--link` | `...config-server...` | Maps the configuration server to a resolvable hostname within the internal network. |
| `--link` | `...monitor...` | Maps the monitoring service to a resolvable hostname. |

### 🛠 Container Configuration
Core Docker settings for the container instance.

*   `-d`: **Detached Mode**. Runs the container in the background.
*   `-ti`: **Interactive & TTY**. Allocates a pseudo-TTY, useful for debugging and maintaining an interactive session if needed.
*   `-p 8084:808attend`: **Port Mapping**. Maps host port `8084` to container port `8084`.
*   `--name backbone-rest`: Assigns a human-readable name to the container for easy management via `docker stop`, `docker logs`, etc.

### ⚙️ Environment Variables (`-e`)

The application logic is driven by several groups of environment variables:

#### ☁️ Spring Cloud Config
Configures how the app fetches its remote configuration on startup.
*   `CNFS_PORT`: The port used by the Config Server (443).
*   `CNFS_URI`: The endpoint for the remote configuration server.
*   `SPRING_BOOT_CLOUD_BOOTSTRAP_ENABLED`: Enables early bootstrapping of Spring Cloud context.
*   `SPRING_CLOUD_CONFIG_LABEL`: Specifies the Git branch or tag (`Develop`) to pull configurations from.

#### 🔐 Secrets Management (Infisical)
Controls integration with **Infisical** for secure, centralized secret storage.
*   `INFISICAL_ENABLED`: Toggles Infisical integration.
*   `INFISICAL_CLIENT_ID` / `SECRET`: Credentials used to authenticate with the Infisical API.
*   `INFISICAL_PROJECT_ID`: Identifies the specific project in Infisical.
*   `INFISICAL_ENVIRONMENT`: Sets the target environment (e.g., `dev`).
*   `INFISICAL_SECRET_PATH`: The root path within Infisical to scan for secrets.

#### 🔒 SSL/TLS Security
Defines the parameters for encrypted communication using Java KeyStores.
*   `SSL_KEYSTORE_LOCATION`: Path to the `.jks` file containing the server's private key and certificate.
*   `SSL_KEYSTORE_PASSWORD`: The password to access the keystore.
*   `SSL_TRUSTSTORE_LOCATION`: Path to the `.jks` file containing trusted CA certificates.
*   `SSL_TRUSTSTORE_PASSWORD`: The password to access the truststore.

#### 📝 Logging & App Settings
*   `LOGGING_TRACE_ENABLED`: When `true`, enables verbose trace-level logging for debugging extreme edge cases.
*   `APP_PORT`: Informs the Spring Boot application which port it should listen on internally.
*   `SPRING_BOOT_PROFILE_ACTIVE`: Sets the active Spring profile (`remote-supabase`).

---

## 📦 Image Reference

**Image Name:** `lamata/backbone-rest`  
**Version/Tag:** `0.0.4`

> [!IMPORTANT]
> Ensure you have pulled the latest version of the image before running this command to avoid "image not found" errors.
