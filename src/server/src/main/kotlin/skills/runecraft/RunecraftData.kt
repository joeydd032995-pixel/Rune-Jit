package skills.runecraft

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

/** Skill-level metadata. Source: https://oldschool.runescape.wiki/w/Runecraft */
data class RcMeta(
    val skill: String,
    val ticksPerAttempt: Int,
    val runeEssenceItemId: Int,
    val pureEssenceItemId: Int,
)

/**
 * A single level threshold for the "multiple runes per essence" rule.
 * The highest [level] that is <= the player's Runecraft level determines
 * [runesPerEssence].
 * Source: https://oldschool.runescape.wiki/w/Runecraft#Multiple_runes_per_essence
 */
data class MultipleThreshold(
    val level: Int,
    val runesPerEssence: Int,
)

/** One rune altar definition. */
data class AltarDef(
    val name: String,
    val levelRequired: Int,
    val xpPerEssence: Double,
    val runeItemId: Int,
    val essenceItemId: Int,
    val altarObjectId: Int,
    val talismanItemId: Int?,
    /** Thresholds sorted ascending by level — invariant established by the loader. */
    val multipleThresholds: List<MultipleThreshold>,
    val wikiUrl: String,
) {
    /**
     * Runes produced per essence at the given Runecraft level.
     * Returns the [MultipleThreshold.runesPerEssence] of the highest threshold whose
     * level is <= [level]. Falls back to 1 if no threshold applies (defensive — a
     * level-1 base threshold is always expected).
     * Source: https://oldschool.runescape.wiki/w/Runecraft#Multiple_runes_per_essence
     */
    fun runesPerEssence(level: Int): Int =
        multipleThresholds.lastOrNull { it.level <= level }?.runesPerEssence ?: 1
}

data class RunecraftConfig(
    val meta: RcMeta,
    val altars: Map<String, AltarDef>,
) {
    /** O(1) lookup: altar object ID → AltarDef (Craft-rune object interaction). */
    val byAltarObjectId: Map<Int, AltarDef> = altars.values.associateBy { it.altarObjectId }

    /** All distinct altar object IDs, for plugin registration. */
    val allAltarObjectIds: IntArray = altars.values.map { it.altarObjectId }.distinct().toIntArray()

    /** All distinct essence item IDs used by any altar (rune + pure essence). */
    val allEssenceItemIds: IntArray = altars.values.map { it.essenceItemId }.distinct().toIntArray()
}

/** Parses data/skills/runecraft.yaml into [RunecraftConfig]. No values are hardcoded here. */
object RunecraftLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): RunecraftConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val altars = parseAltars(raw["altars"] as Map<String, Map<String, Any>>)

        return RunecraftConfig(meta, altars)
    }

    private fun parseMeta(m: Map<String, Any>) = RcMeta(
        skill = m["skill"] as String,
        ticksPerAttempt = m["ticks_per_attempt"] as Int,
        runeEssenceItemId = m["rune_essence_item_id"] as Int,
        pureEssenceItemId = m["pure_essence_item_id"] as Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseAltars(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        val thresholds = (m["multiple_thresholds"] as List<Map<String, Any>>)
            .map { t -> MultipleThreshold(level = t["level"] as Int, runesPerEssence = t["runes_per_essence"] as Int) }
            .sortedBy { it.level }

        AltarDef(
            name = name,
            levelRequired = m["level_required"] as Int,
            xpPerEssence = (m["xp_per_essence"] as Number).toDouble(),
            runeItemId = m["rune_item_id"] as Int,
            essenceItemId = m["essence_item_id"] as Int,
            altarObjectId = m["altar_object_id"] as Int,
            talismanItemId = m["talisman_item_id"] as? Int,
            multipleThresholds = thresholds,
            wikiUrl = m["wiki"] as? String ?: "",
        )
    }
}

/** Singleton config loaded once at plugin startup — same graceful-absent pattern as other skills. */
object RunecraftDefs {
    lateinit var config: RunecraftConfig
        private set

    fun init(yamlPath: Path): RunecraftConfig {
        config = RunecraftLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
