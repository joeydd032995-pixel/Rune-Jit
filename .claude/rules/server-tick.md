---
path: src/server/engine/**
---

# Server Tick Engine Rules

## Core Constraints

1. **All game actions must be scheduled via the tick queue** — never call action handlers directly from HTTP, WebSocket, or any non-tick context.

2. **No Thread.sleep in tick handlers** — the tick loop uses Netty `HashedWheelTimer`; sleeping blocks the event loop.

3. **Tick budget enforcement** — the entire tick loop (player update + NPC update + ground items + shop restock) must complete in under 580ms. If profiling shows a tick approaching 580ms, log a WARN.

4. **Entity update order must match OSRS exactly**:
   ```
   1. Player movement & queued actions
   2. NPC movement & AI
   3. Player update blocks (sent to all clients)
   4. NPC update blocks (sent to all clients)
   5. Ground item updates
   6. Object/scenery updates
   ```
   Any deviation from this order causes client-server desync.

5. **Tick manipulation must be supported** — prayer flicking, 3-tick skilling, and 4-tick combat cycling are legitimate OSRS mechanics. Do not add "anti-exploit" code that breaks these.

6. **RNG must be server-authoritative** — all hit rolls, success rolls, and drop rolls must use the server-side `OsrsRandom`. Never accept RNG results from the client.

7. **Tick counter is monotonic** — `gameTick` only increments, never resets. Use it for all time-based cooldowns and durations.

## Prohibited Patterns

```kotlin
// ❌ WRONG: Blocking inside tick handler
fun processPlayer(player: Player) {
    Thread.sleep(100)  // NEVER
    player.save()      // NEVER — save is async, schedule it
}

// ✅ CORRECT: Schedule async work
fun processPlayer(player: Player) {
    tickQueue.schedule(player::processCombat)
    asyncSaveScheduler.scheduleIfDue(player)
}
```
