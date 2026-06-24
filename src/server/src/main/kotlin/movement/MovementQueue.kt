package movement

import engine.TickEvent
import engine.TickQueue
import entity.Player
import world.pathfinding.Path

/**
 * Per-player movement queue. Drains one step per tick (walking speed).
 *
 * Walk speed: 1 tile per tick (0.6 seconds).
 * Source: https://oldschool.runescape.wiki/w/Walking
 *
 * Run (2 tiles/tick) is deferred pending run-energy/Agility integration.
 * The ctrlKey is parsed by WalkHandler but ignored here until then.
 *
 * Re-scheduling idiom matches WoodcuttingAction: the event reschedules itself
 * each tick rather than returning true (TickQueueImpl ignores the return value).
 * Source: server-tick.md — all scheduling via tick queue, no Thread.sleep.
 */
class MovementQueue(private val player: Player, private val queue: TickQueue) {

    private val steps = ArrayDeque<world.Direction>()
    private var active = false

    /**
     * Registered once; re-scheduled into the queue while steps remain.
     * Held as a field so [reset] can cancel it by identity.
     */
    private val walkEvent = TickEvent { tick -> step(tick) }

    /** True while there are pending movement steps. */
    val isMoving: Boolean get() = steps.isNotEmpty()

    /**
     * Replace any in-progress path with [path].
     * [path.directions] are the per-tile step directions (start tile excluded).
     *
     * A new walk click while moving cancels the current path and starts the new one,
     * exactly as OSRS does.
     * Source: https://oldschool.runescape.wiki/w/Walking
     */
    fun walkTo(path: Path) {
        steps.clear()
        steps.addAll(path.directions)
        if (!active && steps.isNotEmpty()) {
            active = true
            queue.schedule(1, walkEvent)
        }
    }

    /**
     * Stop all movement immediately (teleport, stun, death).
     * Source: https://oldschool.runescape.wiki/w/Stun
     */
    fun reset() {
        steps.clear()
        queue.cancel(walkEvent)
        active = false
    }

    /**
     * Advances the player one tile in the next queued direction.
     * Re-schedules itself while steps remain; deactivates when the queue drains.
     *
     * Walk = 1 tile per tick.
     * Source: https://oldschool.runescape.wiki/w/Walking
     */
    private fun step(@Suppress("UNUSED_PARAMETER") currentTick: Long): Boolean {
        val dir = steps.removeFirstOrNull()
        if (dir == null) {
            active = false
            return false
        }
        player.x += dir.dx
        player.y += dir.dy
        // Run (2 tiles/tick) deferred — ctrlKey parsed in WalkHandler but ignored here
        if (steps.isNotEmpty()) {
            queue.schedule(1, walkEvent)
        } else {
            active = false
        }
        return false // TickQueueImpl ignores return; we self-schedule above
    }
}
