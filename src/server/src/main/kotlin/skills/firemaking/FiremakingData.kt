package skills.firemaking

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class FmMeta(
    val skill: String,
    val ticksPerAttempt: Int,
    val tinderboxItemId: Int,
    val baseLightAnimation: Int?,
)

data class LogDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val itemId: Int,
    val wikiUrl: String,
)

data class FiremakingConfig(
    val meta: FmMeta,
    val logs: Map<String, LogDef>,
) {
    /** O(1) lookup by item ID — built once at load time. */
    val byItemId: Map<Int, LogDef> = logs.values.associateBy { it.itemId }
}

/** Parses data/skills/firemaking.yaml into [FiremakingConfig]. No values are hardcoded here. */
object FiremakingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): FiremakingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val logs = parseLogs(raw["logs"] as Map<String, Map<String, Any>>)

        return FiremakingConfig(meta, logs)
    }

    private fun parseMeta(m: Map<String, Any>) = FmMeta(
        skill              = m["skill"] as String,
        ticksPerAttempt    = m["ticks_per_attempt"] as Int,
        tinderboxItemId    = m["tinderbox_item_id"] as Int,
        baseLightAnimation = m["base_light_animation"] as? Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseLogs(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        LogDef(
            name          = name,
            levelRequired = m["level_required"] as Int,
            xp            = (m["xp"] as Number).toDouble(),
            itemId        = m["item_id"] as Int,
            wikiUrl       = m["wiki"] as? String ?: "",
        )
    }
}

/** Singleton config loaded once at plugin startup — same graceful-absent pattern as WoodcuttingDefs. */
object FiremakingDefs {
    lateinit var config: FiremakingConfig
        private set

    fun init(yamlPath: Path): FiremakingConfig {
        config = FiremakingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
