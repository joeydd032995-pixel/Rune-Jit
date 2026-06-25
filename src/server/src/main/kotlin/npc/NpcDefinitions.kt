package npc

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * Singleton that loads all NPC definitions from osrsbox-db at startup.
 * Uses graceful-absent pattern: if the file is missing, all lookups return [NpcDefinition.EMPTY].
 *
 * Source: https://github.com/osrsbox/osrsbox-db (CC BY 4.0)
 */
object NpcDefinitions {
    private val log = LoggerFactory.getLogger(NpcDefinitions::class.java)
    private val definitions = HashMap<Int, NpcDefinition>()

    fun init(dataDir: Path = Path.of("data/osrsbox")) {
        val file = dataDir.resolve("monsters-complete.json")
        if (!Files.exists(file)) {
            log.warn("monsters-complete.json not found at {} — NPC definitions unavailable.", file)
            return
        }
        try {
            val json = Files.readString(file)
            val root = JsonParser.parseString(json).asJsonObject
            for ((_, element) in root.entrySet()) {
                val obj = element.asJsonObject
                val id = obj.get("id").asInt
                val dropsArray = obj.get("drops")?.nonNull?.asJsonArray
                val drops: List<DropEntry> = if (dropsArray != null) {
                    dropsArray.mapNotNull { elem ->
                        try {
                            val d = elem.asJsonObject
                            val rarity = d.get("rarity")?.nonNull?.asDouble ?: 0.0
                            if (rarity <= 0.0) return@mapNotNull null
                            val quantityStr = d.get("quantity")?.nonNull?.asString ?: "1"
                            val parts = quantityStr.split("-")
                            val minQty = parts[0].trim().toIntOrNull() ?: 1
                            val maxQty = if (parts.size > 1) parts[1].trim().toIntOrNull() ?: minQty else minQty
                            DropEntry(
                                itemId = d.get("id")?.nonNull?.asInt ?: return@mapNotNull null,
                                minQty = minQty,
                                maxQty = maxQty,
                                rarity = rarity,
                                noted  = d.get("noted")?.nonNull?.asBoolean ?: false,
                                rolls  = d.get("rolls")?.nonNull?.asInt ?: 1,
                            )
                        } catch (_: Exception) { null }
                    }
                } else emptyList()
                definitions[id] = NpcDefinition(
                    id = id,
                    name = obj.get("name")?.nonNull?.asString ?: "",
                    hitpoints = obj.get("hitpoints")?.nonNull?.asInt ?: 0,
                    attackLevel = obj.get("attack_level")?.nonNull?.asInt ?: 1,
                    strengthLevel = obj.get("strength_level")?.nonNull?.asInt ?: 1,
                    defenceLevel = obj.get("defence_level")?.nonNull?.asInt ?: 1,
                    magicLevel = obj.get("magic_level")?.nonNull?.asInt ?: 1,
                    rangedLevel = obj.get("ranged_level")?.nonNull?.asInt ?: 1,
                    combatLevel = obj.get("combat_level")?.nonNull?.asInt ?: 0,
                    aggressive = obj.get("aggressive")?.nonNull?.asBoolean ?: false,
                    members = obj.get("members")?.nonNull?.asBoolean ?: false,
                    examine = obj.get("examine")?.nonNull?.asString ?: "",
                    drops = drops,
                )
            }
            log.info("Loaded {} NPC definitions from {}", definitions.size, file)
        } catch (e: Exception) {
            log.error("Failed to load NPC definitions: {}", e.message, e)
        }
    }

    fun get(id: Int): NpcDefinition? = definitions[id]
    fun getOrEmpty(id: Int): NpcDefinition = definitions[id] ?: NpcDefinition.EMPTY

    /** Returns null for both Kotlin-null and Gson JsonNull elements. */
    private val JsonElement.nonNull: JsonElement? get() = if (isJsonNull) null else this
}
