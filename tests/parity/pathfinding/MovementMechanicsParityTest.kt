package pathfinding

import engine.TickQueueImpl
import entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import world.ArrayCollisionMap
import world.ClipFlag
import world.Coordinate
import world.Direction
import world.pathfinding.BreadthFirstSearch

/**
 * Parity tests for OSRS movement mechanics.
 *
 * All tests use [ArrayCollisionMap] + [BreadthFirstSearch] directly — no World singleton.
 * Each assertion cites the OSRS wiki or rsmod reference per tests-parity.md rules.
 *
 * Primary source: https://oldschool.runescape.wiki/w/Walking
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovementMechanicsParityTest {

    // -------------------------------------------------------------------------
    // Walk is one tile per tick
    // -------------------------------------------------------------------------

    /**
     * Walking moves exactly 1 tile per tick at 600ms/tick.
     *
     * Test approach: assert on Path shape — a single-step path has directions.size == 1
     * and the single tile is one step away from start. The MovementQueue drains this one
     * step on the next tick (self-scheduling pattern verified by TickEngineTest).
     *
     * Source: https://oldschool.runescape.wiki/w/Walking
     */
    @Test fun `walk is one tile per tick`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)
        val start = Coordinate(3222, 3218)
        val dest  = Coordinate(3223, 3218) // 1 tile east

        val path = bfs.findPath(start, dest)
        assertEquals(1, path.tiles.size,
            "Walking one tile east should produce exactly 1 step. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        assertEquals(1, path.directions.size,
            "Path directions size must match tiles size. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        assertTrue(path.reachedTarget,
            "One-tile path must reach target. See: https://oldschool.runescape.wiki/w/Walking")
        // Each step advances exactly one tile — MovementQueue applies dir.dx/dir.dy once per tick
        val dir = path.directions[0]
        assertEquals(dest.x, start.x + dir.dx,
            "Single step must land on destination X. See: https://oldschool.runescape.wiki/w/Walking")
        assertEquals(dest.y, start.y + dir.dy,
            "Single step must land on destination Y. See: https://oldschool.runescape.wiki/w/Walking")
    }

    // -------------------------------------------------------------------------
    // Diagonal step costs one tile
    // -------------------------------------------------------------------------

    /**
     * A diagonal move (e.g. NE) counts as exactly 1 tile of movement — same as a cardinal.
     * OSRS uses Chebyshev distance; diagonal is not penalised.
     *
     * Source: https://oldschool.runescape.wiki/w/Walking (diagonal movement)
     */
    @Test fun `diagonal step costs one tile`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)
        val start = Coordinate(3222, 3218)
        val dest  = Coordinate(3223, 3219) // 1 tile NE

        val path = bfs.findPath(start, dest)
        assertEquals(1, path.tiles.size,
            "Diagonal NE move should produce exactly 1 step (same cost as cardinal). " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        assertEquals(Direction.NORTH_EAST, path.directions[0],
            "Direct NE move should use NORTH_EAST direction. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        assertTrue(path.reachedTarget,
            "Diagonal one-tile path must reach target. See: https://oldschool.runescape.wiki/w/Walking")
    }

    // -------------------------------------------------------------------------
    // Basic 3-step walk
    // -------------------------------------------------------------------------

    /**
     * Walking 3 tiles east on a clear map produces a 3-step path.
     *
     * Source: https://oldschool.runescape.wiki/w/Walking
     */
    @Test fun `basic walk finds three step path`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)
        val start  = Coordinate(3222, 3218)
        val target = Coordinate(3225, 3218) // 3 tiles east

        val path = bfs.findPath(start, target)
        assertEquals(3, path.tiles.size,
            "3-tile eastward walk must produce 3 steps. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        assertTrue(path.reachedTarget,
            "3-tile walk on clear map must reach target. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        // Final tile must be the target
        assertEquals(target.x, path.tiles.last().x,
            "Final path tile must be at target X. See: https://oldschool.runescape.wiki/w/Walking")
        assertEquals(target.y, path.tiles.last().y,
            "Final path tile must be at target Y. See: https://oldschool.runescape.wiki/w/Walking")
    }

    // -------------------------------------------------------------------------
    // Wall forces route-around
    // -------------------------------------------------------------------------

    /**
     * A wall of blocked tiles between start and target forces the path to route around.
     * The path still reaches the target (reachedTarget = true) via the unblocked route.
     * No step in the path may land on a BLOCK_MOVEMENT_FLOOR tile (the pathfinder must
     * avoid blocked tiles entirely, not just detour around them).
     *
     * Source: https://oldschool.runescape.wiki/w/Walking (obstacle avoidance)
     */
    @Test fun `wall blocking routes around`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        // Place a tall vertical wall at x=3223, spanning y=3210..3230
        // This is wide enough that BFS cannot sneak around it at a nearby y;
        // the only route is via x=3224 (blocked from the west approach to x=3223).
        // The BLOCK_MOVEMENT_FLOOR flag prevents entering the wall tile from any direction.
        val blockedTiles = mutableSetOf<Coordinate>()
        for (wy in 3210..3230) {
            val wallTile = Coordinate(3223, wy)
            map.addFlag(wallTile, ClipFlag.BLOCK_MOVEMENT_FLOOR)
            blockedTiles.add(wallTile)
        }

        val start  = Coordinate(3222, 3218)
        val target = Coordinate(3225, 3218) // 3 tiles east, wall at x=3223

        val path = bfs.findPath(start, target)
        assertTrue(path.reachedTarget,
            "Path must route around the vertical wall and reach the target. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        // The path must be longer than 3 (detour required)
        assertTrue(path.tiles.size > 3,
            "Path past a wall must be longer than the direct 3-tile path (detour needed). " +
            "Actual size: ${path.tiles.size}. See: https://oldschool.runescape.wiki/w/Walking")
        // No tile in the path may be a blocked tile
        for (tile in path.tiles) {
            assertFalse(tile in blockedTiles,
                "Path must never step onto a BLOCK_MOVEMENT_FLOOR tile. " +
                "Found blocked tile $tile in path. See: https://oldschool.runescape.wiki/w/Walking")
        }
    }

    // -------------------------------------------------------------------------
    // Cannot cut corner diagonally
    // -------------------------------------------------------------------------

    /**
     * Corner-cutting diagonals are forbidden in OSRS.
     * If the N tile from origin is blocked (BLOCK_MOVEMENT_NORTH on origin),
     * then the NE diagonal must also be forbidden — the path must use two cardinals (E then N)
     * or route around, but must never step diagonally through the blocked north edge.
     *
     * Source: https://oldschool.runescape.wiki/w/Walking (corner-cutting rule)
     */
    @Test fun `cannot cut corner diagonally`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        val start = Coordinate(3222, 3218)
        // Block northward movement from start: set BLOCK_MOVEMENT_NORTH on origin
        map.addFlag(start, ClipFlag.BLOCK_MOVEMENT_NORTH)
        // Also block southward entry to the N tile (double-sided wall for parity)
        val northTile = Coordinate(3222, 3219)
        map.addFlag(northTile, ClipFlag.BLOCK_MOVEMENT_SOUTH)

        val target = Coordinate(3223, 3219) // NE of start

        val path = bfs.findPath(start, target)
        // Path must not use NORTH_EAST as its first step (corner-cut forbidden)
        if (path.directions.isNotEmpty()) {
            assertFalse(path.directions[0] == Direction.NORTH_EAST,
                "Must not corner-cut NE when North is blocked from origin. " +
                "See: https://oldschool.runescape.wiki/w/Walking (diagonal/corner rule)")
        }
    }

    // -------------------------------------------------------------------------
    // Path clamped to 104 tiles
    // -------------------------------------------------------------------------

    /**
     * OSRS clamps path length to 104 tiles. Any path longer than 104 steps is truncated
     * and reachedTarget is false.
     *
     * Source: https://oldschool.runescape.wiki/w/Walking (path length limit)
     */
    @Test fun `path clamped to 104 tiles`() {
        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)

        val start  = Coordinate(3000, 3000)
        val target = Coordinate(3000 + 150, 3000) // 150 tiles east — exceeds 104

        val path = bfs.findPath(start, target)
        assertEquals(104, path.tiles.size,
            "Path must be clamped to 104 tiles when target is beyond 104 tiles. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
        assertFalse(path.reachedTarget,
            "reachedTarget must be false when path is truncated at 104 tiles. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
    }

    // -------------------------------------------------------------------------
    // Run moves 2 tiles per tick — DEFERRED
    // -------------------------------------------------------------------------

    /**
     * Running moves 2 tiles per tick (1.2 seconds to travel 2 tiles).
     * Deferred pending run-energy / Agility integration.
     *
     * Source: https://oldschool.runescape.wiki/w/Run
     */
    @Disabled("run deferred — requires run-energy / Agility implementation")
    @Test fun `run moves two tiles per tick`() {
        // When run is implemented: ctrlKey=1 in WalkHandler drains 2 steps per tick.
        // MovementQueue.step() would call queue.schedule(1, walkEvent) after the first step
        // AND immediately process a second step in the same tick invocation.
        // Assert: after 1 tick with 2-step queue, player has moved 2 tiles.
        // Source: https://oldschool.runescape.wiki/w/Run
    }

    // -------------------------------------------------------------------------
    // MovementQueue: walk one step via TickQueueImpl
    // -------------------------------------------------------------------------

    /**
     * Verifies the MovementQueue actually moves the player one tile when the tick fires.
     *
     * Source: https://oldschool.runescape.wiki/w/Walking
     */
    @Test fun `movement queue advances player one tile per tick`() {
        val tickQueue = TickQueueImpl()
        val player = Player("test", tickQueue)
        player.x = 3222
        player.y = 3218

        val map = ArrayCollisionMap()
        val bfs = BreadthFirstSearch(map)
        val start  = Coordinate(3222, 3218)
        val target = Coordinate(3225, 3218) // 3 east

        val path = bfs.findPath(start, target)
        assertEquals(3, path.directions.size,
            "Test precondition: 3-step path east. See: https://oldschool.runescape.wiki/w/Walking")

        player.movement.walkTo(path)

        // Tick 1 — should advance by 1 tile
        tickQueue.tick(1)
        assertEquals(3223, player.x,
            "After tick 1, player must be at x=3223. See: https://oldschool.runescape.wiki/w/Walking")
        assertEquals(3218, player.y, "Y must be unchanged. See: https://oldschool.runescape.wiki/w/Walking")

        // Tick 2 — another tile
        tickQueue.tick(2)
        assertEquals(3224, player.x,
            "After tick 2, player must be at x=3224. See: https://oldschool.runescape.wiki/w/Walking")

        // Tick 3 — final tile
        tickQueue.tick(3)
        assertEquals(3225, player.x,
            "After tick 3, player must be at x=3225 (destination). " +
            "See: https://oldschool.runescape.wiki/w/Walking")

        // No more movement
        assertFalse(player.movement.isMoving,
            "MovementQueue must be empty after all steps consumed. " +
            "See: https://oldschool.runescape.wiki/w/Walking")
    }
}
