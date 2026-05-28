# WoToS Config Server

Spring Cloud Config Server for the [WoToS](https://github.com/users/kevinthelago/projects/2) system. Reads `.properties` files from the [`wotos-config`](https://github.com/kevinthelago/wotos-config) repository and serves them to all WoToS microservices at startup.

**This service must be started after `wotos-eureka-server` and before all business services.**

## Prerequisites

- Java 8 (Temurin recommended)
- Maven or the included `./mvnw` wrapper
- `wotos-eureka-server` running at `localhost:8761`
- The `wotos-config` repository cloned and reachable (the config server reads from it as a local git repo or remote URI)

## Running Locally

```bash
./mvnw spring-boot:run
```

The config server listens on port `4040` and is protected by HTTP Basic auth (see
[Security](#security)). Microservices reference it via their `bootstrap.yml`,
supplying the same credentials:

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:4040
      username: ${CONFIG_SERVER_USERNAME:config}
      password: ${CONFIG_SERVER_PASSWORD:local-dev-only}
```

## Security

The config server serves every microservice's configuration — including any
decrypted secrets — so all HTTP endpoints require authentication. Only
`/actuator/health` is left open for liveness probes.

Configuration is sourced entirely from environment variables; no secrets are
committed to this repository. In CI these come from GitHub Actions secrets and in
production from the cloud secret manager. The local-development defaults are **not
secrets** and must be overridden everywhere else.

| Variable | Purpose | Default (local dev only) |
|---|---|---|
| `CONFIG_SERVER_USERNAME` | HTTP Basic username clients authenticate with | `config` |
| `CONFIG_SERVER_PASSWORD` | HTTP Basic password clients authenticate with | `local-dev-only` |
| `CONFIG_SERVER_PORT` | Listen port | `4040` |
| `CONFIG_GIT_URI` | Backing config repository URI | public `wotos-config` repo |
| `CONFIG_GIT_USERNAME` / `CONFIG_GIT_PASSWORD` | Credentials for a private backing repo | _(unset)_ |
| `ENCRYPT_KEY` | Symmetric key enabling config encryption | _(unset — encryption disabled)_ |

### Encrypting secrets

Set `ENCRYPT_KEY` to a strong symmetric key to enable the `/encrypt` and
`/decrypt` endpoints. Downstream services then store sensitive values (DB
credentials, the WoT API key, etc.) as `{cipher}...` entries in the
[`wotos-config`](https://github.com/kevinthelago/wotos-config) repo, and the
config server decrypts them before serving:

```bash
export ENCRYPT_KEY='<strong-random-key>'
curl -u "$CONFIG_SERVER_USERNAME:$CONFIG_SERVER_PASSWORD" \
  http://localhost:4040/encrypt -d 'my-secret-value'
```

## Building

```bash
./mvnw clean package
./mvnw clean install
```

## Adding Configuration for a New Service

1. Create a new `.properties` file in [`wotos-config`](https://github.com/kevinthelago/wotos-config) named after the service (e.g. `wotos-new-service.properties`).
2. Add `server.port`, `server.host`, and any service-specific properties.
3. The config server will serve the file automatically — no restart required if `@RefreshScope` is used on the consuming beans.
