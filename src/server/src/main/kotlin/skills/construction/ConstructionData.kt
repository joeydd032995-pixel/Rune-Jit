package skills.construction

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class ConstructionMeta(
    val skill: String,
    val ticksPerBuild: Int,
    val toolSawItemId: Int,
    val toolHammerItemId: Int,
)

data class PlankDef(
    val name: String,
    val itemId: Int,
    val xpPerPlank: Double,
    val wikiUrl: String,
)

data class MaterialDef(
    val itemId: Int,
    val quantity: Int,
)

data class FurnitureDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val room: String,
    val hotspot: String,
    /** Object ID of the build hotspot; 0 until cache extraction runs. */
    val hotspotObjectId: Int,
    val materials: List<MaterialDef>,
    val wikiUrl: String,
)

data class RoomDef(
    val name: String,
    val levelRequired: Int,
    val buildCost: Int,
    /** GP refunded when the room is removed (50% of buildCost). */
    val removalRefund: Int,
    val wikiUrl: String,
)

data class ServantDef(
    val name: String,
    val levelRequired: Int,
    /** NPC ID; 0 until cache extraction runs. */
    val npcId: Int,
    val wagePer5Trips: Int,
    val maxCarry: Int,
    /** Round-trip ticks to bank and back. */
    val travelTicks: Int,
    val wikiUrl: String,
)

data class ConstructionConfig(
    val meta: ConstructionMeta,
    val planks: Map<String, PlankDef>,
    val rooms: Map<String, RoomDef>,
    val furniture: Map<String, FurnitureDef>,
    val servants: Map<String, ServantDef>,
)

/** Parses data/skills/construction.yaml into [ConstructionConfig]. No values are hardcoded here. */
object ConstructionLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): ConstructionConfig {
        val yaml = Yaml()
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val meta      = parseMeta(raw["meta"] as Map<String, Any>)
        val planks    = parsePlanks(raw["planks"] as Map<String, Map<String, Any>>)
        val rooms     = parseRooms(raw["rooms"] as Map<String, Map<String, Any>>)
        val furniture = parseFurniture(raw["furniture"] as Map<String, Map<String, Any>>)
        val servants  = parseServants(raw["servants"] as Map<String, Map<String, Any>>)

        return ConstructionConfig(meta, planks, rooms, furniture, servants)
    }

    private fun parseMeta(m: Map<String, Any>) = ConstructionMeta(
        skill            = m["skill"] as String,
        ticksPerBuild    = m["ticks_per_build"] as Int,
        toolSawItemId    = m["tool_saw_item_id"] as Int,
        toolHammerItemId = m["tool_hammer_item_id"] as Int,
    )

    @Suppress("UNCHECKED_CAST")
    private fun parsePlanks(map: Map<String, Map<String, Any>>) = map.mapValues { (name, m) ->
        PlankDef(
            name       = name,
            itemId     = m["item_id"] as Int,
            xpPerPlank = (m["xp_per_plank"] as Number).toDouble(),
            wikiUrl    = m["wiki"] as String,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRooms(map: Map<String, Map<String, Any>>) = map.mapValues { (_, m) ->
        RoomDef(
            name           = m["name"] as String,
            levelRequired  = m["level_required"] as Int,
            buildCost      = m["build_cost"] as Int,
            removalRefund  = m["removal_refund"] as Int,
            wikiUrl        = m["wiki"] as String,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFurniture(map: Map<String, Map<String, Any>>) = map.mapValues { (_, m) ->
        FurnitureDef(
            name            = m["name"] as String,
            levelRequired   = m["level_required"] as Int,
            xp              = (m["xp"] as Number).toDouble(),
            room            = m["room"] as String,
            hotspot         = m["hotspot"] as String,
            hotspotObjectId = (m["hotspot_object_id"] as? Int) ?: 0,
            materials       = parseMaterials(m["materials"] as List<Map<String, Any>>),
            wikiUrl         = m["wiki"] as String,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMaterials(list: List<Map<String, Any>>) = list.map { m ->
        MaterialDef(
            itemId   = m["item_id"] as Int,
            quantity = m["quantity"] as Int,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseServants(map: Map<String, Map<String, Any>>) = map.mapValues { (_, m) ->
        ServantDef(
            name           = m["name"] as String,
            levelRequired  = m["level_required"] as Int,
            npcId          = (m["npc_id"] as? Int) ?: 0,
            wagePer5Trips  = m["wage_per_5_trips"] as Int,
            maxCarry       = m["max_carry"] as Int,
            travelTicks    = m["travel_ticks"] as Int,
            wikiUrl        = m["wiki"] as String,
        )
    }
}

/** Singleton config loaded once at plugin startup — same pattern as WoodcuttingDefs/CookingDefs. */
object ConstructionDefs {
    lateinit var config: ConstructionConfig
        private set

    fun init(yamlPath: Path): ConstructionConfig {
        config = ConstructionLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
