package item

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * Singleton that loads all item definitions from osrsbox-db at startup.
 * Uses graceful-absent pattern: if the file is missing, all lookups return [ItemDefinition.EMPTY].
 *
 * Source: https://github.com/osrsbox/osrsbox-db (CC BY 4.0)
 */
object ItemDefinitions {
    private val log = LoggerFactory.getLogger(ItemDefinitions::class.java)
    private val definitions = HashMap<Int, ItemDefinition>()
    private var loaded = false

    fun init(dataDir: Path = Path.of("data/osrsbox")) {
        val file = dataDir.resolve("items-complete.json")
        if (!Files.exists(file)) {
            log.warn(
                "items-complete.json not found at {} — item bonuses unavailable. Run /import-osrsbox-complete.",
                file,
            )
            loaded = true
            return
        }
        try {
            val json = Files.readString(file)
            val root = JsonParser.parseString(json).asJsonObject
            for ((_, element) in root.entrySet()) {
                val obj = element.asJsonObject
                val id = obj.get("id").asInt
                val equipment = obj.get("equipment")?.nonNull?.asJsonObject
                definitions[id] = ItemDefinition(
                    id = id,
                    name = obj.get("name")?.nonNull?.asString ?: "",
                    examine = obj.get("examine")?.nonNull?.asString ?: "",
                    members = obj.get("members")?.nonNull?.asBoolean ?: false,
                    stackable = obj.get("stackable")?.nonNull?.asBoolean ?: false,
                    noted = obj.get("noted")?.nonNull?.asBoolean ?: false,
                    equipable = obj.get("equipable")?.nonNull?.asBoolean ?: false,
                    weight = obj.get("weight")?.nonNull?.asDouble ?: 0.0,
                    attackStab = equipment?.get("attack_stab")?.nonNull?.asInt ?: 0,
                    attackSlash = equipment?.get("attack_slash")?.nonNull?.asInt ?: 0,
                    attackCrush = equipment?.get("attack_crush")?.nonNull?.asInt ?: 0,
                    attackMagic = equipment?.get("attack_magic")?.nonNull?.asInt ?: 0,
                    attackRanged = equipment?.get("attack_ranged")?.nonNull?.asInt ?: 0,
                    defenceStab = equipment?.get("defence_stab")?.nonNull?.asInt ?: 0,
                    defenceSlash = equipment?.get("defence_slash")?.nonNull?.asInt ?: 0,
                    defenceCrush = equipment?.get("defence_crush")?.nonNull?.asInt ?: 0,
                    defenceMagic = equipment?.get("defence_magic")?.nonNull?.asInt ?: 0,
                    defenceRanged = equipment?.get("defence_ranged")?.nonNull?.asInt ?: 0,
                    meleeStrength = equipment?.get("melee_strength")?.nonNull?.asInt ?: 0,
                    rangedStrength = equipment?.get("ranged_strength")?.nonNull?.asInt ?: 0,
                    magicDamage = equipment?.get("magic_damage")?.nonNull?.asInt ?: 0,
                    prayer = equipment?.get("prayer")?.nonNull?.asInt ?: 0,
                )
            }
            log.info("Loaded {} item definitions from {}", definitions.size, file)
        } catch (e: Exception) {
            log.error("Failed to load item definitions: {}", e.message, e)
        }
        loaded = true
    }

    fun get(id: Int): ItemDefinition? = definitions[id]
    fun getOrEmpty(id: Int): ItemDefinition = definitions[id] ?: ItemDefinition.EMPTY

    /** Returns null for both Kotlin-null and Gson JsonNull elements. */
    private val JsonElement.nonNull: JsonElement? get() = if (isJsonNull) null else this
}
