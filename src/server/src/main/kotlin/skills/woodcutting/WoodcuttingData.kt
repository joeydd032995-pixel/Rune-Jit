package skills.woodcutting

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class WcMeta(
    val skill: String,
    val ticksPerAttempt: Int,
    val baseChopAnimation: Int?,
    val idleAnimation: Int?,
)

data class TreeDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val logItemId: Int?,
    val dropsLog: Boolean,
    val respawnTicks: Int?,
    val difficulty: Int,
    val members: Boolean,
    val questRequired: String?,
    val location: String?,
    val wikiUrl: String,
    val stumpObjectId: Int?,
    val notes: String?,
    /** Object IDs that trigger "Chop down" — populated from cache after /cache-unpack-extract-assets */
    val objectIds: IntArray = IntArray(0),
)

data class AxeDef(
    val name: String,
    val itemId: Int,
    val wieldLevel: Int,
    val wcBonus: Int,
    val specialAttack: String?,
    val burnsLogs: Boolean,
    val firemakingXpMultiplier: Double,
    val questRequired: String?,
)

data class BirdNestConfig(
    val baseDropChance: Int,
    val itemIdEmpty: Int,
    val itemIdSeeds: Int,
    val itemIdRing: Int,
)

data class BeaverPetConfig(
    val itemId: Int,
    val baseRate: Int,
    val scalesWithLevel: Boolean,
)

data class WoodcuttingConfig(
    val meta: WcMeta,
    val axes: List<AxeDef>,
    val trees: Map<String, TreeDef>,
    val birdNests: BirdNestConfig,
    val pet: BeaverPetConfig,
)

/** Parses data/skills/woodcutting.yaml into [WoodcuttingConfig]. No values are hardcoded here. */
object WoodcuttingLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): WoodcuttingConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta = parseMeta(raw["meta"] as Map<String, Any>)
        val axes = parseAxes(raw["axes"] as List<Map<String, Any>>)
        val trees = parseTrees(raw["trees"] as Map<String, Map<String, Any>>)
        val birdNests = parseBirdNests(raw["bird_nests"] as Map<String, Any>)
        val pet = parsePet(raw["pet"] as Map<String, Any>)

        return WoodcuttingConfig(meta, axes, trees, birdNests, pet)
    }

    private fun parseMeta(m: Map<String, Any>) = WcMeta(
        skill = m["skill"] as String,
        ticksPerAttempt = m["ticks_per_attempt"] as Int,
        baseChopAnimation = m["base_chop_animation"] as? Int,
        idleAnimation = m["idle_animation"] as? Int,
    )

    private fun parseAxes(list: List<Map<String, Any>>) = list.map { m ->
        AxeDef(
            name = m["name"] as String,
            itemId = m["item_id"] as Int,
            wieldLevel = m["wield_level"] as Int,
            wcBonus = m["woodcutting_bonus"] as Int,
            specialAttack = m["special_attack"] as? String,
            burnsLogs = m["burns_logs"] as? Boolean ?: false,
            firemakingXpMultiplier = (m["firemaking_xp_multiplier"] as? Double) ?: 0.0,
            questRequired = m["quest_required"] as? String,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTrees(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        TreeDef(
            name = name,
            levelRequired = m["level_required"] as Int,
            xp = (m["xp"] as Number).toDouble(),
            logItemId = m["log_item_id"] as? Int,
            dropsLog = m["drops_log"] as Boolean,
            respawnTicks = m["respawn_ticks"] as? Int,
            difficulty = m["difficulty"] as Int,
            members = m["members"] as Boolean,
            questRequired = m["quest_required"] as? String,
            location = m["location"] as? String,
            wikiUrl = m["wiki"] as String,
            stumpObjectId = m["stump_object_id"] as? Int,
            notes = m["notes"] as? String,
        )
    }

    private fun parseBirdNests(m: Map<String, Any>) = BirdNestConfig(
        baseDropChance = m["base_drop_chance"] as Int,
        itemIdEmpty = m["item_id_empty"] as Int,
        itemIdSeeds = m["item_id_seeds"] as Int,
        itemIdRing = m["item_id_ring"] as Int,
    )

    private fun parsePet(m: Map<String, Any>) = BeaverPetConfig(
        itemId = m["item_id"] as Int,
        baseRate = m["base_rate"] as Int,
        scalesWithLevel = m["scales_with_level"] as Boolean,
    )
}

/** Singleton config loaded once at plugin startup. */
object WoodcuttingDefs {
    lateinit var config: WoodcuttingConfig
        private set

    fun init(yamlPath: Path): WoodcuttingConfig {
        config = WoodcuttingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
