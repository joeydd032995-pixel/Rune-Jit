package skills.smithing

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class SmithingMeta(
    val skill: String,
    val ticksPerAttempt: Int,
    val hammerItemId: Int,
    val furnaceObjectIds: IntArray,
    val anvilObjectIds: IntArray,
)

/** A single ore ingredient consumed when smelting a bar. */
data class IngredientDef(
    val oreItemId: Int,
    val qty: Int,
)

/** A smeltable bar: ores → bar at a furnace. */
data class BarDef(
    val name: String,
    val levelRequired: Int,
    val smeltXp: Double,
    val barItemId: Int,
    val ingredients: List<IngredientDef>,
    val wikiUrl: String,
)

/** A smithable item: bars → item at an anvil. */
data class SmithItemDef(
    val name: String,
    val levelRequired: Int,
    val smithXp: Double,
    val barType: String,
    val barsRequired: Int,
    val outputItemId: Int,
    val outputQty: Int,
    val wikiUrl: String,
)

data class SmithingConfig(
    val meta: SmithingMeta,
    val bars: Map<String, BarDef>,
    val smithing: Map<String, SmithItemDef>,
) {
    /** O(1) lookup: bar item ID → BarDef (used when bars are used on an anvil). */
    val barByItemId: Map<Int, BarDef> = bars.values.associateBy { it.barItemId }

    /** O(1) lookup: ore item ID → all bars that consume it (used when ore is used on a furnace). */
    val barsByOreItemId: Map<Int, List<BarDef>> =
        bars.values
            .flatMap { bar -> bar.ingredients.map { it.oreItemId to bar } }
            .groupBy({ it.first }, { it.second })

    /** O(1) lookup: bar type key → smithable items made from that bar (used at the anvil). */
    val smithByBarType: Map<String, List<SmithItemDef>> =
        smithing.values.groupBy { it.barType }
}

/** Parses data/skills/smithing.yaml into [SmithingConfig]. No values are hardcoded here. */
object SmithingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): SmithingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val bars = parseBars(raw["bars"] as Map<String, Map<String, Any>>)
        val smithing = parseSmithing(raw["smithing"] as Map<String, Map<String, Any>>)

        return SmithingConfig(meta, bars, smithing)
    }

    private fun parseMeta(m: Map<String, Any>) = SmithingMeta(
        skill = m["skill"] as String,
        ticksPerAttempt = m["ticks_per_attempt"] as Int,
        hammerItemId = m["hammer_item_id"] as Int,
        furnaceObjectIds = parseIntList(m["furnace_object_ids"]),
        anvilObjectIds = parseIntList(m["anvil_object_ids"]),
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseBars(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        BarDef(
            name = name,
            levelRequired = m["level_required"] as Int,
            smeltXp = (m["smelt_xp"] as Number).toDouble(),
            barItemId = m["bar_item_id"] as Int,
            ingredients = (m["ingredients"] as List<Map<String, Any>>).map { ing ->
                IngredientDef(
                    oreItemId = ing["ore_item_id"] as Int,
                    qty = ing["qty"] as Int,
                )
            },
            wikiUrl = m["wiki"] as String,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseSmithing(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        SmithItemDef(
            name = name,
            levelRequired = m["level_required"] as Int,
            smithXp = (m["smith_xp"] as Number).toDouble(),
            barType = m["bar_type"] as String,
            barsRequired = m["bars_required"] as Int,
            outputItemId = m["output_item_id"] as Int,
            outputQty = m["output_qty"] as Int,
            wikiUrl = m["wiki"] as String,
        )
    }

    private fun parseIntList(value: Any?): IntArray {
        val list = value as? List<*> ?: return IntArray(0)
        return list.mapNotNull { (it as? Number)?.toInt() }.toIntArray()
    }
}

/** Singleton config loaded once at plugin startup — same pattern as other skills. */
object SmithingDefs {
    lateinit var config: SmithingConfig
        private set

    fun init(yamlPath: Path): SmithingConfig {
        config = SmithingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
