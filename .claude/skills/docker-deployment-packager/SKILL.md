---
name: docker-deployment-packager
description: "Packages the Rune-Jit server and client into Docker containers, creates docker-compose.yml with database and cache volumes, produces a versioned release bundle, and validates the deployment with a health check."
argument-hint: "[mode: server-only|full|dev]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /docker-deployment-packager [mode]

Packages Rune-Jit into production-ready Docker containers.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Server compiles (`./gradlew build`) | Yes |
| Performance targets met (P99 < 580ms at 500 players) | Yes — run `/performance-profiling-and-optimization` first |
| Global parity score ≥90% | Yes — run `/automated-parity-testing-suite` first |
| Docker and docker-compose installed | Yes |
| `data/osrsbox/` populated | Yes |

```bash
./gradlew build --no-daemon -q
docker --version
docker-compose --version
```

## Phase 2: Build Server Fat JAR

Spawn `release-packager` to produce the server artifact:

```bash
./gradlew shadowJar -PappVersion=$(git describe --tags --always)
# Output: build/libs/rune-jit-server-{version}-all.jar
```

Verify the JAR:
```bash
java -jar build/libs/rune-jit-server-*.jar --dry-run
# Should print "Rune-Jit server config validated. Ready to start."
```

## Phase 3: Write Server Dockerfile

Write `docker/server/Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# JVM flags: ZGC for low-latency GC, tuned for 500-player server
ENV JAVA_OPTS="-server \
  -Xms2g -Xmx3g \
  -XX:+UseZGC \
  -XX:ZCollectionInterval=5 \
  -XX:MaxDirectMemorySize=512m \
  -Dfile.encoding=UTF-8"

COPY build/libs/rune-jit-server-*-all.jar /app/server.jar
COPY data/ /app/data/
COPY config/server.yaml /app/config/server.yaml

EXPOSE 43594   # game port
EXPOSE 43595   # JS5 cache port

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/server.jar"]
```

**Note**: `cache/` directory is NOT copied into the Docker image — it must be mounted as a volume. This enforces the rule that cache files are never committed or distributed.

## Phase 4: Write docker-compose.yml

Write `docker-compose.yml` at repo root:

```yaml
version: '3.9'

services:
  rune-jit-server:
    build:
      context: .
      dockerfile: docker/server/Dockerfile
    container_name: rune-jit-server
    restart: unless-stopped
    ports:
      - "43594:43594"   # game traffic
      - "43595:43595"   # JS5 cache serving
      - "8080:8080"     # admin/health endpoint
    volumes:
      - ./cache:/app/cache:ro          # OSRS cache (read-only, user-provided)
      - player_saves:/app/data/saves   # persistent player save files
      - ./data/osrsbox:/app/data/osrsbox:ro
    environment:
      - SERVER_PORT=43594
      - JS5_PORT=43595
      - MAX_PLAYERS=500
      - WORLD_ID=1
      - REVISION=${REVISION:-225}
    healthcheck:
      test: ["CMD", "curl", "-sf", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  rune-jit-db:
    image: postgres:15-alpine
    container_name: rune-jit-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: rune_jit
      POSTGRES_USER: rune_jit
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
    volumes:
      - db_data:/var/lib/postgresql/data
    secrets:
      - db_password

volumes:
  player_saves:
  db_data:

secrets:
  db_password:
    file: ./secrets/db_password.txt
```

Write `docker/.env.example`:
```
REVISION=225
MAX_PLAYERS=500
WORLD_ID=1
```

## Phase 5: Dev Mode Compose Override

Write `docker-compose.dev.yml`:

```yaml
version: '3.9'

services:
  rune-jit-server:
    build:
      target: dev
    volumes:
      - ./src:/app/src    # hot-reload source
    environment:
      - DEBUG=true
      - LOG_LEVEL=DEBUG
    command: ["./gradlew", "run", "--continuous"]
```

## Phase 6: Release Bundle

Spawn `release-packager` to create `dist/rune-jit-{version}.tar.gz`:

```
dist/rune-jit-{version}/
├── docker/
│   ├── server/Dockerfile
├── docker-compose.yml
├── docker-compose.dev.yml
├── docker/.env.example
├── config/
│   └── server.yaml.example
├── data/
│   └── README.md          # "Run /import-osrsbox-complete to populate"
├── INSTALL.md             # Step-by-step deployment guide
└── RELEASE-NOTES.md
```

Write `INSTALL.md` with:
1. Prerequisites (Docker, 4GB RAM, 10GB disk for cache+saves)
2. Cache acquisition instructions (OpenRS2 link)
3. `docker-compose up -d` command
4. Health check verification
5. Port forwarding notes

## Phase 7: Deployment Validation

Spawn `integration-tester` to validate the Docker deployment:

```bash
# Start containers
docker-compose up -d

# Wait for health
docker-compose ps  # expect: rune-jit-server State: healthy

# Run smoke tests
./tests/integration/smoke-test.sh  # connects as bot, walks 5 tiles, disconnects
```

Pass criteria:
- [ ] Server container starts within 60 seconds
- [ ] Health endpoint returns `{"status":"UP","players":0}`
- [ ] JS5 connection serves cache index 0 successfully
- [ ] Game login accepts a test account
- [ ] Player save writes to volume correctly

## Error Recovery

| Error | Recovery |
|-------|---------|
| JAR build fails | Run `./gradlew build --info` for detailed error |
| Container OOM killed | Increase `-Xmx` or reduce `MAX_PLAYERS` |
| Port 43594 in use | Change host port mapping in docker-compose.yml |
| Cache volume empty | Mount host cache dir: `-v /path/to/cache:/app/cache:ro` |
| DB connection refused | Check `db_password.txt` exists; check postgres container logs |

## Nuances

- The `cache/` directory must NEVER be baked into the image — users provide their own cache
- `secrets/db_password.txt` must be created before first `docker-compose up`
- ZGC is available in Java 17+ and is the recommended GC for tick-latency workloads
- Admin port 8080 must NOT be exposed publicly in production (firewall rule required)
- Player save volume must be backed up regularly — no automatic backup in this setup

## Next Steps

1. Set up automated backup for `player_saves` volume (cron + rsync)
2. Configure reverse proxy (nginx) in front of admin port 8080
3. Enable `/revision-update-adapter` monitoring for weekly revision checks
4. Set up log aggregation (container logs → external logging service)
