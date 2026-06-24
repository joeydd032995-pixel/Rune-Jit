package skills.mining

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class MiningMeta(
    val skill: String,
    val ticksPerAttempt: Int,
    val baseAnimation: Int?,
    val idleAnimation: Int?,
)

data class RockDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val oreItemId: Int,
    val respawnTicks: Int,
    val difficulty: Int,
    val members: Boolean,
    val wikiUrl: String,
    /** Object IDs that trigger "Mine" — populated from cache after /cache-unpack-extract-assets */
    val objectIds: IntArray = IntArray(0),
)

data class PickaxeDef(
    val name: String,
    val itemId: Int,
    val wieldLevel: Int,
    val miningBonus: Int,
)

data class GemDef(
    val itemId: Int,
    val weight: Int,
)

data class GemDropConfig(
    val baseDropChance: Int,
    val gems: List<GemDef>,
)

data class PetConfig(
    val itemId: Int,
    val baseRate: Int,
    val scalesWithLevel: Boolean,
)

data class MiningConfig(
    val meta: MiningMeta,
    val pickaxes: List<PickaxeDef>,
    val rocks: Map<String, RockDef>,
    val gemDrops: GemDropConfig,
    val pet: PetConfig,
)

/** Parses data/skills/mining.yaml into [MiningConfig]. No values are hardcoded here. */
object MiningLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): MiningConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val pickaxes = parsePickaxes(raw["pickaxes"] as List<Map<String, Any>>)
        val rocks = parseRocks(raw["rocks"] as Map<String, Map<String, Any>>)
        val gemDrops = parseGemDrops(raw["gem_drops"] as Map<String, Any>)
        val pet = parsePet(raw["pet"] as Map<String, Any>)

        return MiningConfig(meta, pickaxes, rocks, gemDrops, pet)
    }

    private fun parseMeta(m: Map<String, Any>) = MiningMeta(
        skill = m["skill"] as String,
        ticksPerAttempt = m["ticks_per_attempt"] as Int,
        baseAnimation = m["base_animation"] as? Int,
        idleAnimation = m["idle_animation"] as? Int,
    )

    private fun parsePickaxes(list: List<Map<String, Any>>) = list.map { m ->
        PickaxeDef(
            name = m["name"] as String,
            itemId = m["item_id"] as Int,
            wieldLevel = m["wield_level"] as Int,
            miningBonus = m["mining_bonus"] as Int,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRocks(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        RockDef(
            name = name,
            levelRequired = m["level_required"] as Int,
            xp = (m["xp"] as Number).toDouble(),
            oreItemId = m["ore_item_id"] as Int,
            respawnTicks = m["respawn_ticks"] as Int,
            difficulty = m["difficulty"] as Int,
            members = m["members"] as Boolean,
            wikiUrl = m["wiki"] as String,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseGemDrops(m: Map<String, Any>): GemDropConfig {
        val gemsList = (m["gems"] as List<Map<String, Any>>).map { g ->
            GemDef(
                itemId = g["item_id"] as Int,
                weight = g["weight"] as Int,
            )
        }
        return GemDropConfig(
            baseDropChance = m["base_drop_chance"] as Int,
            gems = gemsList,
        )
    }

    private fun parsePet(m: Map<String, Any>) = PetConfig(
        itemId = m["item_id"] as Int,
        baseRate = m["base_rate"] as Int,
        scalesWithLevel = m["scales_with_level"] as Boolean,
    )
}

/** Singleton config loaded once at plugin startup. */
object MiningDefs {
    lateinit var config: MiningConfig
        private set

    fun init(yamlPath: Path): MiningConfig {
        config = MiningLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
