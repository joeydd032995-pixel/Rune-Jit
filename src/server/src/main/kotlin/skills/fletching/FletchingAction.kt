package skills.fletching

import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One fletching action: consumes primary+target items, produces output, awards XP.
 * Scheduled once and does not re-schedule (one action per item-use).
 * Source: https://oldschool.runescape.wiki/w/Fletching
 */
class FletchingAction(
    private val player: Player,
    private val recipe: FletchingRecipe,
    private val tickQueue: TickQueue,
    private val defs: FletchingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false
        active = false

        val level = player.skills.getBoostedLevel(Skill.FLETCHING)
        if (level < recipe.levelRequired) return false

        if (!player.inventory.contains(recipe.primaryItemId)) return false
        if (!player.inventory.contains(recipe.targetItemId)) return false

        player.inventory.remove(recipe.primaryItemId, 1)
        player.inventory.remove(recipe.targetItemId, 1)
        player.inventory.addItem(recipe.outputItemId, recipe.outputQty)
        player.skills.addXp(Skill.FLETCHING, recipe.xp)

        return false
    }

    fun cancel() {
        active = false
    }
}
