package entity

import combat.EquipmentBonuses
import combat.ItemBonusRegistry
import engine.TickQueue

/** Minimal player entity. Fields expanded as further skills and systems are implemented. */
class Player(
    val username: String,
    val tickQueue: TickQueue,
) {
    val skills = SkillSet()
    val inventory = Inventory()

    /** Player's tile coordinate — used for Woodcutting Guild boundary detection. */
    var x: Int = 0
    var y: Int = 0

    /**
     * Whether the player is currently inside the Woodcutting Guild boundaries.
     * Stub returns false until world-region-loader implements boundary checks.
     * Source: https://oldschool.runescape.wiki/w/Woodcutting_Guild (+7% WC XP)
     */
    val isInWoodcuttingGuild: Boolean get() = false

    /** Item IDs of equipped items by slot index (0=head … 12=ammo). */
    val equipment = IntArray(14) { -1 }

    // -------------------------------------------------------------------------
    // HP tracking — Source: https://oldschool.runescape.wiki/w/Hitpoints
    // -------------------------------------------------------------------------

    var currentHp: Int = skills.getLevel(Skill.HITPOINTS)
        private set

    val isDead: Boolean get() = currentHp <= 0

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
    }

    fun heal(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(skills.getLevel(Skill.HITPOINTS))
    }

    /**
     * Sums equipment bonuses across all 14 equipment slots via [ItemBonusRegistry].
     * Returns ZERO until /import-osrsbox-complete populates the item DB.
     */
    fun getEquipmentBonuses(): EquipmentBonuses =
        equipment.fold(EquipmentBonuses.ZERO) { acc, id ->
            if (id >= 0) acc + ItemBonusRegistry.getBonuses(id) else acc
        }
}
