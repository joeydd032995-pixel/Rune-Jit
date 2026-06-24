package world.pathfinding

import world.Coordinate

/**
 * Contract for OSRS-parity pathfinding.
 *
 * OSRS uses BFS (breadth-first search), NOT A* — A* produces wrong paths vs the
 * actual game engine and fails parity (see CLAUDE.md critical rules).
 *
 * Source: rsmod community research — https://github.com/rsmod/rsmod
 */
interface PathFinder {
    /**
     * Find a path from [start] to [target] on the same plane.
     *
     * @param start  Origin tile (excluded from result tiles/directions).
     * @param target Destination tile.
     * @param size   Entity footprint in tiles (1 for players; 2 for 2×2 NPCs, etc.).
     * @return       [Path] with steps to reach (or approach) [target].
     *               Returns [Path.EMPTY] when no progress is possible.
     */
    fun findPath(start: Coordinate, target: Coordinate, size: Int = 1): Path
}
