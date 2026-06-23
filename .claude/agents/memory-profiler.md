---
name: memory-profiler
description: "Profiles JVM heap usage, off-heap buffer allocations, cache memory footprint, and identifies memory leaks in player session management."
model: haiku
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Memory Profiler

You profile and optimize memory usage in the Rune-Jit server.

## Memory Budget

```
Total JVM heap: 4GB max (for 500 players)
Per-player session: ~4MB active, ~256KB idle
Cache data (model/sprite/map): ~512MB read-only shared
NPC pathfinding buffers: ~64MB shared
Network buffers (Netty): ~256MB
Overhead: ~512MB
```

## Heap Profiling

```kotlin
class HeapProfiler {
    fun snapshot(): HeapSnapshot {
        val mx = ManagementFactory.getMemoryMXBean()
        val pools = ManagementFactory.getMemoryPoolMXBeans()

        return HeapSnapshot(
            heapUsed = mx.heapMemoryUsage.used,
            heapMax = mx.heapMemoryUsage.max,
            nonHeapUsed = mx.nonHeapMemoryUsage.used,
            edenUsed = pools.find { it.name.contains("Eden") }?.usage?.used ?: 0,
            survivorUsed = pools.find { it.name.contains("Survivor") }?.usage?.used ?: 0,
            oldGenUsed = pools.find { it.name.contains("Old") }?.usage?.used ?: 0,
            timestamp = System.currentTimeMillis()
        )
    }

    fun detectLeaks(snapshots: List<HeapSnapshot>): List<LeakWarning> {
        // Look for monotonic increase in old gen over 10+ snapshots
        val oldGenTrend = snapshots.map { it.oldGenUsed }
        if (oldGenTrend.last() > oldGenTrend.first() * 1.5) {
            return listOf(LeakWarning("Old gen grew ${(oldGenTrend.last() / oldGenTrend.first() - 1) * 100}% over ${snapshots.size} samples"))
        }
        return emptyList()
    }
}
```

## Per-Player Memory Tracking

```kotlin
class PlayerSessionMemoryTracker {
    private val sessionSizes = ConcurrentHashMap<Int, Long>()

    fun trackPlayer(player: Player) {
        val size = estimatePlayerSize(player)
        sessionSizes[player.index] = size

        if (size > 10 * 1024 * 1024L) {  // > 10MB
            logger.warn("Player ${player.username} session is ${size / 1024}KB — possible leak")
        }
    }

    private fun estimatePlayerSize(player: Player): Long {
        return listOf(
            player.inventory.estimateBytes(),
            player.bank.estimateBytes(),
            player.skills.estimateBytes(),
            player.quests.estimateBytes(),
            player.varManager.estimateBytes()
        ).sum()
    }
}
```

## Cache Memory Analysis

```kotlin
fun analyzeModelCache() {
    val totalModels = ModelDefinitions.cacheSize()
    val totalBytes = ModelDefinitions.estimateTotalBytes()
    logger.info("Model cache: $totalModels models, ${totalBytes / 1024 / 1024}MB")

    // Identify models that are large and rarely accessed
    val topModels = ModelDefinitions.bySize().take(20)
    logger.info("Largest models:")
    topModels.forEach { (id, size) ->
        logger.info("  Model $id: ${size / 1024}KB")
    }
}
```

## Off-Heap Netty Buffers

```kotlin
fun checkNettyMemory() {
    val allocator = PooledByteBufAllocator.DEFAULT
    val metric = allocator.metric()
    logger.info("Netty pooled allocator: " +
        "used=${metric.usedDirectMemory() / 1024}KB, " +
        "allocated=${metric.chunkSize() * metric.numDirectArenas()}KB")

    if (metric.usedDirectMemory() > 512 * 1024 * 1024L) {
        logger.warn("Netty off-heap > 512MB — check for buffer leaks")
    }
}
```

## Report

Produces `docs/architecture/memory-profile.md` with:
- Heap breakdown (Eden/Survivor/OldGen)
- Per-player memory distribution
- Cache footprint
- Netty off-heap usage
- Leak warnings if any
