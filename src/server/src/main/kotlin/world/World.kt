package world

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Singleton world state holder. Initialised once at server startup by [net.GameServer].
 *
 * Holds:
 *  - [collision]    — sparse tile clip-flag store ([ArrayCollisionMap])
 *  - [regionStore]  — lazy region loader (no-ops when cache is absent)
 *  - [doors]        — dynamic door/gate clip toggler
 *  - [pathFinder]   — BFS pathfinder wired by pathfinding-engineer (Agent 2)
 *
 * Pattern mirrors [combat.CombatDefs]: lateinit + isInitialized guard.
 */
object World {
    private val log = LoggerFactory.getLogger(World::class.java)

    lateinit var collision: ArrayCollisionMap
        private set

    lateinit var regionStore: RegionStore
        private set

    lateinit var doors: world.door.DoorHandler
        private set

    /**
     * BFS pathfinder wired by pathfinding-engineer (Agent 2).
     * Uses OSRS BFS algorithm — NOT A* (see CLAUDE.md critical rules).
     * Source: rsmod community research — https://github.com/rsmod/rsmod
     */
    lateinit var pathFinder: world.pathfinding.PathFinder
        private set

    /**
     * Initialise the world. Called from [net.GameServer.start] right after
     * [net.PacketRegistry.init].
     *
     * If [cacheDir] exists and contains `main_file_cache.dat2`, a [RegionLoader] is
     * created and regions are loaded on demand. Otherwise all regions are left walkable
     * (flags = 0) — the graceful-absent contract.
     */
    fun init(cacheDir: Path = Path.of("cache")) {
        collision = ArrayCollisionMap()
        val dat2 = cacheDir.resolve("main_file_cache.dat2")
        val loader: RegionLoader? = if (dat2.exists()) {
            log.info("Cache found at $cacheDir — terrain loading enabled")
            RegionLoader(cacheDir, collision)
        } else {
            log.info("Cache not found at $cacheDir — all regions left walkable (run /load-osrs-cache-full to populate)")
            null
        }
        regionStore = RegionStore(loader, collision)
        doors = world.door.ClipDoorHandler(collision)
        pathFinder = world.pathfinding.BreadthFirstSearch(collision)
        log.info("World initialised (cache=${loader != null})")
    }

    /** True once [init] has been called. */
    val isInitialized: Boolean get() = ::collision.isInitialized
}
