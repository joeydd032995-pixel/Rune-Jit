package entity

import engine.TickQueue
import engine.TickQueueImpl

/**
 * Minimal NPC entity — HP tracking, defence bonuses, and world coordinates.
 * Full AI, pathfinding, and aggression logic wired in the npc package.
 * Source: https://oldschool.runescape.wiki/w/Non-player_character
 */
class Npc(
    val name: String,
    val combatLevel: Int,
    val defenceLevel: Int,
    val defenceStab: Int = 0,
    val defenceSlash: Int = 0,
    val defenceCrush: Int = 0,
    val defenceMagic: Int = 0,
    val defenceRanged: Int = 0,
    val maxHp: Int,
    val npcId: Int = -1,
    x: Int = 0,
    y: Int = 0,
) {
    var currentHp: Int = maxHp
        private set

    val isDead: Boolean get() = currentHp <= 0

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
    }

    // -------------------------------------------------------------------------
    // World position — added by world-region-loader (Agent 1)
    // NPC movement / AI wired by npc-behavior-simulator
    // -------------------------------------------------------------------------

    /** World tile X position. */
    var x: Int = x

    /** World tile Y position (y+ = NORTH). */
    var y: Int = y

    /** Plane / floor level (0..3). */
    var plane: Int = 0

    /**
     * Spawn point X — the tile this NPC was placed at.
     * Wander and retreat actions use spawnX/spawnY to constrain movement radius.
     */
    var spawnX: Int = x

    /**
     * Spawn point Y — the tile this NPC was placed at.
     * Wander and retreat actions use spawnX/spawnY to constrain movement radius.
     */
    var spawnY: Int = y

    /**
     * Footprint size in tiles (1 for most NPCs; larger for bosses such as KQ, Olm).
     * Used by BFS pathfinding to check all occupied tiles for clearance.
     * Actual size loaded from NPC definitions — stub defaults to 1.
     */
    val size: Int = 1

    /** Packed world coordinate (x, y, plane) as a [world.Coordinate] value. */
    val coordinate: world.Coordinate get() = world.Coordinate(x, y, plane)

    // -------------------------------------------------------------------------
    // Tick queue — each NPC owns its own scheduler, mirroring Player.
    // All AI actions (wander, aggro, combat) are scheduled here.
    // See server-tick.md: NO Thread.sleep(); all scheduling via TickQueue.
    // -------------------------------------------------------------------------

    /** Per-NPC tick scheduler. All AI events are scheduled via this queue. */
    val tickQueue: TickQueue = TickQueueImpl()
}
