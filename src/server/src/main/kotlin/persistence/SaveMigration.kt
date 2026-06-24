package persistence

import com.google.gson.JsonObject

/**
 * Contract for a single schema migration step.
 *
 * Each migration transforms a raw [JsonObject] from [from] version to [to] version.
 * Migrations must be idempotent: running the same migration twice on already-migrated
 * data must produce the same result.
 *
 * Source: server-persistence.md — Save Format Versioning
 */
interface SaveMigration {
    val from: Int
    val to: Int
    fun migrate(save: JsonObject): JsonObject
}

/**
 * Applies all registered migrations in ascending order starting from [fromVersion].
 *
 * Usage:
 *   val migrated = MigrationRunner.migrate(jsonObject, savedVersion)
 *   val save = gson.fromJson(migrated, PlayerSave::class.java)
 *
 * To add a new migration (e.g. v1 → v2):
 *   1. Bump PlayerSave.CURRENT_VERSION to 2.
 *   2. Implement SaveMigration with from=1, to=2.
 *   3. Add an instance to the [migrations] list below.
 */
object MigrationRunner {
    /**
     * Ordered list of all registered schema migrations.
     * v1 is the initial schema — no migrations exist yet.
     */
    private val migrations: List<SaveMigration> = emptyList()

    /**
     * Runs all migrations whose [SaveMigration.from] >= [fromVersion], in ascending order.
     * Returns the (possibly transformed) JsonObject at the current schema version.
     */
    fun migrate(json: JsonObject, fromVersion: Int): JsonObject {
        var result = json
        for (m in migrations.filter { it.from >= fromVersion }.sortedBy { it.from }) {
            result = m.migrate(result)
        }
        return result
    }
}
