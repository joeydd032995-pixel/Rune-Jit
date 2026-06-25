package item

data class ItemDefinition(
    val id: Int,
    val name: String,
    val examine: String = "",
    val members: Boolean = false,
    val stackable: Boolean = false,
    val noted: Boolean = false,
    val equipable: Boolean = false,
    val weight: Double = 0.0,
    val attackStab: Int = 0,
    val attackSlash: Int = 0,
    val attackCrush: Int = 0,
    val attackMagic: Int = 0,
    val attackRanged: Int = 0,
    val defenceStab: Int = 0,
    val defenceSlash: Int = 0,
    val defenceCrush: Int = 0,
    val defenceMagic: Int = 0,
    val defenceRanged: Int = 0,
    val meleeStrength: Int = 0,
    val rangedStrength: Int = 0,
    val magicDamage: Int = 0,
    val prayer: Int = 0,
) {
    companion object {
        val EMPTY = ItemDefinition(id = -1, name = "")
    }
}
