package combat

/**
 * All equipment stat bonuses for a player's loadout.
 * Source: https://oldschool.runescape.wiki/w/Equipment_Stats
 */
data class EquipmentBonuses(
    val attackStab: Int = 0,
    val attackSlash: Int = 0,
    val attackCrush: Int = 0,
    val attackMagic: Int = 0,
    val attackRanged: Int = 0,
    val meleeStrength: Int = 0,
    val rangedStrength: Int = 0,
    val magicStrength: Int = 0,
    val defenceStab: Int = 0,
    val defenceSlash: Int = 0,
    val defenceCrush: Int = 0,
    val defenceMagic: Int = 0,
    val defenceRanged: Int = 0,
    val prayerBonus: Int = 0,
) {
    companion object {
        val ZERO = EquipmentBonuses()
    }

    operator fun plus(other: EquipmentBonuses): EquipmentBonuses = EquipmentBonuses(
        attackStab = attackStab + other.attackStab,
        attackSlash = attackSlash + other.attackSlash,
        attackCrush = attackCrush + other.attackCrush,
        attackMagic = attackMagic + other.attackMagic,
        attackRanged = attackRanged + other.attackRanged,
        meleeStrength = meleeStrength + other.meleeStrength,
        rangedStrength = rangedStrength + other.rangedStrength,
        magicStrength = magicStrength + other.magicStrength,
        defenceStab = defenceStab + other.defenceStab,
        defenceSlash = defenceSlash + other.defenceSlash,
        defenceCrush = defenceCrush + other.defenceCrush,
        defenceMagic = defenceMagic + other.defenceMagic,
        defenceRanged = defenceRanged + other.defenceRanged,
        prayerBonus = prayerBonus + other.prayerBonus,
    )
}

/**
 * Looks up real equipment bonuses from [item.ItemDefinitions].
 * Returns [EquipmentBonuses.ZERO] for any item not found (including when the
 * osrsbox data file is absent — graceful-absent pattern).
 *
 * Note: [EquipmentBonuses.magicStrength] maps to [item.ItemDefinition.magicDamage]
 * (different field names, same semantic value).
 */
object ItemBonusRegistry {
    fun getBonuses(itemId: Int): EquipmentBonuses {
        val def = item.ItemDefinitions.getOrEmpty(itemId)
        return EquipmentBonuses(
            attackStab = def.attackStab,
            attackSlash = def.attackSlash,
            attackCrush = def.attackCrush,
            attackMagic = def.attackMagic,
            attackRanged = def.attackRanged,
            meleeStrength = def.meleeStrength,
            rangedStrength = def.rangedStrength,
            magicStrength = def.magicDamage,
            defenceStab = def.defenceStab,
            defenceSlash = def.defenceSlash,
            defenceCrush = def.defenceCrush,
            defenceMagic = def.defenceMagic,
            defenceRanged = def.defenceRanged,
            prayerBonus = def.prayer,
        )
    }
}
