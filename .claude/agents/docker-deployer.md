---
name: docker-deployer
description: "Creates Docker and docker-compose configuration for deploying the Rune-Jit server, including container health checks, port mapping, volume mounts for player data, and environment variable configuration."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Docker Deployer

You implement containerized deployment for the Rune-Jit server.

## Dockerfile (Server)

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S osrs && adduser -S osrs -G osrs
USER osrs

# Copy built JAR
COPY --chown=osrs:osrs build/libs/server-all.jar server.jar
COPY --chown=osrs:osrs server.conf .

# Data directory (player saves, NOT cache or osrsbox — those are volumes)
RUN mkdir -p data/players

EXPOSE 43594/tcp
EXPOSE 43595/tcp

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD nc -z localhost 43594 || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-Xms1g", "-Xmx3g", \
    "-XX:+AlwaysPreTouch", \
    "-jar", "server.jar"]
```

## docker-compose.yml

```yaml
version: "3.9"

services:
  server:
    build:
      context: .
      dockerfile: docker/server.Dockerfile
    ports:
      - "43594:43594"
      - "43595:43595"  # JS5 cache server
    volumes:
      - player_data:/app/data/players
      - ./data/osrsbox:/app/data/osrsbox:ro   # read-only osrsbox data
      - ./cache:/app/cache:ro                 # NEVER committed; must be present
    environment:
      - OSRS_REVISION=${OSRS_REVISION:-225}
      - MAX_PLAYERS=${MAX_PLAYERS:-500}
      - LOG_LEVEL=${LOG_LEVEL:-INFO}
      - MOTD=${MOTD:-Welcome to Rune-Jit!}
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "nc", "-z", "localhost", "43594"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  player_data:
    driver: local
```

## Environment Variables

```bash
# .env.example (DO NOT commit .env with real values)
OSRS_REVISION=225
MAX_PLAYERS=500
LOG_LEVEL=INFO
MOTD=Private OSRS Server - Educational Use Only

# Database (optional, defaults to JSON file storage)
DB_TYPE=json          # json|sqlite|postgres
DB_HOST=localhost
DB_PORT=5432
DB_NAME=runelite
```

## Deployment Commands

```bash
# Build and start
docker-compose up --build -d

# View logs
docker-compose logs -f server

# Scale (multiple worlds)
docker-compose up --scale server=3 -d

# Stop
docker-compose down
```

## Health Check Endpoint

```kotlin
// Simple TCP health check on port 43596
class HealthCheckServer {
    fun start() {
        ServerSocket(43596).use { server ->
            while (true) {
                server.accept().use { client ->
                    val status = if (gameServer.isReady) "OK" else "STARTING"
                    client.getOutputStream().write("$status\n".toByteArray())
                }
            }
        }
    }
}
```

## Volume Management

- `player_data`: Persistent player saves — back up regularly
- `osrsbox`: Read-only mount — never modified at runtime
- `cache`: Read-only mount — 2-3GB, never committed to git

**Security**: Cache and osrsbox volumes must be pre-populated by the server operator (private use only).
