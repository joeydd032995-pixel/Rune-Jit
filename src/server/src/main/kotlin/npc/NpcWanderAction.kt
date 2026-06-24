package npc

import engine.TickEvent
import engine.TickQueue
import entity.Npc
import world.Coordinate
import world.World
import kotlin.random.Random

/**
 * Self-rescheduling tick event that makes an NPC wander randomly within
 * [wanderRadius] tiles of its spawn point.
 *
 * Movement probability: 1-in-8 per tick (~12.5%), matching the OSRS server's
 * NPC wander rate. If the cache is absent ([World.isInitialized] = false) the
 * NPC steps directly to the target tile (graceful-absent pattern).
 *
 * Source: https://oldschool.runescape.wiki/w/Non-player_character#Wandering
 */
class NpcWanderAction(
    private val npc: Npc,
    private val tickQueue: TickQueue,
    private val wanderRadius: Int = 5,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active || npc.isDead) return false
        maybeWander()
        tickQueue.schedule(1, this)
        return true
    }

    private fun maybeWander() {
        // ~12.5% chance to move each tick — NPCs do not move every tick.
        // Source: https://oldschool.runescape.wiki/w/Non-player_character#Wandering
        if (Random.nextInt(8) != 0) return

        val dx = Random.nextInt(-wanderRadius, wanderRadius + 1)
        val dy = Random.nextInt(-wanderRadius, wanderRadius + 1)
        val targetX = (npc.spawnX + dx).coerceIn(npc.spawnX - wanderRadius, npc.spawnX + wanderRadius)
        val targetY = (npc.spawnY + dy).coerceIn(npc.spawnY - wanderRadius, npc.spawnY + wanderRadius)

        if (!World.isInitialized) {
            // Cache absent — move directly (graceful-absent pattern).
            // Clip flags not available; step is considered walkable.
            npc.x = targetX
            npc.y = targetY
            return
        }

        val target = Coordinate(targetX, targetY, npc.plane)
        val path = World.pathFinder.findPath(npc.coordinate, target, npc.size)
        if (path.tiles.isNotEmpty()) {
            val step = path.tiles.first()
            npc.x = step.x
            npc.y = step.y
        }
    }

    /** Stops this wander action from rescheduling itself. */
    fun cancel() { active = false }
}
