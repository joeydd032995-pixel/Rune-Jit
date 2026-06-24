package world

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Loads OSRS cache index-5 map files (`m{regionX}_{regionY}`) and applies terrain clip
 * flags into [clip] via [MapFileDecoder].
 *
 * ## Cache access
 * Index-5 files are read from the standard OSRS cache layout:
 *  - `main_file_cache.dat2`  — sector data store
 *  - `main_file_cache.idx5`  — index-5 table: 6 bytes per file entry
 *    (3-byte size, 3-byte start sector), file ID determined by name-hash lookup.
 *
 * ## Name-hash limitation (current)
 * Resolving a named file (e.g. `m50_50`) to its integer file ID requires the archive's
 * name-hash table from cache index 255. That meta-index is not decoded yet. Until a
 * name-hash resolver is wired in, [loadInto] attempts the read and gracefully no-ops
 * on any error, leaving the region fully walkable. This matches the
 * JS5Handler / PacketRegistry graceful-absent contract.
 *
 * ## XTEA
 * Map (`m`) files are NOT XTEA-encrypted. Only landscape (`l`) files require XTEA keys.
 * Object/wall clipping from landscape files is deferred — see [applyObjectClipping].
 *
 * Source: https://github.com/RuneStar/cache-names (name-hash format)
 */
class RegionLoader(
    private val cacheDir: Path,
    private val clip: ArrayCollisionMap,
) {
    private val log = LoggerFactory.getLogger(RegionLoader::class.java)

    private val idxPath = cacheDir.resolve("main_file_cache.idx5")
    private val datPath = cacheDir.resolve("main_file_cache.dat2")

    /**
     * Attempt to load terrain clip flags for [regionId] from the cache.
     *
     * On any error (missing cache files, IO failure, parse error, missing name-hash
     * resolver) this method logs a warning and returns without modifying [clip],
     * leaving the region walkable. Never throws.
     */
    fun loadInto(regionId: Int) {
        if (!idxPath.exists() || !datPath.exists()) {
            log.debug("Cache index-5 not present — region $regionId left walkable")
            return
        }
        val regionX = regionId shr 8
        val regionY = regionId and 0xFF
        try {
            // TODO: resolve file ID for "m{regionX}_{regionY}" via cache index-255
            //       name-hash table. Until name-hash decoding is implemented, this
            //       path cannot produce useful data but must not throw.
            //
            // When the resolver is available, replace the stub below with:
            //   val fileId = nameHashResolver.resolve(5, "m${regionX}_${regionY}") ?: return
            //   val bytes = readCacheFile(fileId) ?: return
            //   MapFileDecoder.decodeInto(bytes, regionX, regionY, clip)
            log.debug(
                "Region ($regionX,$regionY) id=$regionId: name-hash resolver not yet " +
                "available — terrain left walkable until index-255 is decoded"
            )
        } catch (e: Exception) {
            log.warn("Failed to load map region ($regionX,$regionY): ${e.message}")
        }
    }

    /**
     * Applies wall/object clip flags from the landscape (`l{regionX}_{regionY}`) file.
     *
     * TODO STUB: object/wall clipping requires [ObjectDefinitions] from cache index 2
     * (config archive), which are not decoded yet. Object interactions also need XTEA keys
     * for landscape file decryption. This method is intentionally empty until both
     * prerequisites are provided by a future skill.
     */
    @Suppress("UNUSED_PARAMETER")
    fun applyObjectClipping(regionId: Int) {
        // Deferred: object/wall clip flags need cache index-2 ObjectDefinitions and XTEA.
        // See world-region-loader system doc for the full landscape decode spec.
    }

    /**
     * Reads a raw data block from the OSRS cache using the standard idx/dat2 sector format.
     *
     * Sector math mirrors [net.JS5Handler.readCacheFile]:
     *  - idx entry: 6 bytes at offset (fileId * 6): [size:3][startSector:3]
     *  - dat2 sector: 520 bytes at offset (sector * 520): [header:8][data:512]
     *
     * Returns null if the file entry is missing or any IO error occurs.
     */
    private fun readCacheFile(fileId: Int): ByteArray? {
        return try {
            idxPath.toFile().inputStream().use { idx ->
                idx.skip(fileId.toLong() * 6L)
                val header = ByteArray(6)
                if (idx.read(header) < 6) return null
                val size = (header[0].toInt() and 0xFF shl 16) or
                           (header[1].toInt() and 0xFF shl 8)  or
                           (header[2].toInt() and 0xFF)
                val sector = (header[3].toInt() and 0xFF shl 16) or
                             (header[4].toInt() and 0xFF shl 8)  or
                             (header[5].toInt() and 0xFF)
                if (size == 0 || sector == 0) return null
                datPath.toFile().inputStream().use { dat ->
                    dat.skip(sector.toLong() * 520L)
                    val data = ByteArray(size)
                    val read = dat.read(data)
                    if (read < size) null else data
                }
            }
        } catch (e: Exception) {
            log.warn("readCacheFile($fileId): ${e.message}")
            null
        }
    }
}
