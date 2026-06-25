package npc

data class NpcDefinition(
    val id: Int,
    val name: String,
    val hitpoints: Int = 0,
    val attackLevel: Int = 1,
    val strengthLevel: Int = 1,
    val defenceLevel: Int = 1,
    val magicLevel: Int = 1,
    val rangedLevel: Int = 1,
    val combatLevel: Int = 0,
    val aggressive: Boolean = false,
    val members: Boolean = false,
    val examine: String = "",
) {
    companion object {
        val EMPTY = NpcDefinition(id = -1, name = "")
    }
}
