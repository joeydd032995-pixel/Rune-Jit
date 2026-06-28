package skills.hunter

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class HunterLootEntry(val itemId: Int, val qty: Int, val weight: Int)

data class CreatureDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val trapType: String,
    val trapItemId: Int,
    val catchRateLow: Double,
    val catchRateHigh: Double,
    val checkTicks: Int,
    val members: Boolean,
    val npcId: Int,
    val loot: List<HunterLootEntry>,
    val wiki: String,
) {
    /** Integer catch rate scaled by resolution for a given hunter level. */
    fun catchRateInt(hunterLevel: Int, resolution: Int): Int {
        if (hunterLevel >= 99) return (catchRateHigh * resolution).toInt()
        if (hunterLevel <= levelRequired) return (catchRateLow * resolution).toInt()
        val ratio = (hunterLevel - levelRequired).toDouble() / (99 - levelRequired)
        return (catchRateLow * resolution + ratio * (catchRateHigh - catchRateLow) * resolution).toInt()
    }
}

data class HunterMeta(val skill: String, val defaultCheckTicks: Int)

data class HunterTrapConfig(
    val catchRateResolution: Int,
    val baseMaxTraps: Int,
    val trapsPerLevels: Int,
)

data class HunterPetDef(
    val name: String,
    val itemId: Int,
    val baseRate: Int,
    val scalesWithLevel: Boolean,
    val wiki: String,
)

data class HunterConfig(
    val meta: HunterMeta,
    val trapConfig: HunterTrapConfig,
    val pet: HunterPetDef,
    val creatures: Map<String, CreatureDef>,
    /** All creature defs keyed by trap_item_id for quick dispatch in plugin. */
    val byTrapItemId: Map<Int, List<CreatureDef>>,
)

object HunterLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): HunterConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val trapConfig = parseTrapConfig(raw["config"] as Map<String, Any>)
        val pet = parsePet(raw["pet"] as Map<String, Any>)
        val creatures = parseCreatures(raw["creatures"] as Map<String, Map<String, Any>>, meta.defaultCheckTicks)

        val byTrapItemId: Map<Int, List<CreatureDef>> = creatures.values
            .groupBy { it.trapItemId }

        return HunterConfig(meta, trapConfig, pet, creatures, byTrapItemId)
    }

    private fun parseMeta(m: Map<String, Any>) = HunterMeta(
        skill = m["skill"] as String,
        defaultCheckTicks = m["default_check_ticks"] as Int,
    )

    private fun parseTrapConfig(m: Map<String, Any>) = HunterTrapConfig(
        catchRateResolution = m["catch_rate_resolution"] as Int,
        baseMaxTraps = m["base_max_traps"] as Int,
        trapsPerLevels = m["traps_per_levels"] as Int,
    )

    private fun parsePet(m: Map<String, Any>) = HunterPetDef(
        name = m["name"] as String,
        itemId = m["item_id"] as Int,
        baseRate = m["base_rate"] as Int,
        scalesWithLevel = m["scales_with_level"] as Boolean,
        wiki = m["wiki"] as String,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseCreatures(
        map: Map<String, Map<String, Any>>,
        defaultCheckTicks: Int,
    ): Map<String, CreatureDef> = map.mapValues { (name, m) ->
        val lootRaw = m["loot"] as? List<Map<String, Any>> ?: emptyList()
        CreatureDef(
            name = name,
            levelRequired = m["level_required"] as Int,
            xp = (m["xp"] as Number).toDouble(),
            trapType = m["trap_type"] as String,
            trapItemId = m["trap_item_id"] as Int,
            catchRateLow = (m["catch_rate_low"] as Number).toDouble(),
            catchRateHigh = (m["catch_rate_high"] as Number).toDouble(),
            checkTicks = (m["check_ticks"] as? Int) ?: defaultCheckTicks,
            members = m["members"] as? Boolean ?: true,
            npcId = m["npc_id"] as? Int ?: 0,
            loot = lootRaw.map { l ->
                HunterLootEntry(
                    itemId = l["item_id"] as Int,
                    qty = l["qty"] as? Int ?: 1,
                    weight = l["weight"] as? Int ?: 1,
                )
            },
            wiki = m["wiki"] as String,
        )
    }
}

object HunterDefs {
    lateinit var config: HunterConfig
        private set

    fun init(yamlPath: Path): HunterConfig {
        config = HunterLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
