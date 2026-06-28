package skills.cooking

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class CookingMeta(
    val skill: String,
    val ticksPerAttempt: Int,
    val cookingGauntletsItemId: Int,
    val cookAnimation: Int?,
)

data class FoodDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val rawItemId: Int,
    val cookedItemId: Int,
    val burntItemId: Int,
    /** Level at/above which this food never burns on a range. */
    val stopBurnLevelRange: Int,
    /** Level at/above which this food never burns on a fire. */
    val stopBurnLevelFire: Int,
    /** Whether Cooking gauntlets reduce burn for this food (lobster/swordfish/shark). */
    val usesGauntlets: Boolean,
    /** Stop-burn level when wearing Cooking gauntlets; null when [usesGauntlets] is false. */
    val gauntletsStopBurnLevel: Int?,
    val wikiUrl: String,
)

data class CookingConfig(
    val meta: CookingMeta,
    val food: Map<String, FoodDef>,
    /** Fire object IDs (raw-food-on-fire). Empty until cache extraction. */
    val fireObjectIds: IntArray,
    /** Range object IDs offering the "Cook" option. Empty until cache extraction. */
    val rangeObjectIds: IntArray,
) {
    /** O(1) lookup by raw item ID — built once at load time. */
    val byRawItemId: Map<Int, FoodDef> = food.values.associateBy { it.rawItemId }
}

/** Parses data/skills/cooking.yaml into [CookingConfig]. No values are hardcoded here. */
object CookingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): CookingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val food = parseFood(raw["food"] as Map<String, Map<String, Any>>)
        val fireIds = parseIntList(raw["fire_object_ids"])
        val rangeIds = parseIntList(raw["range_object_ids"])

        return CookingConfig(meta, food, fireIds, rangeIds)
    }

    private fun parseMeta(m: Map<String, Any>) = CookingMeta(
        skill                  = m["skill"] as String,
        ticksPerAttempt        = m["ticks_per_attempt"] as Int,
        cookingGauntletsItemId = m["cooking_gauntlets_item_id"] as Int,
        cookAnimation          = m["cook_animation"] as? Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseFood(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        FoodDef(
            name                  = name,
            levelRequired         = m["level_required"] as Int,
            xp                    = (m["xp"] as Number).toDouble(),
            rawItemId             = m["raw_item_id"] as Int,
            cookedItemId          = m["cooked_item_id"] as Int,
            burntItemId           = m["burnt_item_id"] as Int,
            stopBurnLevelRange    = m["stop_burn_level_range"] as Int,
            stopBurnLevelFire     = m["stop_burn_level_fire"] as Int,
            usesGauntlets         = m["uses_gauntlets"] as? Boolean ?: false,
            gauntletsStopBurnLevel = m["gauntlets_stop_burn_level"] as? Int,
            wikiUrl               = m["wiki"] as String,
        )
    }

    private fun parseIntList(raw: Any?): IntArray =
        (raw as? List<*>)?.map { (it as Number).toInt() }?.toIntArray() ?: IntArray(0)
}

/** Singleton config loaded once at plugin startup — same pattern as WoodcuttingDefs/FiremakingDefs. */
object CookingDefs {
    lateinit var config: CookingConfig
        private set

    fun init(yamlPath: Path): CookingConfig {
        config = CookingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
