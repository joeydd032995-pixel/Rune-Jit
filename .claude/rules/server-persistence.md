---
path: src/server/persistence/**
---

# Server Persistence Rules

## Save Format Versioning

Every change to the player save schema requires a version bump and migration script:

```kotlin
// PlayerSave.kt
data class PlayerSave(
    val schemaVersion: Int = CURRENT_VERSION,
    // ... fields
) {
    companion object {
        const val CURRENT_VERSION = 3
    }
}

// Migration from v2 → v3
object SaveMigrationV2ToV3 : SaveMigration {
    override val from = 2
    override val to = 3

    override fun migrate(save: JsonObject): JsonObject {
        // v3 added questPoints field
        save.addProperty("questPoints", 0)
        return save
    }
}
```

## Atomic Writes

Player saves must be written atomically to prevent corruption:

```kotlin
// ❌ WRONG: Direct overwrite (corrupt if process dies mid-write)
fun save(player: Player) {
    File("data/players/${player.username}.json").writeText(toJson(player))
}

// ✅ CORRECT: Write to temp, rename atomically
fun save(player: Player) {
    val target = Path.of("data/players/${player.username}.json")
    val temp = target.parent.resolve("${player.username}.json.tmp")
    temp.writeText(toJson(player))
    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
}
```

## Auto-Save Interval

Players are auto-saved every 120 seconds (200 ticks). The save is scheduled as an async task off the main tick thread.

## Backup Retention

- Keep last 5 save files per player (`*.json.bak1`, `*.json.bak2`, etc.)
- On save: rotate backups before writing

## Prohibited

- No saving player data to cache/ or data/osrsbox/ directories
- No saving XTEA keys or session tokens to player files
- No schema changes without version bump + migration
- No synchronous file I/O on the tick thread
