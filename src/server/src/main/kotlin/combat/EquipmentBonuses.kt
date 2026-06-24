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
 * Returns ZERO bonuses for all items until the item DB is loaded
 * by /import-osrsbox-complete.
 */
object ItemBonusRegistry {
    fun getBonuses(itemId: Int): EquipmentBonuses = EquipmentBonuses.ZERO
}
