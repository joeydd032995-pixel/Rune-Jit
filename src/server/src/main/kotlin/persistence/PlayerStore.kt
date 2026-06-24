package persistence

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import entity.Player
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * File-based player save/load system.
 *
 * Save files are stored as JSON in data/players/[username].json.
 * Writes are atomic: the serialised JSON is written to a .tmp file first, then
 * renamed over the target with ATOMIC_MOVE to prevent corruption on process crash.
 *
 * Schema versioning: the saved schemaVersion is compared against
 * [PlayerSave.CURRENT_VERSION]; if older, [MigrationRunner.migrate] upgrades it
 * before deserialisation.
 *
 * Thread safety: [save] and [load] may be called from the async save scheduler
 * (off the tick thread). Gson and Path operations are thread-safe here because
 * each call constructs its own Gson instance and there is no shared mutable state.
 *
 * Source: server-persistence.md — Atomic Writes
 * Source: https://oldschool.runescape.wiki/w/Account (player data persistence concept)
 */
object PlayerStore {
    private val log = LoggerFactory.getLogger(PlayerStore::class.java)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    private val saveDir: Path = Path.of("data/players")

    /**
     * Serialises [player] to JSON and atomically writes to data/players/[username].json.
     *
     * Atomic write protocol (per server-persistence.md):
     *   1. Write JSON to [username].json.tmp
     *   2. Rename .tmp -> .json with ATOMIC_MOVE + REPLACE_EXISTING
     *
     * Errors are logged but not re-thrown — a failed save must never crash the tick loop.
     * Source: server-persistence.md — Atomic Writes
     */
    fun save(player: Player) {
        try {
            Files.createDirectories(saveDir)
            val save = PlayerSerializer.serialize(player)
            val json = gson.toJson(save)
            val target = saveDir.resolve("${player.username}.json")
            val temp = saveDir.resolve("${player.username}.json.tmp")
            temp.toFile().writeText(json)
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            log.debug("Saved player {}", player.username)
        } catch (e: Exception) {
            log.error("Failed to save player {}", player.username, e)
        }
    }

    /**
     * Loads and deserialises a player save from data/players/[username].json into [player].
     *
     * If the save file does not exist, returns false — the caller treats this as a new player.
     * If the saved schema version is older than [PlayerSave.CURRENT_VERSION], migrations are
     * applied before deserialisation so old files are always forward-compatible.
     *
     * Returns true on success, false if the file is missing or an error occurs.
     */
    fun load(username: String, player: Player): Boolean {
        val target = saveDir.resolve("$username.json")
        if (!target.toFile().exists()) return false
        return try {
            val raw = target.toFile().readText()
            val jsonObj = gson.fromJson(raw, JsonObject::class.java)
            val version = jsonObj.get("schemaVersion")?.asInt ?: 1
            val migrated = MigrationRunner.migrate(jsonObj, version)
            // Deserialise the migrated JSON back to PlayerSave.
            // We use a TypeToken-less fromJson because PlayerSave contains only
            // primitives, Strings, and generic collections — Gson handles this correctly.
            val save = gson.fromJson(migrated, PlayerSave::class.java)
            PlayerSerializer.deserialize(save, player)
            log.debug("Loaded player {}", username)
            true
        } catch (e: Exception) {
            log.error("Failed to load player {}", username, e)
            false
        }
    }
}
