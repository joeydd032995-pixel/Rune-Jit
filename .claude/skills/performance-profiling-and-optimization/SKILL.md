---
name: performance-profiling-and-optimization
description: "Profiles the tick loop, identifies hot paths, tunes JVM GC settings, and produces concrete optimization recommendations targeting <580ms tick latency at 500 concurrent players."
argument-hint: "[player_count: 200|500|1000]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /performance-profiling-and-optimization [player_count]

Profiles and optimizes server performance.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Server compiled | Yes |
| JFR (Java Flight Recorder) | Java 17+ (built-in) |
| Load test framework available | Yes |

## Phase 2: Baseline Measurement

Spawn `load-tester` to:
- Start server with `[player_count]` simulated bots
- Run for 5 minutes
- Record: P50/P95/P99 tick latency, memory usage, CPU %

Current performance budget:
```
P99 tick: <580ms (hard limit)
Memory: <4GB at 500 players
GC pause: <10ms
```

## Phase 3: JFR Profiling

Spawn `performance-profiler`:
```kotlin
// Enable JFR for 60-second window
val recording = Recording()
recording.enable("jdk.MethodSampling").withPeriod(Duration.ofMillis(10))
recording.enable("jdk.GarbageCollection")
recording.start()
Thread.sleep(60_000)
recording.dump(Path.of("profiling-$(date).jfr"))
```

## Phase 4: Hot Path Analysis

Spawn `performance-profiler` to analyze JFR output:

Common hot paths to check:
| Path | Expected % | Concern Threshold |
|------|-----------|-----------------|
| `PlayerUpdateTask.run()` | 40-60% | >75% = optimize |
| `PathfindingEngine.findPath()` | 10-20% | >30% = optimize |
| `ItemDefinitions.get()` | 5-10% | >15% = use array cache |
| `GC pauses` | <5% | >10% = tune GC |

## Phase 5: Memory Profiling

Spawn `memory-profiler` to:
- Snapshot heap after 5 minutes at `[player_count]`
- Identify top memory consumers
- Check for suspected memory leaks (monotonic old gen growth)

## Phase 6: Apply Optimizations

Based on profiling results, apply targeted fixes:

| Problem | Solution | Agent |
|---------|---------|-------|
| `ItemDefinitions.get()` slow | Replace HashMap with dense IntArray | item-system-engineer |
| GC pressure from BFS | Object pool for path nodes | pathfinding-engineer |
| Large UPDATE_PLAYERS packet | Reduce view distance or player limit | protocol-sync-validator |
| Tick slip from NPC AI | Limit AI evaluation to N NPCs/tick | npc-behavior-simulator |

## Phase 7: Re-measure & Report

Run load test again after optimizations. Produce `docs/architecture/performance-profile.md`.

Pass criteria:
- P99 tick < 580ms at `[player_count]` players ✓
- Memory < 4GB at 500 players ✓
- No memory leak trend ✓

## Error Recovery

| Error | Recovery |
|-------|---------|
| JFR not available | Use async-profiler as alternative |
| Server crashes under load | Profile with fewer players first |
| GC thrashing | Tune -Xms/-Xmx, switch to ZGC |

## Nuances

- Profile under realistic load (mix of combat, skilling, idle) — not just idle bots
- Kotlin coroutines can hide thread contention; use JFR ThreadStall events
- JVM JIT warmup takes ~30 seconds; exclude first minute from P99 measurements
- Netty allocates off-heap buffers — check direct memory separately from heap

## Next Steps

After optimization:
1. Run `/load-tester 500` to verify improvement
2. Update performance-budget.md with new baselines
3. Consider `/docker-deployment-packager` if performance targets met
