package world

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks which regions have been attempted for loading and ensures each is loaded at most
 * once. If [loader] is null (cache absent), all regions are left walkable.
 *
 * Thread-safe: [ensureRegion] may be called from multiple threads concurrently.
 */
class RegionStore(
    private val loader: RegionLoader?,
    private val clip: ArrayCollisionMap,
) {
    private val log = LoggerFactory.getLogger(RegionStore::class.java)
    private val loaded: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    /**
     * Ensures [regionId] has been loaded into [clip]. If [loader] is null or the region
     * was already attempted, this is a no-op. Exceptions from the loader are caught and
     * logged — the region remains walkable (flags = 0).
     */
    fun ensureRegion(regionId: Int) {
        if (loader == null) return                 // cache absent → all tiles walkable
        if (!loaded.add(regionId)) return          // already attempted
        try {
            loader.loadInto(regionId)
            loader.applyObjectClipping(regionId)
        } catch (e: Exception) {
            log.warn("RegionStore: unexpected error loading region $regionId — region left walkable: ${e.message}")
        }
    }
}
