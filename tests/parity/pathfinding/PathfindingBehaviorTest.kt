package pathfinding

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import world.ArrayCollisionMap
import world.ClipFlag
import world.Coordinate
import world.door.ClipDoorHandler
import world.door.DoorState
import world.pathfinding.BreadthFirstSearch
import world.pathfinding.Path

/**
 * Algorithm behaviour tests for the BFS pathfinder.
 *
 * All tests use [ArrayCollisionMap] + [BreadthFirstSearch] directly — no World singleton.
 * Per tests-parity.md rules, sources cite rsmod community reference or OSRS wiki.
 *
 * Algorithm source: rsmod BFS reference — https://github.com/rsmod/rsmod
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PathfindingBehaviorTest {

    // -------------------------------------------------------------------------
    // Unreachable target returns closest reachable tile
    // -------------------------------------------------------------------------

    /**
     * When the exact target is walled off, BFS returns the closest reachable tile
     * (minimum Chebyshev distance to target) with reachedTarget=false.
     *
     * Source: rsmod BFS reference — partial path to closest reachable tile (SKILL Phase 6)
     * https://github.com/rsmod/rsmod
     */
    @Test fun `unreachable target returns closest reachable tile`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        // Surround the target at (3230, 3218) with an impassable box
        val target = Coordinate(3230, 3218)
        // Wall all approaches to target: block movement flags on tiles adjacent to it
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val adj = Coordinate(target.x + dx, target.y + dy)
                map.addFlag(adj, ClipFlag.BLOCK_MOVEMENT_FULL)
            }
        }
        // Also mark the target tile itself as fully blocked
        map.addFlag(target, ClipFlag.BLOCK_MOVEMENT_FULL)

        val start = Coordinate(3222, 3218)
        val path  = bfs.findPath(start, target)

        assertFalse(path.reachedTarget,
            "Path to a fully walled-off target must return reachedTarget=false. " +
            "Source: rsmod BFS reference — partial path to closest reachable tile (SKILL Phase 6)")
        assertFalse(path.isEmpty,
            "Partial path must not be empty — player should move toward the target. " +
            "Source: rsmod BFS reference (SKILL Phase 6)")

        // The last tile of the partial path must be the closest reachable tile to the target
        val lastTile = path.tiles.last()
        val lastChebyshev = lastTile.chebyshev(target)
        // Verify no other reachable tile in the path is closer to target
        for (tile in path.tiles) {
            assertTrue(tile.chebyshev(target) >= lastChebyshev,
                "Last tile of partial path must minimise Chebyshev distance to target. " +
                "Found tile $tile with chebyshev=${tile.chebyshev(target)} < lastChebyshev=$lastChebyshev. " +
                "Source: rsmod BFS reference (SKILL Phase 6)")
        }
    }

    // -------------------------------------------------------------------------
    // Fully enclosed start returns EMPTY path
    // -------------------------------------------------------------------------

    /**
     * When the start tile is completely surrounded by impassable tiles, BFS cannot move
     * at all and returns Path.EMPTY (walk-in-place behaviour).
     *
     * Source: SKILL Error Recovery — walk-in-place
     */
    @Test fun `fully enclosed start returns empty path`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        val start = Coordinate(3222, 3218)

        // Block all 8 neighbours of start (both incoming and outgoing movement flags)
        // Block movement OUT of start in all 8 directions
        map.addFlag(start, ClipFlag.BLOCK_MOVEMENT_NORTH or ClipFlag.BLOCK_MOVEMENT_EAST or
                ClipFlag.BLOCK_MOVEMENT_SOUTH or ClipFlag.BLOCK_MOVEMENT_WEST or
                ClipFlag.BLOCK_MOVEMENT_NORTH_EAST or ClipFlag.BLOCK_MOVEMENT_SOUTH_EAST or
                ClipFlag.BLOCK_MOVEMENT_SOUTH_WEST or ClipFlag.BLOCK_MOVEMENT_NORTH_WEST)

        val target = Coordinate(3225, 3218)
        val path   = bfs.findPath(start, target)

        assertEquals(Path.EMPTY, path,
            "Fully enclosed start must return Path.EMPTY (walk-in-place). " +
            "Source: SKILL Error Recovery — walk-in-place")
    }

    // -------------------------------------------------------------------------
    // Door toggle opens a blocked path
    // -------------------------------------------------------------------------

    /**
     * A door represented as a wall clip flag blocks the path; removing the flag
     * (door open) allows a shorter/direct path through.
     *
     * Source: SKILL Phase 4 — dynamic clip toggle
     */
    @Test fun `door toggle opens a blocked path`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        // Corridor: start=(3222,3218), target=(3224,3218)
        // Place a BLOCK_MOVEMENT_EAST wall on (3222,3218) and reciprocal on (3223,3218)
        // simulating a door that blocks the eastward passage
        val doorTile = Coordinate(3222, 3218)
        val doorMask = ClipFlag.BLOCK_MOVEMENT_EAST
        map.addFlag(doorTile, doorMask)
        map.addFlag(Coordinate(3223, 3218), ClipFlag.BLOCK_MOVEMENT_WEST)

        val start  = Coordinate(3222, 3218)
        val target = Coordinate(3224, 3218)

        // With door closed: path must route around (longer) or fail to reach
        val closedPath = bfs.findPath(start, target)
        // With walls the direct east path is blocked; routing around may or may not succeed
        // depending on whether there is a free route. We only need the open state to be shorter.
        val closedSteps = closedPath.tiles.size

        // Open the door: remove the clip flags
        val door = DoorState(tile = doorTile, wallMask = doorMask)
        val doorHandler = ClipDoorHandler(map)
        doorHandler.open(door)
        // Also remove the reciprocal flag
        map.removeFlag(Coordinate(3223, 3218), ClipFlag.BLOCK_MOVEMENT_WEST)

        val openPath = bfs.findPath(start, target)
        assertTrue(openPath.reachedTarget,
            "After opening door (clip flag removed), path must reach target. " +
            "Source: SKILL Phase 4 — dynamic clip toggle")
        // Direct path is exactly 2 steps east (through the door corridor)
        assertEquals(2, openPath.tiles.size,
            "After door opens, direct path is 2 steps east. " +
            "Source: SKILL Phase 4 — dynamic clip toggle")
        assertTrue(openPath.tiles.size <= closedSteps || !closedPath.reachedTarget,
            "Open-door path must be same length or shorter than closed-door path. " +
            "Source: SKILL Phase 4 — dynamic clip toggle")
    }

    // -------------------------------------------------------------------------
    // BFS is deterministic
    // -------------------------------------------------------------------------

    /**
     * Same BFS inputs (same map, start, target) must always produce identical tile lists.
     * OSRS pathfinding is deterministic; NEIGHBOUR_ORDER is fixed and the algorithm is BFS.
     *
     * Source: OSRS BFS determinism — rsmod reference; NEIGHBOUR_ORDER fixed
     * https://github.com/rsmod/rsmod
     */
    @Test fun `bfs is deterministic`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        // Add some walls to make the path non-trivial
        map.addFlag(Coordinate(3223, 3218), ClipFlag.BLOCK_MOVEMENT_FLOOR)
        map.addFlag(Coordinate(3223, 3219), ClipFlag.BLOCK_MOVEMENT_FLOOR)

        val start  = Coordinate(3222, 3218)
        val target = Coordinate(3226, 3220)

        val path1 = bfs.findPath(start, target)
        val path2 = bfs.findPath(start, target)

        assertEquals(path1.tiles, path2.tiles,
            "BFS must be deterministic: same inputs must produce identical tile lists. " +
            "Source: OSRS BFS determinism — rsmod reference; NEIGHBOUR_ORDER fixed")
        assertEquals(path1.directions, path2.directions,
            "BFS must be deterministic: same inputs must produce identical direction lists. " +
            "Source: OSRS BFS determinism — rsmod reference; NEIGHBOUR_ORDER fixed")
        assertEquals(path1.reachedTarget, path2.reachedTarget,
            "BFS must be deterministic: reachedTarget must match across calls. " +
            "Source: OSRS BFS determinism — rsmod reference; NEIGHBOUR_ORDER fixed")
    }

    // -------------------------------------------------------------------------
    // 64-tile path performance gate
    // -------------------------------------------------------------------------

    /**
     * A 64-tile path on an empty map must complete within the SKILL performance budget.
     * SKILL target: <5ms. CI bound: 50ms (lenient to avoid flaky failures on slow agents).
     *
     * Source: SKILL Phase 7 performance gate (<5ms/64 tiles; lenient CI bound)
     */
    @Tag("perf")
    @Test fun `64 tile path completes under budget`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        val start  = Coordinate(3222, 3218)
        val target = Coordinate(3222 + 64, 3218) // 64 tiles east — open map

        // Warm up JIT: run 50 iterations before timing
        repeat(50) { bfs.findPath(start, target) }

        val tStart = System.currentTimeMillis()
        val path   = bfs.findPath(start, target)
        val elapsedMs = System.currentTimeMillis() - tStart

        assertTrue(path.reachedTarget,
            "64-tile path on open map must reach target. " +
            "Source: SKILL Phase 7 performance gate")
        assertEquals(64, path.tiles.size,
            "64-tile path must have exactly 64 steps. " +
            "Source: SKILL Phase 7 performance gate")
        assertTrue(elapsedMs < 50,
            "64-tile BFS must complete in <50ms on CI (SKILL target <5ms). " +
            "Elapsed: ${elapsedMs}ms. Source: SKILL Phase 7 performance gate (<5ms/64 tiles; lenient CI bound)")
    }
}
