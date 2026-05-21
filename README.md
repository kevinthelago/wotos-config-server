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

The config server listens on port `4040`. Microservices reference it via their `bootstrap.yml`:

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:4040
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
