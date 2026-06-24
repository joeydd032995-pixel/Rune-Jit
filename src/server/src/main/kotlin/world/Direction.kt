package world

/**
 * Eight movement directions for OSRS tile-based movement.
 * Cardinals: NORTH, EAST, SOUTH, WEST (dx, dy relative to y+ = NORTH).
 * Diagonals: the four inter-cardinal directions.
 *
 * [NEIGHBOUR_ORDER] lists cardinals before diagonals — this is the order Agent 2's
 * BFS expands neighbours and must not be changed without updating the pathfinder.
 */
enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, 1),
    EAST(1, 0),
    SOUTH(0, -1),
    WEST(-1, 0),
    NORTH_EAST(1, 1),
    SOUTH_EAST(1, -1),
    SOUTH_WEST(-1, -1),
    NORTH_WEST(-1, 1);

    /** True if this direction is diagonal (both dx and dy are non-zero). */
    val isDiagonal: Boolean get() = dx != 0 && dy != 0

    /**
     * For a diagonal direction, returns the two cardinal directions whose clip flags
     * must BOTH be clear before a diagonal step is legal in OSRS.
     * For cardinals, returns `this to this`.
     *
     * OSRS diagonal movement check:
     *   step NE requires: can walk N from origin AND can walk E from origin.
     */
    val cardinalComponents: Pair<Direction, Direction>
        get() = when (this) {
            NORTH_EAST  -> NORTH to EAST
            SOUTH_EAST  -> SOUTH to EAST
            SOUTH_WEST  -> SOUTH to WEST
            NORTH_WEST  -> NORTH to WEST
            else        -> this to this
        }

    companion object {
        /**
         * Canonical BFS neighbour expansion order: cardinals first, then diagonals.
         * Agent 2's BFS path-reconstruction relies on this exact ordering.
         */
        val NEIGHBOUR_ORDER: List<Direction> = listOf(
            NORTH, EAST, SOUTH, WEST,
            NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST
        )
    }
}
