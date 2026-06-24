package world

import java.util.concurrent.ConcurrentHashMap

/**
 * Sparse, thread-safe collision map backed by a [ConcurrentHashMap] of [IntArray] regions.
 *
 * Storage layout:
 *  - Key: regionKey = (regionId shl 2) or plane
 *    where regionId = (regionX shl 8) or regionY, regionX = x shr 6, regionY = y shr 6
 *  - Value: IntArray(64 * 64) indexed by (localX * 64 + localY)
 *    where localX = x and 0x3F, localY = y and 0x3F
 *
 * Absent regions return flags = 0 (fully walkable), matching the OSRS "no cache → open world"
 * graceful-absent contract.
 */
class ArrayCollisionMap : CollisionMap {

    private val regions = ConcurrentHashMap<Int, IntArray>()

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun regionKey(x: Int, y: Int, plane: Int): Int {
        val regionId = ((x shr 6) shl 8) or (y shr 6)
        return (regionId shl 2) or (plane and 0x3)
    }

    private fun localIndex(x: Int, y: Int): Int =
        (x and 0x3F) * 64 + (y and 0x3F)

    /** Convenience overload keyed by Coordinate. Exposed for use by [RegionLoader]. */
    fun regionKeyFor(c: Coordinate): Int =
        (c.regionId shl 2) or (c.plane and 0x3)

    // -------------------------------------------------------------------------
    // CollisionMap implementation
    // -------------------------------------------------------------------------

    override fun flagsAt(x: Int, y: Int, plane: Int): Int {
        val key = regionKey(x, y, plane)
        val arr = regions[key] ?: return 0
        return arr[localIndex(x, y)]
    }

    override fun addFlag(c: Coordinate, mask: Int) {
        val key = regionKeyFor(c)
        val arr = regions.computeIfAbsent(key) { IntArray(4096) }
        val idx = localIndex(c.x, c.y)
        arr[idx] = arr[idx] or mask
    }

    override fun removeFlag(c: Coordinate, mask: Int) {
        val key = regionKeyFor(c)
        val arr = regions[key] ?: return
        val idx = localIndex(c.x, c.y)
        arr[idx] = arr[idx] and mask.inv()
    }
}
