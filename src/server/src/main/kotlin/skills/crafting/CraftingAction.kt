package skills.crafting

import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One crafting cycle: verifies requirements, consumes inputs, produces output, and awards XP.
 * Crafting is deterministic — there is no RNG success roll.
 * Rescheduled every [CraftingMeta.ticksPerAttempt] ticks while the player has materials.
 * Source: https://oldschool.runescape.wiki/w/Crafting
 */
class CraftingAction(
    private val player: Player,
    private val recipe: RecipeDef,
    private val tickQueue: TickQueue,
    private val defs: CraftingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        val craftingLevel = player.skills.getBoostedLevel(Skill.CRAFTING)

        if (craftingLevel < recipe.levelRequired) {
            active = false
            return false
        }

        // Verify all tools are present (tools are not consumed)
        for (toolId in recipe.tools) {
            if (!player.inventory.contains(toolId)) {
                active = false
                return false
            }
        }

        // Verify all inputs are present before consuming any
        for (ingredient in recipe.inputs) {
            if (!player.inventory.contains(ingredient.itemId)) {
                active = false
                return false
            }
        }

        // Consume inputs
        for (ingredient in recipe.inputs) {
            player.inventory.remove(ingredient.itemId, ingredient.qty)
        }

        // Produce output — stop if inventory is full
        if (player.inventory.isFull()) {
            active = false
            return false
        }
        player.inventory.addItem(recipe.outputItemId, recipe.outputQty)

        // Award XP — exact wiki value, no approximation
        // Source: recipe.wikiUrl (loaded from crafting.yaml per server-skills.md)
        player.skills.addXp(Skill.CRAFTING, recipe.xp)

        // Continue if player still has all inputs for another craft
        val hasMore = recipe.inputs.all { player.inventory.contains(it.itemId) }
        if (!hasMore) {
            active = false
            return false
        }

        tickQueue.schedule(defs.meta.ticksPerAttempt, this)
        return true
    }

    fun cancel() {
        active = false
    }
}
