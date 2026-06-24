package prayer

/**
 * All 30 standard OSRS prayers.
 * Data (drain rates, multipliers, level requirements) is loaded from
 * data/prayers/standard.yaml by [PrayerDefs] at startup — not hardcoded here.
 *
 * Source: https://oldschool.runescape.wiki/w/Prayer#Standard_prayers
 */
enum class Prayer {
    // Tier 1 — levels 1–7
    THICK_SKIN,
    BURST_OF_STRENGTH,
    CLARITY_OF_THOUGHT,

    // Tier 2 — levels 10–19
    ROCK_SKIN,
    SUPERHUMAN_STRENGTH,
    IMPROVED_REFLEXES,
    RAPID_RESTORE,

    // Tier 3 — levels 22–27
    RAPID_HEAL,
    PROTECT_ITEM,
    HAWK_EYE,
    MYSTIC_WILL,

    // Tier 4 — levels 28–35
    STEEL_SKIN,
    ULTIMATE_STRENGTH,
    INCREDIBLE_REFLEXES,
    PROTECT_FROM_SUMMONING,

    // Overhead protection — levels 37–43
    PROTECT_FROM_MAGIC,
    PROTECT_FROM_RANGED,
    PROTECT_FROM_MELEE,

    // Tier 5 — levels 44–52
    EAGLE_EYE,
    MYSTIC_LORE,
    RETRIBUTION,
    REDEMPTION,
    SMITE,

    // Tier 6 — levels 55–60
    PRESERVE,
    CHIVALRY,

    // High-level prayers — levels 45–77
    MYSTIC_MIGHT,
    WRATH,
    PIETY,
    RIGOUR,
    AUGURY,
}
