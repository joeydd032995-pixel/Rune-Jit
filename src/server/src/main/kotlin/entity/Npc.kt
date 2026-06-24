package entity

/**
 * Minimal NPC entity — HP tracking, defence bonuses, and world coordinates.
 * Full AI, pathfinding, and aggression logic deferred to npc-behavior-simulator.
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
    maxHp: Int,
) {
    var currentHp: Int = maxHp
        private set

    val isDead: Boolean get() = currentHp <= 0

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
    }

    // -------------------------------------------------------------------------
    // World position — added by world-region-loader (Agent 1)
    // Full NPC movement / AI wired by npc-behavior-simulator (deferred)
    // -------------------------------------------------------------------------

    /** World tile X position. */
    var x: Int = 0

    /** World tile Y position (y+ = NORTH). */
    var y: Int = 0

    /** Plane / floor level (0..3). */
    var plane: Int = 0

    /**
     * Footprint size in tiles (1 for most NPCs; larger for bosses such as KQ, Olm).
     * Used by BFS pathfinding to check all occupied tiles for clearance.
     * Actual size loaded from NPC definitions — stub defaults to 1.
     */
    val size: Int = 1

    /** Packed world coordinate (x, y, plane) as a [world.Coordinate] value. */
    val coordinate: world.Coordinate get() = world.Coordinate(x, y, plane)
}
