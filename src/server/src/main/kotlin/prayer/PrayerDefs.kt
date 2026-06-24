package prayer

import org.yaml.snakeyaml.Yaml
import java.io.FileReader
import java.nio.file.Path

/**
 * Singleton registry that loads prayer definitions from data/prayers/standard.yaml
 * via SnakeYAML. Must be initialised once at server startup before any [PrayerSet]
 * is ticked or queried.
 *
 * Source: https://oldschool.runescape.wiki/w/Prayer#Standard_prayers
 */
object PrayerDefs {

    private lateinit var definitions: Map<Prayer, PrayerDefinition>

    /**
     * Loads standard.yaml from [path]. Called once by GameServer.start() before
     * the tick engine runs.
     */
    fun init(path: Path = Path.of("data/prayers/standard.yaml")) {
        val yaml = Yaml()
        val raw = FileReader(path.toFile()).use { yaml.load<Map<String, Any>>(it) }

        @Suppress("UNCHECKED_CAST")
        val prayers = raw["prayers"] as Map<String, Map<String, Any>>

        definitions = prayers.entries.associate { (key, values) ->
            val prayer = Prayer.valueOf(key)
            prayer to PrayerDefinition(
                prayer           = prayer,
                levelRequired    = (values["level_required"] as Number).toInt(),
                drainEffect      = (values["drain_effect"]   as Number).toInt(),
                attackMult       = (values["attack_mult"]    as Number?)?.toDouble() ?: 1.0,
                strengthMult     = (values["strength_mult"]  as Number?)?.toDouble() ?: 1.0,
                defenceMult      = (values["defence_mult"]   as Number?)?.toDouble() ?: 1.0,
                rangedMult       = (values["ranged_mult"]    as Number?)?.toDouble() ?: 1.0,
                rangedStrengthMult = (values["ranged_strength_mult"] as Number?)?.toDouble() ?: 1.0,
                magicMult        = (values["magic_mult"]     as Number?)?.toDouble() ?: 1.0,
                magicDefenceMult = (values["magic_defence_mult"] as Number?)?.toDouble() ?: 1.0,
                overheadProtection = (values["overhead_protection"] as String?)
                    ?.let { OverheadProtection.valueOf(it) },
            )
        }
    }

    /**
     * Returns the [PrayerDefinition] for [prayer].
     * @throws IllegalStateException if [init] has not been called.
     * @throws IllegalArgumentException if [prayer] is not found in the loaded data.
     */
    fun get(prayer: Prayer): PrayerDefinition {
        check(::definitions.isInitialized) {
            "PrayerDefs.init() must be called before accessing prayer definitions"
        }
        return definitions[prayer]
            ?: throw IllegalArgumentException("No definition found for prayer: $prayer")
    }

    /** Returns all loaded definitions. Primarily for testing. */
    fun all(): Map<Prayer, PrayerDefinition> {
        check(::definitions.isInitialized) {
            "PrayerDefs.init() must be called before accessing prayer definitions"
        }
        return definitions
    }

    /** Returns true if [prayer] is present in the loaded data (for validation). */
    fun contains(prayer: Prayer): Boolean = ::definitions.isInitialized && definitions.containsKey(prayer)
}
