package world

/**
 * Decodes an OSRS cache index-5 map file (`m{regionX}_{regionY}`) into terrain clip flags.
 *
 * Map files are NOT XTEA-encrypted (only landscape `l{x}_{y}` files are).
 * Object/wall clipping from landscape files is deferred (needs ObjectDefinitions).
 *
 * ## File format (per-tile loop, 4 planes × 64 × 64 tiles)
 *
 * For each plane p ∈ 0..3, for each x ∈ 0..63, for each y ∈ 0..63:
 * ```
 * while (true) {
 *   val op = readUnsignedByte()
 *   if (op == 0)    break                      // tile done
 *   if (op == 1)  { readUnsignedByte(); break } // height byte, then done
 *   if (op <= 49) { readUnsignedByte() }        // overlay (1 extra byte)
 *   if (op <= 81)   settings[p][x][y] = op - 49  // settings nibble
 *   // else: underlay id already consumed as op
 * }
 * ```
 *
 * A tile is blocked when `(settings and 0x1) != 0`.
 *
 * ## Bridge/roof plane shift
 * If plane-1 tile at (x, y) has `(settings[1][x][y] and 0x2) != 0`, a blocked tile
 * on plane `p` is applied to plane `p - 1` instead (the tile is a bridge/overhang
 * that clips the floor below it, not itself).
 *
 * Source: rsmod / OpenRS2 cache format documentation.
 */
object MapFileDecoder {

    private const val PLANES   = 4
    private const val REGION_SIZE = 64

    /**
     * Decode [bytes] (raw index-5 map file) and write [ClipFlag.BLOCK_MOVEMENT_FLOOR] into
     * [clip] for every blocked terrain tile, using world coordinates derived from
     * ([regionX], [regionY]).
     *
     * Errors in the byte stream are propagated to the caller ([RegionLoader]) which wraps
     * this call in a try/catch and treats any exception as an empty/walkable region.
     */
    fun decodeInto(bytes: ByteArray, regionX: Int, regionY: Int, clip: ArrayCollisionMap) {
        // settings[plane][localX][localY]
        val settings = Array(PLANES) { Array(REGION_SIZE) { IntArray(REGION_SIZE) } }

        var pos = 0

        // --- Pass 1: parse all per-tile opcode loops and record settings bytes ---
        for (plane in 0 until PLANES) {
            for (x in 0 until REGION_SIZE) {
                for (y in 0 until REGION_SIZE) {
                    while (true) {
                        val op = bytes[pos++].toInt() and 0xFF
                        when {
                            op == 0 -> break                     // tile done
                            op == 1 -> { pos++; break }         // height byte then done
                            op <= 49 -> pos++                    // overlay id byte
                            op <= 81 -> settings[plane][x][y] = op - 49  // settings nibble
                            // else: underlay id already consumed as op, no extra byte
                        }
                    }
                }
            }
        }

        // --- Pass 2: apply BLOCK_MOVEMENT_FLOOR for blocked tiles ---
        for (plane in 0 until PLANES) {
            for (x in 0 until REGION_SIZE) {
                for (y in 0 until REGION_SIZE) {
                    if ((settings[plane][x][y] and 0x1) == 0) continue  // not blocked

                    // Bridge/overhang check: if plane-1 tile has bit 0x2 set, this
                    // blocked tile actually clips the plane below (bridge floor).
                    var actualPlane = plane
                    if (plane > 0 && (settings[1][x][y] and 0x2) != 0) {
                        actualPlane = plane - 1
                    }
                    if (actualPlane < 0) continue

                    val worldCoord = Coordinate(
                        x = regionX * REGION_SIZE + x,
                        y = regionY * REGION_SIZE + y,
                        plane = actualPlane
                    )
                    clip.addFlag(worldCoord, ClipFlag.BLOCK_MOVEMENT_FLOOR)
                }
            }
        }
    }
}
