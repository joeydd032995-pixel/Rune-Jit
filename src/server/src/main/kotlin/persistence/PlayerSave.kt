package persistence

/**
 * Serialisable snapshot of a player's state, written as JSON to data/players/[username].json.
 *
 * Schema versioning: bump [CURRENT_VERSION] and add a migration in [MigrationRunner] for every
 * breaking schema change. All fields reflect values documented in the player save schema.
 *
 * Source: server-persistence.md — Save Format Versioning
 */
data class PlayerSave(
    val schemaVersion: Int = CURRENT_VERSION,
    val username: String,
    val x: Int,
    val y: Int,
    val plane: Int,
    /** XP per skill, keyed by [entity.Skill].name (e.g. "WOODCUTTING"). */
    val skillXp: Map<String, Double>,
    val prayerPoints: Int,
    /** 28 inventory slots; null entry = empty slot. */
    val inventory: List<SavedItem?>,
    /** 14 equipment slots; -1 = empty. */
    val equipment: List<Int>,
    val currentHp: Int,
) {
    companion object {
        /** Increment on every breaking schema change; add a migration in MigrationRunner. */
        const val CURRENT_VERSION = 1
    }
}

/**
 * Compact representation of a single item stack stored in inventory or bank.
 * Source: https://oldschool.runescape.wiki/w/Inventory
 */
data class SavedItem(val itemId: Int, val quantity: Int)
