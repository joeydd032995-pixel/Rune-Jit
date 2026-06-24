package prayer

import combat.CombatStyle

/**
 * Definition of a single prayer loaded from data/prayers/standard.yaml.
 * All multipliers default to 1.0 (no-op) so only relevant fields need to be set.
 *
 * Source: https://oldschool.runescape.wiki/w/Prayer#Standard_prayers
 */
data class PrayerDefinition(
    val prayer: Prayer,
    val levelRequired: Int,
    /** OSRS drain effect value. Threshold = 600 * (1 + floor(prayerBonus / 30)). */
    val drainEffect: Int,
    val attackMult: Double = 1.0,
    val strengthMult: Double = 1.0,
    val defenceMult: Double = 1.0,
    val rangedMult: Double = 1.0,
    val rangedStrengthMult: Double = 1.0,
    val magicMult: Double = 1.0,
    val magicDefenceMult: Double = 1.0,
    val overheadProtection: OverheadProtection? = null,
)

/**
 * Combat style categories for overhead (protection) prayers.
 * In PvM: reduces incoming damage by 40%.
 * In PvP: reduces incoming damage to 0.
 * Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers
 */
enum class OverheadProtection { MELEE, RANGED, MAGIC }

/**
 * Groups prayers by the combat stat they affect, used for mutual exclusivity
 * enforcement in [PrayerSet.activate].
 * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
 */
enum class PrayerCategory {
    ATTACK,
    STRENGTH,
    DEFENCE,
    RANGED,
    MAGIC,
    OVERHEAD,
    UTILITY,
}

/**
 * Determines which exclusive category a prayer belongs to.
 * Only one prayer per category may be active at a time.
 * Utility prayers (Rapid Restore, Rapid Heal, etc.) stack freely.
 */
fun Prayer.category(def: PrayerDefinition): PrayerCategory = when {
    def.overheadProtection != null                       -> PrayerCategory.OVERHEAD
    def.attackMult > 1.0 && def.strengthMult > 1.0      -> PrayerCategory.STRENGTH // combined — treat as STRENGTH for exclusivity
    def.attackMult > 1.0                                 -> PrayerCategory.ATTACK
    def.strengthMult > 1.0                               -> PrayerCategory.STRENGTH
    def.defenceMult > 1.0 && def.rangedMult <= 1.0
        && def.magicMult <= 1.0                          -> PrayerCategory.DEFENCE
    def.rangedMult > 1.0 || def.rangedStrengthMult > 1.0 -> PrayerCategory.RANGED
    def.magicMult > 1.0 || def.magicDefenceMult > 1.0   -> PrayerCategory.MAGIC
    else                                                  -> PrayerCategory.UTILITY
}
