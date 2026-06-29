package skills.thieving

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

data class LootEntry(val itemId: Int, val qty: Int, val weight: Int)

data class PickpocketDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val npcIds: IntArray,
    val successLow: Int,
    val successHigh: Int,
    val stunTicks: Int,
    val stunDamage: Int,
    val loot: List<LootEntry>,
    val wiki: String,
) {
    // Interpolated success chance (0..255 scale) at the given level.
    // Source: https://oldschool.runescape.wiki/w/Thieving#Success_rate
    fun successRate(thievingLevel: Int): Int {
        if (thievingLevel >= 99) return successHigh
        if (thievingLevel <= levelRequired) return successLow
        val ratio = (thievingLevel - levelRequired).toDouble() / (99 - levelRequired)
        return (ratio * (successHigh - successLow) + successLow).toInt()
    }
}

data class StallDef(
    val name: String,
    val levelRequired: Int,
    val xp: Double,
    val objectId: Int,
    val respawnTicks: Int,
    val loot: List<LootEntry>,
    val wiki: String,
)

data class ThievingMeta(val skill: String, val ticksPerAttempt: Int)

data class ThievingConfig(
    val meta: ThievingMeta,
    val pickpockets: Map<String, PickpocketDef>,
    val stalls: Map<String, StallDef>,
    val allPickpocketNpcIds: IntArray,
    val byNpcId: Map<Int, PickpocketDef>,
    val allStallObjectIds: IntArray,
    val byStallObjectId: Map<Int, StallDef>,
)

object ThievingLoader {
    @Suppress("UNCHECKED_CAST")
    fun load(path: Path): ThievingConfig {
        val yaml = Yaml()
        val raw = yaml.load<Map<String, Any>>(Files.newInputStream(path))

        val metaMap = raw["meta"] as Map<String, Any>
        val meta = ThievingMeta(
            skill = metaMap["skill"] as String,
            ticksPerAttempt = metaMap["ticks_per_attempt"] as Int,
        )

        val pickpocketsRaw = raw["pickpockets"] as Map<String, Map<String, Any>>
        val pickpockets = linkedMapOf<String, PickpocketDef>()
        for ((name, data) in pickpocketsRaw) {
            pickpockets[name] = PickpocketDef(
                name = name,
                levelRequired = data["level_required"] as Int,
                xp = (data["xp"] as Number).toDouble(),
                npcIds = (data["npc_ids"] as List<*>).filterIsInstance<Int>().toIntArray(),
                successLow = data["success_low"] as Int,
                successHigh = data["success_high"] as Int,
                stunTicks = data["stun_ticks"] as Int,
                stunDamage = data["stun_damage"] as Int,
                loot = parseLoot(data["loot"] as List<Map<String, Any>>),
                wiki = data["wiki"] as String,
            )
        }

        val stallsRaw = raw["stalls"] as Map<String, Map<String, Any>>
        val stalls = linkedMapOf<String, StallDef>()
        for ((name, data) in stallsRaw) {
            stalls[name] = StallDef(
                name = name,
                levelRequired = data["level_required"] as Int,
                xp = (data["xp"] as Number).toDouble(),
                objectId = data["object_id"] as Int,
                respawnTicks = data["respawn_ticks"] as Int,
                loot = parseLoot(data["loot"] as List<Map<String, Any>>),
                wiki = data["wiki"] as String,
            )
        }

        val allNpcIds = pickpockets.values.flatMap { it.npcIds.toList() }.toIntArray()
        val byNpcId = mutableMapOf<Int, PickpocketDef>()
        for (def in pickpockets.values) {
            for (id in def.npcIds) byNpcId[id] = def
        }

        val allStallIds = stalls.values.filter { it.objectId != 0 }.map { it.objectId }.toIntArray()
        val byStallId = mutableMapOf<Int, StallDef>()
        for (def in stalls.values) {
            if (def.objectId != 0) byStallId[def.objectId] = def
        }

        return ThievingConfig(
            meta = meta,
            pickpockets = pickpockets,
            stalls = stalls,
            allPickpocketNpcIds = allNpcIds,
            byNpcId = byNpcId,
            allStallObjectIds = allStallIds,
            byStallObjectId = byStallId,
        )
    }

    private fun parseLoot(list: List<Map<String, Any>>): List<LootEntry> =
        list.map { LootEntry(it["item_id"] as Int, it["qty"] as Int, it["weight"] as Int) }
}

object ThievingDefs {
    lateinit var config: ThievingConfig private set

    fun init(yamlPath: Path): ThievingConfig {
        config = ThievingLoader.load(yamlPath)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
