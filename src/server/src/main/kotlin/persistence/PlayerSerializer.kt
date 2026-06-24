package persistence

import entity.Player
import entity.Skill

/**
 * Converts between a live [Player] instance and its [PlayerSave] snapshot.
 *
 * Serialize: called before writing to disk — captures a consistent point-in-time snapshot.
 * Deserialize: called after loading from disk — restores state onto a freshly-constructed Player.
 *
 * Design notes:
 * - Inventory slots are restored by index via [entity.Inventory.setSlot] to preserve exact slot
 *   positions (OSRS preserves item order in the inventory).
 * - Prayer points are restored via [prayer.PrayerSet.restorePoints] because prayerPoints has a
 *   private setter; restorePoints clamps to the player's current Prayer level, which is correct
 *   since skill XP is restored first.
 * - currentHp is stored for completeness but cannot be set directly (private setter on Player).
 *   On login the server resets HP to the Prayer level equivalent via the heal path; full HP
 *   restoration from save requires a Player.restoreHp() method (deferred, pending HP system).
 *
 * Source: server-persistence.md — Atomic Writes
 */
object PlayerSerializer {

    /**
     * Snapshots [player] into a [PlayerSave] suitable for JSON serialisation.
     *
     * XP values: exact doubles from [entity.SkillSet.getXp], keyed by [Skill.name].
     * Inventory: 28-entry list; null for empty slots (preserves slot positions).
     * Equipment: 14-entry list; -1 for empty slots.
     */
    fun serialize(player: Player): PlayerSave {
        val skillXp = Skill.entries.associate { skill ->
            skill.name to player.skills.getXp(skill)
        }
        val inventory = (0 until 28).map { slot ->
            player.inventory.getSlot(slot)?.let { SavedItem(it.itemId, it.quantity) }
        }
        return PlayerSave(
            username = player.username,
            x = player.x,
            y = player.y,
            plane = player.plane,
            skillXp = skillXp,
            prayerPoints = player.prayer.prayerPoints,
            inventory = inventory,
            equipment = player.equipment.toList(),
            currentHp = player.currentHp,
        )
    }

    /**
     * Restores [save] onto [player].
     *
     * Caller contract: [player] must be a freshly-constructed instance (all XP=0, inventory empty).
     * Restoring onto a player with existing XP will add the delta, not overwrite — see notes in
     * the XP block below.
     *
     * Order of restoration matters:
     *   1. Position (safe, no dependencies).
     *   2. Skill XP (sets derived levels; must precede prayer point restoration so Prayer level
     *      is correct before restorePoints() clamps).
     *   3. Prayer points (clamped to restored Prayer level by restorePoints()).
     *   4. Inventory slots (by index, preserving visual order).
     *   5. Equipment array (direct index write).
     */
    fun deserialize(save: PlayerSave, player: Player) {
        // 1. Position
        player.x = save.x
        player.y = save.y
        player.plane = save.plane

        // 2. Skill XP — addXp adds the delta above current (which is 0 for a fresh Player).
        //    Skill enum is in entity package; unknown skill names in the save are ignored
        //    gracefully so that old saves survive skill renames during development.
        save.skillXp.forEach { (skillName, savedXp) ->
            val skill = Skill.entries.firstOrNull { it.name == skillName } ?: return@forEach
            val current = player.skills.getXp(skill)
            val delta = savedXp - current
            if (delta > 0.0) player.skills.addXp(skill, delta)
        }

        // 3. Prayer points — restorePoints() clamps to Prayer level (restored above).
        //    A full-points save restores to the player's Prayer level, which matches OSRS behaviour
        //    (prayer points restored at altar, not carried over death beyond 0).
        player.prayer.restorePoints(save.prayerPoints)

        // 4. Inventory — restore by slot index to preserve item positions exactly.
        save.inventory.forEachIndexed { slot, savedItem ->
            if (savedItem != null) {
                player.inventory.setSlot(slot, savedItem.itemId, savedItem.quantity)
            }
        }

        // 5. Equipment — direct array write; -1 = empty slot (Player.equipment initialises to -1).
        save.equipment.forEachIndexed { slot, itemId ->
            if (slot < player.equipment.size) {
                player.equipment[slot] = itemId
            }
        }
    }
}
