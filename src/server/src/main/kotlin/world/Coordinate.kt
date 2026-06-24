package world

import kotlin.math.abs
import kotlin.math.max

/**
 * A packed world-tile coordinate, wrapping x (15 bits), y (15 bits), and plane (2 bits)
 * into a single Int for allocation-free BFS bookkeeping.
 *
 * Coordinate system: x increases EAST, y increases NORTH, matching OSRS world coords.
 * Region grid: a region is 64×64 tiles; regionId = (regionX shl 8) or regionY.
 *
 * Source: https://oldschool.runescape.wiki/w/Coordinates
 */
@JvmInline
value class Coordinate private constructor(val packed: Int) {

    /**
     * Primary public constructor: pack x (0..32767), y (0..32767), plane (0..3).
     */
    constructor(x: Int, y: Int, plane: Int = 0) : this(
        ((x and 0x7FFF) shl 17) or ((y and 0x7FFF) shl 2) or (plane and 0x3)
    )

    /** World tile X (0..32767). */
    val x: Int get() = (packed ushr 17) and 0x7FFF

    /** World tile Y (0..32767). Y+ = NORTH. */
    val y: Int get() = (packed ushr 2) and 0x7FFF

    /** Plane / floor level (0..3). */
    val plane: Int get() = packed and 0x3

    /** Region column index: x shr 6. */
    val regionX: Int get() = x shr 6

    /** Region row index: y shr 6. */
    val regionY: Int get() = y shr 6

    /** Region ID: (regionX shl 8) or regionY. */
    val regionId: Int get() = (regionX shl 8) or regionY

    /** Local tile X within the region (0..63). */
    val localX: Int get() = x and 0x3F

    /** Local tile Y within the region (0..63). */
    val localY: Int get() = y and 0x3F

    /** Returns a new coordinate displaced by [dx] east and [dy] north on the same plane. */
    fun translate(dx: Int, dy: Int): Coordinate = Coordinate(x + dx, y + dy, plane)

    /** Returns a new coordinate displaced one step in [d] on the same plane. */
    fun translate(d: Direction): Coordinate = translate(d.dx, d.dy)

    /**
     * Chebyshev (chessboard) distance — diagonal counts as 1, matching OSRS tile distance.
     * Source: https://oldschool.runescape.wiki/w/Coordinates
     */
    fun chebyshev(other: Coordinate): Int = max(abs(x - other.x), abs(y - other.y))

    override fun toString(): String = "($x, $y, $plane)"
}
