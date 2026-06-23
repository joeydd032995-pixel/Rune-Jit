---
name: implement-tick-engine-core
description: "Implements the 600ms OSRS tick loop using Netty HashedWheelTimer, entity scheduling, desync prevention, and tick manipulation support. The foundation of all server-side game logic."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /implement-tick-engine-core

Implements the OSRS 600ms game tick loop — the foundation of all server logic.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| `src/server/` scaffold exists | Yes |
| rsmod dependency in build.gradle.kts | Yes |
| Netty dependency available | Yes |
| Tick engine GDD exists | Recommended (create with /gdd-osrs-specialized-framework) |

## Phase 2: Load Context

Read these files before implementation:
- `.claude/docs/revision.yaml` (target revision)
- `design/gdd/tick-engine-gdd.md` (if exists)
- Reference: `src/server/README.md` (rsmod integration notes)

2006Scape reference pattern to follow:
```java
// 2006Scape: Constants.CYCLE_TIME = 600
// CycleEventHandler.addEvent(CycleEvent, ticks)
// All game logic in CycleEvent.execute()
```

## Phase 3: Implement TickLoop

Spawn `tick-engine-programmer` to implement `src/server/engine/TickLoop.kt`:

```kotlin
// Key contract:
// - 600ms interval using Netty HashedWheelTimer
// - Entity update order: players → NPCs → ground items → shops
// - Tick slip detection (warn if >580ms)
// - Server pause/resume for maintenance
// - Graceful shutdown on SIGTERM
```

**Entity update ordering** (must match OSRS exactly):
1. Process player movement + queued actions
2. Process NPC movement + AI
3. Send player update blocks to all clients
4. Send NPC update blocks to all clients
5. Process ground item visibility
6. Process shop restocks

## Phase 4: Implement CycleEvent System

Spawn `tick-engine-programmer` to implement the event scheduling API:

```kotlin
// Based on 2006Scape CycleEventHandler pattern:
tickEngine.schedule(player, ticks = 2) {
    // executes 2 ticks from now
}
tickEngine.scheduleRepeating(shopRestockTask, every = 100)
tickEngine.cancel(player)  // cancel pending events for entity
```

## Phase 5: Desync Prevention

Spawn `desync-investigator` to:
- Review `TickLoop.kt` for update ordering violations
- Verify RNG is server-side only
- Confirm entity processing order matches Phase 3 contract
- Write `tests/parity/tick-engine-ordering.test.kt`

## Phase 6: Write Unit Tests

```kotlin
// tests/parity/tick-engine-test.kt
@Test fun testTickInterval() {
    // Assert tick fires every 600ms (±10ms tolerance)
}
@Test fun testEntityUpdateOrder() {
    // Assert player update processed before NPC update
}
@Test fun testTickSlipDetection() {
    // Assert warning logged when tick >580ms
}
```

## Phase 7: Performance Gate

Spawn `performance-profiler`:
- Run with 2000 simulated entities
- Assert P99 tick < 580ms
- Assert no tick slips in 1000-tick baseline run

## Error Recovery

| Error | Recovery |
|-------|---------|
| Netty timer drift | Tune `ticksPerWheel` parameter |
| Tick slip in test | Profile and optimize hot paths |
| Entity ordering bug | Re-read OSRS update block spec; fix ordering |

## Nuances

- Prayer flicking requires exact same-tick detection — do not add "anti-flick" logic
- 3-tick woodcutting works via animation reset timing — must support it
- `gameTick` counter overflows after ~68 years at 600ms; handle wrapping
- Tick manipulation exploits are features, not bugs

## Next Steps

1. Run `/implement-skill-action-framework woodcutting` (first skill)
2. Run `/combat-engine-full` (combat system)
3. Run `/pathfinding-and-clipping-engine` (movement)
