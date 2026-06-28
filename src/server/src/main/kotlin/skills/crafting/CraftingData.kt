package skills.crafting

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class RecipeIngredient(val itemId: Int, val qty: Int)

data class RecipeDef(
    val name: String,
    val category: String,
    val levelRequired: Int,
    val xp: Double,
    val inputs: List<RecipeIngredient>,
    val tools: IntArray,
    val outputItemId: Int,
    val outputQty: Int,
    /** null means no station required; FURNACE or SPINNING_WHEEL otherwise */
    val station: String?,
    val wikiUrl: String,
)

data class CraftingMeta(
    val skill: String,
    val ticksPerAttempt: Int,
)

data class CraftingConfig(
    val meta: CraftingMeta,
    val allRecipes: List<RecipeDef>,
    /** Recipes keyed by each input item ID — lets handlers look up the recipe from either item in use-item-on-item. */
    val byInputItemId: Map<Int, List<RecipeDef>>,
    /** Station-based recipes grouped by station name (FURNACE, SPINNING_WHEEL). */
    val stationRecipes: Map<String, List<RecipeDef>>,
)

/** Parses data/skills/crafting.yaml into [CraftingConfig]. No values are hardcoded here. */
object CraftingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): CraftingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val allRecipes = parseRecipes(raw["recipes"] as Map<String, Any>)

        val byInputItemId: Map<Int, List<RecipeDef>> = buildMap {
            for (recipe in allRecipes) {
                for (ingredient in recipe.inputs) {
                    getOrPut(ingredient.itemId) { mutableListOf() }
                    (get(ingredient.itemId) as MutableList).add(recipe)
                }
            }
        }

        val stationRecipes = allRecipes
            .filter { it.station != null }
            .groupBy { it.station!! }

        return CraftingConfig(meta, allRecipes, byInputItemId, stationRecipes)
    }

    private fun parseMeta(m: Map<String, Any>) = CraftingMeta(
        skill = m["skill"] as String,
        ticksPerAttempt = m["ticks_per_attempt"] as Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseRecipes(categories: Map<String, Any>): List<RecipeDef> = buildList {
        for ((category, rawCategory) in categories) {
            val recipes = rawCategory as Map<String, Any>
            for ((name, rawRecipe) in recipes) {
                val m = rawRecipe as Map<String, Any>
                val inputsList = (m["inputs"] as List<Map<String, Any>>).map { i ->
                    RecipeIngredient(
                        itemId = i["item_id"] as Int,
                        qty = (i["qty"] as? Int) ?: 1,
                    )
                }
                val toolsList = ((m["tools"] as? List<*>)?.filterIsInstance<Int>() ?: emptyList<Int>()).toIntArray()
                add(
                    RecipeDef(
                        name = name,
                        category = category,
                        levelRequired = m["level_required"] as Int,
                        xp = (m["xp"] as Number).toDouble(),
                        inputs = inputsList,
                        tools = toolsList,
                        outputItemId = m["output_item_id"] as Int,
                        outputQty = (m["output_qty"] as? Int) ?: 1,
                        station = m["station"] as? String,
                        wikiUrl = m["wiki"] as String,
                    )
                )
            }
        }
    }
}

/** Singleton config loaded once at plugin startup. */
object CraftingDefs {
    lateinit var config: CraftingConfig
        private set

    fun init(yamlPath: Path): CraftingConfig {
        config = CraftingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
