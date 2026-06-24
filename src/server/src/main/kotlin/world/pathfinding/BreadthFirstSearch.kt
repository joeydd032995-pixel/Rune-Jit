package world.pathfinding

import world.ClipFlag
import world.CollisionMap
import world.Coordinate
import world.Direction

/**
 * OSRS-accurate BFS pathfinder.
 *
 * Implements the canonical OSRS movement pathfinding algorithm using Breadth-First Search.
 * A* is explicitly NOT used — OSRS uses BFS and A* produces wrong paths vs the game engine
 * (parity failure). See CLAUDE.md critical rules.
 *
 * Algorithm features:
 * - Flat IntArray grids (no per-node allocation) for O(1) visited/distance lookup.
 * - Neighbour expansion strictly follows [Direction.NEIGHBOUR_ORDER] (cardinals then diagonals)
 *   for determinism matching the game server.
 * - Diagonal moves forbidden when either cardinal component is blocked (corner-cutting rule).
 * - Clip flag checks use RuneLite CollisionDataFlag semantics (source and destination tile checks).
 * - Partial path: returns the closest reachable tile when the target is unreachable.
 * - 104-tile clamp: matches OSRS path length limit.
 *
 * Source: rsmod BFS reference — https://github.com/rsmod/rsmod
 * Clip parity: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/CollisionDataFlag.java
 */
