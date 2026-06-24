package entity

/**
 * Minimal NPC combat target — HP tracking and defence bonuses only.
 * No AI, pathfinding, or aggression logic yet (npc-behavior-simulator deferred).
 * Source: https://oldschool.runescape.wiki/w/Non-player_character
 */
class Npc(
    val name: String,
    val combatLevel: Int,
    val defenceLevel: Int,
    val defenceStab: Int = 0,
    val defenceSlash: Int = 0,
    val defenceCrush: Int = 0,
    val defenceMagic: Int = 0,
    val defenceRanged: Int = 0,
    maxHp: Int,
) {
    var currentHp: Int = maxHp
        private set

    val isDead: Boolean get() = currentHp <= 0

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
    }
}
