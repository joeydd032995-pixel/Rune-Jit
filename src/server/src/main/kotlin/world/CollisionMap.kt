package world

/**
 * Abstraction over the tile collision flag store.
 *
 * Each tile carries an Int bitmask of [ClipFlag] constants. A tile with flags = 0 is
 * unconditionally walkable. The concrete implementation is [ArrayCollisionMap].
 */
interface CollisionMap {
    /** Returns the raw clip-flag bitmask for the tile at (x, y, plane), or 0 if absent. */
    fun flagsAt(x: Int, y: Int, plane: Int): Int

    /** Returns true if any bit of [mask] is set in the flags at [c]. */
    fun isFlagged(c: Coordinate, mask: Int): Boolean =
        (flagsAt(c.x, c.y, c.plane) and mask) != 0

    /** OR [mask] into the flags for tile [c], allocating storage if required. */
    fun addFlag(c: Coordinate, mask: Int)

    /** AND-NOT [mask] from the flags for tile [c]. No-op if the tile has no storage. */
    fun removeFlag(c: Coordinate, mask: Int)
}
