# Server — rsmod-based OSRS Emulator Backend

The server is built on top of [rsmod/rsmod](https://github.com/rsmod/rsmod),
a Kotlin-based OSRS game server framework with plugin architecture.

## Prerequisites

- Java 17 (LTS)
- Gradle 8.x
- Kotlin 1.9+

## Setup

```bash
# Clone rsmod as the base
git submodule add https://github.com/rsmod/rsmod src/server/rsmod

# Build
cd src/server
./gradlew build

# Run
./gradlew run
```

## Architecture

```
src/server/
├── engine/       # Tick loop, entity manager, world simulation
├── skills/       # 23+ skill implementations (data-driven)
├── combat/       # Combat engine, prayer system, special attacks
├── net/          # Packet handlers, ISAAC cipher, JS5 cache server
├── persistence/  # Player save/load (JSON/SQLite backends)
├── world/        # Region loading, pathfinding, object spawns
├── plugins/      # rsmod plugin system extensions
└── build.gradle.kts
```

## Key Implementation Notes

- All actions scheduled via the tick queue — no `Thread.sleep()` in game logic
- 600ms tick cycle using `HashedWheelTimer`
- ISAAC cipher for all client<→server packet encryption
- JS5 cache server serves all 21+ cache indices to connecting clients
- Pathfinding uses clip flags decoded from cache region data

## Generating Kotlin data classes from osrsbox

After running `/import-osrsbox-complete`, generate type-safe Kotlin data classes:
```
/documentation-auto-generator --types
```

## Phase 2 Skills

Implement server systems in this order:
```
/implement-tick-engine-core
/implement-skill-action-framework [skill-name]
/combat-engine-full
/pathfinding-and-clipping-engine
/protocol-packet-engine
```
