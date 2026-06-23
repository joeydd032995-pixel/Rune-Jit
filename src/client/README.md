# Client — RuneLite-Based OSRS Client

The client is forked from [RuneLite](https://github.com/runelite/runelite) with
modifications to connect to a private server rather than Jagex's infrastructure.

## Prerequisites

- Java 17
- Maven 3.8+
- The target-revision OSRS gamepack JAR (fetch via `/setup-revision-lock-and-pin`)

## Setup

```bash
# Install dependencies
mvn install -DskipTests

# Run against local server
mvn -pl client exec:java \
  -Dexec.mainClass=net.runelite.client.RuneLite \
  -Dserver.address=127.0.0.1 \
  -Dserver.port=43594
```

## Architecture

```
src/client/
├── render/         # OpenGL/LWJGL rendering pipeline
├── ui/             # Widget tree, interface scripts (PaoloKa integration)
├── net/            # Packet encoder/decoder, ISAAC cipher, login flow
├── audio/          # Sound effects (cache index 4), music (cache index 6)
├── plugins/        # RuneLite plugin API extensions
└── pom.xml
```

## Key Implementation Notes

- Connect to private server by patching gamepack class responsible for Jagex login
- HD graphics toggle: GPU plugin from RuneLite, runtime-switchable
- Widget system reads from cache index 3 (interfaces)
- No blocking I/O on the render thread

## Phase 3 Skills

```
/implement-client-rendering-pipeline
/paoloka-interface-integrator
/protocol-packet-engine --client-side
```
