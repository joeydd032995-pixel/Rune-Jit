package world

/**
 * Collision / clip flag constants for the world tile collision map.
 *
 * Values match RuneLite's CollisionDataFlag for parity with cache-tool outputs.
 * Source: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/CollisionDataFlag.java
 *
 * Notes:
 *  - Projectile (FLY_BLOCKED) and multi-combat zone flags are intentionally absent.
 *    They affect different subsystems (ranged line-of-sight, PvP zone detection) and
 *    live in a separate map — deferred to their respective skills.
 *  - Object / wall flags (BLOCK_MOVEMENT_OBJECT) are OR-ed in by RegionLoader when
 *    landscape object clipping is available (currently a TODO stub — needs
 *    ObjectDefinitions from cache index 2).
 */
object ClipFlag {
    const val BLOCK_MOVEMENT_NORTH_WEST       = 0x1
    const val BLOCK_MOVEMENT_NORTH            = 0x2
    const val BLOCK_MOVEMENT_NORTH_EAST       = 0x4
    const val BLOCK_MOVEMENT_EAST             = 0x8
    const val BLOCK_MOVEMENT_SOUTH_EAST       = 0x10
    const val BLOCK_MOVEMENT_SOUTH            = 0x20
    const val BLOCK_MOVEMENT_SOUTH_WEST       = 0x40
    const val BLOCK_MOVEMENT_WEST             = 0x80
    const val BLOCK_MOVEMENT_OBJECT           = 0x100
    const val BLOCK_MOVEMENT_FLOOR_DECORATION = 0x40000
    const val BLOCK_MOVEMENT_FLOOR            = 0x200000

    /**
     * Aggregate flag marking a tile as fully impassable from all directions
     * (all 8 directional bits + object + floor).
     */
    const val BLOCK_MOVEMENT_FULL: Int =
        BLOCK_MOVEMENT_NORTH_WEST or
        BLOCK_MOVEMENT_NORTH      or
        BLOCK_MOVEMENT_NORTH_EAST or
        BLOCK_MOVEMENT_EAST       or
        BLOCK_MOVEMENT_SOUTH_EAST or
        BLOCK_MOVEMENT_SOUTH      or
        BLOCK_MOVEMENT_SOUTH_WEST or
        BLOCK_MOVEMENT_WEST       or
        BLOCK_MOVEMENT_OBJECT     or
        BLOCK_MOVEMENT_FLOOR
}
