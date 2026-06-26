package skills.herblore

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class HerbloreMeta(
    val skill: String,
    val vialOfWaterItemId: Int,
)

data class HerbDef(
    val name: String,
    val cleanLevel: Int,
    val cleanXp: Double,
    val grimyItemId: Int,
    val cleanItemId: Int,
    val unfPotionItemId: Int,
    val wikiUrl: String,
)

data class PotionRecipe(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val primaryItemId: Int,
    val secondaryItemId: Int,
    val unfinishedItemId: Int,
    val outputItemId: Int,
    val wikiUrl: String,
)

data class HerbloreConfig(
    val meta: HerbloreMeta,
    val herbs: Map<String, HerbDef>,
    val potions: Map<String, PotionRecipe>,
) {
    /** O(1) lookup: grimy herb item ID → HerbDef (herb cleaning). */
    val byGrimyId: Map<Int, HerbDef> = herbs.values.associateBy { it.grimyItemId }

    /** O(1) lookup: clean herb item ID → HerbDef (making unfinished potions). */
    val byCleanId: Map<Int, HerbDef> = herbs.values.associateBy { it.cleanItemId }

    /**
     * O(1) lookup: (secondary item ID, unfinished potion ID) → PotionRecipe.
     * The pair uniquely identifies a finished-potion recipe.
     */
    val bySecondaryAndUnf: Map<Pair<Int, Int>, PotionRecipe> =
        potions.values.associateBy { it.secondaryItemId to it.unfinishedItemId }
}

/** Parses data/skills/herblore.yaml into [HerbloreConfig]. No values are hardcoded here. */
object HerbloreLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): HerbloreConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val herbs = parseHerbs(raw["herbs"] as Map<String, Map<String, Any>>)
        val potions = parsePotions(raw["potions"] as Map<String, Map<String, Any>>)

        return HerbloreConfig(meta, herbs, potions)
    }

    private fun parseMeta(m: Map<String, Any>) = HerbloreMeta(
        skill = m["skill"] as String,
        vialOfWaterItemId = m["vial_of_water_item_id"] as Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseHerbs(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        HerbDef(
            name = name,
            cleanLevel = m["clean_level"] as Int,
            cleanXp = (m["clean_xp"] as Number).toDouble(),
            grimyItemId = m["grimy_item_id"] as Int,
            cleanItemId = m["clean_item_id"] as Int,
            unfPotionItemId = m["unf_potion_item_id"] as Int,
            wikiUrl = m["wiki"] as? String ?: "",
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parsePotions(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        PotionRecipe(
            name = name,
            levelRequired = m["level_required"] as Int,
            xp = (m["xp"] as Number).toDouble(),
            primaryItemId = m["primary_item_id"] as Int,
            secondaryItemId = m["secondary_item_id"] as Int,
            unfinishedItemId = m["unfinished_item_id"] as Int,
            outputItemId = m["output_item_id"] as Int,
            wikiUrl = m["wiki"] as? String ?: "",
        )
    }
}

/** Singleton config loaded once at plugin startup — same graceful-absent pattern as other skills. */
object HerbloreDefs {
    lateinit var config: HerbloreConfig
        private set

    fun init(yamlPath: Path): HerbloreConfig {
        config = HerbloreLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
