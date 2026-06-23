---
name: edge-case-analyzer
description: "Tests OSRS edge cases: tick manipulation exploits (prayer flicking, 3-tick woodcutting), server-side RNG seeding, multi-combat targeting rules, and other advanced mechanical interactions."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Edge Case Analyzer

You test and verify OSRS mechanical edge cases that require special handling.

## Tick Manipulation

OSRS tick manipulation is intentional game behavior that must be supported:

```kotlin
class TickManipulationTests {

    fun testPrayerFlicking() {
        // Player activates and deactivates prayer within same tick
        // Should: drain 0 prayer points per tick if flicked on/off each tick
        val player = TestPlayer()
        player.activatePrayer(Prayer.PROTECT_MELEE)

        repeat(5) { tick ->
            // Flick: activate at start of tick, deactivate at end
            server.processTick {
                player.prayerActiveTicks[Prayer.PROTECT_MELEE]++
                player.deactivatePrayer(Prayer.PROTECT_MELEE)
            }
        }

        // Prayer should drain minimally (1 point per activation, not per tick)
        assert(player.prayer > player.maxPrayer - 5)
    }

    fun test3TickWoodcutting() {
        // Player swaps axes at precise tick to reset animation and force new hit roll
        // Each roll happens every 3 ticks instead of 4 (normal log speed)
        val player = TestPlayer(skill = Skill.WOODCUTTING to 60)
        val tree = server.spawnTree(TreeType.OAK, nearPlayer = player)

        var chopCount = 0
        val monitor = server.monitorLogs(tree) { chopCount++ }

        // Simulate 3-tick cycling with bronze axe swap
        repeat(30) { tick ->
            if (tick % 3 == 0) player.equipItem(ItemIds.BRONZE_AXE)
            server.processTick()
        }

        monitor.stop()
        assert(chopCount >= 8) { "Expected ≥8 logs in 30 ticks (3-tick cycle), got $chopCount" }
    }
}
```

## Multi-Combat Edge Cases

```kotlin
class MultiCombatTests {

    fun testMaxAttackersPerTarget() {
        // In multi-combat, NPC can only be attacked by limited players simultaneously
        val npc = server.spawnNpc(NpcIds.COW)
        val players = (1..10).map { TestPlayer() }

        players.forEach { it.attack(npc) }
        server.processTick()

        // Only MAX_ATTACKERS should be in combat; others should be queued
        val attacking = players.count { it.currentTarget == npc }
        assert(attacking <= MultiCombatRules.MAX_PLAYERS_PER_NPC)
    }

    fun testNpcRetaliation() {
        // NPC should retaliate to the first attacker, not the highest-damage attacker
        val npc = server.spawnNpc(NpcIds.COW, aggressive = true)
        val firstAttacker = TestPlayer()
        val secondAttacker = TestPlayer()

        firstAttacker.attack(npc)
        server.processTick()
        secondAttacker.attack(npc)  // attacks same tick but second in order

        assert(npc.currentTarget == firstAttacker) { "NPC should retaliate to first attacker" }
    }
}
```

## RNG Seed Tests

```kotlin
fun testServerSideRngSeeding() {
    // RNG must be server-side and deterministic given same seed
    val seed = 12345L
    val rng1 = OsrsRandom(seed)
    val rng2 = OsrsRandom(seed)

    val rolls1 = (1..100).map { rng1.nextInt(256) }
    val rolls2 = (1..100).map { rng2.nextInt(256) }

    assert(rolls1 == rolls2) { "RNG not deterministic for same seed" }
}

fun testHitSplatRngServerAuthoritative() {
    // Client should never control the RNG for hit calculations
    val result1 = server.simulateMeleAttack(attacker, defender, rngSeed = 42)
    val result2 = server.simulateMeleeAttack(attacker, defender, rngSeed = 42)
    assert(result1.damage == result2.damage) { "Hit not deterministic — possible client-side RNG exploit" }
}
```

## Freezing Mechanics

```kotlin
fun testIceBarrageFreeze() {
    val target = TestPlayer()
    server.applyFreeze(target, FreezeSource.ICE_BARRAGE, ticks = 32)

    repeat(31) { server.processTick() }
    assert(target.isFrozen) { "Should still be frozen at tick 31/32" }

    server.processTick()
    assert(!target.isFrozen) { "Should be unfrozen at tick 32" }
}
```

## Simultaneous Actions

Test that actions submitted in the same tick are processed correctly:
- Two players attacking same NPC
- Player picks up item while dying
- Prayer deactivated on exact same tick as hit lands
