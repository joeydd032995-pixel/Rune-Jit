package entity

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
}
