package world.pathfinding

import world.Coordinate
import world.Direction

/**
 * Result of a BFS pathfinding query.
 *
 * [tiles]         — ordered list of tile coordinates to visit, EXCLUDING the start tile.
 * [directions]    — one Direction per tile (same size as tiles), the step taken from the
 *                   previous tile to reach each tile in [tiles].
 * [reachedTarget] — true when the path ends exactly at the requested target tile;
 *                   false when the path is partial (best reachable approximation) or empty.
 *
 * Source: rsmod BFS reference — https://github.com/rsmod/rsmod
 */
data class Path(
    val tiles: List<Coordinate>,
    val directions: List<Direction>,
    val reachedTarget: Boolean,
) {
    val isEmpty: Boolean get() = tiles.isEmpty()

    companion object {
        /** Sentinel: no movement possible (start fully enclosed or same tile). */
        val EMPTY = Path(emptyList(), emptyList(), false)
    }
}
