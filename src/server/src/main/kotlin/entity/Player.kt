package entity

import combat.EquipmentBonuses
import combat.ItemBonusRegistry
import engine.TickQueue
import movement.MovementQueue
import prayer.PrayerDrainAction
import prayer.PrayerSet

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

    /** Plane / floor level (0..3). 0 = ground floor. */
    var plane: Int = 0

    /** Packed world coordinate (x, y, plane) as a [world.Coordinate] value. */
    val coordinate: world.Coordinate get() = world.Coordinate(x, y, plane)

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

    /**
     * Active network session for this player. Set by LoginHandler after successful login.
     * Null when the player is not connected (e.g. during unit tests or offline processing).
     */
    var session: net.GameSession? = null

    // -------------------------------------------------------------------------
    // Prayer — Source: https://oldschool.runescape.wiki/w/Prayer
    // -------------------------------------------------------------------------

    /**
     * Prayer state for this player: active prayers, prayer points, and drain accumulator.
     * The [PrayerDrainAction] recurring tick event drains prayer points each tick
     * based on active prayers and equipment prayer bonus.
     * Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
     */
    val prayer: PrayerSet = PrayerSet { skills.getLevel(Skill.PRAYER) }

    init {
        // Schedule the per-tick prayer drain action. Reschedules itself each tick.
        // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
        tickQueue.schedule(1, PrayerDrainAction(this))
    }

    // -------------------------------------------------------------------------
    // Movement — wired by pathfinding-engineer (Agent 2)
    // Source: https://oldschool.runescape.wiki/w/Walking
    // -------------------------------------------------------------------------

    /**
     * Movement queue for this player. Lazy to avoid allocation for offline/test players
     * that never move. Drains one step (tile) per tick (walk = 1 tile/tick @600ms).
     *
     * Use [movement.walkTo] to start moving along a [world.pathfinding.Path].
     * Use [movement.reset] to stop movement immediately (teleport, stun, death).
     */
    val movement: MovementQueue by lazy { MovementQueue(this, tickQueue) }
}
