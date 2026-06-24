package npc

import engine.TickEvent
import engine.TickQueue
import entity.Npc
import entity.Player
import kotlin.math.abs

/**
 * Tick event that scans nearby players every tick and initiates combat when
 * a player enters [aggroRadius] tiles (Chebyshev distance).
 *
 * Once a [NpcCombatAction] is started the aggro scan stops — the NPC commits
 * to that target. Callers may call [cancel] to fully stop both actions.
 *
 * OSRS aggression rules:
 *  - Radius: typically 5 tiles (Chebyshev), varies per NPC.
 *  - Only aggressive to players whose combat level is below the NPC's combat level * 2.
 *    (Simplified stub: always aggressive when in range; full check deferred to
 *    NpcDefinition loading via /import-osrsbox-complete.)
 *  - Aggression timer: NPCs de-aggro after 10 minutes in the same area.
 *    (Stub: timer not implemented; deferred.)
 *
 * Source: https://oldschool.runescape.wiki/w/Aggressive
 *
 * All scheduling uses [tickQueue] — no Thread.sleep() per server-tick.md rules.
 */
class NpcAggroAction(
    private val npc: Npc,
    private val tickQueue: TickQueue,
    private val aggroRadius: Int = 5,
    private val playerSource: () -> Collection<Player>,
) : TickEvent {

    private var active = true
    private var combatAction: NpcCombatAction? = null

    override fun process(currentTick: Long): Boolean {
        if (!active || npc.isDead) return false

        // Already in combat — let NpcCombatAction drive; just keep rescheduling
        // so we can detect when combat ends and resume scanning.
        if (combatAction != null) {
            tickQueue.schedule(1, this)
            return true
        }

        // Find the nearest living player within aggro radius.
        // Source: https://oldschool.runescape.wiki/w/Aggressive
        val nearest = playerSource()
            .filter { !it.isDead }
            .minByOrNull { chebyshev(npc.x, npc.y, it.x, it.y) }

        if (nearest != null && chebyshev(npc.x, npc.y, nearest.x, nearest.y) <= aggroRadius) {
            val action = NpcCombatAction(npc, nearest, tickQueue)
            combatAction = action
            tickQueue.schedule(1, action)
        }

        tickQueue.schedule(1, this)
        return true
    }

    private fun chebyshev(x1: Int, y1: Int, x2: Int, y2: Int): Int =
        maxOf(abs(x1 - x2), abs(y1 - y2))

    /** Stops aggro scanning and cancels any in-progress combat action. */
    fun cancel() {
        active = false
        combatAction?.cancel()
    }
}
