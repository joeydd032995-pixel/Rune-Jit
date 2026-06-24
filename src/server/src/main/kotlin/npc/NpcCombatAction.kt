package npc

import combat.CombatFormulas
import combat.CombatStyle
import engine.TickEvent
import engine.TickQueue
import entity.Npc
import entity.Player
import world.World
import kotlin.math.abs

/**
 * Tick event that drives an NPC's melee combat against a [target] player.
 *
 * Behaviour:
 *  - Attacks once per [attackSpeedTicks] ticks (default 4 = 2.4 s, the standard NPC rate).
 *  - If the target is further than 10 tiles (Chebyshev) the action cancels — the NPC
 *    gives up the chase rather than following indefinitely.
 *  - If the target is within range but not adjacent, the NPC steps toward the target
 *    using BFS pathfinding (graceful-absent: direct step when cache unavailable).
 *  - Protect from Melee overhead prayer = 100% block in PvM.
 *    Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
 *    Source: https://oldschool.runescape.wiki/w/Prayer#Protection_prayers
 *
 * All scheduling uses [tickQueue] — no Thread.sleep() per server-tick.md rules.
 */
class NpcCombatAction(
    private val npc: Npc,
    private val target: Player,
    private val tickQueue: TickQueue,
    private val attackSpeedTicks: Int = 4,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active || npc.isDead || target.isDead) return false

        val dist = chebyshev(npc.x, npc.y, target.x, target.y)

        // Source: https://oldschool.runescape.wiki/w/Non-player_character#Following
        // NPCs abandon pursuit when target is more than ~10 tiles away.
        if (dist > 10) {
            active = false
            return false
        }

        if (dist > 1) {
            moveToward(target)
        } else {
            attack()
        }

        tickQueue.schedule(attackSpeedTicks, this)
        return true
    }

    private fun attack() {
        // Overhead protection prayer = 100% block in PvM.
        // Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
        // Source: https://oldschool.runescape.wiki/w/Prayer#Protection_prayers
        if (target.prayer.isProtectedFrom(CombatStyle.MELEE_AGGRESSIVE)) return

        // Use NPC combat level as simplified strength level input.
        // Full NPC stat definitions deferred to /import-osrsbox-complete.
        // Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit
        val maxHit = CombatFormulas.maxMeleeHit(
            strengthLevel = npc.combatLevel,
            strengthBonus = 0,
            prayerMult = 1.0,
        )
        if (maxHit <= 0) return

        val hit = CombatFormulas.rollDamage(maxHit)
        target.takeDamage(hit)
    }

    private fun moveToward(target: Player) {
        if (!World.isInitialized) {
            // Graceful-absent: step directly without clip-flag validation.
            if (npc.x < target.x) npc.x++ else if (npc.x > target.x) npc.x--
            if (npc.y < target.y) npc.y++ else if (npc.y > target.y) npc.y--
            return
        }
        val path = World.pathFinder.findPath(npc.coordinate, target.coordinate, npc.size)
        if (path.tiles.isNotEmpty()) {
            val step = path.tiles.first()
            npc.x = step.x
            npc.y = step.y
        }
    }

    private fun chebyshev(x1: Int, y1: Int, x2: Int, y2: Int): Int =
        maxOf(abs(x1 - x2), abs(y1 - y2))

    /** Stops this combat action from rescheduling itself. */
    fun cancel() { active = false }
}
