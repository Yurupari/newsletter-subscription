# AGENTS.md — Guidance for automated coding/operational agents

Purpose: Give AI agents the minimal, actionable knowledge to build, run, and inspect the Newsletter Subscription project.

Quick plan for agents
- Bring up infra (PostgreSQL, Kafka, Prometheus, Grafana, KeyCloak, Mailpit, Kafka UI)
- Build or run a single service locally

Quick start (infra)
- From repo root: `docker-compose -f docker-compose-local.yaml up -d` (uses `docker-compose-local.yaml`)
- Stop: `docker-compose down`
- If DB issues occur: remove/recreate volumes or re-run `docker/postgres/init.sql`

Build / run a service
- Build: `./gradlew build`
- Build each service `./gradlew :<service>:build`

Architecture & key components
- Microservices (top-level dirs): `api-gateway`, `subscription-service`, `user-service`.
- Stack: **Spring Boot 4.1.0** + **Java 25** for all services. No Spring Cloud Config Server in this repo—config is per-service `application.yaml`.
- Important infra files: `docker-compose.yaml`, `docker-compose-local.yaml`, `docker/postgres/init.sql`, `docker/kafka_data/`.
- Human-oriented architecture diagrams: `diagrams/*.png` (see top-level `README.md`).

Critical integration points & dataflows (explicit)
- PostgreSQL: DB name `newsletter`, init in `docker/postgres/init.sql`; JDBC URLs appear in `src/main/resources/application.yaml`.

Observability & useful endpoints
- Kafka UI: http://localhost:8070 (inspect topics and messages)
- Mailpit (SMTP/web): SMTP **1025**, web UI http://localhost:8025 (outgoing email from `alert-service`)
- InfluxDB (UI/API): http://localhost:8072 (org/bucket/token from `docker-compose.yaml` env vars, e.g. bucket `usage-bucket`)
- Service ports (defaults in `application.yaml`):
    - `api-gateway` **9000**
    - `user-service` **8080**
    - `subscription-service` **8081**

Agent runbook checks (short)
- Confirm ports reachable: **5432** (PostgreSQL)
- Check service logs: `docker-compose logs <container>` or run the JAR locally and capture stdout

Project-specific conventions
- Gradle wrapper present in root folder — prefer `./gradlew`.
- Package names use underscores: e.g. `com.yurupari.calendar`.

Files to reference when automating (examples)
- `docker-compose.yaml` — infra and envs
- `docker-compose-local.yaml` — infra and envs for local development
- `docker/postgres/init.sql` — DB bootstrap
- `user-service/src/main/java/com/yurupari/user_service/controller/v1/UserControllerV1.java`

Notes
- Java version: use **JDK 25**.
- The top-level `README.md`.

# Problem context
## Backend Coding Challenge: Newsletter Subscription

Your task is to provide a high level overview of the interacting systems/components needed to provide a solution for a given requirement. Please describe in short words why you would set up the system in that way.

### Requirement

The starting point is a platform where users are able to register, update and delete their accounts. Within these accounts the user can subscribe, unsubscribe to several different newsletters (please note that every newsletter subscription is only valid after a double opt-in).

The user data should be forwarded to a CDP directly or indirectly to the API of the CDP (no direct connection between the database of the platform and the CDP is possible, due to security reasons) which then forwards updates to an external email marketing service if needed.

Make sure that the designed system is scalable and resilient against the most common  error cases like:

- Network issues
- Outage of the single services
- Latency of several minutes of the systems due to peak times

The platform should be able to retrieve current subscription data instantly.

End of AGENTS.md