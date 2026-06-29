package skills.construction

import engine.TickEvent
import entity.Player
import entity.Skill

/**
 * Single furniture-build action: waits [ConstructionConfig.meta.ticksPerBuild] ticks,
 * consumes materials, and awards XP. Non-repeating — returns false after one completion.
 *
 * The plugin schedules this immediately after level/tool/material pre-checks pass,
 * so [process] only re-validates to guard against concurrent inventory changes.
 *
 * Source: https://oldschool.runescape.wiki/w/Construction#Building_furniture
 */
class ConstructionAction(
    private val player: Player,
    private val furniture: FurnitureDef,
    private val defs: ConstructionConfig,
) : TickEvent {

    private var processed = false

    override fun process(currentTick: Long): Boolean {
        if (processed) return false
        processed = true

        // Re-validate level in case of drain since scheduling
        if (player.skills.getBoostedLevel(Skill.CONSTRUCTION) < furniture.levelRequired) return false

        // Re-validate all materials still present (inventory may have changed)
        for (mat in furniture.materials) {
            if (!player.inventory.contains(mat.itemId, mat.quantity)) return false
        }

        // Consume materials and award XP
        for (mat in furniture.materials) {
            player.inventory.remove(mat.itemId, mat.quantity)
        }
        player.skills.addXp(Skill.CONSTRUCTION, furniture.xp)

        return false  // one-shot; never reschedule
    }

    fun cancel() {
        processed = true
    }
}