class BreadthFirstSearch(
    private val clip: CollisionMap,
    private val maxPathTiles: Int = 104,
    private val searchRadius: Int = 104,
) : PathFinder {

    override fun findPath(start: Coordinate, target: Coordinate, size: Int): Path {
        // Same-tile: no movement needed
        if (start == target) return Path.EMPTY

        // Local grid dimensions: 2*searchRadius+1 square centered on start
        val side = 2 * searchRadius + 1
        val totalCells = side * side

        // distance[i] = BFS distance from start to local cell i; 0 = unvisited (start = 1)
        val distance = IntArray(totalCells) { 0 }
        // parentDir[i] = ordinal+1 of the Direction used to reach cell i; 0 = unvisited
        val parentDir = IntArray(totalCells) { 0 }

        // Origin in local coords
        val originLx = searchRadius
        val originLy = searchRadius

        fun localIndex(lx: Int, ly: Int): Int = lx * side + ly
        fun inBounds(lx: Int, ly: Int): Boolean = lx in 0 until side && ly in 0 until side

        val startIdx = localIndex(originLx, originLy)
        distance[startIdx] = 1 // mark start as visited (distance 1 so 0 == unvisited)

        // BFS frontier: store packed local indices as Ints in a ring-buffer style ArrayDeque
        val frontier = ArrayDeque<Int>(512)
        frontier.add(startIdx)

        // Track best partial: tile closest to target (by chebyshev), tie-broken by BFS dist
        var bestIdx = startIdx
        var bestChebyshev = start.chebyshev(target)
        var bestDist = 0

        val plane = start.plane

        // --- Helper: world coordinate from local index ---
        fun worldX(lx: Int): Int = start.x + (lx - originLx)
        fun worldY(ly: Int): Int = start.y + (ly - originLy)
        fun worldCoord(lx: Int, ly: Int): Coordinate = Coordinate(worldX(lx), worldY(ly), plane)

        // --- canMove predicates (RuneLite CollisionDataFlag parity) ---
        fun canMoveNorth(lx: Int, ly: Int): Boolean {
            val dly = ly + 1
            if (!inBounds(lx, dly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(lx, dly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or ClipFlag.BLOCK_MOVEMENT_SOUTH) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_NORTH)
        }

        fun canMoveEast(lx: Int, ly: Int): Boolean {
            val dlx = lx + 1
            if (!inBounds(dlx, ly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(dlx, ly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or ClipFlag.BLOCK_MOVEMENT_WEST) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_EAST)
        }

        fun canMoveSouth(lx: Int, ly: Int): Boolean {
            val dly = ly - 1
            if (!inBounds(lx, dly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(lx, dly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or ClipFlag.BLOCK_MOVEMENT_NORTH) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_SOUTH)
        }

        fun canMoveWest(lx: Int, ly: Int): Boolean {
            val dlx = lx - 1
            if (!inBounds(dlx, ly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(dlx, ly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or ClipFlag.BLOCK_MOVEMENT_EAST) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_WEST)
        }

        fun canMoveNorthEast(lx: Int, ly: Int): Boolean {
            if (!canMoveNorth(lx, ly) || !canMoveEast(lx, ly)) return false
            val dlx = lx + 1; val dly = ly + 1
            if (!inBounds(dlx, dly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(dlx, dly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or
                    ClipFlag.BLOCK_MOVEMENT_SOUTH_WEST or ClipFlag.BLOCK_MOVEMENT_SOUTH or ClipFlag.BLOCK_MOVEMENT_WEST) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_NORTH_EAST or ClipFlag.BLOCK_MOVEMENT_NORTH or ClipFlag.BLOCK_MOVEMENT_EAST)
        }

        fun canMoveSouthEast(lx: Int, ly: Int): Boolean {
            if (!canMoveSouth(lx, ly) || !canMoveEast(lx, ly)) return false
            val dlx = lx + 1; val dly = ly - 1
            if (!inBounds(dlx, dly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(dlx, dly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or
                    ClipFlag.BLOCK_MOVEMENT_NORTH_WEST or ClipFlag.BLOCK_MOVEMENT_NORTH or ClipFlag.BLOCK_MOVEMENT_WEST) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_SOUTH_EAST or ClipFlag.BLOCK_MOVEMENT_SOUTH or ClipFlag.BLOCK_MOVEMENT_EAST)
        }

        fun canMoveSouthWest(lx: Int, ly: Int): Boolean {
            if (!canMoveSouth(lx, ly) || !canMoveWest(lx, ly)) return false
            val dlx = lx - 1; val dly = ly - 1
            if (!inBounds(dlx, dly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(dlx, dly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or
                    ClipFlag.BLOCK_MOVEMENT_NORTH_EAST or ClipFlag.BLOCK_MOVEMENT_NORTH or ClipFlag.BLOCK_MOVEMENT_EAST) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_SOUTH_WEST or ClipFlag.BLOCK_MOVEMENT_SOUTH or ClipFlag.BLOCK_MOVEMENT_WEST)
        }

        fun canMoveNorthWest(lx: Int, ly: Int): Boolean {
            if (!canMoveNorth(lx, ly) || !canMoveWest(lx, ly)) return false
            val dlx = lx - 1; val dly = ly + 1
            if (!inBounds(dlx, dly)) return false
            val src = worldCoord(lx, ly)
            val dst = worldCoord(dlx, dly)
            return !clip.isFlagged(dst, ClipFlag.BLOCK_MOVEMENT_FLOOR or ClipFlag.BLOCK_MOVEMENT_OBJECT or
                    ClipFlag.BLOCK_MOVEMENT_SOUTH_EAST or ClipFlag.BLOCK_MOVEMENT_SOUTH or ClipFlag.BLOCK_MOVEMENT_EAST) &&
                   !clip.isFlagged(src, ClipFlag.BLOCK_MOVEMENT_NORTH_WEST or ClipFlag.BLOCK_MOVEMENT_NORTH or ClipFlag.BLOCK_MOVEMENT_WEST)
        }

        // For size > 1: also check that the leading edge tiles of the footprint along the
        // direction are clear. Size 1 (players) does not enter these loops.
        fun canMoveWithSize(lx: Int, ly: Int, dir: Direction): Boolean {
            if (size <= 1) {
                return when (dir) {
                    Direction.NORTH      -> canMoveNorth(lx, ly)
                    Direction.EAST       -> canMoveEast(lx, ly)
                    Direction.SOUTH      -> canMoveSouth(lx, ly)
                    Direction.WEST       -> canMoveWest(lx, ly)
                    Direction.NORTH_EAST -> canMoveNorthEast(lx, ly)
                    Direction.SOUTH_EAST -> canMoveSouthEast(lx, ly)
                    Direction.SOUTH_WEST -> canMoveSouthWest(lx, ly)
                    Direction.NORTH_WEST -> canMoveNorthWest(lx, ly)
                }
            }
            // Multi-tile: all footprint tiles on the leading edge must be passable
            return when (dir) {
                Direction.NORTH -> (0 until size).all { ox -> canMoveNorth(lx + ox, ly + size - 1) }
                Direction.EAST  -> (0 until size).all { oy -> canMoveEast(lx + size - 1, ly + oy) }
                Direction.SOUTH -> (0 until size).all { ox -> canMoveSouth(lx + ox, ly) }
                Direction.WEST  -> (0 until size).all { oy -> canMoveWest(lx, ly + oy) }
                Direction.NORTH_EAST -> {
                    val ok1 = (0 until size).all { ox -> canMoveNorth(lx + ox, ly + size - 1) }
                    val ok2 = (0 until size).all { oy -> canMoveEast(lx + size - 1, ly + oy) }
                    ok1 && ok2 && canMoveNorthEast(lx + size - 1, ly + size - 1)
                }
                Direction.SOUTH_EAST -> {
                    val ok1 = (0 until size).all { ox -> canMoveSouth(lx + ox, ly) }
                    val ok2 = (0 until size).all { oy -> canMoveEast(lx + size - 1, ly + oy) }
                    ok1 && ok2 && canMoveSouthEast(lx + size - 1, ly)
                }
                Direction.SOUTH_WEST -> {
                    val ok1 = (0 until size).all { ox -> canMoveSouth(lx + ox, ly) }
                    val ok2 = (0 until size).all { oy -> canMoveWest(lx, ly + oy) }
                    ok1 && ok2 && canMoveSouthWest(lx, ly)
                }
                Direction.NORTH_WEST -> {
                    val ok1 = (0 until size).all { ox -> canMoveNorth(lx + ox, ly + size - 1) }
                    val ok2 = (0 until size).all { oy -> canMoveWest(lx, ly + oy) }
                    ok1 && ok2 && canMoveNorthWest(lx, ly + size - 1)
                }
            }
        }

        // --- Target in local coords ---
        val targetLx = originLx + (target.x - start.x)
        val targetLy = originLy + (target.y - start.y)
        val targetInBounds = inBounds(targetLx, targetLy)
        val targetIdx = if (targetInBounds) localIndex(targetLx, targetLy) else -1

        var reachedTarget = false

        // --- BFS main loop ---
        bfsLoop@ while (frontier.isNotEmpty()) {
            val curIdx = frontier.removeFirst()
            val curLx = curIdx / side
            val curLy = curIdx % side
            val curDist = distance[curIdx]

            // Update best partial: closest to target by chebyshev, tie-break by BFS dist
            val curWorld = worldCoord(curLx, curLy)
            val cheby = curWorld.chebyshev(target)
            if (cheby < bestChebyshev || (cheby == bestChebyshev && curDist < bestDist)) {
                bestChebyshev = cheby
                bestDist = curDist
                bestIdx = curIdx
            }

            // Check if we reached target
            if (targetInBounds && curIdx == targetIdx) {
                reachedTarget = true
                bestIdx = curIdx
                break@bfsLoop
            }

            // Expand neighbours in canonical NEIGHBOUR_ORDER
            for (dir in Direction.NEIGHBOUR_ORDER) {
                if (!canMoveWithSize(curLx, curLy, dir)) continue
                val nLx = curLx + dir.dx
                val nLy = curLy + dir.dy
                if (!inBounds(nLx, nLy)) continue
                val nIdx = localIndex(nLx, nLy)
                if (distance[nIdx] != 0) continue // already visited
                distance[nIdx] = curDist + 1
                parentDir[nIdx] = dir.ordinal + 1 // 1-based so 0 = unvisited
                frontier.add(nIdx)
            }
        }

        // If best is start and we never moved, return EMPTY
        if (bestIdx == startIdx) return Path.EMPTY

        // --- Reconstruct path by walking parentDir back-pointers ---
        val rawDirs = ArrayDeque<Direction>()
        var cur = bestIdx
        while (cur != startIdx) {
            val dirOrdinal = parentDir[cur] - 1
            val dir = Direction.entries[dirOrdinal]
            rawDirs.addFirst(dir)
            // Step backward: subtract dir's delta from cur's local coords
            val cLx = cur / side
            val cLy = cur % side
            cur = localIndex(cLx - dir.dx, cLy - dir.dy)
        }

        // Build tiles and directions (truncate to maxPathTiles)
        val dirList = rawDirs.toList()
        val truncated = dirList.size > maxPathTiles
        val stepDirs = if (truncated) dirList.subList(0, maxPathTiles) else dirList

        val tileList = ArrayList<Coordinate>(stepDirs.size)
        var cx = start.x
        var cy = start.y
        for (d in stepDirs) {
            cx += d.dx
            cy += d.dy
            tileList.add(Coordinate(cx, cy, plane))
        }

        return Path(
            tiles = tileList,
            directions = stepDirs,
            reachedTarget = reachedTarget && !truncated,
        )
    }
}
