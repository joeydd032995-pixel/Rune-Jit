package skills.fletching

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class FletchingRecipe(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val primaryItemId: Int,
    val targetItemId: Int,
    val outputItemId: Int,
    val outputQty: Int,
    val wiki: String,
)

data class FletchingMeta(val skill: String, val ticksPerAction: Int)

data class FletchingConfig(
    val meta: FletchingMeta,
    val recipes: Map<String, FletchingRecipe>,
    /** Maps (primaryItemId, targetItemId) → recipe */
    val byItemPair: Map<Pair<Int, Int>, FletchingRecipe>,
)

/** Parses data/skills/fletching.yaml into [FletchingConfig]. No values are hardcoded here. */
object FletchingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): FletchingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val allRecipes = mutableMapOf<String, FletchingRecipe>()
        val recipesRaw = raw["recipes"] as Map<String, Map<String, Map<String, Any>>>
        for ((_, categoryMap) in recipesRaw) {
            for ((name, m) in categoryMap) {
                allRecipes[name] = parseRecipe(name, m)
            }
        }
        val byItemPair = allRecipes.values.associateBy { Pair(it.primaryItemId, it.targetItemId) }
        return FletchingConfig(meta, allRecipes, byItemPair)
    }

    private fun parseMeta(m: Map<String, Any>) = FletchingMeta(
        skill = m["skill"] as String,
        ticksPerAction = m["ticks_per_action"] as Int,
    )

    private fun parseRecipe(name: String, m: Map<String, Any>) = FletchingRecipe(
        name = name,
        levelRequired = m["level_required"] as Int,
        xp = (m["xp"] as Number).toDouble(),
        primaryItemId = m["primary_item_id"] as Int,
        targetItemId = m["target_item_id"] as Int,
        outputItemId = m["output_item_id"] as Int,
        outputQty = m["output_qty"] as? Int ?: 1,
        wiki = m["wiki"] as String,
    )
}

/** Singleton config loaded once at plugin startup. */
object FletchingDefs {
    lateinit var config: FletchingConfig
        private set

    fun init(yamlPath: Path): FletchingConfig {
        config = FletchingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
