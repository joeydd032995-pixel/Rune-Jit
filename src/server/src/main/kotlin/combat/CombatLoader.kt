package combat

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

/**
 * Loads all three combat YAML data files and returns a [CombatConfig].
 * No combat values are hardcoded in Kotlin — all come from data files.
 * Source: server-skills.md (data-driven rule applies to combat too).
 */
object CombatLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(dataDir: Path): CombatConfig {
        val yaml = Yaml()
        val melee = loadMelee(yaml, dataDir.resolve("melee.yaml"))
        val magic = loadMagic(yaml, dataDir.resolve("magic.yaml"))
        val ranged = loadRanged(yaml, dataDir.resolve("ranged.yaml"))
        return CombatConfig(melee, magic, ranged)
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadMelee(yaml: Yaml, path: Path): MeleeMeta {
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val stylesRaw = raw["attack_styles"] as Map<String, Map<String, Any>>
        val attackStyles = stylesRaw.mapValues { (_, m) ->
            AttackStyleDef(
                attackStyleBonus   = m["attack_style_bonus"] as Int,
                strengthStyleBonus = m["strength_style_bonus"] as Int,
                defenceStyleBonus  = m["defence_style_bonus"] as Int,
                xpSkill            = m["xp_skill"] as String,
            )
        }

        val xpRates = raw["xp_rates"] as Map<String, Any>
        val combatXp = (xpRates["combat_xp_per_damage"] as Number).toDouble()
        val hpXp    = (xpRates["hp_xp_per_damage"] as Number).toDouble()

        val weaponTicksRaw = raw["weapon_ticks"] as Map<String, Any>
        val weaponTicks = weaponTicksRaw.mapValues { (_, v) -> (v as Number).toInt() }

        return MeleeMeta(
            combatXpPerDamage = combatXp,
            hpXpPerDamage     = hpXp,
            attackStyles      = attackStyles,
            weaponTicks       = weaponTicks,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadMagic(yaml: Yaml, path: Path): SpellBook {
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val bookRaw = raw["standard_book"] as Map<String, Any>
        val cycleTicks   = bookRaw["combat_cycle_ticks"] as Int
        val hpXp         = (bookRaw["hp_xp_per_damage"] as Number).toDouble()
        val magicXpExtra = (bookRaw["magic_xp_per_damage"] as Number).toDouble()

        val spellsRaw = bookRaw["spells"] as Map<String, Map<String, Any>>
        val spells = spellsRaw.mapValues { (name, m) ->
            Spell(
                name          = name,
                levelRequired = m["level_required"] as Int,
                maxHit        = m["max_hit"] as Int,
                baseXp        = (m["base_xp"] as Number).toDouble(),
                freezeTicks   = m["freeze_ticks"] as? Int,
            )
        }

        return SpellBook(
            spells           = spells,
            combatCycleTicks = cycleTicks,
            hpXpPerDamage    = hpXp,
            magicXpPerDamage = magicXpExtra,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadRanged(yaml: Yaml, path: Path): RangedMeta {
        val raw: Map<String, Any> = Files.newInputStream(path).use { yaml.load(it) }

        val stylesRaw = raw["attack_styles"] as Map<String, Map<String, Any>>
        val attackStyles = stylesRaw.mapValues { (_, m) ->
            RangedAttackStyleDef(
                styleBonus      = m["style_bonus"] as Int,
                ticksReduction  = (m["ticks_reduction"] as? Int) ?: 0,
                xpSkill         = m["xp_skill"] as String,
            )
        }

        val xpRates = raw["xp_rates"] as Map<String, Any>
        val rangedXp = (xpRates["ranged_xp_per_damage"] as Number).toDouble()
        val hpXp    = (xpRates["hp_xp_per_damage"] as Number).toDouble()

        val weaponTicksRaw = raw["weapon_ticks"] as Map<String, Any>
        val weaponTicks = weaponTicksRaw.mapValues { (_, v) -> (v as Number).toInt() }

        return RangedMeta(
            rangedXpPerDamage = rangedXp,
            hpXpPerDamage     = hpXp,
            attackStyles      = attackStyles,
            weaponTicks       = weaponTicks,
        )
    }
}

/** Singleton config loaded once at server startup. */
object CombatDefs {
    lateinit var config: CombatConfig
        private set

    fun init(dataDir: Path): CombatConfig {
        config = CombatLoader.load(dataDir)
        return config
    }

    val isInitialized: Boolean get() = ::config.isInitialized
}
