package world

/**
 * Marker record for a loaded region. Terrain clip flags themselves live in
 * [ArrayCollisionMap] for performance (no per-tile object indirection).
 *
 * Region coordinate identity: regionId = (regionX shl 8) or regionY
 *   where regionX = worldX shr 6, regionY = worldY shr 6.
 * Each region covers a 64×64 tile area on each of the 4 planes.
 */
class Region(val regionId: Int) {
    /** Region column index (regionId shr 8). */
    val regionX: Int get() = regionId shr 8

    /** Region row index (regionId and 0xFF). */
    val regionY: Int get() = regionId and 0xFF

    override fun toString(): String = "Region(id=$regionId, rx=$regionX, ry=$regionY)"
}
