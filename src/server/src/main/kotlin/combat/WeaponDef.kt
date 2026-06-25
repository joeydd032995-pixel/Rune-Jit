package combat

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

/**
 * Static definition of a weapon's combat properties.
 *
 * @param weaponType  Key into `melee.yaml` or `ranged.yaml` weapon_ticks (e.g. "WHIP", "SHORTBOW").
 * @param defaultAttackType  Default attack type for this weapon (e.g. "SLASH", "STAB", "RANGED").
 *
 * Source: https://oldschool.runescape.wiki/w/Weapons
 */
data class WeaponDef(
    val weaponType: String,
    val defaultAttackType: String,
)

/**
 * Singleton that loads weapon definitions from `data/combat/weapons.yaml` at startup.
 * Graceful-absent: if the file is missing, [getByItemId] returns [UNARMED] for all IDs.
 *
 * Source: https://oldschool.runescape.wiki/w/Weapons
 */
object WeaponDefs {
    private val log = LoggerFactory.getLogger(WeaponDefs::class.java)

    private val byItemId = HashMap<Int, WeaponDef>()

    /** Returned when a weapon item ID is not found or no weapon is equipped. */
    val UNARMED = WeaponDef(weaponType = "DEFAULT_1H", defaultAttackType = "CRUSH")

    @Suppress("UNCHECKED_CAST")
    fun init(dataDir: Path = Path.of("data/combat")) {
        byItemId.clear()
        val file = dataDir.resolve("weapons.yaml")
        if (!Files.exists(file)) {
            log.warn("weapons.yaml not found at {} — weapon type lookup unavailable", file)
            return
        }
        try {
            val raw: Map<String, Any> = Files.newInputStream(file).use { Yaml().load(it) }
            val weaponsRaw = raw["weapons"] as? Map<*, *> ?: run {
                log.warn("weapons.yaml has no 'weapons' key")
                return
            }
            for ((key, value) in weaponsRaw) {
                val id = (key as? Number)?.toInt() ?: continue
                val entry = value as? Map<*, *> ?: continue
                val weaponType = entry["weapon_type"] as? String ?: continue
                val attackType = entry["default_attack_type"] as? String ?: continue
                byItemId[id] = WeaponDef(weaponType = weaponType, defaultAttackType = attackType)
            }
            log.info("Loaded {} weapon definitions from {}", byItemId.size, file)
        } catch (e: Exception) {
            log.error("Failed to load weapons.yaml: {}", e.message, e)
        }
    }

    /**
     * Returns the [WeaponDef] for [itemId], or [UNARMED] if not found.
     * Negative IDs (empty equipment slot) always return [UNARMED].
     */
    fun getByItemId(itemId: Int): WeaponDef =
        if (itemId < 0) UNARMED else byItemId[itemId] ?: UNARMED
}
