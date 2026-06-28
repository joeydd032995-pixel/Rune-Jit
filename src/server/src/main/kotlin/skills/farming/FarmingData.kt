package skills.farming

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class PatchDef(
    val name: String,
    val patchType: String,
    val levelRequired: Int,
    val seedItemId: Int,
    val produceItemId: Int,
    val plantXp: Double,
    val harvestXp: Double,
    val growthStages: Int,
    val growthTicks: Int,
    val baseYield: Int,
    val patchObjectId: Int,
    val members: Boolean,
    val wiki: String,
)

data class FarmingMeta(val skill: String, val defaultGrowthTicks: Int)

data class FarmingConfig(
    val meta: FarmingMeta,
    val patches: Map<String, PatchDef>,
    /** Seed item ID → patch def */
    val bySeedItemId: Map<Int, PatchDef>,
    /** Patch object ID → patch def (excludes objectId == 0) */
    val byPatchObjectId: Map<Int, PatchDef>,
)

object FarmingLoader {
    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): FarmingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }
        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val patches = parsePatches(raw["patches"] as Map<String, Map<String, Any>>, meta.defaultGrowthTicks)
        val bySeedItemId = patches.values.associateBy { it.seedItemId }
        val byPatchObjectId = patches.values.filter { it.patchObjectId != 0 }.associateBy { it.patchObjectId }
        return FarmingConfig(meta, patches, bySeedItemId, byPatchObjectId)
    }

    private fun parseMeta(m: Map<String, Any>) = FarmingMeta(
        skill = m["skill"] as String,
        defaultGrowthTicks = m["default_growth_ticks"] as Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parsePatches(map: Map<String, Map<String, Any>>, defaultGrowthTicks: Int): Map<String, PatchDef> =
        map.mapValues { (name, m) ->
            PatchDef(
                name = name,
                patchType = m["patch_type"] as String,
                levelRequired = m["level_required"] as Int,
                seedItemId = m["seed_item_id"] as Int,
                produceItemId = m["produce_item_id"] as Int,
                plantXp = (m["plant_xp"] as Number).toDouble(),
                harvestXp = (m["harvest_xp"] as Number).toDouble(),
                growthStages = m["growth_stages"] as? Int ?: 4,
                growthTicks = m["growth_ticks"] as? Int ?: defaultGrowthTicks,
                baseYield = m["base_yield"] as? Int ?: 1,
                patchObjectId = m["patch_object_id"] as? Int ?: 0,
                members = m["members"] as? Boolean ?: true,
                wiki = m["wiki"] as String,
            )
        }
}

/** Singleton config loaded once at plugin startup. */
object FarmingDefs {
    lateinit var config: FarmingConfig
        private set

    fun init(yamlPath: Path): FarmingConfig {
        config = FarmingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
