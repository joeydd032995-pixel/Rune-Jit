package npc

import combat.CombatStyle
import engine.TickEvent
import engine.TickQueue
import engine.TickQueueImpl
import entity.Npc
import entity.Player
import entity.Skill
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import prayer.Prayer
import prayer.PrayerDefs

/**
 * Parity tests for NPC aggression, combat, and protection prayer mechanics.
 *
 * All tests are fully stateless: each test creates fresh [Npc] and [Player]
 * instances. World is NOT initialised — the graceful-absent pattern means
 * pathfinding falls back to direct stepping when the cache is absent, which
 * is acceptable for these unit-level parity tests.
 *
 * Source references:
 *   - Aggression radius: https://oldschool.runescape.wiki/w/Aggressive
 *   - Protect from Melee: https://oldschool.runescape.wiki/w/Protect_from_Melee
 *   - Protection prayer 100% PvM block: https://oldschool.runescape.wiki/w/Prayer#Protection_prayers
 *   - NPC following: https://oldschool.runescape.wiki/w/Non-player_character#Following
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NpcAggroTest {

    @BeforeAll
    fun setup() {
        // PrayerDefs must be loaded before any prayer activation.
        // Working directory is repo root per build.gradle.kts tasks.test workingDir.
        PrayerDefs.init()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Captures all scheduled events without actually advancing time. */
    private class CapturingQueue : TickQueue {
        val scheduled = mutableListOf<Pair<Int, TickEvent>>()
        val cancelled = mutableListOf<TickEvent>()

        override fun schedule(delayTicks: Int, event: TickEvent) {
            scheduled.add(delayTicks to event)
        }

        override fun cancel(event: TickEvent) {
            cancelled.add(event)
        }
    }

    private fun makeNpc(x: Int = 3200, y: Int = 3200, combatLevel: Int = 10, maxHp: Int = 20): Npc =
        Npc(
            name = "Goblin",
            combatLevel = combatLevel,
            defenceLevel = 1,
            maxHp = maxHp,
            x = x,
            y = y,
        )

    /**
     * Creates a test player positioned at (x, y) with enough Prayer XP for level 50
     * (sufficient to activate any standard prayer including Protect from Melee at level 43).
     *
     * Source: Protect from Melee level requirement = 43
     * https://oldschool.runescape.wiki/w/Protect_from_Melee
     */
    private fun makePlayer(x: Int = 3200, y: Int = 3200): Player {
        val player = Player(username = "TestPlayer", tickQueue = TickQueueImpl())
        player.x = x
        player.y = y
        // OSRS XP for level 50 Prayer ≈ 101,333. Using 200,000 to guarantee level ≥ 50.
        // Source: https://oldschool.runescape.wiki/w/Prayer#Training
        player.skills.addXp(Skill.PRAYER, 200_000.0)
        player.prayer.fillPoints()
        return player
    }

    // -------------------------------------------------------------------------
    // Test 1: NPC within 5 tiles → aggro triggers
    // Source: https://oldschool.runescape.wiki/w/Aggressive
    // -------------------------------------------------------------------------

    /**
     * An NPC at (3200, 3200) and player at (3205, 3200) are exactly 5 tiles apart
     * (Chebyshev). This is within the default aggro radius of 5, so aggro must fire.
     *
     * Source: https://oldschool.runescape.wiki/w/Aggressive
     */
    @Test fun `npc within 5 tiles of player triggers aggro`() {
        val capturingQueue = CapturingQueue()
        val npc = makeNpc(x = 3200, y = 3200)
        // Player is exactly 5 tiles east — within aggroRadius 5
        val player = makePlayer(x = 3205, y = 3200)

        val action = NpcAggroAction(
            npc = npc,
            tickQueue = capturingQueue,
            aggroRadius = 5,
            playerSource = { listOf(player) },
        )
        action.process(currentTick = 1L)

        val combatScheduled = capturingQueue.scheduled.any { (_, event) ->
            event is NpcCombatAction
        }
        assertTrue(
            combatScheduled,
            "NpcAggroAction must initiate NpcCombatAction when player is within 5 tiles. " +
                "Source: https://oldschool.runescape.wiki/w/Aggressive"
        )
    }

    // -------------------------------------------------------------------------
    // Test 2: NPC 6 tiles away → no aggro
    // Source: https://oldschool.runescape.wiki/w/Aggressive
    // -------------------------------------------------------------------------

    /**
     * An NPC at (3200, 3200) and player at (3206, 3200) are 6 tiles apart — outside
     * the default aggro radius of 5. No combat action should be started.
     *
     * Source: https://oldschool.runescape.wiki/w/Aggressive
     */
    @Test fun `npc 6 tiles from player does not trigger aggro`() {
        val capturingQueue = CapturingQueue()
        val npc = makeNpc(x = 3200, y = 3200)
        // Player is 6 tiles east — outside aggroRadius 5
        val player = makePlayer(x = 3206, y = 3200)

        val action = NpcAggroAction(
            npc = npc,
            tickQueue = capturingQueue,
            aggroRadius = 5,
            playerSource = { listOf(player) },
        )
        action.process(currentTick = 1L)

        val combatScheduled = capturingQueue.scheduled.any { (_, event) ->
            event is NpcCombatAction
        }
        assertFalse(
            combatScheduled,
            "NpcAggroAction must NOT start combat when player is 6 tiles away (outside radius 5). " +
                "Source: https://oldschool.runescape.wiki/w/Aggressive"
        )
    }

    // -------------------------------------------------------------------------
    // Test 3: Protect from Melee overhead prayer → 0 damage
    // Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
    // Source: https://oldschool.runescape.wiki/w/Prayer#Protection_prayers
    // -------------------------------------------------------------------------

    /**
     * When a player has Protect from Melee active, an NPC melee attack deals 0 damage.
     * Overhead prayers are 100% damage reduction in PvM.
     *
     * Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
     * Source: https://oldschool.runescape.wiki/w/Prayer#Protection_prayers
     */
    @Test fun `protect from melee prayer blocks all npc melee damage`() {
        val npc = makeNpc(x = 3200, y = 3200, combatLevel = 50, maxHp = 100)
        val player = makePlayer(x = 3201, y = 3200)

        // Activate Protect from Melee — player has Prayer level 50, requirement is 43.
        // Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
        val activated = player.prayer.activate(Prayer.PROTECT_FROM_MELEE)
        assertTrue(
            activated,
            "Protect from Melee must activate when player has Prayer level ≥ 43 and prayer points > 0. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee"
        )
        assertTrue(
            player.prayer.isProtectedFrom(CombatStyle.MELEE_AGGRESSIVE),
            "isProtectedFrom(MELEE_AGGRESSIVE) must return true when Protect from Melee is active. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee"
        )

        val hpBefore = player.currentHp

        // Run the combat action directly. NPC is adjacent (dist = 1), prayer active → 0 damage.
        // CapturingQueue is used so no tick advancement occurs.
        val capturingQueue = CapturingQueue()
        val combatAction = NpcCombatAction(npc, player, capturingQueue, attackSpeedTicks = 1)
        combatAction.process(currentTick = 0L)

        assertEquals(
            hpBefore,
            player.currentHp,
            "Protect from Melee overhead prayer must block 100% of NPC melee damage in PvM. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee"
        )
    }

    // -------------------------------------------------------------------------
    // Test 4: NPC at same tile as player → attack fires (dist = 0 ≤ 10)
    // Source: https://oldschool.runescape.wiki/w/Non-player_character#Following
    // -------------------------------------------------------------------------

    /**
     * An NPC occupying the same tile as a player (Chebyshev distance = 0) is within
     * the attack threshold (dist ≤ 1), so an attack must be attempted and the combat
     * action rescheduled.
     *
     * Source: https://oldschool.runescape.wiki/w/Non-player_character#Following
     */
    @Test fun `npc at same tile as player attempts attack`() {
        val queue = CapturingQueue()
        val npc = makeNpc(x = 3200, y = 3200, combatLevel = 50)
        val player = makePlayer(x = 3200, y = 3200)   // same tile — dist = 0

        val combatAction = NpcCombatAction(npc, player, queue, attackSpeedTicks = 4)
        val result = combatAction.process(currentTick = 1L)

        assertTrue(
            result,
            "NpcCombatAction.process must return true (reschedule) when NPC and player share a tile. " +
                "Source: https://oldschool.runescape.wiki/w/Non-player_character#Following"
        )
        assertTrue(
            queue.scheduled.any { (delay, _) -> delay == 4 },
            "Combat action must reschedule at attackSpeedTicks=4 delay after attacking. " +
                "Source: https://oldschool.runescape.wiki/w/Combat#Attack_speed"
        )
    }

    // -------------------------------------------------------------------------
    // Test 5: NPC 11 tiles from player → combat cancels
    // Source: https://oldschool.runescape.wiki/w/Non-player_character#Following
    // -------------------------------------------------------------------------

    /**
     * When the Chebyshev distance between the NPC and target exceeds 10 tiles,
     * the combat action must cancel itself (return false, do not reschedule).
     *
     * Source: https://oldschool.runescape.wiki/w/Non-player_character#Following
     */
    @Test fun `npc 11 tiles from player cancels combat action`() {
        val queue = CapturingQueue()
        val npc = makeNpc(x = 3200, y = 3200)
        val player = makePlayer(x = 3211, y = 3200)  // 11 tiles east

        val combatAction = NpcCombatAction(npc, player, queue, attackSpeedTicks = 4)
        val result = combatAction.process(currentTick = 1L)

        assertFalse(
            result,
            "NpcCombatAction must return false (cancel) when target is more than 10 tiles away. " +
                "Source: https://oldschool.runescape.wiki/w/Non-player_character#Following"
        )
        assertFalse(
            queue.scheduled.any { (_, event) -> event is NpcCombatAction },
            "NpcCombatAction must not reschedule itself when target is more than 10 tiles away"
        )
    }

    // -------------------------------------------------------------------------
    // Test 6: Dead NPC does not attack
    // Source: https://oldschool.runescape.wiki/w/Non-player_character
    // -------------------------------------------------------------------------

    /**
     * A dead NPC (currentHp = 0, isDead = true) must not attack a player,
     * and the combat action must cancel itself immediately.
     *
     * Source: https://oldschool.runescape.wiki/w/Non-player_character
     */
    @Test fun `dead npc does not attack player`() {
        val queue = CapturingQueue()
        // Create NPC with maxHp=1 then deal 1 damage to kill it
        val npc = makeNpc(x = 3200, y = 3200, combatLevel = 50, maxHp = 1)
        npc.takeDamage(1)
        assertTrue(
            npc.isDead,
            "NPC must be dead after taking damage equal to its max HP. " +
                "Source: https://oldschool.runescape.wiki/w/Non-player_character"
        )

        val player = makePlayer(x = 3201, y = 3200)
        val hpBefore = player.currentHp

        val combatAction = NpcCombatAction(npc, player, queue, attackSpeedTicks = 4)
        val result = combatAction.process(currentTick = 1L)

        assertFalse(
            result,
            "Dead NPC's NpcCombatAction must return false (cancel). " +
                "Source: https://oldschool.runescape.wiki/w/Non-player_character"
        )
        assertEquals(
            hpBefore,
            player.currentHp,
            "Dead NPC must deal 0 damage to the player. " +
                "Source: https://oldschool.runescape.wiki/w/Non-player_character"
        )
        assertTrue(
            queue.scheduled.isEmpty(),
            "Dead NPC must not schedule any combat events. " +
                "Source: https://oldschool.runescape.wiki/w/Non-player_character"
        )
    }
}
