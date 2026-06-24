package skills.fishing

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

/**
 * Fishing method enum — maps to the right-click option shown on a fishing spot NPC.
 * Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots
 */
enum class FishingMethod {
    SMALL_NET,
    BIG_NET,
    BAIT,
    FLY_FISH,
    CAGE,
    HARPOON,
    SANDWORMS,
    LOBSTER_POT,
}

/**
 * Definition for a single fishing spot type (e.g. SHARK, LOBSTER).
 * All fields are loaded from data/skills/fishing.yaml — none are hardcoded.
 */
data class FishingSpotDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val fishItemId: Int,
    val method: FishingMethod,
    val toolItemId: Int,
    val baitItemId: Int,    // 0 = no bait required
    val members: Boolean,
    /** Object IDs that trigger this spot's action — populated from cache after /load-osrs-cache-full */
    val objectIds: IntArray = IntArray(0),
    val wikiUrl: String,
)

/**
 * Definition for a fishing tool item.
 * Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_equipment
 */
data class FishingToolDef(
    val name: String,
    val itemId: Int,
)

/**
 * Configuration for the Heron pet drop.
 * Source: https://oldschool.runescape.wiki/w/Heron
 */
data class HeronPetConfig(
    val itemId: Int,
    val baseRate: Int,
    val scalesWithLevel: Boolean,
)

data class FishingMeta(
    val skill: String,
    val ticksPerAttempt: Int,
)

data class FishingConfig(
    val meta: FishingMeta,
    val spots: Map<String, FishingSpotDef>,
    val tools: List<FishingToolDef>,
    val pet: HeronPetConfig,
)

/** Parses data/skills/fishing.yaml into [FishingConfig]. No values are hardcoded here. */
object FishingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): FishingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val spots = parseSpots(raw["spots"] as Map<String, Map<String, Any>>)
        val tools = parseTools(raw["tools"] as List<Map<String, Any>>)
        val pet = parsePet(raw["pet"] as Map<String, Any>)

        return FishingConfig(meta, spots, tools, pet)
    }

    private fun parseMeta(m: Map<String, Any>) = FishingMeta(
        skill = m["skill"] as String,
        ticksPerAttempt = m["ticks_per_attempt"] as Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseSpots(map: Map<String, Map<String, Any>>): Map<String, FishingSpotDef> =
        map.mapValues { (name, m) ->
            FishingSpotDef(
                name = name,
                levelRequired = m["level_required"] as Int,
                xp = (m["xp"] as Number).toDouble(),
                fishItemId = m["fish_item_id"] as Int,
                method = FishingMethod.valueOf(m["method"] as String),
                toolItemId = m["tool_item_id"] as Int,
                baitItemId = m["bait_item_id"] as Int,
                members = m["members"] as Boolean,
                objectIds = ((m["object_ids"] as? List<Int>) ?: emptyList()).toIntArray(),
                wikiUrl = m["wiki"] as String,
            )
        }

    private fun parseTools(list: List<Map<String, Any>>): List<FishingToolDef> = list.map { m ->
        FishingToolDef(
            name = m["name"] as String,
            itemId = m["item_id"] as Int,
        )
    }

    private fun parsePet(m: Map<String, Any>) = HeronPetConfig(
        itemId = m["item_id"] as Int,
        baseRate = m["base_rate"] as Int,
        scalesWithLevel = m["scales_with_level"] as Boolean,
    )
}

/** Singleton config loaded once at plugin startup. */
object FishingDefs {
    lateinit var config: FishingConfig
        private set

    fun init(yamlPath: Path): FishingConfig {
        config = FishingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
